package io.github.com.group31.ui.view;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import io.github.com.group31.ui.model.GameOverViewModel;

public class GameOverView extends View<GameOverViewModel> {

    public GameOverView(Stage stage, Skin skin, GameOverViewModel viewModel) {
        super(stage, skin, viewModel);
    }

    @Override
    protected void setupUI() {
        setFillParent(true);
        align(Align.center);

        // Title
        Label titleLabel = new Label("GAME OVER", skin);
        titleLabel.setFontScale(2f);
        add(titleLabel).padBottom(30f).row();

        // Buttons in a sub-table with background frame
        Table buttonTable = new Table(skin);
        buttonTable.setBackground(skin.getDrawable("frame"));
        buttonTable.pad(20f);
        buttonTable.defaults().padTop(10f).minWidth(150f);

        TextButton retryBtn = new TextButton("Retry", skin);
        retryBtn.setName("retryButton");
        onClick(retryBtn, viewModel::retry);
        buttonTable.add(retryBtn).row();

        TextButton menuBtn = new TextButton("Main Menu", skin);
        menuBtn.setName("menuButton");
        onClick(menuBtn, viewModel::backToMenu);
        buttonTable.add(menuBtn).row();

        TextButton quitBtn = new TextButton("Quit Game", skin);
        quitBtn.setName("quitButton");
        onClick(quitBtn, viewModel::quitGame);
        buttonTable.add(quitBtn).row();

        add(buttonTable);
    }
}
