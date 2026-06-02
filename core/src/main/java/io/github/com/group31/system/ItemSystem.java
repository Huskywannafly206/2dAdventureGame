package io.github.com.group31.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import io.github.com.group31.audio.AudioService;
import io.github.com.group31.component.Inventory;
import io.github.com.group31.component.Item;
import io.github.com.group31.ui.model.GameViewModel;

/**
 * Xử lý logic nhặt vật phẩm trên bản đồ:
 *  - Phát hiện item đã được Player "chạm" (collectedBy != null, được set bởi PhysicSystem).
 *  - Thêm vật phẩm vào Inventory của Player.
 *  - Phát âm thanh pickup tương ứng.
 *  - Cập nhật HUD thông qua GameViewModel.
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

        // Thêm vào Inventory của Player
        Inventory inventory = Inventory.MAPPER.get(collector);
        if (inventory != null) {
            inventory.addItem(item.getType(), 1);
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
}
