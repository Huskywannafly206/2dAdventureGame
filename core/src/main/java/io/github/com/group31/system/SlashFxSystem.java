package io.github.com.group31.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.com.group31.asset.*;
import io.github.com.group31.combat.Weapon;
import io.github.com.group31.component.*;
import io.github.com.group31.component.Facing.FacingDirection;

public class SlashFxSystem extends IteratingSystem {

    // Key của các FX slash trong atlas
    private static final String SLASH_MELEE = "slash_fx/slash01";
    private static final float FX_SIZE = 1.2f;
    private static final float FX_LIFETIME = 0.18f; // giây — đủ ngắn để khớp animation

    private final Engine engine;
    private final AssetService assetService;

    public SlashFxSystem(Engine engine, AssetService assetService) {
        super(Family.all(Attack.class, CombatState.class, Transform.class, Facing.class).get());
        this.engine = engine;
        this.assetService = assetService;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Attack attack = Attack.MAPPER.get(entity);

        // Chỉ spawn FX đúng lúc đánh bắt đầu
        if (!attack.hasAttackStarted()) return;

        CombatState cs = CombatState.MAPPER.get(entity);
        Weapon weapon = cs.getCurrentWeapon();
        if (!weapon.isMelee()) return; // Ranged dùng hiệu ứng projectile riêng

        Transform transform = Transform.MAPPER.get(entity);
        FacingDirection dir = Facing.MAPPER.get(entity).getDirection();

        spawnSlashFx(transform.getPosition(), transform.getSize(), dir);
    }

    private void spawnSlashFx(Vector2 playerPos, Vector2 playerSize, FacingDirection dir) {
        TextureAtlas atlas = assetService.get(AtlasAsset.OBJECTS);
        Array<AtlasRegion> regions = atlas.findRegions(SLASH_MELEE);
        if (regions.isEmpty()) return;

        Entity fx = engine.createEntity();

        // Vị trí: phía trước mặt nhân vật
        Vector2 offset = getFxOffset(dir);
        float x = playerPos.x + playerSize.x * 0.5f + offset.x - FX_SIZE * 0.5f;
        float y = playerPos.y + playerSize.y * 0.5f + offset.y - FX_SIZE * 0.5f;

        float rotation = getRotationForDir(dir);

        // Transform
        fx.add(new Transform(
            new Vector2(x, y),
            2,                          // z-layer cao hơn nhân vật
            new Vector2(FX_SIZE, FX_SIZE),
            new Vector2(1f, 1f),
            rotation,
            0f
        ));

        // Graphic — dùng frame đầu, SlashFxLifetimeSystem sẽ xử lý animation
        fx.add(new Graphic(regions.first(), Color.WHITE));

        // Component lifetime để tự xóa sau FX_LIFETIME giây
        fx.add(new SlashFxLifetime(regions, FX_LIFETIME));

        engine.addEntity(fx);
    }

    private Vector2 getFxOffset(FacingDirection dir) {
        return switch (dir) {
            case RIGHT -> new Vector2( 0.8f,  0.0f);
            case LEFT  -> new Vector2(-0.8f,  0.0f);
            case UP    -> new Vector2( 0.0f,  0.8f);
            case DOWN  -> new Vector2( 0.0f, -0.8f);
        };
    }

    private float getRotationForDir(FacingDirection dir) {
        return switch (dir) {
            case RIGHT -> 0f;
            case LEFT  -> 180f;
            case UP    -> 90f;
            case DOWN  -> 270f;
        };
    }
}
