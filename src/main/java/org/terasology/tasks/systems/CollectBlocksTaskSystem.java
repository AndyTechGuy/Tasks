/*
 * Copyright 2015 MovingBlocks
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
import java.util.Map.Entry;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.inventory.events.InventorySlotStackSizeChangedEvent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.tasks.*;
import org.terasology.tasks.components.PlayerQuestComponent;
import org.terasology.tasks.events.StartTaskEvent;
import org.terasology.tasks.events.TaskCompletedEvent;

/**
 *
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class CollectBlocksTaskSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @ReceiveEvent(components = {ClientComponent.class})
    public void onStartTask(StartTaskEvent event, EntityRef entity) {
        if (event.getTask() instanceof CollectBlocksTask) {
            CollectBlocksTask task = (CollectBlocksTask) event.getTask();
            EntityRef charEntity = entity.getComponent(ClientComponent.class).character;
            InventoryComponent inventory = charEntity.getComponent(InventoryComponent.class);
            int count = 0;
            for (EntityRef itemRef : inventory.itemSlots) {
                ItemComponent item = itemRef.getComponent(ItemComponent.class);
                if (item != null && item.stackId.equalsIgnoreCase(task.getItemId())) {
                    count += item.stackCount;
                }
            }
            task.setAmount(count);
        }
    }

    @ReceiveEvent(components = {InventoryComponent.class})
    public void onInventoryChange(InventorySlotChangedEvent event, EntityRef charEntity) {
        ItemComponent newItem = event.getNewItem().getComponent(ItemComponent.class);
        if (newItem != null) {
            onInventoryChange(charEntity, newItem.stackId, newItem.stackCount);
        }

        ItemComponent oldItem = event.getOldItem().getComponent(ItemComponent.class);
        if (oldItem != null) {
            onInventoryChange(charEntity, oldItem.stackId, -oldItem.stackCount);
        }
    }

    @ReceiveEvent(components = {InventoryComponent.class})
    public void onInventoryChange(InventorySlotStackSizeChangedEvent event, EntityRef charEntity) {
        InventoryComponent inventory = charEntity.getComponent(InventoryComponent.class);
        EntityRef itemRef = inventory.itemSlots.get(event.getSlot());
        ItemComponent item = itemRef.getComponent(ItemComponent.class);

        int amountChange = event.getNewSize() - event.getOldSize();
        onInventoryChange(charEntity, item.stackId, amountChange);
    }

    private void onInventoryChange(EntityRef charEntity, String stackId, int amountChange) {
        PlayerQuestComponent playerQuestComponent = charEntity.getOwner().getComponent(PlayerQuestComponent.class);
        playerQuestComponent.activeTaskList.entrySet()
            .stream()
            .filter(e -> { return e.getValue() instanceof CollectBlocksTask; })
            .forEach(e -> {
                CollectBlocksTask collectBlocksTask = (CollectBlocksTask) e.getValue();
                TaskGraph taskGraph = e.getKey().getTaskGraph();
                Status prevStatus = taskGraph.getTaskStatus(collectBlocksTask);

                if (stackId.equalsIgnoreCase(collectBlocksTask.getItemId())) {
                    collectBlocksTask.setAmount(collectBlocksTask.getAmount() + amountChange);
                }

                Status status = taskGraph.getTaskStatus(collectBlocksTask);
                if (status != prevStatus && status.isComplete()) {
                    EntityRef entity = e.getKey().getEntity();
                    entity.send(new TaskCompletedEvent(e.getKey(), collectBlocksTask, status.isSuccess()));
                }
            });
        charEntity.saveComponent(playerQuestComponent);
    }
}
