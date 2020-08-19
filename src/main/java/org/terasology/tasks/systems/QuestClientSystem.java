// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.tasks.systems;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.math.geom.Rect2f;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.tasks.components.QuestComponent;
import org.terasology.tasks.gui.QuestHud;
import org.terasology.tasks.gui.ToggleQuestsButton;

import java.util.List;

@RegisterSystem(RegisterMode.CLIENT)
public class QuestClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    public static final String HUD_ELEMENT_ID = "Tasks:QuestHud";

    @In
    private EntityManager entityManager;

    @In
    private NUIManager nuiManager;

    private QuestHud questHud;

    @Override
    public void initialise() {
        Rect2f rc = Rect2f.createFromMinAndSize(0, 0, 1, 1);
        questHud = nuiManager.getHUD().addHUDElement(HUD_ELEMENT_ID, QuestHud.class, rc);
    }

    @ReceiveEvent
    public void onToggleMinimapButton(ToggleQuestsButton event, EntityRef entity) {
        if (event.isDown()) {
            questHud.setVisible(!questHud.isVisible());
            event.consume();
        }
    }

    @Override
    public void update(float delta) {
        List<EntityRef> questList = Lists.newArrayList();
        for (EntityRef questEntity : entityManager.getEntitiesWith(QuestComponent.class)) {
            questList.add(questEntity);
        }

        questHud.getQuestList().setList(questList);
    }
}
