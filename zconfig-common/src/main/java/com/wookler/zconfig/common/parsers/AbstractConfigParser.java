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
 * Date: 2/1/19 9:56 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.parsers;

import com.wookler.zconfig.common.ConfigurationException;
import com.wookler.zconfig.common.model.Configuration;

import java.util.Properties;

/**
 * Abstract base class for defining configuration parsers.
 */
public abstract class AbstractConfigParser {
    /**
     * Configuration instance handle.
     */
    protected Configuration configuration;

    /**
     * Get the handle to the parsed configuration.
     *
     * @return - Configuration instance.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Parse and load the configuration instance using the specified properties.
     *
     * @param name       - Configuration name being loaded.
     * @param properties - Initialization properties.
     * @throws ConfigurationException
     */
    public abstract void parse(String name, Properties properties)
    throws ConfigurationException;

}
