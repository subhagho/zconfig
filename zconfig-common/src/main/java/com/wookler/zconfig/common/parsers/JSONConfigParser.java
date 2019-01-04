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
 * Date: 2/1/19 10:08 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.parsers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.wookler.zconfig.common.ConfigurationException;
import com.wookler.zconfig.common.DateTimeUtils;
import com.wookler.zconfig.common.ValueParseException;
import com.wookler.zconfig.common.model.*;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class JSONConfigParser extends AbstractConfigParser {
    public static final String PROP_CONFIG_FILE = "config.file";
    public static final String PROP_CONFIG_VERSION = "config.version";

    public static final String CONFIG_HEADER_NODE = "header";
    public static final String CONFIG_HEADER_NAME = "name";
    public static final String CONFIG_HEADER_VERSION = "version";
    public static final String CONFIG_UPDATE_OWNER = "user";
    public static final String CONFIG_UPDATE_TIMESTAMP = "timestamp";
    public static final String CONFIG_CREATED_BY = "createdBy";
    public static final String CONFIG_UPDATED_BY = "updatedBy";
    public static final String CONFIG_NODE_VERSION = "nodeVersion";

    /**
     * Parse the configuration from the JSON file specified in the properties.
     * <p>
     * Sample JSON configuration:
     * <pre>
     *     {
     *         "header" : {
     *             "name" : "[configuration name]",
     *             "version" : "[version string]",
     *             "createdBy" : {
     *                 "user " : "[username]",
     *                 "timestamp" : "[datetime]"
     *             },
     *             "updatedBy" : {
     *                  "user " : "[username]",
     *                 "timestamp" : "[datetime]"
     *             }
     *         },
     *         "configuration" : {
     *              "properties" : {
     *                  "prop1" : "[value1]",
     *                  "prop2" : "[value2]",
     *                  ...
     *              },
     *              "sample" : {
     *                  "name" : "[node name]",
     *                  "version" : "[version string]",
     *                  "createdBy" : {
     *                      "user " : "[username]",
     *                      "timestamp" : "[datetime]"
     *                  },
     *                  "updatedBy" : {
     *                      "user " : "[username]",
     *                      "timestamp" : "[datetime]"
     *                  },
     *                  "parameters" : {
     *                      "param1" : "[value1]",
     *                      "param2" : "[value2]",
     *                      ...
     *                  },
     *                  "sample2": {
     *                      "attributes" : {
     *                          "attr1" : "[value1]",
     *                          ...
     *                          ...
     *                      }
     *                  }
     *             }
     *         }
     *     }
     * </pre>
     *
     * @param name       - Configuration name being loaded.
     * @param properties - Initialization properties. (property: "config.file", "config.version")
     * @throws ConfigurationException
     */
    @Override
    public void parse(String name, Properties properties)
    throws ConfigurationException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
        Preconditions.checkArgument(properties != null);

        String filename = properties.getProperty(PROP_CONFIG_FILE);
        if (Strings.isNullOrEmpty(filename)) {
            throw new ConfigurationException(String.format(
                    "Invalid Initialization Properties : Missing property [property=%s]",
                    PROP_CONFIG_FILE));
        }
        String vstring = properties.getProperty(PROP_CONFIG_VERSION);
        if (Strings.isNullOrEmpty(vstring)) {
            throw new ConfigurationException(String.format(
                    "Invalid Initialization Properties : Missing property [property=%s]",
                    PROP_CONFIG_VERSION));
        }
        try {
            Version version = Version.parse(vstring);

            File file = new File(filename);
            if (!file.exists()) {
                throw new ConfigurationException(String.format(
                        "Invalid Initialization Properties : Configuration file not found. [file=%s]",
                        file.getAbsolutePath()));
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(file);

            parse(name, version, rootNode);


        } catch (JsonProcessingException e) {
            throw new ConfigurationException(e);
        } catch (IOException e) {
            throw new ConfigurationException((e));
        } catch (ValueParseException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Parse the configuration from the specified JSON node.
     *
     * @param name    - Expected Configuration name.
     * @param version - Expected Compatible version.
     * @param node    - JSON Root node.
     * @throws ConfigurationException
     */
    private synchronized void parse(String name, Version version, JsonNode node)
    throws ConfigurationException {
        configuration = new Configuration();
        configuration.getState().setState(ENodeState.Loading);
        configuration.setName(name);

        // Parse the configuration header.
        parseHeader(node, version);

        // Parse the configuration body
        parseBody(node);

        configuration.getRootConfigNode().updateState(ENodeState.Synced);
    }

    /**
     * Parse the JSON body to load the configuration.
     *
     * @param node - JSON node to load from.
     * @throws ConfigurationException
     */
    private void parseBody(JsonNode node) throws ConfigurationException {
        Iterator<Map.Entry<String, JsonNode>> nodes = node.fields();
        if (nodes != null) {
            while (nodes.hasNext()) {
                Map.Entry<String, JsonNode> nn = nodes.next();
                if (nn.getKey().compareTo(CONFIG_HEADER_NODE) == 0) {
                    continue;
                }
                parseConfiguration(nn.getKey(), nn.getValue());
                break;
            }
        }
    }

    /**
     * Read the root configuration node and then the child nodes.
     *
     * @param name - Name of the root node.
     * @param node - Root configuration node.
     * @throws ConfigurationException
     */
    private void parseConfiguration(String name, JsonNode node)
    throws ConfigurationException {
        if (node.getNodeType() != JsonNodeType.OBJECT) {
            throw new ConfigurationException(String.format(
                    "Invalid Configuration Node : [expected=%s][actual=%s]",
                    JsonNodeType.OBJECT.name(), node.getNodeType().name()));
        }
        // Read the root configuration node.
        ConfigPathNode rootConfigNode = new ConfigPathNode();
        rootConfigNode.setName(name);
        rootConfigNode.loading();

        parseNodeHeader(node, rootConfigNode);
        configuration.setRootConfigNode(rootConfigNode);

        // Read the child nodes.
        parseChildNodes(rootConfigNode, node);
    }

    /**
     * Parse the configuration node header information.
     *
     * @param node       - JSON node to read from.
     * @param configNode - Configuration node to set header for.
     * @throws ConfigurationException
     */
    private void parseNodeHeader(JsonNode node, AbstractConfigNode configNode)
    throws ConfigurationException {
        if (!(configNode instanceof ConfigElementNode)) {
            return;
        }
        Iterator<Map.Entry<String, JsonNode>> nodes = node.fields();
        if (nodes != null) {
            while (nodes.hasNext()) {
                Map.Entry<String, JsonNode> nn = nodes.next();
                if (nn.getKey().compareTo(CONFIG_NODE_VERSION) == 0) {
                    JsonNode n = nn.getValue();
                    if (n.getNodeType() != JsonNodeType.NUMBER) {
                        throw new ConfigurationException(String.format(
                                "Invalid node value : [expected type=%s][actual type=%s]",
                                JsonNodeType.NUMBER.name(),
                                n.getNodeType().name()));
                    }
                    ((ConfigElementNode) configNode).setNodeVersion(n.longValue());
                } else if (nn.getKey().compareTo(CONFIG_CREATED_BY) == 0) {
                    ModifiedBy createdby = parseUpdateInfo(node, CONFIG_CREATED_BY);
                    ((ConfigElementNode) configNode).setCreatedBy(createdby);
                } else if (nn.getKey().compareTo(CONFIG_UPDATED_BY) == 0) {
                    ModifiedBy updatedBy = parseUpdateInfo(node, CONFIG_UPDATED_BY);
                    ((ConfigElementNode) configNode).setUpdatedBy(updatedBy);
                }
            }
        }
    }

    /**
     * Parse this JSON node and create a configuration element.
     *
     * @param name   - Element name
     * @param node   - JSON node to read from.
     * @param parent - Parent configuration node.
     * @throws ConfigurationException
     */
    private void parseNode(String name, JsonNode node, AbstractConfigNode parent)
    throws ConfigurationException {
        if (node.getNodeType() == JsonNodeType.OBJECT) {
            AbstractConfigNode nn = readObjectNode(name, node, parent);
            if (nn != null) {
                addToParentNode(parent, nn);
            } else {
                throw new ConfigurationException("Error reading object node.");
            }
        } else if (node.getNodeType() == JsonNodeType.STRING) {
            if (parent instanceof ConfigKeyValueNode) {
                ((ConfigKeyValueNode) parent).addKeyValue(name, node.textValue());
            } else {
                ConfigValue cv = new ConfigValue();
                cv.setName(name);
                cv.setParent(parent);
                cv.setConfiguration(configuration);

                parseNodeHeader(node, cv);

                if (parent instanceof ConfigPathNode) {
                    ((ConfigPathNode) parent).addChildNode(cv);
                } else if (parent instanceof ConfigListValueNode) {
                    ((ConfigListValueNode) parent).addValue(cv);
                } else {
                    throw new ConfigurationException(String.format(
                            "Cannot add string value to parent node. [type=%s]",
                            parent.getClass().getCanonicalName()));
                }
            }
        } else if (node.getNodeType() == JsonNodeType.ARRAY) {
            // Check if array element types are consistent
            JsonNodeType type = null;
            Iterator<Map.Entry<String, JsonNode>> nodes = node.fields();
            if (nodes != null) {
                while (nodes.hasNext()) {
                    Map.Entry<String, JsonNode> nn = nodes.next();
                    if (type == null) {
                        type = nn.getValue().getNodeType();
                    } else {
                        if (nn.getValue().getNodeType() != type) {
                            throw new ConfigurationException(String.format(
                                    "Invalid Array Element : [expected type=%s][actual type=%s]",
                                    type.name(),
                                    nn.getValue().getNodeType().name()));
                        }
                    }
                }
            }
            if (type != null) {
                if (type == JsonNodeType.STRING) {
                    ConfigListValueNode nn = new ConfigListValueNode();
                    setupNodeWithChildren(name, parent, nn, node, false);
                } else if (type == JsonNodeType.OBJECT) {
                    ConfigListElementNode nn = new ConfigListElementNode();
                    setupNodeWithChildren(name, parent, nn, node, false);
                } else {
                    throw new ConfigurationException(String.format(
                            "Unsupported Array element type. [type=%s]",
                            type.name()));
                }
            }
        }
    }

    /**
     * Setup the common node elements.
     *
     * @param name       - Node name.
     * @param configNode - Config node handle.
     * @param parent     - Parent config node.
     * @param node       - JSON node to parse from.
     * @param withHeader - Parse the header data for this node.
     * @throws ConfigurationException
     */
    private void setupNode(String name, AbstractConfigNode configNode,
                           AbstractConfigNode parent, JsonNode node,
                           boolean withHeader)
    throws ConfigurationException {
        configNode.setName(name);
        configNode.setParent(parent);
        configNode.setConfiguration(configuration);

        if (configNode instanceof ConfigElementNode) {
            ((ConfigElementNode) configNode).loading();
            if (withHeader)
                parseNodeHeader(node, configNode);
        }
        addToParentNode(parent, configNode);
    }

    /**
     * Setup the list node and add the list values.
     *
     * @param name       - Name of the List node.
     * @param parent     - Parent config node.
     * @param configNode - New list node to setup.
     * @param node       - JSON node to parse from.
     * @param withHeader - Parse the header data for this node.
     * @throws ConfigurationException
     */
    private void setupNodeWithChildren(String name, AbstractConfigNode parent,
                                       AbstractConfigNode configNode, JsonNode node,
                                       boolean withHeader)
    throws ConfigurationException {
        setupNode(name, configNode, parent, node, withHeader);
        parseChildNodes(configNode, node);
    }

    /**
     * Parse and add the child config nodes.
     *
     * @param parent - Config node to add children to.
     * @param node   - JSON node to read data from.
     * @throws ConfigurationException
     */
    private void parseChildNodes(AbstractConfigNode parent, JsonNode node)
    throws ConfigurationException {
        Iterator<Map.Entry<String, JsonNode>> nnodes = node.fields();
        if (nnodes != null) {
            while (nnodes.hasNext()) {
                Map.Entry<String, JsonNode> sn = nnodes.next();
                parseNode(sn.getKey(), sn.getValue(), parent);
            }
        }
    }

    /**
     * Validate and add the specified node to the parent.
     *
     * @param parent - Parent configuration node.
     * @param node   - Child node to add.
     * @throws ConfigurationException
     */
    private void addToParentNode(AbstractConfigNode parent, AbstractConfigNode node)
    throws ConfigurationException {
        if (parent instanceof ConfigPathNode) {
            ((ConfigPathNode) parent).addChildNode(node);
        } else if (parent instanceof ConfigListElementNode) {
            if (node instanceof ConfigElementNode) {
                ((ConfigListElementNode) parent).addValue((ConfigElementNode) node);
            }
        } else {
            throw new ConfigurationException(String.format(
                    "Cannot add child node to parent node. [type=%s]",
                    parent.getClass().getCanonicalName()));
        }
    }

    /**
     * Read the JSON Object node and parse the config data.
     *
     * @param name   - Config Node name.
     * @param node   - JSON node to read data from.
     * @param parent - Parent config node.
     * @return - Parsed config node.
     * @throws ConfigurationException
     */
    private AbstractConfigNode readObjectNode(String name, JsonNode node,
                                              AbstractConfigNode parent)
    throws ConfigurationException {
        AbstractConfigNode nn = null;
        if (name.compareTo(ConfigPropertiesNode.NODE_NAME) == 0) {
            ConfigPropertiesNode pn = new ConfigPropertiesNode();
            setupNodeWithChildren(name, parent, pn, node, false);
            nn = pn;
        } else if (name.compareTo(ConfigParametersNode.NODE_NAME) == 0) {
            ConfigParametersNode pn = new ConfigParametersNode();
            setupNodeWithChildren(name, parent, pn, node, false);
            nn = pn;
        } else {
            ConfigPathNode pn = new ConfigPathNode();
            setupNodeWithChildren(name, parent, pn, node, true);
            nn = pn;
        }
        return nn;
    }

    /**
     * Read the configuration header information.
     * <p>
     * Header:
     * - Configuration Name
     * - Configuration Version
     * - Created By
     * - Updated By
     *
     * @param node    - Root node to find header under.
     * @param version - Expected Compatibility Version.
     * @throws ConfigurationException
     */
    private void parseHeader(JsonNode node, Version version)
    throws ConfigurationException {
        try {
            JsonNode header = node.get(CONFIG_HEADER_NODE);
            if (header == null) {
                throw ConfigurationException
                        .propertyNotFoundException(CONFIG_HEADER_NODE);
            }
            // Read the configuration name
            JsonNode hname = header.get(CONFIG_HEADER_NAME);
            if (hname == null) {
                throw ConfigurationException
                        .propertyNotFoundException(CONFIG_HEADER_NAME);
            }
            String sname = hname.textValue();
            // Configuration name in resource should match the expected configuration name.
            if (configuration.getName().compareTo(sname) != 0) {
                throw new ConfigurationException(String.format(
                        "Invalid configuration : Name does not match. [expected=%s][actual=%s]",
                        configuration.getName(), sname));
            }
            // Read the configuration version.
            JsonNode vnode = header.get(CONFIG_HEADER_VERSION);
            if (vnode == null) {
                throw ConfigurationException
                        .propertyNotFoundException(CONFIG_HEADER_VERSION);
            }
            String vstring = vnode.textValue();
            Version cversion = Version.parse(vstring);
            // Check version compatibility
            if (!version.isCompatible(cversion)) {
                throw new ConfigurationException(String.format(
                        "Incompatible Configuration Version. [expected=%s][actual=%s]",
                        version.toString(), cversion.toString()));
            }
            configuration.setVersion(cversion);

            // Read the configuration creation info.
            ModifiedBy createdBy = parseUpdateInfo(header, CONFIG_CREATED_BY);
            configuration.setCreatedBy(createdBy);

            // Read the configuration Last updation info.
            ModifiedBy updatedBy = parseUpdateInfo(header, CONFIG_UPDATED_BY);
            configuration.setUpdatedBy(updatedBy);

        } catch (ValueParseException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Read the modification information from the specified JSON node.
     *
     * @param node - JSON node to read info under.
     * @param name - Name of node containing the info.
     * @return - Modification info object.
     * @throws ConfigurationException
     */
    private ModifiedBy parseUpdateInfo(JsonNode node, String name)
    throws ConfigurationException {
        JsonNode inode = node.get(name);
        if (inode == null) {
            throw ConfigurationException.propertyNotFoundException(name);
        }
        JsonNode jn = inode.get(CONFIG_UPDATE_OWNER);
        if (jn == null) {
            throw ConfigurationException
                    .propertyNotFoundException(CONFIG_UPDATE_OWNER);
        }
        String owner = jn.textValue();
        if (Strings.isNullOrEmpty(owner)) {
            throw new ConfigurationException(
                    "Invalid Configuration : Update Owner is NULL/Empty.");
        }
        jn = inode.get(CONFIG_UPDATE_TIMESTAMP);
        if (jn == null) {
            throw ConfigurationException
                    .propertyNotFoundException(CONFIG_UPDATE_TIMESTAMP);
        }
        String timestamp = jn.textValue();
        if (Strings.isNullOrEmpty(timestamp)) {
            throw new ConfigurationException(
                    "Invalid Configuration : Update Timestamp is NULL/Empty.");
        }

        DateTime dt = DateTimeUtils.parse(timestamp);
        ModifiedBy modifiedBy = new ModifiedBy();
        modifiedBy.setModifiedBy(owner);
        modifiedBy.setTimestamp(dt);

        return modifiedBy;
    }

    @Override
    public void write(String path) throws ConfigurationException {

    }
}
