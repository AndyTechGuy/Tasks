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

package org.terasology.tasks.systems;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.registry.Share;
import org.terasology.tasks.DefaultQuest;
import org.terasology.tasks.Quest;
import org.terasology.tasks.Status;
import org.terasology.tasks.Task;
import org.terasology.tasks.TaskGraph;
import org.terasology.tasks.components.PlayerQuestComponent;
import org.terasology.tasks.components.QuestItemComponent;
import org.terasology.tasks.events.*;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.ListMultimap;

/**
 * This controls the main logic of the quest, and defines what to do with a "quest card"
 */
@Share(QuestSystem.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class QuestSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(QuestSystem.class);

    @ReceiveEvent
    public void onPlayerJoin(OnPlayerSpawnedEvent onPlayerSpawnedEvent, EntityRef player) {
        if (!player.getOwner().hasComponent(PlayerQuestComponent.class)) {
            player.getOwner().addComponent(new PlayerQuestComponent());
        }
    }

    /**
     * This updates the quest card variables for tasks calls.
     */
    @ReceiveEvent(components = {QuestItemComponent.class})
    public void onActivate(ActivateEvent event, EntityRef questItem) {
        QuestItemComponent questComp = questItem.getComponent(QuestItemComponent.class);
        EntityRef entity = event.getInstigator().getOwner();
        PlayerQuestComponent playerQuestComponent = entity.getComponent(PlayerQuestComponent.class);

        BeforeQuestEvent beforeQuestEvent = questItem.send(new BeforeQuestEvent(entity, questComp.shortName));
        if (!beforeQuestEvent.isConsumed()) {
            TaskGraph taskGraph = questComp.tasks;
            DefaultQuest quest = new DefaultQuest(entity, questComp.shortName, questComp.description, taskGraph);
            playerQuestComponent.questList.add(quest);

            for (Task task : taskGraph) {
                if (taskGraph.getTaskStatus(task) != Status.PENDING) {
                    playerQuestComponent.activeTaskList.put(quest, task);
                    logger.info("Starting task {}", task);
                    entity.send(new StartTaskEvent(quest, task));
                }
            }
        }
        entity.saveComponent(playerQuestComponent);
    }

    @ReceiveEvent
    public void onTaskComplete(TaskCompletedEvent event, EntityRef entity) {
        Quest quest = event.getQuest();
        logger.info("Task {} complete", event.getTask());
        PlayerQuestComponent playerQuestComponent = entity.getComponent(PlayerQuestComponent.class);

        playerQuestComponent.activeTaskList.remove(event.getQuest(), event.getTask());
        TaskGraph taskGraph = quest.getTaskGraph();
        for (Task task : taskGraph) {
            entity.send(new QuestStartedEvent(quest));
            if (taskGraph.getDependencies(task).contains(event.getTask())) {
                if (taskGraph.getTaskStatus(task) != Status.PENDING) {
                    playerQuestComponent.activeTaskList.put(quest, task);
                    logger.info("Starting task {}", task);
                    entity.send(new StartTaskEvent(quest, task));
                }
            }
        }
        if (quest.getStatus().isComplete()) {
            entity.send(new QuestCompleteEvent(quest, quest.getStatus().isSuccess()));
        }
        entity.saveComponent(playerQuestComponent);
    }
}
