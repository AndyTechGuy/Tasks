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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.registry.In;
import org.terasology.tasks.Quest;
import org.terasology.tasks.Status;
import org.terasology.tasks.Task;
import org.terasology.tasks.TaskGraph;
import org.terasology.tasks.TimeConstraintTask;
import org.terasology.tasks.components.PlayerQuestComponent;
import org.terasology.tasks.events.StartTaskEvent;
import org.terasology.tasks.events.TaskCompletedEvent;

/**
 * TODO Type description
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class TimedTaskSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private EntityManager entityManager;

    @In
    private Time time;

    private float cycleTime;

    @ReceiveEvent
    public void onStartTask(StartTaskEvent event, EntityRef entity) {
        if (event.getTask() instanceof TimeConstraintTask) {
            TimeConstraintTask task = (TimeConstraintTask) event.getTask();
            task.startTimer(time.getGameTime());
        }
    }

    @Override
    public void update(float delta) {
        for (EntityRef entityRef : entityManager.getEntitiesWith(PlayerQuestComponent.class)) {
            PlayerQuestComponent playerQuestComponent = entityRef.getComponent(PlayerQuestComponent.class);
            playerQuestComponent.activeTaskList.entrySet()
                    .stream()
                    .filter(e -> { return e.getValue() instanceof TimeConstraintTask; })
                    .forEach(e -> {
                        TimeConstraintTask timeConstraintTask = (TimeConstraintTask) e.getValue();
                        TaskGraph taskGraph = e.getKey().getTaskGraph();
                        Status prevStatus = taskGraph.getTaskStatus(timeConstraintTask);

                        if (prevStatus == Status.SUCCEEDED) {
                            timeConstraintTask.setTime(time.getGameTime());
                        }

                        Status status = taskGraph.getTaskStatus(timeConstraintTask);
                        if (status != prevStatus && status.isComplete()) {
                            EntityRef entity = e.getKey().getEntity();
                            entity.send(new TaskCompletedEvent(e.getKey(), timeConstraintTask, status.isSuccess()));
                        }
                    });
        }
    }
}
