package io.github.com.quillraven.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.Animation;
import io.github.com.quillraven.asset.SoundAsset;
import io.github.com.quillraven.audio.AudioService;
import io.github.com.quillraven.component.Animation2D;
import io.github.com.quillraven.component.Life;
import io.github.com.quillraven.component.Tiled;
import io.github.com.quillraven.component.Trigger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class TriggerSystem extends IteratingSystem {
    private final AudioService audioService;
    /** Registry: trigger name → handler(triggerComponent, triggeringEntity) */
    private final Map<String, BiConsumer<Trigger, Entity>> triggerHandlers;

    public TriggerSystem(AudioService audioService) {
        super(Family.all(Trigger.class).get());
        this.audioService = audioService;
        this.triggerHandlers = new HashMap<>();

        // Register built-in triggers
        triggerHandlers.put("trap_trigger", this::trapTrigger);
    }

    /**
     * Register a custom trigger handler from outside (e.g. GameScreen).
     */
    public void registerTrigger(String name, BiConsumer<Trigger, Entity> handler) {
        triggerHandlers.put(name, handler);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Trigger trigger = Trigger.MAPPER.get(entity);
        if (trigger.getTriggeringEntity() == null) return;

        Entity triggeringEntity = trigger.getTriggeringEntity();
        trigger.setTriggeringEntity(null);

        BiConsumer<Trigger, Entity> handler = triggerHandlers.get(trigger.getName());
        if (handler != null) {
            handler.accept(trigger, triggeringEntity);
        } else {
            // Log unknown triggers instead of crashing
            com.badlogic.gdx.Gdx.app.error("TriggerSystem", "No handler registered for trigger: " + trigger.getName());
        }
    }

    private Entity getByTiledId(int tiledId) {
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(Family.all(Tiled.class).get());
        for (Entity entity : entities) {
            if (Tiled.MAPPER.get(entity).getId() == tiledId) {
                return entity;
            }
        }
        return null;
    }

    /**
     * Handles trap trigger effects including animation and damage.
     * Uses targetTiledId from the Trigger component — no more hardcoded ID.
     */
    private void trapTrigger(Trigger trigger, Entity triggeringEntity) {
        Entity trapEntity = getByTiledId(trigger.getTargetTiledId());
        if (trapEntity != null) {
            // Play trap animation
            Animation2D animation2D = Animation2D.MAPPER.get(trapEntity);
            animation2D.setSpeed(1f);
            animation2D.setPlayMode(Animation.PlayMode.NORMAL);
            audioService.playSound(SoundAsset.TRAP);
            // Reset animation after 2.5 s
            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                @Override
                public void run() {
                    animation2D.setSpeed(0f);
                    animation2D.setType(Animation2D.AnimationType.IDLE);
                }
            }, 2.5f);

            // Damage triggering entity (environment source → null sourceEntity)
            Life life = Life.MAPPER.get(triggeringEntity);
            if (life != null && life.getLife() > 2) {
                life.addLife(-2f);
            }
        }
    }
}
