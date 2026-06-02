package io.github.com.group31.ui.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import io.github.com.group31.GdxGame;
import io.github.com.group31.asset.SoundAsset;
import io.github.com.group31.audio.AudioService;

import java.util.Map;

public class GameViewModel extends ViewModel {
    public static final String LIFE_POINTS = "lifePoints";
    public static final String MAX_LIFE = "maxLife";
    public static final String PLAYER_DAMAGE = "playerDamage";
    public static final String FLOATING_TEXT = "floatingText";
    public static final String PLAYER_DEAD = "playerDead";
    public static final String XP_CHANGED = "xpChanged";
    public static final String LEVEL_CHANGED = "levelChanged";
    public static final String INVENTORY_CHANGED = "inventoryChanged";

    private final AudioService audioService;
    private int lifePoints;
    private int maxLife;
    private Map.Entry<Vector2, Integer> playerDamage;
    private Map.Entry<Vector2, String> floatingText;
    private final Vector2 tmpVec2;

    // XP / Level tracking
    private float xp;
    private float xpToNextLevel;
    private int level;

    // Inventory tracking
    private int potions;
    private int coins;
    private int keys;

    public GameViewModel(GdxGame game) {
        super(game);
        this.audioService = game.getAudioService();
        this.lifePoints = 0;
        this.maxLife = 0;
        this.playerDamage = null;
        this.floatingText = null;
        this.tmpVec2 = new Vector2();
        this.xp = 0f;
        this.xpToNextLevel = 100f;
        this.level = 1;
        this.potions = 0;
        this.coins = 0;
        this.keys = 0;
    }

    public void setMaxLife(int maxLife) {
        if (this.maxLife != maxLife) {
            this.propertyChangeSupport.firePropertyChange(MAX_LIFE, this.maxLife, maxLife);
        }
        this.maxLife = maxLife;
    }

    public int getMaxLife() {
        return maxLife;
    }

    public void setLifePoints(int lifePoints) {
        if (this.lifePoints != lifePoints) {
            this.propertyChangeSupport.firePropertyChange(LIFE_POINTS, this.lifePoints, lifePoints);
            if (this.lifePoints != 0 && this.lifePoints < lifePoints) {
                audioService.playSound(SoundAsset.LIFE_REG);
            }
        }
        this.lifePoints = lifePoints;
    }

    public int getLifePoints() {
        return lifePoints;
    }

    public void updateLifeInfo(float maxLife, float life) {
        setMaxLife((int) maxLife);
        setLifePoints((int) life);
    }

    public void playerDamage(int amount, float x, float y) {
        Vector2 position = new Vector2(x, y);
        this.playerDamage = Map.entry(position, amount);
        this.propertyChangeSupport.firePropertyChange(PLAYER_DAMAGE, null, this.playerDamage);
    }

    /**
     * Hiển thị chữ nổi bay tại vị trí game world (dùng cho +HP, tên item, v.v.)
     * @param text  Nội dung chữ nổi (hỗ trợ markup của TypingLabel).
     * @param x     Toạ độ X trong game world.
     * @param y     Toạ độ Y trong game world.
     */
    public void showFloatingText(String text, float x, float y) {
        Vector2 position = new Vector2(x, y);
        this.floatingText = Map.entry(position, text);
        this.propertyChangeSupport.firePropertyChange(FLOATING_TEXT, null, this.floatingText);
    }

    // ── Inventory ────────────────────────────────────────────────────────────

    public int getPotions() { return potions; }
    public int getCoins()   { return coins; }
    public int getKeys()    { return keys; }

    /**
     * Cập nhật số lượng item trong túi đồ và thông báo cho View.
     */
    public void updateInventory(int potions, int coins, int keys) {
        this.potions = potions;
        this.coins   = coins;
        this.keys    = keys;
        // Gửi ba số dưới dạng mảng int[] để View tự parse
        this.propertyChangeSupport.firePropertyChange(INVENTORY_CHANGED, null, new int[]{potions, coins, keys});
    }

    /** Called by LifeSystem when player life reaches 0. */
    public void onPlayerDead() {
        Gdx.app.debug("GameViewModel", "onPlayerDead() called → firing PLAYER_DEAD event");
        this.propertyChangeSupport.firePropertyChange(PLAYER_DEAD, false, true);
    }

    // ── XP / Level ──────────────────────────────────────────────────────────

    public float getXp() {
        return xp;
    }

    public float getXpToNextLevel() {
        return xpToNextLevel;
    }

    public int getLevel() {
        return level;
    }

    public void updateXpInfo(float xp, float xpToNextLevel, int level, boolean leveledUp) {
        this.xp = xp;
        this.xpToNextLevel = xpToNextLevel;
        this.propertyChangeSupport.firePropertyChange(XP_CHANGED, -1f, xp);
        if (leveledUp || this.level != level) {
            this.level = level;
            this.propertyChangeSupport.firePropertyChange(LEVEL_CHANGED, -1, level);
        }
    }

    // ── Coordinate projection ────────────────────────────────────────────────

    public Vector2 toScreenCoords(Vector2 position) {
        tmpVec2.set(position);
        game.getViewport().project(tmpVec2);
        return tmpVec2;
    }
}
