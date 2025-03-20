package org.desp.merge.ui;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class MergeUI implements InventoryHolder {

    public Inventory inventory;
    private final String afterWeaponId;

    public MergeUI(String afterWeaponId) {
        this.afterWeaponId = afterWeaponId;
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (this.inventory == null) {
            this.inventory = Bukkit.createInventory(this, 54, "합성-" + afterWeaponId);

            setBackground();
            setExplainSign(4);
            setExplainSign2(28);
        }
        return inventory;
    }

    private void setExplainSign(int slot) {
        ItemStack explainItemStack = new ItemStack(Material.PAPER, 1);
        ItemMeta explain = explainItemStack.getItemMeta();
        explain.setCustomModelData(10237);
        explain.setDisplayName("§a 합성 성공 시 아이템입니다");
        explainItemStack.setItemMeta(explain);
        inventory.setItem(slot, explainItemStack);
    }

    private void setExplainSign2(int slot) {
        ItemStack explainItemStack = new ItemStack(Material.PAPER, 1);
        ItemMeta explain = explainItemStack.getItemMeta();
        explain.setDisplayName("§a 코어 아이템");
        explain.setCustomModelData(10237);
        List<String> lore = new ArrayList<>();
        lore.add(" ");
        lore.add(" §f합성에 실패해도 사라지지 않습니다.");
        explain.setLore(lore);
        explainItemStack.setItemMeta(explain);
        inventory.setItem(slot, explainItemStack);
    }

    private void setBackground() {
        ItemStack glassItemStack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glass = glassItemStack.getItemMeta();
        glass.setDisplayName("§f");
        glassItemStack.setItemMeta(glass);

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, glassItemStack);
        }
    }
}
