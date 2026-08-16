package io.github.com.group31.save;

import com.badlogic.gdx.utils.TimeUtils;

import java.util.HashMap;
import java.util.Map;

public class RespawnState {
    private static RespawnState instance;

    private Map<String, Long> entityRespawnTimes = new HashMap<>();

    private RespawnState() {}

    public static RespawnState getInstance() {
        if (instance == null) {
            instance = new RespawnState();
        }
        return instance;
    }

    public Map<String, Long> getEntityRespawnTimes() {
        return entityRespawnTimes;
    }

    public void setEntityRespawnTimes(Map<String, Long> times) {
        if (times != null) {
            this.entityRespawnTimes = new HashMap<>(times);
        } else {
            this.entityRespawnTimes.clear();
        }
    }

    public void registerDeath(String entityId, float respawnTimeSec) {
        if (respawnTimeSec == -1f) {
            // Never respawn: Set to a very large time in the future
            entityRespawnTimes.put(entityId, Long.MAX_VALUE);
        } else if (respawnTimeSec > 0f) {
            long respawnTimeMillis = TimeUtils.millis() + (long)(respawnTimeSec * 1000L);
            entityRespawnTimes.put(entityId, respawnTimeMillis);
        }
    }

    public boolean isRespawning(String entityId) {
        if (!entityRespawnTimes.containsKey(entityId)) {
            return false;
        }
        long respawnTimeMillis = entityRespawnTimes.get(entityId);
        if (respawnTimeMillis == Long.MAX_VALUE) {
            return true; // Never respawn
        }
        return TimeUtils.millis() < respawnTimeMillis;
    }

    public void clearRespawn(String entityId) {
        entityRespawnTimes.remove(entityId);
    }
}
