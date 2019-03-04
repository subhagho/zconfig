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
 * Date: 4/3/19 2:50 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.client;

import com.wookler.zconfig.client.factory.ConfigurationManager;
import com.wookler.zconfig.common.ConfigurationException;
import com.wookler.zconfig.common.LogUtils;
import com.wookler.zconfig.common.events.ConfigUpdateBatch;
import com.wookler.zconfig.common.model.Configuration;
import com.wookler.zconfig.common.model.ESyncMode;

import javax.annotation.Nonnull;

/**
 * Abstract base class to define configuration update listeners.
 */
public abstract class AbstractUpdateListener implements Runnable {

    /**
     * Execute the batch update.
     *
     * @param batch - Configuration Update Batch.
     * @throws ConfigurationException
     */
    protected void executeUpdateBatch(@Nonnull ConfigUpdateBatch batch) throws
                                                                        ConfigurationException {
        try {
            ConfigurationManager manager =
                    ZConfigClientEnv.get().getConfigurationManager();
            Configuration config = manager.get(batch.getHeader().getConfigName());
            if (config != null && config.getSyncMode() == ESyncMode.EVENTS) {
                ZConfigClientEnv.get().getUpdateHandler().processEvents(batch);
            }
        } catch (Exception e) {
            LogUtils.error(getClass(), e);
        }
    }
}
