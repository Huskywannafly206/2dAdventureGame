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

    private QuestManager() {}

    public void setViewModel(GameViewModel viewModel) {
        this.viewModel = viewModel;
        updateQuestHUD();
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
        if (currentStage <= 1) return "Khoi Dau Moi";
        if (currentStage <= 4) return "Can Nha Bi An";
        if (currentStage == 5) return "Bao Cao Ket Qua";
        return "Nhiem Vu Da Xong";
    }

    public String getQuestObjective() {
        return switch (currentStage) {
            case 0 -> "Nhat vu khi dau tien tren mat dat.";
            case 1 -> "Noi chuyen voi vo su Monk o phia dong.";
            case 2 -> "Di vao can nha go o phia tay.";
            case 3 -> "Noi chuyen voi Ong Lao trong nha.";
            case 4 -> "Nhat Binh Mau Than Ky tren giuong.";
            case 5 -> "Mang Than Duoc ve cho Monk o phia dong.";
            default -> "Khong co nhiem vu nao.";
        };
    }

    public void checkWeaponPickup(Entity player) {
        if (currentStage == 0) {
            CombatState cs = CombatState.MAPPER.get(player);
            if (cs != null && cs.getUnlockedWeapons().size() > 1) {
                setStage(1);
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
        if (currentStage == 2 && "VILLAGE_HOUSE".equalsIgnoreCase(mapName)) {
            setStage(3);
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
        if (currentStage == 4) {
            Inventory inv = Inventory.MAPPER.get(player);
            if (inv != null && inv.getItemCount(Item.Type.POTION_HEALTH) > 0) {
                setStage(5);
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

    public void onTalkToNpc(Entity npcEntity, Entity player) {
        Npc npc = Npc.MAPPER.get(npcEntity);
        if (npc == null) return;

        if ("Monk".equalsIgnoreCase(npc.getName())) {
            if (currentStage == 0) {
                npc.setDialogue(new String[]{
                    "[IDLE]Chao cau vo si tre! Hay nhat mot mon vu khi tren mat dat truoc da."
                });
            } else if (currentStage == 1) {
                npc.setDialogue(new String[]{
                    "[IDLE]Tot lam, cau da co vu khi trong tay!",
                    "[IDLE]Ta muon thu thach cau.",
                    "[IDLE]Hay den dieu tra can nha go co kinh o phia tay cua lang.",
                    "[IDLE]Nghe don noi do co mot Ong Lao dang giu mot Than Duoc bi an.",
                    "[IDLE]Cau co san long di lay no ve day giup ta khong?"
                });
                setStage(2);
            } else if (currentStage >= 2 && currentStage <= 4) {
                npc.setDialogue(new String[]{
                    "[IDLE]Cau van chua lay duoc Than Duoc sao?",
                    "[IDLE]Hay tim can nha go phia tay lang va noi chuyen voi Ong Lao."
                });
            } else if (currentStage == 5) {
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
                        inv != null ? inv.getItemCount(Item.Type.KEY) : 0
                    );
                    Transform transform = Transform.MAPPER.get(player);
                    if (transform != null) {
                        viewModel.showFloatingText("[GOLD]+150 XP, +20 Coins![]",
                            transform.getPosition().x, transform.getPosition().y + 1f);
                    }
                }
                
                setStage(6); // Quest completed
            } else {
                npc.setDialogue(new String[]{
                    "[IDLE]Cam on cau vi Than Duoc!",
                    "[IDLE]Hay luyen tap cham chi nhe!"
                });
            }
        } else if ("Old Man".equalsIgnoreCase(npc.getName())) {
            if (currentStage <= 2) {
                npc.setDialogue(new String[]{
                    "[IDLE]Khu khu... Cau be tre tuoi, cau tim ai o day?"
                });
            } else if (currentStage == 3) {
                npc.setDialogue(new String[]{
                    "[IDLE]Khu khu... Ta la chu nhan ngoi nha nay.",
                    "[IDLE]Cau tim kiem Than Duoc cho vo su Monk sao?",
                    "[IDLE]Ta co thi dua no cho cau.",
                    "[IDLE]Nhung ta gia yeu qua, hay nhat binh mau than ky ta de tren chiec giuong kia giup ta."
                });
                setStage(4);
            } else if (currentStage == 4) {
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
