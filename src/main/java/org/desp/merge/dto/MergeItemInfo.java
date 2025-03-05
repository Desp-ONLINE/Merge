package org.desp.merge.dto;

import java.util.List;
import java.util.Map;

public class MergeItemInfo {
    private String afterWeapon;
    private List<String> coreItem;
    private int successPercentage;
    private int cost;
    private List<Map<String, Integer>> materials;

    public String getAfterWeapon() {
        return afterWeapon;
    }

    public List<String> getCoreItem() {
        return coreItem;
    }

    public int getSuccessPercentage() {
        return successPercentage;
    }

    public int getCost() {
        return cost;
    }

    public List<Map<String, Integer>> getMaterials() {
        return materials;
    }

    public static MergeItemInfo create(String afterWeapon, List<String> coreItem, int successPercentage, int cost, List<Map<String, Integer>> materials) {
        MergeItemInfo mergeItemInfo = new MergeItemInfo();
        mergeItemInfo.afterWeapon = afterWeapon;
        mergeItemInfo.coreItem = coreItem;
        mergeItemInfo.successPercentage = successPercentage;
        mergeItemInfo.cost = cost;
        mergeItemInfo.materials = materials;
        return mergeItemInfo;
    }
}
