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
 * Date: 4/3/19 11:10 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.codekutter.zconfig.common.utils;

import com.codekutter.zconfig.common.model.ConfigurationSettings;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.codekutter.zconfig.common.model.nodes.AbstractConfigNode;
import com.codekutter.zconfig.common.model.nodes.ConfigKeyValueNode;
import com.codekutter.zconfig.common.model.nodes.ConfigPathNode;
import com.codekutter.zconfig.common.model.nodes.ConfigValueNode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility functions for configuration/configuration nodes.
 */
public class ConfigUtils {
    private static final String NODE_NAME_DESCRIPTION = "__description";

    public static List<String> getResolvedPath(@Nonnull String path,
                                               @Nonnull
                                                       ConfigurationSettings settings) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(path));
        Preconditions.checkArgument(settings != null);

        List<String> stack = new ArrayList<>();
        String[] parts = path.split("\\.");
        if (parts != null && parts.length > 0) {
            for (String part : parts) {
                String[] pc = checkSubPath(part, settings);
                if (pc != null && pc.length > 0) {
                    for (String p : pc) {
                        stack.add(p);
                    }
                } else {
                    stack.add(part);
                }
            }
        } else {
            stack.add(path);
        }
        return stack;
    }

    /**
     * Check if the passed node name contains sub-tags for parameters/attributes.
     *
     * @param name - Path element to parse.
     * @return - Parsed path name, if tags are present, else NULL.
     */
    public static String[] checkSubPath(@Nonnull String name,
                                        @Nonnull ConfigurationSettings settings) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
        Preconditions.checkArgument(settings != null);

        int index = name.indexOf(ConfigurationSettings.PARAM_NODE_CHAR);
        if (index > 0) {
            String[] parts = name.split(ConfigurationSettings.PARAM_NODE_CHAR);
            if (parts.length == 1) {
                return new String[]{parts[0],
                                    settings
                                            .getParametersNodeName()};
            } else if (parts.length == 2) {
                return new String[]{parts[0],
                                    settings
                                            .getParametersNodeName(),
                                    parts[1]};
            }
        }
        index = name.indexOf(ConfigurationSettings.ATTR_NODE_CHAR);
        if (index > 0) {
            String[] parts = name.split(ConfigurationSettings.ATTR_NODE_CHAR);
            if (parts.length == 1) {
                return new String[]{parts[0],
                                    settings
                                            .getAttributesNodeName()};
            } else if (parts.length == 2) {
                return new String[]{parts[0],
                                    settings
                                            .getAttributesNodeName(),
                                    parts[1]};
            }
        }
        index = name.indexOf(ConfigurationSettings.PROP_NODE_CHAR);
        if (index > 0) {
            String[] parts = name.split(
                    String.format("\\%s", ConfigurationSettings.PROP_NODE_CHAR));
            if (parts.length == 1) {
                return new String[]{parts[0],
                                    settings
                                            .getPropertiesNodeName()};
            } else if (parts.length == 2) {
                return new String[]{parts[0],
                                    settings
                                            .getPropertiesNodeName(),
                                    parts[1]};
            }
        }
        index = name.indexOf(ConfigurationSettings.ARRAY_INDEX_CHAR);
        if (index > 0) {
            String[] parts = name.split(ConfigurationSettings.ARRAY_INDEX_CHAR);
            if (parts.length == 2) {
                return new String[]{parts[0],
                                    parts[1]};
            }
        }
        return null;
    }

    /**
     * Add a description element to the specified configuration node.
     *
     * @param node        - Configuration node
     * @param description - Description.
     */
    public static final void addDescription(@Nonnull AbstractConfigNode node,
                                            @Nonnull String description) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(description));
        if (node instanceof ConfigPathNode) {
            AbstractConfigNode cnode = node.find(
                    String.format("%s.%s", node.getName(), NODE_NAME_DESCRIPTION));
            if (cnode instanceof ConfigValueNode) {
                ((ConfigValueNode) cnode).setValue(description);
            } else {
                cnode = new ConfigValueNode(node.getConfiguration(), node);
                ((ConfigValueNode) cnode).setValue(description);
                ((ConfigPathNode) node).addChildNode(cnode);
            }
        } else if (node instanceof ConfigKeyValueNode) {
            ((ConfigKeyValueNode) node)
                    .addKeyValue(NODE_NAME_DESCRIPTION, description);
        }
    }

    /**
     * Extract the description of this node if present.
     *
     * @param node - Configuration node.
     * @return - Description.
     */
    public static final String getDescription(@Nonnull AbstractConfigNode node) {
        if (node instanceof ConfigPathNode) {
            AbstractConfigNode cnode = node.find(
                    String.format("%s.%s", node.getName(), NODE_NAME_DESCRIPTION));
            if (cnode instanceof ConfigValueNode) {
                return ((ConfigValueNode) cnode).getValue();
            }
        } else if (node instanceof ConfigKeyValueNode) {
            if (((ConfigKeyValueNode) node).hasKey(NODE_NAME_DESCRIPTION)) {
                return ((ConfigKeyValueNode) node).getValue(NODE_NAME_DESCRIPTION)
                                                  .getValue();
            }
        }
        return null;
    }
}
