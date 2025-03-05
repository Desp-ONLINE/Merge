package org.desp.merge.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.desp.merge.dto.MergeItemInfo;
import org.jetbrains.annotations.NotNull;

public class MergeSuccessEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private MergeItemInfo mergeItemInfo;

    public MergeSuccessEvent(Player player, MergeItemInfo mergeItemInfo) {
        this.mergeItemInfo = mergeItemInfo;
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public MergeItemInfo getMergeItemInfo() {
        return mergeItemInfo;
    }

    public void setMergeItemInfo(MergeItemInfo mergeItemInfo) {
        this.mergeItemInfo = mergeItemInfo;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {

    }
}
