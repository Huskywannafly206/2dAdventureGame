package io.github.com.group31.quest;

import com.badlogic.ashley.core.Entity;
import io.github.com.group31.component.CombatState;
import io.github.com.group31.component.Inventory;
import io.github.com.group31.component.Item;
import io.github.com.group31.component.Experience;
import io.github.com.group31.component.Npc;
import io.github.com.group31.component.Transform;
import io.github.com.group31.ui.model.GameViewModel;

public class QuestManager {
    public static final QuestManager INSTANCE = new QuestManager();

    private int currentStage = 0;
    private GameViewModel viewModel;
    private Entity player;
    private int slimesDefeated = 0;
    private io.github.com.group31.tiled.TiledAshleyConfigurator configurator;

    // ── Ice World quest tracking ──────────────────────────────────────────────
    private int frostOreCount = 0;           // mục tiêu 5
    private boolean hasSacredSpringWater = false;
    private boolean hasFrozenHeart = false;
    private boolean fishingRodSpawned = false; // chỉ spawn rod 1 lần

    private QuestManager() {}

    public void setViewModel(GameViewModel viewModel) {
        this.viewModel = viewModel;
        updateQuestHUD();
    }

    public void setPlayer(Entity player) {
        this.player = player;
    }

    public void setConfigurator(io.github.com.group31.tiled.TiledAshleyConfigurator configurator) {
        this.configurator = configurator;
    }

    public int getStage() {
        return currentStage;
    }

    public void setStage(int stage) {
        this.currentStage = stage;
        updateQuestHUD();
    }

    public int getFrostOreCount() {
        return frostOreCount;
    }

    public void setFrostOreCount(int frostOreCount) {
        this.frostOreCount = frostOreCount;
        updateQuestHUD();
    }

    public boolean isHasSacredSpringWater() {
        return hasSacredSpringWater;
    }

    public void setHasSacredSpringWater(boolean hasSacredSpringWater) {
        this.hasSacredSpringWater = hasSacredSpringWater;
        updateQuestHUD();
    }

    public boolean isHasFrozenHeart() {
        return hasFrozenHeart;
    }

    public void setHasFrozenHeart(boolean hasFrozenHeart) {
        this.hasFrozenHeart = hasFrozenHeart;
        updateQuestHUD();
    }

    public boolean isFishingRodSpawned() {
        return fishingRodSpawned;
    }

    public void setFishingRodSpawned(boolean fishingRodSpawned) {
        this.fishingRodSpawned = fishingRodSpawned;
    }

    public void updateQuestHUD() {
        if (viewModel == null) return;
        String title = getQuestTitle();
        String objective = getQuestObjective();
        viewModel.updateQuestInfo(title, objective);
    }

    public String getQuestTitle() {
        if (currentStage == 0)  return "Gap Truong Lang";
        if (currentStage == 1)  return "Thu thap Thao Duoc";
        if (currentStage == 2)  return "Gap Tho San";
        if (currentStage == 3)  return "Tieu diet Slime";
        if (currentStage == 4)  return "Bao cao voi Tho san";
        if (currentStage <= 6)  return "Khoi Dau Moi";
        if (currentStage <= 9)  return "Can Nha Bi An";
        if (currentStage == 10) return "Bao Cao Ket Qua";
        // Ice World stages
        if (currentStage == 11) return "The Legendary Blacksmith";
        if (currentStage == 12) return "Mine Frost-Iron Ore";
        if (currentStage == 13) return "Help the Fisherman";
        if (currentStage == 14) return "Find the Fishing Rod";
        if (currentStage == 15) return "Return the Fishing Rod";
        if (currentStage == 18) return "Find the Frozen Heart";
        if (currentStage == 16) return "All Materials Gathered";
        if (currentStage == 17) return "The Glacial Blade";
        return "Quest Complete";
    }

