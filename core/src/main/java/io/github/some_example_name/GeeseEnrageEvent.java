
package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GeeseEnrageEvent implements HiddenEvent {

    private static final float ZONE_SIZE = 100f;
    private static final float DURATION = 10f;
    private static final float SPEED_MULTIPLIER = 1.75f;

    private final Texture birdsNestTexture;

    private boolean triggered = false;
    private boolean active = false;
    private float timer = 0f;

    public GeeseEnrageEvent() {
        birdsNestTexture = new Texture(
            Gdx.files.internal("bird_nest.png")
        );
    }

    @Override
    public void update(float delta, GameContext ctx) {
        if (!triggered
            && ctx.player.getX() > ctx.getWorldWidth() - ZONE_SIZE
            && ctx.player.getY() > ctx.getWorldHeight() - ZONE_SIZE) {

            triggered = true;
            active = true;
            timer = DURATION;

            ctx.setGooseSpeedMultiplier(SPEED_MULTIPLIER);
            ctx.recordEvent();

            Gdx.app.log("HiddenEvent", "geese speed boost event triggered");
        }

        if (active) {
            timer -= delta;

            if (timer <= 0f) {
                active = false;
                ctx.setGooseSpeedMultiplier(1f);
            }
        }
    }

    @Override
    public void drawShapes(ShapeRenderer shapes) {
        // No shape needed: the nest image replaces the blue square.
    }

    @Override
    public void drawSprites(SpriteBatch batch) {
        if (!triggered) {
            return;
        }

        batch.draw(
            birdsNestTexture,
            Gdx.graphics.getWidth() - ZONE_SIZE,
            Gdx.graphics.getHeight() - ZONE_SIZE,
            ZONE_SIZE,
            ZONE_SIZE
        );
    }

    @Override
    public String getStatusText() {
        return active
            ? "GEESE ENRAGED! " + (int) Math.ceil(timer) + "s"
            : null;
    }

    @Override
    public void reset(GameContext ctx) {
        triggered = false;
        active = false;
        timer = 0f;
        ctx.setGooseSpeedMultiplier(1f);
    }

    @Override
    public void dispose() {
        birdsNestTexture.dispose();
    }
}
