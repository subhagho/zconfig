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
 * Date: 1/1/19 10:48 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.model;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.wookler.zconfig.common.ConfigurationException;

import java.util.List;

/**
 * Class represents a configuration node that is a list of configuration elements.
 */
public class ConfigListElementNode extends ConfigListNode<ConfigElementNode> {
    /**
     * Override the add value method, set the parent of the node element being added to this node.
     *
     * @param value - Element value to add.
     */
    @Override
    public void addValue(ConfigElementNode value) {
        Preconditions.checkArgument(value != null);
        value.setParent(this);
        super.addValue(value);
    }

    /**
     * Find the value node in the list with the specified name.
     *
     * @param name - Name of node to find.
     * @return - Value node, else NULL.
     */
    public ConfigElementNode getElement(String name) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
        List<ConfigElementNode> values = getValues();
        if (values != null && !values.isEmpty()) {
            for (ConfigElementNode value : values) {
                if (value != null && value.getName().compareTo(name) == 0) {
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * Search for the configuration node in the list of node elements.
     *
     * @param path  - Tokenized Path array.
     * @param index - Current index in the path array to search for.
     * @return
     */
    @Override
    public AbstractConfigNode find(String[] path, int index) {
        String key = path[index];
        if (getName().compareTo(key) == 0) {
            if (index == path.length - 1) {
                return this;
            } else if (!isEmpty()) {
                index = index + 1;
                List<ConfigElementNode> nodes = getValues();
                for (ConfigElementNode node : nodes) {
                    AbstractConfigNode fn = node.find(path, index);
                    if (fn != null) {
                        return fn;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Update the state of this node and all the nodes in the list.
     *
     * @param state - New state.
     */
    @Override
    public void updateState(ENodeState state) {
        getState().setState(state);

        List<ConfigElementNode> nodes = getValues();
        if (nodes != null && !nodes.isEmpty()) {
            for (ConfigElementNode node : nodes) {
                node.updateState(state);
            }
        }
    }

    /**
     * Mark the configuration instance has been completely loaded.
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
        List<ConfigElementNode> values = getValues();
        if (values != null && !values.isEmpty()) {
            for (ConfigElementNode node : values) {
                node.loaded();
            }
        }
    }
}
