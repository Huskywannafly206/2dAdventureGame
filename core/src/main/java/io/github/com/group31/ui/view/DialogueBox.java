package io.github.com.group31.ui.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.textra.TypingLabel;

/**
 * NPC dialogue box with layout matching classic RPG design:
 * - Uses DialogBoxFaceset.png as the main dialog content frame (with faceset slot on left, dialogue on right).
 * - Leaves faceset slot empty if NPC has no faceset.
 * - Displays character name inside DialogInfo.png nameplate, placed directly above the dialog frame.
 */
public class DialogueBox extends Table implements Disposable {

    private static final float FACESET_W = 38f;
    private static final float FACESET_H = 38f;

    private final Skin skin;
    private final Texture dialogBgTexture;

    private final Image    facesetImage;
    private final Label    nameLabel;
    private Cell<TypingLabel> dialogueLabelCell;
    private TypingLabel    dialogueLabel;

    public DialogueBox(Skin skin) {
        super(skin);
        this.skin = skin;

        // Load background texture
        dialogBgTexture = new Texture(Gdx.files.internal("ui/DialogBoxFaceset.png"));
        dialogBgTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Set background of DialogueBox to DialogBoxFaceset.png directly
        setBackground(new TextureRegionDrawable(new TextureRegion(dialogBgTexture)));

        // Name plate label: aligned inside the brown rectangle of DialogBoxFaceset.png
        // Brown rectangle is x=3 to x=70 (width 67), y=48 to y=58 (height 10) in LibGDX layout
        nameLabel = new Label("", skin, "tiny"); // Using tiny (size 8 font) to fit in 10px height
        nameLabel.setColor(skin.getColor("sand") != null ? skin.getColor("sand") : Color.YELLOW);
        nameLabel.setAlignment(Align.center);

        // Faceset Image
        facesetImage = new Image();
        facesetImage.setScaling(com.badlogic.gdx.utils.Scaling.stretch);

        Table facesetContainer = new Table();
        facesetContainer.add(facesetImage).size(FACESET_W, FACESET_H).center();

        // Dialogue text area
        Table rightCol = new Table();
        rightCol.align(Align.topLeft);
        rightCol.pad(6f, 6f, 6f, 6f); // inner pad to prevent text touching borders

        dialogueLabel = new TypingLabel("", skin, "tiny");
        dialogueLabel.setWrap(true);
        dialogueLabel.setAlignment(Align.topLeft);
        dialogueLabelCell = rightCol.add(dialogueLabel).growX().left().top();

        // Construct layout inside DialogueBox
        // Row 1: Name Label (spans both columns, aligned to the brown rectangle at top-left)
        add(nameLabel).width(68f).height(10f).left().padLeft(4f).padTop(1f).fill().colspan(2).row();

        // Row 2: Faceset (left) and Dialogue text (right)
        add(facesetContainer).width(50f).height(48f).left().bottom();
        add(rightCol).width(250f).height(48f).left().top();

        // Total layout size and positioning in 320x180 world coords
        setSize(300f, 58f);
        setPosition(10f, 4f);
    }

    /**
     * Show dialogue box with name, text, and faceset.
     */
    public void show(String npcName, String text, Texture facesetTexture) {
        // Update name
        // Update name with dynamic scaling if it exceeds the brown rectangle width
        nameLabel.setFontScale(1f);
        nameLabel.setText(npcName != null ? npcName : "");
        if (npcName == null || npcName.isBlank()) {
            nameLabel.setVisible(false);
        } else {
            nameLabel.setVisible(true);
            float prefWidth = nameLabel.getPrefWidth();
            float maxWidth = 64f; // Fit inside the 68f cell with padding
            if (prefWidth > maxWidth) {
                float scale = maxWidth / prefWidth;
                nameLabel.setFontScale(scale);
            }
        }

        // Update faceset
        if (facesetTexture != null) {
            facesetImage.setDrawable(new TextureRegionDrawable(new TextureRegion(facesetTexture)));
            facesetImage.setVisible(true);
        } else {
            facesetImage.setDrawable(null);
            facesetImage.setVisible(false);
        }

        // Reset label to trigger typing effect
        dialogueLabel = new TypingLabel("{SPEED=0.5}" + text, skin, "tiny");
        dialogueLabel.setWrap(true);
        dialogueLabel.setAlignment(Align.topLeft);

        dialogueLabelCell.setActor(dialogueLabel);

        invalidateHierarchy();
    }

    /** Overload helper for missing faceset. */
    public void show(String npcName, String text) {
        show(npcName, text, null);
    }

    public TypingLabel getDialogueLabel() {
        return dialogueLabel;
    }

    public void skipToTheEnd() {
        if (dialogueLabel != null) {
            dialogueLabel.skipToTheEnd();
        }
    }

    @Override
    public void dispose() {
        dialogBgTexture.dispose();
    }
}
