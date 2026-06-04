package io.github.com.group31.ui.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.TypingLabel;

public class DialogueBox extends Table {
    private final Label nameLabel;
    private final Table textTable;
    private TypingLabel dialogueLabel;
    private final Skin skin;

    public DialogueBox(Skin skin) {
        super(skin);
        this.skin = skin;

        // Bọc hội thoại bằng hình nền frame có sẵn trong skin
        setBackground(skin.getDrawable("frame"));
        pad(8f);
        align(Align.topLeft);

        // Label hiển thị tên NPC
        nameLabel = new Label("", skin);
        nameLabel.setColor(skin.getColor("sand") != null ? skin.getColor("sand") : Color.YELLOW);
        add(nameLabel).left().padBottom(3f).row();

        // Một Table chứa riêng dialogue label để dễ thay thế/reset hiệu ứng TypingLabel
        textTable = new Table();
        textTable.align(Align.topLeft);
        add(textTable).expand().fill().left();

        // Kích thước của hộp thoại tương đối trên màn hình 320x180
        setSize(300f, 50f);
        // Vị trí mặc định ở giữa dưới
        setPosition(10f, 5f);
    }

    public void show(String npcName, String text) {
        nameLabel.setText(npcName);

        // Reset TypingLabel cũ để chữ chạy lại từ đầu
        textTable.clearChildren();
        
        // Hỗ trợ tốc độ chạy chữ bằng TypingLabel của Textra
        dialogueLabel = new TypingLabel("{SPEED=0.03}" + text, skin, "tiny");
        dialogueLabel.setWrap(true);
        dialogueLabel.setAlignment(Align.topLeft);
        
        textTable.add(dialogueLabel).expandX().fillX().left().top();
    }
}
