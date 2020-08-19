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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.physics.events.CollideEvent;
import org.terasology.registry.In;
import org.terasology.tasks.Status;
import org.terasology.tasks.components.BeaconTaskComponent;
import org.terasology.tasks.components.QuestBeaconComponent;
import org.terasology.tasks.components.TaskComponent;
import org.terasology.tasks.events.EndTaskEvent;
import org.terasology.tasks.events.TaskCompletedEvent;

/**
 * This class is used for the quest beacons, to see where the player is in relation to the beacon.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class QuestBeaconSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @ReceiveEvent(components = QuestBeaconComponent.class)
    public void onCollision(CollideEvent event, EntityRef beacon) {
        EntityRef charEnt = event.getOtherEntity();
        QuestBeaconComponent component = beacon.getComponent(QuestBeaconComponent.class);

        entityManager.getEntitiesWith(BeaconTaskComponent.class).forEach(beaconTaskEntity -> {
            TaskComponent taskComponent = beaconTaskEntity.getComponent(TaskComponent.class);
            BeaconTaskComponent beaconTaskComponent = beaconTaskEntity.getComponent(BeaconTaskComponent.class);

            if (beaconTaskEntity.getOwner().getOwner().equals(charEnt.getOwner())) {
                if (beaconTaskComponent.beaconID.equals(component.beaconId)) {
                    if (taskComponent.taskStatus.equals(Status.ACTIVE)) {
                        beaconTaskComponent.targetReached = true;
                        beaconTaskEntity.send(new EndTaskEvent(taskComponent, true));
                    }
                }
            }
        });
    }
}