    public String getQuestObjective() {
        return switch (currentStage) {
            case 0  -> "Di gap Truong Lang de nhan chi dan.";
            case 1  -> {
                int count = 0;
                if (player != null) {
                    Inventory inv = Inventory.MAPPER.get(player);
                    if (inv != null) count = inv.getItemCount(Item.Type.SOOTHING_HERB);
                }
                yield "Thu thap 5 Thao Duoc Lam Diu (da co: " + count + "/5).";
            }
            case 2  -> "Ghe qua can leu dau lang gap Tho san.";
            case 3  -> "Tieu diet 4 Slime o lang (da diet: " + slimesDefeated + "/4).";
            case 4  -> "Quay lai bao cao voi Tho san.";
            case 5  -> "Nhat thanh kiem ri sat gan Tho san.";
            case 6  -> "Noi chuyen voi vo su Monk o phia dong.";
            case 7  -> "Di vao can nha go o phia tay.";
            case 8  -> "Noi chuyen voi Ong Lao trong nha.";
            case 9  -> "Nhat Binh Mau Than Ky tren giuong.";
            case 10 -> "Mang Than Duoc ve cho Monk o phia dong.";
            // Ice World
            case 11 -> "Find the Legendary Blacksmith in the frozen highlands (ice_map3).";
            case 12 -> "Mine 5 Frost-Iron Ore blocks at the Northern Quarry (near the dungeon gate) (collected: " + frostOreCount + "/5).";
            case 13 -> "Help the Fisherman to obtain the Sacred Spring Water.";
            case 14 -> "Find the Heirloom Fishing Rod hidden somewhere on the map!";
            case 15 -> "Return the Fishing Rod to the Fisherman.";
            case 18 -> "Find the Frozen Heart near where the Snow Sprite rests.";
            case 16 -> "Return to the Blacksmith — you have all the materials!";
            case 17 -> "The Glacial Blade is forged! Head to the Dark Dungeon.";
            default -> "No active quest.";
        };
    }

    // =========================================================================
    // Weapon & Item pickup checks (các stages cũ)
    // =========================================================================

    public void checkWeaponPickup(Entity player) {
        this.player = player;
        if (currentStage == 5) {
            CombatState cs = CombatState.MAPPER.get(player);
            if (cs != null && cs.getUnlockedWeapons().size() > 1) {
                setStage(6);
                showFloating("[YELLOW]Quest: Meet Monk![]");
            }
        }
    }

    public void checkMapEnter(String mapName, Entity player) {
        this.player = player;
        if (currentStage == 7 && "VILLAGE_HOUSE".equalsIgnoreCase(mapName)) {
            setStage(8);
            showFloating("[YELLOW]Quest: Meet the Old Man![]");
        }

        // Ice World map transitions: respawn dynamic quest items if needed
        if ("ICEMAP2".equalsIgnoreCase(mapName)) {
            if (currentStage == 14 && configurator != null) {
                // Find fisher_man to spawn the rod relative to him
                Entity fisherman = configurator.findNpcByName("fisher_man");
                if (fisherman != null) {
                    Transform npcT = Transform.MAPPER.get(fisherman);
                    if (npcT != null) {
                        configurator.spawnFishingRod(
                            npcT.getPosition().x + 4.5f,
                            npcT.getPosition().y - 3f
                        );
                    }
                }
            }
        } else if ("ICEMAP1".equalsIgnoreCase(mapName)) {
            if (currentStage == 18 && configurator != null) {
                // Spawn Frozen Heart near the Snow Sprite (object ID 33) in ice_map.tmx
                Entity snowSprite = configurator.findEntityByObjectId(33);
                if (snowSprite != null) {
                    Transform spriteT = Transform.MAPPER.get(snowSprite);
                    if (spriteT != null) {
                        float sx = spriteT.getPosition().x;
                        float sy = spriteT.getPosition().y;
                        if (!hasFrozenHeart) {
                            configurator.spawnFrozenHeart(sx + 1f, sy);
                        }
                    }
                } else {
                    // Fallback to hardcoded coordinates if Snow Sprite entity not found
                    if (!hasFrozenHeart) {
                        configurator.spawnFrozenHeart(12f + 1f, 18f); // roughly near the sprite center
                    }
                }
            }
        }
    }

    public void onSnowSpriteDefeated(float x, float y) {
        if (currentStage == 18 && !hasFrozenHeart && configurator != null) {
            configurator.spawnFrozenHeart(x, y);
            showFloating("[YELLOW]The Snow Sprite dropped the Frozen Heart![]");
        }
    }

