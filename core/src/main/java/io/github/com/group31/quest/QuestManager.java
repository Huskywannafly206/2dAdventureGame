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

    public void updateQuestHUD() {
        if (viewModel == null) return;
        String title = getQuestTitle();
        String objective = getQuestObjective();
        viewModel.updateQuestInfo(title, objective);
    }

    public String getQuestTitle() {
        if (currentStage == 0) return "Gap Truong Lang";
        if (currentStage == 1) return "Thu thap Thao Duoc";
        if (currentStage == 2) return "Gap Tho San";
        if (currentStage == 3) return "Tieu diet Slime";
        if (currentStage == 4) return "Bao cao voi Tho san";
        if (currentStage <= 6) return "Khoi Dau Moi";
        if (currentStage <= 9) return "Can Nha Bi An";
        if (currentStage == 10) return "Bao Cao Ket Qua";
        return "Nhiem Vu Da Xong";
    }

    public String getQuestObjective() {
        return switch (currentStage) {
            case 0 -> "Di gap Truong Lang de nhan chi dan.";
            case 1 -> {
                int count = 0;
                if (player != null) {
                    Inventory inv = Inventory.MAPPER.get(player);
                    if (inv != null) {
                        count = inv.getItemCount(Item.Type.SOOTHING_HERB);
                    }
                }
                yield "Thu thap 5 Thao Duoc Lam Diu (da co: " + count + "/5).";
            }
            case 2 -> "Ghe qua can leu dau lang gap Tho san.";
            case 3 -> "Tieu diet 4 Slime o lang (da diet: " + slimesDefeated + "/4).";
            case 4 -> "Quay lai bao cao voi Tho san.";
            case 5 -> "Nhat thanh kiem ri sat gan Tho san.";
            case 6 -> "Noi chuyen voi vo su Monk o phia dong.";
            case 7 -> "Di vao can nha go o phia tay.";
            case 8 -> "Noi chuyen voi Ong Lao trong nha.";
            case 9 -> "Nhat Binh Mau Than Ky tren giuong.";
            case 10 -> "Mang Than Duoc ve cho Monk o phia dong.";
            default -> "Khong co nhiem vu nao.";
        };
    }

    public void checkWeaponPickup(Entity player) {
        this.player = player;
        if (currentStage == 5) {
            CombatState cs = CombatState.MAPPER.get(player);
            if (cs != null && cs.getUnlockedWeapons().size() > 1) {
                setStage(6);
                if (viewModel != null) {
                    Transform transform = Transform.MAPPER.get(player);
                    if (transform != null) {
                        viewModel.showFloatingText("[YELLOW]Nhiem vu: Gap Monk![]", 
                            transform.getPosition().x, transform.getPosition().y + 1f);
                    }
                }
            }
        }
    }

    public void checkMapEnter(String mapName, Entity player) {
        this.player = player;
        if (currentStage == 7 && "VILLAGE_HOUSE".equalsIgnoreCase(mapName)) {
            setStage(8);
            if (viewModel != null) {
                Transform transform = Transform.MAPPER.get(player);
                if (transform != null) {
                    viewModel.showFloatingText("[YELLOW]Nhiem vu: Gap Ong Lao![]", 
                        transform.getPosition().x, transform.getPosition().y + 1f);
                }
            }
        }
    }

    public void checkPotionPickup(Entity player) {
        this.player = player;
        if (currentStage == 9) {
            Inventory inv = Inventory.MAPPER.get(player);
            if (inv != null && inv.getItemCount(Item.Type.POTION_HEALTH) > 0) {
                setStage(10);
                if (viewModel != null) {
                    Transform transform = Transform.MAPPER.get(player);
                    if (transform != null) {
                        viewModel.showFloatingText("[YELLOW]Nhiem vu: Ve gap Monk![]", 
                            transform.getPosition().x, transform.getPosition().y + 1f);
                    }
                }
            }
        }
    }

    public void checkHerbPickup(Entity player) {
        this.player = player;
        if (currentStage == 1) {
            updateQuestHUD();
            Inventory inv = Inventory.MAPPER.get(player);
            if (inv != null && inv.getItemCount(Item.Type.SOOTHING_HERB) >= 5) {
                if (viewModel != null) {
                    Transform transform = Transform.MAPPER.get(player);
                    if (transform != null) {
                        viewModel.showFloatingText("[YELLOW]Da du 5 Thao Duoc, hay ve gap Truong Lang![]", 
                            transform.getPosition().x, transform.getPosition().y + 1f);
                    }
                }
            }
        }
    }

    public void onSlimeDefeated() {
        if (currentStage == 3) {
            slimesDefeated++;
            updateQuestHUD();
            if (viewModel != null) {
                Transform transform = Transform.MAPPER.get(player);
                if (transform != null) {
                    viewModel.showFloatingText("[RED]Slime: " + slimesDefeated + "/4[]", 
                        transform.getPosition().x, transform.getPosition().y + 1f);
                }
            }
            if (slimesDefeated >= 4) {
                setStage(4);
                if (viewModel != null) {
                    Transform transform = Transform.MAPPER.get(player);
                    if (transform != null) {
                        viewModel.showFloatingText("[YELLOW]Da tieu diet 4 Slime, hay ve gap Tho san![]", 
                            transform.getPosition().x, transform.getPosition().y + 1f);
                    }
                }
            }
        }
    }

    public void onTalkToNpc(Entity npcEntity, Entity player) {
        this.player = player;
        Npc npc = Npc.MAPPER.get(npcEntity);
        if (npc == null) return;

        if ("truong_lang".equalsIgnoreCase(npc.getName())) {
            if (currentStage == 0) {
                npc.setDialogue(new String[]{
                    "[IDLE]Ah, you're finally here... Look at them, this strange disease is consuming our village day by day. The only antidote lies deep within the Dark Dungeon, but that place has been sealed away for centuries.",
                    "[IDLE]Ah, you're finally here... Look at them, this strange disease is consuming our village day by day. The only antidote lies deep within the Dark Dungeon, but that place has been sealed away for centuries.",
                    "[IDLE]Before you leave, there is a small favor I must ask. Please help me gather 5 Soothing Herbs from the south so I can brew some medicine to help the sick hold on."
                });
                setStage(1);
                if (viewModel != null) {
                    Transform transform = Transform.MAPPER.get(player);
                    if (transform != null) {
                        viewModel.showFloatingText("[YELLOW]Nhiem vu: Thu thap 5 Thao Duoc Lam Diu![]", 
                            transform.getPosition().x, transform.getPosition().y + 1f);
                    }
                }
            } else if (currentStage == 1) {
                Inventory inv = Inventory.MAPPER.get(player);
                int herbCount = inv != null ? inv.getItemCount(Item.Type.SOOTHING_HERB) : 0;
                if (herbCount >= 5) {
                    npc.setDialogue(new String[]{
                        "[IDLE]Excellent, this will buy everyone a few more days. Now, to enter the Ancient Forest without getting lost, you will need a map.",
                        "[IDLE]Head to the cabin at the edge of the village and meet the Hunter. I've already asked him to prepare a Parchment Map for you."
                    });
                    if (inv != null) {
                        inv.removeItem(Item.Type.SOOTHING_HERB, 5);
                    }
                    setStage(2);
                    if (viewModel != null) {
                        if (inv != null) {
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
                        Transform transform = Transform.MAPPER.get(player);
                        if (transform != null) {
                            viewModel.showFloatingText("[YELLOW]Nhiem vu: Gap Tho San![]", 
                                transform.getPosition().x, transform.getPosition().y + 1f);
                        }
                    }
                } else {
                    npc.setDialogue(new String[]{
                        "[IDLE]Before you leave, there is a small favor I must ask. Please help me gather 5 Soothing Herbs from the south so I can brew some medicine to help the sick hold on."
                    });
                }
            } else {
                npc.setDialogue(new String[]{
                    "[IDLE]Chao cau be! Hay di hoan thanh thu thach cua Monk nhe.",
                    "[IDLE]Ta tin tuong vao nang luc cua cau."
                });
            }
        } else if ("tho_san".equalsIgnoreCase(npc.getName())) {
            if (currentStage < 2) {
                npc.setDialogue(new String[]{
                    "[IDLE]Chào cháu! Ta là Thợ Săn. Có việc gì sao?"
                });
            } else if (currentStage == 2) {
                npc.setDialogue(new String[]{
                    "[IDLE]The Chief told me about your plan. You intend to venture into the Ancient Forest all by yourself?",
                    "[IDLE]That place has become treacherous lately. One careless mistake, and it'll cost you your life.",
                    "[IDLE]Here is your map. However, you must first destroy some slimes in the forest to prove your strength"
                });
                setStage(3);
                if (viewModel != null) {
                    Transform transform = Transform.MAPPER.get(player);
                    if (transform != null) {
                        viewModel.showFloatingText("[YELLOW]Nhiem vu: Tieu diet 4 Slime![]", 
                            transform.getPosition().x, transform.getPosition().y + 1f);
                    }
                }
            } else if (currentStage == 3) {
                npc.setDialogue(new String[]{
                    "[IDLE]Here is your map. However, you must first destroy some slimes in the forest to prove your strength (Defeated: " + slimesDefeated + "/4)."
                });
            } else if (currentStage == 4) {
                npc.setDialogue(new String[]{
                    "[IDLE]Not bad. But look at you, going into battle empty-handed like that? You'll become wolf bait before you even reach the forest edge",
                    "[IDLE]This is a rusty sword I used in my youth. It’s old, but it still gets the job done. Take it, along with these wound ointments.",
                    "[IDLE]Keep your wits about you, young man."
                });
                
                // Spawn rusty sword next to player
                if (configurator != null) {
                    Transform npcT = Transform.MAPPER.get(npcEntity);
                    if (npcT != null) {
                        configurator.spawnRustySword(npcT.getPosition().x - 1f, npcT.getPosition().y);
                        configurator.spawnJungleMapKey(npcT.getPosition().x + 1f, npcT.getPosition().y);
                    }
                }
                
                // Give wound ointments (represented by 2 health potions)
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
        } else if ("Monk".equalsIgnoreCase(npc.getName())) {
            if (currentStage <= 5) {
                npc.setDialogue(new String[]{
                    "[IDLE]Chao cau vo si tre! Hay noi chuyen voi Truong Lang truoc, sau do nhat mot mon vu khi tren mat dat."
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
                
                // Rewards
                Inventory inv = Inventory.MAPPER.get(player);
                if (inv != null) {
                    inv.addItem(Item.Type.COIN, 20); // Reward 20 coins
                    inv.removeItem(Item.Type.POTION_HEALTH, 1); // Consume the quest potion
                }
                Experience xp = Experience.MAPPER.get(player);
                if (xp != null) {
                    xp.addXp(150f); // Reward 150 XP
                }
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
                    Transform transform = Transform.MAPPER.get(player);
                    if (transform != null) {
                        viewModel.showFloatingText("[GOLD]+150 XP, +20 Coins![]",
                            transform.getPosition().x, transform.getPosition().y + 1f);
                    }
                }
                
                setStage(11); // Quest completed
            } else {
                npc.setDialogue(new String[]{
                    "[IDLE]Cam on cau vi Than Duoc!",
                    "[IDLE]Hay luyen tap cham chi nhe!"
                });
            }
        } else if ("Old Man".equalsIgnoreCase(npc.getName())) {
            if (currentStage <= 7) {
                npc.setDialogue(new String[]{
                    "[IDLE]Khu khu... Cau be tre tuoi, cau tim ai o day?"
                });
            } else if (currentStage == 8) {
                npc.setDialogue(new String[]{
                    "[IDLE]Khu khu... Ta la chu nhan ngoi nha nay.",
                    "[IDLE]Cau tim kiem Than Duoc cho vo su Monk sao?",
                    "[IDLE]Ta co thi dua no cho cau.",
                    "[IDLE]Nhung ta gia yeu qua, hay nhat binh mau than ky ta de tren chiec giuong kia giup ta."
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
    }
}
