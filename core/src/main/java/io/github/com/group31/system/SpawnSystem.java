package io.github.com.group31.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.com.group31.component.Ai;
import io.github.com.group31.component.Dead;
import io.github.com.group31.component.Trigger;
import io.github.com.group31.tiled.TiledAshleyConfigurator;

import java.util.HashMap;
import java.util.Map;

/**
 * SpawnSystem: mỗi frame kiểm tra các trigger bắt đầu bằng "spawn_".
 * Quản lý trạng thái spawn (timer, danh sách quái sống) cục bộ mà không cần Spawn component.
 */
public class SpawnSystem extends IteratingSystem {

    private static final int DEFAULT_GLOBAL_MAX_MOBS = 10;

    private final TiledAshleyConfigurator configurator;
    private final int globalMaxMobs;
    private final Vector2 tmpPos = new Vector2();
    private final Map<Entity, SpawnState> spawnStates = new HashMap<>();

    /** Đếm tổng số quái có AI còn sống (chưa chết) trên toàn map. */
    private ImmutableArray<Entity> livingMobsFamily;

    public SpawnSystem(TiledAshleyConfigurator configurator) {
        this(configurator, DEFAULT_GLOBAL_MAX_MOBS);
    }

    public SpawnSystem(TiledAshleyConfigurator configurator, int globalMaxMobs) {
        super(Family.all(Trigger.class).get());
        this.configurator = configurator;
        this.globalMaxMobs = globalMaxMobs;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        livingMobsFamily = engine.getEntitiesFor(Family.all(Ai.class).exclude(Dead.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        spawnStates.clear();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Trigger trigger = Trigger.MAPPER.get(entity);
        if (trigger == null || trigger.getName() == null || !trigger.getName().startsWith("spawn_")) {
            return;
        }

        SpawnState state = spawnStates.get(entity);
        if (state == null) {
            com.badlogic.gdx.maps.MapObject mapObj = trigger.getMapObject();
            if (!(mapObj instanceof com.badlogic.gdx.maps.objects.RectangleMapObject rectMapObj)) {
                return;
            }

            int mobTileId = rectMapObj.getProperties().get("mobTileId", 9, Integer.class);
            int maxCount = rectMapObj.getProperties().get("maxCount", 3, Integer.class);
            float cooldown = rectMapObj.getProperties().get("cooldown", 8f, Float.class);
            float radius = rectMapObj.getProperties().get("spawnRadius", 1.5f, Float.class);

            com.badlogic.gdx.math.Rectangle rect = rectMapObj.getRectangle();
            float centerX = (rect.getX() + rect.getWidth() * 0.5f) * io.github.com.group31.GdxGame.UNIT_SCALE;
            float centerY = (rect.getY() + rect.getHeight() * 0.5f) * io.github.com.group31.GdxGame.UNIT_SCALE;

            state = new SpawnState(mobTileId, maxCount, cooldown, radius, new Vector2(centerX, centerY));
            spawnStates.put(entity, state);
        }

        Engine engine = getEngine();
        Array<Entity> living = state.livingEntities;

        // 1. Dọn danh sách: xóa entity đã bị remove khỏi engine hoặc đang trong trạng thái chết
        for (int i = living.size - 1; i >= 0; i--) {
            Entity mob = living.get(i);
            if (!engine.getEntities().contains(mob, true) || Dead.MAPPER.has(mob)) {
                living.removeIndex(i);
            }
        }

        // 2. Tăng timer
        state.timer += deltaTime;

        // 3. Kiểm tra giới hạn per-spawner
        if (living.size >= state.maxCount) return;

        // 4. Kiểm tra giới hạn toàn map
        if (livingMobsFamily.size() >= globalMaxMobs) return;

        // 5. Kiểm tra cooldown
        if (state.timer < state.cooldown) return;

        // 6. Tính vị trí ngẫu nhiên trong bán kính
        float angle = MathUtils.random(0f, MathUtils.PI2);
        float radiusVal = MathUtils.random(0f, state.spawnRadius);
        tmpPos.set(
            state.center.x + MathUtils.cos(angle) * radiusVal,
            state.center.y + MathUtils.sin(angle) * radiusVal
        );

        // 7. Tạo mob entity
        Entity mob = configurator.spawnMob(state.mobTileId, tmpPos.x, tmpPos.y);
        if (mob != null) {
            living.add(mob);
            state.timer = 0f;
            com.badlogic.gdx.Gdx.app.debug("SpawnSystem",
                "Spawned mob (tileId=" + state.mobTileId + ") at " + tmpPos
                + " | per-spawner=" + living.size + "/" + state.maxCount
                + " | global=" + livingMobsFamily.size() + "/" + globalMaxMobs);
        }
    }

    /**
     * Lớp lưu trữ trạng thái spawn nội bộ cho từng Trigger Entity.
     */
    private static class SpawnState {
        final int mobTileId;
        final int maxCount;
        final float cooldown;
        final float spawnRadius;
        final Vector2 center;
        float timer;
        final Array<Entity> livingEntities;

        SpawnState(int mobTileId, int maxCount, float cooldown, float spawnRadius, Vector2 center) {
            this.mobTileId = mobTileId;
            this.maxCount = maxCount;
            this.cooldown = cooldown;
            this.spawnRadius = spawnRadius;
            this.center = center;
            this.timer = cooldown; // spawn ngay khi bắt đầu
            this.livingEntities = new Array<>();
        }
    }
}
