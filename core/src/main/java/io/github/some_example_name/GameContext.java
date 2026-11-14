package io.github.some_example_name;

import java.util.ArrayList;
import java.util.List;

// Shared game state passed to the systems that need it.
public class GameContext {

    public final Player player;
    public final Inventory inventory;
    public final List<Goose> geese = new ArrayList<>();

    private float worldWidth;
    private float worldHeight;

    private int itemsUsed = 0;
    private int eventsInteracted = 0;

    public GameContext(
        Player player,
        Inventory inventory,
        float worldWidth,
        float worldHeight
    ) {
        this.player = player;
        this.inventory = inventory;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public float getWorldWidth() {
        return worldWidth;
    }

    public float getWorldHeight() {
        return worldHeight;
    }

    public void setWorldSize(float width, float height) {
        worldWidth = width;
        worldHeight = height;
    }

    public int getItemsUsed() {
        return itemsUsed;
    }

    public int getEventsInteracted() {
        return eventsInteracted;
    }

    public void recordItemUsed() {
        itemsUsed++;
    }

    public void recordEvent() {
        eventsInteracted++;
    }

    public void resetStats() {
        itemsUsed = 0;
        eventsInteracted = 0;
    }

}
