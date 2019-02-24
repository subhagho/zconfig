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
 * Date: 18/2/19 10:00 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.core;

/**
 * Update event for a configuration node.
 * <p>
 * Event Structure:
 * <pre>
 *     {
 *         "transactionId" : [unique tnx ID],
 *         "group" : [application group],
 *         "application" : [application],
 *         "config" : [config name],
 *         "version" : [config version],
 *         "path" : [node path],
 *         "value" : [value object],
 *         "operation" : [ADD/UPDATE/DELETE],
 *         "timestamp" : [timestamp]
 *     }
 * </pre>
 */
public class ConfigUpdateEvent {
    /**
     * Transaction Batch ID - Must be unique per batch.
     */
    private String transactionId;
    /**
     * Application Group name
     */
    private String group;
    /**
     * Application name.
     */
    private String application;
    /**
     * Configuration name.
     */
    private String config;
    /**
     * Configuration version - Must be the version of the read configuration.
     */
    private String version;

    /**
     * Configuration element name.
     */
    private String name;
    /**
     * Configuration element description.
     */
    private String description;

    /**
     * Path to the configuration node.
     */
    private String path;
    /**
     * Value to set.
     */
    private Object value;
    /**
     * Operation type.
     */
    private EEventType operation;
    /**
     * Event timestamp.
     */
    private long timestamp;

    /**
     * Get the batch transaction ID.
     *
     * @return - Transaction ID
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Set the batch transaction ID.
     *
     * @param transactionId - Transaction ID
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Get the application group.
     *
     * @return - Application Group name.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Set the application group.
     *
     * @param group - Application Group name.
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Get the application name.
     *
     * @return - Application name.
     */
    public String getApplication() {
        return application;
    }

    /**
     * Set the application name.
     *
     * @param application - Application name.
     */
    public void setApplication(String application) {
        this.application = application;
    }

    /**
     * Get the configuration name.
     *
     * @return - Configuration name.
     */
    public String getConfig() {
        return config;
    }

    /**
     * Set the configuration name.
     *
     * @param config - Configuration name.
     */
    public void setConfig(String config) {
        this.config = config;
    }

    /**
     * Get the base configuration version.
     *
     * @return - Base configuration version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set the base configuration version.
     *
     * @param version - Base configuration version.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get the element name.
     *
     * @return - Element name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the element name.
     *
     * @param name - Element name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the element description.
     *
     * @return - Element description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the element description. (Mandatory for create calls).
     *
     * @param description - Element description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the path to configuration node.
     *
     * @return - Path to configuration node.
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the path to configuration node.
     *
     * @param path - Path to configuration node.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get the updated value. (in case of delete not relevant).
     *
     * @return - Updated value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Set the updated value. (in case of delete not relevant).
     *
     * @param value - Updated value.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Get the operation type.
     *
     * @return - Operation type.
     */
    public EEventType getOperation() {
        return operation;
    }

    /**
     * Set the operation type.
     *
     * @param operation - Operation type.
     */
    public void setOperation(EEventType operation) {
        this.operation = operation;
    }

    /**
     * Get the event timestamp.
     *
     * @return - Event timestamp.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set the event timestamp.
     *
     * @param timestamp - Event timestamp.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
