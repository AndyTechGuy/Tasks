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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Rect2f;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.tasks.Quest;
import org.terasology.tasks.components.PlayerQuestComponent;
import org.terasology.world.time.WorldTimeEvent;

@RegisterSystem(RegisterMode.CLIENT)
public class QuestHudSystem extends BaseComponentSystem {

    public static final String HUD_ELEMENT_ID = "Tasks:QuestHud";

    @In
    private NUIManager nuiManager;

    @In
    private LocalPlayer localPlayer;

    private QuestHud questHud;

    private static final Logger logger = LoggerFactory.getLogger(QuestHudSystem.class);

    @Override
    public void initialise() {
        Rect2f rc = Rect2f.createFromMinAndSize(0, 0, 1, 1);
        questHud = nuiManager.getHUD().addHUDElement(HUD_ELEMENT_ID, QuestHud.class, rc);
    }

    @ReceiveEvent(components = {PlayerQuestComponent.class})
    public void onComponentChanged(OnChangedComponent onChangedComponent, EntityRef entity) {
        updateQuestInformation();
    }

    @ReceiveEvent
    public void onWorldTimeEvent(WorldTimeEvent worldTimeEvent, EntityRef entity) {
        updateQuestInformation();
    }

    @ReceiveEvent
    public void onToggleMinimapButton(ToggleQuestsButton event, EntityRef entity) {
        if (event.isDown()) {
            questHud.setVisible(!questHud.isVisible());
            updateQuestInformation();
            event.consume();
        }
    }

    private void updateQuestInformation() {
        PlayerQuestComponent playerQuestComponent = localPlayer.getClientEntity().getComponent(PlayerQuestComponent.class);

        if (playerQuestComponent == null) {
            logger.warn("Player doesn't have a PlayerQuestComponent!");
            questHud.setVisible(false);
            return;
        }

        UIList<Quest> questListUI = questHud.find("questList", UIList.class);
        questListUI.setList(playerQuestComponent.questList);
    }

}
