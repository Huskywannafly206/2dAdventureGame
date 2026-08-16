package io.github.com.group31.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import io.github.com.group31.component.Door;
import io.github.com.group31.component.Graphic;
import io.github.com.group31.component.Physic;
import io.github.com.group31.component.Player;
import io.github.com.group31.component.Transform;

public class DoorSystem extends IteratingSystem {
    private static final float OPEN_DISTANCE_SQR = 1.5f * 1.5f; // Adjust trigger distance as needed
    private static final float CLOSE_DELAY_SECONDS = 0.5f; // Delay before auto-close

    public DoorSystem() {
        super(Family.all(Door.class, Transform.class, Graphic.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Door door = Door.MAPPER.get(entity);

        ImmutableArray<Entity> players = getEngine().getEntitiesFor(Family.all(Player.class, Transform.class).get());
        if (players.size() == 0) return;

        Entity player = players.first();
        Transform playerTransform = Transform.MAPPER.get(player);
        Transform doorTransform = Transform.MAPPER.get(entity);

        float distSqr = playerTransform.getPosition().dst2(doorTransform.getPosition());

        if (!door.isOpen()) {
            if (distSqr <= OPEN_DISTANCE_SQR) {
                openDoor(entity, door);
            }
        } else {
            if (distSqr > OPEN_DISTANCE_SQR) {
                door.setTimeSincePlayerLeft(door.getTimeSincePlayerLeft() + deltaTime);
                if (door.getTimeSincePlayerLeft() >= CLOSE_DELAY_SECONDS) {
                    closeDoor(entity, door);
                }
            } else {
                door.setTimeSincePlayerLeft(0f); // Reset timer if player comes back
            }
        }
    }

    private void openDoor(Entity entity, Door door) {
        door.setOpen(true);
        door.setTimeSincePlayerLeft(0f);

        Graphic graphic = Graphic.MAPPER.get(entity);
        graphic.setRegion(door.getOpenRegion());

        Transform transform = Transform.MAPPER.get(entity);
        if (door.getOpenRotation() != 0f) {
            transform.setRotationDeg(transform.getRotationDeg() + door.getOpenRotation());
        }

        Physic physic = Physic.MAPPER.get(entity);
        if (physic != null && physic.getBody() != null) {
            for (com.badlogic.gdx.physics.box2d.Fixture fixture : physic.getBody().getFixtureList()) {
                fixture.setSensor(true);
            }
        }
    }

    private void closeDoor(Entity entity, Door door) {
        door.setOpen(false);
        door.setTimeSincePlayerLeft(0f);

        Graphic graphic = Graphic.MAPPER.get(entity);
        graphic.setRegion(door.getClosedRegion());

        Transform transform = Transform.MAPPER.get(entity);
        if (door.getOpenRotation() != 0f) {
            transform.setRotationDeg(transform.getRotationDeg() - door.getOpenRotation());
        }

        Physic physic = Physic.MAPPER.get(entity);
        if (physic != null && physic.getBody() != null) {
            for (com.badlogic.gdx.physics.box2d.Fixture fixture : physic.getBody().getFixtureList()) {
                fixture.setSensor(false);
            }
        }
    }
}
