package org.desp.merge.database;

import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;
import org.desp.merge.Merge;

public class LogSystemDBConfig {

    public String getMongoConnectionContent(){
        File file = new File(Merge.getInstance().getDataFolder().getPath() + "/config.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        String url = yml.getString("mongodb.url");
        int port = yml.getInt("mongodb.port");
        String address = yml.getString("mongodb.address");

        return String.format("%s%s:%s/SystemLog", url,address, port);
    }

}
