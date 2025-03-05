package org.desp.merge.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.desp.merge.Merge;
import org.desp.merge.ui.MergeUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MergeCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            return false;
        }
        if (!player.isOp()) {
            return false;
        }
        String afterWeapon = strings[0];

        MergeUI mergeUI = new MergeUI(afterWeapon);

        player.openInventory(mergeUI.getInventory());
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String s, @NotNull String[] strings) {
        List<String> completions = new ArrayList<>();
        Set<String> keySet = Merge.getAllWeaponData().keySet();
        completions.addAll(keySet);
        return completions;
    }
}
