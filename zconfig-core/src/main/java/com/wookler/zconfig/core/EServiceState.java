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
 * Date: 9/2/19 10:21 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.core;

import com.wookler.zconfig.common.IStateEnum;

/**
 * Enumeration representing the states of a service instance.
 */
public enum EServiceState implements IStateEnum<EServiceState> {
    /**
     * Service state is Unknown.
     */
    Unknown,
    /**
     * Service has been initialized.
     */
    Initialized,
    /**
     * Service is running and available.
     */
    Running,
    /**
     * Service has been stopped.
     */
    Stopped,
    /**
     * Service has terminated due to an error.
     */
    Error;


    /**
     * Get the state that represents an error state.
     *
     * @return - Error state.
     */
    @Override
    public EServiceState getErrorState() {
        return Error;
    }}
