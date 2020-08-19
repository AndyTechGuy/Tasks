// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.tasks.components;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.tasks.Status;

import java.util.List;

public class TaskComponent implements Component {

    public String description;

    public String suffix;

    public Status taskStatus;

    public String taskID;

    public List<EntityRef> dependencies;

    public TaskComponent() {
        this.taskStatus = Status.PENDING;
        this.dependencies = Lists.newArrayList();
    }

}
