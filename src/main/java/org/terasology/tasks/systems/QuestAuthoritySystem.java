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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.tasks.Status;
import org.terasology.tasks.components.QuestComponent;
import org.terasology.tasks.components.QuestItemComponent;
import org.terasology.tasks.components.TaskComponent;
import org.terasology.tasks.events.EndQuestEvent;
import org.terasology.tasks.events.EndTaskEvent;
import org.terasology.tasks.events.QuestEndedEvent;
import org.terasology.tasks.events.QuestStartedEvent;
import org.terasology.tasks.events.StartQuestEvent;
import org.terasology.tasks.events.StartTaskEvent;
import org.terasology.tasks.events.TaskCompletedEvent;
import org.terasology.tasks.events.TaskStartedEvent;

import java.util.Iterator;
import java.util.List;

/**
 * This controls the main logic of the quest, and defines what to do with a "quest card"
 */
@Share(QuestAuthoritySystem.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class QuestAuthoritySystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    private static final Logger logger = LoggerFactory.getLogger(QuestAuthoritySystem.class);

    /**
     * This updates the quest card variables for tasks calls.
     */
    @ReceiveEvent(components = {QuestItemComponent.class})
    public void onActivate(ActivateEvent event, EntityRef questItem) {
        QuestItemComponent questItemComp = questItem.getComponent(QuestItemComponent.class);
        event.getInstigator().getOwner().send(new StartQuestEvent(questItemComp.questURI));
    }

    @ReceiveEvent
    public void startQuestEvent(StartQuestEvent event, EntityRef entity) {
        EntityRef questEntity = entityManager.create(event.questURI);
        if (!questEntity.hasComponent(QuestComponent.class)) {
            return;
        }
        questEntity.setOwner(entity);

        QuestComponent questComponent = questEntity.getComponent(QuestComponent.class);
        questComponent.taskDef.forEach(questDef -> {
            String taskID = questDef.get(0);
            String taskURI = questDef.get(1);

            EntityRef taskEntity = entityManager.create(taskURI);
            if (taskEntity.hasComponent(TaskComponent.class)) {
                TaskComponent taskComponent = taskEntity.getComponent(TaskComponent.class);
                taskComponent.taskID = taskID;

                questComponent.taskList.add(taskEntity);
                taskEntity.setOwner(questEntity);
                taskEntity.saveComponent(taskComponent);
            }
        });

        questComponent.dependencyDef.forEach(dependDef -> {
            String firstTaskID = dependDef.get(0);
            String secondTaskID = dependDef.get(1);

            EntityRef firstTask = questComponent.taskList.stream().filter(task -> task.getComponent(TaskComponent.class).taskID.equals(firstTaskID)).findFirst().get();
            EntityRef secondTask = questComponent.taskList.stream().filter(task -> task.getComponent(TaskComponent.class).taskID.equals(secondTaskID)).findFirst().get();

            TaskComponent secondTaskComponent = secondTask.getComponent(TaskComponent.class);
            secondTaskComponent.dependencies.add(firstTask);
            secondTask.saveComponent(secondTaskComponent);
        });

        questComponent.questStatus = Status.ACTIVE;
        questEntity.saveComponent(questComponent);
        entity.send(new QuestStartedEvent(questEntity));

        questComponent.taskList.forEach(task -> {
            TaskComponent taskComponent = task.getComponent(TaskComponent.class);
            if (taskComponent.dependencies.isEmpty()) {
                taskComponent.taskStatus = Status.ACTIVE;
                task.saveComponent(taskComponent);
                task.send(new StartTaskEvent(taskComponent));
                task.send(new TaskStartedEvent());
            }
        });
    }

    @ReceiveEvent
    public void onTaskComplete(TaskCompletedEvent event, EntityRef entity) {
        TaskComponent oldTaskComponent = entity.getComponent(TaskComponent.class);
        oldTaskComponent.taskStatus = Status.SUCCEEDED;
        entity.saveComponent(oldTaskComponent);

        logger.info("Task {} complete", oldTaskComponent.taskID);
        List<EntityRef> taskList = entity.getOwner().getComponent(QuestComponent.class).taskList;

        Iterator<EntityRef> taskIterator = taskList.iterator();
        while (taskIterator.hasNext()) {
            EntityRef taskEntity = taskIterator.next();
            TaskComponent taskComponent = taskEntity.getComponent(TaskComponent.class);
            if (taskComponent.taskStatus.equals(Status.PENDING)) {
                if (taskComponent.dependencies.stream()
                        .anyMatch(dependencyEntity -> dependencyEntity.getComponent(TaskComponent.class).taskStatus.equals(Status.PENDING))) {
                    continue;
                }
                taskComponent.taskStatus = Status.ACTIVE;
                taskEntity.saveComponent(taskComponent);
                taskEntity.send(new StartTaskEvent());
                taskEntity.send(new TaskStartedEvent());
            }
        }

        if (taskList.stream().allMatch(taskEntity -> taskEntity.getComponent(TaskComponent.class).taskStatus.equals(Status.SUCCEEDED))) {
            entity.getOwner().send(new EndQuestEvent(true));
        }
    }

    @ReceiveEvent
    public void onQuestEnd(EndQuestEvent event, EntityRef entity) {
        entity.getComponent(QuestComponent.class).taskList.stream()
                .filter(taskEntity -> !taskEntity.getComponent(TaskComponent.class).taskStatus.isComplete())
                .forEach(taskEntity -> {
                    TaskComponent taskComponent = taskEntity.getComponent(TaskComponent.class);
                    if (taskComponent.taskStatus.equals(Status.ACTIVE)) {
                        taskComponent.taskStatus = event.questResult ? Status.SUCCEEDED : Status.FAILED;
                        taskEntity.send(new EndTaskEvent(taskComponent, event.questResult));
                        taskEntity.send(new TaskCompletedEvent(taskComponent, event.questResult));
                    }
                });

        entity.send(new QuestEndedEvent(event.questResult));
    }
}
