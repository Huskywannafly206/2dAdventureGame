package io.github.com.group31.asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.maps.tiled.BaseTiledMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public enum MapAsset implements Asset<TiledMap> {
    ICEMAP1("ice_map.tmx"),
    ICEMAP2("ice_map2.tmx"),
    ICEMAP3("ice_map3.tmx"),
    DUNGEONMAP1("dungeon_map.tmx"),
    DUNGEONMAP2("dungeon_map2.tmx"),
    DUNGEONMAP3("dungeon_map3.tmx");


    private final AssetDescriptor<TiledMap> descriptor;

    MapAsset(String mapName) {
        BaseTiledMapLoader.Parameters parameters = new TmxMapLoader.Parameters();
        parameters.projectFilePath = "maps/mystic.tiled-project";
        this.descriptor = new AssetDescriptor<>("maps/" + mapName, TiledMap.class, parameters);
    }

    @Override
    public AssetDescriptor<TiledMap> getDescriptor() {
        return descriptor;
    }
}
