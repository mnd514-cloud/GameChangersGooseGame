package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Player {

    // Movement and collision
    private static final float SPEED = 300f;
    private static final float RADIUS = 15f;

    // Sprite sheet layout: 209 x 326 pixels, 23 x 36 frames, one-pixel border
    private static final int FRAME_WIDTH = 23;
    private static final int FRAME_HEIGHT = 36;
    private static final int SHEET_ROWS = 9;
    private static final int SHEET_COLS = 9;
    private static final float FRAME_DURATION = 0.12f;

    // Size the sprite is drawn on screen
    private static final float SPRITE_WIDTH = 46f;
    private static final float SPRITE_HEIGHT = 72f;

    // Facing directions
    private static final int DOWN = 0;
    private static final int LEFT = 1;
    private static final int UP = 2;
    private static final int RIGHT = 3;

    private float x;
    private float y;

    private final Texture texture;

    private final Animation<TextureRegion> downAnimation;
    private final Animation<TextureRegion> leftAnimation;
    private final Animation<TextureRegion> upAnimation;
    private final Animation<TextureRegion> rightAnimation;

    private float animationTimer = 0f;
    private boolean moving = false;
    private int facingDirection = DOWN;

    public Player(float startX, float startY) {

        x = startX;
        y = startY;

        texture = new Texture(Gdx.files.internal("MCSprite sheet.png"));

        texture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

        // Split the sprite sheet into individual frames.
        TextureRegion[][] frames = new TextureRegion[SHEET_ROWS][SHEET_COLS];

        for (int row = 0; row < SHEET_ROWS; row++) {
            for (int col = 0; col < SHEET_COLS; col++) {

                int frameX = 1 + col * FRAME_WIDTH;
                int frameY = texture.getHeight() - (1 + (row + 1) * FRAME_HEIGHT);

                frames[row][col] = new TextureRegion(
                    texture, frameX, frameY, FRAME_WIDTH, FRAME_HEIGHT
                );
            }
        }

        downAnimation = createAnimation(frames[0]);
        leftAnimation = createAnimation(frames[2]);
        upAnimation = createAnimation(frames[4]);
        rightAnimation = createAnimation(frames[6]);
    }

    private Animation<TextureRegion> createAnimation(TextureRegion[] row) {

        Animation<TextureRegion> animation = new Animation<>(FRAME_DURATION, row);
        animation.setPlayMode(Animation.PlayMode.LOOP);

        return animation;
    }

    // Read WASD, move the player and update the animation.
    // Only called while the game is running.
    public void update(float delta, CollisionManager collisionManager) {

        float movement = SPEED * delta;

        float moveX = 0f;
        float moveY = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveY += movement;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) moveY -= movement;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveX -= movement;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveX += movement;

        updateDirection(moveX, moveY);

        // Let the collision manager stop the player walking through trees.
        Vector2 newPosition = collisionManager.movePlayer(
            x, y, moveX, moveY, RADIUS,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        x = newPosition.x;
        y = newPosition.y;

        // Animate while moving, show the first frame when standing still.
        if (moving) {
            animationTimer += delta;
        } else {
            animationTimer = 0f;
        }
    }

    // Use the dominant movement axis to decide which way the player faces.
    private void updateDirection(float moveX, float moveY) {

        moving = moveX != 0 || moveY != 0;

        if (!moving) {
            return;
        }

        if (Math.abs(moveX) > Math.abs(moveY)) {
            facingDirection = moveX < 0 ? LEFT : RIGHT;
        } else {
            facingDirection = moveY < 0 ? DOWN : UP;
        }
    }

    private Animation<TextureRegion> getAnimation() {

        switch (facingDirection) {
            case LEFT:
                return leftAnimation;
            case UP:
                return upAnimation;
            case RIGHT:
                return rightAnimation;
            default:
                return downAnimation;
        }
    }

    // Draw the current animation frame. The batch must already be started.
    public void draw(SpriteBatch batch) {

        TextureRegion frame = getAnimation().getKeyFrame(animationTimer);

        batch.draw(
            frame,
            x - SPRITE_WIDTH / 2f,
            y - SPRITE_HEIGHT / 2f,
            SPRITE_WIDTH,
            SPRITE_HEIGHT
        );
    }

    // Put the player back at the start, facing down.
    public void reset(float startX, float startY) {

        x = startX;
        y = startY;

        animationTimer = 0f;
        moving = false;
        facingDirection = DOWN;
    }

    public void dispose() {
        texture.dispose();
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getRadius() { return RADIUS; }
}
