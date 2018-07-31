package xyz.vincanote.plugin.inventorysort;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;

final class InventorySort extends JavaPlugin {
    static final String PLUGIN_NAME = "InventorySort";
    private InventorySortListener sortListener;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("sort") && args.length == 0) {
            if(sender instanceof Player) {
                Player player = (Player)sender;
                PlayerInventory inventory =  player.getInventory();

                LinkedList<ItemStack> slotItems = Arrays.stream(inventory.getStorageContents()).limit(9).collect(Collectors.toCollection(LinkedList::new));
                LinkedList<ItemStack> items = Arrays.stream(inventory.getStorageContents()).skip(9).collect(Collectors.toCollection(LinkedList::new));

                sortListener.sortInventory(items);

                slotItems.addAll(items);
                inventory.setStorageContents(slotItems.toArray(new ItemStack[0]));
                sender.sendMessage("[" + InventorySort.PLUGIN_NAME + "] " + player.getDisplayName() +  "のインベントリをソートしました。");
                return true;
            } else {
                sender.sendMessage("Console unsupported.");
                return false;
            }
        }
        return false;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();
        sortListener = new InventorySortListener(this);

        getLogger().info("Chest sort plugin was enabled.");

        loadConfig();
        getLogger().info("Config load complete.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Chest sort plugin was disabled.");
    }

    private void loadConfig(){
        try {
            for (String key : getConfig().getStringList("SortOrder")) {
                Material material = Material.getMaterial(key.trim());
                if( material == null) {
                    getLogger().info("Not found material. SortOrder key: " + key.trim());
                    continue;
                }

                MaterialComparator.addOrder(material);
            }
        } catch(Exception ex)  {
            getLogger().severe("Exception :" + ex.getMessage());

        }
    }
}
