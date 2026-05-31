package io.github.com.quillraven.ui.model;

import com.badlogic.gdx.Gdx;
import io.github.com.quillraven.GdxGame;
import io.github.com.quillraven.screen.GameScreen;
import io.github.com.quillraven.screen.MenuScreen;

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
