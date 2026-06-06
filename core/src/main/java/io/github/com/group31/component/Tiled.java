package io.github.com.group31.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.maps.MapObject;

public class Tiled implements Component {
    public static final ComponentMapper<Tiled> MAPPER = ComponentMapper.getFor(Tiled.class);

    private final int id;
    private final MapObject mapObjectRef;
    private int tileId = -1;

    public Tiled(MapObject mapObjectRef) {
        this(mapObjectRef, -1);
    }

    public Tiled(MapObject mapObjectRef, int tileId) {
        this.id = mapObjectRef.getProperties().get("id", -1, Integer.class);
        this.mapObjectRef = mapObjectRef;
        this.tileId = tileId;
    }

    public Tiled(int tileId) {
        this.id = -1;
        this.mapObjectRef = null;
        this.tileId = tileId;
    }

    public int getId() {
        return id;
    }

    public MapObject getMapObjectRef() {
        return mapObjectRef;
    }

    public int getTileId() {
        return tileId;
    }
}
