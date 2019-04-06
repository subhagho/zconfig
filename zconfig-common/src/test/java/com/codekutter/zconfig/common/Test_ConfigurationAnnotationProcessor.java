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

import com.codekutter.zconfig.common.model.annotations.MethodInvoke;
import com.google.common.base.Strings;
import com.codekutter.zconfig.common.model.Configuration;
import com.codekutter.zconfig.common.model.Version;
import com.codekutter.zconfig.common.model.annotations.ConfigParam;
import com.codekutter.zconfig.common.model.annotations.ConfigPath;
import com.codekutter.zconfig.common.model.annotations.ConfigValue;
import com.codekutter.zconfig.common.model.annotations.transformers.JodaTimeTransformer;
import com.codekutter.zconfig.common.parsers.JSONConfigParser;
import com.codekutter.zconfig.common.readers.ConfigFileReader;
import lombok.Data;
import lombok.ToString;
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

    public enum ETestValue {
        EValue1, EValue2, EValue3
    }

    @ToString
    @Data
    @ConfigPath(path = ".")
    public static class ModifiedBy {
        @ConfigValue(name = "user")
        private String name;
        @ConfigValue(name = "timestamp", transformer = JodaTimeTransformer.class)
        private DateTime timestamp;
    }

    @ToString
    @ConfigPath(path = "configuration.node_1.node_2")
    public static class ConfigAnnotationsTest {
        @ConfigValue(required = true)
        private String nodeName;
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
        private long paramLong = -1;
        private ETestValue paramEnum = ETestValue.EValue1;

        @MethodInvoke
        public ConfigAnnotationsTest(
                @ConfigParam(name = "PARAM_1", required = true) String paramValue) {
            this.paramValue = paramValue;
        }

        @MethodInvoke
        public void updateValues(
                @ConfigParam(name = "PARAM_5", required = true) long paramLong,
                @ConfigParam(name = "PARAM_6", required = true)
                        ETestValue paramEnum) {
            this.paramLong = paramLong;
            this.paramEnum = paramEnum;
        }

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

        public long getParamLong() {
            return paramLong;
        }

        public void setParamLong(long paramLong) {
            this.paramLong = paramLong;
        }

        public ETestValue getParamEnum() {
            return paramEnum;
        }

        public void setParamEnum(
                ETestValue paramEnum) {
            this.paramEnum = paramEnum;
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
            ConfigAnnotationsTest value = ConfigurationAnnotationProcessor
                    .readConfigAnnotations(ConfigAnnotationsTest.class,
                                           configuration);
            assertNotNull(value);
            assertFalse(Strings.isNullOrEmpty(value.paramValue));
            assertTrue(value.paramLong > 0);
            assertEquals(ETestValue.EValue3, value.paramEnum);

            LogUtils.debug(getClass(), value);
        } catch (Throwable t) {
            LogUtils.error(getClass(), t);
            fail(t.getLocalizedMessage());
        }
    }
}