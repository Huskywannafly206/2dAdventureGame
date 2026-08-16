package io.github.com.group31.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.com.group31.audio.AudioService;
import io.github.com.group31.component.Chest;
import io.github.com.group31.component.Graphic;
import io.github.com.group31.component.Respawnable;
import io.github.com.group31.save.RespawnState;
import io.github.com.group31.tiled.TiledAshleyConfigurator;

public class RespawnSystem extends EntitySystem {
    private static final ComponentMapper<Respawnable> RM = ComponentMapper.getFor(Respawnable.class);
    private static final ComponentMapper<Chest> CM = ComponentMapper.getFor(Chest.class);
    private static final ComponentMapper<Graphic> GM = ComponentMapper.getFor(Graphic.class);

    private final TiledAshleyConfigurator configurator;
    private final AudioService audioService;
    private ImmutableArray<Entity> respawnables;
    private float timer = 0f;

    public RespawnSystem(TiledAshleyConfigurator configurator, AudioService audioService) {
        this.configurator = configurator;
        this.audioService = audioService;
    }

    @Override
    public void addedToEngine(Engine engine) {
        respawnables = engine.getEntitiesFor(Family.all(Respawnable.class).get());
    }

    @Override
    public void update(float deltaTime) {
        timer += deltaTime;
        if (timer < 1f) return; // Check only once per second
        timer = 0f;

        Map<String, Long> times = RespawnState.getInstance().getEntityRespawnTimes();
        if (times.isEmpty()) return;

        Map<String, TiledMapTileMapObject> mapObjects = configurator.getCurrentMapObjects();
        List<String> toRemove = new ArrayList<>();

        for (String entityId : times.keySet()) {
            if (!RespawnState.getInstance().isRespawning(entityId)) {
                // Timer elapsed!
                // Check if it belongs to current map
                if (mapObjects.containsKey(entityId)) {
                    // It is on the current map
                    handleRespawn(entityId, mapObjects.get(entityId));
                }
                toRemove.add(entityId);
            }
        }

        // Clean up state
        for (String id : toRemove) {
            RespawnState.getInstance().clearRespawn(id);
        }
    }

    private void handleRespawn(String entityId, TiledMapTileMapObject tileMapObject) {
        // Check if it already exists in the engine (e.g., a Chest that stayed open)
        for (int i = 0; i < respawnables.size(); ++i) {
            Entity entity = respawnables.get(i);
            Respawnable respawnable = RM.get(entity);
            if (respawnable != null && entityId.equals(respawnable.getEntityId())) {
                Chest chest = CM.get(entity);
                if (chest != null) {
                    chest.setOpen(false);
                    Graphic graphic = GM.get(entity);
                    if (graphic != null && chest.getClosedRegion() != null) {
                        graphic.setRegion(chest.getClosedRegion());
                    }
                    // Optional: play sound for chest reset?
                }
                return; // Handled existing entity
            }
        }

        // Entity does not exist (it was removed), so we spawn it fresh
        configurator.onLoadObject(tileMapObject);
    }
}
