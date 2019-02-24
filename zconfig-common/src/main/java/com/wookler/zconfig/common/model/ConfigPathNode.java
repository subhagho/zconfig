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
 * Date: 31/12/18 7:27 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.wookler.zconfig.common.ConfigurationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Class represents a configuration path node.
 * <p>
 * Configuration path(s) can be represented using a (.) or (/) notation while referencing.
 */
public class ConfigPathNode extends ConfigElementNode {

    /**
     * Map containing the child nodes.
     */
    private Map<String, AbstractConfigNode> children;


    /**
     * Get the map of child nodes for this path element.
     *
     * @return - Map of child nodes.
     */
    public Map<String, AbstractConfigNode> getChildren() {
        return children;
    }

    /**
     * Constructor with Configuration and Parent node.
     *
     * @param configuration - Configuration this node belong to.
     * @param parent - Parent node.
     */
    public ConfigPathNode(Configuration configuration,
                          AbstractConfigNode parent) {
        super(configuration, parent);
    }

    /**
     * Set the map of child nodes for this path element.
     *
     * @param children - Map of child nodes.
     */
    public void setChildren(Map<String, AbstractConfigNode> children) {
        this.children = children;
    }

    /**
     * Add a new child node to this path element.
     * <p>
     * Note: Node names are assumed to be unique, adding a child node with an existing name will
     * override the element if already present.
     *
     * @param node - Child node to add.
     */
    public void addChildNode(AbstractConfigNode node) {
        Preconditions.checkArgument(node != null);

        if (children == null) {
            children = new HashMap<>();
        }
        node.setParent(this);
        children.put(node.getName(), node);
        updated();
    }

