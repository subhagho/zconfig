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
 * Date: 1/1/19 8:55 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.wookler.zconfig.common.ConfigurationException;
import com.wookler.zconfig.common.model.nodes.*;

import java.io.File;
import java.util.Map;
import java.util.UUID;

/**
 * Configuration class that defines a configuration set.
 */
public class Configuration {
    /**
     * Unique configuration ID for this configuration/version.
     */
    private String id;
    /**
     * Application Group this configuration belongs to.
     */
    private String applicationGroup;
    /**
     * Application this configuration belong to.
     */
    private String application;
    /**
     * Local instance ID of this configuration handle.
     */
    @JsonIgnore
    private String instanceId;
    /**
     * Name of this configuration set. Must be globally unique within a configuration server instance.
     */
    private String name;
    /**
     * Description of this configuration.
     */
    private String description;
    /**
     * Version of this configuration instance.
     */
    private Version version;
    /**
     * Created By information about this configuration.
     */
    private ModifiedBy createdBy;
    /**
     * Last Modified By information about this configuration.
     */
    private ModifiedBy updatedBy;
    /**
     * Local State of this configuration instance.
     */
    @JsonIgnore
    private NodeState state;
    /**
     * Root configuration node for this configuration.
     */
    private ConfigPathNode rootConfigNode;
    /**
     * Specifies the sync mode for this configuration.
     */
    private ESyncMode syncMode;

    private ConfigurationSettings settings;


    /**
     * Default Empty constructor.
     */
    public Configuration() {
        instanceId = UUID.randomUUID().toString();
        state = new NodeState();
        state.setState(ENodeState.Loading);
    }

    /**
     * Constructor with configuration settings.
     *
     * @param settings - Configuration Settings.
     */
    public Configuration(ConfigurationSettings settings) {
        instanceId = UUID.randomUUID().toString();
        this.settings = settings;
        state = new NodeState();
        state.setState(ENodeState.Loading);
    }

    /**
     * Get the unique ID for this configuration.
     *
     * @return - Unique Configuration ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the unique ID for this configuration.
     *
     * @param id - Unique Configuration ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the Application Group this configuration belongs to.
     *
     * @return - Application Group name.
     */
    public String getApplicationGroup() {
        return applicationGroup;
    }

    /**
     * Set the Application Group this configuration belongs to.
     *
     * @param applicationGroup - Application Group name.
     */
    public void setApplicationGroup(String applicationGroup) {
        this.applicationGroup = applicationGroup;
    }

    /**
     * Get the Application this configuration belongs to.
     *
     * @return - Application name.
     */
    public String getApplication() {
        return application;
    }

    /**
     * Set the Application this configuration belongs to.
     *
     * @param application - Application name.
     */
    public void setApplication(String application) {
        this.application = application;
    }

