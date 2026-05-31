package io.github.com.quillraven.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * Experience component — placed on:
 * - Player: tracks current XP, level, progress to next level.
 * - Enemies: xpReward field only (how much XP they give when killed).
 */
public class Experience implements Component {
    public static final ComponentMapper<Experience> MAPPER = ComponentMapper.getFor(Experience.class);

    private final float xpReward;
    private float xp;
    private int level;
    private float xpToNextLevel;

    /**
     * Enemy constructor: just the reward XP (player XP tracking fields unused).
     */
    public Experience(float xpReward) {
        this.xpReward = xpReward;
        this.xp = 0f;
        this.level = 0;
        this.xpToNextLevel = 0f;
    }

    /**
     * Player constructor: full XP tracking.
     */
    public Experience(float xp, int level, float xpToNextLevel) {
        this.xpReward = 0f;
        this.xp = xp;
        this.level = level;
        this.xpToNextLevel = xpToNextLevel;
    }

    /**
     * Adds XP to player. Returns true if levelled up.
     */
    public boolean addXp(float amount) {
        this.xp += amount;
        if (this.xp >= this.xpToNextLevel) {
            this.xp -= this.xpToNextLevel;
            this.level++;
            this.xpToNextLevel = this.level * 100f;
            return true;
        }
        return false;
    }

    public float getXpReward() {
        return xpReward;
    }

    public float getXp() {
        return xp;
    }

    public int getLevel() {
        return level;
    }

    public float getXpToNextLevel() {
        return xpToNextLevel;
    }

    public void setXp(float xp) {
        this.xp = xp;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setXpToNextLevel(float xpToNextLevel) {
        this.xpToNextLevel = xpToNextLevel;
    }
}
