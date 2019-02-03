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
 * Date: 31/12/18 9:02 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.model;

import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration node representing configuration properties. Properties can be defined for any path scope and used as
 * substitutions markers when specifying configuration values.
 * Properties are specified as key/value pairs enclosed in a {properties} block.
 *
 * Example:
 * JSON >>
 * <pre>
 *     properties: {
 *         key1: value1,
 *         key2: value2,
 *         ...
 *     }
 * </pre>
 * XML >>
 * <pre>
 *     <properties>
 *         <key1 value="value1" />
 *         <key2 value="value2" />
 *         ...
 *     </properties>
 * </pre>
 */
public class ConfigPropertiesNode extends ConfigKeyValueNode {
    /**
     * Static Node name for the parameters node.
     */
    public static final String NODE_NAME = "properties";

    /**
     * Override the setName method to set the name to the static node name.
     *
     * @param name - Configuration node name.
     */
    @Override
    public void setName(String name) {
        super.setName(NODE_NAME);
    }

    /**
     * Settings aren't searchable hence method will always return null.
     *
     * @param path - Tokenized Path array.
     * @param index - Current index in the path array to search for.
     * @return - NULL
     */
    @Override
    public AbstractConfigNode find(String[] path, int index) {
        String key = path[index];
        if (!Strings.isNullOrEmpty(key)) {
            if (key.startsWith("@") && (index == path.length - 1)) {
                key = key.substring(1);
                if (Strings.isNullOrEmpty(key)) {
                    return this;
                } else if (hasKey(key)) {
                    String value = getValue(key);
                    ConfigValueNode cv = new ConfigValueNode();
                    cv.setName(NODE_NAME);
                    cv.setValue(value);
                    return cv;
                }
            }
        }
        return null;
    }

    /**
     * Create a copy of this configuration node.
     *
     * @return - Copy of node.
     */
    public ConfigPropertiesNode copy() {
        ConfigPropertiesNode node = new ConfigPropertiesNode();
        node.setName(this.getName());
        node.setNodeVersion(0);
        Map<String, String> properties = new HashMap<>(this.getKeyValues());
        node.setKeyValues(properties);

        return node;
    }
}
