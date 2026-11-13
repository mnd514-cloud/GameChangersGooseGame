package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Random;

public class Main extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private Matrix4 cameraMatrix;

    private static final float GAME_DURATION = 300f;
    private float timeRemaining = GAME_DURATION;

    private static final float MAX_FRAME_TIME = 0.05f;

    // Game state
    private GameState state = GameState.PAUSED;
    private boolean started = false;

    // Mouse pointer visibility
    private boolean cursorHidden = false;

    // Game statistics
    private int itemsUsed = 0;
    private int eventsInteracted = 0;

    // Player inventory
    private Inventory inventory;

    private BitmapFont font;
    private SpriteBatch batch;

    // Game assets
    private Texture normalGoose;
    private Texture flyingGoose;
    private Texture greyGoose;
    private Texture redBreastedGoose;

    // Item icons
    private Texture breadTexture;
    private Texture shieldTexture;

    // Background map
    private Texture mapTexture;

    // Player sprite sheet and animations
    private Texture playerTexture;
    private TextureRegion[][] playerFrames;

    private Animation<TextureRegion> playerDownAnimation;
    private Animation<TextureRegion> playerLeftAnimation;
    private Animation<TextureRegion> playerUpAnimation;
    private Animation<TextureRegion> playerRightAnimation;

    private float playerAnimationTimer = 0f;

    private TextureRegion currentPlayerFrame;

    // Player movement animation state
    private boolean playerMoving = false;

    // 0 = down, 1 = left, 2 = up, 3 = right
    private int playerFacingDirection = 0;

    // Tree collision areas
    private final ArrayList<Rectangle> treeObstacles = new ArrayList<>();

    // All geese in the game
    private final ArrayList<Goose> geese = new ArrayList<>();

    // Goose spawning system
    private final Random random = new Random();
    private float spawnTimer = 0f;

    private static final int INITIAL_GOOSE_COUNT = 4;

    private static final float BASE_SPAWN_RATE = 0.5f;

    // Goose spawning tuning
    private static final float GOOSE_RADIUS = 22f;
    private static final float MIN_GOOSE_SPEED = 150f;
    private static final float GOOSE_SPEED_VARIATION = 70f;
    private static final float MIN_CIRCLE_RADIUS = 50f;
    private static final float CIRCLE_RADIUS_STEP = 15f;
    private static final float CIRCLE_CENTRE_MIN = 0.3f;
    private static final float CIRCLE_CENTRE_RANGE = 0.4f;
    private static final float MIN_ANGULAR_SPEED = 0.8f;
    private static final float ANGULAR_SPEED_VARIATION = 1.2f;
    private static final float MAX_CIRCLE_RADIUS = 300f;

    private float nextCircleRadius = MIN_CIRCLE_RADIUS;

    // Text layout
    private GlyphLayout layout;

    // Player position and movement
    private float playerX;
    private float playerY;
    private float playerSpeed = 300f;
    private float playerRadius = 15f;

    // Shield collision grace period
    private static final float SHIELD_GRACE_DURATION = 0.75f;
    private float shieldGraceTimer = 0f;

    // Geese attracted to bread
    private boolean breadActive = false;
    private float breadX;
    private float breadY;
    private float breadTimer = 0f;

    // Bread tuning
    private static final float BREAD_DURATION = 5f;
    private static final float BREAD_SPEED = 250f;
    private static final float BREAD_RING_BASE = 15f;
    private static final float BREAD_RING_PER_GOOSE = 8f;
    private static final float BREAD_RING_MAX = 200f;

    // Bread throw tuning
    private static final float BREAD_FLIGHT_TIME = 0.6f;
    private static final float BREAD_ARC_HEIGHT = 120f;
    private static final float BREAD_THROW_MIN = 150f;
    private static final float BREAD_THROW_MAX = 350f;

    private boolean breadFlying = false;
    private float breadFlightTimer;
    private float breadStartX;
    private float breadStartY;
    private float breadTargetX;
    private float breadTargetY;

    // Hidden event
    private boolean hiddenEventTriggered = false;
    private static final float HIDDEN_ZONE_SIZE = 100f;

    // Goose speed boost
    private boolean speedBoostActive = false;
    private float speedBoostTimer = 0f;

    private static final float SPEED_BOOST_DURATION = 10f;
    private static final float SPEED_BOOST_MULTIPLIER = 1.75f;

    // Original map dimensions
    private static final float MAP_WIDTH = 1536f;
    private static final float MAP_HEIGHT = 1024f;

    // Collie positive hidden event
    private static final float DOG_ZONE_X = 470f;
    private static final float DOG_ZONE_Y = 120f;
    private static final float DOG_ZONE_W = 145f;
    private static final float DOG_ZONE_H = 100f;

    private Dog dog;
    private boolean dogEventTriggered = false;


