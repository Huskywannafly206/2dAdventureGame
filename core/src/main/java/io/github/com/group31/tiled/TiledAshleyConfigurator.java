package io.github.com.group31.tiled;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSets;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxRuntimeException;
import io.github.com.group31.GdxGame;
import io.github.com.group31.asset.AssetService;
import io.github.com.group31.asset.AtlasAsset;
import io.github.com.group31.asset.SoundAsset;
import io.github.com.group31.component.Ai;
import io.github.com.group31.component.Animation2D;
import io.github.com.group31.component.Animation2D.AnimationType;
import io.github.com.group31.component.Attack;
import io.github.com.group31.component.CameraFollow;
import io.github.com.group31.component.Controller;
import io.github.com.group31.component.Experience;
import io.github.com.group31.component.Facing;
import io.github.com.group31.component.Facing.FacingDirection;
import io.github.com.group31.component.Fsm;
import io.github.com.group31.component.Graphic;
import io.github.com.group31.component.Life;
import io.github.com.group31.component.Move;
import io.github.com.group31.component.Physic;
import io.github.com.group31.component.Player;
import io.github.com.group31.component.Tiled;
import io.github.com.group31.component.Transform;
import io.github.com.group31.component.Trigger;
import io.github.com.group31.component.Inventory;
import io.github.com.group31.component.Item;

public class TiledAshleyConfigurator {
    private static final Vector2 DEFAULT_PHYSIC_SCALING = new Vector2(1f, 1f);

    private final Engine engine;
    private final World physicWorld;
    private final MapObjects tmpMapObjects;
    private final Vector2 tmpVec2;
    private final AssetService assetService;
    private TiledMap currentMap;

    public TiledAshleyConfigurator(Engine engine, World physicWorld, AssetService assetService) {
        this.engine = engine;
        this.physicWorld = physicWorld;
        this.tmpMapObjects = new MapObjects();
        this.tmpVec2 = new Vector2();
        this.assetService = assetService;
    }

    public void setCurrentMap(TiledMap map) {
        this.currentMap = map;
    }

    public void onLoadTile(TiledMapTile tile, float x, float y) {
        createBody(tile.getObjects(),
            new Vector2(x, y),
            DEFAULT_PHYSIC_SCALING,
            BodyDef.BodyType.StaticBody,
            Vector2.Zero,
            "environment");
    }

    public void onLoadTrigger(String triggerName, MapObject mapObject) {
        if (mapObject instanceof RectangleMapObject rectMapObj) {
            // Spawn point: tên bắt đầu bằng "spawn_" → tạo Trigger entity đại diện cho điểm spawn (không tạo physics body)
            if (triggerName.startsWith("spawn_")) {
                Entity entity = this.engine.createEntity();
                entity.add(new Trigger(triggerName, 0, rectMapObj));
                entity.add(new Tiled(rectMapObj));
                this.engine.addEntity(entity);
                return;
            }

            Entity entity = this.engine.createEntity();
            Rectangle rect = rectMapObj.getRectangle();
            addEntityTransform(
                rect.getX(), rect.getY(), 0,
                rect.getWidth(), rect.getHeight(),
                1f, 1f,
                0,
                entity);
            addEntityPhysic(
                rectMapObj,
                BodyDef.BodyType.StaticBody,
                tmpVec2.set(rect.getX(), rect.getY()).scl(GdxGame.UNIT_SCALE),
                entity);
            int targetId = rectMapObj.getProperties().get("targetId", 0, Integer.class);
            entity.add(new Trigger(triggerName, targetId, rectMapObj));
            entity.add(new Tiled(rectMapObj));
            this.engine.addEntity(entity);
        } else {
            throw new GdxRuntimeException("Unsupported map object type for trigger: " + mapObject.getClass().getSimpleName());
        }
    }

