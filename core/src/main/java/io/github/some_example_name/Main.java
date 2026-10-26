package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

public class Main extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private Matrix4 cameraMatrix;
    private boolean collided = false;

    // Player position and movement
    private float playerX;
    private float playerY;
    private float playerSpeed = 300f;
    private float playerRadius = 15f;

    // Horizontal Goose
    private float horizontalGooseX = 100f;
    private float horizontalGooseY = 400f;
    private float horizontalGooseSpeed = 200f;
    private float horizontalGooseRadius = 22f;
    private int horizontalGooseDirection = 1;

    // Vertical Goose
    private float verticalGooseX = 400f;
    private float verticalGooseY = 100f;
    private float verticalGooseSpeed = 150f;
    private float verticalGooseRadius = 22f;
    private int verticalGooseDirection = 1;

    // Diagonal Goose
    private float diagonalGooseX = 200f;
    private float diagonalGooseY = 200f;
    private float diagonalGooseSpeed = 180f;
    private float diagonalGooseRadius = 22f;
    private int diagonalGooseXDirection = 1;
    private int diagonalGooseYDirection = 1;

    // Circle Goose
    private float circleGooseX;
    private float circleGooseY;
    private float circleGooseRadius = 22f;
    private float circleCentreX;
    private float circleCentreY;
    private float circleOrbitRadius = 80f;
    private float circleAngle = 0f;
    private float circleAngularSpeed = 1.5f;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        cameraMatrix = new Matrix4();

        // Start the player in the centre
        playerX = Gdx.graphics.getWidth() / 2f;
        playerY = Gdx.graphics.getHeight() / 2f;

        // Centre of circle goose's path
        circleCentreX = Gdx.graphics.getWidth() * 0.7f;
        circleCentreY = Gdx.graphics.getHeight() * 0.5f;

        circleGooseX = circleCentreX + circleOrbitRadius;
        circleGooseY = circleCentreY;

        updateProjection();
    }

    private void updateProjection() {
        Gdx.gl.glViewport(
            0, 0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        cameraMatrix.setToOrtho2D(
            0, 0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        shapeRenderer.setProjectionMatrix(cameraMatrix);
    }

    @Override
    public void resize(int width, int height) {
        updateProjection();
    }

    @Override
    public void render() {
        // Makes the screen black
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Calculate movement based on time
        float delta = Gdx.graphics.getDeltaTime();
        float movement = playerSpeed * delta;

        // Only move objects if there has been no collision
        if (!collided) {

            // WASD movement
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                playerY += movement;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                playerY -= movement;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                playerX -= movement;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                playerX += movement;
            }

            // Move Horizontal Goose
            horizontalGooseX += horizontalGooseSpeed
                * horizontalGooseDirection * delta;

            // Reverse Horizontal Goose at screen edges
            if (horizontalGooseX + horizontalGooseRadius >= Gdx.graphics.getWidth()) {
                horizontalGooseDirection = -1;
            }
            if (horizontalGooseX - horizontalGooseRadius <= 0) {
                horizontalGooseDirection = 1;
            }

            // Move Vertical Goose
            verticalGooseY += verticalGooseSpeed
                * verticalGooseDirection * delta;

            // Reverse Vertical Goose at screen edges
            if (verticalGooseY + verticalGooseRadius >= Gdx.graphics.getHeight()) {
                verticalGooseDirection = -1;
            }
            if (verticalGooseY - verticalGooseRadius <= 0) {
                verticalGooseDirection = 1;
            }

            // Move Diagonal Goose
            diagonalGooseX += diagonalGooseSpeed
                * diagonalGooseXDirection * delta;

            diagonalGooseY += diagonalGooseSpeed
                * diagonalGooseYDirection * delta;

            if (diagonalGooseX + diagonalGooseRadius >= Gdx.graphics.getWidth()) {
                diagonalGooseXDirection = -1;
            }

            if (diagonalGooseX - diagonalGooseRadius <= 0) {
                diagonalGooseXDirection = 1;
            }

            // Reverse Diagonal Goose at vertical edges
            if (diagonalGooseY + diagonalGooseRadius >= Gdx.graphics.getHeight()) {
                diagonalGooseYDirection = -1;
            }
            if (diagonalGooseY - diagonalGooseRadius <= 0) {
                diagonalGooseYDirection = 1;
            }

            // Move Circle Goose around a centre
            circleAngle += circleAngularSpeed * delta;

            circleGooseX = circleCentreX
                + circleOrbitRadius * (float) Math.cos(circleAngle);

            circleGooseY = circleCentreY
                + circleOrbitRadius * (float) Math.sin(circleAngle);

            // Check collision with Horizontal Goose
            float dx = playerX - horizontalGooseX;
            float dy = playerY - horizontalGooseY;
            float combinedRadius = playerRadius + horizontalGooseRadius;

            if (dx * dx + dy * dy <= combinedRadius * combinedRadius) {
                collided = true;
                Gdx.app.log("Game", "Hit Horizontal Goose");
            }

            // Check collision with Vertical Goose
            dx = playerX - verticalGooseX;
            dy = playerY - verticalGooseY;
            combinedRadius = playerRadius + verticalGooseRadius;

            if (dx * dx + dy * dy <= combinedRadius * combinedRadius) {
                collided = true;
                Gdx.app.log("Game", "Hit Vertical Goose");
            }

            // Check collision with Diagonal Goose
            dx = playerX - diagonalGooseX;
            dy = playerY - diagonalGooseY;
            combinedRadius = playerRadius + diagonalGooseRadius;

            if (dx * dx + dy * dy <= combinedRadius * combinedRadius) {
                collided = true;
                Gdx.app.log("Game", "Hit Diagonal Goose");
            }

            // Check collision with Circle Goose
            dx = playerX - circleGooseX;
            dy = playerY - circleGooseY;
            combinedRadius = playerRadius + circleGooseRadius;

            if (dx * dx + dy * dy <= combinedRadius * combinedRadius) {
                collided = true;
                Gdx.app.log("Game", "Hit Circle Goose");
            }

            // Keep player inside window
            playerX = Math.max(
                playerRadius,
                Math.min(playerX, Gdx.graphics.getWidth() - playerRadius)
            );

            playerY = Math.max(
                playerRadius,
                Math.min(playerY, Gdx.graphics.getHeight() - playerRadius)
            );
        }

        // Draw player and geese
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Player turns blue after collision
        if (collided) {
            shapeRenderer.setColor(0, 0, 1, 1);
        } else {
            shapeRenderer.setColor(1, 1, 1, 1);
        }

        shapeRenderer.circle(playerX, playerY, playerRadius);

        // Draw Horizontal Goose in red
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.circle(
            horizontalGooseX,
            horizontalGooseY,
            horizontalGooseRadius
        );

        // Draw Vertical Goose in orange
        shapeRenderer.setColor(1, 0.5f, 0, 1);
        shapeRenderer.circle(
            verticalGooseX,
            verticalGooseY,
            verticalGooseRadius
        );

        // Draw Diagonal Goose in green
        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.circle(
            diagonalGooseX,
            diagonalGooseY,
            diagonalGooseRadius
        );

        // Draw Circle Goose in yellow
        shapeRenderer.setColor(1, 1, 0, 1);
        shapeRenderer.circle(
            circleGooseX,
            circleGooseY,
            circleGooseRadius
        );

        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
