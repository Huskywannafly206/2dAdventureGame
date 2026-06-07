package io.github.com.group31.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class ScarecrowComponent implements Component {
    public static final ComponentMapper<ScarecrowComponent> MAPPER = ComponentMapper.getFor(ScarecrowComponent.class);

    private final int index; // 1, 2, or 3
    private Label label;

    public ScarecrowComponent(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }
}
