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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.codekutter.zconfig.common.model.nodes.AbstractConfigNode;
import com.codekutter.zconfig.common.model.nodes.ConfigKeyValueNode;
import com.codekutter.zconfig.common.model.nodes.ConfigPathNode;
import com.codekutter.zconfig.common.model.nodes.ConfigValueNode;

import javax.annotation.Nonnull;

/**
 * Utility functions for configuration/configuration nodes.
 */
public class ConfigUtils {
    private static final String NODE_NAME_DESCRIPTION = "__description";

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
