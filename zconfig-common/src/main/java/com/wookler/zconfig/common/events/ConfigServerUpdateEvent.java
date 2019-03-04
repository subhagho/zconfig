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
 * Date: 4/3/19 4:47 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.events;

/**
 * Configuration Update event to pass updates to the server.
 */
public class ConfigServerUpdateEvent extends AbstractConfigUpdateEvent {
    /**
     * Name of the node.
     */
    private String name;
    /**
     * Description of the node.
     */
    private String description;
    /**
     * Updated value object.
     */
    private Object value;

    /**
     * Get the node name.
     *
     * @return - Node name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the node name.
     *
     * @param name - Node name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the node description.
     *
     * @return - Node description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the node description.
     *
     * @param description - Node description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get updated value.
     *
     * @return - Updated value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Set updated value.
     *
     * @param value - Updated value.
     */
    public void setValue(Object value) {
        this.value = value;
    }
}
