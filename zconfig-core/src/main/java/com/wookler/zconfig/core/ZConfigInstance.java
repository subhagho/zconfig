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
 * Date: 9/2/19 10:12 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.core;

import org.joda.time.DateTime;

/**
 * Class represents a service instance of the Configuration Server.
 */
public class ZConfigInstance {
    /**
     * Unique instance ID.
     */
    private String id;
    /**
     * Service name (if clustered, all nodes will have the same name).
     */
    private String name;
    /**
     * Hostname of the server this instance is running on.
     */
    private String hostname;
    /**
     * IP Address of the server this instance is running on.
     */
    private String ip;
    /**
     * Port this instance is listening on.
     */
    private int port;
    /**
     * Timestamp this service was started.
     */
    private DateTime startTime;

    /**
     * Get the unique instance ID.
     *
     * @return - Unique instance ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the unique instance ID.
     *
     * @param id - Unique instance ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the service name.
     *
     * @return - Service name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the service name.
     *
     * @param name - Service name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the hostname of this server.
     *
     * @return - Server hostname.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Set the hostname of this server.
     *
     * @param hostname - Server hostname.
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Get the IP Address string for this server.
     *
     * @return - IP Address string.
     */
    public String getIp() {
        return ip;
    }

    /**
     * Set the IP Address string for this server.
     *
     * @param ip - IP Address string.
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Get the port this service is listening on.
     *
     * @return - Server Port
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the port this service is listening on.
     *
     * @param port - Server Port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get the timestamp this service was started.
     *
     * @return - Service start timestamp.
     */
    public DateTime getStartTime() {
        return startTime;
    }

    /**
     * Set the timestamp this service was started.
     *
     * @param startTime - Service start timestamp.
     */
    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }
}
