package io.github.com.group31.screen;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.com.group31.GdxGame;
import io.github.com.group31.asset.MapAsset;
import io.github.com.group31.asset.SkinAsset;
import io.github.com.group31.audio.AudioService;
import io.github.com.group31.component.*;
import io.github.com.group31.component.Inventory;
import io.github.com.group31.component.Item;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import io.github.com.group31.input.GameControllerState;
import io.github.com.group31.input.KeyboardController;
import io.github.com.group31.save.SaveData;
import io.github.com.group31.save.SaveService;
import io.github.com.group31.system.AiSystem;
import io.github.com.group31.system.AnimationSystem;
import io.github.com.group31.system.SlashFxLifetimeSystem;
import io.github.com.group31.system.SlashFxSystem;
import io.github.com.group31.system.WeaponHandSystem;
import io.github.com.group31.system.AttackSystem;
import io.github.com.group31.system.CameraSystem;
import io.github.com.group31.system.ControllerSystem;
import io.github.com.group31.system.DamagedSystem;
import io.github.com.group31.system.DeadSystem;
import io.github.com.group31.system.FacingSystem;
import io.github.com.group31.system.FsmSystem;
import io.github.com.group31.system.ItemSystem;
import io.github.com.group31.system.LifeSystem;
import io.github.com.group31.system.PhysicDebugRenderSystem;
import io.github.com.group31.system.PhysicMoveSystem;
import io.github.com.group31.system.PhysicSystem;
import io.github.com.group31.system.ProjectileSystem;
import io.github.com.group31.system.RenderSystem;
import io.github.com.group31.system.SpawnSystem;
import io.github.com.group31.system.TriggerSystem;
import io.github.com.group31.tiled.TiledAshleyConfigurator;
import io.github.com.group31.tiled.TiledService;
import io.github.com.group31.ui.model.GameViewModel;
import io.github.com.group31.ui.view.GameView;

import java.util.function.Consumer;

public class GameScreen extends ScreenAdapter {
    private final GdxGame game;
    private final Stage stage;
    private final Skin skin;
    private final GameViewModel viewModel;
    private final Viewport uiViewport;
    private final TiledService tiledService;
    private final Engine engine;
    private final TiledAshleyConfigurator tiledAshleyConfigurator;
    private final World physicWorld;
    private final KeyboardController keyboardController;
    private final AudioService audioService;
    private GameView gameView;

    public GameScreen(GdxGame game) {
        this.game = game;
        this.uiViewport = new FitViewport(320f, 180f);
        this.stage = new Stage(uiViewport, game.getBatch());
        this.skin = game.getAssetService().get(SkinAsset.DEFAULT);
        this.viewModel = new GameViewModel(game);
        this.audioService = game.getAudioService();
        this.physicWorld = new World(Vector2.Zero, true);
        this.physicWorld.setAutoClearForces(false);
        this.tiledService = new TiledService(game.getAssetService(), this.physicWorld);
        this.engine = new Engine();
        this.tiledAshleyConfigurator = new TiledAshleyConfigurator(this.engine, this.physicWorld, this.game.getAssetService());
        this.keyboardController = new KeyboardController(GameControllerState.class, engine, null);

        this.engine.addSystem(new AiSystem());
        this.engine.addSystem(new SpawnSystem(this.tiledAshleyConfigurator));
        this.engine.addSystem(new PhysicMoveSystem());
        this.engine.addSystem(new PhysicSystem(physicWorld, 1 / 60f));
        this.engine.addSystem(new FacingSystem());
        this.engine.addSystem(new AttackSystem(physicWorld, audioService));
        this.engine.addSystem(new FsmSystem());
        // DamagedSystem must run after FsmSystem to correctly
        // detect when a damaged animation should be played.
        // This is done by checking if an entity has a Damaged component,
        // and this component is removed in the DamagedSystem.
        this.engine.addSystem(new DamagedSystem(viewModel));
        this.engine.addSystem(new TriggerSystem(audioService));
        this.engine.addSystem(new ItemSystem(audioService, viewModel));
        this.engine.addSystem(new LifeSystem(this.viewModel));
        this.engine.addSystem(new DeadSystem(this.viewModel));
        this.engine.addSystem(new AnimationSystem(game.getAssetService()));
        this.engine.addSystem(new CameraSystem(game.getCamera()));
        this.engine.addSystem(new SlashFxLifetimeSystem());
        this.engine.addSystem(new SlashFxSystem(this.engine, game.getAssetService()));
        this.engine.addSystem(new WeaponHandSystem(game.getBatch(), game.getAssetService()));
        this.engine.addSystem(new RenderSystem(game.getBatch(), game.getViewport(), game.getCamera()));
        this.engine.addSystem(new PhysicDebugRenderSystem(this.physicWorld, game.getCamera()));
        this.engine.addSystem(new ProjectileSystem());
        this.engine.addSystem(new ControllerSystem(game, audioService, viewModel,
            physicWorld, game.getAssetService()));
    }

