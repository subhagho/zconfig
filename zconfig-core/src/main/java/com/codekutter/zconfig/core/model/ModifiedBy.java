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
 * Date: 13/2/19 4:46 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.core.model;

import org.joda.time.DateTime;

import javax.annotation.Nonnull;

/**
 * Class represents a Owner/Modifier attribute.
 *
 * @param <K> - Owner ID type.
 */
public class ModifiedBy<K> implements ICopiable<ModifiedBy<K>> {
    /**
     * Owner/Modifier ID
     */
    private K ownerId;
    /**
     * Updation timestamp.
     */
    private DateTime timestamp;

    /**
     * Default empty constructor
     */
    public ModifiedBy() {
    }

    /**
     * Constructor with Owner/Timestamp.
     *
     * @param ownerId - Owner ID.
     */
    public ModifiedBy(@Nonnull K ownerId) {
        this.ownerId = ownerId;
        this.timestamp = DateTime.now();
    }

    /**
     * Get the owner/modifier ID.
     *
     * @return - owner/modifier ID
     */
    public K getOwnerId() {
        return ownerId;
    }

    /**
     * Set the owner/modifier ID.
     *
     * @param ownerId - owner/modifier ID
     */
    public void setOwnerId(K ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * Get the update timestamp.
     *
     * @return - Update timestamp.
     */
    public DateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Set the update timestamp.
     *
     * @param timestamp - Update timestamp.
     */
    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Copy the attribute values from the passed source entity to the current.
     *
     * @param source - Source entity to copy from.
     * @throws EntityException
     */
    @Override
    public void copyChanges(ModifiedBy<K> source) throws EntityException {
        this.ownerId = source.ownerId;
        this.timestamp = source.timestamp;
    }

    /**
     * Provide a custom clone interface for entities to be cloned.
     *
     * @param params - Additional parameters.
     * @return - Cloned entity
     * @throws EntityException
     */
    @Override
    public ModifiedBy<K> clone(Object... params) throws EntityException {
        return this;
    }
}
