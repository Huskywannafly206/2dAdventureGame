package io.github.com.group31.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;

public class Tiled implements Component {
    public static final ComponentMapper<Tiled> MAPPER = ComponentMapper.getFor(Tiled.class);

    private final int id;
    private final MapObject mapObjectRef;
    private int tileId = -1;

    public Tiled(MapObject mapObjectRef) {
        this.id = mapObjectRef.getProperties().get("id", -1, Integer.class);
        this.mapObjectRef = mapObjectRef;
        if (mapObjectRef instanceof TiledMapTileMapObject tileMapObject) {
            this.tileId = tileMapObject.getTile().getId();
        }
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
