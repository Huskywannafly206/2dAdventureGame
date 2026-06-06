package io.github.com.group31.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.com.group31.GdxGame;
import io.github.com.group31.component.Dead;
import io.github.com.group31.component.Graphic;
import io.github.com.group31.component.Life;
import io.github.com.group31.component.Player;
import io.github.com.group31.component.Transform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RenderSystem extends SortedIteratingSystem implements Disposable {
    private final Batch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final WeaponHandSystem weaponHandSystem;

    private final PublicTiledMapRenderer tiledRenderer;
    private final List<MapLayer> fgdLayers;
    private final List<MapLayer> bgdLayers;

    private final Texture whiteTexture;
    private final TextureRegion whiteRegion;

    public RenderSystem(Batch batch, Viewport viewport, OrthographicCamera camera, WeaponHandSystem weaponHandSystem) {
        super(
            Family.all(Transform.class, Graphic.class).get(),
            Comparator.comparing(Transform.MAPPER::get)
        );

        this.batch = batch;
        this.viewport = viewport;
        this.camera = camera;
        this.weaponHandSystem = weaponHandSystem;
        this.tiledRenderer = new PublicTiledMapRenderer(null, GdxGame.UNIT_SCALE, batch);
        this.fgdLayers = new ArrayList<>();
        this.bgdLayers = new ArrayList<>();

        // Generate 1x1 white texture for health bar drawing
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.whiteTexture = new Texture(pixmap);
        this.whiteRegion = new TextureRegion(whiteTexture);
        pixmap.dispose();
    }

    /**
     * Renders the scene with background, entities, and foreground layers.
     */
    @Override
    public void update(float deltaTime) {
        AnimatedTiledMapTile.updateAnimationBaseTime();
        viewport.apply();

        batch.begin();
        batch.setColor(Color.WHITE);
        this.tiledRenderer.setView(camera);
        bgdLayers.forEach(layer -> tiledRenderer.renderMapLayer(layer));

        forceSort();
        super.update(deltaTime);

        batch.setColor(Color.WHITE);
        fgdLayers.forEach(layer -> tiledRenderer.renderMapLayer(layer));
        batch.end();
    }

    /**
     * Renders a single entity with its transform and graphic components.
     */
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Transform transform = Transform.MAPPER.get(entity);
        Graphic graphic = Graphic.MAPPER.get(entity);
        if (graphic.getRegion() == null) {
            return;
        }

        Vector2 position = transform.getPosition();
        Vector2 scaling = transform.getScaling();
        Vector2 size = transform.getSize();
        
        TextureRegion region = graphic.getRegion();
        float width = region.getRegionWidth() * GdxGame.UNIT_SCALE;
        float height = region.getRegionHeight() * GdxGame.UNIT_SCALE;

        batch.setColor(graphic.getColor());
        batch.draw(
            region,
            position.x - (1f - scaling.x) * width * 0.5f,
            position.y - (1f - scaling.y) * height * 0.5f,
            width * 0.5f, height * 0.5f,
            width, height,
            scaling.x, scaling.y,
            transform.getRotationDeg()
        );

        if (weaponHandSystem != null) {
            weaponHandSystem.drawWeapon(entity, deltaTime);
        }

        // Draw health bar for non-player entities with Life component that are not dead
        Life life = Life.MAPPER.get(entity);
        if (life != null && !Player.MAPPER.has(entity) && !Dead.MAPPER.has(entity)) {
            float barW = size.x * 0.8f;
            float barH = 0.05f;
            float barX = position.x + (size.x - barW) * 0.5f;
            float barY = position.y + size.y + 0.04f;

            // Background (black border)
            batch.setColor(Color.BLACK);
            batch.draw(whiteRegion, barX - 0.01f, barY - 0.01f, barW + 0.02f, barH + 0.02f);

            // Dark background inside
            batch.setColor(Color.DARK_GRAY);
            batch.draw(whiteRegion, barX, barY, barW, barH);

            // Red foreground health bar
            float ratio = Math.max(0f, Math.min(1f, life.getLife() / life.getMaxLife()));
            batch.setColor(Color.RED);
            batch.draw(whiteRegion, barX, barY, barW * ratio, barH);
        }
    }

    /**
     * Sets up the map and organizes layers into background and foreground.
     */
    public void setMap(TiledMap tiledMap) {
        this.tiledRenderer.setMap(tiledMap);

        this.fgdLayers.clear();
        this.bgdLayers.clear();
        List<MapLayer> currentLayers = bgdLayers;
        for (MapLayer layer : tiledMap.getLayers()) {
            if ("objects".equals(layer.getName())) {
                currentLayers = fgdLayers;
                continue;
            }
            if (layer.getClass().equals(MapLayer.class)) {
                continue;
            }
            currentLayers.add(layer);
        }
    }

    @Override
    public void dispose() {
        this.tiledRenderer.dispose();
        if (this.whiteTexture != null) {
            this.whiteTexture.dispose();
        }
    }
}