    public void checkPotionPickup(Entity player) {
        this.player = player;
        if (currentStage == 9) {
            Inventory inv = Inventory.MAPPER.get(player);
            if (inv != null && inv.getItemCount(Item.Type.POTION_HEALTH) > 0) {
                setStage(10);
                showFloating("[YELLOW]Quest: Return to Monk![]");
            }
        }
    }

    public void checkHerbPickup(Entity player) {
        this.player = player;
        if (currentStage == 1) {
            updateQuestHUD();
            Inventory inv = Inventory.MAPPER.get(player);
            if (inv != null && inv.getItemCount(Item.Type.SOOTHING_HERB) >= 5) {
                showFloating("[YELLOW]5 Herbs collected! Return to Village Chief![]");
            }
        }
    }

    public void onSlimeDefeated() {
        if (currentStage == 3) {
            slimesDefeated++;
            updateQuestHUD();
            showFloating("[RED]Slime: " + slimesDefeated + "/4[]");
            if (slimesDefeated >= 4) {
                setStage(4);
                showFloating("[YELLOW]4 Slimes defeated! Return to Hunter![]");
            }
        }
    }

    /**
     * Gọi khi player nhặt FROST_IRON_ORE.
     * Tăng bộ đếm và kiểm tra đủ 5 chưa.
     */
    public void checkFrostOrePickup(Entity player) {
        this.player = player;
        if (currentStage == 12) {
            frostOreCount++;
            updateQuestHUD();
            showFloating("[CYAN]Frost-Iron Ore: " + frostOreCount + "/5[]");
            if (frostOreCount >= 5) {
                setStage(13);
                showFloating("[YELLOW]Ores collected! Go meet the Fisherman![]");
            }
        }
    }

    /**
     * Gọi khi player nhặt HEIRLOOM_FISHING_ROD trên map.
     * Chuyển stage sang 15 (sẵn sàng trả rod).
     */
    public void checkFishingRodPickup(Entity player) {
        this.player = player;
        if (currentStage == 14) {
            setStage(15);
            showFloating("[YELLOW]Found the Fishing Rod! Return it to the Fisherman![]");
        }
    }

    /**
     * Gọi khi player nhặt SACRED_SPRING_WATER (do Fisherman trao).
     */
    public void checkSacredWaterPickup(Entity player) {
        this.player = player;
        if (currentStage == 15) {
            hasSacredSpringWater = true;
            setStage(18);
            showFloating("[AQUA]Obtained Sacred Spring Water! Find the Frozen Heart![]");
        }
    }

    /**
     * Gọi khi player nhặt FROZEN_HEART (từ Snow Sprite hoặc spawn programmatically).
     */
    public void checkFrozenHeartPickup(Entity player) {
        this.player = player;
        if (currentStage == 18) {
            hasFrozenHeart = true;
            setStage(16);
            showFloating("[CYAN]Obtained the Frozen Heart! Return to the Blacksmith![]");
        }
    }

    // =========================================================================
    // NPC Dialogue Dispatch
    // =========================================================================

    public void onTalkToNpc(Entity npcEntity, Entity player) {
        this.player = player;
        Npc npc = Npc.MAPPER.get(npcEntity);
        if (npc == null) return;

        String name = npc.getName();
        if (name == null) return;

        switch (name.toLowerCase()) {
            case "truong_lang" -> handleVillageChief(npc, player);
            case "tho_san"     -> handleHunter(npc, npcEntity, player);
            case "monk"        -> handleMonk(npc, player);
            case "old man"     -> handleOldMan(npc, player);
            case "black_smith" -> handleBlacksmith(npc, npcEntity, player);
            case "fisher_man"  -> handleFisherman(npc, npcEntity, player);
        }
    }

    // =========================================================================
    // Old NPC handlers (Village Chief, Hunter, Monk, Old Man)
    // =========================================================================

