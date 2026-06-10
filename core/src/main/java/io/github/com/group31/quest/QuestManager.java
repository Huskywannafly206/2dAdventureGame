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
        if (currentStage == 0) return "Meet the Village Chief";
        if (currentStage == 1) return "Gather Soothing Herbs";
        if (currentStage == 2) return "Meet the Hunter";
        if (currentStage == 3) return "Defeat the Slimes";
        if (currentStage == 4) return "Report to the Hunter";
        if (currentStage == 5) return "Pick up the Rusty Sword";
        if (currentStage == 6) return "Enter the Ancient Forest";
        if (currentStage == 7) return "Secrets of the Forest";
        if (currentStage == 8) return "The Three Relics";
        if (currentStage == 9) return "The Golden Key";
        if (currentStage == 10) return "Report to the Chief";
        if (currentStage == 11) return "Pick up Ice Map Key";
        if (currentStage == 12) return "Enter the Ice Land";
        return "All Quests Completed";
    }

    public String getQuestObjective() {
        return switch (currentStage) {
            case 0 -> "Go meet the Village Chief to receive instructions.";
            case 1 -> {
                int count = 0;
                if (player != null) {
                    Inventory inv = Inventory.MAPPER.get(player);
                    if (inv != null) {
                        count = inv.getItemCount(Item.Type.SOOTHING_HERB);
                    }
                }
                yield "Gather 5 Soothing Herbs (Gathered: " + count + "/5).";
            }
            case 2 -> "Go to the cabin at the edge of the village and meet the Hunter.";
            case 3 -> "Defeat 4 Slimes in the village (Defeated: " + slimesDefeated + "/4).";
            case 4 -> "Go back and report to the Hunter.";
            case 5 -> "Pick up the rusty sword near the Hunter.";
            case 6 -> "Enter the Ancient Forest to investigate its secrets.";
            case 7 -> "Meet the Forest Spirit to talk.";
            case 8 -> "Find all 3 objects: coin, silver cup, and silver key following the 3 stone tablets.";
            case 9 -> "Pick up the golden key to the right of the Forest Spirit.";
            case 10 -> "Return to the Village Chief to report and receive further instructions.";
            case 11 -> "Pick up the Ice Map Key next to the Chief.";
            case 12 -> "Enter the Ice Land through the northern portal.";
            default -> "No active quests.";
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
                        viewModel.showFloatingText("[YELLOW]Quest: Enter the Ancient Forest![]", 
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
                    viewModel.showFloatingText("[YELLOW]Quest: Meet the Forest Spirit![]", 
                        transform.getPosition().x, transform.getPosition().y + 1f);
                }
            }
        }
        if (currentStage == 12 && "ICEMAP1".equalsIgnoreCase(mapName)) {
            setStage(13);
            if (viewModel != null) {
                Transform transform = Transform.MAPPER.get(player);
                if (transform != null) {
                    viewModel.showFloatingText("[YELLOW]All Quests Completed![]", 
                        transform.getPosition().x, transform.getPosition().y + 1f);
                }
            }
        }
    }

    public void checkIceMapKeyPickup(Entity player) {
        this.player = player;
        if (currentStage == 11) {
            setStage(12);
            if (viewModel != null) {
                Transform transform = Transform.MAPPER.get(player);
                if (transform != null) {
                    viewModel.showFloatingText("[YELLOW]Quest: Enter the Ice Land![]", 
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
                    viewModel.showFloatingText("[YELLOW]Quest: Return to the Chief![]", 
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
                        viewModel.showFloatingText("[YELLOW]Got 5 herbs, return to the Chief![]", 
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
                        viewModel.showFloatingText("[YELLOW]Defeated 4 Slimes, return to the Hunter![]", 
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
                            viewModel.showFloatingText("[YELLOW]Quest: Meet the Hunter![]", 
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
                    "[IDLE]Enter the forest to investigate its secrets."
                });
            } else if (currentStage == 7) {
                npc.setDialogue(new String[]{
                    "[IDLE]Find and meet the Forest Spirit in the forest."
                });
            } else if (currentStage == 8) {
                npc.setDialogue(new String[]{
                    "[IDLE]Find all 3 relics following the instructions of the 3 stone tablets."
                });
            } else if (currentStage == 9) {
                npc.setDialogue(new String[]{
                    "[IDLE]Pick up the Golden Key and bring it back here."
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
                if (currentStage == 11 && iceKeyCount <= 0) {
                    npc.setDialogue(new String[]{
                        "[IDLE]Pick up the Ice Map Key next to me and head to the Ice Land."
                    });
                    Transform npcT = Transform.MAPPER.get(npcEntity);
                    float spawnX = npcT != null ? npcT.getPosition().x + 1f : player.getComponent(Transform.class).getPosition().x;
                    float spawnY = npcT != null ? npcT.getPosition().y : player.getComponent(Transform.class).getPosition().y;
                    if (configurator != null) {
                        configurator.spawnIceMapKey(spawnX, spawnY);
                    }
                } else if (currentStage == 11) {
                    npc.setDialogue(new String[]{
                        "[IDLE]Pick up the Ice Map Key next to me and head to the Ice Land."
                    });
                } else if (currentStage == 12) {
                    npc.setDialogue(new String[]{
                        "[IDLE]Use the Ice Map Key to pass through the northern portal."
                    });
                } else {
                    if (iceKeyCount <= 0) {
                        npc.setDialogue(new String[]{
                            "[IDLE]Did you lose the Ice Map Key? I will leave another one here for you."
                        });
                        Transform npcT = Transform.MAPPER.get(npcEntity);
                        float spawnX = npcT != null ? npcT.getPosition().x + 1f : player.getComponent(Transform.class).getPosition().x;
                        float spawnY = npcT != null ? npcT.getPosition().y : player.getComponent(Transform.class).getPosition().y;
                        if (configurator != null) {
                            configurator.spawnIceMapKey(spawnX, spawnY);
                        }
                    } else {
                        npc.setDialogue(new String[]{
                            "[IDLE]Prepare yourself well to enter the Dark Dungeon.",
                            "[IDLE]I believe in your strength."
                        });
                    }
                }
            }
        } else if ("tho_san".equalsIgnoreCase(npc.getName())) {
            if (currentStage < 2) {
                npc.setDialogue(new String[]{
                    "[IDLE]Hello! I am the Hunter. Is there something you need?"
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
