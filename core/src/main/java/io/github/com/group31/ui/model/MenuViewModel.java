package io.github.com.group31.ui.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import io.github.com.group31.GdxGame;
import io.github.com.group31.asset.SoundAsset;
import io.github.com.group31.audio.AudioService;
import io.github.com.group31.screen.GameScreen;

public class MenuViewModel extends ViewModel {

    private final AudioService audioService;
    private long lastSndPlayTime;

    public MenuViewModel(GdxGame game) {
        super(game);
        this.audioService = game.getAudioService();
        this.lastSndPlayTime = 0L;
    }

    public float getMusicVolume() {
        return audioService.getMusicVolume();
    }

    public void setMusicVolume(float volume) {
        this.audioService.setMusicVolume(volume);
    }

    public float getSoundVolume() {
        return audioService.getSoundVolume();
    }

    public void setSoundVolume(float soundVolume) {
        this.audioService.setSoundVolume(soundVolume);
        if (TimeUtils.timeSinceMillis(lastSndPlayTime) > 500L) {
            this.lastSndPlayTime = TimeUtils.millis();
            this.audioService.playSound(SoundAsset.SWORD_HIT);
        }
    }

    public void startGame() {
        game.getSaveService().clearSave();
        game.setScreen(GameScreen.class);
    }

    public boolean hasSaveFile() {
        return game.getSaveService().hasSaveFile();
    }

    public void continueGame() {
        game.setScreen(GameScreen.class);
    }

    public void quitGame() {
        Gdx.app.exit();
    }
}
