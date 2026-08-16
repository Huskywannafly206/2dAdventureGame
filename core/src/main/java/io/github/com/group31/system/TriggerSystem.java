package io.github.com.group31.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Timer;
import io.github.com.group31.asset.SoundAsset;
import io.github.com.group31.audio.AudioService;
import io.github.com.group31.component.Animation2D;
import io.github.com.group31.component.Graphic;
import io.github.com.group31.component.Life;
import io.github.com.group31.component.Physic;
import io.github.com.group31.component.Tiled;
import io.github.com.group31.component.Trigger;

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
        triggerHandlers.put("barrier_trigger", this::barrierTrigger);
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

        String triggerName = trigger.getName();
        BiConsumer<Trigger, Entity> handler = triggerHandlers.get(triggerName);
        if (handler == null && triggerName != null && triggerName.startsWith("portal_trigger")) {
            handler = triggerHandlers.get("portal_trigger");
        }
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
     * Handles trap trigger: gây 3 damage ngay khi chạm, rồi gây thêm 3 damage mỗi giây
     * miễn là player vẫn còn đứng trong vùng trap (triggeringEntity != null).
     * Khi player rời khỏi vùng (endContact → triggeringEntity = null), timer dừng.
     */
    private void trapTrigger(Trigger trigger, Entity triggeringEntity) {
        Entity trapEntity = getByTiledId(trigger.getTargetTiledId());
        if (trapEntity == null) return;

        // Play trap animation
        Animation2D animation2D = Animation2D.MAPPER.get(trapEntity);
        animation2D.setSpeed(1f);
        animation2D.setPlayMode(Animation.PlayMode.NORMAL);
        audioService.playSound(SoundAsset.TRAP);

        // Gây damage ngay lập tức
        applyTrapDamage(triggeringEntity, 3f);

        // Gây damage liên tục mỗi giây — dừng khi player rời trap (trigger.getTriggeringEntity() == null)
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                // Player đã rời khỏi vùng trap (endContact đã clear về null)
                if (trigger.getTriggeringEntity() == null) {
                    cancel();
                    animation2D.setSpeed(0f);
                    animation2D.setType(Animation2D.AnimationType.IDLE);
                    return;
                }

                Life life = Life.MAPPER.get(triggeringEntity);
                if (life == null || life.getLife() <= 0f) {
                    cancel();
                    return;
                }

                applyTrapDamage(triggeringEntity, 3f);

                if (life.getLife() <= 0f) {
                    cancel();
                }
            }
        }, 1f, 1f); // delay 1s, interval 1s
    }

    private void applyTrapDamage(Entity entity, float damage) {
        Life life = Life.MAPPER.get(entity);
        if (life != null && life.getLife() > 0f) {
            life.addLife(-damage);
            com.badlogic.gdx.Gdx.app.debug("TriggerSystem", "Trap damage! HP now: " + life.getLife());
        }
    }

    private void barrierTrigger(Trigger trigger, Entity triggeringEntity) {
        boolean activated = false;
        ImmutableArray<Entity> matchingEntities = getEngine().getEntitiesFor(Family.all(Tiled.class, Graphic.class, Physic.class).get());
        for (Entity entity : matchingEntities) {
            Tiled tiled = Tiled.MAPPER.get(entity);
            if (tiled.getMapObjectRef() != null && "stone_block_gate".equals(tiled.getMapObjectRef().getName())) {
                Graphic graphic = Graphic.MAPPER.get(entity);
                if (graphic.getColor().a < 1f) {
                    graphic.getColor().a = 1f;

                    Physic physic = Physic.MAPPER.get(entity);
                    if (physic != null && physic.getBody() != null) {
                        for (com.badlogic.gdx.physics.box2d.Fixture fixture : physic.getBody().getFixtureList()) {
                            fixture.setSensor(false);
                        }
                    }
                    activated = true;
                }
            }
        }
        if (activated) {
            audioService.playSound(SoundAsset.TRAP);
            com.badlogic.gdx.Gdx.app.log("TriggerSystem", "barrier_trigger activated! Stone blocks are now visible and solid.");
        }
    }
}