// =========================================================
// CREATE
// =========================================================

    @Override
    public void create() {

        inventory = new Inventory();

        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);

// Load goose textures
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

// Load item icons
        breadTexture = new Texture(
            Gdx.files.internal("bread.png")
        );

        shieldTexture = new Texture(
            Gdx.files.internal("shield.png")
        );

        breadTexture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

        shieldTexture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

// Load background map
        mapTexture = new Texture(
            Gdx.files.internal("park_map.png")
        );

        mapTexture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

// Load player sprite sheet
        playerTexture = new Texture(
            Gdx.files.internal("MCSprite sheet.png")
        );

        playerTexture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

// Split sprite sheet into individual frames.
// Sheet dimensions: 209 x 326 pixels.
// Each frame: 23 x 36 pixels.
// One-pixel border around the sheet.

        playerFrames = new TextureRegion[9][9];

        for (int row = 0; row < 9; row++) {

            for (int col = 0; col < 9; col++) {

                int x = 1 + col * 23;

                int y = playerTexture.getHeight()
                    - (1 + (row + 1) * 36);

                playerFrames[row][col] = new TextureRegion(
                    playerTexture,
                    x,
                    y,
                    23,
                    36
                );
            }
        }

// Create directional animations.
// Each animation uses all nine frames from its row.

        playerDownAnimation = new Animation<>(
            0.12f,
            playerFrames[0]
        );

        playerLeftAnimation = new Animation<>(
            0.12f,
            playerFrames[2]
        );

        playerUpAnimation = new Animation<>(
            0.12f,
            playerFrames[4]
        );

        playerRightAnimation = new Animation<>(
            0.12f,
            playerFrames[6]
        );

        playerDownAnimation.setPlayMode(
            Animation.PlayMode.LOOP
        );

        playerLeftAnimation.setPlayMode(
            Animation.PlayMode.LOOP
        );

        playerUpAnimation.setPlayMode(
            Animation.PlayMode.LOOP
        );

        playerRightAnimation.setPlayMode(
            Animation.PlayMode.LOOP
        );

        currentPlayerFrame = playerDownAnimation.getKeyFrame(0);

// Create rendering objects
        shapeRenderer = new ShapeRenderer();
        cameraMatrix = new Matrix4();

        font = new BitmapFont();
        batch = new SpriteBatch();

        font.getData().setScale(2f);
        layout = new GlyphLayout();

// Create tree collision areas
        createTreeCollisions();

// Create the border collie
        dog = new Dog();

// Start the player in the centre
        playerX = Gdx.graphics.getWidth() / 2f;
        playerY = Gdx.graphics.getHeight() / 2f;

// Create the original four geese
        geese.add(new Goose(
            normalGoose,
            MovementType.HORIZONTAL,
            100f, 400f, 200f, GOOSE_RADIUS
        ));

        geese.add(new Goose(
            greyGoose,
            MovementType.VERTICAL,
            400f, 100f, 150f, GOOSE_RADIUS
        ));

        geese.add(new Goose(
            flyingGoose,
            MovementType.DIAGONAL,
            200f, 200f, 180f, GOOSE_RADIUS
        ));

        geese.add(new Goose(
            redBreastedGoose,
            Gdx.graphics.getWidth() * 0.7f,
            Gdx.graphics.getHeight() * 0.5f,
            80f,
            1.5f,
            GOOSE_RADIUS
        ));

        updateProjection();
    }


