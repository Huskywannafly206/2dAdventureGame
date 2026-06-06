package io.github.com.group31.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import io.github.com.group31.audio.AudioService;
import io.github.com.group31.combat.Weapon;
import io.github.com.group31.component.CombatState;
import io.github.com.group31.component.Inventory;
import io.github.com.group31.component.Item;
import io.github.com.group31.component.Transform;
import io.github.com.group31.ui.model.GameViewModel;

/**
 * Xử lý logic nhặt vật phẩm trên bản đồ:
 *  - Phát hiện item đã được Player "chạm" (collectedBy != null, được set bởi PhysicSystem).
 *  - Nếu là vũ khí: mở khóa trong CombatState, hiển thị floating text vàng.
 *  - Nếu là vật phẩm thường: thêm vào Inventory, phát âm thanh, cập nhật HUD.
 *  - Xóa thực thể item khỏi engine.
 */
public class ItemSystem extends IteratingSystem {
    private final AudioService audioService;
    private final GameViewModel viewModel;
    /** Buffer tạm - tránh ConcurrentModificationException khi remove entity trong lúc iterate. */
    private final Array<Entity> toRemove = new Array<>();

    public ItemSystem(AudioService audioService, GameViewModel viewModel) {
        super(Family.all(Item.class).get());
        this.audioService = audioService;
        this.viewModel = viewModel;
    }

    @Override
    public void update(float deltaTime) {
        toRemove.clear();
        super.update(deltaTime);
        // Xóa entity SAU KHI hoàn tất vòng lặp
        for (Entity entity : toRemove) {
            getEngine().removeEntity(entity);
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Item item = Item.MAPPER.get(entity);
        Entity collector = item.getCollectedBy();
        if (collector == null) return;

        Item.Type type = item.getType();

        // --- Xử lý nhặt vũ khí ---
        Weapon unlocked = weaponFor(type);
        if (unlocked != null) {
            CombatState combatState = CombatState.MAPPER.get(collector);
            if (combatState != null && combatState.unlockWeapon(unlocked)) {
                // Thông báo vũ khí mới bằng floating text màu vàng
                Transform t = Transform.MAPPER.get(collector);
                if (t != null) {
                    float x = t.getPosition().x + t.getSize().x * 0.5f;
                    float y = t.getPosition().y + t.getSize().y;
                    viewModel.showFloatingText(
                        "[YELLOW]Nhận: " + unlocked.displayName + "![]", x, y);
                }
                audioService.playSound(item.getPickupSound());
            }
            toRemove.add(entity);
            return;
        }

        // --- Xử lý vật phẩm thường ---
        Inventory inventory = Inventory.MAPPER.get(collector);
        if (inventory != null) {
            inventory.addItem(type, 1);
        }

        // Phát âm thanh
        audioService.playSound(item.getPickupSound());

        // Cập nhật HUD
        if (inventory != null) {
            viewModel.updateInventory(
                inventory.getItemCount(Item.Type.POTION_HEALTH),
                inventory.getItemCount(Item.Type.COIN),
                inventory.getItemCount(Item.Type.KEY)
            );
        }

        toRemove.add(entity);
    }

    /** Trả về Weapon tương ứng với Item.Type vũ khí, hoặc null nếu không phải vũ khí. */
    private static Weapon weaponFor(Item.Type type) {
        return switch (type) {
            case WEAPON_SWORD      -> Weapon.SWORD;
            case WEAPON_BOW        -> Weapon.BOW;
            case WEAPON_MAGIC_WAND -> Weapon.MAGIC_WAND;
            default                -> null;
        };
    }
}
