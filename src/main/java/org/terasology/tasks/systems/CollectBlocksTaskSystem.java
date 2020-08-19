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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.inventory.events.InventorySlotStackSizeChangedEvent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.tasks.Status;
import org.terasology.tasks.components.CollectBlocksTaskComponent;
import org.terasology.tasks.components.QuestComponent;
import org.terasology.tasks.components.TaskComponent;
import org.terasology.tasks.events.EndTaskEvent;
import org.terasology.tasks.events.StartTaskEvent;
import org.terasology.tasks.events.TaskCompletedEvent;
import org.terasology.world.block.items.BlockItemComponent;

/**
 *
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class CollectBlocksTaskSystem extends BaseComponentSystem {

    @In
    private InventoryManager inventoryManager;

    @In
    private EntityManager entityManager;

    @ReceiveEvent
    public void onTaskStart(StartTaskEvent event, EntityRef taskEntity) {
        if (taskEntity.hasComponent(CollectBlocksTaskComponent.class)) {
            CollectBlocksTaskComponent collectBlocksTaskComponent = taskEntity.getComponent(CollectBlocksTaskComponent.class);
            EntityRef charEntity = taskEntity.getOwner().getComponent(ClientComponent.class).character;

            InventoryComponent inventory = charEntity.getComponent(InventoryComponent.class);
            for (EntityRef itemRef : inventory.itemSlots) {
                ItemComponent item = itemRef.getComponent(ItemComponent.class);
                if (item != null && item.stackId.equalsIgnoreCase(collectBlocksTaskComponent.itemId)) {
                    collectBlocksTaskComponent.amountGathered += item.stackCount;
                }

                checkTargetAmount(taskEntity, collectBlocksTaskComponent);
            }
            taskEntity.saveComponent(collectBlocksTaskComponent);
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
        entityManager.getEntitiesWith(CollectBlocksTaskComponent.class).forEach(collectBlocksEntity -> {
            if (collectBlocksEntity.getOwner().getOwner().equals(charEntity.getOwner())) {
                if (collectBlocksEntity.getComponent(TaskComponent.class).taskStatus.equals(Status.ACTIVE)) {
                    CollectBlocksTaskComponent collectBlocksTaskComponent = collectBlocksEntity.getComponent(CollectBlocksTaskComponent.class);
                    if (collectBlocksTaskComponent.itemId.equalsIgnoreCase(stackId)) {
                        collectBlocksTaskComponent.amountGathered += amountChange;
                        checkTargetAmount(collectBlocksEntity, collectBlocksTaskComponent);
                    }
                }
            }
        });
    }

    private void checkTargetAmount(EntityRef taskEntity, CollectBlocksTaskComponent taskComponent) {
        if (taskComponent.amountGathered >= taskComponent.targetAmount) {
            if (taskComponent.destroyItemsOnComplete) {
                EntityRef character = taskEntity.getOwner().getOwner().getComponent(ClientComponent.class).character;
                destroyItemOrBlock(character, taskComponent.itemId, taskComponent.targetAmount);
            }

            taskEntity.send(new EndTaskEvent(taskEntity.getComponent(TaskComponent.class), true));
        }
    }

    private boolean destroyItemOrBlock(EntityRef character, String name, int amount) {
        EntityRef item = EntityRef.NULL;
        for (int i = 0; i < inventoryManager.getNumSlots(character); i++) {
            EntityRef current = inventoryManager.getItemInSlot(character, i);

            if (EntityRef.NULL.equals(current)) {
                continue;
            }

            if (name.equalsIgnoreCase(current.getParentPrefab().getName())) {
                item = current;
                break;
            }

            if (current.getParentPrefab().getName().equalsIgnoreCase("engine:blockItemBase")) {
                if (current.getComponent(BlockItemComponent.class).blockFamily.getURI().toString().equalsIgnoreCase(name)) {
                    item = current;
                    break;
                }
            }
        }
        if (item == EntityRef.NULL) {
            return false;
        }

        return inventoryManager.removeItem(character, EntityRef.NULL, item, true, amount) == EntityRef.NULL;
    }
}
