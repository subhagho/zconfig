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
import com.wookler.zconfig.common.ConfigurationException;
import com.wookler.zconfig.common.DateTimeUtils;
import com.wookler.zconfig.common.ValueParseException;
import com.wookler.zconfig.common.XMLConfigConstants;
import com.wookler.zconfig.common.model.*;
import com.wookler.zconfig.common.model.nodes.*;
import com.wookler.zconfig.common.readers.AbstractConfigReader;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Configuration Parser implementation that reads the configuration from a XML file.
 */
public class XmlConfigParser extends AbstractConfigParser {
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
                        throw new ConfigurationException(String.format(
                                "Invalid Configuration Node : [expected=%s][actual=%s]",
                                Element.class.getCanonicalName(),
                                nn.getClass().getCanonicalName()));
                    }
                    parseBody((Element) nn);
                    break;
                }
                configuration.validate();
                configuration.getRootConfigNode().updateState(ENodeState.Synced);
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
                    throw new ConfigurationException(String.format(
                            "Invalid Configuration Node : [expected=%s][actual=%s]",
                            Element.class.getCanonicalName(),
                            nn.getClass().getCanonicalName()));
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
    private void parseNode(Node node, AbstractConfigNode parent)
    throws ConfigurationException {
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
            parseChildren((Element) node, nodeList);
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
            parseChildren((Element) node, nodeList);
        } else if (node.getNodeType() == Node.ELEMENT_NODE) {
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
                ((ConfigPathNode) parent).addChildNode(pnode);
                parseChildren((Element) node, pnode);
            } else if (nodeName.compareTo(settings.getParametersNodeName()) == 0) {
                ConfigParametersNode pnode =
                        new ConfigParametersNode(configuration, parent);
                ((ConfigPathNode) parent).addChildNode(pnode);
                parseChildren((Element) node, pnode);
            } else if (nodeName.compareTo(settings.getAttributesNodeName()) == 0) {
                ConfigAttributesNode pnode =
                        new ConfigAttributesNode(configuration, parent);
                ((ConfigPathNode) parent).addChildNode(pnode);
                parseChildren((Element) node, pnode);
            } else if (nodeName.compareTo(ConfigIncludeNode.NODE_NAME) == 0) {
                ConfigAttributesNode pnode =
                        new ConfigAttributesNode(configuration, parent);
                ((ConfigPathNode) parent).addChildNode(pnode);
                parseChildren((Element) node, pnode);
            } else {
                ConfigPathNode pnode = new ConfigPathNode(configuration, parent);
                pnode.setName(node.getNodeName());
                ((ConfigPathNode) parent).addChildNode(pnode);
                parseChildren((Element) node, pnode);
            }
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            ConfigValueNode vn = new ConfigValueNode(configuration, parent);
            vn.setName(node.getNodeName());
            String value = vn.getValue();
            if (!Strings.isNullOrEmpty(value)) {
                value = value.trim();
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

    private void parseIncludeNode(Element node, AbstractConfigNode parent)
    throws ConfigurationException {

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
                parseNode(nn, parent);
            }
        }
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
                for (int ii = 0; ii < children.getLength(); ii++) {
                    Node nn = children.item(ii);
                    if (Strings.isNullOrEmpty(name)) {
                        name = nn.getNodeName();
                        nodeType = nn.getNodeType();
                        continue;
                    }
                    short nt = nn.getNodeType();
                    String nname = nn.getNodeName();
                    if (nodeType != nt || name.compareTo(nname) != 0) {
                        return -1;
                    }
                }
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
            NodeList children = header.getChildNodes();
            if (children != null && children.getLength() > 0) {
                for (int ii = 0; ii < children.getLength(); ii++) {
                    Node nn = children.item(ii);
                    if (nn.getNodeName()
                          .compareTo(XMLConfigConstants.CONFIG_HEADER_ID) == 0) {
                        String id = nn.getTextContent();
                        Preconditions.checkState(!Strings.isNullOrEmpty(id));
                        configuration.setId(id);
                    } else if (nn.getNodeName()
                                 .compareTo(
                                         XMLConfigConstants.CONFIG_HEADER_NAME) ==
                            0) {
                        String name = nn.getTextContent();
                        Preconditions.checkState(!Strings.isNullOrEmpty(name));
                        configuration.setName(name);
                    } else if (nn.getNodeName()
                                 .compareTo(
                                         XMLConfigConstants.CONFIG_HEADER_GROUP) ==
                            0) {
                        String name = nn.getTextContent();
                        Preconditions.checkState(!Strings.isNullOrEmpty(name));
                        configuration.setApplicationGroup(name);
                    } else if (nn.getNodeName()
                                 .compareTo(XMLConfigConstants.CONFIG_HEADER_APP) ==
                            0) {
                        String name = nn.getTextContent();
                        Preconditions.checkState(!Strings.isNullOrEmpty(name));
                        configuration.setApplication(name);
                    } else if (nn.getNodeName()
                                 .compareTo(
                                         XMLConfigConstants.CONFIG_HEADER_VERSION) ==
                            0) {
                        String vstring = nn.getTextContent();
                        Preconditions.checkState(!Strings.isNullOrEmpty(vstring));
                        Version cversion = Version.parse(vstring);
                        // Check version compatibility
                        if (!version.isCompatible(cversion)) {
                            throw new ConfigurationException(String.format(
                                    "Incompatible Configuration Version. [expected=%s][actual=%s]",
                                    version.toString(), cversion.toString()));
                        }
                        configuration.setVersion(cversion);
                    } else if (nn.getNodeName()
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
        NodeList children = node.getChildNodes();
        if (children != null && children.getLength() > 0) {
            ModifiedBy mb = new ModifiedBy();
            for (int ii = 0; ii < children.getLength(); ii++) {
                Node nn = children.item(ii);
                if (nn.getNodeName()
                      .compareTo(XMLConfigConstants.CONFIG_CREATED_BY) == 0) {
                    String user = nn.getTextContent();
                    Preconditions.checkState(!Strings.isNullOrEmpty(user));
                    mb.setModifiedBy(user);
                } else if (nn.getNodeName()
                             .compareTo(
                                     XMLConfigConstants.CONFIG_UPDATE_TIMESTAMP) ==
                        0) {
                    String ts = nn.getTextContent();
                    Preconditions.checkState(!Strings.isNullOrEmpty(ts));
                    DateTime dt = DateTimeUtils.parse(ts);
                    mb.setTimestamp(dt);
                }
            }
            return mb;
        }
        return null;
    }
}
