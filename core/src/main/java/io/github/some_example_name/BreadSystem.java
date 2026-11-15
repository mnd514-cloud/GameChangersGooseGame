package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

import java.util.Random;

public class BreadSystem {

    private static final float BREAD_DURATION = 5f;
    private static final float BREAD_SPEED = 250f;

    private static final float BREAD_RING_BASE = 15f;
    private static final float BREAD_RING_PER_GOOSE = 8f;
    private static final float BREAD_RING_MAX = 200f;

    private static final float BREAD_FLIGHT_TIME = 0.6f;
    private static final float BREAD_ARC_HEIGHT = 120f;
    private static final float BREAD_THROW_MIN = 150f;
    private static final float BREAD_THROW_MAX = 350f;

    private final Texture texture;
    private final Random random = new Random();

    private boolean active = false;
    private float x;
    private float y;
    private float timer;

    private boolean flying = false;
    private float flightTimer;
    private float startX;
    private float startY;
    private float targetX;
    private float targetY;

    public BreadSystem(Texture texture) {
        this.texture = texture;
    }

    public void startThrow(Player player, float width, float height) {
        startX = player.getX();
        startY = player.getY();

        float angle = random.nextFloat() * MathUtils.PI2;

        float distance = BREAD_THROW_MIN
            + random.nextFloat() * (BREAD_THROW_MAX - BREAD_THROW_MIN);

        targetX = MathUtils.clamp(
            startX + distance * MathUtils.cos(angle),
            0f,
            width
        );

        targetY = MathUtils.clamp(
            startY + distance * MathUtils.sin(angle),
            0f,
            height
        );

        flightTimer = 0f;
        flying = true;
    }

    public void updateFlight(float delta) {
        if (!flying) {
            return;
        }

        flightTimer += delta;

        if (flightTimer >= BREAD_FLIGHT_TIME) {
            flying = false;
            active = true;
            x = targetX;
            y = targetY;
            timer = BREAD_DURATION;
        }
    }

    public void updateGeese(
        float delta,
        GameContext ctx,
        float width,
        float height
    ) {
        if (active) {
            timer -= delta;

            if (timer <= 0f) {
                active = false;

                for (Goose goose : ctx.geese) {
                    goose.stopFleeing();
                }
            }
        }

        float movementDelta = delta * ctx.getGooseSpeedMultiplier();

        float ringRadius = Math.min(
            BREAD_RING_BASE + ctx.geese.size() * BREAD_RING_PER_GOOSE,
            BREAD_RING_MAX
        );

        for (int i = 0; i < ctx.geese.size(); i++) {
            Goose goose = ctx.geese.get(i);

            if (active) {
                float angle = (float) (
                    2 * Math.PI * i / ctx.geese.size()
                );

                float targetGooseX = MathUtils.clamp(
                    x + ringRadius * (float) Math.cos(angle),
                    22f,
                    width - 22f
                );

                float targetGooseY = MathUtils.clamp(
                    y + ringRadius * (float) Math.sin(angle),
                    22f,
                    height - 22f
                );

                goose.lureTowards(
                    targetGooseX,
                    targetGooseY,
                    BREAD_SPEED * movementDelta
                );

            } else {
                goose.update(
                    movementDelta,
                    ctx.getWorldWidth(),
                    ctx.getWorldHeight()
                );
            }
        }
    }

    public void reset() {
        active = false;
        timer = 0f;
        flying = false;
        flightTimer = 0f;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isFlying() {
        return flying;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getFlightProgress() {
        return Math.min(flightTimer / BREAD_FLIGHT_TIME, 1f);
    }

    public float getFlightX() {
        return MathUtils.lerp(startX, targetX, getFlightProgress());
    }

    public float getFlightY() {
        return MathUtils.lerp(startY, targetY, getFlightProgress());
    }

    public float getFlightHeight() {
        float t = getFlightProgress();
        return 4f * t * (1f - t) * BREAD_ARC_HEIGHT;
    }

    public float getTargetX() {
        return targetX;
    }

    public float getTargetY() {
        return targetY;
    }

    public Texture getTexture() {
        return texture;
    }
}
