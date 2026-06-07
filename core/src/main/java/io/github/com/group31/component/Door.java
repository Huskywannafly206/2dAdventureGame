package io.github.com.group31.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Door implements Component {
    public static final ComponentMapper<Door> MAPPER = ComponentMapper.getFor(Door.class);

    private boolean isOpen;
    private final TextureRegion openRegion;

    public Door(TextureRegion openRegion) {
        this.isOpen = false;
        this.openRegion = openRegion;
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
}
