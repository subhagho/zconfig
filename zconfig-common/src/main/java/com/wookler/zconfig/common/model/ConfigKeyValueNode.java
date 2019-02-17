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
 * Date: 31/12/18 7:41 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.wookler.zconfig.common.ConfigurationException;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract Configuration node represents Key/Value pairs.
 */
public abstract class ConfigKeyValueNode extends ConfigElementNode
        implements IVersionedNode<String> {
    /**
     * Current change version of this node.
     */
    private String changeVersion;

    /**
     * Map containing the defined parameters within a node definition.
     */
    private Map<String, String> keyValues;

    /**
     * Get the current version of this node.
     *
     * @return - Current Version.
     */
    @Override
    public String getVersion() {
        return changeVersion;
    }

    /**
     * Set the current version of this node.
     *
     * @param version - Current Version.
     */
    @Override
    public void setVersion(@Nonnull String version) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(version));
        changeVersion = version;
    }

    /**
     * Get the defined parameters for a specific node.
     *
     * @return - Map of parameters.
     */
    public Map<String, String> getKeyValues() {
        return keyValues;
    }

    /**
     * Set the parameters for a specific node.
     *
     * @param keyValues - Map of parameters.
     */
    public void setKeyValues(Map<String, String> keyValues) {
        updated();
        this.keyValues = keyValues;
    }

    /**
     * Get the value for the specified key.
     *
     * @param key - Parameter key.
     * @return - Parameter value or NULL if not found.
     */
    @JsonIgnore
    public String getValue(String key) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key));
        if (keyValues != null) {
            return keyValues.get(key);
        }
        return null;
    }

    /**
     * Check if the specified key exists in the Map.
     *
     * @param key - Key to look for.
     * @return - Exists?
     */
    public boolean hasKey(String key) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key));
        if (keyValues != null) {
            return keyValues.containsKey(key);
        }
        return false;
    }

    /**
     * Check if this node is empty (doesn't have any key/values).
     *
     * @return - Is empty?
     */
    public boolean isEmpty() {
        return (keyValues == null || keyValues.isEmpty());
    }

    /**
     * Add all the key/values to this map.
     *
     * @param map - Key/Value map.
     */
    public void addAll(Map<String, String> map) {
        Preconditions.checkArgument(map != null);
        if (!map.isEmpty()) {
            if (keyValues == null) {
                keyValues = new HashMap<>(map);
            } else {
                keyValues.putAll(map);
            }
        }
    }

    /**
     * Add a new key/value with the specified key and value.
     *
     * @param key   - Parameter key.
     * @param value - Parameter value.
     */
    public void addKeyValue(String key, String value) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key));
        if (keyValues == null) {
            keyValues = new HashMap<>();
        }
        keyValues.put(key, value);
        updated();
    }

    /**
     * Remove the key/value with the specified key.
     *
     * @param key - Parameter key.
     * @return - True if removed, else NULL.
     */
    public boolean removeKeyValue(String key) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key));
        if (keyValues != null && !keyValues.isEmpty()) {
            if (keyValues.containsKey(key)) {
                keyValues.remove(key);
                updated();
                return true;
            }
        }
        return false;
    }

    /**
     * Prints the key/value pairs.
     *
     * @return - Key/Value pairs string.
     */
    @Override
    public String toString() {
        return String.format("%s:[key/values=%s]", keyValues);
    }

    /**
     * Update the state of this node.
     *
     * @param state - New state.
     */
    @Override
    public void updateState(ENodeState state) {
        getState().setState(state);
    }

    /**
     * Update the state of this node as Synced.
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
    }
}
