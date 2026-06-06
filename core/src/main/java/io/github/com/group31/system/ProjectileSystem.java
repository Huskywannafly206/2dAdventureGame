package io.github.com.group31.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import io.github.com.group31.component.Physic;
import io.github.com.group31.component.Projectile;

/**
 * Quản lý vòng đời của các thực thể đạn bay:
 *  - Mỗi frame cập nhật vận tốc Box2D Body của đạn (giữ hướng bay không đổi).
 *  - Giảm thời gian sống (lifeTime). Khi hết hạn hoặc đã đánh trúng mục tiêu
 *    (hitTarget = true) → đưa vào hàng chờ hủy.
 */
public class ProjectileSystem extends IteratingSystem {

    private final Array<Entity> toRemove = new Array<>();

    public ProjectileSystem() {
        super(Family.all(Projectile.class, Physic.class).get());
    }

    @Override
    public void update(float deltaTime) {
        toRemove.clear();
        super.update(deltaTime);
        for (Entity e : toRemove) {
            getEngine().removeEntity(e);
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Projectile proj = Projectile.MAPPER.get(entity);

        // Đã trúng mục tiêu hoặc hết tuổi thọ → hủy
        if (proj.isHitTarget()) {
            toRemove.add(entity);
            return;
        }

        proj.decLifeTime(deltaTime);
        if (proj.isExpired()) {
            toRemove.add(entity);
            return;
        }

        // Đảm bảo Box2D body luôn giữ đúng vận tốc bay
        // (box2d có thể làm chậm velocity do ma sát linear damping)
        Body body = Physic.MAPPER.get(entity).getBody();
        Vector2 vel = proj.getVelocity();
        body.setLinearVelocity(vel.x, vel.y);
    }
}
