package io.github.com.group31.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Npc implements Component {
    public static final ComponentMapper<Npc> MAPPER = ComponentMapper.getFor(Npc.class);

    private final String name;
    private final String[] dialogue;
    private int currentLineIndex;

    public Npc(String name, String[] dialogue) {
        this.name = name;
        this.dialogue = dialogue != null ? dialogue : new String[0];
        this.currentLineIndex = 0;
    }

    public String getName() {
        return name;
    }

    public String[] getDialogue() {
        return dialogue;
    }

    public int getCurrentLineIndex() {
        return currentLineIndex;
    }

    public void setCurrentLineIndex(int index) {
        this.currentLineIndex = index;
    }

    public boolean hasMoreDialogue() {
        return currentLineIndex < dialogue.length;
    }

    public String getCurrentLine() {
        if (currentLineIndex >= 0 && currentLineIndex < dialogue.length) {
            return dialogue[currentLineIndex];
        }
        return "";
    }

    public void advanceDialogue() {
        currentLineIndex++;
    }

    public void resetDialogue() {
        currentLineIndex = 0;
    }
}
