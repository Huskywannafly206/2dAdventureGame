package io.github.com.group31.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import io.github.com.group31.asset.SoundAsset;

/**
 * Component đánh dấu một thực thể là vật phẩm có thể nhặt trên bản đồ.
 * Khi Player va chạm, ItemSystem sẽ xử lý logic nhặt và xóa thực thể này.
 */
public class Item implements Component {
    public static final ComponentMapper<Item> MAPPER = ComponentMapper.getFor(Item.class);

    /** Loại vật phẩm - mỗi loại tương ứng với hành động và âm thanh khác nhau. */
    public enum Type {
        POTION_HEALTH,
        COIN,
        KEY,
        GOLD_KEY,
        SILVER_KEY,
        // Vũ khí nhặt được — mở khóa vũ khí tương ứng trong CombatState
        WEAPON_SWORD,
        WEAPON_BOW,
        WEAPON_MAGIC_WAND
    }

    private final Type type;
    private final float value;
    private final SoundAsset pickupSound;
    /** Entity đã nhặt vật phẩm (thường là Player). Được set bởi PhysicSystem. */
    private Entity collectedBy;

    public Item(Type type, float value, SoundAsset pickupSound) {
        this.type = type;
        this.value = value;
        this.pickupSound = pickupSound;
        this.collectedBy = null;
    }

    public Type getType() {
        return type;
    }

    public float getValue() {
        return value;
    }

    public SoundAsset getPickupSound() {
        return pickupSound;
    }

    public Entity getCollectedBy() {
        return collectedBy;
    }

    public void setCollectedBy(Entity collectedBy) {
        this.collectedBy = collectedBy;
    }
}
