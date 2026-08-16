package io.github.com.group31.save;

public class SaveData {
    public float playerX;
    public float playerY;
    public float playerHp;
    public float playerMaxHp;
    public float playerXp;
    public int playerLevel;
    public String mapName;
    public int playerPotions;
    public int playerCoins;
    public int playerKeys;
    public int playerGoldKeys;
    public int playerSilverKeys;
    public int playerSoothingHerbs;
    public int playerJungleMapKeys;
    public int playerSilverCups;
    public int playerHeartContainers;
    public int playerLaurelLeaves;
    public int playerMagicShards;
    public int questStage;
    public java.util.List<String> unlockedWeapons;
    public int currentWeaponIndex;
    public java.util.Map<String, Long> respawnTimes = new java.util.HashMap<>();

    // Ice World quest tracking fields
    public int frostOreCount;
    public boolean hasSacredSpringWater;
    public boolean hasFrozenHeart;
    public boolean fishingRodSpawned;

    public SaveData() {
        // Default constructor for libGDX JSON
    }
}