// =========================================================
// MAP AND TREE COLLISION
// =========================================================

    private void createTreeCollisions() {

        treeObstacles.clear();

        float scaleX = Gdx.graphics.getWidth() / MAP_WIDTH;
        float scaleY = Gdx.graphics.getHeight() / MAP_HEIGHT;

// Top row
        addTreeObstacle(52, 105, 36, 45, scaleX, scaleY);
        addTreeObstacle(662, 95, 38, 60, scaleX, scaleY);
        addTreeObstacle(1390, 115, 38, 60, scaleX, scaleY);

// Middle row
        addTreeObstacle(110, 295, 40, 65, scaleX, scaleY);
        addTreeObstacle(977, 230, 38, 65, scaleX, scaleY);
        addTreeObstacle(1195, 390, 40, 75, scaleX, scaleY);

// Lower row
        addTreeObstacle(200, 590, 40, 60, scaleX, scaleY);
        addTreeObstacle(410, 680, 40, 55, scaleX, scaleY);
        addTreeObstacle(910, 780, 40, 80, scaleX, scaleY);
        addTreeObstacle(1460, 690, 40, 90, scaleX, scaleY);

// Bottom row
        addTreeObstacle(60, 770, 40, 80, scaleX, scaleY);
        addTreeObstacle(1240, 850, 40, 90, scaleX, scaleY);
    }


    private void addTreeObstacle(
        float imageX,
        float imageY,
        float imageWidth,
        float imageHeight,
        float scaleX,
        float scaleY
    ) {

        float x = imageX * scaleX;

        float y = (
            MAP_HEIGHT - imageY - imageHeight
        ) * scaleY;

        float width = imageWidth * scaleX;
        float height = imageHeight * scaleY;

        treeObstacles.add(
            new Rectangle(x, y, width, height)
        );
    }


    private boolean canPlayerOccupy(float x, float y) {

        for (Rectangle tree : treeObstacles) {

            float closestX = MathUtils.clamp(
                x,
                tree.x,
                tree.x + tree.width
            );

            float closestY = MathUtils.clamp(
                y,
                tree.y,
                tree.y + tree.height
            );

            float distanceX = x - closestX;
            float distanceY = y - closestY;

            if (distanceX * distanceX + distanceY * distanceY
                < playerRadius * playerRadius) {

                return false;
            }
        }

        return true;
    }


    private void movePlayer(float moveX, float moveY) {

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        float newX = MathUtils.clamp(
            playerX + moveX,
            playerRadius,
            screenW - playerRadius
        );

        if (canPlayerOccupy(newX, playerY)) {
            playerX = newX;
        }

        float newY = MathUtils.clamp(
            playerY + moveY,
            playerRadius,
            screenH - playerRadius
        );

        if (canPlayerOccupy(playerX, newY)) {
            playerY = newY;
        }
    }


// =========================================================
// PLAYER SPRITE ANIMATION
// =========================================================

    // Select the direction the player is facing.
    private void updatePlayerDirection(float moveX, float moveY) {

        playerMoving = moveX != 0 || moveY != 0;

        if (!playerMoving) {
            return;
        }

// Use the dominant movement axis to determine direction.
        if (Math.abs(moveX) > Math.abs(moveY)) {

            if (moveX < 0) {
                playerFacingDirection = 1;
            } else {
                playerFacingDirection = 3;
            }

        } else {

            if (moveY < 0) {
                playerFacingDirection = 0;
            } else {
                playerFacingDirection = 2;
            }
        }
    }


    // Retrieve the correct animation for the player's direction.
    private Animation<TextureRegion> getPlayerAnimation() {

        switch (playerFacingDirection) {

            case 1:
                return playerLeftAnimation;

            case 2:
                return playerUpAnimation;

            case 3:
                return playerRightAnimation;

            default:
                return playerDownAnimation;
        }
    }


    // Draw the player using the current directional animation.
    private void drawPlayer(float delta) {

        if (playerMoving && state == GameState.RUNNING) {

            playerAnimationTimer += delta;

        } else {

// Show the first frame when standing still.
            playerAnimationTimer = 0f;
        }

        currentPlayerFrame = getPlayerAnimation().getKeyFrame(
            playerAnimationTimer
        );

        float spriteWidth = 46f;
        float spriteHeight = 72f;

        batch.begin();

        batch.draw(
            currentPlayerFrame,
            playerX - spriteWidth / 2f,
            playerY - spriteHeight / 2f,
            spriteWidth,
            spriteHeight
        );

        batch.end();
    }


