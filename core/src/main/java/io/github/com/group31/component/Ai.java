package io.github.com.group31.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * AI component for enemy entities.
 * Entities with sightRange > 0 in their Tiled tile properties automatically receive this component.
 */
public class Ai implements Component {
    public static final ComponentMapper<Ai> MAPPER = ComponentMapper.getFor(Ai.class);

    private final float sightRange;
    private final float attackRange;

    public Ai(float sightRange, float attackRange) {
        this.sightRange = sightRange;
        this.attackRange = attackRange;
    }

    public float getSightRange() {
        return sightRange;
    }

    public float getAttackRange() {
        return attackRange;
    }
}
