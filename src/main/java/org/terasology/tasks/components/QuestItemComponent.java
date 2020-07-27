package org.terasology.tasks.components;

import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;
import org.terasology.tasks.TaskGraph;

/**
 * Attaches to a quest item with initial information about a quest & the individual tasks it contains. When the item
 * is activated, a {@link org.terasology.tasks.DefaultQuest} is created and stored
 * in {@link org.terasology.tasks.systems.QuestSystem}.
 */
public class QuestItemComponent implements Component {

    /**
     * The title of this quest. Displayed on the player's UI.
     */
    @Replicate
    public String shortName;

    /**
     * A short description of what this quest contains. Displayed on the player's UI.
     */
    @Replicate
    public String description;

    /**
     * A list of tasks that this quest contains. Must be completed in order.
     */
    @Replicate
    public TaskGraph tasks;

}