// =========================================================
// COLLIE HIDDEN EVENT
// =========================================================

    private void checkDogEvent() {

        if (dogEventTriggered) {
            return;
        }

        float scaleX = Gdx.graphics.getWidth() / MAP_WIDTH;
        float scaleY = Gdx.graphics.getHeight() / MAP_HEIGHT;

        float zoneX = DOG_ZONE_X * scaleX;
        float zoneY = (MAP_HEIGHT - DOG_ZONE_Y - DOG_ZONE_H) * scaleY;
        float zoneW = DOG_ZONE_W * scaleX;
        float zoneH = DOG_ZONE_H * scaleY;

        if (playerX >= zoneX && playerX <= zoneX + zoneW
            && playerY >= zoneY && playerY <= zoneY + zoneH) {

            dogEventTriggered = true;
            eventsInteracted++;

            dog.start(zoneX + zoneW / 2f, zoneY + zoneH / 2f);

            Gdx.app.log("HiddenEvent", "collie has awakened!");
        }
    }


// =========================================================
// BREAD THROWING
// =========================================================

    private void startBreadThrow() {

        breadStartX = playerX;
        breadStartY = playerY;

        pickThrowTarget();

        breadFlightTimer = 0f;
        breadFlying = true;
    }


    private void pickThrowTarget() {

        float angle = random.nextFloat() * MathUtils.PI2;

        float distance = BREAD_THROW_MIN
            + random.nextFloat() * (BREAD_THROW_MAX - BREAD_THROW_MIN);

        breadTargetX = MathUtils.clamp(
            breadStartX + distance * MathUtils.cos(angle),
            0f,
            Gdx.graphics.getWidth()
        );

        breadTargetY = MathUtils.clamp(
            breadStartY + distance * MathUtils.sin(angle),
            0f,
            Gdx.graphics.getHeight()
        );
    }


    private void updateBreadFlight(float delta) {

        if (!breadFlying) {
            return;
        }

        breadFlightTimer += delta;

        if (breadFlightTimer >= BREAD_FLIGHT_TIME) {

            breadFlying = false;
            breadActive = true;
            breadX = breadTargetX;
            breadY = breadTargetY;
            breadTimer = BREAD_DURATION;
        }
    }


// =========================================================
// CAMERA AND PROJECTION
// =========================================================

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


