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
 * Date: 31/12/18 8:45 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.model;

import com.google.common.base.Strings;

/**
 * Configuration node representing parameters to be specified for a path node.
 * Parameters are specified as key/value pairs enclosed in a {parameters} block.
 * <p>
 * Example:
 * JSON >>
 * <pre>
 *     parameters: {
 *         key1: value1,
 *         key2: value2,
 *         ...
 *     }
 * </pre>
 * XML >>
 * <pre>
 *     <parameters>
 *         <key1 value="value1" />
 *         <key2 value="value2" />
 *         ...
 *     </parameters>
 * </pre>
 */
public class ConfigParametersNode extends ConfigKeyValueNode {
    /**
     * Static Node name for the parameters node.
     */
    public static final String NODE_NAME = "parameters";

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
     * Check if the path points to a configuration parameters. Will return a local
     * instance of a configuration value node if attribute found..
     *
     * @param path  - Tokenized Path array.
     * @param index - Current index in the path array to search for.
     * @return - Local instance of a value node.
     */
    @Override
    public AbstractConfigNode find(String[] path, int index) {
        String key = path[index];
        if (!Strings.isNullOrEmpty(key)) {
            if (key.startsWith("#") && (index == path.length - 1)) {
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
}
