package io.github.com.group31.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import io.github.com.group31.component.Dead;
import io.github.com.group31.component.Life;
import io.github.com.group31.component.Player;
import io.github.com.group31.ui.model.GameViewModel;

public class LifeSystem extends IteratingSystem implements EntityListener {
    private final GameViewModel viewModel;
    // Flag để đảm bảo onPlayerDead() chỉ được gọi đúng 1 lần
    private boolean playerDeadFired = false;

    public LifeSystem(GameViewModel viewModel) {
        // Không exclude Dead.class - vẫn xử lý entity để detect cái chết
        // (enemy sẽ được DeadSystem xử lý, player sẽ trigger Game Over)
        super(Family.all(Life.class).get());
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
        // Reset flag khi player mới được tạo (ví dụ: restart game)
        playerDeadFired = false;
    }

    @Override
    public void entityRemoved(Entity entity) {
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Life life = Life.MAPPER.get(entity);

        // Kiểm tra CHẾT TRƯỚC — trước khi hồi HP.
        // Nếu hồi HP trước, life=0 sẽ thành 0.004 và death check sẽ không bao giờ trigger!
        if (life.getLife() <= 0f && !Dead.MAPPER.has(entity)) {
            if (Player.MAPPER.has(entity)) {
                if (!playerDeadFired) {
                    playerDeadFired = true;
                    entity.add(new Dead());
                    Gdx.app.debug("LifeSystem", "Player died! HP=" + life.getLife() + " → Game Over!");
                    viewModel.onPlayerDead();
                }
            } else {
                // Enemy/NPC died
                entity.add(new Dead());
            }
            return; // Không hồi HP cho entity đã chết
        }

        // Hồi HP — chỉ khi còn sống (life > 0) và chưa đầy
        if (!Dead.MAPPER.has(entity) && life.getLife() > 0f && life.getLife() < life.getMaxLife()) {
            life.addLife(life.getLifePerSec() * deltaTime);
            if (Player.MAPPER.has(entity)) {
                viewModel.updateLifeInfo(life.getMaxLife(), life.getLife());
            }
        }
    }
}
