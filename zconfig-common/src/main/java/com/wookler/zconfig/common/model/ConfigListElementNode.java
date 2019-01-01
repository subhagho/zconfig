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
 * Date: 1/1/19 10:48 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.model;

import com.google.common.base.Preconditions;

/**
 * Class represents a configuration node that is a list of configuration elements.
 *
 */
public class ConfigListElementNode extends ConfigListNode<AbstractConfigNode> {
    /**
     * Override the add value method, set the parent of the node element being added to this node.
     *
     * @param value - Element value to add.
     */
    @Override
    public void addValue(AbstractConfigNode value) {
        Preconditions.checkArgument(value != null);
        value.setParent(this);
        super.addValue(value);
    }
}
