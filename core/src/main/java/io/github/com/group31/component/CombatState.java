package io.github.com.group31.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import io.github.com.group31.combat.Weapon;

import java.util.ArrayList;
import java.util.List;

/**
 * Component lưu trạng thái chiến đấu của Player:
 * - Danh sách vũ khí đã mở khóa (unlockedWeapons)
 * - Vũ khí đang sử dụng (currentWeaponIndex)
 *
 * Mặc định Player chỉ có FIST. Nhặt weapon item trên bản đồ sẽ mở khóa
 * vũ khí mới và cho phép chuyển đổi bằng phím V.
 */
public class CombatState implements Component {
    public static final ComponentMapper<CombatState> MAPPER =
        ComponentMapper.getFor(CombatState.class);

    private final List<Weapon> unlockedWeapons;
    private int currentIndex;

    public CombatState() {
        this.unlockedWeapons = new ArrayList<>();
        this.unlockedWeapons.add(Weapon.FIST);  // luôn có đánh tay
        this.currentIndex = 0;
    }

    /** Vũ khí đang cầm. */
    public Weapon getCurrentWeapon() {
        return unlockedWeapons.get(currentIndex);
    }

    /** Chuyển sang vũ khí tiếp theo trong danh sách đã mở khóa. */
    public void nextWeapon() {
        if (unlockedWeapons.size() <= 1) return;
        currentIndex = (currentIndex + 1) % unlockedWeapons.size();
    }

    /**
     * Mở khóa một vũ khí mới. Nếu đã có thì bỏ qua.
     * @return true nếu vừa thêm mới, false nếu đã có.
     */
    public boolean unlockWeapon(Weapon weapon) {
        if (unlockedWeapons.contains(weapon)) return false;
        unlockedWeapons.add(weapon);
        return true;
    }

    public List<Weapon> getUnlockedWeapons() {
        return unlockedWeapons;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int index) {
        if (index >= 0 && index < unlockedWeapons.size()) {
            this.currentIndex = index;
        }
    }
}
