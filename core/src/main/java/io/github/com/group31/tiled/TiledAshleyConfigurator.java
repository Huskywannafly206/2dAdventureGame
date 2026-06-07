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
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.GdxRuntimeException;
import io.github.com.group31.GdxGame;
import io.github.com.group31.asset.AssetService;
import io.github.com.group31.asset.AtlasAsset;
import io.github.com.group31.asset.MapAsset;
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
import io.github.com.group31.component.Npc;
import io.github.com.group31.component.Physic;
import io.github.com.group31.component.Player;
import io.github.com.group31.component.Door;
import io.github.com.group31.component.Tiled;
import io.github.com.group31.component.Transform;
import io.github.com.group31.component.Trigger;
import io.github.com.group31.component.CombatState;
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
            rectMapObj.getProperties().put("sensor", true);
            addEntityTransform(
                rect.getX(), rect.getY(), 0,
                rect.getWidth(), rect.getHeight(),
                1f, 1f,
                0f,
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
        boolean hiddenBarrier = tile.getProperties().get("hiddenBarrier", false, Boolean.class);

        addEntityTransform(
            tileMapObject.getX(), tileMapObject.getY(), z,
            textureRegion.getRegionWidth(), textureRegion.getRegionHeight(),
            tileMapObject.getScaleX(), tileMapObject.getScaleY(),
            -tileMapObject.getRotation(), // LibGDX RenderSystem rotation is CCW, Tiled is CW
            sortOffsetY,
            entity);

        // ── Rẽ nhánh riêng cho Item tile (không cần Fsm / Facing / Move / AI) ──
        String itemTypeStr = tile.getProperties().get("itemType", null, String.class);
        if (itemTypeStr != null && !itemTypeStr.isBlank()) {
            BodyType bodyType = BodyType.StaticBody; // item không di chuyển
            addEntityPhysic(tile.getObjects(), bodyType, Vector2.Zero, entity);
            if (itemTypeStr.startsWith("WEAPON_")) {
                entity.add(new Npc(itemTypeStr, new String[0]));
            } else {
                addEntityItem(tile, entity);
            }
            entity.add(new Graphic(textureRegion, Color.WHITE.cpy()));
            entity.add(new Tiled(tileMapObject, getLocalTileId(tileMapObject.getTile())));
            this.engine.addEntity(entity);
            return;
        }

        // ── Rẽ nhánh riêng cho NPC tile ──
        String npcName = tileMapObject.getProperties().get("npcName", null, String.class);
        if (npcName == null) {
            npcName = tile.getProperties().get("npcName", null, String.class);
        }
        if (npcName != null && !npcName.isBlank()) {
            String dialogueStr = tileMapObject.getProperties().get("dialogue", "", String.class);
            if (dialogueStr.isBlank()) {
                dialogueStr = tile.getProperties().get("dialogue", "...", String.class);
            }
            String[] dialogue = dialogueStr.split("\\|");

            // Đọc đường dẫn faceset: ưu tiên object property, fallback sang tile property
            String facesetPath = tileMapObject.getProperties().get("faceset", null, String.class);
            if (facesetPath == null || facesetPath.isBlank()) {
                facesetPath = tile.getProperties().get("faceset", null, String.class);
            }
            if (facesetPath != null && facesetPath.isBlank()) {
                facesetPath = null; // chuẩn hóa chuỗi rỗng → null
            }

            entity.add(new Npc(npcName, dialogue, facesetPath));
            entity.add(new Facing(FacingDirection.DOWN));
            entity.add(new Graphic(textureRegion, Color.WHITE.cpy()));
            entity.add(new Tiled(tileMapObject, getLocalTileId(tileMapObject.getTile())));
            addEntityPhysic(tile.getObjects(), BodyType.StaticBody, Vector2.Zero, entity);
            addEntityAnimation(tile, entity);

            this.engine.addEntity(entity);
            return;
        }

        BodyType bodyType = getObjectBodyType(tile);
        addEntityPhysic(
            tile.getObjects(),
            bodyType,
            Vector2.Zero,
            entity,
            hiddenBarrier);
        addEntityAnimation(tile, entity);
        addEntityMove(tile, entity);
        addEntityController(tileMapObject, entity);
        addEntityCameraFollow(tileMapObject, entity);
        addEntityLife(tile, entity);
        addEntityPlayer(tileMapObject, entity);
        addEntityAttack(tile, entity);
        addEntityAi(tile, entity);
        addEntityExperience(tile, entity);
        addEntityDoor(tile, tileMapObject, entity);
        addEntityChest(tile, tileMapObject, entity);
        entity.add(new Facing(FacingDirection.DOWN));
        entity.add(new Fsm(entity));
        Color graphicColor = Color.WHITE.cpy();
        if (hiddenBarrier) {
            graphicColor.a = 0f;
        }
        entity.add(new Graphic(textureRegion, graphicColor));
        int localTileId = getLocalTileId(tileMapObject.getTile());
        if (localTileId == 15 || localTileId == 16) {
            com.badlogic.gdx.Gdx.app.log("TiledAshleyConfigurator", "onLoadObject: loaded tile " + localTileId + " (GID=" + tileMapObject.getTile().getId() + ")");
        }
        entity.add(new Tiled(tileMapObject, localTileId));

        this.engine.addEntity(entity);
    }

    private void addEntityDoor(TiledMapTile tile, TiledMapTileMapObject tileMapObject, Entity entity) {
        String classType = tileMapObject.getProperties().get("type", "", String.class);
        if (classType.isBlank()) {
            classType = tile.getProperties().get("type", "", String.class);
        }
        if (!"door".equals(classType)) return;

        int openTileLocalId = tileMapObject.getProperties().get("openTileId", -1, Integer.class);
        if (openTileLocalId == -1) {
            openTileLocalId = tile.getProperties().get("openTileId", -1, Integer.class);
        }

        float openRotation = tileMapObject.getProperties().get("openRotation", 0f, Float.class);
        if (openRotation == 0f) {
            openRotation = tile.getProperties().get("openRotation", 0f, Float.class);
        }

        if (openTileLocalId != -1) {
            com.badlogic.gdx.maps.tiled.TiledMapTileSet tileset = null;
            int firstGid = -1;
            for (com.badlogic.gdx.maps.tiled.TiledMapTileSet ts : currentMap.getTileSets()) {
                if (ts.getTile(tile.getId()) != null) {
                    tileset = ts;
                    firstGid = ts.getProperties().get("firstgid", 1, Integer.class);
                    break;
                }
            }

            if (tileset != null) {
                TiledMapTile openTile = tileset.getTile(firstGid + openTileLocalId);
                if (openTile != null) {
                    entity.add(new Door(getTextureRegion(tile), getTextureRegion(openTile), openRotation));
                } else {
                    com.badlogic.gdx.Gdx.app.error("TiledAshleyConfigurator", "Door openTileId " + openTileLocalId + " not found in tileset!");
                }
            }
        } else {
             com.badlogic.gdx.Gdx.app.error("TiledAshleyConfigurator", "Door tile missing openTileId property!");
        }
    }

    private void addEntityChest(TiledMapTile tile, TiledMapTileMapObject tileMapObject, Entity entity) {
        String classType = tileMapObject.getProperties().get("type", "", String.class);
        if (classType.isBlank()) {
            classType = tile.getProperties().get("type", "", String.class);
        }
        if (!"chest".equals(classType)) return;

        int openTileLocalId = tileMapObject.getProperties().get("openTileId", -1, Integer.class);
        if (openTileLocalId == -1) {
            openTileLocalId = tile.getProperties().get("openTileId", -1, Integer.class);
        }

        String lootType = tileMapObject.getProperties().get("lootType", "COIN", String.class);
        if (lootType.equals("COIN")) {
            lootType = tile.getProperties().get("lootType", "COIN", String.class);
        }

        float trapDamage = 2f;
        Object trapDmgObj = tileMapObject.getProperties().get("trapDamage");
        if (trapDmgObj == null) trapDmgObj = tile.getProperties().get("trapDamage");
        if (trapDmgObj != null) {
            try { trapDamage = Float.parseFloat(trapDmgObj.toString()); } catch (Exception ignored) {}
        }

        if (openTileLocalId != -1) {
            com.badlogic.gdx.maps.tiled.TiledMapTileSet tileset = null;
            int firstGid = -1;
            for (com.badlogic.gdx.maps.tiled.TiledMapTileSet ts : currentMap.getTileSets()) {
                if (ts.getTile(tile.getId()) != null) {
                    tileset = ts;
                    firstGid = ts.getProperties().get("firstgid", 1, Integer.class);
                    break;
                }
            }

            if (tileset != null) {
                TiledMapTile openTile = tileset.getTile(firstGid + openTileLocalId);
                if (openTile != null) {
                    entity.add(new io.github.com.group31.component.Chest(getTextureRegion(tile), getTextureRegion(openTile), lootType, trapDamage));
                } else {
                    com.badlogic.gdx.Gdx.app.error("TiledAshleyConfigurator", "Chest openTileId " + openTileLocalId + " not found in tileset!");
                }
            }
        } else {
             com.badlogic.gdx.Gdx.app.error("TiledAshleyConfigurator", "Chest tile missing openTileId property!");
        }
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
            entity.add(new CombatState());

            // Spawn test weapons near player for testing combat system
            Transform transform = Transform.MAPPER.get(entity);
            if (transform != null) {
                float px = transform.getPosition().x;
                float py = transform.getPosition().y;
                
                MapAsset asset = currentMap != null ? currentMap.getProperties().get("mapAsset", MapAsset.class) : null;
                if (asset == MapAsset.VILLAGE) {
                    spawnTestWeapon(Item.Type.WEAPON_SWORD, px + 1f, py, "weapon_sword/weapon_sword");
                    spawnTestWeapon(Item.Type.WEAPON_BOW, px + 2f, py, "weapon_bow/weapon_bow");
                    spawnTestWeapon(Item.Type.WEAPON_MAGIC_WAND, px + 3f, py, "weapon_magicWand/weapon_magicWand");
                } else if (asset == MapAsset.VILLAGE_HOUSE) {
                    spawnPotion(px + 2f, py);
                }
            }
        }
    }

    private void spawnPotion(float x, float y) {
        Entity itemEntity = this.engine.createEntity();

        // 1. Transform
        float size = 0.5f;
        Transform transform = new Transform(
            new Vector2(x, y),
            1,
            new Vector2(size, size),
            new Vector2(1f, 1f),
            0f,
            0f
        );
        itemEntity.add(transform);

        // 2. Graphic
        TextureAtlas atlas = assetService.get(AtlasAsset.OBJECTS);
        TextureRegion region = atlas.findRegion("potion_health/potion_health");
        if (region != null) {
            itemEntity.add(new Graphic(region, Color.WHITE.cpy()));
        } else {
            com.badlogic.gdx.Gdx.app.error("TiledAshleyConfigurator", "Failed to find atlas region: potion_health/potion_health");
        }

        // 3. Physic Body (StaticBody sensor)
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x + size * 0.5f, y + size * 0.5f);
        Body body = this.physicWorld.createBody(bodyDef);
        body.setUserData(itemEntity);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(size * 0.5f, size * 0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        body.createFixture(fixtureDef);
        shape.dispose();

        itemEntity.add(new Physic(body, new Vector2(body.getPosition())));

        // 4. Item component
        itemEntity.add(new Item(Item.Type.POTION_HEALTH, 1f, SoundAsset.PICKUP));

        this.engine.addEntity(itemEntity);
    }

    public void spawnRustySword(float x, float y) {
        spawnTestWeapon(Item.Type.WEAPON_RUSTY_SWORD, x, y, "weapon_rusty_sword/weapon_rusty_sword");
    }

    public void spawnJungleMapKey(float x, float y) {
        Entity itemEntity = this.engine.createEntity();

        float size = 0.5f;
        Transform transform = new Transform(
            new Vector2(x, y),
            1,
            new Vector2(size, size),
            new Vector2(1f, 1f),
            0f,
            0f
        );
        itemEntity.add(transform);

        TextureAtlas atlas = assetService.get(AtlasAsset.OBJECTS);
        TextureRegion region = atlas.findRegion("jungle_map_key/jungle_map_key");
        if (region != null) {
            itemEntity.add(new Graphic(region, Color.WHITE.cpy()));
        } else {
            com.badlogic.gdx.Gdx.app.error("TiledAshleyConfigurator", "Failed to find atlas region: jungle_map_key/jungle_map_key");
        }

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x + size * 0.5f, y + size * 0.5f);
        Body body = this.physicWorld.createBody(bodyDef);
        body.setUserData(itemEntity);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(size * 0.5f, size * 0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        body.createFixture(fixtureDef);
        shape.dispose();

        itemEntity.add(new Physic(body, new Vector2(body.getPosition())));
        itemEntity.add(new Item(Item.Type.JUNGLE_MAP_KEY, 1f, SoundAsset.PICKUP));

        this.engine.addEntity(itemEntity);
    }

    private void spawnTestWeapon(Item.Type type, float x, float y, String atlasRegionName) {
        Entity itemEntity = this.engine.createEntity();

        // 1. Transform
        float size = 0.5f;
        Transform transform = new Transform(
            new Vector2(x, y),
            1,
            new Vector2(size, size),
            new Vector2(1f, 1f),
            0f,
            0f
        );
        itemEntity.add(transform);

        // 2. Graphic
        TextureAtlas atlas = assetService.get(AtlasAsset.OBJECTS);
        TextureRegion region = atlas.findRegion(atlasRegionName);
        if (region != null) {
            itemEntity.add(new Graphic(region, Color.WHITE.cpy()));
        } else {
            com.badlogic.gdx.Gdx.app.error("TiledAshleyConfigurator", "Failed to find atlas region: " + atlasRegionName);
        }

        // 3. Physic Body (StaticBody sensor)
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x + size * 0.5f, y + size * 0.5f);
        Body body = this.physicWorld.createBody(bodyDef);
        body.setUserData(itemEntity);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(size * 0.5f, size * 0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        body.createFixture(fixtureDef);
        shape.dispose();

        itemEntity.add(new Physic(body, new Vector2(body.getPosition())));

        // 4. Npc component (để nhặt bằng nút E thay vì tự động chạm nhặt)
        itemEntity.add(new Npc(type.name(), new String[0]));

        this.engine.addEntity(itemEntity);
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
            case GOLD_KEY      -> SoundAsset.PICKUP;
            case SILVER_KEY    -> SoundAsset.PICKUP;
            case SOOTHING_HERB -> SoundAsset.PICKUP;
            case WEAPON_SWORD  -> SoundAsset.PICKUP;
            case WEAPON_BOW    -> SoundAsset.PICKUP;
            case WEAPON_MAGIC_WAND -> SoundAsset.PICKUP;
            case WEAPON_RUSTY_SWORD -> SoundAsset.PICKUP;
            case JUNGLE_MAP_KEY -> SoundAsset.PICKUP;
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
        addEntityPhysic(mapObject, bodyType, relativeTo, entity, false);
    }

    private void addEntityPhysic(MapObject mapObject, @SuppressWarnings("SameParameterValue") BodyType bodyType, Vector2 relativeTo, Entity entity, boolean isSensor) {
        if (tmpMapObjects.getCount() > 0) tmpMapObjects.remove(0);

        tmpMapObjects.add(mapObject);
        addEntityPhysic(tmpMapObjects, bodyType, relativeTo, entity, isSensor);
    }

    private void addEntityPhysic(MapObjects mapObjects, BodyType bodyType, Vector2 relativeTo, Entity entity) {
        addEntityPhysic(mapObjects, bodyType, relativeTo, entity, false);
    }

    private void addEntityPhysic(MapObjects mapObjects, BodyType bodyType, Vector2 relativeTo, Entity entity, boolean isSensor) {
        if (mapObjects.getCount() == 0) return;

        Transform transform = Transform.MAPPER.get(entity);
        Body body = createBody(mapObjects,
            transform.getPosition(),
            transform.getScaling(),
            bodyType,
            relativeTo,
            entity,
            isSensor);

        entity.add(new Physic(body, new Vector2(body.getPosition())));
    }

    private Body createBody(MapObjects mapObjects,
                            Vector2 position,
                            Vector2 scaling,
                            BodyType bodyType,
                            Vector2 relativeTo,
                            Object userData) {
        return createBody(mapObjects, position, scaling, bodyType, relativeTo, userData, false);
    }

    private Body createBody(MapObjects mapObjects,
                            Vector2 position,
                            Vector2 scaling,
                            BodyType bodyType,
                            Vector2 relativeTo,
                            Object userData,
                            boolean isSensor) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(position);
        bodyDef.fixedRotation = true;

        Body body = this.physicWorld.createBody(bodyDef);
        body.setUserData(userData);
        for (MapObject object : mapObjects) {
            FixtureDef fixtureDef = TiledPhysics.fixtureDefOf(object, scaling, relativeTo);
            if (isSensor) {
                fixtureDef.isSensor = true;
            }
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
        float rotation,
        float sortOffsetY,
        Entity entity
    ) {
        Vector2 position = new Vector2(x, y);
        Vector2 size = new Vector2(w, h);
        Vector2 scaling = new Vector2(scaleX, scaleY);

        position.scl(GdxGame.UNIT_SCALE);
        size.scl(GdxGame.UNIT_SCALE);

        entity.add(new Transform(position, z, size, scaling, rotation, sortOffsetY));
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
        addEntityTransform(pixelX, pixelY, z, tileW, tileH, 1f, 1f, 0f, sortOffsetY, entity);

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
        entity.add(new Tiled(mobTileId));

        this.engine.addEntity(entity);
        return entity;
    }

    private int getLocalTileId(TiledMapTile tile) {
        if (tile == null || currentMap == null) return -1;
        int gid = tile.getId();
        TiledMapTileSets tileSets = currentMap.getTileSets();
        com.badlogic.gdx.maps.tiled.TiledMapTileSet matchingTileset = null;
        int maxFirstGid = -1;

        for (com.badlogic.gdx.maps.tiled.TiledMapTileSet ts : tileSets) {
            int firstgid = ts.getProperties().get("firstgid", 1, Integer.class);
            if (gid >= firstgid && firstgid > maxFirstGid) {
                maxFirstGid = firstgid;
                matchingTileset = ts;
            }
        }

        if (matchingTileset != null) {
            return gid - maxFirstGid;
        }
        return gid;
    }

}
