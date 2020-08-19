/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.tasks.systems;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.registry.In;
import org.terasology.tasks.components.QuestComponent;
import org.terasology.tasks.components.TaskComponent;
import org.terasology.tasks.components.TimedTaskComponent;
import org.terasology.tasks.events.EndQuestEvent;
import org.terasology.tasks.events.EndTaskEvent;
import org.terasology.tasks.events.QuestStartedEvent;
import org.terasology.tasks.events.StartTaskEvent;
import org.terasology.tasks.events.TaskCompletedEvent;

/**
 * TODO Type description
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class TimedTaskSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private Time time;

    @In
    private EntityManager entityManager;

    @ReceiveEvent
    public void onQuestStart(QuestStartedEvent event, EntityRef entity) {
        QuestComponent questComponent = event.questEntity.getComponent(QuestComponent.class);
        questComponent.taskList.stream()
                .filter(taskEntity -> taskEntity.hasComponent(TimedTaskComponent.class))
                .forEach(taskEntity -> {
                        TimedTaskComponent timedTaskComponent = taskEntity.getComponent(TimedTaskComponent.class);
                        if (timedTaskComponent.earlyStart) {
                            timedTaskComponent.startTime = time.getGameTime();
                            timedTaskComponent.timerRunning = true;
                        }
                });
    }

    @ReceiveEvent
    public void startTaskEvent(StartTaskEvent event, EntityRef taskEntity) {
        if (taskEntity.hasComponent(TimedTaskComponent.class)) {
            TimedTaskComponent timedTaskComponent = taskEntity.getComponent(TimedTaskComponent.class);

            if (!timedTaskComponent.timerRunning) {
                timedTaskComponent.startTime = time.getGameTime();
                timedTaskComponent.timerRunning = true;
            }
        }
    }

    @Override
    public void update(float delta) {
        for (EntityRef taskEntity : entityManager.getEntitiesWith(TimedTaskComponent.class)) {
            TimedTaskComponent timedTaskComponent = taskEntity.getComponent(TimedTaskComponent.class);
            if (timedTaskComponent.timerRunning) {
                TaskComponent taskComponent = taskEntity.getComponent(TaskComponent.class);
                if ((time.getGameTime() - timedTaskComponent.startTime) >= timedTaskComponent.targetTime) {
                    timedTaskComponent.timerRunning = false;
                    taskEntity.send(new EndTaskEvent(taskComponent, false));
                }

                taskComponent.suffix = String.valueOf((int) (timedTaskComponent.targetTime - (time.getGameTime() - timedTaskComponent.startTime)));
            }
        }
    }
}
