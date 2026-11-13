package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

// Draws everything shown on top of the game world: the timer and counters,
// the start / pause / win / loss overlays, the instructions screen and the hotbar.

public class HUD {
    private static final float TEXT_SCALE = 2f;
    private static final float INSTRUCTIONS_SCALE = 1.5f;
    private static final float MARGIN = 20f;
    private static final float LINE_SPACING = 40f;

    private static final String[] INSTRUCTIONS = {
        "HOW TO PLAY",
        "",
        "Dodge the geese for 5 minutes to win.",
        "Touch a goose and you lose!",
        "",
        "CONTROLS",
        "W A S D - move",
        "1 / 2 - select bread / shield",
        "E - use selected item",
        "ESC - pause / resume",
        "R - restart after winning or losing",
        "",
        "ITEMS",
        "Bread - throw it to lure the geese away for 5 seconds",
        "Shield - automatically blocks one hit",
        "",
        "Press I or ESC to go back"
    };

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final Texture breadTexture;
    private final Texture shieldTexture;

    private final BitmapFont font;
    private final GlyphLayout layout;

    public HUD(
        SpriteBatch batch,
        ShapeRenderer shapeRenderer,
        Texture breadTexture,
        Texture shieldTexture
    ) {
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.breadTexture = breadTexture;
        this.shieldTexture = shieldTexture;

        font = new BitmapFont();
        font.getData().setScale(TEXT_SCALE);

        layout = new GlyphLayout();
    }

    // Timer, counters and the speed boost warning in the top-left corner.
    public void drawStats(
        float timeRemaining,
        int itemsUsed,
        int eventsInteracted,
        boolean speedBoostActive,
        float speedBoostTimer
    ) {
        float top = Gdx.graphics.getHeight() - MARGIN;

        int displayedTime = (int) Math.ceil(timeRemaining);
        String timerText = String.format("%02d:%02d", displayedTime / 60, displayedTime % 60);

        batch.begin();

        font.draw(batch, timerText, MARGIN, top);
        font.draw(batch, "Items used: " + itemsUsed, MARGIN, top - LINE_SPACING);
        font.draw(batch, "Events triggered: " + eventsInteracted, MARGIN, top - LINE_SPACING * 2);

        if (speedBoostActive) {
            font.setColor(1, 0.3f, 0.3f, 1);
            font.draw(
                batch,
                "GEESE ENRAGED! " + (int) Math.ceil(speedBoostTimer) + "s",
                MARGIN,
                top - LINE_SPACING * 3
            );
            font.setColor(1, 1, 1, 1);
        }

        batch.end();
    }

    // Start, pause, instructions, win and loss screens.
    public void drawScreen(GameState state, boolean started) {

        switch (state) {
            case PAUSED:
                if (started) {
                    drawOverlay("PAUSED", "ESC to resume | I for instructions");
                } else {
                    drawOverlay("GOOSE GAME", "SPACE to start | I for instructions");
                }
                break;

            case INSTRUCTIONS:
                drawInstructions();
                break;

            case LOST:
                drawOverlay("YOU LOST", "Press R to restart");
                break;

            case WON:
                drawOverlay("YOU WIN!", "Press R to restart");
                break;

            default:
                break;
        }
    }

    // Inventory hotbar along the bottom of the screen.
    public void drawHotbar(Inventory inventory) {
        inventory.drawHotbar(
            shapeRenderer,
            batch,
            font,
            breadTexture,
            shieldTexture,
            Gdx.graphics.getWidth()
        );
    }

    // Darken the screen and show a centred title with a hint underneath.
    private void drawOverlay(String title, String hint) {

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        dimScreen(0.6f);

        batch.begin();
        font.setColor(1, 1, 1, 1);

        layout.setText(font, title);
        font.draw(batch, layout, (width - layout.width) / 2f, height / 2f + 40f);

        layout.setText(font, hint);
        font.draw(batch, layout, (width - layout.width) / 2f, height / 2f - 10f);

        batch.end();
    }

    // Darken the screen and list the controls and items.
    private void drawInstructions() {

        float width = Gdx.graphics.getWidth();
        float y = Gdx.graphics.getHeight() - 50f;

        dimScreen(0.85f);

        batch.begin();
        font.getData().setScale(INSTRUCTIONS_SCALE);
        font.setColor(1, 1, 1, 1);

        for (String line : INSTRUCTIONS) {
            layout.setText(font, line);
            font.draw(batch, layout, (width - layout.width) / 2f, y);
            y -= 32f;
        }

        font.getData().setScale(TEXT_SCALE);
        batch.end();
    }

    // Draw a see-through black rectangle over the whole screen.
    private void dimScreen(float alpha) {

        Gdx.gl.glEnable(GL20.GL_BLEND);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, alpha);
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void dispose() {
        font.dispose();
    }
}
