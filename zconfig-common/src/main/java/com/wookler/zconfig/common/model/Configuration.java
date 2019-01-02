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

/**
 * Configuration class that defines a configuration set.
 */
public class Configuration {
    /**
     * Name of this configuration set. Must be globally unique within a configuration server instance.
     */
    private String name;
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
     * Default constructor
     */
    public Configuration() {
        state = new NodeState();
        state.setState(ENodeState.Loading);
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

    public AbstractConfigNode find(AbstractConfigNode node, String path) {
        return null;
    }
}
