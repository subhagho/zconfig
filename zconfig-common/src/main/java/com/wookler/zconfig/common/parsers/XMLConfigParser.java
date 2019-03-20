/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Copyright (c) $year
 * Date: 2/2/19 11:15 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.parsers;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.wookler.zconfig.common.*;
import com.wookler.zconfig.common.model.*;
import com.wookler.zconfig.common.model.nodes.*;
import com.wookler.zconfig.common.readers.AbstractConfigReader;
import com.wookler.zconfig.common.readers.EReaderType;
import com.wookler.zconfig.common.utils.IOUtils;
import com.wookler.zconfig.common.utils.RemoteFileHelper;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * Configuration Parser implementation that reads the configuration from a XML file.
 */
public class XMLConfigParser extends AbstractConfigParser {
    /**
     * Parse and load the configuration instance using the specified properties.
     *
     * @param name     - Configuration name being loaded.
     * @param reader   - Configuration reader handle to read input from.
     * @param settings - Configuration Settings to use for parsing.
     * @param version  - Configuration version to load.
     * @throws ConfigurationException
     */
    @Override
    public void parse(String name, AbstractConfigReader reader,
                      ConfigurationSettings settings, Version version)
    throws ConfigurationException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
        Preconditions.checkArgument(reader != null);
        Preconditions.checkArgument(version != null);

