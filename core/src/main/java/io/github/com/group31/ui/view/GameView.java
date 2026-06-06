package io.github.com.group31.ui.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Scaling;
import com.github.tommyettinger.textra.TextraLabel;
import com.github.tommyettinger.textra.TypingLabel;
import io.github.com.group31.asset.AssetService;
import io.github.com.group31.asset.AtlasAsset;
import io.github.com.group31.ui.model.GameViewModel;
import io.github.com.group31.ui.view.DialogueBox;

import java.util.HashMap;
import java.util.Map;

public class GameView extends View<GameViewModel> implements Disposable {
    private final HorizontalGroup lifeGroup;
    private ProgressBar xpBar;
    private Label levelLabel;
    private final DialogueBox dialogueBox;

    // Cache faceset textures theo đường dẫn – lazy load khi cần
    private final HashMap<String, Texture> facesetCache = new HashMap<>();

    // Inventory HUD elements (top-right)
    private Image potionImage;
    private Image coinImage;
    private Image keyImage;
    private Label potionLabel;
    private Label coinLabel;
    private Label keyLabel;

    // Atlas để lấy icon item
    private final AssetService assetService;

    public GameView(Stage stage, Skin skin, GameViewModel viewModel, AssetService assetService) {
        super(stage, skin, viewModel);
        this.assetService = assetService;


        this.dialogueBox = new DialogueBox(skin);
        this.lifeGroup = findActor("lifeGroup");
        updateLife(viewModel.getLifePoints());
        updateXp(viewModel.getXp());
        updateLevel(viewModel.getLevel());
        updateInventoryLabels(viewModel.getPotions(), viewModel.getCoins(), viewModel.getKeys());
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(GameViewModel.LIFE_POINTS, Integer.class, this::updateLife);
        viewModel.onPropertyChange(GameViewModel.PLAYER_DAMAGE, Map.Entry.class, this::showDamage);
        viewModel.onPropertyChange(GameViewModel.FLOATING_TEXT, Map.Entry.class, this::showFloatingText);
        viewModel.onPropertyChange(GameViewModel.XP_CHANGED, Float.class, this::updateXp);
        viewModel.onPropertyChange(GameViewModel.LEVEL_CHANGED, Integer.class, this::updateLevel);
        viewModel.onPropertyChange(GameViewModel.INVENTORY_CHANGED, int[].class, counts -> {
            updateInventoryLabels(counts[0], counts[1], counts[2]);
        });
        viewModel.onPropertyChange(GameViewModel.DIALOGUE_CHANGED, String[].class, this::updateDialogue);
    }

    private void updateDialogue(String[] data) {
        if (data != null) {
            if (dialogueBox.getParent() == null) {
                stage.addActor(dialogueBox);
            }
            // data[2] = facesetPath từ NPC property trong TMX
            String facesetPath = data.length > 2 ? data[2] : "";
            Texture faceset = loadFaceset(facesetPath);
            dialogueBox.show(data[0], data[1], faceset);
        } else {
            dialogueBox.remove();
        }
    }

    /**
     * Lazy-load faceset texture theo đường dẫn lưu trong NPC property.
     * Kết quả được cache – mỗi ảnh chỉ load 1 lần.
     *
     * @param path Đường dẫn tương đối từ assets/ (vd: "ui/monk_faceset.png"), hoặc rỗng/null.
     * @return Texture tương ứng, hoặc null nếu không có.
     */
    private Texture loadFaceset(String path) {
        if (path == null || path.isBlank()) return null;
        return facesetCache.computeIfAbsent(path, p -> {
            Texture tex = new Texture(Gdx.files.internal(p));
            tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            return tex;
        });
    }

    @Override
    protected void setupUI() {
        setFillParent(true);

        // ── Bottom-left: life hearts ──
        Table bottomLeft = new Table();
        bottomLeft.align(Align.bottomLeft);

        HorizontalGroup horizontalGroup = new HorizontalGroup();
        horizontalGroup.setName("lifeGroup");
        horizontalGroup.padLeft(5.0f);
        horizontalGroup.padBottom(5.0f);
        horizontalGroup.space(5.0f);
        bottomLeft.add(horizontalGroup).left().row();

        // ── XP bar row ──
        Table xpRow = new Table();
        levelLabel = new Label("Lv.1", skin, "small");
        levelLabel.setName("levelLabel");
        xpRow.add(levelLabel).padLeft(5f).padBottom(3f);

        xpBar = new ProgressBar(0f, 100f, 0.5f, false, skin);
        xpBar.setName("xpBar");
        xpRow.add(xpBar).width(80f).padLeft(4f).padBottom(3f);
        bottomLeft.add(xpRow).left();

        add(bottomLeft).expand().align(Align.bottomLeft);

        // ── Top-right: inventory HUD (icon + "x N") ──
        // Atlas chưa load xong khi setupUI() gọi lần đầu → dùng placeholder,
        // icon thực sẽ được gắn sau trong constructor khi atlas đã sẵn sàng.
        Table topRight = new Table();
        topRight.setName("inventoryPanel");
        topRight.pad(4f).padRight(6f);

        potionImage = new Image();
        potionLabel = new Label("x0", skin, "tiny");

        coinImage = new Image();
        coinLabel = new Label("x0", skin, "tiny");

        keyImage = new Image();
        keyLabel = new Label("x0", skin, "tiny");

        // Add to table with explicit sizes to prevent the layouts from expanding the icons
        topRight.add(potionImage).size(10f, 10f);
        topRight.add(potionLabel).padLeft(2f);
        topRight.add(coinImage).size(10f, 10f).padLeft(6f);
        topRight.add(coinLabel).padLeft(2f);
        topRight.add(keyImage).size(10f, 10f).padLeft(6f);
        topRight.add(keyLabel).padLeft(2f);

        add(topRight).expand().align(Align.topRight);
    }

