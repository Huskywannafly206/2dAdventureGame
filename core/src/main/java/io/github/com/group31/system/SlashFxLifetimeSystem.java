package io.github.com.group31.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import io.github.com.group31.component.*;
import java.util.ArrayList;
import java.util.List;

public class SlashFxLifetimeSystem extends IteratingSystem {
    private final List<Entity> toRemove = new ArrayList<>();

    public SlashFxLifetimeSystem() {
        super(Family.all(SlashFxLifetime.class, Graphic.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        SlashFxLifetime lt = SlashFxLifetime.MAPPER.get(entity);
        lt.elapsed += deltaTime;

        // Cập nhật frame animation
        Array<AtlasRegion> frames = lt.frames;
        if (!frames.isEmpty()) {
            int frameIndex = (int) (lt.elapsed / lt.totalTime * frames.size);
            frameIndex = Math.min(frameIndex, frames.size - 1);
            Graphic.MAPPER.get(entity).setRegion(frames.get(frameIndex));
        }

        if (lt.elapsed >= lt.totalTime) {
            toRemove.add(entity);
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        toRemove.forEach(getEngine()::removeEntity);
        toRemove.clear();
    }
}
