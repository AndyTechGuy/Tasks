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
package org.terasology.tasks.gui;

import java.util.ArrayList;
import java.util.List;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.tasks.systems.QuestAuthoritySystem;

public class QuestHud extends CoreHudWidget {
    private UIList<EntityRef> questList;

    @In
    private QuestAuthoritySystem questAuthoritySystem;

    @In
    private LocalPlayer localPlayer;

    @Override
    public void initialise() {
        questList = find("questList", UIList.class);
        if (questList != null) {
            questList.setItemRenderer(new QuestRenderer());
        }
    }

    public UIList<EntityRef> getQuestList() {
        return questList;
    }
}

