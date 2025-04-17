package org.desp.merge.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import java.util.HashMap;
import java.util.Map;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.desp.merge.dto.MergeItemInfo;

public class LogSystemRepository {

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    public static MongoCollection<Document> logDataDB = null;
    public static Map<String, Boolean> playerCheckCache = new HashMap<>();

    public LogSystemRepository() {
        LogSystemDBConfig connector = new LogSystemDBConfig();
        String path = connector.getMongoConnectionContent();
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString(path))
                .build();

        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase("MergeLog");
        logDataDB = database.getCollection("MergeLog");
    }

    public static void isPlayerFirstMerge(Player player) {
        String uuid = player.getUniqueId().toString();

        Document document = logDataDB.find(Filters.eq("uuid", uuid)).first();
        if (document == null) {
            playerCheckCache.put(uuid, true);
        }
    }
}
