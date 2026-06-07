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
        if (currentStage == 5) return "Nhat Kiem Ri Sat";
        if (currentStage == 6) return "Tien Vao Rung Gioc";
        if (currentStage == 7) return "Bi Mat Rung Gioc";
        if (currentStage == 8) return "Ba Co Vat";
        if (currentStage == 9) return "Golden Key";
        if (currentStage == 10) return "Bao Cao Truong Lang";
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
            case 6 -> "Tiến vào khu rừng để tìm hiểu bí mật.";
            case 7 -> "Gặp Forest_Spirit để nói chuyện.";
            case 8 -> "Đi tìm đủ 3 objects: coin, silvercup và silverkey theo chỉ dẫn của 3 bia đá.";
            case 9 -> "Nhặt chiếc chìa khóa golden_key bên phải Forest_Spirit.";
            case 10 -> "Quay lại gặp Trưởng làng để báo cáo và nhận chỉ dẫn tiếp theo.";
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
                        viewModel.showFloatingText("[YELLOW]Nhiem vu: Tien vao Rung Gioc![]", 
                            transform.getPosition().x, transform.getPosition().y + 1f);
                    }
                }
            }
        }
    }

    public void checkMapEnter(String mapName, Entity player) {
        this.player = player;
        if (currentStage == 6 && "JUNGLEMAP1".equalsIgnoreCase(mapName)) {
            setStage(7);
            if (viewModel != null) {
                Transform transform = Transform.MAPPER.get(player);
                if (transform != null) {
                    viewModel.showFloatingText("[YELLOW]Nhiem vu: Gap Forest_Spirit![]", 
                        transform.getPosition().x, transform.getPosition().y + 1f);
                }
            }
        }
    }

    public void checkPotionPickup(Entity player) {
        // Potion pickup is no longer a quest stage, keeping method empty for compatibility
    }

    public void checkGoldKeyPickup(Entity player) {
        this.player = player;
        if (currentStage == 9) {
            setStage(10);
            if (viewModel != null) {
                Transform transform = Transform.MAPPER.get(player);
                if (transform != null) {
                    viewModel.showFloatingText("[YELLOW]Nhiem vu: Ve gap Truong Lang![]", 
                        transform.getPosition().x, transform.getPosition().y + 1f);
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
                    "[IDLE]Ah, you're finally here... Look at them, this strange disease is consuming our village day by day.",
                    "[IDLE]The only antidote lies deep within the Dark Dungeon, but that place has been sealed away for centuries.",
                    "[IDLE]Before you leave, there is a small favor I must ask.",
                    "[IDLE]Please help me gather 5 Soothing Herbs from the south so I can brew some medicine to help the sick hold on."
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
                                inv.getItemCount(Item.Type.JUNGLE_MAP_KEY),
                                inv.getItemCount(Item.Type.SILVER_CUP),
                                inv.getItemCount(Item.Type.LAUREL_LEAF),
                                inv.getItemCount(Item.Type.ICE_MAP_KEY)
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
            } else if (currentStage == 6) {
                npc.setDialogue(new String[]{
                    "[IDLE]Hãy tiến vào khu rừng để tìm hiểu bí mật."
                });
            } else if (currentStage == 7) {
                npc.setDialogue(new String[]{
                    "[IDLE]Hãy tìm gặp Forest_Spirit trong khu rừng."
                });
            } else if (currentStage == 8) {
                npc.setDialogue(new String[]{
                    "[IDLE]Hãy tìm đủ 3 báu vật theo chỉ dẫn của 3 bia đá."
                });
            } else if (currentStage == 9) {
                npc.setDialogue(new String[]{
                    "[IDLE]Hãy nhặt chiếc chìa khóa vàng (Golden Key) và mang về đây."
                });
            } else if (currentStage == 10) {
                npc.setDialogue(new String[]{
                    "[IDLE]Ah! You have returned safely, and with the Golden Key! Incredible!",
                    "[IDLE]This Golden Key is the key to the Dark Dungeon, where the cure for our village is located.",
                    "[IDLE]Prepare yourself well, then proceed to the dungeon."
                });
                setStage(11);
                
                Inventory inv = Inventory.MAPPER.get(player);
                if (inv != null) {
                    inv.addItem(Item.Type.COIN, 30);
                }
                Experience xp = Experience.MAPPER.get(player);
                if (xp != null) {
                    xp.addXp(200f);
                }

                Transform npcT = Transform.MAPPER.get(npcEntity);
                float spawnX = npcT != null ? npcT.getPosition().x + 1f : player.getComponent(Transform.class).getPosition().x;
                float spawnY = npcT != null ? npcT.getPosition().y : player.getComponent(Transform.class).getPosition().y;
                if (configurator != null) {
                    configurator.spawnIceMapKey(spawnX, spawnY);
                }

                if (viewModel != null) {
                    if (inv != null) {
                        viewModel.updateInventory(
                            inv.getItemCount(Item.Type.POTION_HEALTH),
                            inv.getItemCount(Item.Type.COIN),
                            inv.getItemCount(Item.Type.KEY),
                            inv.getItemCount(Item.Type.GOLD_KEY),
                            inv.getItemCount(Item.Type.SILVER_KEY),
                            inv.getItemCount(Item.Type.SOOTHING_HERB),
                            inv.getItemCount(Item.Type.JUNGLE_MAP_KEY),
                            inv.getItemCount(Item.Type.SILVER_CUP),
                            inv.getItemCount(Item.Type.LAUREL_LEAF),
                            inv.getItemCount(Item.Type.ICE_MAP_KEY)
                        );
                    }
                    Transform transform = Transform.MAPPER.get(player);
                    if (transform != null) {
                        viewModel.showFloatingText("[GOLD]+200 XP, +30 Coins![]",
                            transform.getPosition().x, transform.getPosition().y + 1f);
                    }
                }
            } else {
                Inventory inv = Inventory.MAPPER.get(player);
                int iceKeyCount = inv != null ? inv.getItemCount(Item.Type.ICE_MAP_KEY) : 0;
                if (currentStage >= 11 && iceKeyCount <= 0) {
                    npc.setDialogue(new String[]{
                        "[IDLE]Cháu đã làm mất Ice Map Key ư? Ta sẽ để một cái khác ở đây cho cháu."
                    });
                    Transform npcT = Transform.MAPPER.get(npcEntity);
                    float spawnX = npcT != null ? npcT.getPosition().x + 1f : player.getComponent(Transform.class).getPosition().x;
                    float spawnY = npcT != null ? npcT.getPosition().y : player.getComponent(Transform.class).getPosition().y;
                    if (configurator != null) {
                        configurator.spawnIceMapKey(spawnX, spawnY);
                    }
                } else {
                    npc.setDialogue(new String[]{
                        "[IDLE]Hãy chuẩn bị kỹ lưỡng để vào ngục tối (Dark Dungeon).",
                        "[IDLE]Ta tin tưởng vào năng lực của cháu."
                    });
                }
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
                            inv.getItemCount(Item.Type.JUNGLE_MAP_KEY),
                            inv.getItemCount(Item.Type.SILVER_CUP),
                            inv.getItemCount(Item.Type.LAUREL_LEAF),
                            inv.getItemCount(Item.Type.ICE_MAP_KEY)
                        );
                    }
                }
                
                setStage(5);
            } else {
                npc.setDialogue(new String[]{
                    "[IDLE]Keep your wits about you, young man."
                });
            }
        } else if ("Forest_Spirit".equalsIgnoreCase(npc.getName())) {
            if (currentStage == 7) {
                npc.setDialogue(new String[]{
                    "[IDLE]Halt, mortal... This forest does not welcome the weak and shallow-minded. You come seeking the guardian relics, do you not?",
                    "[IDLE]Hahaha! Pleading holds no value here. If you wish to claim the Key, the Chalice, and the Coin, you must overcome three trials representing the three virtues of a hero: Courage, Wisdom, and Character.",
                    "[IDLE]Venture deeper inside, the mist shall guide your way."
                });
                setStage(8);
            } else if (currentStage == 8) {
                Inventory inv = Inventory.MAPPER.get(player);
                boolean hasCoin = inv != null && inv.getItemCount(Item.Type.COIN) >= 1;
                boolean hasCup = inv != null && inv.getItemCount(Item.Type.SILVER_CUP) >= 1;
                boolean hasKey = inv != null && inv.getItemCount(Item.Type.SILVER_KEY) >= 1;

                if (hasCoin && hasCup && hasKey) {
                    npc.setDialogue(new String[]{
                        "[IDLE]Impressive, human youth. You possess the Courage to endure, the Wisdom to see through, and the Character to choose wisely. You have gathered all three relics."
                    });
                    if (inv != null) {
                        inv.removeItem(Item.Type.COIN, 1);
                        inv.removeItem(Item.Type.SILVER_CUP, 1);
                        inv.removeItem(Item.Type.SILVER_KEY, 1);
                    }
                    if (configurator != null) {
                        Transform t = Transform.MAPPER.get(npcEntity);
                        if (t != null) {
                            configurator.spawnGoldKey(t.getPosition().x + 1f, t.getPosition().y);
                        }
                    }
                    setStage(9);
                    if (viewModel != null) {
                        if (inv != null) {
                            viewModel.updateInventory(
                                inv.getItemCount(Item.Type.POTION_HEALTH),
                                inv.getItemCount(Item.Type.COIN),
                                inv.getItemCount(Item.Type.KEY),
                                inv.getItemCount(Item.Type.GOLD_KEY),
                                inv.getItemCount(Item.Type.SILVER_KEY),
                                inv.getItemCount(Item.Type.SOOTHING_HERB),
                                inv.getItemCount(Item.Type.JUNGLE_MAP_KEY),
                                inv.getItemCount(Item.Type.SILVER_CUP),
                                inv.getItemCount(Item.Type.LAUREL_LEAF),
                                inv.getItemCount(Item.Type.ICE_MAP_KEY)
                            );
                        }
                        Transform transform = Transform.MAPPER.get(player);
                        if (transform != null) {
                            viewModel.showFloatingText("[YELLOW]Nhiem vu: Nhat Golden Key![]", 
                                transform.getPosition().x, transform.getPosition().y + 1f);
                        }
                    }
                } else {
                    npc.setDialogue(new String[]{
                        "[IDLE]If you wish to claim the Key, the Chalice, and the Coin, you must overcome three trials representing the three virtues of a hero: Courage, Wisdom, and Character.",
                        "[IDLE]Venture deeper inside, the mist shall guide your way."
                    });
                }
            } else if (currentStage == 9) {
                npc.setDialogue(new String[]{
                    "[IDLE]Take the Golden Key on the right and return to the village chief."
                });
            } else {
                npc.setDialogue(new String[]{
                    "[IDLE]Return to your village chief. He is waiting for you."
                });
            }
        }
    }
}
