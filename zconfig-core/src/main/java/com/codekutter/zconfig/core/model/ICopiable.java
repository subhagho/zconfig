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
 * Date: 13/2/19 4:50 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.codekutter.zconfig.core.model;

/**
 * Interface to be implemented for entities that expose a copy/clone method.
 *
 * @param <T> - Entity Type
 */
public interface ICopiable<T> {
    /**
     * Copy the attribute values from the passed source entity to the current.
     *
     * @param source - Source entity to copy from.
     * @throws EntityException
     */
    void copyChanges(T source) throws EntityException;

    /**
     * Provide a custom clone interface for entities to be cloned.
     *
     * @param params - Additional parameters.
     * @return - Cloned entity
     * @throws EntityException
     */
    T clone(Object... params) throws EntityException;
}