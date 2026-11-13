package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

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


    // Handle the number keys used to select the available hotbar items.
    public void handleSelectionInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            selectSlot(0);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            selectSlot(1);
        }
    }

    // Select a hotbar slot.
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

    // Use the currently selected item.
    public String useSelectedItem() {

        if (quantities[selectedSlot] <= 0) {
            return null;
        }

        // Shield is passive and cannot be manually used.
        if (items[selectedSlot].equals("Shield")) {
            return null;
        }

        quantities[selectedSlot]--;

        return items[selectedSlot];
    }

    // Check whether a shield is available.
    public boolean hasShield() {
        return quantities[1] > 0;
    }

    // Consume shield after blocking a hit.
    public void consumeShield() {
        if (hasShield()) {
            quantities[1]--;
        }
    }

    /** Draws the complete nine-slot inventory hotbar. */
    public void drawHotbar(
        ShapeRenderer shapeRenderer,
        SpriteBatch batch,
        BitmapFont font,
        Texture breadTexture,
        Texture shieldTexture,
        float screenWidth
    ) {
        float slotSize = 55f;
        float gap = 6f;
        float totalWidth = (slotSize * items.length) + (gap * (items.length - 1));
        float startX = (screenWidth - totalWidth) / 2f;
        float startY = 15f;

        // Draw slot backgrounds.
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < items.length; i++) {
            float x = startX + i * (slotSize + gap);
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(x, startY, slotSize, slotSize);
        }

        shapeRenderer.end();

        // Highlight the selected slot.
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.85f, 0f, 1f);

        float selectedX = startX + selectedSlot * (slotSize + gap);
        shapeRenderer.rect(selectedX, startY, slotSize, slotSize);
        shapeRenderer.end();

        // Draw slot numbers, item icons and quantities.
        batch.begin();
        font.getData().setScale(1f);

        for (int i = 0; i < items.length; i++) {
            float x = startX + i * (slotSize + gap);

            font.draw(batch, String.valueOf(i + 1), x + 5, startY + slotSize - 5);

            if (items[i].equals("Bread")) {
                batch.draw(breadTexture, x + 11, startY + 15, 33, 30);
            } else if (items[i].equals("Shield")) {
                batch.draw(shieldTexture, x + 12, startY + 14, 31, 33);
            }

            if (quantities[i] > 0) {
                font.draw(batch, "x" + quantities[i], x + slotSize - 22, startY + 9);
            }
        }

        font.getData().setScale(2f);
        batch.end();
    }

    // Restore starting inventory when restarting.
    public void reset() {
        quantities[0] = 3;
        quantities[1] = 1;

        selectedSlot = 0;
    }
}
