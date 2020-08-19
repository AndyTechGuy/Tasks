// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.tasks.components;

import org.terasology.entitySystem.Component;

/**
 * Indicates a quest item which starts a particular quest when used.
 */
public class QuestItemComponent implements Component {

    /**
     * The URI of the quest prefab.
     */
    public String questURI;

    public QuestItemComponent() {}

    public QuestItemComponent(String questURI) {
        this.questURI = questURI;
    }

}
