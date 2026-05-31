package io.github.com.quillraven.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * Marker component — entity has died and is pending removal.
 */
public class Dead implements Component {
    public static final ComponentMapper<Dead> MAPPER = ComponentMapper.getFor(Dead.class);

    private float timer;

    public Dead() {
        this.timer = 0f;
    }

    public void incTimer(float deltaTime) {
        this.timer += deltaTime;
    }

    public float getTimer() {
        return timer;
    }
}