    private void handleVillageChief(Npc npc, Entity player) {
        if (currentStage == 0) {
            npc.setDialogue(new String[]{
                "[IDLE]Ah, you're finally here...",
                "[IDLE]Look at them, this strange disease is consuming our village day by day.",
                "[IDLE]The only antidote lies deep within the Dark Dungeon.",
                "[IDLE]But that place has been sealed away for centuries.",
                "[IDLE]Before you leave, there is a small favor I must ask.",
                "[IDLE]Please help me gather 5 Soothing Herbs from the south...",
                "[IDLE]...so I can brew some medicine to help the sick hold on."
            });
            setStage(1);
            showFloating("[YELLOW]Quest: Gather 5 Soothing Herbs![]");
        } else if (currentStage == 1) {
            Inventory inv = Inventory.MAPPER.get(player);
            int herbCount = inv != null ? inv.getItemCount(Item.Type.SOOTHING_HERB) : 0;
            if (herbCount >= 5) {
                npc.setDialogue(new String[]{
                    "[IDLE]Excellent, this will buy everyone a few more days.",
                    "[IDLE]Now, to enter the Ancient Forest without getting lost, you will need a map.",
                    "[IDLE]Head to the cabin at the edge of the village and meet the Hunter.",
                    "[IDLE]I've already asked him to prepare a Parchment Map for you."
                });
                if (inv != null) inv.removeItem(Item.Type.SOOTHING_HERB, 5);
                setStage(2);
                if (viewModel != null && inv != null) {
                    viewModel.updateInventory(
                        inv.getItemCount(Item.Type.POTION_HEALTH),
                        inv.getItemCount(Item.Type.COIN),
                        inv.getItemCount(Item.Type.KEY),
                        inv.getItemCount(Item.Type.GOLD_KEY),
                        inv.getItemCount(Item.Type.SILVER_KEY),
                        inv.getItemCount(Item.Type.SOOTHING_HERB),
                        inv.getItemCount(Item.Type.JUNGLE_MAP_KEY)
                    );
                }
                showFloating("[YELLOW]Quest: Meet the Hunter![]");
            } else {
                npc.setDialogue(new String[]{
                    "[IDLE]Before you leave, there is a small favor I must ask.",
                    "[IDLE]Please help me gather 5 Soothing Herbs from the south...",
                    "[IDLE]...so I can brew some medicine to help the sick hold on."
                });
            }
        } else {
            npc.setDialogue(new String[]{
                "[IDLE]Chao cau be! Hay di hoan thanh thu thach cua Monk nhe.",
                "[IDLE]Ta tin tuong vao nang luc cua cau."
            });
        }
    }

    private void handleHunter(Npc npc, Entity npcEntity, Entity player) {
        if (currentStage < 2) {
            npc.setDialogue(new String[]{
                "[IDLE]Chào cháu! Ta là Thợ Săn. Có việc gì sao?"
            });
        } else if (currentStage == 2) {
            npc.setDialogue(new String[]{
                "[IDLE]The Chief told me about your plan.",
                "[IDLE]You intend to venture into the Ancient Forest all by yourself?",
                "[IDLE]That place has become treacherous lately.",
                "[IDLE]One careless mistake, and it'll cost you your life.",
                "[IDLE]Here is your map.",
                "[IDLE]However, you must first destroy some slimes in the forest to prove your strength."
            });
            setStage(3);
            showFloating("[YELLOW]Quest: Defeat 4 Slimes![]");
        } else if (currentStage == 3) {
            npc.setDialogue(new String[]{
                "[IDLE]Here is your map.",
                "[IDLE]However, you must first destroy some slimes in the forest to prove your strength.",
                "[IDLE]Defeated: " + slimesDefeated + "/4."
            });
        } else if (currentStage == 4) {
            npc.setDialogue(new String[]{
                "[IDLE]Not bad.",
                "[IDLE]But look at you, going into battle empty-handed like that?",
                "[IDLE]You'll become wolf bait before you even reach the forest edge.",
                "[IDLE]This is a rusty sword I used in my youth.",
                "[IDLE]It's old, but it still gets the job done.",
                "[IDLE]Take it, along with these wound ointments.",
                "[IDLE]Keep your wits about you, young man."
            });
            if (configurator != null) {
                Transform npcT = Transform.MAPPER.get(npcEntity);
                if (npcT != null) {
                    configurator.spawnRustySword(npcT.getPosition().x - 1f, npcT.getPosition().y);
                    configurator.spawnJungleMapKey(npcT.getPosition().x + 1f, npcT.getPosition().y);
                }
            }
            Inventory inv = Inventory.MAPPER.get(player);
            if (inv != null) {
                inv.addItem(Item.Type.POTION_HEALTH, 2);
                if (viewModel != null) {
                    viewModel.updateInventory(
                        inv.getItemCount(Item.Type.POTION_HEALTH),
                        inv.getItemCount(Item.Type.COIN),
                        inv.getItemCount(Item.Type.KEY),
                        inv.getItemCount(Item.Type.GOLD_KEY),
                        inv.getItemCount(Item.Type.SILVER_KEY),
                        inv.getItemCount(Item.Type.SOOTHING_HERB),
                        inv.getItemCount(Item.Type.JUNGLE_MAP_KEY)
                    );
                }
            }
            setStage(5);
        } else {
            npc.setDialogue(new String[]{
                "[IDLE]Keep your wits about you, young man."
            });
        }
    }

