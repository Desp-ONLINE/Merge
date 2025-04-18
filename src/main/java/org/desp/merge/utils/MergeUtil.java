package org.desp.merge.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.desp.merge.Merge;
import org.desp.merge.database.LogSystemRepository;
import org.desp.merge.dto.MergeItemInfo;

public class MergeUtil {

    private static final String MERGE_TICKET_ID = "기타_합성의서"; // 합성권 ID

    public static void setLore(Player player, InventoryOpenEvent e, MergeItemInfo weaponData) {
        ItemStack cursor = e.getInventory().getItem(13);
        if (cursor == null || !cursor.hasItemMeta()) return;

        LogSystemRepository.isPlayerFirstMerge(player);

        Map<String, Boolean> playerCheckCache = LogSystemRepository.playerCheckCache;

        List<String> upgradeLore;
        if (playerCheckCache.containsKey(player.getUniqueId().toString()) && playerCheckCache.get(
                player.getUniqueId().toString())) {
            upgradeLore = Arrays.asList(
                    "§f    합성 정보",
                    "§a     강화 필요 비용: §f" + weaponData.getCost() + "골드",
                    "§a     성공 확률: §f 100%",
                    "§a     실패 확률: §f 0%"
            );
        } else {
            upgradeLore = Arrays.asList(
                    "§f    합성 정보",
                    "§a     강화 필요 비용: §f" + weaponData.getCost() + "골드",
                    "§a     성공 확률: §f" + weaponData.getSuccessPercentage() + "%",
                    "§a     실패 확률: §f" + (100 - weaponData.getSuccessPercentage()) + "%"
            );
        }

        ItemStack button = new ItemStack(Material.PAPER, 1);
        ItemMeta buttonItemMeta = button.getItemMeta();
        buttonItemMeta.setDisplayName("§a 합성하기");
        buttonItemMeta.setCustomModelData(10260);
        button.setItemMeta(buttonItemMeta);
        button.setLore(upgradeLore);

        e.getInventory().setItem(Button.MERGE_SLOT, button);
    }

    public static boolean isMergeSuccessful(Player player, int successPercentage) {
        // 합성 시도 처음이면 무조건 성공
        Map<String, Boolean> playerCheckCache = LogSystemRepository.playerCheckCache;

        if (playerCheckCache.containsKey(player.getUniqueId().toString()) && playerCheckCache.get(
                player.getUniqueId().toString())) {
            playerCheckCache.put(player.getUniqueId().toString(), false);
            return true;
        }
        return Math.random() * 100 < successPercentage;
    }

    public static MergeItemInfo getMergeItemInfo(InventoryClickEvent e) {
        String afterWeapon = e.getView().getTitle().split("-")[1];
        return Merge.getAllWeaponData().get(afterWeapon);
    }

    public static boolean hasMergeTicket(Inventory inventory) {
        return containsItem(inventory, MERGE_TICKET_ID);
    }

    private static boolean containsItem(Inventory inventory, String itemId) {
        for (ItemStack item : inventory.getContents()) {
            if (item != null && itemId.equals(MMOItems.plugin.getID(item))) {
                return true;
            }
        }
        return false;
    }

    public static boolean removeItem(Inventory inventory, String itemId) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);

            if (item != null && itemId.equals(MMOItems.plugin.getID(item))) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1); // 개수 1 감소
                } else {
                    inventory.setItem(i, null); // 개수가 1개면 슬롯을 비움
                }
                return true; // 첫 번째로 찾은 아이템 하나만 처리하고 종료
            }
        }
        return false; // 해당 아이템이 없으면 false 반환
    }

    public static void removeMaterials(Player player, List<Map<String, Integer>> materials, boolean removeCoreItems, List<String> coreItems) {
        PlayerInventory inventory = player.getInventory();

        for (Map<String, Integer> material : materials) {
            for (Map.Entry<String, Integer> entry : material.entrySet()) {
                String materialId = entry.getKey();
                int requiredQuantity = entry.getValue();

                if (!removeCoreItems && coreItems != null && coreItems.contains(materialId)) {
                    continue;
                }

                ItemStack offHandItem = inventory.getItemInOffHand();
                if (offHandItem != null) {
                    String offHandId = MMOItems.plugin.getID(offHandItem);
                    if (offHandId != null && materialId.equals(offHandId)) {
                        int amount = offHandItem.getAmount();
                        if (amount >= requiredQuantity) {
                            offHandItem.setAmount(amount - requiredQuantity);
                            if (amount - requiredQuantity <= 0) {
                                inventory.setItemInOffHand(null);
                            } else {
                                inventory.setItemInOffHand(offHandItem);
                            }
                            requiredQuantity = 0;
                        } else {
                            requiredQuantity -= amount;
                            inventory.setItemInOffHand(null);
                        }
                    }
                }

                if (requiredQuantity > 0) {
                    List<ItemStack> toRemove = new ArrayList<>();

                    for (ItemStack item : inventory.getContents()) {
                        if (item == null) continue;

                        String itemId = MMOItems.plugin.getID(item);
                        if (itemId == null || !materialId.equals(itemId)) continue;

                        if (item.getAmount() > requiredQuantity) {
                            item.setAmount(item.getAmount() - requiredQuantity);
                            requiredQuantity = 0;
                            break;
                        } else {
                            requiredQuantity -= item.getAmount();
                            toRemove.add(item);
                        }

                        if (requiredQuantity <= 0) break;
                    }

                    for (ItemStack item : toRemove) {
                        inventory.remove(item);
                    }
                }
            }
        }

        if (removeCoreItems && coreItems != null) {
            for (String coreItem : coreItems) {
                ItemStack offHandItem = inventory.getItemInOffHand();
                if (offHandItem != null) {
                    String offHandId = MMOItems.plugin.getID(offHandItem);
                    if (offHandId != null && coreItem.equals(offHandId)) {
                        inventory.setItemInOffHand(null);
                        continue;
                    }
                }

                for (ItemStack item : inventory.getContents()) {
                    if (item == null) continue;

                    String itemId = MMOItems.plugin.getID(item);
                    if (itemId != null && coreItem.equals(itemId)) {
                        inventory.remove(item);
                        break;
                    }
                }
            }
        }
    }

    public static void giveReward(Inventory inventory, String afterWeapon, Player player) {
        ItemStack rewardItem = MMOItems.plugin.getItem(Type.SWORD, afterWeapon);
        if (rewardItem != null) {
            inventory.addItem(rewardItem);
            player.sendMessage("축하합니다! 강화에 성공했습니다: " + rewardItem.getItemMeta().getDisplayName());
        } else {
            player.sendMessage("§c 보상 아이템 생성에 실패했습니다.");
        }
    }
}
