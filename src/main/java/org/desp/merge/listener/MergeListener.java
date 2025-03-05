package org.desp.merge.listener;

import static org.desp.merge.utils.Button.WOOL_SLOT;
import static org.desp.merge.utils.MergeUtil.giveReward;
import static org.desp.merge.utils.Validator.isMergeInventoryClick;
import static org.desp.merge.utils.Validator.isPlayerInventory;
import static org.desp.merge.utils.Validator.isValidClick;

import com.binggre.velocitysocketclient.VelocityClient;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.desp.merge.dto.MergeItemInfo;
import org.desp.merge.event.MergeFailEvent;
import org.desp.merge.event.MergeSuccessEvent;
import org.desp.merge.event.MergeTryEvent;
import org.desp.merge.utils.Button;
import org.desp.merge.utils.MergeUtil;
import org.desp.merge.utils.Validator;
import org.desp.merge.view.ItemRender;

public class MergeListener implements Listener {

    private static final String MERGE_TICKET_ID = "기타_합성의서"; // 합성권 ID
    private static final String PROTECTION_ITEM_ID = "기타_파괴방지권"; // 파괴방지권 ID
    private static ItemStack wool;

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!Validator.isMergeInventory(e)) return;

        Inventory inventory = e.getInventory();
        setupWoolSlot(inventory);
        ItemRender.rendMaterials(e);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!isValidClick(e)) return;

        if (isPlayerInventory(e)) {
            e.setCancelled(true);
            return;
        }

        if (isMergeInventoryClick(e)) {
            e.setCancelled(true);
            handleMergeInventoryClick(e);
        }
    }

    private void handleMergeInventoryClick(InventoryClickEvent e) {
        Inventory clickedInventory = e.getClickedInventory();
        if (clickedInventory == null) return;

        if (e.getSlot() == WOOL_SLOT) {
            toggleWoolState(clickedInventory);
        } else if (e.getSlot() == Button.MERGE_SLOT) {
            handleMergeAction(e);
        }
    }

    private void setupWoolSlot(Inventory inventory) {
        wool = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = wool.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "파괴방지권 비활성화");
        wool.setItemMeta(meta);
        inventory.setItem(WOOL_SLOT, wool);
    }

    private void setWoolState(Inventory inventory, Material type, String displayName) {
        wool.setType(type);
        ItemMeta meta = wool.getItemMeta();
        meta.setDisplayName(displayName);
        wool.setItemMeta(meta);
        inventory.setItem(WOOL_SLOT, wool);
    }

    private void toggleWoolState(Inventory inventory) {
        wool = inventory.getItem(WOOL_SLOT);
        if (wool == null || wool.getType() == Material.RED_WOOL) {
            setWoolState(inventory, Material.GREEN_WOOL, ChatColor.GREEN + "파괴방지권 활성화");
        } else if (wool.getType() == Material.GREEN_WOOL) {
            setWoolState(inventory, Material.RED_WOOL, ChatColor.RED + "파괴방지권 비활성화");
        }
    }

    private void handleMergeAction(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory playerInventory = player.getInventory();

        if (!MergeUtil.hasMergeTicket(playerInventory)) {
            player.sendMessage("§c 합성권이 필요합니다!");
            return;
        }

        MergeItemInfo mergeItemInfo = MergeUtil.getMergeItemInfo(e);
        if (mergeItemInfo == null) {
            return;
        }

        if (!Validator.hasRequiredMaterials(playerInventory, mergeItemInfo.getMaterials(), mergeItemInfo.getCoreItem())) {
            player.sendMessage("§c 강화재료가 부족합니다.");
            return;
        }

        // 합성권 차감
        MergeUtil.removeItem(playerInventory, MERGE_TICKET_ID);

        Bukkit.getPluginManager().callEvent(new MergeTryEvent(player, mergeItemInfo));
        // 합성 진행
        if (MergeUtil.isMergeSuccessful(mergeItemInfo.getSuccessPercentage())) {
            Bukkit.getPluginManager().callEvent(new MergeSuccessEvent(player, mergeItemInfo));
            handleSuccessfulMerge(player, mergeItemInfo);
        } else {
            Bukkit.getPluginManager().callEvent(new MergeFailEvent(player, mergeItemInfo));

            handleFailedMerge(player, mergeItemInfo, playerInventory);
        }
    }

    private void handleSuccessfulMerge(Player player, MergeItemInfo mergeItemInfo) {

        MergeUtil.removeMaterials(player.getInventory(), mergeItemInfo.getMaterials(), true, mergeItemInfo.getCoreItem());

        String message = "§f" + player.getName() + "§a 님께서 " + mergeItemInfo.getAfterWeapon().replace("합성무기_", "")
                .replace("0", "") + " 합성에 성공했습니다!";
        Bukkit.broadcast(Component.text(message));
        VelocityClient.getInstance().getConnectClient().send(VelocityProxyListener.class, message);

        giveReward(player.getInventory(), mergeItemInfo.getAfterWeapon(), player);
    }

    private void handleFailedMerge(Player player, MergeItemInfo mergeItemInfo, Inventory playerInventory) {

        if (wool.getType() == Material.GREEN_WOOL && MergeUtil.removeItem(playerInventory, PROTECTION_ITEM_ID)) {
            player.sendMessage("§c 강화에 실패하였지만 파괴방지권으로 재료가 유지됩니다.");

            String message = "§f" + player.getName() + "§c 님께서 " + mergeItemInfo.getAfterWeapon().replace("합성무기_", "")
                    .replace("0", "") + " 합성에 실패하였지만, 수호의 빛이 작동했습니다!";
            Bukkit.broadcast(Component.text(message));
            VelocityClient.getInstance().getConnectClient().send(VelocityProxyListener.class, message);

        } else {
            MergeUtil.removeMaterials(playerInventory, mergeItemInfo.getMaterials(), false,
                    mergeItemInfo.getCoreItem());

            String message = "§f" + player.getName() + "§c 님께서 " + mergeItemInfo.getAfterWeapon().replace("합성무기_", "")
                    .replace("0", "") + " 합성에 실패하였습니다.";
            Bukkit.broadcast(Component.text(message));
            VelocityClient.getInstance().getConnectClient().send(VelocityProxyListener.class, message);
            player.sendMessage("§c 강화에 실패하였습니다.");
        }
    }
}
