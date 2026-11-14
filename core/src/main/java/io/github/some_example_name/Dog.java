package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

// POSITIVE HIDDEN EVENT: border collie; chases and disperses geese
public class Dog {

    private static final float SPEED = 350f;
    private static final float CATCH_RADIUS = 24f;
    private static final float DURATION = 10f;

    private static final float DRAW_SIZE = 64f;
    private static final float BODY_CENTRE_OFFSET = 0.45f;

    private static final float WALK_FRAME_TIME = 0.1f;
    private static final float DOWN_BOB_TIME = 0.15f;
    private static final float DOWN_BOB_HEIGHT = 3f;
    private static final float BARK_DURATION = 0.3f;

    private final Texture[] right = new Texture[4];
    private final Texture[] up = new Texture[4];
    private final Texture down;
    private final Texture bark;

    private boolean active = false;
    private float x;
    private float y;
    private float timeLeft = 0f;
    private float barkTimer = 0f;
    private float animTimer = 0f;

    // most recent movement direction, used to pick which sprite to show
    private float dirX = 1f;
    private float dirY = 0f;

    public Dog() {
        for (int i = 0; i < 4; i++) {
            right[i] = new Texture(Gdx.files.internal("dog/dog_right_" + i + ".png"));
            up[i] = new Texture(Gdx.files.internal("dog/dog_up_" + i + ".png"));
        }
        down = new Texture(Gdx.files.internal("dog/dog_down.png"));
        bark = new Texture(Gdx.files.internal("dog/dog_bark.png"));
    }

    public void start(float startX, float startY) {
        x = startX;
        y = startY;
        timeLeft = DURATION;
        barkTimer = 0f;
        animTimer = 0f;
        active = true;
    }

    public void reset() {
        active = false;
    }

    public void update(float delta, GameContext ctx) {

        if (!active) {
            return;
        }

        animTimer += delta;
        barkTimer = Math.max(0f, barkTimer - delta);
        timeLeft -= delta;

        Goose target = timeLeft > 0f ? nearestGoose(ctx.geese) : null;

        if (target != null) {

            moveTowards(target.getX(), target.getY(), SPEED * delta);
            catchGeese(ctx.geese);

        } else {

            float width = ctx.getWorldWidth();
            float exitX = x < width / 2f ? -DRAW_SIZE * 2f : width + DRAW_SIZE * 2f;

            moveTowards(exitX, y, SPEED * delta);

            if (x < -DRAW_SIZE || x > width + DRAW_SIZE) {
                active = false;
            }
        }
    }

    private Goose nearestGoose(List<Goose> geese) {

        Goose nearest = null;
        float best = Float.MAX_VALUE;

        for (Goose goose : geese) {

            if (goose.getState() == GooseState.DISPERSED) {
                continue;
            }

            float dx = goose.getX() - x;
            float dy = goose.getY() - y;
            float sqDist = dx * dx + dy * dy;

            if (sqDist < best) {
                best = sqDist;
                nearest = goose;
            }
        }
        return nearest;
    }

    private void moveTowards(float targetX, float targetY, float maxStep) {
        float dx = targetX - x;
        float dy = targetY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist == 0f) {
            return;
        }

        dirX = dx / dist;
        dirY = dy / dist;

        float step = Math.min(maxStep, dist);

        x += dirX * step;
        y += dirY * step;
    }

    private void catchGeese(List<Goose> geese) {

        for (Goose goose : geese) {

            if (goose.getState() == GooseState.DISPERSED) {
                continue;
            }

            float dx = goose.getX() - x;
            float dy = goose.getY() - y;
            float reach = CATCH_RADIUS + goose.getRadius();
            float sqDist = dx * dx + dy * dy;
            if (sqDist <= reach * reach) {
                goose.disperse();
                barkTimer = BARK_DURATION;
            }
        }
    }

    public void draw(SpriteBatch batch) {

        if (!active) {
            return;
        }

        Texture frame;
        boolean flip = dirX < 0f;
        float bob = 0f;

        int walkFrame = (int) (animTimer / WALK_FRAME_TIME) % 4;

        if (barkTimer > 0f) {

             frame = bark;

        } else if (Math.abs(dirX) >= Math.abs(dirY)) {

            frame = right[walkFrame];

        } else if (dirY > 0f) {

            frame = up[walkFrame];
            flip = false;

        } else {

            frame = down;
            flip = false;

            if ((int) (animTimer / DOWN_BOB_TIME) % 2 == 1) {
                bob = DOWN_BOB_HEIGHT;
            }
        }

        batch.draw(
          frame,
          x - DRAW_SIZE / 2f,
          y - DRAW_SIZE * BODY_CENTRE_OFFSET + bob,
          DRAW_SIZE,
          DRAW_SIZE,
          0, 0,
          frame.getWidth(),
          frame.getHeight(),
          flip,
          false
        );
    }

    public void dispose() {
        for (int i = 0; i < 4; i++) {
            right[i].dispose();
            up[i].dispose();
        }
        down.dispose();
        bark.dispose();
    }
}
