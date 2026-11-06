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
import java.util.ArrayList;

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

    // All geese in the game
    private final ArrayList<Goose> geese = new ArrayList<>();

    // Where each goose stands around the bread (repeats after every 4 geese)
    private static final float[] BREAD_OFFSET_X = { -30f, 30f, 0f, 0f };
    private static final float[] BREAD_OFFSET_Y = { 0f, 0f, 30f, -30f };

    // Text layout
    private GlyphLayout layout;

    // Player position and movement
    private float playerX;
    private float playerY;
    private float playerSpeed = 300f;
    private float playerRadius = 15f;

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

        // Start the player in the center
        playerX = Gdx.graphics.getWidth() / 2f;
        playerY = Gdx.graphics.getHeight() / 2f;

        // Create the four geese (same values as before)
        geese.add(new Goose(normalGoose, MovementType.HORIZONTAL,
                100f, 400f, 200f, 22f));
        geese.add(new Goose(greyGoose, MovementType.VERTICAL,
            400f, 100f, 150f, 22f));
        geese.add(new Goose(flyingGoose, MovementType.DIAGONAL,
            200f, 200f, 180f, 22f));
        geese.add(new Goose(redBreastedGoose,
            Gdx.graphics.getWidth() * 0.7f,
            Gdx.graphics.getHeight() * 0.5f,
            80f,
            1.5f,
            22f));

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

        // Draw every goose
        for (Goose goose : geese) {
            goose.draw(batch);
        }

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

        // Move every goose
        for (int i = 0; i < geese.size(); i++) {

            Goose goose = geese.get(i);

            if (breadActive) {
                // Walk towards the bread, each goose to its own spot
                goose.moveTowards(
                    breadX + BREAD_OFFSET_X[i % 4],
                    breadY + BREAD_OFFSET_Y[i % 4],
                    BREAD_SPEED * delta
                );

            }else{
                // Normal Movement pattern
                goose.update(delta);
            }
        }

    }

    // Handles collisions between the player and all four geese
    private void checkGooseCollisions() {
        // Handles collisions between the player and every goose
        for (Goose goose : geese) {
            if (goose.collidesWith(playerX, playerY, playerRadius)) {
                state = GameState.LOST;
                Gdx.app.log("Game", "Hit " + goose.getMovementType() + " goose");
                return;
            }
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

       // Reset every goose
        for (Goose goose : geese) {
            goose.reset();
        }

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
