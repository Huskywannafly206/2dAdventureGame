package io.github.com.group31.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.com.group31.component.Dead;
import io.github.com.group31.component.Life;
import io.github.com.group31.component.Player;
import io.github.com.group31.ui.model.GameViewModel;

public class LifeSystem extends IteratingSystem implements EntityListener {
    private final GameViewModel viewModel;

    public LifeSystem(GameViewModel viewModel) {
        super(Family.all(Life.class).exclude(Dead.class).get());
        this.viewModel = viewModel;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(Family.all(Life.class, Player.class).get(), this);
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        engine.removeEntityListener(this);
    }

    @Override
    public void entityAdded(Entity entity) {
        Life life = Life.MAPPER.get(entity);
        viewModel.updateLifeInfo(life.getMaxLife(), life.getLife());
    }

    @Override
    public void entityRemoved(Entity entity) {
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Life life = Life.MAPPER.get(entity);

        // Regenerate life (skip if full or dead)
        if (life.getLife() < life.getMaxLife()) {
            life.addLife(life.getLifePerSec() * deltaTime);
            if (Player.MAPPER.get(entity) != null) {
                viewModel.updateLifeInfo(life.getMaxLife(), life.getLife());
            }
        }

        // Death detection
        if (life.getLife() <= 0f) {
            if (Player.MAPPER.has(entity)) {
                // Chỉ gọi onPlayerDead() MỘT LẦN bằng cách thêm Dead component làm flag.
                // Nếu không có guard này, onPlayerDead() sẽ được gọi mỗi frame khi HP = 0,
                // khiến PropertyChangeSupport bỏ qua sự kiện (old==new) và Game Over không hiện.
                if (!Dead.MAPPER.has(entity)) {
                    entity.add(new Dead());
                    viewModel.onPlayerDead();
                }
            } else {
                // Enemy/NPC died → add Dead marker for DeadSystem to handle
                if (!Dead.MAPPER.has(entity)) {
                    entity.add(new Dead());
                }
            }
        }
    }
}
