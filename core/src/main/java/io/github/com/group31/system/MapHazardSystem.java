package io.github.com.group31.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import io.github.com.group31.GdxGame;
import io.github.com.group31.asset.SoundAsset;
import io.github.com.group31.audio.AudioService;
import io.github.com.group31.component.Damaged;
import io.github.com.group31.component.Life;
import io.github.com.group31.component.Player;
import io.github.com.group31.component.Transform;
import io.github.com.group31.tiled.TiledService;

public class MapHazardSystem extends IteratingSystem {
    private final TiledService tiledService;
    private final AudioService audioService;
    private float hazardCooldown = 0f;

    public MapHazardSystem(TiledService tiledService, AudioService audioService) {
        super(Family.all(Player.class, Transform.class, Life.class).get());
        this.tiledService = tiledService;
        this.audioService = audioService;
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        if (hazardCooldown > 0f) {
            hazardCooldown -= deltaTime;
        }

        if (hazardCooldown > 0f) {
            return;
        }

        Transform transform = Transform.MAPPER.get(player);
        TiledMap map = tiledService.getCurrentMap();
        if (map == null) return;

        // Player's feet position: center bottom
        float pixelX = (transform.getPosition().x + transform.getSize().x * 0.5f) / GdxGame.UNIT_SCALE;
        float pixelY = transform.getPosition().y / GdxGame.UNIT_SCALE;

        for (MapLayer layer : map.getLayers()) {
            if (layer instanceof TiledMapTileLayer) {
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
                int tileX = (int) (pixelX / tileLayer.getTileWidth());
                int tileY = (int) (pixelY / tileLayer.getTileHeight());

                TiledMapTileLayer.Cell cell = tileLayer.getCell(tileX, tileY);
                if (cell != null) {
                    TiledMapTile tile = cell.getTile();
                    if (tile instanceof AnimatedTiledMapTile) {
                        AnimatedTiledMapTile animatedTile = (AnimatedTiledMapTile) tile;
                        TiledMapTile currentFrame = animatedTile.getCurrentFrame();
                        if (currentFrame.getProperties().containsKey("damage")) {
                            float damage = currentFrame.getProperties().get("damage", 1f, Float.class);
                            if (damage > 0f) {
                                player.add(new Damaged(damage, null));
                                audioService.playSound(SoundAsset.TRAP);
                                hazardCooldown = 1.0f;
                                return; // Apply damage once per tick
                            }
                        }
                    }
                }
            }
        }
    }
}