// =========================================================
// GOOSE SPAWNING
// =========================================================

    private float calculateSpawnRate() {

        float elapsedMinutes = (GAME_DURATION - timeRemaining) / 60f;

        return BASE_SPAWN_RATE
            + (0.01f * elapsedMinutes)
            + (0.005f * elapsedMinutes * elapsedMinutes);
    }


    private void updateGooseSpawning(float delta) {

        spawnTimer += delta;

        float spawnRate = calculateSpawnRate();
        float spawnInterval = 1f / spawnRate;

        while (spawnTimer >= spawnInterval) {

            spawnTimer -= spawnInterval;
            spawnGoose();
        }
    }


    private void spawnGoose() {

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        float radius = GOOSE_RADIUS;

        float speed = MIN_GOOSE_SPEED
            + random.nextFloat() * GOOSE_SPEED_VARIATION;

        int choice = random.nextInt(4);

        Goose newGoose;

        switch (choice) {

            case 0: {

                boolean fromLeft = random.nextBoolean();

                newGoose = new Goose(
                    normalGoose,
                    MovementType.HORIZONTAL,
                    fromLeft ? radius : width - radius,
                    radius + random.nextFloat() * (height - radius * 2),
                    speed,
                    radius
                );

                newGoose.setDirection(fromLeft ? 1 : -1, 1);

                break;
            }

            case 1: {

                boolean fromBottom = random.nextBoolean();

                newGoose = new Goose(
                    greyGoose,
                    MovementType.VERTICAL,
                    radius + random.nextFloat() * (width - radius * 2),
                    fromBottom ? radius : height - radius,
                    speed,
                    radius
                );

                newGoose.setDirection(1, fromBottom ? 1 : -1);

                break;
            }

            case 2: {

                boolean leftSide = random.nextBoolean();
                boolean bottomSide = random.nextBoolean();

                newGoose = new Goose(
                    flyingGoose,
                    MovementType.DIAGONAL,
                    leftSide ? radius : width - radius,
                    bottomSide ? radius : height - radius,
                    speed,
                    radius
                );

                newGoose.setDirection(
                    leftSide ? 1 : -1,
                    bottomSide ? 1 : -1
                );

                break;
            }

            default: {

                float centerX = width * (
                    CIRCLE_CENTRE_MIN
                        + random.nextFloat() * CIRCLE_CENTRE_RANGE
                );

                float centerY = height * (
                    CIRCLE_CENTRE_MIN
                        + random.nextFloat() * CIRCLE_CENTRE_RANGE
                );

                float orbitRadius = nextCircleRadius;

                nextCircleRadius += CIRCLE_RADIUS_STEP;

                if (nextCircleRadius > MAX_CIRCLE_RADIUS) {
                    nextCircleRadius = MIN_CIRCLE_RADIUS;
                }

                float margin = orbitRadius + radius;

                centerX = MathUtils.clamp(
                    centerX,
                    margin,
                    width - margin
                );

                centerY = MathUtils.clamp(
                    centerY,
                    margin,
                    height - margin
                );

                float angularSpeed = MIN_ANGULAR_SPEED
                    + random.nextFloat() * ANGULAR_SPEED_VARIATION;

                newGoose = new Goose(
                    redBreastedGoose,
                    centerX,
                    centerY,
                    orbitRadius,
                    angularSpeed,
                    radius
                );

                newGoose.setCircleAngle(
                    random.nextFloat() * (float) (Math.PI * 2)
                );

                break;
            }
        }

        geese.add(newGoose);

        Gdx.app.log(
            "GooseSpawner",
            "New goose spawned! Total geese: " + geese.size()
        );
    }


// =========================================================
// WINDOW RESIZING
// =========================================================

    @Override
    public void resize(int width, int height) {

        if (width == 0 || height == 0) {
            return;
        }

        updateProjection();

        createTreeCollisions();
    }


// =========================================================
// MAIN GAME LOOP
// =========================================================

    @Override
    public void render() {

        if (Gdx.graphics.getWidth() == 0
            || Gdx.graphics.getHeight() == 0) {

            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float realDelta = Gdx.graphics.getDeltaTime();
        float delta = Math.min(realDelta, MAX_FRAME_TIME);

// Draw background map
        batch.begin();

        batch.draw(
            mapTexture,
            0,
            0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        batch.end();


// Countdown uses real elapsed time.
        if (state == GameState.RUNNING) {

            timeRemaining = Math.max(
                0f,
                timeRemaining - realDelta
            );

            if (shieldGraceTimer > 0f) {

                shieldGraceTimer = Math.max(
                    0f,
                    shieldGraceTimer - realDelta
                );
            }
        }

        float movement = playerSpeed * delta;


// Select bread with 1.
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            inventory.selectSlot(0);
        }

// Select shield with 2.
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            inventory.selectSlot(1);
        }


// E uses bread if bread is selected.
        if (state == GameState.RUNNING
            && Gdx.input.isKeyJustPressed(Input.Keys.E)
            && inventory.getSelectedSlot() == 0
            && !breadFlying) {

            String itemUsed = inventory.useSelectedItem();

            if (itemUsed != null) {

                itemsUsed++;

                startBreadThrow();

                Gdx.app.log("Inventory", "Threw bread!");
            }
        }


// SPACE starts the game.
        if (!started
            && state == GameState.PAUSED
            && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {

            started = true;
            state = GameState.RUNNING;
        }


// ESC pauses and resumes.
        if (started
            && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

            togglePause();
        }


// Instructions screen.
        if (state == GameState.PAUSED
            && Gdx.input.isKeyJustPressed(Input.Keys.I)) {

            state = GameState.INSTRUCTIONS;

        } else if (state == GameState.INSTRUCTIONS
            && (Gdx.input.isKeyJustPressed(Input.Keys.I)
            || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))) {

            state = GameState.PAUSED;
        }


// R restarts after winning or losing.
        if ((state == GameState.WON || state == GameState.LOST)
            && Gdx.input.isKeyJustPressed(Input.Keys.R)) {

            resetGame();
            state = GameState.RUNNING;
        }


// Only move objects while game is running.
        if (state == GameState.RUNNING) {

            float moveX = 0f;
            float moveY = 0f;

            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                moveY += movement;
            }

            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                moveY -= movement;
            }

            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                moveX -= movement;
            }

            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                moveX += movement;
            }

