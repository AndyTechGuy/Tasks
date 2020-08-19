// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.tasks.events;

import org.terasology.entitySystem.event.Event;

public class QuestEndedEvent implements Event {

    public boolean questResult;

    public QuestEndedEvent() { }

    public QuestEndedEvent(boolean questResult) {
        this.questResult = questResult;
    }

}
