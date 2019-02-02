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
 * Date: 3/1/19 9:21 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.parsers;

import com.google.common.base.Strings;
import com.wookler.zconfig.common.ConfigProviderFactory;
import com.wookler.zconfig.common.ConfigTestConstants;
import com.wookler.zconfig.common.model.Configuration;
import com.wookler.zconfig.common.model.Version;
import com.wookler.zconfig.common.readers.ConfigFileReader;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static com.wookler.zconfig.common.LogUtils.*;

class JSONConfigParserTest {
    private static final String JSON_FILE =
            "src/test/resources/test-config.properties";


    @Test
    void parse() {
        try {
            JSONConfigParser parser =
                    (JSONConfigParser) ConfigProviderFactory.parser(
                            ConfigProviderFactory.EConfigType.JSON);
            assertNotNull(parser);

            Properties properties = new Properties();
            properties.load(new FileInputStream(JSON_FILE));

            String filename = properties.getProperty(ConfigTestConstants.PROP_CONFIG_FILE);
            assertFalse(Strings.isNullOrEmpty(filename));
            String vs = properties.getProperty(ConfigTestConstants.PROP_CONFIG_VERSION);
            assertFalse(Strings.isNullOrEmpty(vs));
            Version version = Version.parse(vs);
            assertNotNull(version);

            try (ConfigFileReader reader = new ConfigFileReader(filename)) {

                parser.parse("test-config", reader, version);
                Configuration configuration = parser.getConfiguration();
                assertNotNull(configuration);

                debug(getClass(), configuration);
            }
        } catch (Throwable t) {
            error(getClass(), t);
        }
    }
}