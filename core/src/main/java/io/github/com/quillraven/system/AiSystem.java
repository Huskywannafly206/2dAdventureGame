package io.github.com.quillraven.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import io.github.com.quillraven.component.Ai;
import io.github.com.quillraven.component.Attack;
import io.github.com.quillraven.component.Dead;
import io.github.com.quillraven.component.Facing;
import io.github.com.quillraven.component.Facing.FacingDirection;
import io.github.com.quillraven.component.Move;
import io.github.com.quillraven.component.Physic;
import io.github.com.quillraven.component.Player;

/**
 * AiSystem drives enemy behaviour using a simple 3-state model:
 * IDLE → (in sight) → CHASE → (in attack range) → ATTACK
 *
 * Enemies automatically get Ai component if their Tiled tile has sightRange > 0
 * (set via TiledAshleyConfigurator).  Default values: sightRange=3, attackRange=0.8.
 */
public class AiSystem extends IteratingSystem {

    private ImmutableArray<Entity> playerEntities;
    private final Vector2 tmpDir = new Vector2();

    public AiSystem() {
        super(Family.all(Ai.class, Physic.class, Move.class).exclude(Dead.class).get());
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        playerEntities = engine.getEntitiesFor(Family.all(Player.class, Physic.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (playerEntities.size() == 0) return;

        Entity playerEntity = playerEntities.first();
        Physic enemyPhysic = Physic.MAPPER.get(entity);
        Physic playerPhysic = Physic.MAPPER.get(playerEntity);

        Vector2 enemyPos = enemyPhysic.getBody().getPosition();
        Vector2 playerPos = playerPhysic.getBody().getPosition();
        float dist = enemyPos.dst(playerPos);

        Ai ai = Ai.MAPPER.get(entity);
        Move move = Move.MAPPER.get(entity);

        if (dist <= ai.getAttackRange()) {
            // ATTACK: stop moving, attack player if possible
            move.getDirection().setZero();
            Attack attack = Attack.MAPPER.get(entity);
            if (attack != null && attack.canAttack()) {
                attack.startAttack();
            }
            // Face the player
            updateFacing(entity, enemyPos, playerPos);

        } else if (dist <= ai.getSightRange()) {
            // CHASE: move toward player
            tmpDir.set(playerPos).sub(enemyPos).nor();
            move.getDirection().set(tmpDir);
            updateFacing(entity, enemyPos, playerPos);

        } else {
            // IDLE: stop moving
            move.getDirection().setZero();
        }
    }

    private void updateFacing(Entity entity, Vector2 from, Vector2 to) {
        Facing facing = Facing.MAPPER.get(entity);
        if (facing == null) return;

        float dx = to.x - from.x;
        float dy = to.y - from.y;
        if (Math.abs(dx) >= Math.abs(dy)) {
            facing.setDirection(dx >= 0 ? FacingDirection.RIGHT : FacingDirection.LEFT);
        } else {
            facing.setDirection(dy >= 0 ? FacingDirection.UP : FacingDirection.DOWN);
        }
    }
}
