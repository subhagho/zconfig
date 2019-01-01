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
 * Date: 31/12/18 7:27 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

/**
 * Class represents a configuration path node.
 *
 * Configuration path(s) can be represented using a (.) or (/) notation while referencing.
 */
public class ConfigPathNode extends AbstractConfigNode {

    /**
     * Map containing the child nodes.
     */
    private Map<String, AbstractConfigNode> children;



    /**
     * Get the map of child nodes for this path element.
     *
     * @return - Map of child nodes.
     */
    public Map<String, AbstractConfigNode> getChildren() {
        return children;
    }

    /**
     * Set the map of child nodes for this path element.
     *
     * @param children - Map of child nodes.
     */
    public void setChildren(Map<String, AbstractConfigNode> children) {
        this.children = children;
    }

    /**
     * Add a new child node to this path element.
     *
     * Note: Node names are assumed to be unique, adding a child node with an existing name will
     * override the element if already present.
     *
     * @param node - Child node to add.
     */
    public void addChildNode(AbstractConfigNode node) {
        Preconditions.checkArgument(node != null);

        if (children == null) {
            children = new HashMap<>();
        }
        node.setParent(this);
        children.put(node.getName(), node);
        setUpdateTimeStamp(DateTime.now());
    }

    /**
     * Get the child node (if any) with the specified node name.
     *
     * @param name - Node name to search for.
     *
     * @return - Config Node, if found else NULL.
     */
    @JsonIgnore
    public AbstractConfigNode getChildNode(String name) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));

        if (children != null) {
            return children.get(name);
        }
        return null;
    }

    /**
     * Remove the child node with the specified node name.
     *
     * @param name - Node name of the child to remvoe.
     * @return - True if removed, else False.
     */
    public boolean removeChildNode(String name) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
        if (children != null && children.containsKey(name)) {
            children.remove(name);
            setUpdateTimeStamp(DateTime.now());
            return true;
        }
        return false;
    }

    /**
     * Override default toString(). Will print a path element with children if any.
     * Example: /path:[{child}, {child}...]
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer(String.format("/%s", getName()));
        if (children != null && !children.isEmpty()) {
            buff.append(":[");
            boolean first = true;
            for(String key : children.keySet()) {
                AbstractConfigNode node = children.get(key);
                if (node != null) {
                    if (first) {
                        first = false;
                    } else {
                        buff.append(", ");
                    }
                }
                buff.append(String.format("{%s}", node.toString()));
            }
            buff.append("]");
        }
        return buff.toString();
    }
}
