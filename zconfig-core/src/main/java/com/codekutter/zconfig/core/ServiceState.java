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
 * Date: 9/2/19 10:23 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.codekutter.zconfig.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.codekutter.zconfig.common.AbstractState;
import com.codekutter.zconfig.common.StateException;

/**
 * Class represents a service state instance.
 */
public class ServiceState extends AbstractState<EServiceState> {

    /**
     * Default constructor - initializes state to Unknown.
     */
    public ServiceState() {
        setState(EServiceState.Unknown);
    }

    /**
     * Check if this service instance is in a Running state.
     *
     * @return - Is Running?
     */
    @JsonIgnore
    public boolean isRunning() {
        return (getState() == EServiceState.Running);
    }

    /**
     * Check if this service instance is in a Initialized state.
     *
     * @return - Is Initialized?
     */
    @JsonIgnore
    public boolean isInitialized() {
        return (getState() == EServiceState.Initialized);
    }

    /**
     * Mark this service instance as stopped.
     */
    public void stop() {
        if (isRunning()) {
            setState(EServiceState.Stopped);
        }
    }

    /**
     * Check if the current state is in the expected state.
     *
     * @param expected - Expected state.
     * @throws StateException - Will be raised if state doesn't match with expected.
     */
    public void checkState(EServiceState expected) throws StateException {
        if (getState() != expected) {
            throw new StateException(
                    String.format("Invalid State Error : [expected=%s][current=%s]",
                                  expected.name(), getState().name()));
        }
    }
}
