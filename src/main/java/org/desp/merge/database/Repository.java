package org.desp.merge.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.desp.merge.dto.MergeItemInfo;

public class Repository {

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    public static MongoCollection<Document> weapons = null;
    private final Map<String, MergeItemInfo> weaponRepository = new HashMap<>();

    public Repository() {
        DBConfig connector = new DBConfig();
        String path = connector.getMongoConnectionContent();
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString(path))
                .build();

        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase("Merge");
        weapons = database.getCollection("Merge");
    }

    public Map<String, MergeItemInfo> getAllWeaponData() {
        FindIterable<Document> documents = weapons.find();
        for (Document document : documents) {
            String itemName = document.getString("afterWeapon");
            if (itemName != null) {
                List<Map<String, Integer>> materialList = parseMaterials(document);
                MergeItemInfo mergeItemInfo = MergeItemInfo.create(
                        itemName,
                        document.getList("coreItem", String.class),
                        document.getInteger("successPercentage", 0),
                        document.getInteger("cost", 0),
                        materialList
                );
                weaponRepository.put(itemName, mergeItemInfo);
            }
        }
        return weaponRepository;
    }

    private List<Map<String, Integer>> parseMaterials(Document document) {
        List<Map<String, Integer>> materialList = new ArrayList<>();
        List<Document> materials = document.getList("material", Document.class);
        if (materials != null) {
            for (Document materialDoc : materials) {
                Map<String, Integer> materialMap = new HashMap<>();
                materialMap.put(materialDoc.getString("id"), materialDoc.getInteger("quantity"));
                materialList.add(materialMap);
            }
        }
        return materialList;
    }
}