// Update player direction and animation state.
            updatePlayerDirection(moveX, moveY);

// Move player while checking tree collisions.
            movePlayer(moveX, moveY);


// Spawn additional geese.
            updateGooseSpawning(delta);

// Advance thrown bread.
            updateBreadFlight(delta);

// Move all geese.
            updateGooseMovement(delta);

// Move the collie.
            dog.update(delta, geese);

// Check collisions with all geese.
            checkGooseCollisions();

// Check for a win.
            if (state == GameState.RUNNING && timeRemaining <= 0) {

                state = GameState.WON;

                Gdx.app.log("Game", "Survived - You Win");
            }


// Hidden event in the top-right corner.
            float screenW = Gdx.graphics.getWidth();
            float screenH = Gdx.graphics.getHeight();

            if (!hiddenEventTriggered
                && playerX > screenW - HIDDEN_ZONE_SIZE
                && playerY > screenH - HIDDEN_ZONE_SIZE) {

                hiddenEventTriggered = true;
                eventsInteracted++;

                speedBoostActive = true;
                speedBoostTimer = SPEED_BOOST_DURATION;

                Gdx.app.log(
                    "HiddenEvent",
                    "geese speed boost event triggered"
                );
            }

// Positive hidden event: sleeping collie.
            checkDogEvent();
        }


// Update mouse visibility.
        updateCursorVisibility();


// Draw the hidden event.
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


// Draw player sprite instead of the original blue circle.
        drawPlayer(delta);


// Shadow marking where bread will land.
        if (breadFlying) {

            Gdx.gl.glEnable(GL20.GL_BLEND);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 0.35f);

            shapeRenderer.ellipse(
                breadTargetX - 14f,
                breadTargetY - 7f,
                28f,
                14f
            );

            shapeRenderer.end();

            Gdx.gl.glDisable(GL20.GL_BLEND);
        }


// Draw bread, geese and HUD.
        batch.begin();

// Show bread at its landing position.
        if (breadActive) {

            float droppedBreadSize = 36f;

            batch.draw(
                breadTexture,
                breadX - droppedBreadSize / 2f,
                breadY - droppedBreadSize / 2f,
                droppedBreadSize,
                droppedBreadSize
            );
        }


// Draw bread in flight.
        if (breadFlying) {

            float t = breadFlightTimer / BREAD_FLIGHT_TIME;

            float groundX = MathUtils.lerp(
                breadStartX,
                breadTargetX,
                t
            );

            float groundY = MathUtils.lerp(
                breadStartY,
                breadTargetY,
                t
            );

            float arc = 4f * t * (1f - t);
            float height = arc * BREAD_ARC_HEIGHT;

            float breadSize = 36f;

            batch.draw(
                breadTexture,
                groundX - breadSize / 2f,
                groundY + height - breadSize / 2f,
                breadSize,
                breadSize
            );
        }


// Draw all geese.
        for (Goose goose : geese) {
            goose.draw(batch);
        }


// Draw the collie.
        dog.draw(batch);


// Timer display.
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


// Items used counter.
        font.draw(
            batch,
            "Items used: " + itemsUsed,
            20,
            Gdx.graphics.getHeight() - 60
        );