    private void handleMonk(Npc npc, Entity player) {
        if (currentStage <= 5) {
            npc.setDialogue(new String[]{
                "[IDLE]Chao cau vo si tre!",
                "[IDLE]Hay noi chuyen voi Truong Lang truoc, sau do nhat mot mon vu khi tren mat dat."
            });
        } else if (currentStage == 6) {
            npc.setDialogue(new String[]{
                "[IDLE]Tot lam, cau da co vu khi trong tay!",
                "[IDLE]Ta muon thu thach cau.",
                "[IDLE]Hay den dieu tra can nha go co kinh o phia tay cua lang.",
                "[IDLE]Nghe don noi do co mot Ong Lao dang giu mot Than Duoc bi an.",
                "[IDLE]Cau co san long di lay no ve day giup ta khong?"
            });
            setStage(7);
        } else if (currentStage >= 7 && currentStage <= 9) {
            npc.setDialogue(new String[]{
                "[IDLE]Cau van chua lay duoc Than Duoc sao?",
                "[IDLE]Hay tim can nha go phia tay lang va noi chuyen voi Ong Lao."
            });
        } else if (currentStage == 10) {
            npc.setDialogue(new String[]{
                "[IDLE]Oi troi! Cau thuc su da mang Than Duoc tro ve!",
                "[IDLE]Cau da hoan thanh thu thach xuat sac.",
                "[IDLE]Day la phan thuong xung dang danh cho su dung cam cua cau!"
            });
            Inventory inv = Inventory.MAPPER.get(player);
            if (inv != null) {
                inv.addItem(Item.Type.COIN, 20);
                inv.removeItem(Item.Type.POTION_HEALTH, 1);
            }
            Experience xp = Experience.MAPPER.get(player);
            if (xp != null) xp.addXp(150f);
            if (viewModel != null) {
                viewModel.updateInventory(
                    inv != null ? inv.getItemCount(Item.Type.POTION_HEALTH) : 0,
                    inv != null ? inv.getItemCount(Item.Type.COIN) : 0,
                    inv != null ? inv.getItemCount(Item.Type.KEY) : 0,
                    inv != null ? inv.getItemCount(Item.Type.GOLD_KEY) : 0,
                    inv != null ? inv.getItemCount(Item.Type.SILVER_KEY) : 0,
                    inv != null ? inv.getItemCount(Item.Type.SOOTHING_HERB) : 0,
                    inv != null ? inv.getItemCount(Item.Type.JUNGLE_MAP_KEY) : 0
                );
                showFloating("[GOLD]+150 XP, +20 Coins![]");
            }
            setStage(11); // Bắt đầu Ice World quest
            showFloating("[YELLOW]A new journey begins... Head to the frozen lands![]");
        } else {
            npc.setDialogue(new String[]{
                "[IDLE]Cam on cau vi Than Duoc!",
                "[IDLE]Hay luyen tap cham chi nhe!"
            });
        }
    }