    /**
     * Được gọi từ constructor sau khi atlas sẵn sàng — thay thế Image placeholder bằng icon thực.
     */
    private void buildInventoryIcons() {
        TextureAtlas atlas = assetService.get(AtlasAsset.OBJECTS);
        if (atlas == null) return;

        TextureRegion potionRegion = atlas.findRegion("potion_health/potion_health");
        TextureRegion coinRegion   = atlas.findRegion("coin/coin");
        TextureRegion keyRegion    = atlas.findRegion("key/key");

        if (potionRegion != null && potionImage != null) {
            potionImage.setDrawable(new TextureRegionDrawable(potionRegion));
        }
        if (coinRegion != null && coinImage != null) {
            coinImage.setDrawable(new TextureRegionDrawable(coinRegion));
        }
        if (keyRegion != null && keyImage != null) {
            keyImage.setDrawable(new TextureRegionDrawable(keyRegion));
        }
    }

    // ── Life ──────────────────────────────────────────────────────────────────

    private void updateLife(int lifePoints) {
        lifeGroup.clear();

        int maxLife = viewModel.getMaxLife();
        while (maxLife > 0) {
            int imgIdx = MathUtils.clamp(lifePoints, 0, 4);
            Image image = new Image(skin, "life_0" + imgIdx);
            lifeGroup.addActor(image);

            maxLife -= 4;
            lifePoints -= 4;
        }
    }

    // ── XP / Level ────────────────────────────────────────────────────────────

    private void updateXp(float xp) {
        if (xpBar == null) return;
        float ratio = viewModel.getXpToNextLevel() > 0
            ? (xp / viewModel.getXpToNextLevel()) * 100f
            : 0f;
        xpBar.setValue(ratio);
    }

    private void updateLevel(int level) {
        if (levelLabel == null) return;
        levelLabel.setText("Lv." + level);
    }

    // ── Inventory ─────────────────────────────────────────────────────────────

    private void updateInventoryLabels(int potions, int coins, int keys) {
        // Lần đầu cập nhật: thử gắn icon từ atlas (atlas đã load sau khi game bắt đầu)
        buildInventoryIcons();

        if (potionLabel != null) potionLabel.setText("x" + potions);
        if (coinLabel   != null) coinLabel.setText("x" + coins);
        if (keyLabel    != null) keyLabel.setText("x" + keys);
    }

    // ── Coordinate helper ─────────────────────────────────────────────────────

    private Vector2 toStageCoords(Vector2 gamePosition) {
        Vector2 resultPosition = viewModel.toScreenCoords(gamePosition);
        stage.getViewport().unproject(resultPosition);
        resultPosition.y = stage.getViewport().getWorldHeight() - resultPosition.y;
        return resultPosition;
    }

    // ── Floating text ─────────────────────────────────────────────────────────

    private void showDamage(Map.Entry<Vector2, Integer> damAndPos) {
        final Vector2 position = damAndPos.getKey();
        int damage = damAndPos.getValue();

        TextraLabel textraLabel = new TypingLabel("[%75]{JUMP=2.0;0.5;0.9}{RAINBOW}" + damage, skin, "small");
        stage.addActor(textraLabel);

        textraLabel.addAction(
            Actions.parallel(
                Actions.sequence(Actions.delay(1.25f), Actions.removeActor()),
                Actions.forever(Actions.run(() -> {
                    Vector2 stageCoords = toStageCoords(position);
                    textraLabel.setPosition(stageCoords.x, stageCoords.y);
                }))
            )
        );
    }

    private void showFloatingText(Map.Entry<Vector2, String> textAndPos) {
        final Vector2 position = textAndPos.getKey();
        String text = textAndPos.getValue();

        TextraLabel textraLabel = new TypingLabel("[%75]{JUMP=1.5;0.4;0.8}" + text, skin, "small");
        stage.addActor(textraLabel);

        textraLabel.addAction(
            Actions.parallel(
                Actions.sequence(Actions.delay(1.0f), Actions.removeActor()),
                Actions.forever(Actions.run(() -> {
                    Vector2 stageCoords = toStageCoords(position);
                    textraLabel.setPosition(stageCoords.x, stageCoords.y);
                }))
            )
        );
    }

    /** Giải phóng tất cả faceset texture đã cache và nền dialogue khi screen bị hủy. */
    @Override
    public void dispose() {
        facesetCache.values().forEach(Texture::dispose);
        facesetCache.clear();
        dialogueBox.dispose();
    }
}
