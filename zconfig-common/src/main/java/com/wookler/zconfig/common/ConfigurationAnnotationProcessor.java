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
import com.wookler.zconfig.common.model.annotations.ConfigAttribute;
import com.wookler.zconfig.common.model.annotations.ConfigParam;
import com.wookler.zconfig.common.model.annotations.ConfigPath;
import com.wookler.zconfig.common.model.annotations.ConfigValue;
import com.wookler.zconfig.common.model.nodes.*;
import com.wookler.zconfig.common.utils.CollectionUtils;
import com.wookler.zconfig.common.utils.ReflectionUtils;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Helper class to auto-apply configuration values to annotated types.
 */
public class ConfigurationAnnotationProcessor {
    /**
     * Struct class to pass information about field annotations.
     */
    public static class FieldAnnotation {
        /**
         * Annotated field.
         */
        public Field field;
        /**
         * Annotation type.
         */
        public Annotation annotation;
    }

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
    public static <T> T readConfigAnnotations(@Nonnull Class<? extends T> type,
                                              @Nonnull Configuration config,
                                              @Nonnull T target)
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
     * Reader and apply the values from the passed configuration based on the type annotations.
     *
     * @param type   - Type of the target object.
     * @param config - Configuration source.
     * @param target - Target to apply the values to.
     * @param path   - Node path to search under.
     * @param <T>    - Annotated object type.
     * @return - Updated target instance.
     * @throws ConfigurationException
     */
    public static <T> T readConfigAnnotations(@Nonnull Class<? extends T> type,
                                              @Nonnull Configuration config,
                                              @Nonnull T target,
                                              @Nonnull String path)
    throws ConfigurationException {
        Preconditions.checkArgument(config != null);
        Preconditions.checkArgument(target != null);
        Preconditions.checkArgument(type != null);

        ConfigPath cPath = type.getAnnotation(ConfigPath.class);
        if (cPath != null) {
            path = String.format("%s.%s", path, cPath.path());
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
     * Reader and apply the values from the passed configuration based on the type annotations.
     *
     * @param type   - Type of the target object.
     * @param config - Configuration node source.
     * @param target - Target to apply the values to.
     * @param <T>    - Annotated object type.
     * @return - Updated target instance.
     * @throws ConfigurationException
     */
    public static <T> T readConfigAnnotations(@Nonnull Class<? extends T> type,
                                              @Nonnull ConfigPathNode config,
                                              @Nonnull T target)
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
        try {
            if (canProcessFieldType(field)) {
                if (field.isAnnotationPresent(ConfigParam.class)) {
                    if (!ReflectionUtils.isPrimitiveTypeOrString(field)) {
                        throw new ConfigurationException(String.format(
                                "Parameter cannot be set for field of type = %s",
                                field.getType().getCanonicalName()));
                    }
                    ConfigParam cParam = field.getAnnotation(ConfigParam.class);
                    String name = cParam.name();
                    if (Strings.isNullOrEmpty(name)) {
                        name = field.getName();
                    }
                    if (name.contains(ConfigParametersNode.NODE_ABBR_PREFIX)) {
                        String[] parts =
                                name.split(ConfigParametersNode.NODE_ABBR_PREFIX);
                        if (parts.length == 2) {
                            String path = parts[0];
                            String nname = parts[1];
                            if (Strings.isNullOrEmpty(path)) {
                                name = nname;
                            } else {
                                node = node.find(path);
                                if (node == null) {
                                    throw new ConfigurationException(
                                            String.format(
                                                    "Invalid ConfigParam : path not found. [name=%s]",
                                                    name));
                                }
                                name = nname;
                            }
                        } else {
                            throw new ConfigurationException(
                                    String.format("Invalid ConfigParam : [name=%s]",
                                                  name));
                        }
                    }
                    String value = null;
                    if (node instanceof ConfigPathNode) {
                        ConfigPathNode pathNode = (ConfigPathNode) node;
                        ConfigParametersNode params = pathNode.parmeters();
                        if (params != null && !params.isEmpty()) {
                            if (params.hasKey(name))
                                value = params.getValue(name).getValue();
                        }
                    }
                    if (!Strings.isNullOrEmpty(value)) {
                        ReflectionUtils.setValueFromString(value, target, field);
                    } else if (cParam.required()) {
                        throw new ConfigurationException(String.format(
                                "Required parameter not specified: [path=%s][name=%s]",
                                node.getAbsolutePath(), name));
                    }
                } else if (field.isAnnotationPresent(ConfigAttribute.class)) {
                    if (!ReflectionUtils.isPrimitiveTypeOrString(field)) {
                        throw new ConfigurationException(String.format(
                                "Parameter cannot be set for field of type = %s",
                                field.getType().getCanonicalName()));
                    }
                    ConfigAttribute cParam =
                            field.getAnnotation(ConfigAttribute.class);
                    String name = cParam.name();
                    if (Strings.isNullOrEmpty(name)) {
                        name = field.getName();
                    }
                    if (name.contains(ConfigAttributesNode.NODE_ABBR_PREFIX)) {
                        String[] parts =
                                name.split(ConfigAttributesNode.NODE_ABBR_PREFIX);
                        if (parts.length == 2) {
                            String path = parts[0];
                            String nname = parts[1];
                            if (Strings.isNullOrEmpty(path)) {
                                name = nname;
                            } else {
                                node = node.find(path);
                                if (node == null) {
                                    throw new ConfigurationException(
                                            String.format(
                                                    "Invalid ConfigParam : path not found. [name=%s]",
                                                    name));
                                }
                                name = nname;
                            }
                        } else {
                            throw new ConfigurationException(
                                    String.format("Invalid ConfigParam : [name=%s]",
                                                  name));
                        }
                    }
                    String value = null;
                    if (node instanceof ConfigPathNode) {
                        ConfigPathNode pathNode = (ConfigPathNode) node;
                        ConfigAttributesNode params = pathNode.attributes();
                        if (params != null && !params.isEmpty()) {
                            if (params.hasKey(name))
                                value = params.getValue(name).getValue();
                        }
                    }
                    if (!Strings.isNullOrEmpty(value)) {
                        ReflectionUtils.setValueFromString(value, target, field);
                    } else if (cParam.required()) {
                        throw new ConfigurationException(String.format(
                                "Required attribute not specified: [path=%s][name=%s]",
                                node.getAbsolutePath(), name));
                    }
                } else if (field.isAnnotationPresent(ConfigValue.class)) {
                    ConfigValue cValue = field.getAnnotation(ConfigValue.class);
                    String name = cValue.name();
                    if (Strings.isNullOrEmpty(name)) {
                        name = field.getName();
                    }

                    if (!ReflectionUtils.isPrimitiveTypeOrString(field)) {
                        Class<?> ftype = field.getType();
                        String path = hasConfigAnnotation(ftype);
                        if (!Strings.isNullOrEmpty(path) &&
                                (node instanceof ConfigPathNode)) {
                            Object value = ftype.newInstance();
                            value = readConfigAnnotations(ftype,
                                                          (ConfigPathNode) node,
                                                          value);
                            ReflectionUtils.setObjectValue(target, field, value);
                        } else {
                            throw new ConfigurationException(String.format(
                                    "Parameter cannot be set for field of type = %s",
                                    field.getType().getCanonicalName()));
                        }
                    } else {
                        String value = null;
                        if (node instanceof ConfigPathNode) {
                            AbstractConfigNode fnode = node.find(name);
                            if (fnode != null) {
                                if (fnode instanceof ConfigValueNode) {
                                    ConfigValueNode cv = (ConfigValueNode) fnode;
                                    value = cv.getValue();
                                } else if (fnode instanceof ConfigListValueNode) {
                                    setListValueFromNode(type,
                                                         ((ConfigListValueNode) fnode),
                                                         target, field);
                                }
                            }
                        }
                        if (!Strings.isNullOrEmpty(value)) {
                            if (!ReflectionUtils.isPrimitiveTypeOrString(field)) {
                                throw new ConfigurationException(String.format(
                                        "Parameter cannot be set for field of type = %s",
                                        field.getType().getCanonicalName()));
                            }
                            ReflectionUtils
                                    .setValueFromString(value, target, field);
                        } else if (cValue.required()) {
                            throw new ConfigurationException(String.format(
                                    "Required configuration value not specified: [path=%s][name=%s]",
                                    node.getAbsolutePath(), name));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Set the value of the Collection type based on the passed Value list.
     *
     * @param type          - Class type of the target object.
     * @param listValueNode - List Value node to extract values from.
     * @param target        - Target object to set the values for
     * @param field         - Field in the target object.
     * @param <T>           - Generic type.
     * @throws ConfigurationException
     */
    private static <T> void setListValueFromNode(Class<? extends T> type,
                                                 ConfigListValueNode listValueNode,
                                                 T target, Field field)
    throws ConfigurationException {
        List<String> values = new ArrayList<>(listValueNode.size());
        List<ConfigValueNode> nodes = listValueNode.getValues();
        for (ConfigValueNode cvn : nodes) {
            values.add(cvn.getValue());
        }
        try {
            if (ReflectionUtils.implementsInterface(List.class, field.getType())) {
                CollectionUtils.setListValues(target, field, values);
            } else if (ReflectionUtils
                    .implementsInterface(Set.class, field.getType())) {
                CollectionUtils.setSetValues(target, field, values);
            }
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Check if the specified annotated field can be processed.
     *
     * @param field - Field to check for.
     * @return - Can process?
     * @throws Exception
     */
    private static boolean canProcessFieldType(Field field) throws Exception {
        if (ReflectionUtils.isPrimitiveTypeOrString(field)) {
            return true;
        } else if (ReflectionUtils
                .implementsInterface(List.class, field.getType())) {
            Class<?> itype = ReflectionUtils.getGenericListType(field);
            Preconditions.checkArgument(itype != null);
            if (ReflectionUtils.isPrimitiveTypeOrString(itype)) {
                return true;
            } else if (itype.equals(BigInteger.class) ||
                    itype.equals(BigDecimal.class) || itype.equals(
                    Date.class)) {
                return true;
            }
        } else if (ReflectionUtils
                .implementsInterface(Set.class, field.getType())) {
            Class<?> itype = ReflectionUtils.getGenericSetType(field);
            Preconditions.checkArgument(itype != null);
            if (ReflectionUtils.isPrimitiveTypeOrString(itype)) {
                return true;
            } else if (itype.equals(BigInteger.class) ||
                    itype.equals(BigDecimal.class) || itype.equals(
                    Date.class)) {
                return true;
            }
        } else {
            Class<?> type = field.getType();
            String ann = hasConfigAnnotation(type);
            if (!Strings.isNullOrEmpty(ann)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check and get the path annotation from the passed type.
     *
     * @param type - Type to check annotation for.
     * @param <T>  - Generic type.
     * @return - Config Path annotation value.
     */
    public static <T> String hasConfigAnnotation(Class<? extends T> type) {
        ConfigPath cPath = type.getAnnotation(ConfigPath.class);
        if (cPath != null) {
            return cPath.path();
        }
        return null;
    }

    /**
     * Get all the field configuration annotations for this type.
     *
     * @param type - Type to get annotation for.
     * @param <T>  - Generic type.
     * @return - List of field/annotation
     */
    public static <T> List<FieldAnnotation> getFieldAnnotations(
            Class<? extends T> type) {
        String path = hasConfigAnnotation(type);
        if (!Strings.isNullOrEmpty(path)) {
            List<FieldAnnotation> annotations = new ArrayList<>();
            Field[] fields = ReflectionUtils.getAllFields(type);
            if (fields != null && fields.length > 0) {
                for (Field field : fields) {
                    if (field.isAnnotationPresent(ConfigParam.class)) {
                        ConfigParam param = field.getAnnotation(ConfigParam.class);
                        FieldAnnotation fa = new FieldAnnotation();
                        fa.field = field;
                        fa.annotation = param;
                        annotations.add(fa);
                    } else if (field.isAnnotationPresent(ConfigValue.class)) {
                        ConfigValue param = field.getAnnotation(ConfigValue.class);
                        FieldAnnotation fa = new FieldAnnotation();
                        fa.field = field;
                        fa.annotation = param;
                        annotations.add(fa);
                    } else if (field.isAnnotationPresent(ConfigAttribute.class)) {
                        ConfigAttribute param =
                                field.getAnnotation(ConfigAttribute.class);
                        FieldAnnotation fa = new FieldAnnotation();
                        fa.field = field;
                        fa.annotation = param;
                        annotations.add(fa);
                    }
                }
            }
            if (!annotations.isEmpty()) {
                return annotations;
            }
        }
        return null;
    }
}
