package io.github.com.group31.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import io.github.com.group31.GdxGame;
import io.github.com.group31.asset.AssetService;
import io.github.com.group31.asset.AtlasAsset;
import io.github.com.group31.asset.SoundAsset;
import io.github.com.group31.audio.AudioService;
import io.github.com.group31.combat.Weapon;
import io.github.com.group31.combat.Weapon.ProjectileType;
import io.github.com.group31.component.Animation2D;
import io.github.com.group31.component.Animation2D.AnimationType;
import io.github.com.group31.component.Attack;
import io.github.com.group31.component.CombatState;
import io.github.com.group31.component.Controller;
import io.github.com.group31.component.Damaged;
import io.github.com.group31.component.Facing;
import io.github.com.group31.component.Facing.FacingDirection;
import io.github.com.group31.component.Graphic;
import io.github.com.group31.component.Inventory;
import io.github.com.group31.component.Item;
import io.github.com.group31.component.Life;
import io.github.com.group31.component.Move;
import io.github.com.group31.component.Npc;
import io.github.com.group31.component.Physic;
import io.github.com.group31.component.Player;
import io.github.com.group31.component.Projectile;
import io.github.com.group31.component.Transform;
import io.github.com.group31.input.Command;
import io.github.com.group31.screen.MenuScreen;
import io.github.com.group31.ui.model.GameViewModel;
import io.github.com.group31.tiled.TiledAshleyConfigurator;

public class ControllerSystem extends IteratingSystem {
    private static final float POTION_HEAL_AMOUNT = 4f;

    /** Thời gian Dash: thời lượng, tốc độ, cooldown */
    private static final float DASH_DURATION   = 0.25f;
    private static final float DASH_SPEED      = 10f;
    private static final float DASH_COOLDOWN   = 1.0f;

    /** Tầm auto-aim của phép (tính bằng game units = tiles). */
    private static final float AUTO_AIM_RANGE  = 6f;

    private final GdxGame game;
    private final AudioService audioService;
    private final GameViewModel viewModel;
    private final World physicWorld;
    private final AssetService assetService;
    private final TiledAshleyConfigurator configurator;
    private Entity activeNpcEntity = null;

    // --- Dash state (per-player, gắn trên system vì chỉ có 1 player) ---
    private float dashTimer    = 0f; // thời gian đang dash còn lại
    private float dashCooldown = 0f; // thời gian hồi chiêu còn lại
    private final Vector2 dashDir = new Vector2();

    public ControllerSystem(GdxGame game, AudioService audioService,
                            GameViewModel viewModel, World physicWorld,
                            AssetService assetService, TiledAshleyConfigurator configurator) {
        super(Family.all(Controller.class).get());
        this.game         = game;
        this.audioService = audioService;
        this.viewModel    = viewModel;
        this.physicWorld  = physicWorld;
        this.assetService = assetService;
        this.configurator = configurator;
    }

    /**
     * Processes input commands for the entity, handling movement and actions.
     */
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // --- Đếm timer Dash ---
        if (dashTimer > 0f) {
            dashTimer -= deltaTime;
            if (dashTimer < 0f) dashTimer = 0f;
        }
        if (dashCooldown > 0f) {
            dashCooldown -= deltaTime;
            if (dashCooldown < 0f) dashCooldown = 0f;
        }

        Controller controller = Controller.MAPPER.get(entity);
        if (controller.getPressedCommands().isEmpty() && controller.getReleasedCommands().isEmpty()) {
            return;
        }

