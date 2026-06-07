package io.github.com.group31.ui.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
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
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Scaling;
import com.github.tommyettinger.textra.TextraLabel;
import com.github.tommyettinger.textra.TypingLabel;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.com.group31.asset.AssetService;
import io.github.com.group31.asset.AtlasAsset;
import io.github.com.group31.ui.model.GameViewModel;
import io.github.com.group31.ui.view.DialogueBox;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import io.github.com.group31.component.Transform;

import java.util.HashMap;
import java.util.Map;

public class GameView extends View<GameViewModel> implements Disposable {
    private final HorizontalGroup lifeGroup;
    private ProgressBar xpBar;
    private Label levelLabel;
    private Label weaponLabel;
    private final DialogueBox dialogueBox;
    
    // Quest Panel Background
    private Texture questBgTexture;

    // Dynamically drawn inventory slot background
    private Texture slotTexture;
    private TextureRegionDrawable slotDrawable;

    /**
     * Cache texture faceset theo đường dẫn — lazy-load khi NPC xuất hiện lần đầu.
     * Key = đường dẫn tương đối assets/ (vd: "ui/monk_faceset.png").
     * Tất cả texture được dispose() trong dispose().
     */
    private final HashMap<String, Texture> facesetCache = new HashMap<>();

    // Tabbed Menu Overlay
    private Table menuContainer;
    private final Label[] scarecrowLabels = new Label[4];

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
        updateWeaponLabel(viewModel.getCurrentWeaponName());

