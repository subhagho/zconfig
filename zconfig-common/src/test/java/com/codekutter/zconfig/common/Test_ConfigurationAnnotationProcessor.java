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
 * Date: 3/2/19 4:19 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.codekutter.zconfig.common;

import com.google.common.base.Strings;
import com.codekutter.zconfig.common.model.Configuration;
import com.codekutter.zconfig.common.model.Version;
import com.codekutter.zconfig.common.model.annotations.ConfigParam;
import com.codekutter.zconfig.common.model.annotations.ConfigPath;
import com.codekutter.zconfig.common.model.annotations.ConfigValue;
import com.codekutter.zconfig.common.model.annotations.transformers.JodaTimeTransformer;
import com.codekutter.zconfig.common.parsers.JSONConfigParser;
import com.codekutter.zconfig.common.readers.ConfigFileReader;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class Test_ConfigurationAnnotationProcessor {
    private static final String BASE_PROPS_FILE =
            "src/test/resources/json/test-config.properties";
    private static Configuration configuration = null;

    @ConfigPath(path = ".")
    public static class ModifiedBy {
        @ConfigValue(name = "user")
        private String name;
        @ConfigValue(name = "timestamp", transformer = JodaTimeTransformer.class)
        private DateTime timestamp;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public DateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(DateTime timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "ModifiedBy{" +
                    "name='" + name + '\'' +
                    ", timestamp='" + timestamp + '\'' +
                    '}';
        }
    }

    @ConfigPath(path = "configuration.node_1.node_2")
    public static class ConfigAnnotationsTest {
        @ConfigValue(required = true)
        private String nodeName;
        @ConfigParam(name = "PARAM_1", required = true)
        private String paramValue;
        @ConfigValue(name = "values.longValue", required = true)
        private long longValue;
        @ConfigValue(name = "values.doubleValue", required = true)
        private double doubleValue;
        @ConfigValue(name = "node_3.node_4.TEST_LONG_LIST")
        private Set<Long> longListSet;
        @ConfigValue(name = "updatedBy")
        private ModifiedBy updatedBy;
        @ConfigValue(name = "createdBy")
        private ModifiedBy createdBy;

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public String getParamValue() {
            return paramValue;
        }

        public void setParamValue(String paramValue) {
            this.paramValue = paramValue;
        }

        public long getLongValue() {
            return longValue;
        }

        public void setLongValue(long longValue) {
            this.longValue = longValue;
        }

        public double getDoubleValue() {
            return doubleValue;
        }

        public void setDoubleValue(double doubleValue) {
            this.doubleValue = doubleValue;
        }

        public Set<Long> getLongListSet() {
            return longListSet;
        }

        public void setLongListSet(Set<Long> longListSet) {
            this.longListSet = longListSet;
        }

        public ModifiedBy getUpdatedBy() {
            return updatedBy;
        }

        public void setUpdatedBy(
                ModifiedBy updatedBy) {
            this.updatedBy = updatedBy;
        }

        public ModifiedBy getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(
                ModifiedBy createdBy) {
            this.createdBy = createdBy;
        }

        @Override
        public String toString() {
            return "ConfigAnnotationsTest{" +
                    "nodeName='" + nodeName + '\'' +
                    ", paramValue='" + paramValue + '\'' +
                    ", longValue=" + longValue +
                    ", doubleValue=" + doubleValue +
                    ", longListSet=" + longListSet +
                    ", updatedBy=" + updatedBy +
                    ", createdBy=" + createdBy +
                    '}';
        }
    }

    @BeforeAll
    static void init() throws Exception {
        JSONConfigParser parser =
                (JSONConfigParser) ConfigProviderFactory.parser(
                        ConfigProviderFactory.EConfigType.JSON);
        assertNotNull(parser);

        Properties properties = new Properties();
        properties.load(new FileInputStream(BASE_PROPS_FILE));

        String filename = properties.getProperty(
                ConfigTestConstants.PROP_CONFIG_FILE);
        assertFalse(Strings.isNullOrEmpty(filename));
        String vs = properties.getProperty(ConfigTestConstants.PROP_CONFIG_VERSION);
        assertFalse(Strings.isNullOrEmpty(vs));
        Version version = Version.parse(vs);
        assertNotNull(version);

        try (ConfigFileReader reader = new ConfigFileReader(filename)) {
            parser.parse("test-config", reader, null, version);
            configuration = parser.getConfiguration();
            assertNotNull(configuration);
        }
    }

    @Test
    void readConfigAnnotations() {
        try {
            assertNotNull(configuration);
            ConfigAnnotationsTest value = new ConfigAnnotationsTest();
            value = ConfigurationAnnotationProcessor
                    .readConfigAnnotations(ConfigAnnotationsTest.class,
                                           configuration, value);
            LogUtils.debug(getClass(), value);
        } catch (Throwable t) {
            LogUtils.error(getClass(), t);
            fail(t.getLocalizedMessage());
        }
    }
}