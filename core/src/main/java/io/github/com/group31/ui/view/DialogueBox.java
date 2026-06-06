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
 * Hộp thoại NPC dùng DialogBoxFaceset.png (Ninja Adventure Pack) làm nền 9-patch.
 * Layout: [Faceset 38×38] | [Tên NPC / Nội dung thoại]
 *
 * BUG FIX: TypingLabel.setWrap(true) yêu cầu cell có width xác định TRƯỚC khi layout.
 * Giải pháp: thêm TypingLabel trực tiếp vào right column với width=0 grow,
 * và gọi invalidateHierarchy() sau khi add để buộc Table tính lại layout.
 */
public class DialogueBox extends Table implements Disposable {

    // DialogBoxFaceset.png = 300×58 px
    // Ô faceset đen nằm ở góc trái, chiếm khoảng 50×50 px (tính theo tỉ lệ ảnh gốc)
    // Sau khi scale xuống UI world 320×180: chiều cao box ~48px → scale = 48/58 ≈ 0.83
    // → faceset slot thực tế ≈ 50 * 0.83 ≈ 41px → dùng 38px (khớp monk_faceset 38×38)
    private static final int   PATCH_LEFT   = 6;
    private static final int   PATCH_RIGHT  = 6;
    private static final int   PATCH_TOP    = 6;
    private static final int   PATCH_BOTTOM = 6;

    // Kích thước slot faceset trong hộp thoại (pixel UI)
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

        // ── Nền 9-patch từ DialogBoxFaceset.png ───────────────────────────
        dialogBgTexture = new Texture(Gdx.files.internal("ui/DialogBoxFaceset.png"));
        dialogBgTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        NinePatch patch = new NinePatch(
            new TextureRegion(dialogBgTexture),
            PATCH_LEFT, PATCH_RIGHT, PATCH_TOP, PATCH_BOTTOM
        );
        setBackground(new NinePatchDrawable(patch));

        // Padding bên trong box (top, left, bottom, right)
        pad(5f, 5f, 5f, 6f);

        // ── Cột trái: ảnh faceset ──────────────────────────────────────────
        facesetImage = new Image();
        // Không dùng Scaling.fit để tránh bị scale nhỏ lại – dùng kích thước cố định
        facesetImage.setScaling(com.badlogic.gdx.utils.Scaling.stretch);

        // ── Cột phải: table chứa tên + text ───────────────────────────────
        Table rightCol = new Table();
        rightCol.align(Align.topLeft);

        // Tên NPC
        nameLabel = new Label("", skin, "small");
        nameLabel.setColor(skin.getColor("sand") != null ? skin.getColor("sand") : Color.YELLOW);
        rightCol.add(nameLabel).left().padLeft(6f).padTop(4f).padBottom(1f).row();

        // Placeholder cho TypingLabel – sẽ được thêm vào trong show()
        // (dùng Label rỗng để giữ row)
        dialogueLabel = new TypingLabel("", skin, "tiny");
        dialogueLabel.setWrap(true);
        dialogueLabel.setAlignment(Align.topLeft);
        dialogueLabelCell = rightCol.add(dialogueLabel).growX().left().top().padLeft(6f);

        // ── Ghép layout chính ─────────────────────────────────────────────
        add(facesetImage).size(FACESET_W, FACESET_H).padRight(5f).left().top();
        add(rightCol).growX().fillY().left().top();

        // ── Kích thước tổng của hộp thoại (UI world 320×180) ──────────────
        // width: hầu hết màn hình, để lại lề 2 bên
        // height: đủ chứa faceset + tên + 2 dòng text
        setSize(300f, 52f);
        setPosition(10f, 4f);
    }

    /**
     * Hiển thị dialogue với faceset tùy chọn.
     *
     * @param npcName        Tên NPC.
     * @param text           Nội dung thoại.
     * @param facesetTexture Texture avatar NPC, hoặc null nếu không có.
     */
    public void show(String npcName, String text, Texture facesetTexture) {
        // Cập nhật tên
        nameLabel.setText(npcName);

        // Cập nhật avatar
        if (facesetTexture != null) {
            facesetImage.setDrawable(new TextureRegionDrawable(new TextureRegion(facesetTexture)));
            facesetImage.setVisible(true);
        } else {
            facesetImage.setDrawable(null);
            facesetImage.setVisible(false);
        }

        // Tạo TypingLabel mới (để reset hiệu ứng gõ chữ từ đầu)
        // Dùng {FAST} + SPEED thấp để chữ gõ ra từng ký tự
        dialogueLabel = new TypingLabel("{SPEED=0.5}" + text, skin, "tiny");
        dialogueLabel.setWrap(true);
        dialogueLabel.setAlignment(Align.topLeft);

        // Cập nhật cell với label mới – giữ nguyên constraint growX
        dialogueLabelCell.setActor(dialogueLabel);

        // Buộc Table recompute layout để TypingLabel biết preferred width
        invalidateHierarchy();
    }

    /** Overload không cần faceset. */
    public void show(String npcName, String text) {
        show(npcName, text, null);
    }

    @Override
    public void dispose() {
        dialogBgTexture.dispose();
    }
}