        setupMenuContainer();
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(GameViewModel.LIFE_POINTS, Integer.class, this::updateLife);
        viewModel.onPropertyChange(GameViewModel.PLAYER_DAMAGE, Map.Entry.class, this::showDamage);
        viewModel.onPropertyChange(GameViewModel.FLOATING_TEXT, Map.Entry.class, this::showFloatingText);
        viewModel.onPropertyChange(GameViewModel.XP_CHANGED, Float.class, this::updateXp);
        viewModel.onPropertyChange(GameViewModel.LEVEL_CHANGED, Integer.class, this::updateLevel);
        viewModel.onPropertyChange(GameViewModel.SCARECROW_HITS_CHANGED, int[].class, indexAndHits -> {
            if (indexAndHits != null && indexAndHits.length >= 2) {
                int index = indexAndHits[0];
                int hits = indexAndHits[1];
                if (index >= 1 && index <= 3 && scarecrowLabels[index] != null) {
                    scarecrowLabels[index].setText(hits + "/" + index);
                    scarecrowLabels[index].addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.color(Color.RED),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.color(Color.YELLOW, 0.5f)
                    ));
                }
            }
        });
        viewModel.onPropertyChange(GameViewModel.INVENTORY_CHANGED, int[].class, counts -> {
            if (menuContainer != null && menuContainer.isVisible()) {
                com.badlogic.gdx.Gdx.app.postRunnable(this::rebuildMenu);
            }
        });
        viewModel.onPropertyChange(GameViewModel.DIALOGUE_CHANGED, String[].class, this::updateDialogue);
        viewModel.onPropertyChange(GameViewModel.SKIP_DIALOGUE_TYPING, Boolean.class, skip -> {
            if (skip != null && skip && dialogueBox != null) {
                dialogueBox.skipToTheEnd();
            }
        });
        viewModel.onPropertyChange(GameViewModel.WEAPON_CHANGED, String.class, this::updateWeaponLabel);
        viewModel.onPropertyChange(GameViewModel.QUEST_CHANGED, String[].class, questInfo -> {
            if (menuContainer != null && menuContainer.isVisible()) {
                com.badlogic.gdx.Gdx.app.postRunnable(this::rebuildMenu);
            }
        });
        viewModel.onPropertyChange(GameViewModel.MENU_TOGGLED, Boolean.class, this::setMenuVisible);
        viewModel.onPropertyChange(GameViewModel.TAB_CHANGED, Integer.class, tab -> {
            if (menuContainer != null && menuContainer.isVisible()) {
                com.badlogic.gdx.Gdx.app.postRunnable(this::rebuildMenu);
            }
        });
        viewModel.onPropertyChange(GameViewModel.UNLOCKED_WEAPONS_CHANGED, java.util.List.class, weapons -> {
            if (menuContainer != null && menuContainer.isVisible()) {
                com.badlogic.gdx.Gdx.app.postRunnable(this::rebuildMenu);
            }
        });
        viewModel.onPropertyChange(GameViewModel.SELECTED_ITEM_CHANGED, io.github.com.group31.component.Item.Type.class, type -> {
            if (menuContainer != null && menuContainer.isVisible()) {
                com.badlogic.gdx.Gdx.app.postRunnable(this::rebuildMenu);
            }
        });
    }

    private void updateDialogue(String[] data) {
        if (data != null) {
            if (dialogueBox.getParent() == null) {
                stage.addActor(dialogueBox);
            }
            // data[2] là facesetPath từ TMX property (rỗng = không có avatar)
            String facesetPath = (data.length > 2 && !data[2].isBlank()) ? data[2] : null;
            Texture faceset = loadFaceset(facesetPath);
            dialogueBox.show(data[0], data[1], faceset);
            if (dialogueBox.getDialogueLabel() != null) {
                dialogueBox.getDialogueLabel().setTypingListener(new com.github.tommyettinger.textra.TypingAdapter() {
                    @Override
                    public void end() {
                        viewModel.setDialogueTyping(false);
                    }
                });
            }
        } else {
            dialogueBox.remove();
        }
    }

    /**
     * Lazy-load và cache texture faceset theo đường dẫn assets/.
     *
     * @param path Đường dẫn tương đối, ví dụ "ui/monk_faceset.png". Null → trả về null.
     * @return Texture đã load và lọc Nearest, hoặc null nếu path null/không tìm thấy.
     */
    private Texture loadFaceset(String path) {
        if (path == null || path.isBlank()) return null;
        return facesetCache.computeIfAbsent(path, p -> {
            if (!Gdx.files.internal(p).exists()) {
                Gdx.app.error("GameView", "Faceset không tìm thấy: " + p);
                return null;
            }
            Texture tex = new Texture(Gdx.files.internal(p));
            tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            return tex;
        });
    }

    @Override
    protected void setupUI() {
        setFillParent(true);

        // Load background texture for dialog boxes/quest panels
        questBgTexture = new Texture(Gdx.files.internal("ui/DialogueBoxSimple.png"));
        questBgTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

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
        bottomLeft.add(xpRow).left().row();

        // ── Weapon row ──
        Table weaponRow = new Table();
        Label weaponTitle = new Label("Vũ khí: ", skin, "tiny");
        weaponLabel = new Label("Đấm", skin, "tiny");
        weaponRow.add(weaponTitle).padLeft(5f).padBottom(3f);
        weaponRow.add(weaponLabel).padBottom(3f);
        bottomLeft.add(weaponRow).left();

        add(bottomLeft).expand().align(Align.bottomLeft);
    }

    /**
     * Set up the menu container centered on the stage viewport.
     */
    private void setupMenuContainer() {
        menuContainer = new Table();
        menuContainer.setSize(260f, 140f);
        menuContainer.setPosition(
            (stage.getViewport().getWorldWidth() - menuContainer.getWidth()) / 2f,
            (stage.getViewport().getWorldHeight() - menuContainer.getHeight()) / 2f
        );
        menuContainer.setVisible(false);
        stage.addActor(menuContainer);

        // Initialize dynamically drawn slot background
        slotDrawable = createSlotDrawable(24);
    }

    /**
     * Dynamically creates a pixel-perfect 24x24 slot background using Pixmap.
     */
    private TextureRegionDrawable createSlotDrawable(int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        
        // Fill base dark background
        pixmap.setColor(0.145f, 0.122f, 0.110f, 1.0f);
        pixmap.fill();

        // Sunken outer border
        // Dark top and left borders
        pixmap.setColor(0.082f, 0.067f, 0.055f, 1.0f);
        pixmap.drawLine(0, 0, 0, size - 1);
        pixmap.drawLine(0, 0, size - 1, 0);

        // Light bottom and right borders
        pixmap.setColor(0.388f, 0.318f, 0.278f, 1.0f);
        pixmap.drawLine(0, size - 1, size - 1, size - 1);
        pixmap.drawLine(size - 1, 0, size - 1, size - 1);

        // Inner shadow/bevel for extra depth
        pixmap.setColor(0.05f, 0.04f, 0.03f, 1.0f);
        pixmap.drawLine(1, 1, 1, size - 2);
        pixmap.drawLine(1, 1, size - 2, 1);

        pixmap.setColor(0.25f, 0.20f, 0.17f, 1.0f);
        pixmap.drawLine(1, size - 2, size - 2, size - 2);
        pixmap.drawLine(size - 2, 1, size - 2, size - 2);

        slotTexture = new Texture(pixmap);
        slotTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();

        return new TextureRegionDrawable(new TextureRegion(slotTexture));
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

    private void setMenuVisible(boolean visible) {
        if (menuContainer != null) {
            menuContainer.setVisible(visible);
            if (visible) {
                com.badlogic.gdx.Gdx.app.postRunnable(this::rebuildMenu);
            }
        }
    }

    private Table createTabButton(String text, boolean active) {
        Table tab = new Table();
        Label label = new Label(text, skin, "tiny");
        tab.add(label).pad(3f, 6f, 3f, 6f);

        NinePatch patch = new NinePatch(skin.getPatch("button"));
        if (active) {
            patch.setColor(Color.WHITE);
        } else {
            patch.setColor(new Color(0.5f, 0.45f, 0.4f, 1f));
        }
        tab.setBackground(new NinePatchDrawable(patch));
        return tab;
    }

    private void rebuildMenu() {
        if (menuContainer == null) return;
        menuContainer.clear();

        Table tabRow = new Table();
        tabRow.align(Align.left);

        Table invTab = createTabButton("INVENTORY", viewModel.getCurrentTab() == 0);
        invTab.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                viewModel.setCurrentTab(0);
            }
        });
        tabRow.add(invTab).padRight(2f);

        Table questTab = createTabButton("QUEST", viewModel.getCurrentTab() == 1);
        questTab.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                viewModel.setCurrentTab(1);
            }
        });
        tabRow.add(questTab).padRight(2f);

        Table mapTab = createTabButton("MAP", viewModel.getCurrentTab() == 2);
        mapTab.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                viewModel.setCurrentTab(2);
            }
        });
        tabRow.add(mapTab);

        menuContainer.add(tabRow).left().padLeft(10f).row();

        Table contentArea = new Table();
        NinePatch patch = new NinePatch(
            new TextureRegion(questBgTexture),
            6, 6, 6, 6
        );
        contentArea.setBackground(new NinePatchDrawable(patch));
        contentArea.pad(6f);

        Table tabContent = switch (viewModel.getCurrentTab()) {
            case 0 -> buildInventoryContent();
            case 1 -> buildQuestContent();
            case 2 -> buildMapContent();
            default -> new Table();
        };
        contentArea.add(tabContent).expand().fill();

        menuContainer.add(contentArea).width(260f).height(118f).padTop(-1f);
    }

    private Table buildInventoryContent() {
        Table content = new Table();
        content.align(Align.center);

        Table grid = new Table();
        grid.align(Align.center);

        class InvItem {
            TextureRegion region;
            int count;
            io.github.com.group31.component.Item.Type type;
            InvItem(TextureRegion region, int count, io.github.com.group31.component.Item.Type type) {
                this.region = region;
                this.count = count;
                this.type = type;
            }
        }
        java.util.List<InvItem> items = new java.util.ArrayList<>();

        TextureAtlas atlas = assetService.get(AtlasAsset.OBJECTS);
        if (atlas != null) {
            if (viewModel.getPotions() > 0) {
                items.add(new InvItem(atlas.findRegion("potion_health/potion_health"), viewModel.getPotions(), io.github.com.group31.component.Item.Type.POTION_HEALTH));
            }
            if (viewModel.getHeartContainers() > 0) {
                items.add(new InvItem(atlas.findRegion("heart_container/heart_container"), viewModel.getHeartContainers(), io.github.com.group31.component.Item.Type.HEART_CONTAINER));
            }
            if (viewModel.getCoins() > 0) {
                items.add(new InvItem(atlas.findRegion("coin/coin"), viewModel.getCoins(), io.github.com.group31.component.Item.Type.COIN));
            }
            if (viewModel.getKeys() > 0) {
                items.add(new InvItem(atlas.findRegion("key/key"), viewModel.getKeys(), io.github.com.group31.component.Item.Type.KEY));
            }
            if (viewModel.getGoldKeys() > 0) {
                items.add(new InvItem(atlas.findRegion("gold_key/gold_key"), viewModel.getGoldKeys(), io.github.com.group31.component.Item.Type.GOLD_KEY));
            }
            if (viewModel.getSilverKeys() > 0) {
                items.add(new InvItem(atlas.findRegion("silver_key/silver_key"), viewModel.getSilverKeys(), io.github.com.group31.component.Item.Type.SILVER_KEY));
            }
            if (viewModel.getSoothingHerbs() > 0) {
                items.add(new InvItem(atlas.findRegion("soothing_herb/soothing_herb"), viewModel.getSoothingHerbs(), io.github.com.group31.component.Item.Type.SOOTHING_HERB));
            }
            if (viewModel.getJungleMapKeys() > 0) {
                items.add(new InvItem(atlas.findRegion("jungle_map_key/jungle_map_key"), viewModel.getJungleMapKeys(), io.github.com.group31.component.Item.Type.JUNGLE_MAP_KEY));
            }
            if (viewModel.getSilverCups() > 0) {
                items.add(new InvItem(atlas.findRegion("silver_cup/silver_cup"), viewModel.getSilverCups(), io.github.com.group31.component.Item.Type.SILVER_CUP));
            }
            for (String wName : viewModel.getUnlockedWeapons()) {
                if ("SWORD".equalsIgnoreCase(wName)) {
                    items.add(new InvItem(atlas.findRegion("weapon_sword/weapon_sword"), 1, io.github.com.group31.component.Item.Type.WEAPON_SWORD));
                } else if ("BOW".equalsIgnoreCase(wName)) {
                    items.add(new InvItem(atlas.findRegion("weapon_bow/weapon_bow"), 1, io.github.com.group31.component.Item.Type.WEAPON_BOW));
                } else if ("MAGIC_WAND".equalsIgnoreCase(wName)) {
                    items.add(new InvItem(atlas.findRegion("weapon_magicWand/weapon_magicWand"), 1, io.github.com.group31.component.Item.Type.WEAPON_MAGIC_WAND));
                } else if ("RUSTY_SWORD".equalsIgnoreCase(wName)) {
                    items.add(new InvItem(atlas.findRegion("weapon_rusty_sword/weapon_rusty_sword"), 1, io.github.com.group31.component.Item.Type.WEAPON_RUSTY_SWORD));
                }
            }
        }

        int totalSlots = 24;
        for (int i = 0; i < totalSlots; i++) {
            Table slot = new Table();
            slot.setBackground(slotDrawable);

            if (i < items.size()) {
                InvItem item = items.get(i);
                if (item.region != null) {
                    Image img = new Image(item.region);
                    img.setScaling(Scaling.fit);

                    com.badlogic.gdx.scenes.scene2d.ui.Stack stack = new com.badlogic.gdx.scenes.scene2d.ui.Stack();
                    stack.add(img);

                    if (item.count > 1) {
                        Label countLabel = new Label("" + item.count, skin, "tiny");
                        countLabel.setAlignment(Align.bottomRight);
                        Table countWrapper = new Table();
                        countWrapper.align(Align.bottomRight);
                        countWrapper.add(countLabel).padRight(1f).padBottom(1f);
                        stack.add(countWrapper);
                    }
                    slot.add(stack).size(16f, 16f).center();

                    slot.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
                    slot.addListener(new ClickListener() {
                        @Override
                        public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                            viewModel.setSelectedItemType(item.type);
                        }
                    });

                    if (viewModel.getSelectedItemType() == item.type) {
                        slot.setColor(Color.YELLOW);
                    } else {
                        slot.setColor(Color.WHITE);
                    }
                }
            } else {
                slot.add().size(16f, 16f);
                slot.setColor(Color.WHITE);
            }

            grid.add(slot).size(24f, 24f).pad(2f);
            if ((i + 1) % 8 == 0) {
                grid.row();
            }
        }

        content.add(grid).center().row();

        com.badlogic.gdx.scenes.scene2d.ui.TextButton useBtn = new com.badlogic.gdx.scenes.scene2d.ui.TextButton("USE", skin);
        io.github.com.group31.component.Item.Type selType = viewModel.getSelectedItemType();
        boolean canUse = false;
        if (selType == io.github.com.group31.component.Item.Type.POTION_HEALTH && viewModel.getPotions() > 0) canUse = true;
        if (selType == io.github.com.group31.component.Item.Type.HEART_CONTAINER && viewModel.getHeartContainers() > 0) canUse = true;
        useBtn.setDisabled(!canUse);
        if (!canUse) {
            useBtn.setColor(Color.DARK_GRAY);
        } else {
            useBtn.setColor(Color.WHITE);
        }
        useBtn.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (!useBtn.isDisabled()) {
                    viewModel.useInventoryItem();
                }
            }
        });
        
        Table btnTable = new Table();
        btnTable.add(useBtn).height(20f);
        content.add(btnTable).padTop(4f).center();

        return content;
    }

    private Table buildQuestContent() {
        Table content = new Table();
        content.align(Align.top);

        Label titleLabel = new Label("Quest Log", skin, "small");
        titleLabel.setColor(Color.WHITE);
        content.add(titleLabel).padBottom(5f).row();

        Table questBox = new Table();
        NinePatch questPatch = new NinePatch(
            new TextureRegion(questBgTexture),
            6, 6, 6, 6
        );
        questBox.setBackground(new NinePatchDrawable(questPatch));
        questBox.pad(6f);

        String[] questInfo = viewModel.getActiveQuest();
        String questTitle = (questInfo != null && !questInfo[0].isEmpty()) ? questInfo[0] : "Khong co nhiem vu";
        String questObjective = (questInfo != null && !questInfo[1].isEmpty()) ? questInfo[1] : "Chua nhan nhiem vu nao.";

        Label qTitle = new Label(questTitle, skin, "small");
        qTitle.setColor(Color.ORANGE);
        Label qObjective = new Label(questObjective, skin, "tiny");
        qObjective.setWrap(true);

        questBox.add(qTitle).left().row();
        questBox.add(qObjective).width(210f).left().padTop(2f);

        content.add(questBox).width(230f).row();
        return content;
    }

    private Table buildMapContent() {
        Table content = new Table();
        content.align(Align.center);
        Label label = new Label("Map (Chua mo khoa)", skin, "small");
        content.add(label);
        return content;
    }

    private void updateWeaponLabel(String weaponName) {
        if (weaponLabel != null) {
            weaponLabel.setText(weaponName);
        }
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

    public void clearScarecrowLabels() {
        for (int i = 1; i <= 3; i++) {
            if (scarecrowLabels[i] != null) {
                scarecrowLabels[i].remove();
                scarecrowLabels[i] = null;
            }
        }
    }

    public void setupScarecrowLabels(com.badlogic.ashley.core.Engine engine) {
        clearScarecrowLabels();

        ImmutableArray<Entity> scarecrows = engine.getEntitiesFor(
            com.badlogic.ashley.core.Family.all(io.github.com.group31.component.ScarecrowComponent.class, Transform.class).get()
        );
        for (Entity sc : scarecrows) {
            final io.github.com.group31.component.ScarecrowComponent scComp = io.github.com.group31.component.ScarecrowComponent.MAPPER.get(sc);
            if (scComp != null) {
                int index = scComp.getIndex();
                int target = index;
                int current = io.github.com.group31.puzzle.ScarecrowPuzzleManager.INSTANCE.getHits(index);

                final Label label = new Label(current + "/" + target, skin, "small");
                label.setColor(Color.YELLOW);
                stage.addActor(label);
                scarecrowLabels[index] = label;
                scComp.setLabel(label);

                label.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.forever(
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.run(() -> {
                        Transform transform = Transform.MAPPER.get(sc);
                        if (transform != null) {
                            Vector2 pos = transform.getPosition();
                            Vector2 size = transform.getSize();
                            Vector2 stageCoords = toStageCoords(new Vector2(pos.x + size.x * 0.5f, pos.y + size.y + 0.2f));
                            label.setPosition(stageCoords.x - label.getPrefWidth() * 0.5f, stageCoords.y);
                        }
                    })
                ));
            }
        }
    }

    /** Giải phóng tất cả texture faceset đã cache và nền dialogue khi screen bị hủy. */
    @Override
    public void dispose() {
        for (Texture tex : facesetCache.values()) {
            if (tex != null) tex.dispose();
        }
        facesetCache.clear();
        dialogueBox.dispose();
        if (questBgTexture != null) {
            questBgTexture.dispose();
        }
        if (slotTexture != null) {
            slotTexture.dispose();
        }
    }
}
