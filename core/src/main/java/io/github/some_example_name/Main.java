
package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

import java.util.ArrayList;
import java.util.List;

public class Main extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private Matrix4 cameraMatrix;

    private static final float GAME_DURATION = 300f;
    private float timeRemaining = GAME_DURATION;

    private static final float MAX_FRAME_TIME = 0.05f;
    private static final int INITIAL_GOOSE_COUNT = 4;

    // Game state
    private GameState state = GameState.PAUSED;
    private boolean started = false;

    // Mouse pointer visibility
    private boolean cursorHidden = false;

    // Gameplay systems
    private CollisionManager collisionManager;
    private GameContext ctx;

    private GooseSpawner gooseSpawner;
    private BreadSystem breadSystem;
    private GameRenderer gameRenderer;

    // Rendering and HUD
    private HUD hud;
    private SpriteBatch batch;

    // Game assets
    private Texture normalGoose;
    private Texture flyingGoose;
    private Texture greyGoose;
    private Texture redBreastedGoose;

    private Texture breadTexture;
    private Texture shieldTexture;
    private Texture mapTexture;

    // Hidden events
    private final List<HiddenEvent> events = new ArrayList<>();
    private final List<String> statusLines = new ArrayList<>();

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

        hud = new HUD(
            batch,
            shapeRenderer,
            breadTexture,
            shieldTexture
        );

        // Create tree collision areas
        collisionManager.createTreeCollisions(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            mapTexture.getWidth(),
            mapTexture.getHeight()
        );

        // Create the border collie
        dog = new Dog();

        // Create hidden events
        events.add(new GeeseEnrageEvent());

        // Create shared game context
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
            100f, 400f, 200f, 22f
        ));

        ctx.geese.add(new Goose(
            greyGoose,
            MovementType.VERTICAL,
            400f, 100f, 150f, 22f
        ));

        ctx.geese.add(new Goose(
            flyingGoose,
            MovementType.DIAGONAL,
            200f, 200f, 180f, 22f
        ));

        ctx.geese.add(new Goose(
            redBreastedGoose,
            Gdx.graphics.getWidth() * 0.7f,
            Gdx.graphics.getHeight() * 0.5f,
            80f,
            1.5f,
            22f
        ));

        // Initialise gameplay systems
        gooseSpawner = new GooseSpawner(
            normalGoose,
            flyingGoose,
            greyGoose,
            redBreastedGoose
        );

        breadSystem = new BreadSystem(breadTexture);

        // Initialise world renderer
        gameRenderer = new GameRenderer(
            shapeRenderer,
            batch,
            mapTexture,
            ctx,
            dog,
            events,
            breadSystem
        );

        updateProjection();
    }


    // =========================================================
    // COLLIE HIDDEN EVENT
    // =========================================================

    private void checkDogEvent() {

        if (dogEventTriggered) {
            return;
        }

        float mapWidth = mapTexture.getWidth();
        float mapHeight = mapTexture.getHeight();

        float scaleX = Gdx.graphics.getWidth() / mapWidth;
        float scaleY = Gdx.graphics.getHeight() / mapHeight;

        float zoneX = DOG_ZONE_X * scaleX;
        float zoneY = (mapHeight - DOG_ZONE_Y - DOG_ZONE_H) * scaleY;
        float zoneW = DOG_ZONE_W * scaleX;
        float zoneH = DOG_ZONE_H * scaleY;

        if (ctx.player.getX() >= zoneX
            && ctx.player.getX() <= zoneX + zoneW
            && ctx.player.getY() >= zoneY
            && ctx.player.getY() <= zoneY + zoneH) {

            dogEventTriggered = true;
            ctx.recordEvent();

            dog.start(
                zoneX + zoneW / 2f,
                zoneY + zoneH / 2f
            );

            Gdx.app.log(
                "HiddenEvent",
                "collie has awakened!"
            );
        }
    }


    // =========================================================
    // CAMERA AND PROJECTION
    // =========================================================

    private void updateProjection() {

        Gdx.gl.glViewport(
            0,
            0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        cameraMatrix.setToOrtho2D(
            0,
            0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        shapeRenderer.setProjectionMatrix(cameraMatrix);
        batch.setProjectionMatrix(cameraMatrix);
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

        collisionManager.createTreeCollisions(
            width,
            height,
            mapTexture.getWidth(),
            mapTexture.getHeight()
        );
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

        // Countdown uses real elapsed time.
        if (state == GameState.RUNNING) {

            timeRemaining = Math.max(
                0f,
                timeRemaining - realDelta
            );

            collisionManager.updateShieldGraceTimer(realDelta);
        }

        // Handle item use.
        if (state == GameState.RUNNING
            && !breadSystem.isFlying()) {

            String itemUsed = ctx.inventory.handleItemUseInput();

            if (itemUsed != null) {

                ctx.recordItemUsed();

                breadSystem.startThrow(
                    ctx.player,
                    Gdx.graphics.getWidth(),
                    Gdx.graphics.getHeight()
                );

                Gdx.app.log(
                    "Inventory",
                    "Threw bread!"
                );
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

        // Update gameplay.
        if (state == GameState.RUNNING) {

            ctx.player.update(delta, collisionManager);

            // Spawn additional geese.
            gooseSpawner.update(
                delta,
                timeRemaining,
                ctx,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
            );

            // Update bread flight and goose attraction.
            breadSystem.updateFlight(delta);

            breadSystem.updateGeese(
                delta,
                ctx,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
            );

            // Update hidden events.
            for (HiddenEvent event : events) {
                event.update(delta, ctx);
            }

            // Move the collie.
            dog.update(delta, ctx);

            // Check collisions with all geese.
            CollisionManager.GooseCollisionResult collisionResult =
                collisionManager.checkGooseCollisions(ctx);

            if (collisionResult
                == CollisionManager.GooseCollisionResult.SHIELD_BLOCKED) {

                ctx.recordItemUsed();

                Gdx.app.log(
                    "Inventory",
                    "Shield blocked a goose attack!"
                );

            } else if (collisionResult
                == CollisionManager.GooseCollisionResult.PLAYER_HIT) {

                state = GameState.LOST;

                Gdx.app.log(
                    "Game",
                    "The geese caught you!"
                );
            }

            // Check for a win.
            if (state == GameState.RUNNING
                && timeRemaining <= 0) {

                state = GameState.WON;

                Gdx.app.log(
                    "Game",
                    "Survived - You Win"
                );
            }

            // Check the positive hidden event.
            checkDogEvent();
        }

        // Update mouse visibility.
        updateCursorVisibility();

        // Render the game world.
        gameRenderer.render();

        // Collect hidden-event status messages.
        statusLines.clear();

        for (HiddenEvent event : events) {

            String text = event.getStatusText();

            if (text != null) {
                statusLines.add(text);
            }
        }

        // Draw HUD.
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
    // PLAYER AND GAME STATE
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

        // Remove additional geese.
        while (ctx.geese.size() > INITIAL_GOOSE_COUNT) {

            ctx.geese.remove(ctx.geese.size() - 1);
        }

        // Reset original geese.
        for (Goose goose : ctx.geese) {
            goose.reset();
        }

        // Reset spawning and bread systems.
        gooseSpawner.reset();
        breadSystem.reset();

        // Reset collision protection.
        collisionManager.resetShieldGraceTimer();

        // Reset hidden events.
        for (HiddenEvent event : events) {
            event.reset(ctx);
        }

        // Reset the positive hidden event.
        dogEventTriggered = false;
        dog.reset();

        // Reset inventory and statistics.
        ctx.inventory.reset();
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

        // Dispose of player sprite sheet and hidden-event assets.
        ctx.player.dispose();

        for (HiddenEvent event : events) {
            event.dispose();
        }

        dog.dispose();

        // Restore normal mouse pointer.
        Gdx.graphics.setSystemCursor(
            Cursor.SystemCursor.Arrow
        );
    }
}