// Events interacted counter.
        font.draw(
            batch,
            "Events triggered: " + eventsInteracted,
            20,
            Gdx.graphics.getHeight() - 100
        );


// Goose speed boost warning.
        if (speedBoostActive) {

            font.setColor(1, 0.3f, 0.3f, 1);

            font.draw(
                batch,
                "GEESE ENRAGED! "
                    + (int) Math.ceil(speedBoostTimer) + "s",
                20,
                Gdx.graphics.getHeight() - 140
            );

            font.setColor(1, 1, 1, 1);
        }

        batch.end();


// Draw start or pause screen.
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


// Draw instructions.
        if (state == GameState.INSTRUCTIONS) {
            drawInstructions();
        }


// Draw win or loss screen.
        if (state == GameState.LOST) {

            drawOverlay(
                "YOU LOST",
                "Press R to restart"
            );

        } else if (state == GameState.WON) {

            drawOverlay(
                "YOU WIN!",
                "Press R to restart"
            );
        }


// Draw inventory hotbar.
        if (started) {
            drawHotbar();
        }
    }


// =========================================================
// PLAYER AND GOOSE GAMEPLAY
// =========================================================

    private void updateCursorVisibility() {

        boolean shouldHideCursor = state == GameState.RUNNING;

        if (shouldHideCursor != cursorHidden) {

            if (shouldHideCursor) {

                Gdx.graphics.setSystemCursor(
                    Cursor.SystemCursor.None
                );

            } else {

                Gdx.graphics.setSystemCursor(
                    Cursor.SystemCursor.Arrow
                );
            }

            cursorHidden = shouldHideCursor;
        }
    }


    private void updateGooseMovement(float realDelta) {

// Update bread distraction timer.
        if (breadActive) {

            breadTimer -= realDelta;

            if (breadTimer <= 0) {

                breadActive = false;

                for (Goose goose : geese) {
                    goose.stopFleeing();
                }
            }
        }


// Update speed boost timer.
        if (speedBoostActive) {

            speedBoostTimer -= realDelta;

            if (speedBoostTimer <= 0) {
                speedBoostActive = false;
            }
        }


// Geese move faster during the boost.
        float delta = speedBoostActive
            ? realDelta * SPEED_BOOST_MULTIPLIER
            : realDelta;


// Scale the circle surrounding bread with goose count.
        float ringRadius = Math.min(
            BREAD_RING_BASE + geese.size() * BREAD_RING_PER_GOOSE,
            BREAD_RING_MAX
        );


// Move every goose.
        for (int i = 0; i < geese.size(); i++) {

            Goose goose = geese.get(i);

            if (breadActive) {

// Spread geese around the bread.
                float angle = (float) (
                    2 * Math.PI * i / geese.size()
                );

                float targetX = MathUtils.clamp(
                    breadX + ringRadius * (float) Math.cos(angle),
                    GOOSE_RADIUS,
                    Gdx.graphics.getWidth() - GOOSE_RADIUS
                );

                float targetY = MathUtils.clamp(
                    breadY + ringRadius * (float) Math.sin(angle),
                    GOOSE_RADIUS,
                    Gdx.graphics.getHeight() - GOOSE_RADIUS
                );

                goose.lureTowards(
                    targetX,
                    targetY,
                    BREAD_SPEED * delta
                );

            } else {

                goose.update(delta);
            }
        }
    }


    private void checkGooseCollisions() {

        if (shieldGraceTimer > 0f) {
            return;
        }

        for (Goose goose : geese) {

            if (goose.collidesWith(
                playerX,
                playerY,
                playerRadius
            )) {

                if (inventory.hasShield()) {

                    inventory.consumeShield();

                    itemsUsed++;

                    goose.disperse();

                    shieldGraceTimer = SHIELD_GRACE_DURATION;

                    Gdx.app.log(
                        "Inventory",
                        "Shield blocked a goose attack!"
                    );

                    return;

                } else {

                    state = GameState.LOST;

                    Gdx.app.log(
                        "Game",
                        "The geese caught you!"
                    );

                    return;
                }
            }
        }
    }


