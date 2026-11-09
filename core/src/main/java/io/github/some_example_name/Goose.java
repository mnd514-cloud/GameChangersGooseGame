package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;


public class Goose {

    // Appearance and Size
    private final Texture texture;
    private final float radius;

    // How this goose normally moves
    private final MovementType movementType;
    private final float speed;

    // Current position (center of the goose)
    private float x;
    private float y;

    // Starting position, used when restarting
    private final float startX;
    private final float startY;

    // Direction for straight-line movement
    private int xDirection = 1;
    private int yDirection = 1;

    // Only used by circle geese
    private float centerX;
    private float centerY;
    private float orbitRadius;
    private float angle = 0f;

    // What the goose is currently doing
    private GooseState state = GooseState.NORMAL;

    private static final float RETURN_SPEED = 100f;

    // how far in pixels a circle goose may be from its orbit point
    // before it glides back
    private static final float ORBIT_TOLERANCE = 2f;

    // Constructor for HORIZONTAL, VERTICAL AND DIAGONAL Geese
    public Goose(Texture texture, MovementType movementType,
                 float x, float y, float speed, float radius){
        this.texture = texture;
        this.movementType = movementType;
        this.x = x;
        this.y = y;
        this.startX = x;
        this.startY = y;
        this.speed = speed;
        this.radius = radius;
    }

    // Constructor for CIRCLE geese
    // (speed here is how fast the goose goes round in radians per second)
    public Goose(Texture texture, float centerX, float centerY,
                 float orbitRadius, float angularSpeed, float radius){
        this.texture = texture;
        this.movementType = MovementType.CIRCLE;
        this.centerX = centerX;
        this.centerY = centerY;
        this.orbitRadius = orbitRadius;
        this.speed = angularSpeed;
        this.radius = radius;

        // Start on the right-hand side of the circle
        this.x = centerX + orbitRadius;
        this.y = centerY;
        this.startX = this.x;
        this.startY = this.y;
    }

    // Normal movement called every frame
    public void update(float delta) {
        if (state != GooseState.NORMAL) {
            return;
        }

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        switch (movementType) {

            case HORIZONTAL:
                x += speed * xDirection * delta;
                xDirection = bounce(x, xDirection, width);
                break;

            case VERTICAL:
                y += speed * yDirection * delta;
                yDirection = bounce(y, yDirection, height);
                break;

            case DIAGONAL:
                x += speed * xDirection * delta;
                y += speed * yDirection * delta;
                xDirection = bounce(x, xDirection, width);
                yDirection = bounce(y, yDirection, height);
                break;

            case CIRCLE:
                // where goose should be on its orbit at this moment
                float targetX = orbitX(angle);
                float targetY = orbitY(angle);

                float dx = targetX - x;
                float dy = targetY - y;

                if (dx * dx + dy * dy > ORBIT_TOLERANCE * ORBIT_TOLERANCE) {
                    // off orbit, goose glides back with its angle frozen
                    moveTowards(targetX, targetY, RETURN_SPEED * delta);
                } else {
                    angle += speed * delta;
                    x = orbitX(angle);
                    y = orbitY(angle);
                }
                break;
        }
    }

    private float orbitX(float angle) {
        return centerX + orbitRadius * (float) Math.cos(angle);
    }

    private float orbitY(float angle) {
        return centerY + orbitRadius * (float) Math.sin(angle);
    }

    // flips direction at either window edge: -1 at right/ top, 1 at left/ bottom
    private int bounce(float position, int direction, float limit) {
        if (position + radius >= limit) {
            return -1;
        }
        if (position - radius <= 0) {
            return 1;
        }
        return direction;
    }

    // Move towards a point without overshooting. Used for bread lure and orbit glide
    private void moveTowards(float targetX, float targetY, float maxStep) {
        x = stepTowards(x, targetX, maxStep);
        y = stepTowards(y, targetY, maxStep);
    }

    private float stepTowards(float current, float target, float maxStep) {
        return current + MathUtils.clamp(target - current, -maxStep, maxStep);
    }

    // Draw the goose (Dispersed geese are invisible)
    public void draw(SpriteBatch batch) {

        if (state == GooseState.DISPERSED) {
            return;
        }

        batch.draw(
            texture,
            x - radius,
            y - radius,
            radius * 2,
            radius * 2
        );
    }

    // Circle-circle collision with the player
    public boolean collidesWith(float otherX, float otherY, float otherRadius) {

        if (state != GooseState.NORMAL) {
            return false;
        }

        float dx = otherX - x;
        float dy = otherY - y;
        float combinedRadius = radius + otherRadius;

        return dx * dx + dy * dy <= combinedRadius * combinedRadius;
    }

    // put the goose back to how it started
    public void reset() {
        x = startX;
        y = startY;
        xDirection = 1;
        yDirection = 1;
        angle = 0f;
        state = GooseState.NORMAL;
    }

    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public float getRadius() { return radius; }
    public GooseState getState() { return state; }
    public MovementType getMovementType() { return movementType; }

    // Disperse the goose
    public void disperse() {
        state = GooseState.DISPERSED;
    }

    // lure this goose towards bread
    public void lureTowards(float targetX, float targetY, float maxStep) {
        if (state == GooseState.DISPERSED) {
            return;
        }
        state = GooseState.FLEEING;
        moveTowards(targetX, targetY, maxStep);
    }

    // called when bread lure wears off
    public void stopFleeing() {
        if (state == GooseState.FLEEING) {
            state = GooseState.NORMAL;
        }
    }

    // Set the direction of a newly spawned goose
    public void setDirection(int xDirection, int yDirection) {
        this.xDirection = xDirection;
        this.yDirection = yDirection;
    }

    // Give circle geese a different starting angle
    public void setCircleAngle(float angle) {
        this.angle = angle;

        if (movementType == MovementType.CIRCLE) {
            x = orbitX(angle);
            y = orbitY(angle);
        }
    }
}
