package io.github.com.group31.ui.model;

import com.badlogic.gdx.Gdx;
import io.github.com.group31.GdxGame;
import io.github.com.group31.screen.GameScreen;
import io.github.com.group31.screen.MenuScreen;

public class GameOverViewModel extends ViewModel {

    public GameOverViewModel(GdxGame game) {
        super(game);
    }

    public void retry() {
        game.setScreen(GameScreen.class);
    }

    public void backToMenu() {
        game.setScreen(MenuScreen.class);
    }

    public void quitGame() {
        Gdx.app.exit();
    }
}
