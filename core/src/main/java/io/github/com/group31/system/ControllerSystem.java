package io.github.com.group31.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.com.group31.GdxGame;
import io.github.com.group31.asset.SoundAsset;
import io.github.com.group31.audio.AudioService;
import io.github.com.group31.component.Attack;
import io.github.com.group31.component.Controller;
import io.github.com.group31.component.Inventory;
import io.github.com.group31.component.Item;
import io.github.com.group31.component.Life;
import io.github.com.group31.component.Move;
import io.github.com.group31.component.Npc;
import io.github.com.group31.component.Transform;
import io.github.com.group31.input.Command;
import io.github.com.group31.screen.MenuScreen;
import io.github.com.group31.ui.model.GameViewModel;

public class ControllerSystem extends IteratingSystem {
    private static final float POTION_HEAL_AMOUNT = 4f;

    private final GdxGame game;
    private final AudioService audioService;
    private final GameViewModel viewModel;
    private Entity activeNpcEntity = null;

    public ControllerSystem(GdxGame game, AudioService audioService, GameViewModel viewModel) {
        super(Family.all(Controller.class).get());
        this.game = game;
        this.audioService = audioService;
        this.viewModel = viewModel;
    }

    /**
     * Processes input commands for the entity, handling movement and actions.
     */
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Controller controller = Controller.MAPPER.get(entity);
        if (controller.getPressedCommands().isEmpty() && controller.getReleasedCommands().isEmpty()) {
            return;
        }

        if (activeNpcEntity != null) {
            if (controller.getPressedCommands().contains(Command.INTERACT)) {
                interactWithNpc(entity);
            }
            controller.getPressedCommands().clear();
            controller.getReleasedCommands().clear();
            return;
        }

        for (Command command : controller.getPressedCommands()) {
            switch (command) {
                case UP -> moveEntity(entity, 0f, 1f);
                case DOWN -> moveEntity(entity, 0f, -1f);
                case LEFT -> moveEntity(entity, -1f, 0f);
                case RIGHT -> moveEntity(entity, 1f, 0f);
                case SELECT -> startEntityAttack(entity);
                case CANCEL -> game.setScreen(MenuScreen.class);
                case USE_ITEM -> usePotion(entity);
                case INTERACT -> interactWithNpc(entity);
            }
        }
        controller.getPressedCommands().clear();

        for (Command command : controller.getReleasedCommands()) {
            switch (command) {
                case UP -> moveEntity(entity, 0f, -1f);
                case DOWN -> moveEntity(entity, 0f, 1f);
                case LEFT -> moveEntity(entity, 1f, 0f);
                case RIGHT -> moveEntity(entity, -1f, 0f);
            }
        }
        controller.getReleasedCommands().clear();
    }

    /**
     * Uống bình máu: hồi 4 HP nếu Player có bình và HP chưa đầy.
     */
    private void usePotion(Entity entity) {
        Inventory inventory = Inventory.MAPPER.get(entity);
        Life life = Life.MAPPER.get(entity);
        if (inventory == null || life == null) return;

        // Không dùng nếu hết bình hoặc HP đã đầy
        if (inventory.getItemCount(Item.Type.POTION_HEALTH) <= 0) return;
        if (life.getLife() >= life.getMaxLife()) return;

        // Hồi HP
        life.addLife(POTION_HEAL_AMOUNT);
        inventory.removeItem(Item.Type.POTION_HEALTH, 1);

        // Phát âm thanh
        audioService.playSound(SoundAsset.HEAL);

        // Cập nhật HUD life
        viewModel.updateLifeInfo(life.getMaxLife(), life.getLife());

        // Hiện chữ nổi màu xanh lá cây
        Transform transform = Transform.MAPPER.get(entity);
        if (transform != null) {
            float x = transform.getPosition().x + transform.getSize().x * 0.5f;
            float y = transform.getPosition().y + transform.getSize().y;
            viewModel.showFloatingText("[GREEN]+" + (int) POTION_HEAL_AMOUNT + " HP[]", x, y);
        }

        // Cập nhật HUD inventory
        viewModel.updateInventory(
            inventory.getItemCount(Item.Type.POTION_HEALTH),
            inventory.getItemCount(Item.Type.COIN),
            inventory.getItemCount(Item.Type.KEY)
        );
    }

    private void startEntityAttack(Entity entity) {
        Attack attack = Attack.MAPPER.get(entity);
        if (attack != null && attack.canAttack()) {
            attack.startAttack();
        }
    }

    private void moveEntity(Entity entity, float dx, float dy) {
        Move move = Move.MAPPER.get(entity);
        if (move != null) {
            move.getDirection().x += dx;
            move.getDirection().y += dy;
        }
    }

    private void interactWithNpc(Entity player) {
        if (activeNpcEntity != null) {
            Npc npc = Npc.MAPPER.get(activeNpcEntity);
            npc.advanceDialogue();
            if (npc.hasMoreDialogue()) {
                viewModel.showDialogue(npc.getName(), npc.getCurrentLine());
            } else {
                viewModel.hideDialogue();
                npc.resetDialogue();
                activeNpcEntity = null;
            }
            return;
        }

        Transform playerTransform = Transform.MAPPER.get(player);
        if (playerTransform == null) return;

        Entity closestNpc = null;
        float minDistance = 1.5f; // khoảng cách 1.5 tiles

        for (Entity npcEntity : getEngine().getEntitiesFor(Family.all(Npc.class, Transform.class).get())) {
            Transform npcTransform = Transform.MAPPER.get(npcEntity);
            float dist = playerTransform.getPosition().dst(npcTransform.getPosition());
            if (dist < minDistance) {
                minDistance = dist;
                closestNpc = npcEntity;
            }
        }

        if (closestNpc != null) {
            activeNpcEntity = closestNpc;
            Move move = Move.MAPPER.get(player);
            if (move != null) {
                move.getDirection().setZero();
            }

            Npc npc = Npc.MAPPER.get(activeNpcEntity);
            npc.resetDialogue();
            viewModel.showDialogue(npc.getName(), npc.getCurrentLine());
        }
    }
}