    private void handleOldMan(Npc npc, Entity player) {
        if (currentStage <= 7) {
            npc.setDialogue(new String[]{
                "[IDLE]Khu khu... Cau be tre tuoi, cau tim ai o day?"
            });
        } else if (currentStage == 8) {
            npc.setDialogue(new String[]{
                "[IDLE]Khu khu... Ta la chu nhan ngoi nha nay.",
                "[IDLE]Cau tim kiem Than Duoc cho vo su Monk sao?",
                "[IDLE]Ta co thi dua no cho cau.",
                "[IDLE]Nhung ta gia yeu qua.",
                "[IDLE]Hay nhat binh mau than ky ta de tren chiec giuong kia giup ta."
            });
            setStage(9);
        } else if (currentStage == 9) {
            npc.setDialogue(new String[]{
                "[IDLE]Binh thuoc o ngay tren chiec giuong do cau be."
            });
        } else {
            npc.setDialogue(new String[]{
                "[IDLE]Cam on cau da lay binh thuoc giup ta khu khu..."
            });
        }
    }

    // =========================================================================
    // Ice World NPC handlers
    // =========================================================================

    /**
     * Xử lý hội thoại với Blacksmith (black_smith) trong ice_map3.
     *
     * Stage 11 → lần đầu gặp, giải thích quest
     * Stage 12 → đang thu thập nguyên liệu
     * Stage 16 → đủ nguyên liệu, rèn vũ khí
     * Stage 17+ → đã có Glacial Blade
     */
    private void handleBlacksmith(Npc npc, Entity npcEntity, Entity player) {
        if (currentStage < 11) {
            // Chưa tới Ice World quest
            npc.setDialogue(new String[]{
                "[IDLE]Who are you? This is no place for the weak. Come back when you're stronger."
            });
            return;
        }

        if (currentStage == 11) {
            // Lần đầu gặp Blacksmith
            npc.setDialogue(new String[]{
                "[IDLE]Player: Excuse me... are you the Legendary Blacksmith?",
                "[IDLE]Blacksmith: Legendary? Hah, just an old man who likes pounding iron.",
                "[IDLE]Blacksmith: A human brat managed to withstand the cold to get here...",
                "[IDLE]Blacksmith: What do you want?",
                "[IDLE]Player: I need a weapon strong enough to break the seal...",
                "[IDLE]Player: ...and defeat the monsters in the Dark Dungeon to save my village!",
                "[IDLE]Blacksmith: With that piece of rusty junk?",
                "[IDLE]Blacksmith: Fine, I can forge a masterpiece for you.",
                "[IDLE]Blacksmith: But my forge has run completely dry of rare materials.",
                "[IDLE]Blacksmith: If you want a powerful weapon, you'll have to find the materials yourself!",
                "[IDLE]Blacksmith: First, mine 5 Frost-Iron Ore blocks at the Northern Quarry.",
                "[IDLE]Blacksmith: The quarry is near the dungeon gate. Go on, find those ores!"
            });
            frostOreCount = 0;
            hasSacredSpringWater = false;
            hasFrozenHeart = false;
            fishingRodSpawned = false;
            setStage(12);
            showFloating("[YELLOW]Quest: Mine Frost-Iron Ore![]");
            return;
        }

        if (currentStage == 12) {
            // Đang đào quặng
            npc.setDialogue(new String[]{
                "[IDLE]Blacksmith: Still gathering?",
                "[IDLE]Blacksmith: I need 5 Frost-Iron Ore blocks from the Northern Quarry.",
                "[IDLE]Blacksmith: The quarry is near the dungeon gate.",
                "[IDLE]Blacksmith: You have collected: " + frostOreCount + "/5."
            });
            return;
        }

        if (currentStage == 13 || currentStage == 14 || currentStage == 15) {
            // Đã đào đủ quặng, cần đi gặp Fisherman
            npc.setDialogue(new String[]{
                "[IDLE]Blacksmith: Have you met the Fisherman yet?",
                "[IDLE]Blacksmith: He should be near the frozen lake.",
                "[IDLE]Blacksmith: Go help him to obtain the Sacred Spring Water."
            });
            return;
        }

        if (currentStage == 18) {
            // Đã có nước suối thiêng, cần đi tìm Frozen Heart
            npc.setDialogue(new String[]{
                "[IDLE]Blacksmith: Almost there. You have the ore and the water.",
                "[IDLE]Blacksmith: Now you need to find the Frozen Heart...",
                "[IDLE]Blacksmith: ...near where the Snow Sprite rests."
            });
            return;
        }

        if (currentStage == 16) {
            // Đủ nguyên liệu — rèn Glacial Blade
            npc.setDialogue(new String[]{
                "[IDLE]Blacksmith: You actually found everything? I'm genuinely impressed.",
                "[IDLE]Blacksmith: Stand back... this will take a moment.",
                "[IDLE]Blacksmith: The forge hasn't burned this hot in decades.",
                "[IDLE]Blacksmith: Here it is — the Glacial Blade.",
                "[IDLE]Blacksmith: Forged from Frost-Iron and quenched in Sacred Spring Water.",
                "[IDLE]Blacksmith: The Frozen Heart is sealed within its core.",
                "[IDLE]Blacksmith: This weapon carries the fury of the blizzard...",
                "[IDLE]Blacksmith: ...and the purity of sacred waters. No seal can withstand its edge.",
                "[IDLE]Blacksmith: With this blade, the Dark Dungeon's seal will crumble.",
                "[IDLE]Blacksmith: Go, young warrior — the fate of your village rests with you!"
            });

            // Trao vũ khí (mở khóa SWORD mạnh hơn, tượng trưng là WEAPON_SWORD upgraded)
            Inventory inv = Inventory.MAPPER.get(player);
            Experience xp = Experience.MAPPER.get(player);
            if (inv != null) {
                inv.addItem(Item.Type.COIN, 10);
            }
            if (xp != null) {
                xp.addXp(200f); // Thưởng 200 XP cho việc hoàn thành thu thập nguyên liệu
            }
            if (viewModel != null && inv != null) {
                viewModel.updateInventory(
                    inv.getItemCount(Item.Type.POTION_HEALTH),
                    inv.getItemCount(Item.Type.COIN),
                    inv.getItemCount(Item.Type.KEY),
                    inv.getItemCount(Item.Type.GOLD_KEY),
                    inv.getItemCount(Item.Type.SILVER_KEY),
                    inv.getItemCount(Item.Type.SOOTHING_HERB),
                    inv.getItemCount(Item.Type.JUNGLE_MAP_KEY)
                );
                showFloating("[GOLD]+200 XP! The Glacial Blade is yours![]");
            }

            // Spawn Glacial Blade (dùng WEAPON_SWORD làm đại diện)
            if (configurator != null) {
                Transform npcT = Transform.MAPPER.get(npcEntity);
                if (npcT != null) {
                    configurator.spawnRustySword(npcT.getPosition().x - 1f, npcT.getPosition().y);
                }
            }

            setStage(17);
            return;
        }

        if (currentStage == 17) {
            npc.setDialogue(new String[]{
                "[IDLE]Blacksmith: You carry the Glacial Blade now.",
                "[IDLE]Blacksmith: The Dark Dungeon awaits.",
                "[IDLE]Blacksmith: Trust in your strength, young warrior.",
                "[IDLE]Blacksmith: And trust in that blade — it will not fail you."
            });
            return;
        }

        // Stage > 17 hoặc < 11
        npc.setDialogue(new String[]{
            "[IDLE]Blacksmith: Go. Your village needs you. Don't keep them waiting."
        });
    }

