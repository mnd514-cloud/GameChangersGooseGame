
package io.github.some_example_name;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

/**
 * Handles world collision detection and collision response.
 * This includes player movement around trees and collisions with geese.
 */
public class CollisionManager {

    private static final float SHIELD_GRACE_DURATION = 0.75f;

    private final ArrayList<Rectangle> treeObstacles = new ArrayList<>();
    private final Vector2 resolvedPlayerPosition = new Vector2();

    private float shieldGraceTimer = 0f;

    public enum GooseCollisionResult {
        NONE,
        SHIELD_BLOCKED,
        PLAYER_HIT
    }

    /** Rebuilds tree collision rectangles to match the current window size. */
    public void createTreeCollisions(
        float screenWidth,
        float screenHeight,
        float mapWidth,
        float mapHeight
    ) {

        treeObstacles.clear();

        float scaleX = screenWidth / mapWidth;
        float scaleY = screenHeight / mapHeight;

        // Top row
        addTreeObstacle(52, 105, 36, 45, scaleX, scaleY, mapHeight);
        addTreeObstacle(662, 95, 38, 60, scaleX, scaleY, mapHeight);
        addTreeObstacle(1390, 115, 38, 60, scaleX, scaleY, mapHeight);

        // Middle row
        addTreeObstacle(110, 295, 40, 65, scaleX, scaleY, mapHeight);
        addTreeObstacle(977, 230, 38, 65, scaleX, scaleY, mapHeight);
        addTreeObstacle(1195, 390, 40, 75, scaleX, scaleY, mapHeight);

        // Lower row
        addTreeObstacle(200, 590, 40, 60, scaleX, scaleY, mapHeight);
        addTreeObstacle(410, 680, 40, 55, scaleX, scaleY, mapHeight);
        addTreeObstacle(910, 780, 40, 80, scaleX, scaleY, mapHeight);
        addTreeObstacle(1460, 690, 40, 90, scaleX, scaleY, mapHeight);

        // Bottom row
        addTreeObstacle(60, 770, 40, 80, scaleX, scaleY, mapHeight);
        addTreeObstacle(1240, 850, 40, 90, scaleX, scaleY, mapHeight);
    }

    private void addTreeObstacle(
        float imageX,
        float imageY,
        float imageWidth,
        float imageHeight,
        float scaleX,
        float scaleY,
        float mapHeight
    ) {
        float x = imageX * scaleX;

        // Convert from image top-left coordinates to LibGDX bottom-left coordinates.
        float y = (mapHeight - imageY - imageHeight) * scaleY;

        treeObstacles.add(new Rectangle(
            x,
            y,
            imageWidth * scaleX,
            imageHeight * scaleY
        ));
    }

    /** Checks whether the player's circular collision body can occupy a position. */
    public boolean canPlayerOccupy(float x, float y, float playerRadius) {

        for (Rectangle tree : treeObstacles) {
            float closestX = MathUtils.clamp(x, tree.x, tree.x + tree.width);
            float closestY = MathUtils.clamp(y, tree.y, tree.y + tree.height);

            float distanceX = x - closestX;
            float distanceY = y - closestY;

            if (distanceX * distanceX + distanceY * distanceY
                < playerRadius * playerRadius) {
                return false;
            }
        }

        return true;
    }

    /**
     * Resolves horizontal and vertical movement separately so the player can
     * slide along the sides of tree obstacles. The returned Vector2 is reused
     * by this manager; read its values before calling this method again.
     */
    public Vector2 movePlayer(
        float playerX,
        float playerY,
        float moveX,
        float moveY,
        float playerRadius,
        float screenWidth,
        float screenHeight
    ) {
        float newX = MathUtils.clamp(
            playerX + moveX,
            playerRadius,
            screenWidth - playerRadius
        );

        if (canPlayerOccupy(newX, playerY, playerRadius)) {
            playerX = newX;
        }

        float newY = MathUtils.clamp(
            playerY + moveY,
            playerRadius,
            screenHeight - playerRadius
        );

        if (canPlayerOccupy(playerX, newY, playerRadius)) {
            playerY = newY;
        }

        return resolvedPlayerPosition.set(playerX, playerY);
    }

    /** Updates the brief invulnerability period after a shield blocks a hit. */
    public void updateShieldGraceTimer(float delta) {
        shieldGraceTimer = Math.max(0f, shieldGraceTimer - delta);
    }

    public void resetShieldGraceTimer() {
        shieldGraceTimer = 0f;
    }

    /** Checks player/goose collisions and consumes a shield when one is available. */
    public GooseCollisionResult checkGooseCollisions(GameContext ctx) {
        if (shieldGraceTimer > 0f) {
            return GooseCollisionResult.NONE;
        }

        for (Goose goose : ctx.geese) {
            if (!goose.collidesWith(
                ctx.player.getX(),
                ctx.player.getY(),
                ctx.player.getRadius()
            )) {
                continue;
            }

            if (ctx.inventory.hasShield()) {
                ctx.inventory.consumeShield();
                goose.disperse();
                shieldGraceTimer = SHIELD_GRACE_DURATION;
                return GooseCollisionResult.SHIELD_BLOCKED;
            }

            return GooseCollisionResult.PLAYER_HIT;
        }

        return GooseCollisionResult.NONE;
    }
}