// =========================================================
// INVENTORY HOTBAR
// =========================================================

    private void drawHotbar() {

        float slotSize = 55f;
        float gap = 6f;

        float totalWidth = (slotSize * 9) + (gap * 8);

        float startX = (
            Gdx.graphics.getWidth() - totalWidth
        ) / 2f;

        float startY = 15f;


// Draw slot backgrounds.
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < 9; i++) {

            float x = startX + i * (slotSize + gap);

            shapeRenderer.setColor(
                0.2f, 0.2f, 0.2f, 1f
            );

            shapeRenderer.rect(
                x,
                startY,
                slotSize,
                slotSize
            );
        }

        shapeRenderer.end();


// Highlight selected slot.
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


// Draw slot numbers, icons and quantities.
        batch.begin();

        font.getData().setScale(1f);

        for (int i = 0; i < 9; i++) {

            float x = startX + i * (slotSize + gap);

            font.draw(
                batch,
                String.valueOf(i + 1),
                x + 5,
                startY + slotSize - 5
            );

            String item = inventory.getItem(i);


// Bread icon.
            if (item.equals("Bread")) {

                batch.draw(
                    breadTexture,
                    x + 11,
                    startY + 15,
                    33,
                    30
                );

// Shield icon.
            } else if (item.equals("Shield")) {

                batch.draw(
                    shieldTexture,
                    x + 12,
                    startY + 14,
                    31,
                    33
                );
            }


// Item quantity.
            if (inventory.getQuantity(i) > 0) {

                font.draw(
                    batch,
                    "x" + inventory.getQuantity(i),
                    x + slotSize - 22,
                    startY + 9
                );
            }
        }

        font.getData().setScale(2f);

        batch.end();
    }


// =========================================================
// GAME STATE
// =========================================================

    private void togglePause() {

        if (state == GameState.RUNNING) {

            state = GameState.PAUSED;

        } else if (state == GameState.PAUSED) {

            state = GameState.RUNNING;
        }
    }


    private void resetGame() {

        timeRemaining = GAME_DURATION;

        playerX = Gdx.graphics.getWidth() / 2f;
        playerY = Gdx.graphics.getHeight() / 2f;

// Reset player animation.
        playerAnimationTimer = 0f;
        playerMoving = false;
        playerFacingDirection = 0;


// Remove all additional geese.
        while (geese.size() > INITIAL_GOOSE_COUNT) {

            geese.remove(geese.size() - 1);
        }


// Reset original four geese.
        for (Goose goose : geese) {
            goose.reset();
        }


// Reset spawning system.
        spawnTimer = 0f;
        nextCircleRadius = MIN_CIRCLE_RADIUS;


// Reset bread.
        breadActive = false;
        breadTimer = 0f;
        breadFlying = false;
        breadFlightTimer = 0f;


// Reset shield grace period.
        shieldGraceTimer = 0f;


// Reset hidden event.
        hiddenEventTriggered = false;

        speedBoostActive = false;
        speedBoostTimer = 0f;


// Reset the positive hidden event.
        dogEventTriggered = false;
        dog.reset();


// Reset inventory.
        inventory.reset();


// Reset statistics.
        itemsUsed = 0;
        eventsInteracted = 0;
    }


// =========================================================
// MENUS AND INSTRUCTIONS
// =========================================================

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
            "1 / 2 - select bread / shield",
            "E - use selected item",
            "ESC - pause / resume",
            "R - restart after winning or losing",
            "",

            "ITEMS",
            "Bread - lures all geese to you for 5 seconds",
            "Shield - automatically blocks one hit",
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


// =========================================================
// PAUSE AND DISPOSE
// =========================================================

    @Override
    public void pause() {

        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
        }

        updateCursorVisibility();
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

        breadTexture.dispose();
        shieldTexture.dispose();

        mapTexture.dispose();

// Dispose of player sprite sheet.
        playerTexture.dispose();

        dog.dispose();

// Restore normal mouse pointer.
        Gdx.graphics.setSystemCursor(
            Cursor.SystemCursor.Arrow
        );
    }
}