    /**
     * Xử lý hội thoại với Fisherman (fisher_man) trong ice_map2.
     *
     * Stage 12   → lần đầu gặp, cần tìm cần câu
     * Stage 13   → vừa nhận quest tìm cần câu (rod chưa spawn)
     * Stage 14   → đang tìm cần câu
     * Stage 15   → đã có rod, trả lại và nhận Sacred Spring Water
     */
    private void handleFisherman(Npc npc, Entity npcEntity, Entity player) {
        if (currentStage < 13) {
            npc.setDialogue(new String[]{
                "[IDLE]Fisherman: Hmm? You don't look like you're from around here, kid.",
                "[IDLE]Fisherman: Be careful out here — blizzards come without warning in these parts."
            });
            return;
        }

        if (currentStage == 13) {
            // Lần đầu gặp Fisherman khi đã có đủ quặng
            npc.setDialogue(new String[]{
                "[IDLE]Player: Hello, sir.",
                "[IDLE]Player: I heard you possess the Sacred Spring Water.",
                "[IDLE]Player: Is there anything I can trade to get it?",
                "[IDLE]Fisherman: Sacred spring water? I have it.",
                "[IDLE]Fisherman: But I'm in no mood to worry about that now.",
                "[IDLE]Fisherman: A massive blizzard just swept by...",
                "[IDLE]Fisherman: ...and blew away my Heirloom Fishing Rod.",
                "[IDLE]Fisherman: Without it, I can't catch fish to feed my family through this winter...",
                "[IDLE]Player: Don't worry, sir.",
                "[IDLE]Player: I will definitely find your fishing rod for you!",
                "[IDLE]Fisherman: Please hurry... My children are going hungry.",
                "[IDLE]Fisherman: The rod should be somewhere nearby...",
                "[IDLE]Fisherman: ...maybe caught on something in the wind..."
            });

            // Spawn cần câu ẩn trên map nếu chưa spawn
            if (!fishingRodSpawned && configurator != null) {
                Transform npcT = Transform.MAPPER.get(npcEntity);
                if (npcT != null) {
                    // Spawn cần câu ở vị trí cách xa NPC (người chơi phải đi tìm)
                    configurator.spawnFishingRod(
                        npcT.getPosition().x + 4.5f,
                        npcT.getPosition().y - 3f
                    );
                    fishingRodSpawned = true;
                }
            }
            setStage(14);
            showFloating("[YELLOW]Quest: Find the Heirloom Fishing Rod![]");
            return;
        }

        if (currentStage == 14) {
            // Đang tìm cần câu
            npc.setDialogue(new String[]{
                "[IDLE]Fisherman: Have you found my rod yet? Please hurry...",
                "[IDLE]Fisherman: My children haven't eaten since yesterday.",
                "[IDLE]Fisherman: That rod is the only thing keeping us alive through this winter."
            });
            return;
        }

        if (currentStage == 15) {
            // Đã tìm được cần câu, trả lại
            npc.setDialogue(new String[]{
                "[IDLE]Player: Here is your fishing rod, sir!",
                "[IDLE]Fisherman: Oh, it really is! My precious rod...",
                "[IDLE]Fisherman: Thank you so much, kind young man.",
                "[IDLE]Fisherman: I am a man of my word.",
                "[IDLE]Fisherman: Here — take the Sacred Spring Water as promised.",
                "[IDLE]Fisherman: Wish you the best of luck on your journey.",
                "[IDLE]Fisherman: May the winds be kind to you!"
            });

            // Trao Sacred Spring Water cho player (spawn item gần player)
            Transform playerT = Transform.MAPPER.get(player);
            if (playerT != null && configurator != null) {
                configurator.spawnSacredSpringWater(
                    playerT.getPosition().x + 0.5f,
                    playerT.getPosition().y
                );
            }

            // Xóa Fishing Rod khỏi inventory
            Inventory inv = Inventory.MAPPER.get(player);
            if (inv != null) {
                inv.removeItem(Item.Type.HEIRLOOM_FISHING_ROD, 1);
                if (viewModel != null) {
                    viewModel.updateInventory(
                        inv.getItemCount(Item.Type.POTION_HEALTH),
                        inv.getItemCount(Item.Type.COIN),
                        inv.getItemCount(Item.Type.KEY),
                        inv.getItemCount(Item.Type.GOLD_KEY),
                        inv.getItemCount(Item.Type.SILVER_KEY),
                        inv.getItemCount(Item.Type.SOOTHING_HERB),
                        inv.getItemCount(Item.Type.JUNGLE_MAP_KEY)
                    );
                }
            }

            showFloating("[YELLOW]Quest: Get Sacred Spring Water![]");
            return;
        }

        // Stage 16+: đã hoàn thành nhiệm vụ với Fisherman
        npc.setDialogue(new String[]{
            "[IDLE]Fisherman: You've done so much for this family... I can't thank you enough.",
            "[IDLE]Fisherman: Godspeed, young hero. May your blade never dull."
        });
    }

    // =========================================================================
    // Utility
    // =========================================================================

    /** Hiển thị floating text tại vị trí player. */
    private void showFloating(String text) {
        if (viewModel == null || player == null) return;
        Transform transform = Transform.MAPPER.get(player);
        if (transform != null) {
            viewModel.showFloatingText(text,
                transform.getPosition().x,
                transform.getPosition().y + 1f);
        }
    }
}
