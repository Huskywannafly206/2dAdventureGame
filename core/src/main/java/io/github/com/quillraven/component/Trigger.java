package io.github.com.quillraven.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.maps.MapObject;

public class Trigger implements Component {
    public static final ComponentMapper<Trigger> MAPPER = ComponentMapper.getFor(Trigger.class);

    private final String name;
    private final int targetTiledId;
    private final com.badlogic.gdx.maps.MapObject mapObject;
    private Entity triggeringEntity;

    public Trigger(String name, int targetTiledId, com.badlogic.gdx.maps.MapObject mapObject) {
        this.name = name;
        this.targetTiledId = targetTiledId;
        this.mapObject = mapObject;
        this.triggeringEntity = null;
    }

    public Trigger(String name, int targetTiledId) {
//        this.name = name;
//        this.targetTiledId = targetTiledId;
//        this.triggeringEntity = null;
        this(name, targetTiledId, null);
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

    public com.badlogic.gdx.maps.MapObject getMapObject() {
        return mapObject;
    }

    public Entity getTriggeringEntity() {
        return triggeringEntity;
    }
}
