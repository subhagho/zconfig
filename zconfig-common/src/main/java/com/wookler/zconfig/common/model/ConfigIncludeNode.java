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
 * Date: 1/1/19 8:50 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.model;

import com.google.common.base.Preconditions;

/**
 * Configuration node that represents an external configuration set to be included
 * under the current path.
 * <p>
 * Included configurations are expected to be complete configuration sets.
 */
public abstract class ConfigIncludeNode extends ConfigElementNode {
    /**
     * Path to the node that should be considered the root node when including under the current path.
     */
    private String rootNodePath;
    /**
     * The included child node.
     */
    private ConfigPathNode node;

    /**
     * Get the root node path.
     *
     * @return - Root node path.
     */
    public String getRootNodePath() {
        return rootNodePath;
    }

    /**
     * Set the root node path.
     *
     * @param rootNodePath - Root node path.
     */
    public void setRootNodePath(String rootNodePath) {
        this.rootNodePath = rootNodePath;
    }

    /**
     * Get the included node.
     *
     * @return - Included node.
     */
    public ConfigPathNode getNode() {
        return node;
    }

    /**
     * Set the included node.
     *
     * @param node - Included node.
     */
    public void setNode(ConfigPathNode node) {
        Preconditions.checkArgument(node != null);
        this.node = node;
    }

    /**
     * Delegate the call to the included node.
     *
     * @param path  - Tokenized Path array.
     * @param index - Current index in the path array to search for.
     * @return
     */
    @Override
    public AbstractConfigNode find(String[] path, int index) {
        return node.find(path, index);
    }

    /**
     * Update the state for this node and the embedded node.
     *
     * @param state - New state.
     */
    @Override
    public void updateState(ENodeState state) {
        getState().setState(state);
        if (node != null) {
            node.updateState(state);
        }
    }
}
