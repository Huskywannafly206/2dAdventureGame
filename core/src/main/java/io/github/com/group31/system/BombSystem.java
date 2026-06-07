package io.github.com.group31.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
import io.github.com.group31.asset.SoundAsset;
import io.github.com.group31.audio.AudioService;
import io.github.com.group31.component.*;
import io.github.com.group31.ui.model.GameViewModel;

public class BombSystem extends IteratingSystem {
    private final AudioService audioService;
    private final GameViewModel viewModel;
    private final Array<Entity> toRemove = new Array<>();

    public BombSystem(AudioService audioService, GameViewModel viewModel) {
        super(Family.all(BombComponent.class, Transform.class).get());
        this.audioService = audioService;
        this.viewModel = viewModel;
    }

    @Override
    public void update(float deltaTime) {
        toRemove.clear();
        super.update(deltaTime);
        for (Entity entity : toRemove) {
            getEngine().removeEntity(entity);
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        BombComponent bomb = BombComponent.MAPPER.get(entity);
        bomb.timeLeft -= deltaTime;

        if (bomb.timeLeft <= 0f) {
            toRemove.add(entity);

            Transform transform = Transform.MAPPER.get(entity);
            if (transform != null) {
                float bombCenterX = transform.getPosition().x + transform.getSize().x * 0.5f;
                float bombCenterY = transform.getPosition().y + transform.getSize().y * 0.5f;

                // Play explosion sound
                audioService.playSound(SoundAsset.TRAP);

                // Show explosion visual effect
                viewModel.showFloatingText("[RED]*EXPLOSION*[]", bombCenterX, bombCenterY);

                // Find player entity to apply damage
                ImmutableArray<Entity> players = getEngine().getEntitiesFor(
                    Family.all(Player.class, Transform.class, Life.class).get()
                );
                if (players.size() > 0) {
                    Entity player = players.first();
                    Transform playerTransform = Transform.MAPPER.get(player);
                    float playerCenterX = playerTransform.getPosition().x + playerTransform.getSize().x * 0.5f;
                    float playerCenterY = playerTransform.getPosition().y + playerTransform.getSize().y * 0.5f;

                    float dx = Math.abs(playerCenterX - bombCenterX);
                    float dy = Math.abs(playerCenterY - bombCenterY);
                    float dist = Math.max(dx, dy); // Chebyshev distance

                    if (dist <= 1.5f) {
                        player.add(new Damaged(4f, null));
                        viewModel.playerDamage(-4, playerCenterX, playerCenterY);
                        
                        Life life = Life.MAPPER.get(player);
                        if (life != null) {
                            viewModel.updateLifeInfo(life.getMaxLife(), Math.max(0f, life.getLife() - 4f));
                        }
                    }
                }
            }
        }
    }
}
