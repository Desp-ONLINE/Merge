package org.desp.merge.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.desp.merge.ui.MergeUI;

public class Validator {
    private static final String MERGE_TITLE = "합성";

    public static boolean isInvalidClick(InventoryClickEvent e) {
        return e.getClickedInventory() == null || !(e.getInventory().getHolder() instanceof MergeUI);
    }

    public static boolean hasRequiredMaterials(Inventory inventory, List<Map<String, Integer>> requiredMaterials, List<String> coreItems) {
        Map<String, Integer> playerMaterials = new HashMap<>();
        for (ItemStack item : inventory.getContents()) {
            if (item == null) continue;
            String materialId = MMOItems.plugin.getID(item);
            if (materialId != null) {
                playerMaterials.put(materialId, playerMaterials.getOrDefault(materialId, 0) + item.getAmount());
            }
        }
        for (Map<String, Integer> requiredMaterial : requiredMaterials) {
            for (Map.Entry<String, Integer> entry : requiredMaterial.entrySet()) {
                if (playerMaterials.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                    return false;
                }
            }
        }
        for (String coreItem : coreItems) {
            if (!playerMaterials.containsKey(coreItem)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isMergeInventory(InventoryOpenEvent e) {
        return e.getInventory().getHolder() instanceof MergeUI;
    }

    public static boolean isMergeInventoryClick(InventoryClickEvent e) {
//        return e.getView().getTitle().split("-")[0].equals(MERGE_TITLE) && isChestSlot(e);
        return e.getInventory().getHolder() instanceof MergeUI;

    }

    public static boolean isValidClick(InventoryClickEvent e) {
        return !Validator.isInvalidClick(e);
    }

    public static boolean isPlayerInventory(InventoryClickEvent e) {
        return e.getClickedInventory() != null && e.getClickedInventory().getType() == InventoryType.PLAYER;
    }

    public static boolean isChestSlot(InventoryClickEvent e) {
        return e.getClickedInventory() != null && e.getClickedInventory().getType() == InventoryType.CHEST;
    }
}
