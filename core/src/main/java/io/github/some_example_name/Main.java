package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Main extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private Matrix4 cameraMatrix;
    private boolean collided = false;
    private float timeRemaining = 300f; // game timer
    private BitmapFont font;
    private SpriteBatch batch;

    //putting in the game assets
    private Texture normalGoose;
    private Texture flyingGoose;
    private Texture greyGoose;
    private Texture redBreastedGoose;

    // Pause state: the game starts paused until the player presses Start
    private GameState state = GameState.PAUSED;
    private boolean started = false;

    // Text for the start and pause screens
    private GlyphLayout layout;

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
        normalGoose = new Texture(Gdx.files.internal("normal_goose.png"));
        flyingGoose = new Texture(Gdx.files.internal("flying_goose.png"));
        greyGoose = new Texture(Gdx.files.internal("grey_goose.png"));
        redBreastedGoose = new Texture(Gdx.files.internal("red_breasted_goose.png"));

        shapeRenderer = new ShapeRenderer();
        cameraMatrix = new Matrix4();
        font = new BitmapFont();
        batch = new SpriteBatch();

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);
        layout = new GlyphLayout();

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
        batch.setProjectionMatrix(cameraMatrix);
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

        // countdown timer (only ticks when game is actually running)
        if (state == GameState.RUNNING && !collided) {
            timeRemaining = Math.max(0f, timeRemaining - delta); // ensuring timer never goes below 0
        }

        float movement = playerSpeed * delta;

        // SPACE starts the game from the start screen
        if (!started && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            started = true;
            state = GameState.RUNNING;
        }

        // ESC pauses and resumes once the game has started
        if (started && !collided && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }

        // Only move objects while running and there has been no collision
        if (state == GameState.RUNNING && !collided) {

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

        // Draw player
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

// Player turns blue after collision
        if (collided) {
            shapeRenderer.setColor(0, 0, 1, 1);
        } else {
            shapeRenderer.setColor(1, 1, 1, 1);
        }

        shapeRenderer.circle(playerX, playerY, playerRadius);

        shapeRenderer.end();

// Draw geese and timer
        batch.begin();

// Horizontal Goose - Normal Goose
        batch.draw(
            normalGoose,
            horizontalGooseX - horizontalGooseRadius,
            horizontalGooseY - horizontalGooseRadius,
            horizontalGooseRadius * 2,
            horizontalGooseRadius * 2
        );

// Vertical Goose - Grey Goose
        batch.draw(
            greyGoose,
            verticalGooseX - verticalGooseRadius,
            verticalGooseY - verticalGooseRadius,
            verticalGooseRadius * 2,
            verticalGooseRadius * 2
        );

// Diagonal Goose - Flying Goose
        batch.draw(
            flyingGoose,
            diagonalGooseX - diagonalGooseRadius,
            diagonalGooseY - diagonalGooseRadius,
            diagonalGooseRadius * 2,
            diagonalGooseRadius * 2
        );

// Circle Goose - Red-Breasted Goose
        batch.draw(
            redBreastedGoose,
            circleGooseX - circleGooseRadius,
            circleGooseY - circleGooseRadius,
            circleGooseRadius * 2,
            circleGooseRadius * 2
        );

// Draw timer
        int minutes = (int) timeRemaining / 60;
        int seconds = (int) timeRemaining % 60;

        String timerText = String.format("%02d:%02d", minutes, seconds);

        font.draw(batch, timerText, 20, Gdx.graphics.getHeight() - 20);

        batch.end();

        // Draw the start or pause screen over the game
        if (state == GameState.PAUSED) {
            if (started) {
                drawOverlay("PAUSED", "Press ESC to resume");
            } else {
                drawOverlay("GOOSE GAME", "Press SPACE to start");
            }
        }
    }

    // Switch between RUNNING and PAUSED
    private void togglePause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
        } else if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
        }
    }

    // Darken the screen and show a title with a hint underneath
    private void drawOverlay(String title, String hint) {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.6f);
        shapeRenderer.rect(0, 0, width, height);
        shapeRenderer.end(); //



        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        font.setColor(1, 1, 1, 1);

        layout.setText(font, title);
        font.draw(batch, layout, (width - layout.width) / 2f, height / 2f + 40f);

        layout.setText(font, hint);
        font.draw(batch, layout, (width - layout.width) / 2f, height / 2f - 10f);

        batch.end();
    }

    @Override
    public void pause() {
        // Pause automatically if the window loses focus or is minimised
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        normalGoose.dispose();
        flyingGoose.dispose();
        greyGoose.dispose();
        redBreastedGoose.dispose();
    }
}
