
package io.github.some_example_name;

public class Inventory {

    private final String[] items = {
        "Bread",
        "Shield",
        "",
        "",
        "",
        "",
        "",
        "",
        ""
    };

    private final int[] quantities = {
        3, 1, 0, 0, 0, 0, 0, 0, 0
    };

    private int selectedSlot = 0;

    // Select a hotbar slot
    public void selectSlot(int slot) {
        if (slot >= 0 && slot < items.length) {
            selectedSlot = slot;
        }
    }

    // Get currently selected slot
    public int getSelectedSlot() {
        return selectedSlot;
    }

    // Get item name in a particular slot
    public String getItem(int slot) {
        return items[slot];
    }

    // Get quantity in a particular slot
    public int getQuantity(int slot) {
        return quantities[slot];
    }

    // Get currently selected item
    public String getSelectedItem() {
        return items[selectedSlot];
    }

    // Use currently selected item
    public String useSelectedItem() {

        if (quantities[selectedSlot] <= 0) {
            return null;
        }

        quantities[selectedSlot]--;

        return items[selectedSlot];
    }

    // Check whether a shield is available
    public boolean hasShield() {
        return quantities[1] > 0;
    }

    // Consume shield after blocking a hit
    public void consumeShield() {
        if (quantities[1] > 0) {
            quantities[1]--;
        }
    }
}

