package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import java.util.ArrayList;
import java.util.List;

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

    // Gameplay systems
    private CollisionManager collisionManager;
    private GameContext ctx;

    private HUD hud;
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

    private final List<HiddenEvent> events = new ArrayList<>();
    private final List<String> statusLines = new ArrayList<>();

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

        collisionManager = new CollisionManager();

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


        // Create rendering objects
        shapeRenderer = new ShapeRenderer();
        cameraMatrix = new Matrix4();

        batch = new SpriteBatch();
        hud = new HUD(batch, shapeRenderer, breadTexture, shieldTexture);


        // Create tree collision areas
        collisionManager.createTreeCollisions(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        // Create the border collie
        dog = new Dog();

        // Create hidden events
        events.add(new GeeseEnrageEvent());

        // Create the shared game context
        ctx = new GameContext(
            new Player(
                Gdx.graphics.getWidth() / 2f,
                Gdx.graphics.getHeight() / 2f
            ),
            new Inventory(),
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        // Create the original four geese
        ctx.geese.add(new Goose(
            normalGoose,
            MovementType.HORIZONTAL,
            100f, 400f, 200f, GOOSE_RADIUS
        ));

        ctx.geese.add(new Goose(
            greyGoose,
            MovementType.VERTICAL,
            400f, 100f, 150f, GOOSE_RADIUS
        ));

        ctx.geese.add(new Goose(
            flyingGoose,
            MovementType.DIAGONAL,
            200f, 200f, 180f, GOOSE_RADIUS
        ));

        ctx.geese.add(new Goose(
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

        if (ctx.player.getX() >= zoneX && ctx.player.getX() <= zoneX + zoneW
            && ctx.player.getY() >= zoneY && ctx.player.getY() <= zoneY + zoneH) {

            dogEventTriggered = true;
            ctx.recordEvent();

            dog.start(zoneX + zoneW / 2f, zoneY + zoneH / 2f);

            Gdx.app.log("HiddenEvent", "collie has awakened!");
        }
    }

// =========================================================
// BREAD THROWING
// =========================================================

    private void startBreadThrow() {

        breadStartX = ctx.player.getX();
        breadStartY = ctx.player.getY();

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

        ctx.geese.add(newGoose);

        Gdx.app.log(
            "GooseSpawner",
            "New goose spawned! Total geese: " + ctx.geese.size()
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
        ctx.setWorldSize(width, height);

        collisionManager.createTreeCollisions(width, height);
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

            collisionManager.updateShieldGraceTimer(realDelta);
        }

        if (state == GameState.RUNNING && !breadFlying) {
            String itemUsed = ctx.inventory.handleItemUseInput();

            if (itemUsed != null) {
                ctx.recordItemUsed();
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

            ctx.player.update(delta, collisionManager);

            // Spawn additional geese.
            updateGooseSpawning(delta);

            // Advance thrown bread.
            updateBreadFlight(delta);

            // Move all geese.
            updateGooseMovement(delta);

            for (HiddenEvent event : events) {
                event.update(delta, ctx);
            }

            // Move the collie.
            dog.update(delta, ctx);

            // Check collisions with all geese.
            CollisionManager.GooseCollisionResult collisionResult =
                collisionManager.checkGooseCollisions(ctx);

            if (collisionResult == CollisionManager.GooseCollisionResult.SHIELD_BLOCKED) {
                ctx.recordItemUsed();
                Gdx.app.log("Inventory", "Shield blocked a goose attack!");
            } else if (collisionResult == CollisionManager.GooseCollisionResult.PLAYER_HIT) {
                state = GameState.LOST;
                Gdx.app.log("Game", "The geese caught you!");
            }

            // Check for a win.
            if (state == GameState.RUNNING && timeRemaining <= 0) {

                state = GameState.WON;

                Gdx.app.log("Game", "Survived - You Win");
            }

        // Positive hidden event: sleeping collie.
            checkDogEvent();
        }

        // Update mouse visibility.
        updateCursorVisibility();

        // Draw the hidden event.
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (HiddenEvent event : events) {
            event.drawShapes(shapeRenderer);
        }

        shapeRenderer.end();

        // Draw player sprite.
        batch.begin();
        ctx.player.draw(batch);
        batch.end();

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

        // Draw bread, geese, the HUD and the collie.
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
        for (Goose goose : ctx.geese) {
            goose.draw(batch);
        }

        // Draw the collie.
        dog.draw(batch);
        batch.end();

        statusLines.clear();

        for (HiddenEvent event : events) {
            String text = event.getStatusText();

            if (text != null) {
                statusLines.add(text);
            }
        }

        hud.drawStats(
            timeRemaining,
            ctx.getItemsUsed(),
            ctx.getEventsInteracted(),
            statusLines
        );

        hud.drawScreen(state, started);

        if (started) {
            hud.drawHotbar(ctx.inventory);
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

                for (Goose goose : ctx.geese) {
                    goose.stopFleeing();
                }
            }
        }

        // Geese move faster while an event (e.g. enrage) boosts them
        float delta = realDelta * ctx.getGooseSpeedMultipler();

        // Scale the circle surrounding bread with goose count.
        float ringRadius = Math.min(
            BREAD_RING_BASE + ctx.geese.size() * BREAD_RING_PER_GOOSE,
            BREAD_RING_MAX
        );

        // Move every goose.
        for (int i = 0; i < ctx.geese.size(); i++) {

            Goose goose = ctx.geese.get(i);

            if (breadActive) {

                // Spread geese around the bread.
                float angle = (float) (
                    2 * Math.PI * i / ctx.geese.size()
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

                goose.update(delta, ctx.getWorldWidth(), ctx.getWorldHeight());
            }
        }
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

        ctx.player.reset(
            Gdx.graphics.getWidth() / 2f,
            Gdx.graphics.getHeight() / 2f
        );

        // Remove all additional geese.
        while (ctx.geese.size() > INITIAL_GOOSE_COUNT) {

            ctx.geese.remove(ctx.geese.size() - 1);
        }

        // Reset original four geese.
        for (Goose goose : ctx.geese) {
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

        // Reset collision protection period.
        collisionManager.resetShieldGraceTimer();

        for (HiddenEvent event: events) {
            event.reset(ctx);
        }

        // Reset the positive hidden event.
        dogEventTriggered = false;
        dog.reset();

        // Reset inventory.
        ctx.inventory.reset();

        // Reset statistics.
        ctx.resetStats();
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
        hud.dispose();

        normalGoose.dispose();
        flyingGoose.dispose();
        greyGoose.dispose();
        redBreastedGoose.dispose();

        breadTexture.dispose();
        shieldTexture.dispose();

        mapTexture.dispose();

        // Dispose of player sprite sheet.
        ctx.player.dispose();

        dog.dispose();

        // Restore normal mouse pointer.
        Gdx.graphics.setSystemCursor(
            Cursor.SystemCursor.Arrow
        );
    }
}