        try {
            if (!reader.isOpen()) {
                reader.open();
            }

            if (settings != null) {
                this.settings = settings;
            } else {
                this.settings = new ConfigurationSettings();
            }

            try (InputStream stream = reader.getInputStream()) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setValidating(true);
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(stream);

                // optional, but recommended
                // read this -
                // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
                doc.getDocumentElement().normalize();

                Element rootNode = doc.getDocumentElement();

                configuration = new Configuration(this.settings);
                configuration.getState().setState(ENodeState.Loading);
                configuration.setName(name);

                parseHeader(rootNode, version);

                NodeList children = rootNode.getChildNodes();
                for (int ii = 0; ii < children.getLength(); ii++) {
                    Node nn = children.item(ii);
                    if (nn.getNodeName()
                          .compareTo(XMLConfigConstants.CONFIG_HEADER_NODE) == 0) {
                        continue;
                    }
                    if (!(nn instanceof Element)) {
                        continue;
                    }
                    parseBody((Element) nn);
                    break;
                }

                doPostLoad();
            }
        } catch (SAXException e) {
            if (configuration != null)
                configuration.getState().setError(e);
            throw new ConfigurationException(e);
        } catch (IOException e) {
            if (configuration != null)
                configuration.getState().setError(e);
            throw new ConfigurationException(e);
        } catch (ParserConfigurationException e) {
            if (configuration != null)
                configuration.getState().setError(e);
            throw new ConfigurationException(e);
        } catch (ConfigurationException e) {
            if (configuration != null)
                configuration.getState().setError(e);
            throw e;
        }
    }

    /**
     * Parse the XML body to create the configuration nodes.
     *
     * @param node - Root node of the configuration.
     * @throws ConfigurationException
     */
    private void parseBody(Element node) throws ConfigurationException {
        ConfigPathNode rootNode = new ConfigPathNode(configuration, null);
        rootNode.setName(node.getNodeName());
        configuration.setRootConfigNode(rootNode);

        NodeList children = node.getChildNodes();
        if (children != null && children.getLength() > 0) {
            for (int ii = 0; ii < children.getLength(); ii++) {
                Node nn = children.item(ii);
                if (!(nn instanceof Element)) {
                    continue;
                }
                parseNode((Element) nn, rootNode);
            }
        }
    }

    /**
     * Parse the XML Node and generate corresponding Configuration nodes.
     *
     * @param node   - XML Node Element.
     * @param parent - Parent Config Node.
     * @throws ConfigurationException
     */
    private void parseNode(Element node, AbstractConfigNode parent)
    throws ConfigurationException {
        String name = node.getNodeName();
        short nodeListType = isListNode(node);
        if (nodeListType == Node.ELEMENT_NODE) {
            if (!(parent instanceof ConfigPathNode)) {
                throw new ConfigurationException(String.format(
                        "Cannot add Config Node List to parent : [parent=%s][path=%s]",
                        parent.getClass().getCanonicalName(),
                        parent.getAbsolutePath()));
            }
            if (!(node instanceof Element)) {
                throw new ConfigurationException(String.format(
                        "Expecting XML Element node : [type=%s][path=%s]",
                        node.getClass().getCanonicalName(),
                        parent.getAbsolutePath()));
            }
            ConfigListElementNode nodeList =
                    new ConfigListElementNode(configuration, parent);
            nodeList.setName(node.getNodeName());
            ((ConfigPathNode) parent).addChildNode(nodeList);
            parseChildren(node, nodeList);
        } else if (nodeListType == Node.TEXT_NODE) {
            if (!(parent instanceof ConfigPathNode)) {
                throw new ConfigurationException(String.format(
                        "Cannot add Config Value List to parent : [parent=%s][path=%s]",
                        parent.getClass().getCanonicalName(),
                        parent.getAbsolutePath()));
            }
            if (!(node instanceof Element)) {
                throw new ConfigurationException(String.format(
                        "Expecting XML Element node : [type=%s][path=%s]",
                        node.getClass().getCanonicalName(),
                        parent.getAbsolutePath()));
            }
            ConfigListValueNode nodeList =
                    new ConfigListValueNode(configuration, parent);
            nodeList.setName(node.getNodeName());
            ((ConfigPathNode) parent).addChildNode(nodeList);
            parseChildren(node, nodeList);
        } else if (node.getNodeType() == Node.ELEMENT_NODE && !isTextNode(node)) {
            if (parent instanceof ConfigListElementNode) {
                ConfigPathNode pnode =
                        new ConfigPathNode(configuration, parent);
                pnode.setName(node.getNodeName());
                ((ConfigListElementNode) parent).addValue(pnode);
                parseChildren(node, pnode);
            } else {
                if (!(parent instanceof ConfigPathNode)) {
                    throw new ConfigurationException(String.format(
                            "Cannot add Config Node to parent : [parent=%s][path=%s]",
                            parent.getClass().getCanonicalName(),
                            parent.getAbsolutePath()));
                }

                String nodeName = node.getNodeName();
                if (nodeName.compareTo(settings.getPropertiesNodeName()) == 0) {
                    ConfigPropertiesNode pnode =
                            new ConfigPropertiesNode(configuration, parent);
                    pnode.setName(settings.getPropertiesNodeName());
                    ((ConfigPathNode) parent).addChildNode(pnode);
                    parseChildren(node, pnode);
                } else if (nodeName.compareTo(settings.getParametersNodeName()) ==
                        0) {
                    ConfigParametersNode pnode =
                            new ConfigParametersNode(configuration, parent);
                    pnode.setName(settings.getParametersNodeName());
                    ((ConfigPathNode) parent).addChildNode(pnode);
                    parseChildren(node, pnode);
                } else if (nodeName.compareTo(settings.getAttributesNodeName()) ==
                        0) {
                    ConfigAttributesNode pnode =
                            new ConfigAttributesNode(configuration, parent);
                    pnode.setName(settings.getAttributesNodeName());
                    ((ConfigPathNode) parent).addChildNode(pnode);
                    parseChildren(node, pnode);
                } else if (nodeName.compareTo(ConfigIncludeNode.NODE_NAME) == 0) {
                    ConfigIncludeNode pnode =
                            new ConfigIncludeNode(configuration, parent);
                    ((ConfigPathNode) parent).addChildNode(pnode);
                    parseIncludeNode(node, pnode);
                } else if (nodeName.compareTo(ConfigResourceNode.NODE_NAME) == 0) {
                    EResourceType type = parseResourceType(node);
                    if (type == null) {
                        throw ConfigurationException.propertyNotFoundException(
                                ConfigResourceNode.NODE_RESOURCE_TYPE);
                    }
                    ConfigResourceFile pnode = null;
                    if (type == EResourceType.FILE) {
                        pnode = new ConfigResourceFile(configuration, parent);
                        ((ConfigPathNode) parent).addChildNode(pnode);
                        parseResourceFileNode(node, pnode);
                    } else if (type == EResourceType.BLOB) {
                        pnode = new ConfigResourceBlob(configuration, parent);
                        ((ConfigPathNode) parent).addChildNode(pnode);
                        parseResourceFileNode(node, pnode);
                    } else if (type == EResourceType.DIRECTORY) {
                        pnode = new ConfigResourceDirectory(configuration, parent);
                        ((ConfigPathNode) parent).addChildNode(pnode);
                        parseResourceDirNode(node, (ConfigResourceDirectory) pnode);
                    }
                    pnode.setName(nodeName);
                } else {
                    ConfigPathNode pnode =
                            new ConfigPathNode(configuration, parent);
                    pnode.setName(node.getNodeName());
                    ((ConfigPathNode) parent).addChildNode(pnode);
                    parseChildren(node, pnode);
                }
            }
        } else if (node.getNodeType() == Node.ELEMENT_NODE && isTextNode(node)) {
            ConfigValueNode vn = new ConfigValueNode(configuration, parent);
            vn.setName(node.getNodeName());
            String value = node.getTextContent();
            value = value.trim();
            if (!Strings.isNullOrEmpty(value)) {
                vn.setValue(value);
            }
            if (parent instanceof ConfigPathNode) {
                ((ConfigPathNode) parent).addChildNode(vn);
            } else if (parent instanceof ConfigListValueNode) {
                ((ConfigListValueNode) parent).addValue(vn);
            } else if (parent instanceof ConfigKeyValueNode) {
                ((ConfigKeyValueNode) parent)
                        .addKeyValue(vn.getName(), vn.getValue());
            } else {
                throw new ConfigurationException(String.format(
                        "Cannot add ConfigValue to parent : [parent=%s][path=%s]",
                        parent.getClass().getCanonicalName(),
                        parent.getAbsolutePath()));
            }
        }
    }

    /**
     * Extract and parse the resource node type.
     *
     * @param node - JsonNode to extract from.
     * @return - Parsed Resource node type.
     * @throws ConfigurationException
     */
    private EResourceType parseResourceType(Element node)
    throws ConfigurationException {
        String nt = node.getAttribute(ConfigResourceNode.NODE_RESOURCE_TYPE);
        if (Strings.isNullOrEmpty(nt)) {
            throw ConfigurationException.propertyNotFoundException(
                    ConfigResourceNode.NODE_RESOURCE_TYPE);
        }
        return EResourceType.valueOf(nt);
    }

    /**
     * Parse a Resource Directory node.
     *
     * @param node     - XML Node to parse
     * @param resource - Config Directory resource handle.
     * @throws ConfigurationException
     */
    private void parseResourceDirNode(Element node,
                                      ConfigResourceDirectory resource)
    throws ConfigurationException {
        setupResourceNode(node, resource);
        URI uri = resource.getLocation();
        if (uri == null) {
            throw ConfigurationException.propertyNotFoundException("location");
        }
        if (IOUtils.isLocalFile(uri)) {
            File file = Paths.get(uri).toFile();
            if (!file.exists()) {
                throw new ConfigurationException(String.format(
                        "Specified resource file not found : [path=%s]",
                        file.getAbsolutePath()));
            }
            resource.setResourceHandle(file);
        } else {
            String filename = String.format("%s/%s", settings.getTempDirectory(), resource.getResourceName());
            File file = new File(filename);
            IOUtils.CheckDirectory(file.getAbsolutePath());
            resource.setResourceHandle(file);
            if (!file.exists()) {
                if (configuration.getSettings().getDownloadRemoteFiles() ==
                        ConfigurationSettings.EStartupOptions.OnStartUp) {
                    EReaderType type =
                            EReaderType.parseFromUri(resource.getLocation());
                    Preconditions.checkNotNull(type);

                    if (type == EReaderType.HTTP || type == EReaderType.HTTPS) {
                        try {
                            long bread = RemoteFileHelper
                                    .downloadRemoteDirectory(resource.getLocation(),
                                                             resource.getResourceHandle());
                            if (bread <= 0) {
                                throw new ConfigurationException(String.format(
                                        "No bytes read for remote file. [url=%s]",
                                        uri.toString()));
                            }
                        } catch (IOException e) {
                            throw new ConfigurationException(e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Parse a Resource File/Blob node.
     *
     * @param node     - XML Node to parse
     * @param resource - Config File/Blob resource handle.
     * @throws ConfigurationException
     */
    private void parseResourceFileNode(Element node, ConfigResourceFile resource)
    throws ConfigurationException {
        setupResourceNode(node, resource);
        URI uri = resource.getLocation();
        if (uri == null) {
            throw ConfigurationException.propertyNotFoundException("location");
        }
        if (IOUtils.isLocalFile(uri)) {
            File file = Paths.get(uri).toFile();
            if (!file.exists()) {
                throw new ConfigurationException(String.format(
                        "Specified resource file not found : [path=%s]",
                        file.getAbsolutePath()));
            }
            resource.setResourceHandle(file);
        } else {
            String filename = String.format("%s/%s", settings.getTempDirectory(), resource.getResourceName());
            File file = new File(filename);
            IOUtils.CheckParentDirectory(file.getAbsolutePath());
            resource.setResourceHandle(file);
            if (!file.exists()) {
                if (configuration.getSettings().getDownloadRemoteFiles() ==
                        ConfigurationSettings.EStartupOptions.OnStartUp) {
                    EReaderType type =
                            EReaderType.parseFromUri(resource.getLocation());
                    Preconditions.checkNotNull(type);

                    if (type == EReaderType.HTTP || type == EReaderType.HTTPS) {
                        try {
                            long bread = RemoteFileHelper
                                    .downloadRemoteFile(resource.getLocation(),
                                                        resource.getResourceHandle());
                            if (bread <= 0) {
                                throw new ConfigurationException(String.format(
                                        "No bytes read for remote file. [url=%s]",
                                        uri.toString()));
                            }
                            LogUtils.debug(getClass(), String.format(
                                    "Downloaded remote file. [path=%s][size=%s]",
                                    file.getAbsolutePath(), bread));
                        } catch (IOException e) {
                            throw new ConfigurationException(e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Setup the common resource information.
     *
     * @param node     - XML Node to parse
     * @param resource - Config resource handle.
     * @throws ConfigurationException
     */
    private void setupResourceNode(Element node, ConfigResourceNode resource)
    throws ConfigurationException {
        if (!node.hasAttributes()) {
            throw new ConfigurationException(
                    String.format("No attributes found : [path=%s]",
                                  node.toString()));
        }
        String attr = node.getAttribute(ConfigResourceNode.NODE_RESOURCE_TYPE);
        if (Strings.isNullOrEmpty(attr)) {
            throw ConfigurationException.propertyNotFoundException(
                    ConfigResourceNode.NODE_RESOURCE_TYPE);
        }
        EResourceType type = EResourceType.valueOf(attr);
        if (type == null) {
            throw new ConfigurationException(String.format(
                    "Invalid Resource Type : [type=%s]", attr));
        }
        resource.setType(type);

        attr = node.getAttribute(ConfigResourceNode.NODE_RESOURCE_NAME);
        if (Strings.isNullOrEmpty(attr)) {
            throw ConfigurationException.propertyNotFoundException(
                    ConfigResourceNode.NODE_RESOURCE_NAME);
        }
        resource.setResourceName(attr);
        if (!node.hasChildNodes()) {
            throw new ConfigurationException(
                    String.format("No child nodes found : [path=%s]",
                                  node.toString()));
        }
        NodeList children = node.getChildNodes();
        for (int ii = 0; ii < children.getLength(); ii++) {
            Node cnode = children.item(ii);
            if (!isTextNode(cnode)) {
                continue;
            }
            if (node instanceof Element) {
                String name = cnode.getNodeName();
                if (name.compareTo(ConfigResourceNode.NODE_RESOURCE_URL) == 0) {
                    String url = cnode.getTextContent().trim();
                    if (Strings.isNullOrEmpty(url)) {
                        throw ConfigurationException.propertyNotFoundException(
                                ConfigResourceNode.NODE_RESOURCE_URL);
                    }
                    try {
                        resource.setLocation(new URI(url));
                    } catch (URISyntaxException e) {
                        throw new ConfigurationException(e);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Parse a included configuration node.
     *
     * @param node   - XML Node Element.
     * @param parent - Config Include Node.
     * @throws ConfigurationException
     */
    private void parseIncludeNode(Element node, ConfigIncludeNode parent)
    throws ConfigurationException {
        String attr = node.getAttribute(ConfigIncludeNode.NODE_CONFIG_NAME);
        if (Strings.isNullOrEmpty(attr)) {
            throw ConfigurationException
                    .propertyNotFoundException(ConfigIncludeNode.NODE_CONFIG_NAME);
        }
        parent.setConfigName(attr);

        attr = node.getAttribute(ConfigIncludeNode.NODE_PATH);
        if (Strings.isNullOrEmpty(attr)) {
            throw ConfigurationException
                    .propertyNotFoundException(ConfigIncludeNode.NODE_PATH);
        }
        parent.setPath(attr);

        attr = node.getAttribute(ConfigIncludeNode.NODE_TYPE);
        if (Strings.isNullOrEmpty(attr)) {
            throw ConfigurationException
                    .propertyNotFoundException(ConfigIncludeNode.NODE_TYPE);
        }
        EReaderType type = EReaderType.parse(attr);
        if (type == null) {
            throw new ConfigurationException(
                    String.format("Invalid Reader Type : [value=%s]", attr));
        }
        parent.setReaderType(type);
        attr = node.getAttribute(ConfigIncludeNode.NODE_VERSION);
        if (Strings.isNullOrEmpty(attr)) {
            throw ConfigurationException
                    .propertyNotFoundException(ConfigIncludeNode.NODE_VERSION);
        }
        try {
            Version version = Version.parse(attr);
            parent.setVersion(version);
        } catch (ValueParseException e) {
            throw new ConfigurationException(e);
        }

        URI uri = parent.getURI();
        if (uri == null) {
            throw new ConfigurationException(
                    "Error getting URI for include node.");
        }
        AbstractConfigReader reader = ConfigProviderFactory.reader(uri);
        if (reader == null) {
            throw new ConfigurationException(
                    String.format("Error getting reader instance : [URI=%s]",
                                  uri.toString()));
        }
        XMLConfigParser nparser = new XMLConfigParser();
        nparser.parse(parent.getConfigName(), reader, settings,
                      parent.getVersion());
        if (nparser.configuration != null) {
            ConfigPathNode configPathNode =
                    nparser.configuration.getRootConfigNode();
            parent.setNode(configPathNode);
            configPathNode.changeConfiguration(configuration);
        } else {
            throw new ConfigurationException(String.format(
                    "Error loading included configuration. [URI=%s]",
                    uri.toString()));
        }
    }

    /**
     * Parse the child nodes for the passed XML Element.
     *
     * @param node   - XML Node Element.
     * @param parent - Parent Config Node.
     * @throws ConfigurationException
     */
    private void parseChildren(Element node, AbstractConfigNode parent)
    throws ConfigurationException {
        if (node.hasChildNodes()) {
            NodeList nodeList = node.getChildNodes();
            for (int ii = 0; ii < nodeList.getLength(); ii++) {
                Node nn = nodeList.item(ii);
                if (nn.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                parseNode((Element) nn, parent);
            }
        }
    }

    /**
     * Check if the node is basically a Text Node wrapper.
     *
     * @param node - Node to check.
     * @return - Is Text Node?
     */
    private boolean isTextNode(Node node) {
        if ((node instanceof Element) && node.hasChildNodes()) {
            NodeList children = node.getChildNodes();
            if (children.getLength() == 1) {
                if (children.item(0).getNodeType() == Node.TEXT_NODE) {
                    String value = children.item(0).getTextContent();
                    // Empty element nodes also have empty text
                    value = value.trim();
                    if (!Strings.isNullOrEmpty(value))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if the passed node is a List node of type String/Elements.
     *
     * @param node - XML Node to check for.
     * @return - Node Type if List else -1
     */
    private short isListNode(Node node) {
        String nodeName = node.getNodeName();
        if (nodeName.compareTo(settings.getAttributesNodeName()) == 0 ||
                nodeName.compareTo(settings.getParametersNodeName()) == 0 ||
                nodeName.compareTo(settings.getPropertiesNodeName()) == 0) {
            return -1;
        }
        if (node.hasChildNodes()) {
            NodeList children = node.getChildNodes();
            if (children.getLength() > 1) {
                short nodeType = -1;
                String name = null;
                int count = 0;
                for (int ii = 0; ii < children.getLength(); ii++) {
                    Node nn = children.item(ii);
                    if (nn.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    if (!nn.hasChildNodes()) {
                        return -1;
                    }
                    if (Strings.isNullOrEmpty(name)) {
                        name = nn.getNodeName();
                        nodeType = nn.getNodeType();
                        if (isTextNode(nn)) {
                            nodeType = Node.TEXT_NODE;
                        }
                        continue;
                    }
                    short nt = nn.getNodeType();
                    if (isTextNode(nn)) {
                        nt = Node.TEXT_NODE;
                    }
                    String nname = nn.getNodeName();
                    if (nodeType != nt || name.compareTo(nname) != 0) {
                        return -1;
                    }
                    count++;
                }
                if (count > 0)
                    return nodeType;
            }
        }
        return -1;
    }

    /**
     * Parse the Configuration header information from the passed Node Element.
     * Will also do a version compatibility check.
     *
     * @param node    - XML Node Element.
     * @param version - Expected Version.
     * @throws ConfigurationException
     */
    private void parseHeader(Element node, Version version)
    throws ConfigurationException {
        NodeList hnode =
                node.getElementsByTagName(XMLConfigConstants.CONFIG_HEADER_NODE);
        if (hnode == null || hnode.getLength() == 0) {
            throw ConfigurationException
                    .propertyNotFoundException(
                            XMLConfigConstants.CONFIG_HEADER_NODE);
        }
        try {
            Element header = (Element) hnode.item(0);
            if (header.hasAttribute(XMLConfigConstants.CONFIG_HEADER_ID)) {
                String id = header.getAttribute(XMLConfigConstants.CONFIG_HEADER_ID);
                Preconditions.checkState(!Strings.isNullOrEmpty(id));
                configuration.setId(id);
            }
            if (header.hasAttribute(
                            XMLConfigConstants.CONFIG_HEADER_NAME)) {
                String name = header.getAttribute(XMLConfigConstants.CONFIG_HEADER_NAME);
                Preconditions.checkState(!Strings.isNullOrEmpty(name));
                configuration.setName(name);
            }
            if (header.hasAttribute(
                            XMLConfigConstants.CONFIG_HEADER_GROUP) ) {
                String name = header.getAttribute(
                        XMLConfigConstants.CONFIG_HEADER_GROUP);
                Preconditions.checkState(!Strings.isNullOrEmpty(name));
                configuration.setApplicationGroup(name);
            }
            if (header.hasAttribute(XMLConfigConstants.CONFIG_HEADER_APP) ) {
                String name = header.getAttribute(XMLConfigConstants.CONFIG_HEADER_APP) ;
                Preconditions.checkState(!Strings.isNullOrEmpty(name));
                configuration.setApplication(name);
            }
            if (header.hasAttribute(
                            XMLConfigConstants.CONFIG_HEADER_VERSION) ) {
                String vstring = header.getAttribute(
                        XMLConfigConstants.CONFIG_HEADER_VERSION) ;
                Preconditions.checkState(!Strings.isNullOrEmpty(vstring));
                Version cversion = Version.parse(vstring);
                // Check version compatibility
                if (!version.isCompatible(cversion)) {
                    throw new ConfigurationException(String.format(
                            "Incompatible Configuration Version. [expected=%s][actual=%s]",
                            version.toString(), cversion.toString()));
                }
                configuration.setVersion(cversion);
            }
            NodeList children = header.getChildNodes();
            if (children != null && children.getLength() > 0) {
                for (int ii = 0; ii < children.getLength(); ii++) {
                    Node nn = children.item(ii);
                    if (nn.getNodeName()
                                 .compareTo(XMLConfigConstants.CONFIG_CREATED_BY) ==
                            0) {
                        ModifiedBy modifiedBy = parseUpdateInfo((Element) nn);
                        if (modifiedBy == null) {
                            throw ConfigurationException
                                    .propertyNotFoundException(
                                            XMLConfigConstants.CONFIG_CREATED_BY);
                        }
                        configuration.setCreatedBy(modifiedBy);
                    } else if (nn.getNodeName()
                                 .compareTo(XMLConfigConstants.CONFIG_UPDATED_BY) ==
                            0) {
                        ModifiedBy modifiedBy = parseUpdateInfo((Element) nn);
                        if (modifiedBy == null) {
                            throw ConfigurationException
                                    .propertyNotFoundException(
                                            XMLConfigConstants.CONFIG_UPDATED_BY);
                        }
                        configuration.setUpdatedBy(modifiedBy);
                    } else if (nn.getNodeName()
                                 .compareTo(
                                         XMLConfigConstants.CONFIG_HEADER_DESC) ==
                            0) {
                        String name = nn.getTextContent();
                        Preconditions.checkState(!Strings.isNullOrEmpty(name));
                        configuration.setDescription(name);
                    }
                }
            }
        } catch (ValueParseException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Parse an Update Info node from the XML Element.
     *
     * @param node - XML Node Element.
     * @return - Update Info.
     * @throws ConfigurationException
     */
    private ModifiedBy parseUpdateInfo(Element node)
    throws ConfigurationException {
        if (node.hasAttributes()) {
            ModifiedBy mb = new ModifiedBy();
            if (node.hasAttribute(XMLConfigConstants.CONFIG_CREATED_BY)) {
                String user = node.getAttribute(XMLConfigConstants.CONFIG_CREATED_BY);
                Preconditions.checkState(!Strings.isNullOrEmpty(user));
                mb.setModifiedBy(user);
            }
            if (node.hasAttribute(XMLConfigConstants.CONFIG_UPDATE_TIMESTAMP)) {
                String ts = node.getAttribute(XMLConfigConstants.CONFIG_UPDATE_TIMESTAMP);
                Preconditions.checkState(!Strings.isNullOrEmpty(ts));
                long dt = Long.parseLong(ts);
                mb.setTimestamp(dt);
            }
            return mb;
        }
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
