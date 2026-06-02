package io.github.com.group31.ui.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import io.github.com.group31.ui.model.GameOverViewModel;

public class GameOverView extends View<GameOverViewModel> {
    private final Image selectionImg;
    private Group selectedItem;

    public GameOverView(Stage stage, Skin skin, GameOverViewModel viewModel) {
        super(stage, skin, viewModel);
        this.selectionImg = new Image(skin.getDrawable("selection"));
        this.selectionImg.setTouchable(Touchable.disabled);

        // Delay selection by one frame so layout is validated and button sizes are known
        addAction(Actions.run(() -> {
            Group retryBtn = findActor(GameOverOption.RETRY.name());
            if (retryBtn != null) {
                this.selectedItem = retryBtn;
                selectMenuItem(retryBtn);
            }
        }));
    }

    /**
     * Selects a menu item and animates the selection indicator.
     */
    private void selectMenuItem(Group menuItem) {
        if (selectionImg.getParent() != null) {
            selectionImg.getParent().removeActor(selectionImg);
        }
        this.selectedItem = menuItem;

        float extraSize = 7f;
        float halfExtraSize = extraSize * 0.5f;
        float resizeTime = 0.2f;

        menuItem.addActor(selectionImg);
        selectionImg.setPosition(-halfExtraSize, -halfExtraSize);
        selectionImg.setSize(menuItem.getWidth() + extraSize, menuItem.getHeight() + extraSize);
        selectionImg.clearActions();
        selectionImg.addAction(Actions.forever(Actions.sequence(
            Actions.parallel(
                Actions.sizeBy(extraSize, extraSize, resizeTime, Interpolation.linear),
                Actions.moveBy(-halfExtraSize, -halfExtraSize, resizeTime, Interpolation.linear)
            ),
            Actions.parallel(
                Actions.sizeBy(-extraSize, -extraSize, resizeTime, Interpolation.linear),
                Actions.moveBy(halfExtraSize, halfExtraSize, resizeTime, Interpolation.linear)
            )
        )));
    }

    @Override
    protected void setupUI() {
        setFillParent(true);
        align(Align.center);

        // Title
        Label titleLabel = new Label("GAME OVER", skin);
        titleLabel.setFontScale(2.5f);
        titleLabel.setColor(Color.RED);
        titleLabel.addAction(Actions.forever(Actions.sequence(
            Actions.color(Color.RED, 0.6f, Interpolation.sine),
            Actions.color(Color.valueOf("ff5252"), 0.6f, Interpolation.sine)
        )));
        add(titleLabel).padBottom(10f).row();

        // Subtitle
        Label subtitleLabel = new Label("Your journey has ended...", skin, "small");
        subtitleLabel.setColor(skin.getColor("sand"));
        subtitleLabel.getColor().a = 0f;
        subtitleLabel.addAction(Actions.sequence(
            Actions.delay(0.3f),
            Actions.fadeIn(0.5f)
        ));
        add(subtitleLabel).padBottom(30f).row();

        // Buttons table with frame background
        Table buttonTable = new Table(skin);
        buttonTable.setBackground(skin.getDrawable("frame"));
        buttonTable.padLeft(40.0f);
        buttonTable.padRight(40.0f);
        buttonTable.padTop(25.0f);
        buttonTable.padBottom(20.0f);
        buttonTable.defaults().padBottom(10f).minWidth(160f);

        TextButton retryBtn = new TextButton("Retry", skin);
        retryBtn.setName(GameOverOption.RETRY.name());
        onClick(retryBtn, viewModel::retry);
        onEnter(retryBtn, this::selectMenuItem);
        buttonTable.add(retryBtn).row();

        TextButton menuBtn = new TextButton("Main Menu", skin);
        menuBtn.setName(GameOverOption.MAIN_MENU.name());
        onClick(menuBtn, viewModel::backToMenu);
        onEnter(menuBtn, this::selectMenuItem);
        buttonTable.add(menuBtn).row();

        TextButton quitBtn = new TextButton("Quit Game", skin);
        quitBtn.setName(GameOverOption.QUIT.name());
        onClick(quitBtn, viewModel::quitGame);
        onEnter(quitBtn, this::selectMenuItem);
        buttonTable.add(quitBtn).padBottom(0f).row(); // Reset padding for last button

        // Animate button table fade-in
        buttonTable.getColor().a = 0f;
        buttonTable.addAction(Actions.sequence(
            Actions.delay(0.5f),
            Actions.fadeIn(0.8f)
        ));

        add(buttonTable);
    }

    @Override
    public void onDown() {
        if (this.selectedItem == null) return;
        Group menuContentTable = this.selectedItem.getParent();
        int currentIdx = menuContentTable.getChildren().indexOf(this.selectedItem, true);
        if (currentIdx == -1) {
            throw new GdxRuntimeException("'selectedItem' is not a child of 'menuContentTable'");
        }

        int numOptions = menuContentTable.getChildren().size;
        currentIdx = (currentIdx + 1) % numOptions;
        selectMenuItem((Group) menuContentTable.getChild(currentIdx));
    }

    @Override
    public void onUp() {
        if (this.selectedItem == null) return;
        Group menuContentTable = this.selectedItem.getParent();
        int currentIdx = menuContentTable.getChildren().indexOf(this.selectedItem, true);
        if (currentIdx == -1) {
            throw new GdxRuntimeException("'selectedItem' is not a child of 'menuContentTable'");
        }

        int numOptions = menuContentTable.getChildren().size;
        currentIdx = currentIdx == 0 ? numOptions - 1 : currentIdx - 1;
        selectMenuItem((Group) menuContentTable.getChild(currentIdx));
    }

    @Override
    public void onSelect() {
        if (this.selectedItem == null) return;
        GameOverOption option = GameOverOption.valueOf(this.selectedItem.getName());
        switch (option) {
            case RETRY -> viewModel.retry();
            case MAIN_MENU -> viewModel.backToMenu();
            case QUIT -> viewModel.quitGame();
        }
    }

    private enum GameOverOption {
        RETRY,
        MAIN_MENU,
        QUIT
    }
}
