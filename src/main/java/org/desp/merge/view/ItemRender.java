package org.desp.merge.view;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.desp.merge.Merge;
import org.desp.merge.dto.MergeItemInfo;
import org.desp.merge.utils.Button;
import org.desp.merge.utils.MergeUtil;

public class ItemRender {

    public static void rendMaterials(Player player, InventoryOpenEvent e){
        String title = e.getView().getTitle().split("-")[0];
        if (!title.equals("합성")) {
            return;
        }
        Map<String, MergeItemInfo> allWeaponData = Merge.getAllWeaponData();

        String afterWeapon = e.getView().getTitle().split("-")[1];

        MergeItemInfo mergeItemInfo = allWeaponData.get(afterWeapon);

        if (mergeItemInfo == null) {
            return;
        }

        setCoreItem(e, mergeItemInfo);
        setAfterWeapon(player, e, mergeItemInfo);
        setMaterials(e, mergeItemInfo);
    }

    private static void setMaterials(InventoryOpenEvent e, MergeItemInfo mergeItemInfo) {
        List<Map<String, Integer>> materials = mergeItemInfo.getMaterials();

        int slot = 39;
        for (Map<String, Integer> material : materials) {
            for (Entry<String, Integer> entry : material.entrySet()) {
                if (slot > 44) slot = 48;
                String materialId = entry.getKey();
                int quantity = entry.getValue();

                ItemStack materialItem;
                if (MMOItems.plugin.getItem(Type.SWORD, materialId) == null) {
                    materialItem = MMOItems.plugin.getItem(Type.MISCELLANEOUS, materialId);
                } else {
                    materialItem = MMOItems.plugin.getItem(Type.SWORD, materialId);
                }
                if (materialItem == null) {
                    continue;
                }

                materialItem.setAmount(quantity);

                e.getInventory().setItem(slot, materialItem);
                slot++;
            }
        }
    }

    private static void setAfterWeapon(Player player, InventoryOpenEvent e, MergeItemInfo mergeItemInfo) {
        ItemStack item = MMOItems.plugin.getItem(Type.SWORD, mergeItemInfo.getAfterWeapon());
        MergeUtil.setLore(player, e, mergeItemInfo);
        e.getInventory().setItem(Button.AFTER_SLOT, item);
    }

    private static void setCoreItem(InventoryOpenEvent e, MergeItemInfo mergeItemInfo) {
        List<String> coreItems = mergeItemInfo.getCoreItem();
        ItemStack coreItem = MMOItems.plugin.getItem(Type.SWORD, coreItems.getFirst());
        e.getInventory().setItem(Button.CORE_SLOT, coreItem);
    }
}
