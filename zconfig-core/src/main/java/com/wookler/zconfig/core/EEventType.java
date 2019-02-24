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
 * Date: 18/2/19 10:03 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.core;

import com.wookler.zconfig.common.EEnvState;

/**
 * Config update event type.
 */
public enum EEventType {
    /**
     * Add a new node/value.
     */
    ADD,
    /**
     * Update node value.
     */
    UPDATE,
    /**
     * Delete node value.
     */
    DELETE;

    /**
     * Parse the input String as the Event Type.
     *
     * @param value - Value to parse.
     * @return - Parsed Event Type.
     */
    public static EEventType parse(String value) {
        value = value.toUpperCase();
        return EEventType.valueOf(value);
    }
}
