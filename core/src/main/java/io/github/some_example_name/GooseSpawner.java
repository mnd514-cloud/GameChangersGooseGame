package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

import java.util.Random;

public class GooseSpawner {

    private static final float BASE_SPAWN_RATE = 0.5f;

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

    private final Texture normalGoose;
    private final Texture flyingGoose;
    private final Texture greyGoose;
    private final Texture redBreastedGoose;

    private final Random random = new Random();

    private float spawnTimer = 0f;
    private float nextCircleRadius = MIN_CIRCLE_RADIUS;

    public GooseSpawner(
        Texture normalGoose,
        Texture flyingGoose,
        Texture greyGoose,
        Texture redBreastedGoose
    ) {
        this.normalGoose = normalGoose;
        this.flyingGoose = flyingGoose;
        this.greyGoose = greyGoose;
        this.redBreastedGoose = redBreastedGoose;
    }

    public void update(
        float delta,
        float timeRemaining,
        GameContext ctx,
        float width,
        float height
    ) {
        spawnTimer += delta;

        float elapsedMinutes = (300f - timeRemaining) / 60f;

        float spawnRate = BASE_SPAWN_RATE
            + (0.01f * elapsedMinutes)
            + (0.005f * elapsedMinutes * elapsedMinutes);

        float spawnInterval = 1f / spawnRate;

        while (spawnTimer >= spawnInterval) {
            spawnTimer -= spawnInterval;
            spawnGoose(ctx, width, height);
        }
    }

    private void spawnGoose(
        GameContext ctx,
        float width,
        float height
    ) {
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
                    random.nextFloat() * MathUtils.PI2
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

    public void reset() {
        spawnTimer = 0f;
        nextCircleRadius = MIN_CIRCLE_RADIUS;
    }
}