    @Override
    public void show() {
        this.game.setInputProcessors(stage, keyboardController);
        keyboardController.setActiveState(GameControllerState.class);

        gameView = new GameView(stage, skin, this.viewModel, this.game.getAssetService());
        this.stage.addActor(gameView);
        this.viewModel.onPropertyChange(GameViewModel.PLAYER_DEAD, Boolean.class, isDead -> {
            if (Boolean.TRUE.equals(isDead)) {
                com.badlogic.gdx.Gdx.app.debug("GameScreen", "PLAYER_DEAD received! Switching to GameOverScreen...");
                // postRunnable đảm bảo chuyển màn hình xảy ra NGOÀI vòng lặp engine.update()
                // tránh ConcurrentModificationException từ Ashley khi remove entity trong khi đang iterate
                com.badlogic.gdx.Gdx.app.postRunnable(() -> this.game.setScreen(GameOverScreen.class));
            }
        });

        Consumer<TiledMap> renderConsumer = this.engine.getSystem(RenderSystem.class)::setMap;
        Consumer<TiledMap> cameraConsumer = this.engine.getSystem(CameraSystem.class)::setMap;
        Consumer<TiledMap> audioConsumer = this.audioService::setMap;
        Consumer<TiledMap> spawnConsumer = this.tiledAshleyConfigurator::setCurrentMap;
        this.tiledService.setMapChangeConsumer(
            spawnConsumer.andThen(renderConsumer).andThen(cameraConsumer).andThen(audioConsumer));
        this.tiledService.setLoadTriggerConsumer(tiledAshleyConfigurator::onLoadTrigger);
        this.tiledService.setLoadObjectConsumer(tiledAshleyConfigurator::onLoadObject);
        this.tiledService.setLoadTileConsumer(tiledAshleyConfigurator::onLoadTile);

//        TiledMap startMap = this.tiledService.loadMap(MapAsset.ICEMAP1);
//        this.tiledService.setMap(startMap);

        // Load Game State

        this.engine.getSystem(TriggerSystem.class).registerTrigger("portal_trigger", this::portalTrigger);
        MapAsset startMapAsset = MapAsset.ICEMAP1;
        SaveService saveService = game.getSaveService();
        SaveData data = null;
        if(saveService != null && saveService.hasSaveFile()){
            data = saveService.load();
            if(data != null && data.mapName != null){
                try{
                    startMapAsset = MapAsset.valueOf(data.mapName.toUpperCase());
                } catch(IllegalArgumentException e){
                    startMapAsset = MapAsset.ICEMAP1;
                }
            }
        }

        TiledMap startMap = this.tiledService.loadMap(startMapAsset);
        this.tiledService.setMap(startMap);

        if (data != null) {
            ImmutableArray<com.badlogic.ashley.core.Entity> players =
                engine.getEntitiesFor(Family.all(Player.class).get());
            if(players.size() > 0){
                com.badlogic.ashley.core.Entity player = players.first();

                // Setup QuestManager with loaded quest stage
                io.github.com.group31.quest.QuestManager.INSTANCE.setViewModel(viewModel);
                io.github.com.group31.quest.QuestManager.INSTANCE.setStage(data.questStage);

                Life life = Life.MAPPER.get(player);
                if(life != null){
                    life.setLife(data.playerHp);
                    viewModel.updateLifeInfo(life.getMaxLife(), life.getLife());
                }

                Experience xp = Experience.MAPPER.get(player);
                if(xp != null){
                    xp.setXp(data.playerXp);
                    xp.setLevel(data.playerLevel);
                    xp.setXpToNextLevel(data.playerLevel * 100f);
                    viewModel.updateXpInfo(data.playerXp, data.playerLevel*100f, data.playerLevel, false);
                }

                Physic physic = Physic.MAPPER.get(player);
                if(physic != null){
                    physic.getBody().setTransform(data.playerX, data.playerY, 0f);
                    physic.getPrevPosition().set(data.playerX, data.playerY);
                }

                // Khôi phục Inventory từ file lưu
                Inventory inventory = Inventory.MAPPER.get(player);
                if (inventory != null) {
                    inventory.setItemCount(Item.Type.POTION_HEALTH, data.playerPotions);
                    inventory.setItemCount(Item.Type.COIN,          data.playerCoins);
                    inventory.setItemCount(Item.Type.KEY,           data.playerKeys);
                    viewModel.updateInventory(data.playerPotions, data.playerCoins, data.playerKeys);
                }

                // Khôi phục CombatState từ file lưu
                CombatState cs = CombatState.MAPPER.get(player);
                if (cs != null) {
                    if (data.unlockedWeapons != null) {
                        cs.getUnlockedWeapons().clear();
                        for (String wName : data.unlockedWeapons) {
                            try {
                                cs.unlockWeapon(io.github.com.group31.combat.Weapon.valueOf(wName));
                            } catch (IllegalArgumentException e) {
                                // Ignored
                            }
                        }
                        cs.setCurrentIndex(data.currentWeaponIndex);
                    }
                    // Sync to viewModel
                    java.util.List<String> wNames = new java.util.ArrayList<>();
                    for (io.github.com.group31.combat.Weapon w : cs.getUnlockedWeapons()) {
                        wNames.add(w.name());
                    }
                    viewModel.updateUnlockedWeapons(wNames);
                }

                // Check map transition for quest progress
                io.github.com.group31.quest.QuestManager.INSTANCE.checkMapEnter(startMapAsset.name(), player);
            }
        } else {
            // New game: setup viewModel on QuestManager and start at stage 0
            io.github.com.group31.quest.QuestManager.INSTANCE.setViewModel(viewModel);
            io.github.com.group31.quest.QuestManager.INSTANCE.setStage(0);
            viewModel.updateUnlockedWeapons(java.util.List.of("FIST"));
        }
    }

