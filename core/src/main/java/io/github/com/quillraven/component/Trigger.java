package io.github.com.quillraven.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.maps.MapObject;

public class Trigger implements Component {
    public static final ComponentMapper<Trigger> MAPPER = ComponentMapper.getFor(Trigger.class);

    private final MapObject mabObject;

    private final String name;
    private final int targetTiledId;
    private Entity triggeringEntity;

    public Trigger(String name, int targetTiledId) {
        this.name = name;
        this.targetTiledId = targetTiledId;
        this.triggeringEntity = null;
    }

    public String getName() {
        return name;
    }

    public int getTargetTiledId() {
        return targetTiledId;
    }

    public void setTriggeringEntity(Entity triggeringEntity) {
        this.triggeringEntity = triggeringEntity;
    }

    public Entity getTriggeringEntity() {
        return triggeringEntity;
    }
}
