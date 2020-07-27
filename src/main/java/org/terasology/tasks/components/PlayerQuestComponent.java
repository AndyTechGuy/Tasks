/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.tasks.components;

import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;
import org.terasology.tasks.Quest;
import org.terasology.tasks.Task;
import org.terasology.tasks.TaskGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Attaches to the player and stores the quests that they are completing/have already completed.
 */
public class PlayerQuestComponent implements Component {

    /**
     * The list of quests that this player is taking on/has finished.
     */
    @Replicate
    public List<Quest> questList;

    /**
     * The list of active tasks that this player is taking on.
     */
    @Replicate
    public Map<Quest, Task> activeTaskList;

    public PlayerQuestComponent() {
        questList = new ArrayList<Quest>();
        activeTaskList = new HashMap<Quest, Task>();
    }
}
