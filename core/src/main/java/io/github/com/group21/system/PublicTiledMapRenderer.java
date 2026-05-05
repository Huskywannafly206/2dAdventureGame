package io.github.com.group21.system;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class PublicTiledMapRenderer extends OrthogonalTiledMapRenderer {
    public PublicTiledMapRenderer(TiledMap map, float unitScale, Batch batch) {
        super(map, unitScale, batch);
    }

    @Override
    public void renderMapLayer(MapLayer layer) {
        super.renderMapLayer(layer);
    }
}
