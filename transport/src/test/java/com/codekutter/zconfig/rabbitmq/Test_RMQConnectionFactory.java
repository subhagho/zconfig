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
 * Date: 10/2/19 9:06 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.codekutter.zconfig.rabbitmq;

import com.codekutter.zconfig.common.ConfigProviderFactory;
import com.codekutter.zconfig.common.ConfigurationException;
import com.codekutter.zconfig.common.LogUtils;
import com.codekutter.zconfig.common.model.Configuration;
import com.codekutter.zconfig.common.model.Version;
import com.codekutter.zconfig.common.model.nodes.AbstractConfigNode;
import com.codekutter.zconfig.common.model.nodes.ConfigPathNode;
import com.codekutter.zconfig.common.parsers.AbstractConfigParser;
import com.codekutter.zconfig.transport.rabbitmq.RMQConnectionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class Test_RMQConnectionFactory {
    private static final String CONFIG_FILE =
            "src/test/resources/json/zconfig-client.json";
    private static final String CONFIG_VERSION = "0.0";
    private static final String CONFIG_NAME = "zconfig-client";
    private static final String CONFIG_RMQ_PATH = "zconfig.client";
    private static Configuration configuration;

    @BeforeAll
    static void setup() throws Exception {
        AbstractConfigParser parser = ConfigProviderFactory.parser(CONFIG_FILE);
        if (parser == null) {
            throw new ConfigurationException(String.format(
                    "Cannot get configuration parser instance. [file=%s]",
                    CONFIG_FILE));
        }
        Path path = Paths.get(CONFIG_FILE);
        parser.parse(CONFIG_NAME, ConfigProviderFactory.reader(path.toUri()), null,
                Version.parse(CONFIG_VERSION), null);
        configuration = parser.getConfiguration();
    }

    @AfterAll
    static void dispose() throws Exception {

    }

    @Test
    void open() {
        try {
            AbstractConfigNode node = configuration.find(CONFIG_RMQ_PATH);
            if (!(node instanceof ConfigPathNode)) {
                throw new Exception("Invalid configuration node type.");
            }
            RMQConnectionFactory factory = new RMQConnectionFactory();
            factory.configure(node);
            factory.open("guest", "guest");
        } catch (Exception e) {
            LogUtils.error(getClass(), e);
            fail(e.getLocalizedMessage());
        }
    }
}