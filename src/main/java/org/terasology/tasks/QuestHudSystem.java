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
package org.terasology.tasks;

import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Rect2f;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;

@RegisterSystem(RegisterMode.CLIENT)
public class QuestHudSystem extends BaseComponentSystem {

    public static final String HUD_ELEMENT_ID = "Tasks:QuestHud";

    @In
    private NUIManager nuiManager;

    @Override
    public void initialise() {
        Rect2f rc = Rect2f.createFromMinAndSize(0, 0, 0.5f, 1);
        nuiManager.getHUD().addHUDElement(HUD_ELEMENT_ID, QuestHud.class, rc);
    }

}
