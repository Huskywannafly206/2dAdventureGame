package io.github.com.group31.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Door implements Component {
    public static final ComponentMapper<Door> MAPPER = ComponentMapper.getFor(Door.class);

    private boolean isOpen;
    private final TextureRegion openRegion;
    private final TextureRegion closedRegion;
    private float timeSincePlayerLeft;

    public Door(TextureRegion closedRegion, TextureRegion openRegion) {
        this.isOpen = false;
        this.closedRegion = closedRegion;
        this.openRegion = openRegion;
        this.timeSincePlayerLeft = 0f;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public TextureRegion getOpenRegion() {
        return openRegion;
    }

    public TextureRegion getClosedRegion() {
        return closedRegion;
    }

    public float getTimeSincePlayerLeft() {
        return timeSincePlayerLeft;
    }

    public void setTimeSincePlayerLeft(float timeSincePlayerLeft) {
        this.timeSincePlayerLeft = timeSincePlayerLeft;
    }
}
