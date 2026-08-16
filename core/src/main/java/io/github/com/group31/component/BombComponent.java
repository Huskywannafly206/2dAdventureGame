package io.github.com.group31.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class BombComponent implements Component {
    public static final ComponentMapper<BombComponent> MAPPER = ComponentMapper.getFor(BombComponent.class);

    public float timeLeft = 1.0f;
}
