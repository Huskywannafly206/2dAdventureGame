package io.github.com.group31.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Component lưu trữ túi đồ của Player.
 * Gắn cho Player entity, lưu số lượng từng loại vật phẩm.
 */
public class Inventory implements Component {
    public static final ComponentMapper<Inventory> MAPPER = ComponentMapper.getFor(Inventory.class);

    private final Map<Item.Type, Integer> items;

    public Inventory() {
        this.items = new HashMap<>();
        // Khởi tạo mọi loại vật phẩm với số lượng = 0
        for (Item.Type type : Item.Type.values()) {
            items.put(type, 0);
        }
    }

    /** Thêm một số lượng vật phẩm vào túi đồ. */
    public void addItem(Item.Type type, int amount) {
        items.merge(type, amount, Integer::sum);
    }

    /**
     * Xóa một số lượng vật phẩm khỏi túi đồ.
     * @return true nếu đủ số lượng và xóa thành công, false nếu không đủ.
     */
    public boolean removeItem(Item.Type type, int amount) {
        int current = getItemCount(type);
        if (current < amount) return false;
        items.put(type, current - amount);
        return true;
    }

    /** Trả về số lượng vật phẩm hiện có trong túi. */
    public int getItemCount(Item.Type type) {
        return items.getOrDefault(type, 0);
    }

    /** Đặt trực tiếp số lượng vật phẩm (dùng khi load save). */
    public void setItemCount(Item.Type type, int count) {
        items.put(type, Math.max(0, count));
    }
}
