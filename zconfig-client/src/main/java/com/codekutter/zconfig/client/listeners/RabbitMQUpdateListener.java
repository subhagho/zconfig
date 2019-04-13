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
 * Date: 4/3/19 8:01 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.codekutter.zconfig.client.listeners;

import com.codekutter.zconfig.common.ZConfigClientEnv;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.codekutter.zconfig.common.ConfigurationException;
import com.codekutter.zconfig.common.LogUtils;
import com.codekutter.zconfig.common.events.ConfigUpdateBatch;
import com.codekutter.zconfig.common.events.RegisterMessage;
import com.codekutter.zconfig.common.model.nodes.ConfigPathNode;
import com.codekutter.zconfig.common.rmq.RMQChannelConstants;
import com.codekutter.zconfig.common.rmq.RMQConnectionFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RabbitMQUpdateListener extends AbstractUpdateListener {
    public static final String NODE_NAME_LISTENER = "listener";

    private RMQConnectionFactory connectionFactory = new RMQConnectionFactory();

    /**
     * Configure this type instance.
     *
     * @param node - Handle to the configuration node.
     * @throws ConfigurationException
     */
    @Override
    public void configure(@Nonnull ConfigPathNode node)
    throws ConfigurationException {
        if (!NODE_NAME_LISTENER.equals(node.getName())) {
            throw new ConfigurationException(String.format(
                    "Invalid Configuration Node: [expected=%s][actual=%s]",
                    NODE_NAME_LISTENER, node.getName()));
        }
        connectionFactory.configure(node);
    }

    @Override
    public void run() {
        LogUtils.info(getClass(),
                      String.format("Starting Update listener: [type=%s]",
                                    getClass().getCanonicalName()));
        try {

            updateServer(RMQChannelConstants.RMQ_REGISTER_ROUTING_KEY);
            try (
                    Channel updateChannel = connectionFactory.getConnection()
                                                             .createChannel()) {
                updateChannel
                        .exchangeDeclarePassive(
                                RMQChannelConstants.RMQ_UPDATE_CHANNEL);
                String queueName = updateChannel.queueDeclarePassive(
                        RMQChannelConstants.getGroupUpdateQueue(
                                ZConfigClientEnv.clientEnv().getInstance()
                                                .getApplicationGroup()))
                                                .getQueue();
                updateChannel.queueBind(queueName,
                                        RMQChannelConstants.RMQ_UPDATE_CHANNEL,
                                        ZConfigClientEnv.clientEnv().getInstance()
                                                        .getApplicationName());
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    try {
                        if (!state.isAvailable()) {
                            updateChannel.close();
                            updateServer(
                                    RMQChannelConstants.RMQ_SHUTDOWN_ROUTING_KEY);
                            return;
                        }
                        String message = new String(delivery.getBody(),
                                                    StandardCharsets.UTF_8);
                        LogUtils.debug(getClass(), " [x] Received '" +
                                delivery.getEnvelope()
                                        .getRoutingKey() +
                                "':'" + message + "'");
                        ObjectMapper mapper =
                                ZConfigClientEnv.clientEnv().getJsonMapper();
                        ConfigUpdateBatch batch = mapper.readValue(message,
                                                                   ConfigUpdateBatch.class);
                        executeUpdateBatch(batch);
                        LogUtils.debug(getClass(), batch);
                    } catch (Exception e) {
                        state.setError(e);
                        LogUtils.error(getClass(), e);
                        throw new IOException(e);
                    }
                };
                updateChannel.basicConsume(queueName, true, deliverCallback,
                                           consumerTag -> {
                                           });
                LogUtils.warn(getClass(),
                              String.format(
                                      "Shutting down Update listener: [type=%s][state=%s]",
                                      getClass().getCanonicalName(),
                                      state.getState().name()));

            } finally {
                connectionFactory.close();
            }
        } catch (Exception e) {
            state.setError(e);
            LogUtils.error(getClass(), e);
        }
    }

    /**
     * Update the server regarding client startup/shutdown.
     *
     * @param key - Routing Key
     * @throws Exception
     */
    private void updateServer(String key) throws Exception {
        try (
                Channel registerChannel = connectionFactory.getConnection()
                                                           .createChannel()) {
            registerChannel
                    .exchangeDeclarePassive(RMQChannelConstants.RMQ_ADMIN_CHANNEL);
            String queueName = registerChannel
                    .queueDeclarePassive(RMQChannelConstants.RMQ_REGISTER_QUEUE)
                    .getQueue();
            ObjectMapper mapper = ZConfigClientEnv.clientEnv().getJsonMapper();
            RegisterMessage message = new RegisterMessage();
            message.setInstance(ZConfigClientEnv.clientEnv().getInstance());
            String json = mapper.writeValueAsString(message);

            registerChannel.basicPublish(RMQChannelConstants.RMQ_ADMIN_CHANNEL,
                                         key,
                                         MessageProperties.PERSISTENT_TEXT_PLAIN,
                                         json.getBytes(StandardCharsets.UTF_8));
        }
    }
}
