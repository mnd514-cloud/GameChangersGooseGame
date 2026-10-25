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

    // Player position and movement
    private float playerX;
    private float playerY;
    private float playerSpeed = 300f;
    private float playerRadius = 10f;

    // Enemy position and movement
    private float enemyX = 100f;
    private float enemyY = 400f;
    private float enemySpeed = 200f;
    private float enemyRadius = 15f;
    private int enemyDirection = 1;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        cameraMatrix = new Matrix4();

        // Start the player in the centre
        playerX = Gdx.graphics.getWidth() / 2f;
        playerY = Gdx.graphics.getHeight() / 2f;

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

        // Move enemy horizontally
        enemyX += enemySpeed * enemyDirection * delta;

        //Reverse direction at edge of screen
            if (enemyX + enemyRadius >= Gdx.graphics.getWidth()) {
                enemyDirection = -1;
            } else if (enemyX - enemyRadius <= 0) {
                enemyDirection = 1;

        //Check collison between player and enemy
        float dx = playerX-enemyX;
        float dy = playerY - enemyY;

        if (dx * dx * dy * dy <=
            (playerRadius + enemyRadius)*
                (playerRadius + enemyRadius)) {
            collided = true;

        }

        }

        // Keep player inside window
        playerX = Math.max(playerRadius,
            Math.min(playerX, Gdx.graphics.getWidth() - playerRadius));

        playerY = Math.max(playerRadius,
            Math.min(playerY, Gdx.graphics.getHeight() - playerRadius));

        //Draw  player
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.circle(playerX, playerY, playerRadius);

        shapeRenderer.setColor(0, 0, 1, 1);
        shapeRenderer.circle(enemyX, enemyY, enemyRadius);

        shapeRenderer.end();

    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
