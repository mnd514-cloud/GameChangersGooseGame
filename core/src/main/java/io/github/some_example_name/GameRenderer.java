package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.List;

public class GameRenderer {

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final Texture mapTexture;
    private final GameContext ctx;
    private final Dog dog;
    private final List<HiddenEvent> events;
    private final BreadSystem breadSystem;

    public GameRenderer(
        ShapeRenderer shapeRenderer,
        SpriteBatch batch,
        Texture mapTexture,
        GameContext ctx,
        Dog dog,
        List<HiddenEvent> events,
        BreadSystem breadSystem
    ) {
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.mapTexture = mapTexture;
        this.ctx = ctx;
        this.dog = dog;
        this.events = events;
        this.breadSystem = breadSystem;
    }

    public void render() {
        drawBackground();
        drawEventShapes();
        drawEventSpritesAndPlayer();
        drawBreadShadow();
        drawBreadGeeseAndDog();
    }

    private void drawBackground() {
        batch.begin();

        batch.draw(
            mapTexture,
            0,
            0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        batch.end();
    }

    private void drawEventShapes() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (HiddenEvent event : events) {
            event.drawShapes(shapeRenderer);
        }

        shapeRenderer.end();
    }

    private void drawEventSpritesAndPlayer() {
        batch.begin();

        for (HiddenEvent event : events) {
            event.drawSprites(batch);
        }

        ctx.player.draw(batch);

        batch.end();
    }

    private void drawBreadShadow() {
        if (!breadSystem.isFlying()) {
            return;
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.35f);

        shapeRenderer.ellipse(
            breadSystem.getTargetX() - 14f,
            breadSystem.getTargetY() - 7f,
            28f,
            14f
        );

        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawBreadGeeseAndDog() {
        batch.begin();

        if (breadSystem.isActive()) {
            float size = 36f;

            batch.draw(
                breadSystem.getTexture(),
                breadSystem.getX() - size / 2f,
                breadSystem.getY() - size / 2f,
                size,
                size
            );
        }

        if (breadSystem.isFlying()) {
            float size = 36f;

            batch.draw(
                breadSystem.getTexture(),
                breadSystem.getFlightX() - size / 2f,
                breadSystem.getFlightY()
                    + breadSystem.getFlightHeight()
                    - size / 2f,
                size,
                size
            );
        }

        for (Goose goose : ctx.geese) {
            goose.draw(batch);
        }

        dog.draw(batch);

        batch.end();
    }
}
