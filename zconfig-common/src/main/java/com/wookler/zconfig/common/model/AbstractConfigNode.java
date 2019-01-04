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
 * Date: 31/12/18 7:12 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Abstract base node for defining configuration elements.
 */
public abstract class AbstractConfigNode {
    /**
     * Configuration instance this node belongs to.
     */
    @JsonIgnore
    private Configuration configuration;
    /**
     * Parent element of this configuration node.
     * <p>
     * Note: Should be null only for the root configuration element.
     */
    @JsonIgnore
    private AbstractConfigNode parent = null;
    /**
     * Name of this configuration node.
     * <p>
     * Note: Name must be unique for a path.
     */
    private String name;



    /**
     * Get the name of this configuration node.
     *
     * @return - Node name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the parent configuration instance.
     *
     * @return - Parent configuration.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Set the parent configuration instance.
     *
     * @param configuration - Parent configuration.
     */
    public void setConfiguration(
            Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Set the name of this configuration node.
     *
     * @param name - Configuration node name.
     */
    public void setName(String name) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
        if (name.indexOf('.') >= 0 || name.indexOf('/') >= 0) {
            throw new RuntimeException(
                    "Invalid name string. Name cannot contain (.) or (/)");
        }
        this.name = name;
    }

    /**
     * Get the parent node for this configuration node.
     *
     * @return - Parent path node or NULL if root node.
     */
    public AbstractConfigNode getParent() {
        return parent;
    }

    /**
     * Set the parent node for this configuration node.
     *
     * @param parent - Parent path node.
     */
    public void setParent(AbstractConfigNode parent) {
        this.parent = parent;
    }

    /**
     * Get the path of this node relative to the root node. Path is represented in the Unix
     * path format.
     *
     * @return - Path string.
     */
    public String getAbsolutePath() {
        String path = null;
        if (parent != null) {
            path = parent.getAbsolutePath();
            path = String.format("%s/%s", path, name);
        } else {
            path = String.format("/%s", name);
        }
        return path;
    }

    /**
     * Override default toString().
     *
     * @return - Node name.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Find a configuration node specified by the path/index.
     *
     * @param path  - Tokenized Path array.
     * @param index - Current index in the path array to search for.
     * @return - Configuration Node found.
     */
    public abstract AbstractConfigNode find(String[] path, int index);
}
