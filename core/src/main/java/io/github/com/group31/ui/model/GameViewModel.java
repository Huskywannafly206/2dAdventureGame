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
    public static final String DIALOGUE_CHANGED    = "dialogueChanged";
    public static final String WEAPON_CHANGED      = "weaponChanged";
    public static final String QUEST_CHANGED       = "questChanged";
    public static final String MENU_TOGGLED        = "menuToggled";
    public static final String UNLOCKED_WEAPONS_CHANGED = "unlockedWeaponsChanged";
    public static final String SCARECROW_HITS_CHANGED = "scarecrowHitsChanged";
    public static final String USE_INVENTORY_ITEM = "useInventoryItem";
    public static final String SELECTED_ITEM_CHANGED = "selectedItemChanged";


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
    private int goldKeys;
    private int silverKeys;
    private int soothingHerbs;
    private int jungleMapKeys;
    private int silverCups;
    private int heartContainers;
    private int laurelLeaves;
    private int iceMapKeys;
    private int frostOres;
    private int fishingRods;
    private int sacredWaters;
    private int frozenHearts;


    private io.github.com.group31.component.Item.Type selectedItemType = null;

    private String[] activeDialogue = null;
    private String   currentWeaponName = "Đấm";
    private String[] activeQuest = new String[]{"", ""};
    private boolean menuOpen = false;
    private java.util.List<String> unlockedWeapons = new java.util.ArrayList<>(java.util.List.of("FIST"));


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
        this.goldKeys = 0;
        this.silverKeys = 0;
        this.soothingHerbs = 0;
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
    public int getGoldKeys() { return goldKeys; }
    public int getSilverKeys() { return silverKeys; }
    public int getSoothingHerbs() { return soothingHerbs; }
    public int getJungleMapKeys() { return jungleMapKeys; }
    public int getSilverCups() { return silverCups; }
    public int getHeartContainers() { return heartContainers; }
    public int getLaurelLeaves() { return laurelLeaves; }
    public int getIceMapKeys() { return iceMapKeys; }
    public int getFrostOres() { return frostOres; }
    public int getFishingRods() { return fishingRods; }
    public int getSacredWaters() { return sacredWaters; }
    public int getFrozenHearts() { return frozenHearts; }


    public io.github.com.group31.component.Item.Type getSelectedItemType() {
        return selectedItemType;
    }

    public void setSelectedItemType(io.github.com.group31.component.Item.Type type) {
        if (this.selectedItemType != type) {
            this.selectedItemType = type;
            this.propertyChangeSupport.firePropertyChange(SELECTED_ITEM_CHANGED, null, type);
        }
    }

    private io.github.com.group31.component.Item.Type itemToUse = null;

    public void useInventoryItem() {
        if (this.selectedItemType != null) {
            this.itemToUse = this.selectedItemType;
        }
    }

    public io.github.com.group31.component.Item.Type consumeItemToUse() {
        io.github.com.group31.component.Item.Type type = this.itemToUse;
        this.itemToUse = null;
        return type;
    }

    /**
     * Cập nhật số lượng item trong túi đồ và thông báo cho View.
     */
    
    public void updateInventory(int potions, int coins, int keys) {
        updateInventory(potions, coins, keys, this.goldKeys, this.silverKeys, this.soothingHerbs, this.jungleMapKeys, this.silverCups, this.heartContainers, this.laurelLeaves, this.iceMapKeys);
    }

    public void updateInventory(int potions, int coins, int keys, int goldKeys, int silverKeys) {
        updateInventory(potions, coins, keys, goldKeys, silverKeys, this.soothingHerbs, this.jungleMapKeys, this.silverCups, this.heartContainers, this.laurelLeaves, this.iceMapKeys);
    }

    public void updateInventory(int potions, int coins, int keys, int goldKeys, int silverKeys, int soothingHerbs) {
        updateInventory(potions, coins, keys, goldKeys, silverKeys, soothingHerbs, this.jungleMapKeys, this.silverCups, this.heartContainers, this.laurelLeaves, this.iceMapKeys);
    }

    public void updateInventory(int potions, int coins, int keys, int goldKeys, int silverKeys, int soothingHerbs, int jungleMapKeys) {
        updateInventory(potions, coins, keys, goldKeys, silverKeys, soothingHerbs, jungleMapKeys, this.silverCups, this.heartContainers, this.laurelLeaves, this.iceMapKeys);
    }

    public void updateInventory(int potions, int coins, int keys, int goldKeys, int silverKeys, int soothingHerbs, int jungleMapKeys, int silverCups) {
        updateInventory(potions, coins, keys, goldKeys, silverKeys, soothingHerbs, jungleMapKeys, silverCups, this.heartContainers, this.laurelLeaves, this.iceMapKeys);
    }

    
    
    public void updateInventory(int potions, int coins, int keys, int goldKeys, int silverKeys, int soothingHerbs, int jungleMapKeys, int silverCups, int heartContainers, int laurelLeaves, int iceMapKeys) {
        updateInventory(potions, coins, keys, goldKeys, silverKeys, soothingHerbs, jungleMapKeys, silverCups, heartContainers, laurelLeaves, iceMapKeys, this.frostOres, this.fishingRods, this.sacredWaters, this.frozenHearts);
    }

    public void updateInventory(int potions, int coins, int keys, int goldKeys, int silverKeys, int soothingHerbs, int jungleMapKeys, int silverCups, int heartContainers, int laurelLeaves, int iceMapKeys, int frostOres, int fishingRods, int sacredWaters, int frozenHearts) {
        this.potions = potions;
        this.coins   = coins;
        this.keys    = keys;
        this.goldKeys = goldKeys;
        this.silverKeys = silverKeys;
        this.soothingHerbs = soothingHerbs;
        this.jungleMapKeys = jungleMapKeys;
        this.silverCups = silverCups;
        this.heartContainers = heartContainers;
        this.laurelLeaves = laurelLeaves;
        this.iceMapKeys = iceMapKeys;
        this.frostOres = frostOres;
        this.fishingRods = fishingRods;
        this.sacredWaters = sacredWaters;
        this.frozenHearts = frozenHearts;
        this.propertyChangeSupport.firePropertyChange(INVENTORY_CHANGED, null, new int[]{
            this.potions, this.coins, this.keys, this.goldKeys, this.silverKeys, this.soothingHerbs, this.jungleMapKeys, this.silverCups, this.heartContainers, this.laurelLeaves, this.iceMapKeys, this.frostOres, this.fishingRods, this.sacredWaters, this.frozenHearts
        });
    }

    
    public void updateInventory(int potions, int coins, int keys, int goldKeys, int silverKeys, int soothingHerbs, int jungleMapKeys, int silverCups, int heartContainers) {
        updateInventory(potions, coins, keys, goldKeys, silverKeys, soothingHerbs, jungleMapKeys, silverCups, heartContainers, this.laurelLeaves, this.iceMapKeys);
    }

    public void updateInventory(int potions, int coins, int keys, int goldKeys, int silverKeys, int soothingHerbs, int jungleMapKeys, int silverCups, int heartContainers, int laurelLeaves) {
        updateInventory(potions, coins, keys, goldKeys, silverKeys, soothingHerbs, jungleMapKeys, silverCups, heartContainers, laurelLeaves, this.iceMapKeys);
    }

    public void updateInventory(io.github.com.group31.component.Inventory inventory) {
        if (inventory == null) return;
        updateInventory(
            inventory.getItemCount(io.github.com.group31.component.Item.Type.POTION_HEALTH),
            inventory.getItemCount(io.github.com.group31.component.Item.Type.COIN),
            inventory.getItemCount(io.github.com.group31.component.Item.Type.KEY),
            inventory.getItemCount(io.github.com.group31.component.Item.Type.GOLD_KEY),
            inventory.getItemCount(io.github.com.group31.component.Item.Type.SILVER_KEY),
            inventory.getItemCount(io.github.com.group31.component.Item.Type.SOOTHING_HERB),
            inventory.getItemCount(io.github.com.group31.component.Item.Type.JUNGLE_MAP_KEY),
            inventory.getItemCount(io.github.com.group31.component.Item.Type.SILVER_CUP),
            inventory.getItemCount(io.github.com.group31.component.Item.Type.HEART_CONTAINER),
            inventory.getItemCount(io.github.com.group31.component.Item.Type.LAUREL_LEAF),
            inventory.getItemCount(io.github.com.group31.component.Item.Type.ICE_MAP_KEY),
            inventory.getItemCount(io.github.com.group31.component.Item.Type.FROST_IRON_ORE),
            inventory.getItemCount(io.github.com.group31.component.Item.Type.HEIRLOOM_FISHING_ROD),
            inventory.getItemCount(io.github.com.group31.component.Item.Type.SACRED_SPRING_WATER),
            inventory.getItemCount(io.github.com.group31.component.Item.Type.FROZEN_HEART)
        );
    }

    public void scarecrowHitsChanged(int index, int hits) {
        this.propertyChangeSupport.firePropertyChange(SCARECROW_HITS_CHANGED, null, new int[]{index, hits});
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
    public static final String SKIP_DIALOGUE_TYPING = "skipDialogueTyping";
    private boolean dialogueTyping = false;

    public boolean isDialogueTyping() {
        return dialogueTyping;
    }

    public void setDialogueTyping(boolean typing) {
        this.dialogueTyping = typing;
    }

    public void skipDialogueTyping() {
        this.propertyChangeSupport.firePropertyChange(SKIP_DIALOGUE_TYPING, false, true);
        this.dialogueTyping = false;
    }

    public void showDialogue(String npcName, String line, String facesetPath) {
        setDialogueTyping(true);
        String[] prev = this.activeDialogue;
        // activeDialogue[0] = npcName, [1] = line, [2] = facesetPath (có thể rỗng)
        this.activeDialogue = new String[]{npcName, line, facesetPath != null ? facesetPath : ""};
        this.propertyChangeSupport.firePropertyChange(DIALOGUE_CHANGED, prev, this.activeDialogue);
    }

    /** Overload backward-compat — không có faceset. */
    public void showDialogue(String npcName, String line) {
        showDialogue(npcName, line, null);
    }

    public void hideDialogue() {
        setDialogueTyping(false);
        String[] prev = this.activeDialogue;
        this.activeDialogue = null;
        this.propertyChangeSupport.firePropertyChange(DIALOGUE_CHANGED, prev, null);
    }

    public String[] getActiveDialogue() {
        return activeDialogue;
    }

    public void updateWeaponName(String name) {
        String oldName = this.currentWeaponName;
        this.currentWeaponName = name;
        this.propertyChangeSupport.firePropertyChange(WEAPON_CHANGED, oldName, name);
    }

    public String getCurrentWeaponName() {
        return currentWeaponName;
    }

    public void updateQuestInfo(String title, String objective) {
        String[] prev = this.activeQuest;
        this.activeQuest = new String[]{title, objective};
        this.propertyChangeSupport.firePropertyChange(QUEST_CHANGED, prev, this.activeQuest);
    }

    public String[] getActiveQuest() {
        return activeQuest;
    }

    public void toggleMenu() {
        boolean prev = this.menuOpen;
        this.menuOpen = !this.menuOpen;
        this.propertyChangeSupport.firePropertyChange(MENU_TOGGLED, prev, this.menuOpen);
        if (this.menuOpen) {
            audioService.playSound(SoundAsset.PICKUP);
        }
    }

    public boolean isMenuOpen() {
        return menuOpen;
    }

    public static final String TAB_CHANGED = "tabChanged";
    private int currentTab = 0; // 0 = INVENTORY, 1 = QUEST, 2 = MAP

    public int getCurrentTab() {
        return currentTab;
    }

    public void setCurrentTab(int tab) {
        int prev = this.currentTab;
        this.currentTab = (tab + 3) % 3;
        if (prev != this.currentTab) {
            this.propertyChangeSupport.firePropertyChange(TAB_CHANGED, prev, this.currentTab);
            audioService.playSound(SoundAsset.PICKUP);
        }
    }

    public void nextTab() {
        setCurrentTab(currentTab + 1);
    }

    public void prevTab() {
        setCurrentTab(currentTab - 1);
    }

    public void updateUnlockedWeapons(java.util.List<String> weapons) {
        java.util.List<String> prev = this.unlockedWeapons;
        this.unlockedWeapons = weapons;
        this.propertyChangeSupport.firePropertyChange(UNLOCKED_WEAPONS_CHANGED, prev, weapons);
    }

    public java.util.List<String> getUnlockedWeapons() {
        return unlockedWeapons;
    }
}
