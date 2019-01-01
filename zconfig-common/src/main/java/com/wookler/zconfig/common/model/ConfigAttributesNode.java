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
 * Date: 31/12/18 8:54 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.model;

/**
 * Configuration node representing attributes to be specified for a path node.
 * Attributes are specified as key/value pairs.
 *
 * Example:
 * JSON >>
 * <pre>
 *     node: {
 *         @key1: value1,
 *         @key2: value2,
 *         ...
 *     }
 * </pre>
 * XML >>
 * <pre>
 *     <node key1="value1" key2="value2">
 *         ...
 *         ...
 *     </parameters>
 * </pre>
 */
public class ConfigAttributesNode extends ConfigKeyValueNode {
    /**
     * Static Node name for the parameters node.
     */
    public static final String NODE_NAME = "attributes";

    /**
     * Override the setName method to set the name to the static node name.
     * 
     * @param name - Configuration node name.
     */
    @Override
    public void setName(String name) {
        super.setName(NODE_NAME);
    }
}
