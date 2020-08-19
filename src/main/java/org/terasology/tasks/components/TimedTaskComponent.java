// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.tasks.components;

import org.terasology.entitySystem.Component;

public class TimedTaskComponent implements Component {

    public float targetTime;

    public float startTime;

    public boolean earlyStart;

    public boolean timerRunning;

    public TimedTaskComponent() { }

    public TimedTaskComponent(float targetTime) {
        this.targetTime = targetTime;
    }

}
