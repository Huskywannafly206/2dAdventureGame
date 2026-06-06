package io.github.com.group31.component;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

public class SlashFxLifetime implements Component {
    public static final ComponentMapper<SlashFxLifetime> MAPPER =
        ComponentMapper.getFor(SlashFxLifetime.class);

    public final Array<AtlasRegion> frames;
    public final float totalTime;
    public float elapsed;

    public SlashFxLifetime(Array<AtlasRegion> frames, float totalTime) {
        this.frames = frames;
        this.totalTime = totalTime;
        this.elapsed = 0f;
    }
}
