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
 * Date: 4/3/19 8:40 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.events;

import com.wookler.zconfig.common.model.nodes.ConfigValueNode;
import org.joda.time.DateTime;

/**
 * Update event structure for a configuration node update.
 */
public class ConfigUpdateEvent {
    /**
     * Name of the configuration.
     */
    private String configName;
    /**
     * Type of update event.
     */
    private EUpdateEventType eventType;
    /**
     * Configuration Version prior to update.
     */
    private String preVersion;
    /**
     * Configuration Version post update.
     */
    private String newVersion;
    /**
     * Node path of the node being updated.
     */
    private String path;
    /**
     * Updated node value.
     */
    private ConfigValueNode value;
    /**
     * Unique transaction ID.
     */
    private String transaction;
    /**
     * Sequence of this event in the transaction block.
     */
    private long transactionSequence;
    /**
     * Event timestamp of the transaction.
     */
    private DateTime timestamp;

    /**
     * Get the configuration name.
     *
     * @return - Configuration name.
     */
    public String getConfigName() {
        return configName;
    }

    /**
     * Set the configuration name.
     *
     * @param configName - Configuration name.
     */
    public void setConfigName(String configName) {
        this.configName = configName;
    }

    /**
     * Get the update event type.
     *
     * @return - Event type.
     */
    public EUpdateEventType getEventType() {
        return eventType;
    }

    /**
     * Set the update event type.
     *
     * @param eventType - Event type.
     */
    public void setEventType(EUpdateEventType eventType) {
        this.eventType = eventType;
    }

    /**
     * Get the configuration update prior to this update.
     *
     * @return - Pre update version
     */
    public String getPreVersion() {
        return preVersion;
    }

    /**
     * Set the configuration update prior to this update.
     *
     * @param preVersion - Pre update version
     */
    public void setPreVersion(String preVersion) {
        this.preVersion = preVersion;
    }

    /**
     * Get the configuration update post to this update.
     *
     * @return - Post update version
     */
    public String getNewVersion() {
        return newVersion;
    }

    /**
     * Set the configuration update post to this update.
     *
     * @param newVersion - Post update version
     */
    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }

    /**
     * Get the node path to update.
     *
     * @return - Node path to update.
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the node path to update.
     *
     * @param path - Node path to update.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get the node value.
     *
     * @return - Updated node value.
     */
    public ConfigValueNode getValue() {
        return value;
    }

    /**
     * Set the node value.
     *
     * @param value - Updated node value.
     */
    public void setValue(ConfigValueNode value) {
        this.value = value;
    }

    /**
     * Get the transaction ID for this update block.
     *
     * @return - Transaction ID.
     */
    public String getTransaction() {
        return transaction;
    }

    /**
     * Set the transaction ID for this update block.
     *
     * @param transaction - Transaction ID.
     */
    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    /**
     * Get the sequence number of this event in the transaction block.
     *
     * @return - Sequence in transaction.
     */
    public long getTransactionSequence() {
        return transactionSequence;
    }

    /**
     * Set the sequence number of this event in the transaction block.
     *
     * @param transactionSequence - Sequence in transaction.
     */
    public void setTransactionSequence(long transactionSequence) {
        this.transactionSequence = transactionSequence;
    }

    /**
     * Get the event timestamp of this transaction block.
     *
     * @return - Event timestamp.
     */
    public DateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Set the event timestamp of this transaction block.
     *
     * @param timestamp - Event timestamp.
     */
    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }
}
