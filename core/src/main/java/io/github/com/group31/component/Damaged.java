package io.github.com.group31.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

public class Damaged implements Component {
    public static final ComponentMapper<Damaged> MAPPER = ComponentMapper.getFor(Damaged.class);

    private float damage;
    private Entity sourceEntity;

    public Damaged(float damage, Entity sourceEntity) {
        this.damage = damage;
        this.sourceEntity = sourceEntity;
    }

    public void addDamage(float amount) {
        this.damage += amount;
    }

    public float getDamage() {
        return damage;
    }

    /** Null-safe: returns null if damage came from environment (trap, etc.) */
    public Entity getSourceEntity() {
        return sourceEntity;
    }
}