    /**
     * Get the local instance ID of this configuration.
     *
     * @return - Local instance ID
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Get the name of this configuration.
     *
     * @return - Configuration name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this configuration.
     *
     * @param name - Configuration name.
     */
    public void setName(String name) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
        this.name = name;
    }

    /**
     * Get the description of this configuration.
     *
     * @return - Configuration description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of this configuration.
     *
     * @param description - Configuration description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the version of this configuration instance.
     *
     * @return - Configuration Version.
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Set the version of this configuration instance.
     *
     * @param version - Configuration Version.
     */
    public void setVersion(Version version) {
        Preconditions.checkArgument(version != null);
        this.version = version;
    }

    /**
     * Get the created By info for this configuration.
     *
     * @return - Created By information.
     */
    public ModifiedBy getCreatedBy() {
        return createdBy;
    }

    /**
     * Set the created By info for this configuration.
     *
     * @param createdBy - Created By information.
     */
    public void setCreatedBy(ModifiedBy createdBy) {
        Preconditions.checkArgument(createdBy != null);
        this.createdBy = createdBy;
    }

    /**
     * Get the last modified By info for this configuration.
     *
     * @return - Last Updated By information.
     */
    public ModifiedBy getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Set the last modified By info for this configuration.
     *
     * @param updatedBy - Last Updated By information.
     */
    public void setUpdatedBy(ModifiedBy updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Get the local state of this configuration instance.
     *
     * @return - Local State
     */
    public NodeState getState() {
        return state;
    }

    /**
     * Get the root configuration node.
     *
     * @return - Root configuration node.
     */
    public ConfigPathNode getRootConfigNode() {
        return rootConfigNode;
    }

    /**
     * Set the root configuration node.
     *
     * @param rootConfigNode - Root configuration node.
     */
    public void setRootConfigNode(
            ConfigPathNode rootConfigNode) {
        Preconditions.checkArgument(rootConfigNode != null);
        this.rootConfigNode = rootConfigNode;
    }

    /**
     * Get the Sync mode for this configuration.
     *
     * @return - Update Sync mode.
     */
    public ESyncMode getSyncMode() {
        return syncMode;
    }

    /**
     * Set the Sync mode for this configuration.
     *
     * @param syncMode - Update Sync mode.
     */
    public void setSyncMode(ESyncMode syncMode) {
        this.syncMode = syncMode;
    }

    /**
     * Get the configuration settings used to parse this configuration.
     *
     * @return - Configuration Settings.
     */
    public ConfigurationSettings getSettings() {
        return settings;
    }

    /**
     * Set the configuration settings used to parse this configuration.
     *
     * @param settings - Configuration Settings.
     */
    public void setSettings(ConfigurationSettings settings) {
        this.settings = settings;
    }

    /**
     * Mark this configuration has been completely loaded.
     *
     * @throws ConfigurationException
     */
    public void loaded() throws ConfigurationException {
        if (state.hasError()) {
            throw new ConfigurationException("Configuration in error state.",
                                             state.getError());
        }
        state.setState(ENodeState.Synced);
        if (rootConfigNode != null)
            rootConfigNode.loaded();
    }

    /**
     * Find the configuration node at the specified path under the specified node.
     * <p>
     * Path is specified as a (dot) notation with the following additions.
     * Example:
     * node1.node2.@attr1 - "@" denotes an attribute to search for under the specified path
     * node1.node2.#param1 - "#" denotes a parameter to search for under the specified path
     *
     * @param node - Configuration node to search under.
     * @param path - Path to search for.
     * @return - Configuration Node or NULL.
     */
    public AbstractConfigNode find(AbstractConfigNode node, String path) {
        Preconditions.checkArgument(node != null);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(path));

        String[] parts = path.split("\\.");
        if (parts.length > 0) {
            return node.find(parts, 0);
        }
        return null;
    }

    /**
     * Find the configuration node at the specified path under the root node.
     * <p>
     * Path is specified as a (dot) notation with the following additions.
     * Example:
     * node1.node2.@attr1 - "@" denotes an attribute to search for under the specified path
     * node1.node2.#param1 - "#" denotes a parameter to search for under the specified path
     *
     * @param path - Path to search for.
     * @return - Configuration Node or NULL.
     */
    public AbstractConfigNode find(String path) {
        return find(rootConfigNode, path);
    }

    /**
     * Get the parameters, if any, for the specified node.
     *
     * @param node - Node to search parameters.
     * @return - Parameters Node, else NULL.
     */
    public ConfigParametersNode parameters(AbstractConfigNode node) {
        Preconditions.checkArgument(node != null);
        if (node instanceof ConfigPathNode) {
            ConfigPathNode cp = (ConfigPathNode) node;
            return cp.parmeters();
        }
        return null;
    }

    /**
     * Get the parameter value for the specified parameter name.
     *
     * @param node - Node to find the parameters under.
     * @param name - Parameter name.
     * @return - Parameter value or NULL
     */
    public String parameter(AbstractConfigNode node, String name) {
        Preconditions.checkArgument(node != null);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));

        ConfigParametersNode pnode = parameters(node);
        if (pnode != null) {
            ConfigValueNode vn = pnode.getValue(name);
            if (vn != null) {
                return vn.getValue();
            }
        }
        return null;
    }

    /**
     * Get the properties, if any, for the specified node.
     *
     * @param node - Node to search properties.
     * @return - Properties Node, else NULL.
     */
    public ConfigPropertiesNode properties(AbstractConfigNode node) {
        Preconditions.checkArgument(node != null);
        if (node instanceof ConfigPathNode) {
            ConfigPathNode cp = (ConfigPathNode) node;
            return cp.properties();
        }
        return null;
    }

    /**
     * Get the property value for the specified parameter name.
     *
     * @param node - Node to find the property under.
     * @param name - Property name.
     * @return - Property value or NULL
     */
    public String property(AbstractConfigNode node, String name) {
        Preconditions.checkArgument(node != null);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));

        ConfigPropertiesNode pnode = properties(node);
        if (pnode != null) {
            ConfigValueNode vn = pnode.getValue(name);
            if (vn != null) {
                return vn.getValue();
            }
        }
        return null;
    }

    /**
     * Get all the defined properties till this node. Properties are defined in scope an can be overridden by
     * subsequent nodes. This method will return a set of properties defined till the root node in this nodes
     * ancestor path.
     *
     * @param node - Node to get properties for.
     * @return - Aggregated properties set.
     */
    public ConfigPropertiesNode resolvedProperties(AbstractConfigNode node) {
        Preconditions.checkArgument(node != null);
        node = findParentPath(node);
        if (node instanceof ConfigPathNode) {
            ConfigPropertiesNode props = null;
            ConfigPathNode cp = (ConfigPathNode) node;
            ConfigPropertiesNode pn = cp.properties();
            if (pn != null) {
                props = pn.copy();
            } else {
                props = new ConfigPropertiesNode(this, cp);
                props.setConfiguration(this);
            }
            if (node.getParent() != null) {
                ConfigPropertiesNode pp = resolvedProperties(node.getParent());
                if (pp != null && !pp.isEmpty()) {
                    Map<String, ConfigValueNode> pmap = pp.getKeyValues();
                    if (pmap != null && !pmap.isEmpty()) {
                        if (!props.isEmpty()) {
                            for (String key : pmap.keySet()) {
                                if (!props.hasKey(key)) {
                                    props.addKeyValue(key,
                                                      pmap.get(key).getValue());
                                }
                            }
                        } else {
                            props.addAll(pmap);
                        }
                    }
                }
            }
            return props;
        }
        return null;
    }

    /**
     * Find the closest ancestor node that is a Config Path node.
     *
     * @param node - Node to get path for.
     * @return - Config Path node or NULL.
     */
    public ConfigPathNode findParentPath(AbstractConfigNode node) {
        Preconditions.checkArgument(node != null);
        if (node instanceof ConfigPathNode) {
            return (ConfigPathNode) node;
        } else {
            AbstractConfigNode parent = node.getParent();
            while (parent != null) {
                if (parent instanceof ConfigPathNode) {
                    return (ConfigPathNode) parent;
                }
                parent = parent.getParent();
            }
        }
        return null;
    }

    /**
     * Get the path to the temp directory for this configuration instance.
     * <p>
     * If temp directory doesn't exist, this call will create it.
     *
     * @return - File path to temp directory.
     * @throws ConfigurationException
     */
    public synchronized File getInstancePath() throws ConfigurationException {
        String tmp = System.getProperty("java.io.tmpdir");
        String path = String.format("%s/%s/%s", tmp, name, instanceId);
        File fp = new File(path);
        if (!fp.exists()) {
            fp.mkdirs();
        }
        return fp;
    }
}
