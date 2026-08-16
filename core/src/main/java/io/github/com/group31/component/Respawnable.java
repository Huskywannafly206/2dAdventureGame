package io.github.com.group31.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Respawnable implements Component {
    public static final ComponentMapper<Respawnable> MAPPER = ComponentMapper.getFor(Respawnable.class);

    private final String entityId;
    private final float respawnTimeSec;

    public Respawnable(String entityId, float respawnTimeSec) {
        this.entityId = entityId;
        this.respawnTimeSec = respawnTimeSec;
    }

    public String getEntityId() {
        return entityId;
    }

    public float getRespawnTimeSec() {
        return respawnTimeSec;
    }
}
