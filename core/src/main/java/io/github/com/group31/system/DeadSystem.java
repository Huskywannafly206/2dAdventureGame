package io.github.com.group31.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import io.github.com.group31.component.Animation2D;
import io.github.com.group31.component.Dead;
import io.github.com.group31.component.Experience;
import io.github.com.group31.component.Player;
import io.github.com.group31.component.Tiled;
import io.github.com.group31.component.Transform;
import io.github.com.group31.ui.model.GameViewModel;

/**
 * DeadSystem: waits for dead enemy's death animation to finish,
 * awards XP to player, then removes the entity from the engine.
 */
public class DeadSystem extends IteratingSystem {
    private static final float REMOVAL_DELAY = 1.5f;

    private final GameViewModel viewModel;
    private ImmutableArray<Entity> playerEntities;

    public DeadSystem(GameViewModel viewModel) {
        super(Family.all(Dead.class).get());
        this.viewModel = viewModel;
    }

    @Override
    public void addedToEngine(com.badlogic.ashley.core.Engine engine) {
        super.addedToEngine(engine);
        playerEntities = engine.getEntitiesFor(Family.all(Player.class, Experience.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Dead dead = Dead.MAPPER.get(entity);
        dead.incTimer(deltaTime);

        Animation2D anim = Animation2D.MAPPER.get(entity);
        boolean animDone = (anim == null) || anim.isFinished();

        if (animDone && dead.getTimer() >= REMOVAL_DELAY) {
            // Không xóa Player — Player chết được xử lý bởi Game Over screen
            if (Player.MAPPER.has(entity)) {
                return;
            }

            // Award XP to player
            Experience enemyXp = Experience.MAPPER.get(entity);
            if (enemyXp != null && enemyXp.getXpReward() > 0 && playerEntities.size() > 0) {
                Entity playerEntity = playerEntities.first();
                Experience playerXp = Experience.MAPPER.get(playerEntity);
                if (playerXp != null) {
                    boolean leveledUp = playerXp.addXp(enemyXp.getXpReward());
                    viewModel.updateXpInfo(playerXp.getXp(), playerXp.getXpToNextLevel(), playerXp.getLevel(), leveledUp);
                }
            }

            // If it is a green tower, keep it on the map permanently as a ruined structure.
            Tiled tiled = Tiled.MAPPER.get(entity);
            if (tiled != null && tiled.getMapObjectRef() != null) {
                String name = tiled.getMapObjectRef().getName();
                if (name != null && name.contains("green_tower")) {
                    entity.remove(Dead.class);
                    return;
                }
                if ("GiantBlueSamurai".equals(name)) {
                    removeStoneBlockGates();
                }
            }
            if (tiled != null && tiled.getTileId() == 9) {
                io.github.com.group31.quest.QuestManager.INSTANCE.onSlimeDefeated();
            }
            if (tiled != null && tiled.getTileId() == 19) {
                Transform transform = Transform.MAPPER.get(entity);
                if (transform != null) {
                    io.github.com.group31.quest.QuestManager.INSTANCE.onSnowSpriteDefeated(
                        transform.getPosition().x,
                        transform.getPosition().y
                    );
                }
            }

            getEngine().removeEntity(entity);
        }
    }

    private void removeStoneBlockGates() {
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(Family.all(Tiled.class).get());
        java.util.List<Entity> toRemove = new java.util.ArrayList<>();
        for (Entity entity : entities) {
            Tiled tiled = Tiled.MAPPER.get(entity);
            if (tiled.getMapObjectRef() != null && "stone_block_gate".equals(tiled.getMapObjectRef().getName())) {
                toRemove.add(entity);
            }
        }
        for (Entity entity : toRemove) {
            getEngine().removeEntity(entity);
        }
        com.badlogic.gdx.Gdx.app.log("DeadSystem", "GiantBlueSamurai defeated! Removed " + toRemove.size() + " stone block gates.");
    }
}
