package io.github.com.group31.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Npc implements Component {
    public static final ComponentMapper<Npc> MAPPER = ComponentMapper.getFor(Npc.class);

    private final String name;
    private final String[] dialogue;
    /** Đường dẫn tương đối đến ảnh faceset (trong assets/), null nếu không có. */
    private final String facesetPath;
    private int currentLineIndex;

    public Npc(String name, String[] dialogue, String facesetPath) {
        this.name = name;
        this.dialogue = dialogue != null ? dialogue : new String[0];
        this.facesetPath = facesetPath;
        this.currentLineIndex = 0;
    }

    /** Backward-compat: không có faceset. */
    public Npc(String name, String[] dialogue) {
        this(name, dialogue, null);
    }

    public String getName() {
        return name;
    }

    public String[] getDialogue() {
        return dialogue;
    }

    /** Trả về đường dẫn ảnh faceset (tương đối từ assets/), hoặc null nếu không có. */
    public String getFacesetPath() {
        return facesetPath;
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
