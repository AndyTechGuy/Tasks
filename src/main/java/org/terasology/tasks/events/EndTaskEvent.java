// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.tasks.events;

import org.terasology.entitySystem.event.Event;
import org.terasology.tasks.components.TaskComponent;

public class EndTaskEvent implements Event {

    public boolean taskResult;

    public TaskComponent taskComponent;

    public EndTaskEvent() { }

    public EndTaskEvent(TaskComponent taskComponent, boolean taskResult) {
        this.taskResult = taskResult;
        this.taskComponent = taskComponent;
    }

}