    /**
     * Creates and configures an entity from a Tiled map object with all necessary components.
     */
    public void onLoadObject(TiledMapTileMapObject tileMapObject) {
        Entity entity = this.engine.createEntity();
        TiledMapTile tile = tileMapObject.getTile();
        TextureRegion textureRegion = getTextureRegion(tile);
        float sortOffsetY = tile.getProperties().get("sortOffsetY", 0, Integer.class);
        sortOffsetY *= GdxGame.UNIT_SCALE;
        int z = tile.getProperties().get("z", 1, Integer.class);

        addEntityTransform(
            tileMapObject.getX(), tileMapObject.getY(), z,
            textureRegion.getRegionWidth(), textureRegion.getRegionHeight(),
            tileMapObject.getScaleX(), tileMapObject.getScaleY(),
            sortOffsetY,
            entity);

        // ── Rẽ nhánh riêng cho Item tile (không cần Fsm / Facing / Move / AI) ──
        String itemTypeStr = tile.getProperties().get("itemType", null, String.class);
        if (itemTypeStr != null && !itemTypeStr.isBlank()) {
            BodyType bodyType = BodyType.StaticBody; // item không di chuyển
            addEntityPhysic(tile.getObjects(), bodyType, Vector2.Zero, entity);
            addEntityItem(tile, entity);
            entity.add(new Graphic(textureRegion, Color.WHITE.cpy()));
            entity.add(new Tiled(tileMapObject));
            this.engine.addEntity(entity);
            return;
        }

        BodyType bodyType = getObjectBodyType(tile);
        addEntityPhysic(
            tile.getObjects(),
            bodyType,
            Vector2.Zero,
            entity);
        addEntityAnimation(tile, entity);
        addEntityMove(tile, entity);
        addEntityController(tileMapObject, entity);
        addEntityCameraFollow(tileMapObject, entity);
        addEntityLife(tile, entity);
        addEntityPlayer(tileMapObject, entity);
        addEntityAttack(tile, entity);
        addEntityAi(tile, entity);
        addEntityExperience(tile, entity);
        entity.add(new Facing(FacingDirection.DOWN));
        entity.add(new Fsm(entity));
        entity.add(new Graphic(textureRegion, Color.WHITE.cpy()));
        entity.add(new Tiled(tileMapObject));

        this.engine.addEntity(entity);
    }

    private BodyType getObjectBodyType(TiledMapTile tile) {
        String classType = tile.getProperties().get("type", "", String.class);
        if ("Prop".equals(classType)) {
            return BodyType.StaticBody;
        }

        String bodyTypeStr = tile.getProperties().get("bodyType", "DynamicBody", String.class);
        return BodyType.valueOf(bodyTypeStr);
    }

    private void addEntityAttack(TiledMapTile tile, Entity entity) {
        float damage = tile.getProperties().get("damage", 0f, Float.class);
        if (damage == 0f) return;

        float damageDelay = tile.getProperties().get("damageDelay", 0f, Float.class);
        String soundAssetStr = tile.getProperties().get("attackSound", String.class);
        SoundAsset soundAsset = null;
        if (soundAssetStr != null) {
            soundAsset = SoundAsset.valueOf(soundAssetStr);
        }
        entity.add(new Attack(damage, damageDelay, soundAsset));
    }

    /**
     * Adds AI component to non-player entities that have Move + Life.
     * Detects AI if the tile's "type" property is "mob".
     */
    private void addEntityAi(TiledMapTile tile, Entity entity) {
        // Skip player and entities with no movement capability
        if (Player.MAPPER.has(entity)) return;
        if (Move.MAPPER.get(entity) == null) return;
        if (Life.MAPPER.get(entity) == null) return;

        String classType = tile.getProperties().get("type", "", String.class);
        if (!"mob".equalsIgnoreCase(classType)) return;

        float sightRange = tile.getProperties().get("sightRange", 3f, Float.class);
        float attackRange = tile.getProperties().get("attackRange", 0.8f, Float.class);
        entity.add(new Ai(sightRange, attackRange));
    }

    /**
     * Adds Experience component to entities that give XP when killed.
     * Reads xpReward from tile properties (default: 0 = no XP).
     */
    private void addEntityExperience(TiledMapTile tile, Entity entity) {
        float xpReward = tile.getProperties().get("xpReward", 0f, Float.class);
        if (xpReward <= 0f) return;
        entity.add(new Experience(xpReward));
    }

    private void addEntityPlayer(TiledMapTileMapObject tileMapObject, Entity entity) {
        if ("Player".equals(tileMapObject.getName())) {
            entity.add(new Player());
            entity.add(new Experience(0f, 1, 100f));
            entity.add(new Inventory());
        }
    }

