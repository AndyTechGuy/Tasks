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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.physics.events.CollideEvent;
import org.terasology.registry.In;
import org.terasology.tasks.*;
import org.terasology.tasks.components.PlayerQuestComponent;
import org.terasology.tasks.components.QuestBeaconComponent;
import org.terasology.tasks.events.StartTaskEvent;
import org.terasology.tasks.events.TaskCompletedEvent;

/**
 * This class is used for the quest beacons, to see where the player is in relation to the beacon.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class QuestBeaconSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    private final Map<GoToBeaconTask, Quest> tasks = new LinkedHashMap<>();

    @ReceiveEvent(components = QuestBeaconComponent.class)
    public void onCollision(CollideEvent event, EntityRef beacon) {
        EntityRef charEnt = event.getOtherEntity();
        QuestBeaconComponent component = beacon.getComponent(QuestBeaconComponent.class);

        PlayerQuestComponent playerQuestComponent = event.getOtherEntity().getOwner().getComponent(PlayerQuestComponent.class);
        playerQuestComponent.activeTaskList.entrySet()
                .stream()
                .filter(e -> { return e.getValue() instanceof GoToBeaconTask; })
                .forEach(e -> {
                    GoToBeaconTask goToBeaconTask = (GoToBeaconTask) e.getValue();

                    if (goToBeaconTask.getTargetBeaconId().equals(component.beaconId)) {
                        TaskGraph taskGraph = e.getKey().getTaskGraph();
                        Status prevStatus = taskGraph.getTaskStatus(goToBeaconTask);

                        if (prevStatus == Status.ACTIVE) {
                            goToBeaconTask.targetReached();
                        }

                        Status status = taskGraph.getTaskStatus(goToBeaconTask);
                        if (status != prevStatus && status.isComplete()) {
                            EntityRef entity = e.getKey().getEntity();
                            entity.send(new TaskCompletedEvent(e.getKey(), goToBeaconTask, status.isSuccess()));
                        }
                    }
                });
    }
}