    /**
     * Get the child node (if any) with the specified node name.
     *
     * @param name - Node name to search for.
     * @return - Config Node, if found else NULL.
     */
    @JsonIgnore
    public AbstractConfigNode getChildNode(String name) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));

        if (children != null) {
            return children.get(name);
        }
        return null;
    }

    /**
     * Remove the child node with the specified node name.
     *
     * @param name - Node name of the child to remvoe.
     * @return - True if removed, else False.
     */
    public boolean removeChildNode(String name) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
        if (children != null && children.containsKey(name)) {
            children.remove(name);
            updated();
            return true;
        }
        return false;
    }

    /**
     * Get the parameters, if any for this path node.
     *
     * @return - Parameters node, else NULL.
     */
    public ConfigParametersNode parmeters() {
        if (children != null &&
                children.containsKey(ConfigParametersNode.NODE_NAME)) {
            return (ConfigParametersNode) children
                    .get(ConfigParametersNode.NODE_NAME);
        }
        return null;
    }

    /**
     * Get the properties, if any for this path node.
     *
     * @return - Properties node, else NULL.
     */
    public ConfigPropertiesNode properties() {
        if (children != null &&
                children.containsKey(ConfigPropertiesNode.NODE_NAME)) {
            return (ConfigPropertiesNode) children
                    .get(ConfigPropertiesNode.NODE_NAME);
        }
        return null;
    }

    /**
     * Get the properties, if any for this path node.
     *
     * @return - Properties node, else NULL.
     */
    public ConfigAttributesNode attributes() {
        if (children != null &&
                children.containsKey(ConfigAttributesNode.NODE_NAME)) {
            return (ConfigAttributesNode) children
                    .get(ConfigAttributesNode.NODE_NAME);
        }
        return null;
    }

    /**
     * Search for the path under this node.
     *
     * @param path  - Tokenized Path array.
     * @param index - Current index in the path array to search for.
     * @return
     */
    @Override
    public AbstractConfigNode find(String[] path, int index) {
        String key = path[index];
        String pp = hasSubPath(getName(), key);
        if (Strings.isNullOrEmpty(pp)) {
            if (getName().compareTo(key) == 0) {
                if (index == path.length - 1) {
                    return this;
                } else {
                    String cname = path[index + 1];
                    String[] pc = hasSubPath(cname);
                    if (pc != null && pc.length == 2) {
                        cname = pc[0];
                        if (children.containsKey(cname)) {
                            AbstractConfigNode node = children.get(cname);
                            return node.find(pc[1]);
                        }
                    } else if (children.containsKey(cname)) {
                        AbstractConfigNode node = children.get(cname);
                        return node.find(path, index + 1);
                    }
                }
            }
        } else if (index == path.length - 1) {
            return find(pp);
        }
        return null;
    }

    /**
     * Check if the passed node name contains sub-tags for parameters/attributes.
     *
     * @param name - Path element to parse.
     * @return - Parsed path name, if tags are present, else NULL.
     */
    private String[] hasSubPath(String name) {
        int index = name.indexOf("#");
        if (index > 0) {
            String[] parts = name.split("#");
            if (parts.length == 1) {
                return new String[]{parts[0], String
                        .format("%s.%s", parts[0],
                                ConfigParametersNode.NODE_NAME)};
            }
        }
        index = name.indexOf("@");
        if (index > 0) {
            String[] parts = name.split("@");
            if (parts.length == 1) {
                return new String[]{parts[0], String
                        .format("%s.%s", parts[0],
                                ConfigAttributesNode.NODE_NAME)};
            }
        }
        index = name.indexOf("$");
        if (index > 0) {
            String[] parts = name.split("\\$");
            if (parts.length == 1) {
                return new String[]{parts[0], String
                        .format("%s.%s", parts[0],
                                ConfigPropertiesNode.NODE_NAME)};
            }
        }
        return null;
    }

    /**
     * Check if the passed node name contains sub-tags for parameters/attributes.
     *
     * @param nodeName - This node name.
     * @param name     - Path element to parse.
     * @return - Parsed path name, if tags are present, else NULL.
     */
    private String hasSubPath(String nodeName, String name) {
        int index = name.indexOf("#");
        if (index > 0) {
            String[] parts = name.split("#");
            if (nodeName.compareTo(parts[0]) == 0) {
                if (parts.length == 1) {
                    return String
                            .format("%s.%s", parts[0],
                                    ConfigParametersNode.NODE_NAME);
                } else {
                    return String
                            .format("%s.%s.%s", parts[0],
                                    ConfigParametersNode.NODE_NAME,
                                    parts[1]);
                }
            }
        }
        index = name.indexOf("@");
        if (index > 0) {
            String[] parts = name.split("@");
            if (nodeName.compareTo(parts[0]) == 0) {
                if (parts.length == 1) {
                    return String
                            .format("%s.%s", parts[0],
                                    ConfigAttributesNode.NODE_NAME);
                } else {
                    return String
                            .format("%s.%s.%s", parts[0],
                                    ConfigAttributesNode.NODE_NAME,
                                    parts[1]);
                }
            }
        }
        index = name.indexOf("$");
        if (index > 0) {
            String[] parts = name.split("\\$");
            if (nodeName.compareTo(parts[0]) == 0) {
                if (parts.length == 1) {
                    return String
                            .format("%s.%s", parts[0],
                                    ConfigPropertiesNode.NODE_NAME);
                } else {
                    return String
                            .format("%s.%s.%s", parts[0],
                                    ConfigPropertiesNode.NODE_NAME,
                                    parts[1]);
                }
            }
        }
        return null;
    }

    /**
     * Override default toString(). Will print a path element with children if any.
     * Example: /path:[{child}, {child}...]
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer(String.format("/%s", getName()));
        if (children != null && !children.isEmpty()) {
            buff.append(":[");
            boolean first = true;
            for (String key : children.keySet()) {
                AbstractConfigNode node = children.get(key);
                if (node != null) {
                    if (first) {
                        first = false;
                    } else {
                        buff.append(", ");
                    }
                }
                buff.append(String.format("{%s}", node.toString()));
            }
            buff.append("]");
        }
        return buff.toString();
    }

    /**
     * Update the state of this node and all the child nodes.
     *
     * @param state - New state.
     */
    @Override
    public void updateState(ENodeState state) {
        getState().setState(state);

        if (children != null && !children.isEmpty()) {
            for (String key : children.keySet()) {
                AbstractConfigNode node = children.get(key);
                if (node instanceof ConfigElementNode) {
                    ((ConfigElementNode) node).updateState(state);
                }
            }
        }
    }

    /**
     * Update the state of this node as Synced and for all the children.
     *
     * @throws ConfigurationException
     */
    @Override
    public void loaded() throws ConfigurationException {
        if (getState().hasError()) {
            throw new ConfigurationException(String.format(
                    "Cannot mark as loaded : Object state is in error. [state=%s]",
                    getState().getState().name()));
        }
        updateState(ENodeState.Synced);
        if (children != null && !children.isEmpty()) {
            for (String key : children.keySet()) {
                AbstractConfigNode node = children.get(key);
                node.loaded();
            }
        }
    }
}
