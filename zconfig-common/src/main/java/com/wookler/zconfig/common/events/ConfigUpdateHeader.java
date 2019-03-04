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
 * Date: 4/3/19 1:55 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.events;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Class to define the header of a configuration update transaction batch.
 */
public class ConfigUpdateHeader {
    /**
     * Configuration name this batch is for.
     */
    private String configName;
    /**
     * Pre-Update version (base version) of the configuration.
     */
    private String preVersion;
    /**
     * Updated version post applying changes.
     */
    private String updatedVersion;
    /**
     * Transaction ID of this update batch.
     */
    private String transactionId;
    /**
     * Timestamp of the update.
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
     * Get the pre-update version of the configuration.
     *
     * @return - Pre-Update version.
     */
    public String getPreVersion() {
        return preVersion;
    }

    /**
     * Set the pre-update version of the configuration.
     *
     * @param preVersion - Pre-Update version.
     */
    public void setPreVersion(String preVersion) {
        this.preVersion = preVersion;
    }

    /**
     * Get the updated version of this configuration.
     *
     * @return - Updated (post-update) version.
     */
    public String getUpdatedVersion() {
        return updatedVersion;
    }

    /**
     * Set the updated version of this configuration.
     *
     * @param updatedVersion - Updated (post-update) version.
     */
    public void setUpdatedVersion(String updatedVersion) {
        this.updatedVersion = updatedVersion;
    }

    /**
     * Get the transaction ID of this update batch.
     *
     * @return - Update transaction ID.
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Set the transaction ID of this update batch.
     *
     * @param transactionId - Update transaction ID.
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Get the update timestamp of this batch.
     *
     * @return - Update timestamp
     */
    public DateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Set the update timestamp of this batch.
     *
     * @param timestamp - Update timestamp
     */
    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

}
