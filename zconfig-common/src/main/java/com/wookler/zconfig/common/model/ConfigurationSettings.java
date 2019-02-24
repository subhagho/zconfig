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
 * Date: 24/2/19 11:53 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.model;

import com.google.common.base.Strings;

/**
 * Settings definition for parsing/writing configurations.
 */
public class ConfigurationSettings {
    /**
     * Wildcard for node search.
     */
    public static final String NODE_SEARCH_WILDCARD = "*";

    private static final String DEFAULT_PROPS_NAME = "properties";
    private static final String DEFAULT_ATTR_NAME = "@";
    private static final String DEFAULT_PARAMS_NAME = "parameters";

    private String propertiesNodeName = DEFAULT_PROPS_NAME;
    private String parametersNodeName = DEFAULT_PARAMS_NAME;
    private String attributesNodeName = DEFAULT_ATTR_NAME;

    /**
     * Get the Properties Node name.
     *
     * @return - Properties Node name.
     */
    public String getPropertiesNodeName() {
        return propertiesNodeName;
    }

    /**
     * Set the Properties Node name.
     *
     * @param propertiesNodeName - Properties Node name.
     */
    public void setPropertiesNodeName(String propertiesNodeName) {
        this.propertiesNodeName = propertiesNodeName;
    }

    /**
     * Get the Parameters Node name.
     *
     * @return - Parameters Node name.
     */
    public String getParametersNodeName() {
        return parametersNodeName;
    }

    /**
     * Set the Parameters Node name.
     *
     * @param parametersNodeName - Parameters Node name.
     */
    public void setParametersNodeName(String parametersNodeName) {
        this.parametersNodeName = parametersNodeName;
    }

    /**
     * Get the Attributes Node name.
     *
     * @return - Attributes Node name.
     */
    public String getAttributesNodeName() {
        return attributesNodeName;
    }

    /**
     * Set the Attributes Node name.
     *
     * @param attributesNodeName - Attributes Node name.
     */
    public void setAttributesNodeName(String attributesNodeName) {
        this.attributesNodeName = attributesNodeName;
    }

    /**
     * Check if this name is a wildcard.
     *
     * @param name - Node name.
     * @return - Is Wildcard?
     */
    public static boolean isWildcard(String name) {
        if (!Strings.isNullOrEmpty(name)) {
            if (NODE_SEARCH_WILDCARD.compareTo(name.trim()) == 0) {
                return true;
            }
        }
        return false;
    }
}
