package io.github.com.group31.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Chest implements Component {
    public static final ComponentMapper<Chest> MAPPER = ComponentMapper.getFor(Chest.class);

    private boolean isOpen;
    private final TextureRegion openRegion;
    private final TextureRegion closedRegion;
    private final String lootType;

    public Chest(TextureRegion closedRegion, TextureRegion openRegion, String lootType) {
        this.isOpen = false;
        this.closedRegion = closedRegion;
        this.openRegion = openRegion;
        this.lootType = lootType;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        this.isOpen = open;
    }

    public TextureRegion getOpenRegion() {
        return openRegion;
    }

    public TextureRegion getClosedRegion() {
        return closedRegion;
    }

    public String getLootType() {
        return lootType;
    }
}