    /**
     * Nếu tile có property "itemType", tạo và gắn component Item tương ứng.
     * Item entity sử dụng sensor fixture nên không cần các component AI, Move, Life.
     */
    private void addEntityItem(TiledMapTile tile, Entity entity) {
        String itemTypeStr = tile.getProperties().get("itemType", null, String.class);
        if (itemTypeStr == null || itemTypeStr.isBlank()) return;

        Item.Type type;
        try {
            type = Item.Type.valueOf(itemTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            com.badlogic.gdx.Gdx.app.error("TiledAshleyConfigurator", "Unknown itemType: " + itemTypeStr);
            return;
        }

        SoundAsset sound = switch (type) {
            case COIN          -> SoundAsset.COIN;
            case POTION_HEALTH -> SoundAsset.PICKUP;
            case KEY           -> SoundAsset.PICKUP;
        };

        entity.add(new Item(type, 1f, sound));
    }

    private void addEntityLife(TiledMapTile tile, Entity entity) {
        int life = tile.getProperties().get("life", 0, Integer.class);
        if (life == 0) return;

        float lifeReg = tile.getProperties().get("lifeReg", 0f, Float.class);
        entity.add(new Life(life, lifeReg));
    }

    private TextureRegion getTextureRegion(TiledMapTile tile) {
        String atlasAssetStr = tile.getProperties().get("atlasAsset", "OBJECTS", String.class);
        AtlasAsset atlasAsset = AtlasAsset.valueOf(atlasAssetStr);
        FileTextureData textureData = (FileTextureData) tile.getTextureRegion().getTexture().getTextureData();
        String atlasKey = textureData.getFileHandle().nameWithoutExtension();
        TextureAtlas textureAtlas = assetService.get(atlasAsset);
        TextureAtlas.AtlasRegion region = textureAtlas.findRegion(atlasKey + "/" + atlasKey);
        if (region != null) {
            return region;
        }

        // Region not part of an atlas, or the object has an animation.
        // If it has an animation, then its region is updated in the AnimationSystem.
        // If it has no region, then we render the region of the Tiled editor to show something, but
        // that will add one render call due to texture swapping.
        return tile.getTextureRegion();
    }

    private void addEntityCameraFollow(TiledMapTileMapObject tileMapObject, Entity entity) {
        boolean cameraFollow = tileMapObject.getProperties().get("camFollow", false, Boolean.class);
        if (!cameraFollow) return;

        entity.add(new CameraFollow());
    }

    private void addEntityController(TiledMapTileMapObject tileMapObject, Entity entity) {
        boolean controller = tileMapObject.getProperties().get("controller", false, Boolean.class);
        if (!controller) return;

        entity.add(new Controller());
    }

    private void addEntityMove(TiledMapTile tile, Entity entity) {
        float speed = tile.getProperties().get("speed", 0f, Float.class);
        if (speed == 0f) return;

        entity.add(new Move(speed));
    }

    private void addEntityAnimation(TiledMapTile tile, Entity entity) {
        String animationStr = tile.getProperties().get("animation", "", String.class);
        if (animationStr.isBlank()) {
            return;
        }
        AnimationType animationType = AnimationType.valueOf(animationStr);

        String atlasAssetStr = tile.getProperties().get("atlasAsset", "OBJECTS", String.class);
        AtlasAsset atlasAsset = AtlasAsset.valueOf(atlasAssetStr);
        FileTextureData textureData = (FileTextureData) tile.getTextureRegion().getTexture().getTextureData();
        String atlasKey = textureData.getFileHandle().nameWithoutExtension();
        float speed = tile.getProperties().get("animationSpeed", 0f, Float.class);

        entity.add(new Animation2D(atlasAsset, atlasKey, animationType, Animation.PlayMode.LOOP, speed));
    }

    private void addEntityPhysic(MapObject mapObject, @SuppressWarnings("SameParameterValue") BodyType bodyType, Vector2 relativeTo, Entity entity) {
        if (tmpMapObjects.getCount() > 0) tmpMapObjects.remove(0);

        tmpMapObjects.add(mapObject);
        addEntityPhysic(tmpMapObjects, bodyType, relativeTo, entity);
    }

    private void addEntityPhysic(MapObjects mapObjects, BodyType bodyType, Vector2 relativeTo, Entity entity) {
        if (mapObjects.getCount() == 0) return;

        Transform transform = Transform.MAPPER.get(entity);
        Body body = createBody(mapObjects,
            transform.getPosition(),
            transform.getScaling(),
            bodyType,
            relativeTo,
            entity);

        entity.add(new Physic(body, new Vector2(body.getPosition())));
    }

    private Body createBody(MapObjects mapObjects,
                            Vector2 position,
                            Vector2 scaling,
                            BodyType bodyType,
                            Vector2 relativeTo,
                            Object userData) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(position);
        bodyDef.fixedRotation = true;

        Body body = this.physicWorld.createBody(bodyDef);
        body.setUserData(userData);
        for (MapObject object : mapObjects) {
            FixtureDef fixtureDef = TiledPhysics.fixtureDefOf(object, scaling, relativeTo);
            Fixture fixture = body.createFixture(fixtureDef);
            fixture.setUserData(object.getName());
            fixtureDef.shape.dispose();
        }
        return body;
    }

