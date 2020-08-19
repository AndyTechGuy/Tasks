// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.tasks.components;

import org.terasology.entitySystem.Component;

public class CollectBlocksTaskComponent implements Component {

    public String itemId;

    public int targetAmount;

    public int amountGathered;

    public boolean destroyItemsOnComplete;

    public CollectBlocksTaskComponent(String itemId, int targetAmount) {
        this.itemId = itemId;
        this.targetAmount = targetAmount;
    }

    public CollectBlocksTaskComponent() { }

}
