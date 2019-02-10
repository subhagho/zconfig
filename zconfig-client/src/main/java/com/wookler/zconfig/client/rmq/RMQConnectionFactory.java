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
 * Date: 10/2/19 6:32 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.client.rmq;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.wookler.zconfig.client.ClientState;
import com.wookler.zconfig.client.EClientState;
import com.wookler.zconfig.common.ConfigurationAnnotationProcessor;
import com.wookler.zconfig.common.ConfigurationException;
import com.wookler.zconfig.common.IConfigurable;
import com.wookler.zconfig.common.LogUtils;
import com.wookler.zconfig.common.model.Configuration;
import com.wookler.zconfig.common.model.annotations.ConfigParam;
import com.wookler.zconfig.common.model.annotations.ConfigPath;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;

/**
 * RabbitMQ Connection Factory - Class abstracts the RabbitMQ Connection.
 */
@ConfigPath(path = "zconfig.client.rmq.settings")
public class RMQConnectionFactory implements IConfigurable, Closeable {
    /**
     * Default Virtual Host value.
     */
    private static final String DEFAULT_VIRTUAL_HOST = "/";
    /**
     * Default RabbitMQ port. (TLS Port)
     */
    private static final int DEFAULT_PORT = 5671;

    /**
     * State instance of this connection factory.
     */
    private ClientState state = new ClientState();
    /**
     * Virtual Host for RabbitMQ
     */
    @ConfigParam(name = "virtualHost")
    private String virtualHost;
    /**
     * Hostname of the RabbitMQ Server
     */
    @ConfigParam(name = "hostname")
    private String hostname;
    /**
     * Port the server is running on.
     */
    @ConfigParam(name = "port")
    private int port = -1;

    /**
     * RabbitMQ Connection factory.
     */
    private ConnectionFactory connectionFactory;

    /**
     * Configure this type instance.
     *
     * @param configuration - Handle to the configuration instance.
     * @throws ConfigurationException
     */
    @Override
    public void configure(@Nonnull Configuration configuration)
    throws ConfigurationException {
        Preconditions.checkArgument(configuration != null);
        try {
            ConfigurationAnnotationProcessor
                    .readConfigAnnotations(getClass(), configuration, this);
            if (Strings.isNullOrEmpty(virtualHost)) {
                virtualHost = DEFAULT_VIRTUAL_HOST;
            }
            if (Strings.isNullOrEmpty(hostname)) {
                throw new ConfigurationException(
                        String.format("Missing configuration parameter : [%s]",
                                      "hostname"));
            }
            if (port <= 0) {
                port = DEFAULT_PORT;
            }
            state.setState(EClientState.Initialized);
        } catch (Exception e) {
            state.setError(e);
            throw new ConfigurationException(e);
        }
    }

    /**
     * Open this connection factory instance.
     * Method will open a test connection to the server.
     *
     * @param username - Username to connect with.
     * @param password - Password to connect with.
     * @throws RMQException
     */
    public void open(@Nonnull String username, @Nonnull String password)
    throws RMQException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(username));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(password));
        try {
            state.checkState(EClientState.Initialized);
            connectionFactory = new ConnectionFactory();
            connectionFactory.setUsername(username);
            connectionFactory.setPassword(password);
            connectionFactory.setVirtualHost(virtualHost);
            connectionFactory.setHost(hostname);
            connectionFactory.setPort(port);
            try (Connection connection = connectionFactory.newConnection()) {
                LogUtils.info(getClass(),
                              "RabbitMQ Connection successfully initialized...");
            }
            state.setState(EClientState.Available);
        } catch (Exception e) {
            state.setError(e);
            throw new RMQException(e);
        }
    }

    /**
     * Get a new connection instance from this factory.
     *
     * @return - New connection instance.
     * @throws RMQException
     */
    public Connection getConnection() throws RMQException {
        try {
            state.checkState(EClientState.Available);
            return connectionFactory.newConnection();
        } catch (Exception e) {
            throw new RMQException(e);
        }
    }

    /**
     * Close this connection factory.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        state.dispose();
        if (connectionFactory != null) {
            connectionFactory = null;
        }
    }
}
