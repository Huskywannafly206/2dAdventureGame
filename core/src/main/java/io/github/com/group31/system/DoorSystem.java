package io.github.com.group31.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.Sprite;
import io.github.com.group31.component.Door;
import io.github.com.group31.component.Graphic;
import io.github.com.group31.component.Physic;
import io.github.com.group31.component.Player;
import io.github.com.group31.component.Transform;

public class DoorSystem extends IteratingSystem {
    private static final float OPEN_DISTANCE_SQR = 1.5f * 1.5f; // Adjust trigger distance as needed

    public DoorSystem() {
        super(Family.all(Door.class, Transform.class, Graphic.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Door door = Door.MAPPER.get(entity);
        if (door.isOpen()) return;

        ImmutableArray<Entity> players = getEngine().getEntitiesFor(Family.all(Player.class, Transform.class).get());
        if (players.size() == 0) return;

        Entity player = players.first();
        Transform playerTransform = Transform.MAPPER.get(player);
        Transform doorTransform = Transform.MAPPER.get(entity);

        float distSqr = playerTransform.getPosition().dst2(doorTransform.getPosition());
        if (distSqr <= OPEN_DISTANCE_SQR) {
            openDoor(entity, door);
        }
    }

    private void openDoor(Entity entity, Door door) {
        door.setOpen(true);

        // Update graphic
        Graphic graphic = Graphic.MAPPER.get(entity);
        graphic.setRegion(door.getOpenRegion());

        // Remove collision by setting sensors
        Physic physic = Physic.MAPPER.get(entity);
        if (physic != null && physic.getBody() != null) {
            for (com.badlogic.gdx.physics.box2d.Fixture fixture : physic.getBody().getFixtureList()) {
                fixture.setSensor(true);
            }
        }
    }
}