        if (viewModel.isMenuOpen()) {
            if (controller.getPressedCommands().contains(Command.TOGGLE_MENU) ||
                controller.getPressedCommands().contains(Command.CANCEL)) {
                viewModel.toggleMenu();
            } else if (controller.getPressedCommands().contains(Command.LEFT)) {
                viewModel.prevTab();
            } else if (controller.getPressedCommands().contains(Command.RIGHT)) {
                viewModel.nextTab();
            }
            controller.getPressedCommands().clear();
            controller.getReleasedCommands().clear();
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
                case UP    -> moveEntity(entity, 0f, 1f);
                case DOWN  -> moveEntity(entity, 0f, -1f);
                case LEFT  -> moveEntity(entity, -1f, 0f);
                case RIGHT -> moveEntity(entity, 1f, 0f);
                case SELECT        -> handleAttack(entity);
                case CANCEL        -> game.setScreen(MenuScreen.class);
                case USE_ITEM      -> usePotion(entity);
                case INTERACT      -> interactWithNpc(entity);
                case DASH          -> startDash(entity);
                case SWITCH_WEAPON -> switchWeapon(entity);
                case TOGGLE_MENU   -> viewModel.toggleMenu();
            }
        }
        controller.getPressedCommands().clear();

        for (Command command : controller.getReleasedCommands()) {
            switch (command) {
                case UP    -> moveEntity(entity, 0f, -1f);
                case DOWN  -> moveEntity(entity, 0f, 1f);
                case LEFT  -> moveEntity(entity, 1f, 0f);
                case RIGHT -> moveEntity(entity, -1f, 0f);
            }
        }
        controller.getReleasedCommands().clear();
    }

    // =========================================================================
    // Weapon switching
    // =========================================================================

    private void switchWeapon(Entity entity) {
        CombatState cs = CombatState.MAPPER.get(entity);
        if (cs == null || cs.getUnlockedWeapons().size() <= 1) return;

        cs.nextWeapon();
        Weapon w = cs.getCurrentWeapon();

        // Floating text thông báo tên vũ khí
        Transform t = Transform.MAPPER.get(entity);
        if (t != null) {
            float x = t.getPosition().x + t.getSize().x * 0.5f;
            float y = t.getPosition().y + t.getSize().y;
            viewModel.showFloatingText("[CYAN]" + w.displayName + "[]", x, y);
        }
        viewModel.updateWeaponName(w.displayName);
    }

    // =========================================================================
    // Attack: cận chiến hoặc bắn đạn tuỳ vũ khí hiện tại
    // =========================================================================

    private void handleAttack(Entity entity) {
        CombatState cs = CombatState.MAPPER.get(entity);
        Weapon weapon = (cs != null) ? cs.getCurrentWeapon() : null;

        if (weapon == null || weapon.isMelee()) {
            // Cận chiến — dùng Attack component gốc
            startEntityAttack(entity);
        } else {
            // Bắn đạn
            fireProjectile(entity, weapon);
        }
    }

    private void startEntityAttack(Entity entity) {
        Attack attack = Attack.MAPPER.get(entity);
        if (attack != null && attack.canAttack()) {
            attack.startAttack();
        }
    }

    // =========================================================================
    // Projectile spawn
    // =========================================================================

    private void fireProjectile(Entity shooter, Weapon weapon) {
        ProjectileType projType = weapon.projectileType;
        if (projType == null) return;

        // Cooldown đơn giản: dùng Attack component của player
        Attack attack = Attack.MAPPER.get(shooter);
        if (attack != null && !attack.canAttack()) return;
        if (attack != null) attack.startAttack();

        // Xác định hướng bắn
        FacingDirection facing = Facing.MAPPER.get(shooter).getDirection();
        Vector2 dir = directionVector(facing);

        // Auto-aim cho Fireball
        if (projType.autoAim) {
            Vector2 aimTarget = findNearestEnemy(shooter);
            if (aimTarget != null) {
                Transform playerT = Transform.MAPPER.get(shooter);
                Vector2 origin = playerT.getPosition();
                dir = aimTarget.sub(origin).nor();
            }
        }

        // Vị trí xuất phát = tâm nhân vật
        Transform playerT = Transform.MAPPER.get(shooter);
        float spawnX = playerT.getPosition().x + playerT.getSize().x * 0.5f;
        float spawnY = playerT.getPosition().y + playerT.getSize().y * 0.5f;

        spawnProjectileEntity(shooter, projType, spawnX, spawnY, dir, weapon.damage);
    }

    private void spawnProjectileEntity(Entity owner, ProjectileType type,
                                       float x, float y,
                                       Vector2 dir, float damage) {
        Engine engine = getEngine();
        Entity entity = engine.createEntity();

        // --- Transform ---
        float size = 0.25f; // ~8px tại 32px/unit
        Transform transform = new Transform(
            new Vector2(x - size * 0.5f, y - size * 0.5f),
            1,                          // z-layer (trên mặt đất)
            new Vector2(size, size),
            new Vector2(1f, 1f),
            0f,                         // rotationDeg
            0f                          // sortOffsetY
        );
        entity.add(transform);

        // --- Graphic: lấy region từ atlas ---
        TextureAtlas atlas = assetService.get(AtlasAsset.OBJECTS);
        TextureAtlas.AtlasRegion region = atlas.findRegion(type.atlasKey);
        if (region == null) region = atlas.findRegions(type.atlasKey).first();
        entity.add(new Graphic(region, Color.WHITE));

        // --- Attack (chứa damage) ---
        entity.add(new Attack(damage, 0f, null));

        // --- Projectile ---
        Projectile proj = new Projectile(owner, type);
        Vector2 vel = dir.nor().scl(type.speed);
        proj.getVelocity().set(vel);
        entity.add(proj);

        // --- Box2D body (sensor circle) ---
        BodyDef bDef = new BodyDef();
        bDef.type            = BodyDef.BodyType.DynamicBody;
        bDef.position.set(x, y);
        bDef.fixedRotation   = true;
        bDef.linearDamping   = 0f;
        Body body = physicWorld.createBody(bDef);
        body.setUserData(entity);

        CircleShape circle = new CircleShape();
        circle.setRadius(size * 0.5f);
        FixtureDef fDef = new FixtureDef();
        fDef.shape    = circle;
        fDef.isSensor = true;   // sensor: không đẩy vật lý, chỉ detect va chạm
        body.createFixture(fDef);
        circle.dispose();

        body.setLinearVelocity(vel.x, vel.y);
        entity.add(new Physic(body, new Vector2(body.getPosition())));

        engine.addEntity(entity);
    }

    // =========================================================================
    // Auto-aim: tìm kẻ địch gần nhất trong tầm AUTO_AIM_RANGE
    // =========================================================================

    private Vector2 findNearestEnemy(Entity shooter) {
        Transform shooterT = Transform.MAPPER.get(shooter);
        if (shooterT == null) return null;

        Vector2 origin = shooterT.getPosition();
        float minDist = AUTO_AIM_RANGE;
        Vector2 best  = null;

        for (Entity e : getEngine().getEntitiesFor(Family.all(Life.class, Transform.class).get())) {
            if (e == shooter) continue;
            if (Player.MAPPER.has(e)) continue; // không tự aim mình

            Transform t = Transform.MAPPER.get(e);
            float dist  = origin.dst(t.getPosition());
            if (dist < minDist) {
                minDist = dist;
                best    = new Vector2(t.getPosition().x + t.getSize().x * 0.5f,
                                      t.getPosition().y + t.getSize().y * 0.5f);
            }
        }
        return best;
    }

    // =========================================================================
    // Dash
    // =========================================================================

    private void startDash(Entity entity) {
        if (dashCooldown > 0f) return; // còn hồi chiêu

        Move move = Move.MAPPER.get(entity);
        if (move == null) return;

        // Hướng dash = hướng đang chạy hoặc hướng nhìn nếu đứng yên
        if (!move.getDirection().isZero()) {
            dashDir.set(move.getDirection()).nor();
        } else {
            FacingDirection f = Facing.MAPPER.get(entity).getDirection();
            dashDir.set(directionVector(f));
        }

        dashTimer    = DASH_DURATION;
        dashCooldown = DASH_COOLDOWN;

        // Chuyển animation sang ROLL
        Animation2D anim = Animation2D.MAPPER.get(entity);
        if (anim != null) anim.setType(AnimationType.ROLL);

        // Áp lực vận tốc trực tiếp lên Body
        Physic physic = Physic.MAPPER.get(entity);
        if (physic != null) {
            Body body = physic.getBody();
            body.setLinearVelocity(dashDir.x * DASH_SPEED, dashDir.y * DASH_SPEED);
        }
    }

    // =========================================================================
    // Helper: uống thuốc
    // =========================================================================

    private void usePotion(Entity entity) {
        Inventory inventory = Inventory.MAPPER.get(entity);
        Life life = Life.MAPPER.get(entity);
        if (inventory == null || life == null) return;

        if (inventory.getItemCount(Item.Type.POTION_HEALTH) <= 0) return;
        if (life.getLife() >= life.getMaxLife()) return;

        life.addLife(POTION_HEAL_AMOUNT);
        inventory.removeItem(Item.Type.POTION_HEALTH, 1);
        audioService.playSound(SoundAsset.HEAL);
        viewModel.updateLifeInfo(life.getMaxLife(), life.getLife());

        Transform transform = Transform.MAPPER.get(entity);
        if (transform != null) {
            float x = transform.getPosition().x + transform.getSize().x * 0.5f;
            float y = transform.getPosition().y + transform.getSize().y;
            viewModel.showFloatingText("[GREEN]+" + (int) POTION_HEAL_AMOUNT + " HP[]", x, y);
        }

        viewModel.updateInventory(
            inventory.getItemCount(Item.Type.POTION_HEALTH),
            inventory.getItemCount(Item.Type.COIN),
            inventory.getItemCount(Item.Type.KEY),
            inventory.getItemCount(Item.Type.GOLD_KEY),
            inventory.getItemCount(Item.Type.SILVER_KEY),
            inventory.getItemCount(Item.Type.SOOTHING_HERB),
            inventory.getItemCount(Item.Type.JUNGLE_MAP_KEY),
            inventory.getItemCount(Item.Type.SILVER_CUP),
            inventory.getItemCount(Item.Type.LAUREL_LEAF),
            inventory.getItemCount(Item.Type.ICE_MAP_KEY)
        );
    }

    private void moveEntity(Entity entity, float dx, float dy) {
        Move move = Move.MAPPER.get(entity);
        if (move != null) {
            move.getDirection().x += dx;
            move.getDirection().y += dy;
        }
    }

    // =========================================================================
    // NPC Interaction (unchanged)
    // =========================================================================

    private void interactWithNpc(Entity player) {
        if (activeNpcEntity != null) {
            if (viewModel.isDialogueTyping()) {
                viewModel.skipDialogueTyping();
                return;
            }
            Npc npc = Npc.MAPPER.get(activeNpcEntity);
            npc.advanceDialogue();
            if (npc.hasMoreDialogue()) {
                displayNpcDialogue(activeNpcEntity, npc);
            } else {
                viewModel.hideDialogue();
                Animation2D animation2D = Animation2D.MAPPER.get(activeNpcEntity);
                if (animation2D != null) {
                    animation2D.setType(Animation2D.AnimationType.IDLE);
                }
                npc.resetDialogue();
                activeNpcEntity = null;
            }
            return;
        }

        Transform playerTransform = Transform.MAPPER.get(player);
        if (playerTransform == null) return;

        Entity closestNpc = null;
        float minDistance = 1.5f;

        for (Entity npcEntity : getEngine().getEntitiesFor(Family.all(Npc.class, Transform.class).get())) {
            Transform npcTransform = Transform.MAPPER.get(npcEntity);
            float dist = playerTransform.getPosition().dst(npcTransform.getPosition());
            if (dist < minDistance) {
                minDistance = dist;
                closestNpc = npcEntity;
            }
        }

        Entity closestChest = null;
        float minChestDistance = 1.5f;
        for (Entity chestEntity : getEngine().getEntitiesFor(Family.all(io.github.com.group31.component.Chest.class, Transform.class).get())) {
            io.github.com.group31.component.Chest chest = io.github.com.group31.component.Chest.MAPPER.get(chestEntity);
            if (chest.isOpen()) continue;
            Transform chestTransform = Transform.MAPPER.get(chestEntity);
            float dist = playerTransform.getPosition().dst(chestTransform.getPosition());
            if (dist < minChestDistance) {
                minChestDistance = dist;
                closestChest = chestEntity;
            }
        }

        if (closestChest != null) {
            io.github.com.group31.component.Chest chest = io.github.com.group31.component.Chest.MAPPER.get(closestChest);
            chest.setOpen(true);
            
            io.github.com.group31.component.Respawnable respawnable = closestChest.getComponent(io.github.com.group31.component.Respawnable.class);
            if (respawnable != null) {
                io.github.com.group31.save.RespawnState.getInstance().registerDeath(respawnable.getEntityId(), respawnable.getRespawnTimeSec());
            }

            Graphic graphic = Graphic.MAPPER.get(closestChest);
            if (graphic != null && chest.getOpenRegion() != null) {
                graphic.setRegion(chest.getOpenRegion());
            }
            audioService.playSound(SoundAsset.PICKUP);

            io.github.com.group31.component.Tiled tiled = io.github.com.group31.component.Tiled.MAPPER.get(closestChest);
            if (tiled != null && tiled.getMapObjectRef() != null && "chest2".equalsIgnoreCase(tiled.getMapObjectRef().getName())) {
                configurator.spawnChest2Skulls();
            }

            Transform chestTransform = Transform.MAPPER.get(closestChest);
            String lootType = chest.getLootType();
            
            if (lootType.startsWith("WEAPON_")) {
                CombatState combatState = CombatState.MAPPER.get(player);
                if (combatState != null) {
                    io.github.com.group31.combat.Weapon unlocked = null;
                    if (lootType.endsWith("SWORD")) unlocked = io.github.com.group31.combat.Weapon.SWORD;
                    else if (lootType.endsWith("BOW")) unlocked = io.github.com.group31.combat.Weapon.BOW;
                    else if (lootType.endsWith("MAGIC_WAND")) unlocked = io.github.com.group31.combat.Weapon.MAGIC_WAND;
                    else if (lootType.endsWith("RUSTY_SWORD")) unlocked = io.github.com.group31.combat.Weapon.RUSTY_SWORD;
                    
                    if (unlocked != null) {
                        boolean newlyUnlocked = combatState.unlockWeapon(unlocked);
                        if (newlyUnlocked) {
                            viewModel.showFloatingText("[YELLOW]Nhận: " + unlocked.displayName + "![]", 
                                chestTransform.getPosition().x, chestTransform.getPosition().y + 1f);
                        } else {
                            viewModel.showFloatingText("Đã có " + unlocked.displayName + "!", 
                                chestTransform.getPosition().x, chestTransform.getPosition().y + 1f);
                        }
                        java.util.List<String> wNames = new java.util.ArrayList<>();
                        for (io.github.com.group31.combat.Weapon w : combatState.getUnlockedWeapons()) {
                            wNames.add(w.name());
                        }
                        viewModel.updateUnlockedWeapons(wNames);
                    }
                }
            } else if ("EXPLOSION".equalsIgnoreCase(lootType) || "BOMB".equalsIgnoreCase(lootType) || "TRAP".equalsIgnoreCase(lootType)) {
                audioService.playSound(SoundAsset.TRAP);
                float dmg = chest.getTrapDamage();
                viewModel.showFloatingText("[RED]It's a trap! -" + (int)dmg + " HP[]", chestTransform.getPosition().x, chestTransform.getPosition().y + 1f);
                
                Damaged currentDamage = Damaged.MAPPER.get(player);
                if (currentDamage != null) {
                    currentDamage.addDamage(dmg);
                } else {
                    player.add(new Damaged(dmg, closestChest));
                }
            } else {
                try {
                    Item.Type type = Item.Type.valueOf(lootType);
                    Inventory inventory = Inventory.MAPPER.get(player);
                    if (inventory != null) {
                        inventory.addItem(type, 1);
                        viewModel.showFloatingText("+1 " + type.name(), chestTransform.getPosition().x, chestTransform.getPosition().y + 1f);
                        viewModel.updateInventory(
                            inventory.getItemCount(Item.Type.POTION_HEALTH),
                            inventory.getItemCount(Item.Type.COIN),
                            inventory.getItemCount(Item.Type.KEY),
                            inventory.getItemCount(Item.Type.GOLD_KEY),
                            inventory.getItemCount(Item.Type.SILVER_KEY),
                            inventory.getItemCount(Item.Type.SOOTHING_HERB),
                            inventory.getItemCount(Item.Type.JUNGLE_MAP_KEY),
                            inventory.getItemCount(Item.Type.SILVER_CUP),
                            inventory.getItemCount(Item.Type.LAUREL_LEAF),
                            inventory.getItemCount(Item.Type.ICE_MAP_KEY)
                        );
                    }
                } catch (Exception e) {
                    com.badlogic.gdx.Gdx.app.error("ControllerSystem", "Invalid loot type in chest: " + lootType);
                }
            }
            return;
        }

        if (closestNpc != null) {
            Npc npc = Npc.MAPPER.get(closestNpc);
            if (npc != null && "coin1".equalsIgnoreCase(npc.getName())) {
                getEngine().removeEntity(closestNpc);
                Transform coinTransform = Transform.MAPPER.get(closestNpc);
                if (coinTransform != null) {
                    float cx = coinTransform.getPosition().x;
                    float cy = coinTransform.getPosition().y;
                    configurator.spawnBomb(cx - 1f, cy);
                    configurator.spawnBomb(cx + 1f, cy);
                }
                return;
            }

            if (npc != null && "coin2".equalsIgnoreCase(npc.getName())) {
                getEngine().removeEntity(closestNpc);
                configurator.spawnCoin2Slimes();
                return;
            }

            if (npc != null && "coin3".equalsIgnoreCase(npc.getName())) {
                audioService.playSound(SoundAsset.COIN);
                Inventory inventory = Inventory.MAPPER.get(player);
                if (inventory != null) {
                    inventory.addItem(Item.Type.COIN, 1);
                    viewModel.updateInventory(
                        inventory.getItemCount(Item.Type.POTION_HEALTH),
                        inventory.getItemCount(Item.Type.COIN),
                        inventory.getItemCount(Item.Type.KEY),
                        inventory.getItemCount(Item.Type.GOLD_KEY),
                        inventory.getItemCount(Item.Type.SILVER_KEY),
                        inventory.getItemCount(Item.Type.SOOTHING_HERB),
                        inventory.getItemCount(Item.Type.JUNGLE_MAP_KEY),
                        inventory.getItemCount(Item.Type.SILVER_CUP),
                        inventory.getItemCount(Item.Type.LAUREL_LEAF),
                        inventory.getItemCount(Item.Type.ICE_MAP_KEY)
                    );
                }
                getEngine().removeEntity(closestNpc);
                return;
            }

            if (npc != null && "silvercup".equalsIgnoreCase(npc.getName())) {
                audioService.playSound(SoundAsset.PICKUP);
                Inventory inventory = Inventory.MAPPER.get(player);
                if (inventory != null) {
                    inventory.addItem(Item.Type.SILVER_CUP, 1);
                    viewModel.updateInventory(
                        inventory.getItemCount(Item.Type.POTION_HEALTH),
                        inventory.getItemCount(Item.Type.COIN),
                        inventory.getItemCount(Item.Type.KEY),
                        inventory.getItemCount(Item.Type.GOLD_KEY),
                        inventory.getItemCount(Item.Type.SILVER_KEY),
                        inventory.getItemCount(Item.Type.SOOTHING_HERB),
                        inventory.getItemCount(Item.Type.JUNGLE_MAP_KEY),
                        inventory.getItemCount(Item.Type.SILVER_CUP),
                        inventory.getItemCount(Item.Type.LAUREL_LEAF),
                        inventory.getItemCount(Item.Type.ICE_MAP_KEY)
                    );
                    Transform transform = Transform.MAPPER.get(player);
                    if (transform != null) {
                        viewModel.showFloatingText("[YELLOW]Nhat: Silver Cup![]", 
                            transform.getPosition().x, transform.getPosition().y + 1f);
                    }
                }
                getEngine().removeEntity(closestNpc);
                return;
            }

            if (npc != null && "Heart Container".equalsIgnoreCase(npc.getName())) {
                audioService.playSound(SoundAsset.HEAL);
                Life life = Life.MAPPER.get(player);
                if (life != null) {
                    life.setMaxLife(life.getMaxLife() + 4);
                    life.addLife(4);
                    viewModel.updateLifeInfo(life.getMaxLife(), life.getLife());
                }
                viewModel.showFloatingText("[RED]+4 Max HP![]", 
                    playerTransform.getPosition().x, playerTransform.getPosition().y + 1f);


                getEngine().removeEntity(closestNpc);
                return;
            }

            if (npc != null && ("Gold Key".equalsIgnoreCase(npc.getName()) || "Silver Key".equalsIgnoreCase(npc.getName()) || "Ice Map Key".equalsIgnoreCase(npc.getName()))) {
                audioService.playSound(SoundAsset.PICKUP);
                Inventory inventory = Inventory.MAPPER.get(player);
                if (inventory != null) {
                    if ("Gold Key".equalsIgnoreCase(npc.getName())) {
                        inventory.addItem(Item.Type.GOLD_KEY, 1);
                        viewModel.showFloatingText("[GOLD]+1 Gold Key![]", 
                            playerTransform.getPosition().x, playerTransform.getPosition().y + 1f);
                        io.github.com.group31.quest.QuestManager.INSTANCE.checkGoldKeyPickup(player);
                    } else if ("Ice Map Key".equalsIgnoreCase(npc.getName())) {
                        inventory.addItem(Item.Type.ICE_MAP_KEY, 1);
                        viewModel.showFloatingText("[GREEN]+1 Ice Map Key![]", 
                            playerTransform.getPosition().x, playerTransform.getPosition().y + 1f);
                    } else {
                        inventory.addItem(Item.Type.SILVER_KEY, 1);
                        viewModel.showFloatingText("[LIGHT_GRAY]+1 Silver Key![]", 
                            playerTransform.getPosition().x, playerTransform.getPosition().y + 1f);
                    }
                    viewModel.updateInventory(
                        inventory.getItemCount(Item.Type.POTION_HEALTH),
                        inventory.getItemCount(Item.Type.COIN),
                        inventory.getItemCount(Item.Type.KEY),
                        inventory.getItemCount(Item.Type.GOLD_KEY),
                        inventory.getItemCount(Item.Type.SILVER_KEY),
                        inventory.getItemCount(Item.Type.SOOTHING_HERB),
                        inventory.getItemCount(Item.Type.JUNGLE_MAP_KEY),
                        inventory.getItemCount(Item.Type.SILVER_CUP),
                        inventory.getItemCount(Item.Type.LAUREL_LEAF),
                        inventory.getItemCount(Item.Type.ICE_MAP_KEY)
                    );
                }
                getEngine().removeEntity(closestNpc);
                return;
            }

            if (npc != null && npc.getName() != null && npc.getName().startsWith("WEAPON_")) {
                audioService.playSound(SoundAsset.PICKUP);
                CombatState combatState = CombatState.MAPPER.get(player);
                if (combatState != null) {
                    io.github.com.group31.combat.Weapon unlocked = null;
                    if (npc.getName().endsWith("SWORD")) {
                        unlocked = io.github.com.group31.combat.Weapon.SWORD;
                    } else if (npc.getName().endsWith("BOW")) {
                        unlocked = io.github.com.group31.combat.Weapon.BOW;
                    } else if (npc.getName().endsWith("MAGIC_WAND")) {
                        unlocked = io.github.com.group31.combat.Weapon.MAGIC_WAND;
                    } else if (npc.getName().endsWith("RUSTY_SWORD")) {
                        unlocked = io.github.com.group31.combat.Weapon.RUSTY_SWORD;
                    }

                    if (unlocked != null) {
                        boolean newlyUnlocked = combatState.unlockWeapon(unlocked);
                        if (newlyUnlocked) {
                            viewModel.showFloatingText("[YELLOW]Nhận: " + unlocked.displayName + "![]", 
                                playerTransform.getPosition().x, playerTransform.getPosition().y + 1f);
                            io.github.com.group31.quest.QuestManager.INSTANCE.checkWeaponPickup(player);
                        } else {
                            viewModel.showFloatingText("Đã có " + unlocked.displayName + "!", 
                                playerTransform.getPosition().x, playerTransform.getPosition().y + 1f);
                        }
                        
                        // Sync to viewModel
                        java.util.List<String> wNames = new java.util.ArrayList<>();
                        for (io.github.com.group31.combat.Weapon w : combatState.getUnlockedWeapons()) {
                            wNames.add(w.name());
                        }
                        viewModel.updateUnlockedWeapons(wNames);
                    }
                }
                getEngine().removeEntity(closestNpc);
                return;
            }

            activeNpcEntity = closestNpc;
            Move move = Move.MAPPER.get(player);
            if (move != null) {
                move.getDirection().setZero();
            }

            // Cập nhật dialogue của NPC dựa trên tiến trình Quest trước khi hiển thị
            io.github.com.group31.quest.QuestManager.INSTANCE.onTalkToNpc(activeNpcEntity, player);

            npc.resetDialogue();
            displayNpcDialogue(activeNpcEntity, npc);
        }
    }

    private void displayNpcDialogue(Entity npcEntity, Npc npc) {
        String rawLine = npc.getCurrentLine();
        String cleanLine = rawLine;
        Animation2D.AnimationType animType = Animation2D.AnimationType.IDLE;

        if (rawLine.startsWith("[")) {
            int closeBracket = rawLine.indexOf("]");
            if (closeBracket > 0) {
                String tag = rawLine.substring(1, closeBracket).toUpperCase();
                try {
                    animType = Animation2D.AnimationType.valueOf(tag);
                    cleanLine = rawLine.substring(closeBracket + 1);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        Animation2D animation2D = Animation2D.MAPPER.get(npcEntity);
        if (animation2D != null) {
            animation2D.setType(animType);
        }

        viewModel.showDialogue(npc.getName(), cleanLine, npc.getFacesetPath());
    }

    // =========================================================================
    // Utility
    // =========================================================================

    /** Chuyển FacingDirection thành Vector2 đơn vị. */
    private static Vector2 directionVector(FacingDirection dir) {
        return switch (dir) {
            case UP    -> new Vector2(0f,  1f);
            case DOWN  -> new Vector2(0f, -1f);
            case LEFT  -> new Vector2(-1f, 0f);
            case RIGHT -> new Vector2(1f,  0f);
        };
    }
}
