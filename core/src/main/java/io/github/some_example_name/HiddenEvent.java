package io.github.some_example_name;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface HiddenEvent {

    void update(float delta, GameContext ctx);

    void reset(GameContext ctx);

    default void drawShapes(ShapeRenderer shapes) {}
    default void drawSprites(SpriteBatch batch) {}
    default String getStatusText() { return null; }
    default void dispose() {}

}
