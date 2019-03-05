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
import com.wookler.zconfig.common.model.annotations.*;
import com.wookler.zconfig.common.model.annotations.transformers.NullTransformer;
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
    public static class StructFieldAnnotation {
        /**
         * Annotated field.
         */
        public Field field;
        /**
         * Annotation type.
         */
        public Annotation annotation;
    }

    private static class StructNodeInfo {
        public String name;
        public AbstractConfigNode node;
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
            if (field.isAnnotationPresent(ConfigParam.class)) {
                ConfigParam param = field.getAnnotation(ConfigParam.class);
                processParam(param, field, node, target);
            } else if (field.isAnnotationPresent(ConfigAttribute.class)) {
                ConfigAttribute attr =
                        field.getAnnotation(ConfigAttribute.class);
                processAttributes(attr, field, node, target);
            } else if (field.isAnnotationPresent(ConfigValue.class)) {
                ConfigValue value = field.getAnnotation(ConfigValue.class);
                processValue(type, value, field, node, target);
            }
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Process a Config Value annotation.
     *
     * @param type        - Target type.
     * @param configValue - Config Value annotation.
     * @param field       - Field to process.
     * @param node        - Configuration node.
     * @param target      - Target instance.
     * @param <T>         - Target Type
     * @throws ConfigurationException
     */
    @SuppressWarnings("unchecked")
    private static <T> void processValue(Class<? extends T> type,
                                         ConfigValue configValue, Field field,
                                         AbstractConfigNode node, T target)
    throws ConfigurationException {
        try {
            String name = configValue.name();
            if (Strings.isNullOrEmpty(name)) {
                name = field.getName();
            }
            if (field.getType().isEnum() || canSetFieldType(field)) {
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
                    ReflectionUtils
                            .setValueFromString(value, target, field);
                } else if (configValue.required()) {
                    throw new ConfigurationException(String.format(
                            "Required configuration value not specified: [path=%s][name=%s]",
                            node.getAbsolutePath(), name));
                }
            } else {
                Class<? extends ITransformer> tt = configValue.transformer();
                if (tt != NullTransformer.class) {
                    ITransformer<?, String> transformer = tt.newInstance();
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
                        Object tValue = transformer.transform(value);
                        ReflectionUtils.setObjectValue(target, field, tValue);
                    } else if (configValue.required()) {
                        throw new ConfigurationException(String.format(
                                "Required configuration value not specified: [path=%s][name=%s]",
                                node.getAbsolutePath(), name));
                    }
                } else {
                    Class<?> ftype = field.getType();
                    String path = hasConfigAnnotation(ftype);
                    if (!Strings.isNullOrEmpty(path) &&
                            (node instanceof ConfigPathNode)) {
                        if (path.equals(".")) {
                            path = name;
                        } else {
                            path = String.format("%s.%s", path, name);
                        }
                        AbstractConfigNode cnode = node.find(path);
                        if (cnode != null &&
                                (cnode instanceof ConfigPathNode)) {
                            Object value = ftype.newInstance();
                            value = readConfigAnnotations(ftype,
                                                          (ConfigPathNode) cnode,
                                                          value);
                            ReflectionUtils
                                    .setObjectValue(target, field, value);
                        }
                        Object fv = ReflectionUtils.getFieldValue(target, field);
                        if (fv == null && configValue.required()) {
                            throw new ConfigurationException(String.format(
                                    "Required configuration value not specified: [path=%s][name=%s]",
                                    node.getAbsolutePath(), name));
                        }
                    } else {
                        throw new ConfigurationException(String.format(
                                "Parameter cannot be set for field of type = %s",
                                field.getType().getCanonicalName()));
                    }
                }
            }
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Process a Config Parameter annotation.
     *
     * @param param  - Config Parameter annotation.
     * @param field  - Field to process.
     * @param node   - Configuration node.
     * @param target - Target instance.
     * @param <T>    - Target Type
     * @throws ConfigurationException
     */
    @SuppressWarnings("unchecked")
    private static <T> void processParam(ConfigParam param, Field field,
                                         AbstractConfigNode node, T target)
    throws ConfigurationException {
        try {
            String name = param.name();
            if (Strings.isNullOrEmpty(name)) {
                name = field.getName();
            }
            StructNodeInfo nodeInfo = checkAnnotationTags(name, node,
                                                          ConfigParametersNode.NODE_ABBR_PREFIX);
            String value = null;
            if (node instanceof ConfigPathNode) {
                ConfigPathNode pathNode = (ConfigPathNode) nodeInfo.node;
                ConfigParametersNode params = pathNode.parmeters();
                if (params != null && !params.isEmpty()) {
                    if (params.hasKey(nodeInfo.name))
                        value = params.getValue(nodeInfo.name).getValue();
                }
            }
            if (!Strings.isNullOrEmpty(value)) {
                if (canProcessFieldType(field) || field.getType().isEnum()) {
                    if (!Strings.isNullOrEmpty(value)) {
                        ReflectionUtils.setValueFromString(value, target, field);

                    } else {
                        Class<? extends ITransformer> tt = param.transformer();
                        if (tt != NullTransformer.class) {
                            ITransformer<?, String> transformer = tt.newInstance();

                            Object tValue = transformer.transform(value);
                            ReflectionUtils.setObjectValue(target, field, tValue);
                        }
                    }
                }
            } else if (param.required()) {
                throw new ConfigurationException(String.format(
                        "Required parameter not specified: [path=%s][name=%s]",
                        node.getAbsolutePath(), nodeInfo.name));
            }
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Process a Config Attribute annotation.
     *
     * @param attribute - Config Attribute annotation.
     * @param field     - Field to process.
     * @param node      - Configuration node.
     * @param target    - Target instance.
     * @param <T>       - Target Type
     * @throws ConfigurationException
     */
    @SuppressWarnings("unchecked")
    private static <T> void processAttributes(ConfigAttribute attribute,
                                              Field field,
                                              AbstractConfigNode node, T target)
    throws ConfigurationException {
        try {
            String name = attribute.name();
            if (Strings.isNullOrEmpty(name)) {
                name = field.getName();
            }
            StructNodeInfo nodeInfo = checkAnnotationTags(name, node,
                                                          ConfigAttributesNode.NODE_ABBR_PREFIX);
            String value = null;
            if (node instanceof ConfigPathNode) {
                ConfigPathNode pathNode = (ConfigPathNode) nodeInfo.node;
                ConfigAttributesNode attrs = pathNode.attributes();
                if (attrs != null && !attrs.isEmpty()) {
                    if (attrs.hasKey(nodeInfo.name))
                        value = attrs.getValue(nodeInfo.name).getValue();
                }
            }
            if (!Strings.isNullOrEmpty(value)) {
                if (canProcessFieldType(field) || field.getType().isEnum()) {
                    ReflectionUtils.setValueFromString(value, target, field);
                } else {
                    Class<? extends ITransformer> tt = attribute.transformer();
                    if (tt != NullTransformer.class) {
                        ITransformer<?, String> transformer = tt.newInstance();

                        Object tValue = transformer.transform(value);
                        ReflectionUtils.setObjectValue(target, field, tValue);
                    }
                }
            } else if (attribute.required()) {
                throw new ConfigurationException(String.format(
                        "Required parameter not specified: [path=%s][name=%s]",
                        node.getAbsolutePath(), nodeInfo.name));
            }
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Check field for annotation tag.
     *
     * @param name - Annotation name property.
     * @param node - Configuration node.
     * @param tag  - Annotation tag.
     * @return - Processed Name/Node.
     * @throws ConfigurationException
     */
    private static StructNodeInfo checkAnnotationTags(String name,
                                                      AbstractConfigNode node,
                                                      String tag)
    throws ConfigurationException {
        StructNodeInfo ni = new StructNodeInfo();
        ni.name = name;
        ni.node = node;
        if (name.contains(tag)) {
            String[] parts =
                    name.split(tag);
            if (parts.length == 2) {
                String path = parts[0];
                String nname = parts[1];
                if (Strings.isNullOrEmpty(path)) {
                    ni.name = nname;
                } else {
                    ni.node = node.find(path);
                    if (ni.node == null) {
                        throw new ConfigurationException(
                                String.format(
                                        "Invalid ConfigParam : path not found. [name=%s]",
                                        name));
                    }
                    ni.name = nname;
                }
            } else {
                throw new ConfigurationException(
                        String.format("Invalid ConfigParam : [name=%s]",
                                      name));
            }
        }
        return ni;
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
        } else if (canSetFieldType(field)) {
            return true;
        } else {
            Class<?> type = field.getType();
            String ann = hasConfigAnnotation(type);
            if (!Strings.isNullOrEmpty(ann)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canSetFieldType(Field field) throws Exception {
        if (ReflectionUtils.isPrimitiveTypeOrString(field) ||
                field.getType().isEnum()) {
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
    public static <T> List<StructFieldAnnotation> getFieldAnnotations(
            Class<? extends T> type) {
        String path = hasConfigAnnotation(type);
        if (!Strings.isNullOrEmpty(path)) {
            List<StructFieldAnnotation> annotations = new ArrayList<>();
            Field[] fields = ReflectionUtils.getAllFields(type);
            if (fields != null && fields.length > 0) {
                for (Field field : fields) {
                    if (field.isAnnotationPresent(ConfigParam.class)) {
                        ConfigParam param = field.getAnnotation(ConfigParam.class);
                        StructFieldAnnotation fa = new StructFieldAnnotation();
                        fa.field = field;
                        fa.annotation = param;
                        annotations.add(fa);
                    } else if (field.isAnnotationPresent(ConfigValue.class)) {
                        ConfigValue param = field.getAnnotation(ConfigValue.class);
                        StructFieldAnnotation fa = new StructFieldAnnotation();
                        fa.field = field;
                        fa.annotation = param;
                        annotations.add(fa);
                    } else if (field.isAnnotationPresent(ConfigAttribute.class)) {
                        ConfigAttribute param =
                                field.getAnnotation(ConfigAttribute.class);
                        StructFieldAnnotation fa = new StructFieldAnnotation();
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
