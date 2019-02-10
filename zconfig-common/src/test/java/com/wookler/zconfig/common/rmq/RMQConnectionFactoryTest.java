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

package com.wookler.zconfig.common.rmq;

import com.wookler.zconfig.common.ConfigProviderFactory;
import com.wookler.zconfig.common.ConfigurationException;
import com.wookler.zconfig.common.LogUtils;
import com.wookler.zconfig.common.model.Configuration;
import com.wookler.zconfig.common.model.Version;
import com.wookler.zconfig.common.parsers.AbstractConfigParser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class RMQConnectionFactoryTest {
    private static final String CONFIG_FILE =
            "src/test/resources/zconfig-client.json";
    private static final String CONFIG_VERSION = "0.0";
    private static final String CONFIG_NAME = "zconfig-client";
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
        parser.parse(CONFIG_NAME, ConfigProviderFactory.reader(path.toUri()),
                     Version.parse(CONFIG_VERSION));
        configuration = parser.getConfiguration();
    }

    @AfterAll
    static void dispose() throws Exception {

    }

    @Test
    void open() {
        try {
            RMQConnectionFactory factory = new RMQConnectionFactory();
            factory.configure(configuration);
            factory.open("guest", "guest");
        } catch (Exception e) {
            LogUtils.error(getClass(), e);
            fail(e.getLocalizedMessage());
        }
    }
}