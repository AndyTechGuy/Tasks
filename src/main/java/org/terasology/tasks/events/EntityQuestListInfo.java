package org.terasology.tasks.events;

import org.terasology.entitySystem.event.Event;
import org.terasology.network.OwnerEvent;
import org.terasology.tasks.Quest;

import java.util.List;

@OwnerEvent
public class EntityQuestListInfo implements Event {

    public List<Quest> questList;

    public EntityQuestListInfo(List<Quest> quests) {
        this.questList = quests;
    }

    public EntityQuestListInfo() {

    }

}
