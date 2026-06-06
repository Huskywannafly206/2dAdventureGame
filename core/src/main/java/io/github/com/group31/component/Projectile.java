package io.github.com.group31.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import io.github.com.group31.combat.Weapon.ProjectileType;

/**
 * Component đánh dấu thực thể là một vật thể bay (đạn).
 *
 * Projectile entity sẽ được tạo động bởi ControllerSystem khi Player
 * bắn đạn, bao gồm các component: Transform, Physic, Graphic, Projectile, Attack.
 */
public class Projectile implements Component {
    public static final ComponentMapper<Projectile> MAPPER =
        ComponentMapper.getFor(Projectile.class);

    /** Entity đã bắn viên đạn này (để không tự sát). */
    private final Entity owner;

    /** Loại đạn (dùng để lấy thông số speed, atlas key...). */
    private final ProjectileType type;

    /** Thời gian sống còn lại (giây). Khi = 0 thì ProjectileSystem hủy entity. */
    private float lifeTime;

    /** Vận tốc bay (vector, tính bằng đơn vị game/giây). Đã nhân speed. */
    private final Vector2 velocity;

    /** Đã đánh trúng mục tiêu chưa (tránh va chạm nhiều lần trong 1 frame). */
    private boolean hitTarget;

    public Projectile(Entity owner, ProjectileType type) {
        this.owner     = owner;
        this.type      = type;
        this.lifeTime  = type.maxLifeTime;
        this.velocity  = new Vector2();
        this.hitTarget = false;
    }

    public Entity getOwner()           { return owner; }
    public ProjectileType getType()    { return type; }

    public float getLifeTime()         { return lifeTime; }
    public void  decLifeTime(float dt) { lifeTime = Math.max(0f, lifeTime - dt); }
    public boolean isExpired()         { return lifeTime <= 0f; }

    public Vector2 getVelocity()       { return velocity; }

    public boolean isHitTarget()       { return hitTarget; }
    public void    setHitTarget(boolean v) { this.hitTarget = v; }
}
