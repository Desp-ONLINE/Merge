package org.desp.merge;

import com.binggre.velocitysocketclient.VelocityClient;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.desp.merge.command.MergeCommand;
import org.desp.merge.database.Repository;
import org.desp.merge.dto.MergeItemInfo;
import org.desp.merge.listener.MergeListener;
import org.desp.merge.listener.PlayerQuitListener;
import org.desp.merge.listener.VelocityProxyListener;

public final class Merge extends JavaPlugin {

    private static Map<String, MergeItemInfo> allWeaponData = new HashMap<>();
    private Repository dbRepository;
    private static Merge instance;

    public static Merge getInstance() {
        return instance;
    }

    public static Map<String, MergeItemInfo> getAllWeaponData() {
        return allWeaponData;
    }

    @Override
    public void onEnable() {
        instance = this;
        this.dbRepository = new Repository();
        register();
        VelocityClient.getInstance().getConnectClient().registerListener(VelocityProxyListener.class);

        this.getServer().getPluginManager().registerEvents(new MergeListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getCommand("merge").setExecutor(new MergeCommand());
    }

    @Override
    public void onDisable() {

    }

    public void register() {
        Map<String, MergeItemInfo> newAllWeaponData = dbRepository.getAllWeaponData();
        allWeaponData = newAllWeaponData;
    }

}
