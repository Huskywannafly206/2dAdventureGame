package io.github.com.group31.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.com.group31.asset.*;
import io.github.com.group31.combat.Weapon;
import io.github.com.group31.component.*;
import io.github.com.group31.component.Facing.FacingDirection;
import io.github.com.group31.component.Animation2D.AnimationType;

public class WeaponHandSystem extends IteratingSystem {

    // Offset sprite vũ khí tính từ góc dưới-trái nhân vật (đơn vị tile = 1f)
    private static final float WEAPON_SIZE = 1f;
    private static final float FRAME_DURATION = 1 / 8f;

    private final Batch batch;
    private final AssetService assetService;

    // Offset theo hướng mặt (x, y) tính theo tile unit
    private static final Vector2 OFF_RIGHT = new Vector2( 0.5f, 0.0f);
    private static final Vector2 OFF_LEFT  = new Vector2(-0.5f, 0.0f);
    private static final Vector2 OFF_UP    = new Vector2( 0.0f, 0.5f);
    private static final Vector2 OFF_DOWN  = new Vector2( 0.0f,-0.3f);

    // stateTime theo từng entity — dùng entity id làm key (hoặc dùng Map<Entity, Float>)
    private final java.util.Map<Entity, float[]> stateTimeMap = new java.util.WeakHashMap<>();

    public WeaponHandSystem(Batch batch, AssetService assetService) {
        super(Family.all(CombatState.class, Transform.class, Facing.class, Animation2D.class).get());
        this.batch = batch;
        this.assetService = assetService;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        CombatState cs = CombatState.MAPPER.get(entity);
        Weapon weapon = cs.getCurrentWeapon();

        // FIST không có sprite → bỏ qua
        if (weapon.inHandAtlasKey == null) return;

        Transform transform = Transform.MAPPER.get(entity);
        FacingDirection dir = Facing.MAPPER.get(entity).getDirection();
        Animation2D anim2d = Animation2D.MAPPER.get(entity);

        // Lấy animation frame tương ứng hướng + trạng thái đánh
        boolean isAttacking = anim2d.getType() == AnimationType.ATTACK;
        TextureRegion frame = getWeaponFrame(entity, weapon, dir, isAttacking, deltaTime);
        if (frame == null) return;

        // Tính vị trí vẽ
        Vector2 pos = transform.getPosition();
        Vector2 size = transform.getSize();
        Vector2 offset = getOffset(dir);

        float drawX = pos.x + size.x * 0.5f + offset.x - WEAPON_SIZE * 0.5f;
        float drawY = pos.y + size.y * 0.5f + offset.y - WEAPON_SIZE * 0.5f;

        batch.draw(frame, drawX, drawY, WEAPON_SIZE, WEAPON_SIZE);
    }

    private TextureRegion getWeaponFrame(Entity entity, Weapon weapon,
                                         FacingDirection dir, boolean isAttacking,
                                         float deltaTime) {
        TextureAtlas atlas = assetService.get(AtlasAsset.OBJECTS);
        String key = weapon.inHandAtlasKey + "/" + (isAttacking ? "attack" : "idle") + "_" + dir.getAtlasKey();
        Array<AtlasRegion> regions = atlas.findRegions(key);

        // Fallback về idle nếu không có attack
        if (regions.isEmpty()) {
            key = weapon.inHandAtlasKey + "/idle_" + dir.getAtlasKey();
            regions = atlas.findRegions(key);
        }
        // Fallback về 1 frame tĩnh
        if (regions.isEmpty()) {
            AtlasRegion single = atlas.findRegion(weapon.inHandAtlasKey);
            return single;
        }

        // Cập nhật stateTime
        float[] st = stateTimeMap.computeIfAbsent(entity, e -> new float[]{0f});
        if (isAttacking) {
            st[0] += deltaTime;
        } else {
            st[0] = 0f;
        }
        Animation<TextureRegion> animation = new Animation<>(FRAME_DURATION, regions);
        animation.setPlayMode(Animation.PlayMode.NORMAL);
        return animation.getKeyFrame(st[0]);
    }

    private Vector2 getOffset(FacingDirection dir) {
        return switch (dir) {
            case RIGHT -> OFF_RIGHT;
            case LEFT  -> OFF_LEFT;
            case UP    -> OFF_UP;
            case DOWN  -> OFF_DOWN;
        };
    }
}