    private static void addEntityTransform(
        float x, float y, int z,
        float w, float h,
        float scaleX, float scaleY,
        float sortOffsetY,
        Entity entity
    ) {
        Vector2 position = new Vector2(x, y);
        Vector2 size = new Vector2(w, h);
        Vector2 scaling = new Vector2(scaleX, scaleY);

        position.scl(GdxGame.UNIT_SCALE);
        size.scl(GdxGame.UNIT_SCALE);

        entity.add(new Transform(position, z, size, scaling, 0f, sortOffsetY));
    }

    /**
     * Tạo một mob entity tại tọa độ thế giới (worldX, worldY) dựa trên tile ID.
     * Được gọi từ SpawnSystem.
     *
     * @param mobTileId ID tile trong tileset của map hiện tại
     * @param worldX    tọa độ X thế giới (đơn vị game)
     * @param worldY    tọa độ Y thế giới (đơn vị game)
     * @return entity mới hoặc null nếu không tìm thấy tile
     */
    public Entity spawnMob(int mobTileId, float worldX, float worldY) {
        if (currentMap == null) {
            com.badlogic.gdx.Gdx.app.error("TiledAshleyConfigurator", "spawnMob: currentMap is null");
            return null;
        }

        // Tra tile theo local ID trong tileset (firstGid + localId = globalGid)
        TiledMapTileSets tileSets = currentMap.getTileSets();
        TiledMapTile tile = tileSets.getTile(mobTileId);
        if (tile == null) {
            // Thử tìm qua từng tileset với offset firstGid
            for (com.badlogic.gdx.maps.tiled.TiledMapTileSet ts : tileSets) {
                tile = ts.getTile(ts.getProperties().get("firstgid", 1, Integer.class) + mobTileId - 1);
                if (tile != null) break;
            }
        }
        if (tile == null) {
            com.badlogic.gdx.Gdx.app.error("TiledAshleyConfigurator",
                "spawnMob: cannot find tile with id=" + mobTileId);
            return null;
        }

        // Tính kích thước texture của tile
        TextureRegion textureRegion = getTextureRegion(tile);
        float sortOffsetY = tile.getProperties().get("sortOffsetY", 0, Integer.class) * GdxGame.UNIT_SCALE;
        int z = tile.getProperties().get("z", 1, Integer.class);

        // Kích thước tile theo pixel → chia UNIT_SCALE để ra world units
        float tileW = textureRegion.getRegionWidth();
        float tileH = textureRegion.getRegionHeight();

        // Tính lại pixelX/pixelY từ worldX/worldY để dùng addEntityTransform
        // (addEntityTransform sẽ nhân UNIT_SCALE vào bên trong)
        float pixelX = worldX / GdxGame.UNIT_SCALE - tileW * 0.5f;
        float pixelY = worldY / GdxGame.UNIT_SCALE - tileH * 0.5f;

        Entity entity = this.engine.createEntity();
        addEntityTransform(pixelX, pixelY, z, tileW, tileH, 1f, 1f, sortOffsetY, entity);

        BodyType bodyType = getObjectBodyType(tile);
        addEntityPhysic(tile.getObjects(), bodyType, Vector2.Zero, entity);
        addEntityAnimation(tile, entity);
        addEntityMove(tile, entity);
        addEntityLife(tile, entity);
        addEntityAttack(tile, entity);
        addEntityAi(tile, entity);
        addEntityExperience(tile, entity);
        entity.add(new Facing(FacingDirection.DOWN));
        entity.add(new Fsm(entity));
        entity.add(new Graphic(textureRegion, Color.WHITE.cpy()));

        this.engine.addEntity(entity);
        return entity;
    }

}