    @Override
    public void hide() {
        // Chỉ lưu save khi player còn sống (HP > 0)
        // Nếu lưu HP=0 vào file, lần sau load lên sẽ chết ngay lập tức
        SaveService saveService = game.getSaveService();
        if (saveService != null) {
            ImmutableArray<com.badlogic.ashley.core.Entity> players = engine.getEntitiesFor(Family.all(Player.class).get());
            if (players.size() > 0) {
                com.badlogic.ashley.core.Entity player = players.first();

                Life life = Life.MAPPER.get(player);
                float currentHp = life != null ? life.getLife() : 0f;

                // Không lưu nếu player đã chết
                if (currentHp > 0f) {
                    SaveData data = new SaveData();
                    data.playerHp = currentHp;
                    data.questStage = io.github.com.group31.quest.QuestManager.INSTANCE.getStage();

                    Experience xp = Experience.MAPPER.get(player);
                    if (xp != null) {
                        data.playerXp = xp.getXp();
                        data.playerLevel = xp.getLevel();
                    } else {
                        data.playerXp = 0f;
                        data.playerLevel = 1;
                    }

                    Physic physic = Physic.MAPPER.get(player);
                    if (physic != null) {
                        data.playerX = physic.getBody().getPosition().x;
                        data.playerY = physic.getBody().getPosition().y;
                    }

                    MapAsset currentAsset = MapAsset.ICEMAP1;
                    if (tiledService.getCurrentMap() != null) {
                        currentAsset = tiledService.getCurrentMap().getProperties().get("mapAsset", MapAsset.class);
                    }
                    data.mapName = currentAsset.name();

                    // Lưu trạng thái Inventory
                    Inventory inventory = Inventory.MAPPER.get(player);
                    if (inventory != null) {
                        data.playerPotions = inventory.getItemCount(Item.Type.POTION_HEALTH);
                        data.playerCoins   = inventory.getItemCount(Item.Type.COIN);
                        data.playerKeys    = inventory.getItemCount(Item.Type.KEY);
                    }

                    // Lưu trạng thái CombatState (vũ khí)
                    CombatState cs = CombatState.MAPPER.get(player);
                    if (cs != null) {
                        data.unlockedWeapons = new java.util.ArrayList<>();
                        for (io.github.com.group31.combat.Weapon w : cs.getUnlockedWeapons()) {
                            data.unlockedWeapons.add(w.name());
                        }
                        data.currentWeaponIndex = cs.getCurrentIndex();
                    }

                    saveService.save(data);
                }
            }
        }

        this.viewModel.clearPropertyChanges();
        this.engine.removeAllEntities();
        this.stage.clear();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        uiViewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        delta = Math.min(1 / 30f, delta);
        engine.update(delta);

        uiViewport.apply();
        stage.getBatch().setColor(Color.WHITE);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        for (EntitySystem system : this.engine.getSystems()) {
            if (system instanceof Disposable disposable) {
                disposable.dispose();
            }
        }
        this.physicWorld.dispose();
        this.stage.dispose();
        if (gameView != null) {
            gameView.dispose();
            gameView = null;
        }
    }


