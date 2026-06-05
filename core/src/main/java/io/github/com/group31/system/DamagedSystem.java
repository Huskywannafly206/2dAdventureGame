package io.github.com.group31.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import io.github.com.group31.component.Damaged;
import io.github.com.group31.component.Life;
import io.github.com.group31.component.Player;
import io.github.com.group31.component.Tiled;
import io.github.com.group31.component.Transform;
import io.github.com.group31.ui.model.GameViewModel;

public class DamagedSystem extends IteratingSystem {
    private final GameViewModel viewModel;

    public DamagedSystem(GameViewModel viewModel) {
        super(Family.all(Damaged.class).get());
        this.viewModel = viewModel;
    }

    private boolean areTowersAlive() {
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(
            Family.all(Tiled.class, Life.class).get()
        );
        for (Entity e : entities) {
            Tiled tiled = Tiled.MAPPER.get(e);
            if (tiled != null && tiled.getTileId() == 15) {
                Life life = Life.MAPPER.get(e);
                if (life != null && life.getLife() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Damaged damaged = Damaged.MAPPER.get(entity);
        entity.remove(Damaged.class);

        Tiled tiled = Tiled.MAPPER.get(entity);
        if (tiled != null && tiled.getTileId() == 16) {
            if (areTowersAlive()) {
                // Boss is immune!
                com.badlogic.gdx.Gdx.app.log("DamagedSystem", "Boss is attacked but IMMUNE because green towers are still alive!");
                Transform transform = Transform.MAPPER.get(entity);
                Entity source = damaged.getSourceEntity();
                boolean sourceIsPlayer = source != null && Player.MAPPER.has(source);
                if (transform != null && sourceIsPlayer) {
                    float x = transform.getPosition().x + transform.getSize().x * 0.5f;
                    float y = transform.getPosition().y;
                    viewModel.playerDamage(0, x, y);
                }
                return;
            } else {
                com.badlogic.gdx.Gdx.app.log("DamagedSystem", "Boss is taking damage! Amount: " + damaged.getDamage());
            }
        }

        Life life = Life.MAPPER.get(entity);
        if (life != null) {
            life.addLife(-damaged.getDamage());
        }

        Transform transform = Transform.MAPPER.get(entity);
        // Only show floating damage text when source is the player
        Entity source = damaged.getSourceEntity();
        boolean sourceIsPlayer = source != null && Player.MAPPER.has(source);
        if (transform != null && sourceIsPlayer) {
            float x = transform.getPosition().x + transform.getSize().x * 0.5f;
            float y = transform.getPosition().y;
            viewModel.playerDamage((int) damaged.getDamage(), x, y);
        }
    }
}
