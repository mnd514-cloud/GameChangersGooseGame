package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


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
                bounceX(width);
                break;

            case VERTICAL:
                y += speed * yDirection * delta;
                bounceY(height);
                break;

            case DIAGONAL:
                x += speed * xDirection * delta;
                y += speed * yDirection * delta;
                bounceX(width);
                bounceY(height);
                break;

            case CIRCLE:
                angle += speed * delta;
                x = centerX + orbitRadius * (float) Math.cos(angle);
                y = centerY + orbitRadius * (float) Math.sin(angle);
                break;
        }
    }
    // Reverse direction ate the left/right edges
    private void bounceX(float width) {
        if (x + radius >= width) {
            xDirection = -1;
        }
        if (x - radius <= 0) {
            xDirection = 1;
        }
    }

    // Reverse direction at the top/bottom edges
    private void bounceY(float height) {
        if (y + radius >= height) {
            yDirection = -1;
        }
        if (y - radius <= 0) {
            yDirection = 1;
        }
    }

    // Move towards a point without overshooting (used for bread)
    public void moveTowards(float targetX, float targetY, float maxStep) {
        x = stepTowards(x, targetX, maxStep);
        y = stepTowards(y, targetY, maxStep);
    }

    private float stepTowards(float current, float target, float maxStep) {
        if (Math.abs(target - current) <= maxStep) {
            return target;
        }
        if (current < target) {
            return current + maxStep;
        }else{
            return current - maxStep;
        }
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

        if (state == GooseState.DISPERSED) {
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

    // Set the direction of a newly spawned goose
    public void setDirection(int xDirection, int yDirection) {
        this.xDirection = xDirection;
        this.yDirection = yDirection;
    }

    // Give circle geese a different starting angle
    public void setCircleAngle(float angle) {
        this.angle = angle;

        if (movementType == MovementType.CIRCLE) {
            x = centerX + orbitRadius * (float) Math.cos(angle);
            y = centerY + orbitRadius * (float) Math.sin(angle);
        }
    }
}
