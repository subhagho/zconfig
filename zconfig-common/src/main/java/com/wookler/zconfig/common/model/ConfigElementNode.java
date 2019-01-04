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
 * Date: 3/1/19 5:14 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;

public abstract class ConfigElementNode extends AbstractConfigNode {
    /**
     * Represents the local state of this configuration node instance.
     */
    @JsonIgnore
    private NodeState state;

    /**
     * Node creation info.
     */
    private ModifiedBy createdBy;
    /**
     * Node updation info.
     */
    private ModifiedBy updatedBy;

    /**
     * Incremented version indicator.
     */
    private long nodeVersion = 0;

    /**
     * Default constructor - Will initialize the update indicators.
     */
    public ConfigElementNode() {
        state = new NodeState();
        state.setState(ENodeState.Loading);

        this.nodeVersion = 0;
    }

    /**
     * Get the state handle for this node.
     *
     * @return - Node state handle.
     */
    public NodeState getState() {
        return state;
    }

    /**
     * Get the node creation info.
     *
     * @return - Created By info.
     */
    public ModifiedBy getCreatedBy() {
        return createdBy;
    }

    /**
     * Set the node creation info.
     *
     * @param createdBy - Created timestamp.
     */
    public void setCreatedBy(ModifiedBy createdBy) {
        Preconditions.checkArgument(createdBy != null);

        this.createdBy = createdBy;
    }

    /**
     * Get node last updation info.
     *
     * @return - Last updation info.
     */
    public ModifiedBy getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Set the last updation info for this node.
     *
     * @param updatedBy - Last updated timestamp
     */
    public void setUpdatedBy(ModifiedBy updatedBy) {
        Preconditions.checkArgument(updatedBy != null);

        this.updatedBy = updatedBy;
    }

    /**
     * Get the record version of this node.
     *
     * @return - Record version.
     */
    public long getNodeVersion() {
        return nodeVersion;
    }

    /**
     * Set the record version of this node.
     *
     * @param nodeVersion - Record version
     */
    public void setNodeVersion(long nodeVersion) {
        Preconditions.checkArgument(nodeVersion >= 0);
        this.nodeVersion = nodeVersion;
    }


    /**
     * Indicate this node has been updated.
     */
    protected void updated() {
        if (state.isSynced()) {
            this.nodeVersion++;
            this.state.setState(ENodeState.Updated);
        }
    }

    /**
     * Mark this node as deleted.
     */
    protected void deleted() {
        if (state.isSynced()) {
            this.nodeVersion++;
            this.state.setState(ENodeState.Deleted);
        } else if (state.isUpdated()) {
            this.state.setState(ENodeState.Deleted);
        }
    }

    /**
     * Mark this node is being loaded.
     */
    public void loading() {
        if (state == null) {
            state = new NodeState();
        }
        state.setState(ENodeState.Loading);
    }

    /**
     * Mark this node has been synchronized.
     */
    public void synced() {
        Preconditions.checkNotNull(state);
        state.setState(ENodeState.Synced);
    }


    /**
     * Update the node states recursively to the new state.
     *
     * @param state - New state.
     */
    public abstract void updateState(ENodeState state);
}