    private void portalTrigger(Trigger trigger, Entity player) {
        if (trigger.getMapObject() == null) return;

        String targetMapStr = trigger.getMapObject().getProperties().get("targetMap", String.class);
        if (targetMapStr == null) return;

        Float targetX = trigger.getMapObject().getProperties().get("targetX", Float.class);
        Float targetY = trigger.getMapObject().getProperties().get("targetY", Float.class);

        // Lưu lại các thông số cốt lõi của player
        Life life = Life.MAPPER.get(player);
        float hp = life != null ? life.getLife() : 100f;

        Experience xp = Experience.MAPPER.get(player);
        float playerXp = xp != null ? xp.getXp() : 0f;
        int level = xp != null ? xp.getLevel() : 1;

        // Lưu lại trạng thái Inventory trước khi chuyển map
        Inventory inventory = Inventory.MAPPER.get(player);
        int potions = inventory != null ? inventory.getItemCount(Item.Type.POTION_HEALTH) : 0;
        int coins = inventory != null ? inventory.getItemCount(Item.Type.COIN) : 0;
        int keys = inventory != null ? inventory.getItemCount(Item.Type.KEY) : 0;

        // Lưu lại trạng thái CombatState trước khi chuyển map
        CombatState cs = CombatState.MAPPER.get(player);
        final java.util.List<io.github.com.group31.combat.Weapon> unlockedWeapons = new java.util.ArrayList<>();
        final int currentWeaponIndex;
        if (cs != null) {
            unlockedWeapons.addAll(cs.getUnlockedWeapons());
            currentWeaponIndex = cs.getCurrentIndex();
        } else {
            currentWeaponIndex = 0;
        }

        // Thực hiện chuyển map không đồng bộ bằng postRunnable để tránh lỗi đa luồng trong Ashley
        com.badlogic.gdx.Gdx.app.postRunnable(() -> {
            // 1. Giải phóng tất cả Entity hiện tại (Box2D bodies của chúng sẽ tự động bị hủy)
            engine.removeAllEntities();

            // 2. Reset trạng thái bàn phím để tránh Player bị trượt hướng sau khi chuyển map.
            //    Vì commandState[] trong KeyboardController vẫn ghi nhớ các phím đang giữ,
            //    nếu không reset thì khi player thả phím, ControllerSystem sẽ nhận lệnh
            //    "released" và dịch chuyển Player theo hướng ngược lại trong một frame.
            keyboardController.setActiveState(GameControllerState.class);

            // 3. Tải và thiết lập map mới
            MapAsset targetMapAsset = MapAsset.valueOf(targetMapStr.toUpperCase());
            TiledMap newMap = tiledService.loadMap(targetMapAsset);
            tiledService.setMap(newMap);

            // 4. Khôi phục trạng thái cho Player được tạo mới ở bản đồ tiếp theo
            ImmutableArray<Entity> players = engine.getEntitiesFor(Family.all(Player.class).get());
            if (players.size() > 0) {
                Entity newPlayer = players.first();

                // Check map transition for quest progress
                io.github.com.group31.quest.QuestManager.INSTANCE.checkMapEnter(targetMapAsset.name(), newPlayer);

                Life newLife = Life.MAPPER.get(newPlayer);
                if (newLife != null) {
                    newLife.setLife(hp);
                    viewModel.updateLifeInfo(newLife.getMaxLife(), newLife.getLife());
                }

                Experience newXp = Experience.MAPPER.get(newPlayer);
                if (newXp != null) {
                    newXp.setXp(playerXp);
                    newXp.setLevel(level);
                    newXp.setXpToNextLevel(level * 100f);
                    viewModel.updateXpInfo(playerXp, level * 100f, level, false);
                }

                // Khôi phục Inventory
                Inventory newInventory = Inventory.MAPPER.get(newPlayer);
                if (newInventory != null) {
                    newInventory.setItemCount(Item.Type.POTION_HEALTH, potions);
                    newInventory.setItemCount(Item.Type.COIN, coins);
                    newInventory.setItemCount(Item.Type.KEY, keys);
                    viewModel.updateInventory(potions, coins, keys);
                }

                // Khôi phục CombatState
                CombatState newCs = CombatState.MAPPER.get(newPlayer);
                if (newCs != null) {
                    newCs.getUnlockedWeapons().clear();
                    for (io.github.com.group31.combat.Weapon w : unlockedWeapons) {
                        newCs.unlockWeapon(w);
                    }
                    newCs.setCurrentIndex(currentWeaponIndex);

                    java.util.List<String> wNames = new java.util.ArrayList<>();
                    for (io.github.com.group31.combat.Weapon w : newCs.getUnlockedWeapons()) {
                        wNames.add(w.name());
                    }
                    viewModel.updateUnlockedWeapons(wNames);
                }

                // 5. Nếu có cấu hình vị trí spawn đích, dịch chuyển Player tới tọa độ đó.
                //    Tiled dùng hệ tọa độ Y từ trên xuống (y-down), nhưng Box2D dùng Y từ dưới lên (y-up).
                //    LibGDX tự động flip Y khi tạo entity từ bản đồ, nên targetY cũng cần được flip:
                //      worldY = (mapHeight_pixels - targetY) * UNIT_SCALE
                if (targetX != null && targetY != null) {
                    Physic physic = Physic.MAPPER.get(newPlayer);
                    if (physic != null) {
                        int mapHeightTiles = newMap.getProperties().get("height", 0, Integer.class);
                        int tileHeightPx  = newMap.getProperties().get("tileheight", 16, Integer.class);
                        float mapHeightPx = mapHeightTiles * tileHeightPx;

                        float worldX = targetX * GdxGame.UNIT_SCALE;
                        float worldY = (mapHeightPx - targetY) * GdxGame.UNIT_SCALE;
                        physic.getBody().setTransform(worldX, worldY, 0f);
                        physic.getPrevPosition().set(worldX, worldY);

                        // Cập nhật Transform để RenderSystem vẽ đúng vị trí ngay lập tức
                        Transform transform = Transform.MAPPER.get(newPlayer);
                        if (transform != null) {
                            transform.getPosition().set(worldX, worldY);
                        }

                        // Snap camera to the correct spawn position now that Transform is updated.
                        // CameraSystem.setMap() was called earlier but the player entity wasn't
                        // in its family yet (Ashley flushes pending entities on the next update).
                        // Calling it again here, after the Transform is set, snaps the camera
                        // immediately instead of leaving it stuck in the corner.
                        engine.getSystem(CameraSystem.class).setMap(newMap);
                    }
                }
            }
        });
    }
}
