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
 * Date: 3/2/19 12:03 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.wookler.zconfig.common.model.*;
import com.wookler.zconfig.common.model.annotations.ConfigParam;
import com.wookler.zconfig.common.model.annotations.ConfigPath;
import com.wookler.zconfig.common.model.annotations.ConfigValue;
import com.wookler.zconfig.common.utils.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * Helper class to auto-apply configuration values to annotated types.
 */
public class ConfigurationAnnotationHandler {
    /**
     * Reader and apply the values from the passed configuration based on the type annotations.
     *
     * @param type   - Type of the target object.
     * @param config - Configuration source.
     * @param target - Target to apply the values to.
     * @param <T>    - Annotated object type.
     * @return - Updated target instance.
     * @throws ConfigurationException
     */
    public static <T> T readConfigAnnotations(Class<? extends T> type,
                                              Configuration config, T target)
    throws ConfigurationException {
        Preconditions.checkArgument(config != null);
        Preconditions.checkArgument(target != null);
        Preconditions.checkArgument(type != null);

        ConfigPath cPath = type.getAnnotation(ConfigPath.class);
        if (cPath != null) {
            String path = cPath.path();
            if (Strings.isNullOrEmpty(path)) {
                throw new ConfigurationException(
                        "Invalid Config Path : Path is NULL/Empty");
            }
            AbstractConfigNode node = config.find(path);
            if (node == null) {
                throw new ConfigurationException(
                        String.format("Invalid Path : Path not found. [path=%s]",
                                      path));
            }
            Field[] fields = ReflectionUtils.getAllFields(type);
            if (fields != null && fields.length > 0) {
                for (Field field : fields) {
                    processField(type, node, target, field);
                }
            }
        }
        return target;
    }

    /**
     * Check and apply any annotations for the specified field.
     *
     * @param type   - Type of the target object.
     * @param node   - Extracted configuration node.
     * @param target - Target to apply the values to.
     * @param field  - Field to check and apply to.
     * @param <T>    - Annotated object type.
     * @throws ConfigurationException
     */
    private static <T> void processField(Class<? extends T> type,
                                         AbstractConfigNode node, T target,
                                         Field field)
    throws ConfigurationException {
        if (ReflectionUtils.isPrimitiveTypeOrString(field)) {
            if (field.isAnnotationPresent(ConfigParam.class)) {
                ConfigParam cParam = field.getAnnotation(ConfigParam.class);
                String name = cParam.name();
                if (Strings.isNullOrEmpty(name)) {
                    name = field.getName();
                }
                String value = null;
                if (node instanceof ConfigPathNode) {
                    ConfigPathNode pathNode = (ConfigPathNode) node;
                    ConfigParametersNode params = pathNode.parmeters();
                    if (params != null && !params.isEmpty()) {
                        value = params.getValue(name);
                    }
                }
                if (!Strings.isNullOrEmpty(value)) {
                    ReflectionUtils.setValueFromString(value, target, field);
                } else if (cParam.required()) {
                    throw new ConfigurationException(String.format(
                            "Required parameter not specified: [path=%s][name=%s]",
                            node.getAbsolutePath(), name));
                }
            } else if (field.isAnnotationPresent(ConfigValue.class)) {
                ConfigValue cValue = field.getAnnotation(ConfigValue.class);
                String name = cValue.name();
                if (Strings.isNullOrEmpty(name)) {
                    name = field.getName();
                }
                String value = null;
                if (node instanceof ConfigPathNode) {
                    AbstractConfigNode fnode = node.find(name);
                    if (fnode != null) {
                        if (fnode instanceof ConfigValueNode) {
                            ConfigValueNode cv = (ConfigValueNode) fnode;
                            value = cv.getValue();
                        }
                    }
                }
                if (!Strings.isNullOrEmpty(value)) {
                    ReflectionUtils.setValueFromString(value, target, field);
                } else if (cValue.required()) {
                    throw new ConfigurationException(String.format(
                            "Required configuration value not specified: [path=%s][name=%s]",
                            node.getAbsolutePath(), name));
                }
            }
        }
    }
}
