package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

public class Main extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private Matrix4 cameraMatrix;

    private static final float GAME_DURATION = 300f;
    private float timeRemaining = GAME_DURATION;

    // Game state
    private GameState state = GameState.PAUSED;
    private boolean started = false;

    // Player inventory
    private Inventory inventory;

    private BitmapFont font;
    private SpriteBatch batch;

    // Game assets
    private Texture normalGoose;
    private Texture flyingGoose;
    private Texture greyGoose;
    private Texture redBreastedGoose;

    // Text layout
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

    // Geese attracted to bread
    private boolean breadActive = false;
    private float breadX;
    private float breadY;
    private float breadTimer = 0f;

    private static final float BREAD_DURATION = 10f;
    private static final float BREAD_SPEED = 250f;

    // Hidden event
    private boolean hiddenEventTriggered = false;
    private static final float HIDDEN_ZONE_SIZE = 100f;

    // Goose speed boost (hidden event)
    private boolean speedBoostActive = false;
    private float speedBoostTimer = 0f;
    private static final float SPEED_BOOST_DURATION = 10f;
    private static final float SPEED_BOOST_MULTIPLIER = 1.75f;

    @Override
    public void create() {

        inventory = new Inventory();

        normalGoose = new Texture(
            Gdx.files.internal("normal_goose.png")
        );
        flyingGoose = new Texture(
            Gdx.files.internal("flying_goose.png")
        );
        greyGoose = new Texture(
            Gdx.files.internal("grey_goose.png")
        );
        redBreastedGoose = new Texture(
            Gdx.files.internal("red_breasted_goose.png")
        );

        shapeRenderer = new ShapeRenderer();
        cameraMatrix = new Matrix4();

        font = new BitmapFont();
        batch = new SpriteBatch();

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

        // Avoid updating the projection when minimised
        if (width == 0 || height == 0) {
            return;
        }

        updateProjection();
    }

    @Override
    public void render() {

        // Skip the frame if the window is minimised
        if (Gdx.graphics.getWidth() == 0
            || Gdx.graphics.getHeight() == 0) {
            return;
        }

        // Makes the screen black
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Calculate movement based on time
        float delta = Gdx.graphics.getDeltaTime();

        // Countdown timer only while the game is running
        if (state == GameState.RUNNING) {
            timeRemaining = Math.max(
                0f,
                timeRemaining - delta
            );
        }

        float movement = playerSpeed * delta;

        // Select bread with 1
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            inventory.selectSlot(0);
        }

        // Select shield with 2
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            inventory.selectSlot(1);
        }

        // E uses bread if bread is selected
        if (state == GameState.RUNNING
            && Gdx.input.isKeyJustPressed(Input.Keys.E)
            && inventory.getSelectedSlot() == 0) {

            String itemUsed = inventory.useSelectedItem();

            if (itemUsed != null) {

                // Record where the bread was thrown
                breadX = playerX;
                breadY = playerY;

                // Activate the bread distraction
                breadActive = true;
                breadTimer = BREAD_DURATION;

                Gdx.app.log("Inventory", "Threw bread!");
            }
        }

        // SPACE starts the game from the start screen
        if (!started
            && state == GameState.PAUSED
            && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {

            started = true;
            state = GameState.RUNNING;
        }

        // ESC pauses and resumes once the game has started
        if (started
            && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

            togglePause();
        }

        // It displays instructions from the start/pause screens
        if (state == GameState.PAUSED
            && Gdx.input.isKeyJustPressed(Input.Keys.I)) {

            state = GameState.INSTRUCTIONS;

        } else if (state == GameState.INSTRUCTIONS
            && (Gdx.input.isKeyJustPressed(Input.Keys.I)
            || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))) {

            state = GameState.PAUSED;
        }

        // R restarts the game after a win or loss
        if ((state == GameState.WON || state == GameState.LOST)
            && Gdx.input.isKeyJustPressed(Input.Keys.R)) {

            resetGame();
            state = GameState.RUNNING;
        }

        // Only move objects while the game is running
        if (state == GameState.RUNNING) {

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

            // Move all four geese
            updateGooseMovement(delta);

            // Check collisions with all four geese
            checkGooseCollisions();

            // Check game state again to prevent a win
            // from overriding a loss in the same frame
            if (state == GameState.RUNNING
                && timeRemaining <= 0) {

                state = GameState.WON;
                Gdx.app.log("Game", "Survived - You Win");
            }

            // Keep player inside the window
            playerX = Math.max(
                playerRadius,
                Math.min(
                    playerX,
                    Gdx.graphics.getWidth() - playerRadius
                )
            );

            playerY = Math.max(
                playerRadius,
                Math.min(
                    playerY,
                    Gdx.graphics.getHeight() - playerRadius
                )
            );
            float screenW = Gdx.graphics.getWidth();
            float screenH = Gdx.graphics.getHeight();

            if (!hiddenEventTriggered
            && playerX > screenW - HIDDEN_ZONE_SIZE
            && playerY > screenH - HIDDEN_ZONE_SIZE) {
                hiddenEventTriggered = true;
                speedBoostActive = true;
                speedBoostTimer = SPEED_BOOST_DURATION;
                Gdx.app.log("HiddenEvent", "Top-right event triggered");
            }

        }
        // Draw the hidden event
        if (hiddenEventTriggered) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 1, 1);
            shapeRenderer.rect(
                Gdx.graphics.getWidth() - HIDDEN_ZONE_SIZE,
                Gdx.graphics.getHeight() - HIDDEN_ZONE_SIZE,
                HIDDEN_ZONE_SIZE,
                HIDDEN_ZONE_SIZE
            );
            shapeRenderer.end();
        }

        // Draw player
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Player turns blue after losing
        if (state == GameState.LOST) {
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

        // Draw timer, rounding up partial seconds
        int displayedTime = (int) Math.ceil(timeRemaining);
        int minutes = displayedTime / 60;
        int seconds = displayedTime % 60;

        String timerText = String.format(
            "%02d:%02d",
            minutes,
            seconds
        );

        font.draw(
            batch,
            timerText,
            20,
            Gdx.graphics.getHeight() - 20
        );

        // Show a warning while the speed boost is active
        if (speedBoostActive) {
            font.setColor(1, 0.3f, 0.3f, 1);
            font.draw(
                batch,
                "GEESE ENRAGED! " + (int) Math.ceil(speedBoostTimer) + "s",
                20,
                Gdx.graphics.getHeight() - 60
            );
            font.setColor(1, 1, 1, 1);
        }

        batch.end();

        // Draw the start or pause screen
        if (state == GameState.PAUSED) {

            if (started) {
                drawOverlay(
                    "PAUSED",
                    "ESC to resume | I for instructions"
                );
            } else {
                drawOverlay(
                    "GOOSE GAME",
                    "SPACE to start | I for instructions"
                );
            }
        }

        // Draw instructions screen
        if (state == GameState.INSTRUCTIONS) {
            drawInstructions();
        }

        // Draw win or loss screen
        if (state == GameState.LOST) {
            drawOverlay("YOU LOST", "Press R to restart");

        } else if (state == GameState.WON) {
            drawOverlay("YOU WIN!", "Press R to restart");
        }
        // Draw the inventory hotbar
        if (started) {
            drawHotbar();
        }

    }

    // Handles the movement of all four geese
    private void updateGooseMovement(float realDelta) {

        // Update the bread distraction timer
        if (breadActive) {
            breadTimer -= realDelta;

            if (breadTimer <= 0) {
                breadActive = false;
            }
        }
        // Update the speed boost timer (real time)
        if (speedBoostActive) {
            speedBoostTimer -= realDelta;

            if (speedBoostTimer <= 0) {
                speedBoostActive = false;
            }
        }

        // Geese move faster while the boost is active
        float delta = speedBoostActive
            ? realDelta * SPEED_BOOST_MULTIPLIER
            : realDelta;

        // BREAD MOVEMENT OVERRIDE
        if (breadActive) {

            // Horizontal Goose
            horizontalGooseX = moveTowards(
                horizontalGooseX, breadX - 30f, BREAD_SPEED * delta
            );
            horizontalGooseY = moveTowards(
                horizontalGooseY, breadY, BREAD_SPEED * delta
            );

            // Vertical Goose
            verticalGooseX = moveTowards(
                verticalGooseX, breadX + 30f, BREAD_SPEED * delta
            );
            verticalGooseY = moveTowards(
                verticalGooseY, breadY, BREAD_SPEED * delta
            );

            // Diagonal Goose
            diagonalGooseX = moveTowards(
                diagonalGooseX, breadX, BREAD_SPEED * delta
            );
            diagonalGooseY = moveTowards(
                diagonalGooseY, breadY + 30f, BREAD_SPEED * delta
            );

            // Circle Goose
            circleGooseX = moveTowards(
                circleGooseX, breadX, BREAD_SPEED * delta
            );
            circleGooseY = moveTowards(
                circleGooseY, breadY - 30f, BREAD_SPEED * delta
            );

        } else {

            // NORMAL MOVEMENT

            // Horizontal Goose
            horizontalGooseX += horizontalGooseSpeed
                * horizontalGooseDirection * delta;

            // Reverse Horizontal Goose at screen edges
            if (horizontalGooseX + horizontalGooseRadius
                >= Gdx.graphics.getWidth()) {

                horizontalGooseDirection = -1;
            }

            if (horizontalGooseX - horizontalGooseRadius <= 0) {
                horizontalGooseDirection = 1;
            }

            // Vertical Goose
            verticalGooseY += verticalGooseSpeed
                * verticalGooseDirection * delta;

            // Reverse Vertical Goose at screen edges
            if (verticalGooseY + verticalGooseRadius
                >= Gdx.graphics.getHeight()) {

                verticalGooseDirection = -1;
            }

            if (verticalGooseY - verticalGooseRadius <= 0) {
                verticalGooseDirection = 1;
            }

            // Diagonal Goose
            diagonalGooseX += diagonalGooseSpeed
                * diagonalGooseXDirection * delta;

            diagonalGooseY += diagonalGooseSpeed
                * diagonalGooseYDirection * delta;

            // Reverse Diagonal Goose at horizontal edges
            if (diagonalGooseX + diagonalGooseRadius
                >= Gdx.graphics.getWidth()) {

                diagonalGooseXDirection = -1;
            }

            if (diagonalGooseX - diagonalGooseRadius <= 0) {
                diagonalGooseXDirection = 1;
            }

            // Reverse Diagonal Goose at vertical edges
            if (diagonalGooseY + diagonalGooseRadius
                >= Gdx.graphics.getHeight()) {

                diagonalGooseYDirection = -1;
            }

            if (diagonalGooseY - diagonalGooseRadius <= 0) {
                diagonalGooseYDirection = 1;
            }

            // Circle Goose
            circleAngle += circleAngularSpeed * delta;

            circleGooseX = circleCentreX
                + circleOrbitRadius * (float) Math.cos(circleAngle);

            circleGooseY = circleCentreY
                + circleOrbitRadius * (float) Math.sin(circleAngle);
        }
    }

    // Handles collisions between the player and all four geese
    private void checkGooseCollisions() {

        // Horizontal Goose
        float dx = playerX - horizontalGooseX;
        float dy = playerY - horizontalGooseY;
        float combinedRadius = playerRadius + horizontalGooseRadius;

        if (dx * dx + dy * dy <= combinedRadius * combinedRadius) {

            state = GameState.LOST;
            Gdx.app.log("Game", "Hit Horizontal Goose");
        }

        // Vertical Goose
        dx = playerX - verticalGooseX;
        dy = playerY - verticalGooseY;
        combinedRadius = playerRadius + verticalGooseRadius;

        if (dx * dx + dy * dy <= combinedRadius * combinedRadius) {

            state = GameState.LOST;
            Gdx.app.log("Game", "Hit Vertical Goose");
        }

        // Diagonal Goose
        dx = playerX - diagonalGooseX;
        dy = playerY - diagonalGooseY;
        combinedRadius = playerRadius + diagonalGooseRadius;

        if (dx * dx + dy * dy <= combinedRadius * combinedRadius) {

            state = GameState.LOST;
            Gdx.app.log("Game", "Hit Diagonal Goose");
        }

        // Circle Goose
        dx = playerX - circleGooseX;
        dy = playerY - circleGooseY;
        combinedRadius = playerRadius + circleGooseRadius;

        if (dx * dx + dy * dy <= combinedRadius * combinedRadius) {

            state = GameState.LOST;
            Gdx.app.log("Game", "Hit Circle Goose");
        }
    }

    // Moves a goose towards a target without overshooting
    private float moveTowards(float current, float target, float maxStep) {

        if (Math.abs(target - current) <= maxStep) {
            return target;
        }

        if (current < target) {
            return current + maxStep;
        } else {
            return current - maxStep;
        }
    }

    // Draw the inventory hotbar
    private void drawHotbar() {

        float slotSize = 55f;
        float gap = 6f;

        float totalWidth = (slotSize * 9) + (gap * 8);

        float startX = (Gdx.graphics.getWidth() - totalWidth) / 2f;
        float startY = 15f;

        // Draw the slot backgrounds
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < 9; i++) {

            float x = startX + i * (slotSize + gap);

            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);

            shapeRenderer.rect(
                x,
                startY,
                slotSize,
                slotSize
            );
        }

        shapeRenderer.end();

        // Highlight the selected slot
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(1f, 0.85f, 0f, 1f);

        float selectedX = startX
            + inventory.getSelectedSlot() * (slotSize + gap);

        shapeRenderer.rect(
            selectedX,
            startY,
            slotSize,
            slotSize
        );

        shapeRenderer.end();

        // Item names and amounts
        batch.begin();

        font.getData().setScale(1f);

        for (int i = 0; i < 9; i++) {

            float x = startX + i * (slotSize + gap);

            // Slot number
            font.draw(
                batch,
                String.valueOf(i + 1),
                x + 5,
                startY + slotSize - 5
            );

            // Item abbreviation
            String item = inventory.getItem(i);

            if (item.equals("Bread")) {
                font.draw(batch, "B", x + 25, startY + 32);

            } else if (item.equals("Shield")) {
                font.draw(batch, "S", x + 25, startY + 32);
            }

            // Item quantity
            if (inventory.getQuantity(i) > 0) {
                font.draw(
                    batch,
                    "x" + inventory.getQuantity(i),
                    x + 20,
                    startY + 10
                );
            }
        }

        font.getData().setScale(2f);

        batch.end();
    }

    // Switch between RUNNING and PAUSED
    private void togglePause() {

        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;

        } else if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
        }
    }

    // Reset the timer, player and geese
    private void resetGame() {

        timeRemaining = GAME_DURATION;

        playerX = Gdx.graphics.getWidth() / 2f;
        playerY = Gdx.graphics.getHeight() / 2f;

        horizontalGooseX = 100f;
        horizontalGooseY = 400f;
        horizontalGooseDirection = 1;

        verticalGooseX = 400f;
        verticalGooseY = 100f;
        verticalGooseDirection = 1;

        diagonalGooseX = 200f;
        diagonalGooseY = 200f;
        diagonalGooseXDirection = 1;
        diagonalGooseYDirection = 1;

        circleAngle = 0f;
        circleGooseX = circleCentreX + circleOrbitRadius;
        circleGooseY = circleCentreY;

        // Reset bread
        breadActive = false;
        breadTimer = 0f;

        // Reset HiddenEvent
        hiddenEventTriggered = false;
        speedBoostActive = false;
        speedBoostTimer = 0f;

    }

    // Darken the screen and show a title with a hint
    private void drawOverlay(String title, String hint) {

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        dimScreen(0.6f);

        batch.begin();

        font.setColor(1, 1, 1, 1);

        layout.setText(font, title);

        font.draw(
            batch,
            layout,
            (width - layout.width) / 2f,
            height / 2f + 40f
        );

        layout.setText(font, hint);

        font.draw(
            batch,
            layout,
            (width - layout.width) / 2f,
            height / 2f - 10f
        );

        batch.end();
    }

    // Darken the screen and show game instructions
    private void drawInstructions() {

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        String[] lines = {
            "HOW TO PLAY",
            "",
            "Dodge the geese for 5 minutes to win.",
            "Touch a goose and you lose!",
            "",
            "CONTROLS",
            "W A S D - move",
            "ESC - pause / resume",
            "R - restart after winning or losing",
            "",
            "Press I or ESC to go back"
        };

        dimScreen(0.85f);

        batch.begin();

        font.getData().setScale(1.5f);
        font.setColor(1, 1, 1, 1);

        float y = height - 50f;

        for (String line : lines) {

            layout.setText(font, line);

            font.draw(
                batch,
                layout,
                (width - layout.width) / 2f,
                y
            );

            y -= 32f;
        }

        font.getData().setScale(2f);

        batch.end();
    }

    // Darken the screen behind an overlay
    private void dimScreen(float alpha) {

        Gdx.gl.glEnable(GL20.GL_BLEND);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, alpha);

        shapeRenderer.rect(
            0,
            0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
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
