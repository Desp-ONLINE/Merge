package org.desp.merge.listener;

import static org.desp.merge.utils.MergeUtil.giveReward;
import static org.desp.merge.utils.Validator.isMergeInventoryClick;
import static org.desp.merge.utils.Validator.isPlayerInventory;
import static org.desp.merge.utils.Validator.isValidClick;

import com.binggre.binggreEconomy.BinggreEconomy;
import com.binggre.velocitysocketclient.VelocityClient;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
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
    private Map<String, ItemStack> woolCache = new HashMap<>();

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!Validator.isMergeInventory(e)) return;

        Player player = (Player) e.getPlayer();
        Inventory inventory = e.getInventory();
        setupWoolSlot(inventory, player);
        ItemRender.rendMaterials(player, e);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!isValidClick(e)) return;

        if (isPlayerInventory(e)) {
            e.setCancelled(true);
            return;
        }

        if (!e.getAction().equals(InventoryAction.PICKUP_ALL)) {
            e.setCancelled(true);
            return;
        }

        if (isMergeInventoryClick(e)) {
            e.setCancelled(true);
            handleMergeInventoryClick(e);
        }
    }

    private void handleMergeInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory clickedInventory = e.getClickedInventory();
        if (clickedInventory == null) return;

        if (e.getSlot() == 16) {
            toggleWoolState(clickedInventory, player);
        } else if (e.getSlot() == Button.MERGE_SLOT) {
            handleMergeAction(e);
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
            player.sendMessage("§c 합성재료가 부족합니다.");
            return;
        }

        double balance = BinggreEconomy.getInst().getEconomy().getBalance(player);

        if (mergeItemInfo.getCost() > balance) {
            player.sendMessage("§c 합성에 필요한 금액이 부족합니다");
            return;
        }

        // 합성권 차감
        MergeUtil.removeItem(playerInventory, MERGE_TICKET_ID);

        // 합성 시도 비용 차감
        BinggreEconomy.getInst().getEconomy().withdrawPlayer(player, mergeItemInfo.getCost());

        Bukkit.getPluginManager().callEvent(new MergeTryEvent(player, mergeItemInfo));
        // 합성 진행
        if (MergeUtil.isMergeSuccessful(player, mergeItemInfo.getSuccessPercentage())) {
            Bukkit.getPluginManager().callEvent(new MergeSuccessEvent(player, mergeItemInfo));
            handleSuccessfulMerge(player, mergeItemInfo, playerInventory);
        } else {
            Bukkit.getPluginManager().callEvent(new MergeFailEvent(player, mergeItemInfo));
            handleFailedMerge(player, mergeItemInfo, playerInventory);
        }
    }

    private void handleSuccessfulMerge(Player player, MergeItemInfo mergeItemInfo, Inventory playerInventory) {
        ItemStack wool = woolCache.get(player.getUniqueId().toString());
        if (wool != null && wool.getType() == Material.GREEN_WOOL) {
            MergeUtil.removeItem(playerInventory, PROTECTION_ITEM_ID);
        }
            MergeUtil.removeMaterials(player, mergeItemInfo.getMaterials(), true, mergeItemInfo.getCoreItem());

            String message = "§f" + player.getName() + "§a 님께서 " + mergeItemInfo.getAfterWeapon().replace("합성무기_", "")
                    .replace("0", "") + " 합성에 성공했습니다!";
            Bukkit.broadcast(Component.text(message));
            VelocityClient.getInstance().getConnectClient().send(VelocityProxyListener.class, message);

            giveReward(player.getInventory(), mergeItemInfo.getAfterWeapon(), player);
    }

    private void handleFailedMerge(Player player, MergeItemInfo mergeItemInfo, Inventory playerInventory) {
        ItemStack wool = woolCache.get(player.getUniqueId().toString());
        if (wool != null && wool.getType() == Material.GREEN_WOOL && MergeUtil.removeItem(playerInventory, PROTECTION_ITEM_ID)) {
            player.sendMessage("§c 강화에 실패하였지만 수호의 빛이 재료를 보호했습니다!");

            String message = "§f" + player.getName() + "§c 님께서 " + mergeItemInfo.getAfterWeapon().replace("합성무기_", "")
                    .replace("0", "") + " 합성에 실패하였지만, 수호의 빛이 작동했습니다!";
            Bukkit.broadcast(Component.text(message));
            VelocityClient.getInstance().getConnectClient().send(VelocityProxyListener.class, message);

        } else {
            MergeUtil.removeMaterials(player, mergeItemInfo.getMaterials(), false,
                    mergeItemInfo.getCoreItem());

            String message = "§f" + player.getName() + "§c 님께서 " + mergeItemInfo.getAfterWeapon().replace("합성무기_", "")
                    .replace("0", "") + " 합성에 실패하였습니다.";
            Bukkit.broadcast(Component.text(message));
            VelocityClient.getInstance().getConnectClient().send(VelocityProxyListener.class, message);
            player.sendMessage("§c 강화에 실패하였습니다.");
        }
    }

    private void setupWoolSlot(Inventory inventory, Player player) {
        ItemStack wool = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = wool.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "수호의 빛 비활성화");
        wool.setItemMeta(meta);
        inventory.setItem(16, wool);
        woolCache.put(player.getUniqueId().toString(), wool);
    }

    private void setWoolState(Inventory inventory, Player player, Material type, String displayName) {
        ItemStack wool = new ItemStack(type);
        ItemMeta meta = wool.getItemMeta();
        meta.setDisplayName(displayName);
        wool.setItemMeta(meta);

        inventory.setItem(16, wool);
        woolCache.put(player.getUniqueId().toString(), wool); // 변경된 wool을 저장
    }


    private void toggleWoolState(Inventory inventory, Player player) {
        ItemStack wool = woolCache.getOrDefault(player.getUniqueId().toString(), new ItemStack(Material.RED_WOOL));

        if (wool.getType() == Material.RED_WOOL) {
            setWoolState(inventory, player, Material.GREEN_WOOL, ChatColor.GREEN + "수호의 빛 활성화");
        } else {
            setWoolState(inventory, player, Material.RED_WOOL, ChatColor.RED + "수호의 빛 비활성화");
        }
    }
}
