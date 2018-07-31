package xyz.vincanote.plugin.inventorysort;
import org.bukkit.Material;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

class MaterialComparator implements Comparator<Material> {
    private static Map<Material, Integer> sortOrder = new HashMap<>();

    @Override
    public int compare(Material item1, Material item2) {
        if((item1 == null && item2 == null) || (!sortOrder.containsKey(item1) && !sortOrder.containsKey(item2))) return 0;
        if(item1 == null || !sortOrder.containsKey(item1)) return -1;
        if(item2 == null || !sortOrder.containsKey(item2)) return 1;

        int o1 = sortOrder.get(item1);
        int o2 = sortOrder.get(item2);

        if(o1 != o2) {
            return o2 - o1;
        }

        return 0;
    }

    static void addOrder(Material material) {
        if(!sortOrder.containsKey(material)){
            sortOrder.put(material, sortOrder.size() + 1);
        }
    }

    public static void removeOrder(Material material) {
        sortOrder.remove(material);
    }

    public static void clear() {
        sortOrder.clear();
    }
}
