// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.tasks.events;

import org.terasology.entitySystem.event.Event;

public class StartQuestEvent implements Event {

    public String questURI;

    public StartQuestEvent() { }

    public StartQuestEvent(String questURI) {
        this.questURI = questURI;
    }

}
