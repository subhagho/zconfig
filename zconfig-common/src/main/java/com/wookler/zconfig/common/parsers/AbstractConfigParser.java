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
 * Date: 2/1/19 9:56 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.parsers;

import com.google.common.base.Strings;
import com.wookler.zconfig.common.ConfigurationException;
import com.wookler.zconfig.common.VariableRegexParser;
import com.wookler.zconfig.common.model.*;
import com.wookler.zconfig.common.readers.AbstractConfigReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for defining configuration parsers.
 */
public abstract class AbstractConfigParser {
    /**
     * Configuration instance handle.
     */
    protected Configuration configuration;

    /**
     * Get the handle to the parsed configuration.
     *
     * @return - Configuration instance.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Method to be called post loading of the configuration.
     * <p>
     * This method updates property values if required.
     *
     * @throws ConfigurationException
     */
    protected void doPostLoad() throws ConfigurationException {
        ConfigPathNode node = configuration.getRootConfigNode();
        if (node != null) {
            Map<String, String> properties = new HashMap<>();
            nodePostLoad(node, properties);
        }
        // Mark the configuration has been loaded.
        configuration.loaded();
    }

    /**
     * Replace variable values with the scoped property sets.
     *
     * @param node - Node to preform replacement on.
     * @param inputProps - Input Property set.
     * @throws ConfigurationException
     */
    private void nodePostLoad(AbstractConfigNode node,
                              Map<String, String> inputProps)
    throws ConfigurationException {
        Map<String, String> properties = new HashMap<>(inputProps);
        if (node instanceof ConfigPathNode) {
            // Get defined properties, if any.
            ConfigPathNode cp = (ConfigPathNode) node;
            ConfigPropertiesNode props = cp.properties();
            if (props != null) {
                Map<String, String> pp = props.getKeyValues();
                if (pp != null && !pp.isEmpty()) {
                    properties.putAll(pp);
                }
            }

            // Do property replacement for all child nodes.
            Map<String, AbstractConfigNode> nodes = cp.getChildren();
            if (nodes != null && !nodes.isEmpty()) {
                for (String key : nodes.keySet()) {
                    nodePostLoad(nodes.get(key), properties);
                }
            }
        } else if (node instanceof ConfigParametersNode) {
            // Check parameter value replacement.
            ConfigParametersNode params = (ConfigParametersNode) node;
            Map<String, String> pp = params.getKeyValues();
            for (String key : pp.keySet()) {
                String value = pp.get(key);
                if (!Strings.isNullOrEmpty(value)) {
                    String nValue = replaceVariables(value, properties);
                    if (value.compareTo(nValue) != 0) {
                        params.addKeyValue(key, nValue);
                    }
                }
            }
        } else if (node instanceof ConfigListElementNode) {
            ConfigListElementNode le = (ConfigListElementNode) node;
            List<ConfigElementNode> nodes = le.getValues();
            if (nodes != null && !nodes.isEmpty()) {
                for (ConfigElementNode nn : nodes) {
                    nodePostLoad(nn, properties);
                }
            }
        } else if (node instanceof ConfigListValueNode) {
            ConfigListValueNode le = (ConfigListValueNode) node;
            List<ConfigValueNode> nodes = le.getValues();
            if (nodes != null && !nodes.isEmpty()) {
                for (ConfigValueNode nn : nodes) {
                    nodePostLoad(nn, properties);
                }
            }
        } else if (node instanceof ConfigValueNode) {
            ConfigValueNode cv = (ConfigValueNode) node;
            String value = cv.getValue();
            if (!Strings.isNullOrEmpty(value)) {
                String nValue = replaceVariables(value, properties);
                if (value.compareTo(nValue) != 0) {
                    cv.setValue(nValue);
                }
            }
        }
    }

    /**
     * Replace all the variables, if any, defined in the value string with the property values specified.
     *
     * @param value      - Value String to replace variables in.
     * @param properties - Property Map to lookup variable values.
     * @return - Replaced String
     */
    private String replaceVariables(String value, Map<String, String> properties) {
        if (!Strings.isNullOrEmpty(value)) {
            if (VariableRegexParser.hasVariable(value)) {
                List<String> vars = VariableRegexParser.getVariables(value);
                if (vars != null && !vars.isEmpty()) {
                    for (String var : vars) {
                        String vv = properties.get(var);
                        if (!Strings.isNullOrEmpty(vv)) {
                            String rv = String.format("\\$\\{%s\\}", var);
                            value = value.replaceAll(rv, vv);
                        }
                    }
                }
            }
        }
        return value;
    }

    /**
     * Parse and load the configuration instance using the specified properties.
     *
     * @param name    - Configuration name being loaded.
     * @param reader  - Configuration reader handle to read input from.
     * @param version - Configuration version to load.
     * @throws ConfigurationException
     */
    public abstract void parse(String name, AbstractConfigReader reader,
                               Version version)
    throws ConfigurationException;

}
