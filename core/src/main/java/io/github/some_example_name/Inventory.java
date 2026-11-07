
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

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public String getItem(int slot) {
        return items[slot];
    }

    public int getQuantity(int slot) {
        return quantities[slot];
    }

    public String getSelectedItem() {
        return items[selectedSlot];
    }

    // Use the currently selected item
    public String useSelectedItem() {

        if (quantities[selectedSlot] <= 0) {
            return null;
        }

        // Shield is passive and cannot be manually used
        if (items[selectedSlot].equals("Shield")) {
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
        if (hasShield()) {
            quantities[1]--;
        }
    }

    // Restore starting inventory when restarting
    public void reset() {
        quantities[0] = 3;
        quantities[1] = 1;

        selectedSlot = 0;
    }
}
