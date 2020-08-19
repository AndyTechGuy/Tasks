// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.tasks.events;

import org.terasology.entitySystem.event.Event;
import org.terasology.tasks.Status;

public class EndQuestEvent implements Event {

    public boolean questResult;

    public EndQuestEvent(boolean questResult) {
        this.questResult = questResult;
    }

    public EndQuestEvent() { }
}
