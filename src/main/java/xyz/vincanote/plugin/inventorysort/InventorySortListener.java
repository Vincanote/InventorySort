package xyz.vincanote.plugin.inventorysort;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.stream.Collectors;

class InventorySortListener implements  Listener{

    InventorySortListener(InventorySort plugin) {

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        if(event.isCancelled()) return;

        // ソート条件
        //   ・スニーク中
        //   ・右クリック
        //   ・クリックした対象がチェスト
        //   ・メインハンドに何も持っていない
        if(!event.getPlayer().isSneaking()) return;
        Entity entity = event.getRightClicked();

        if(hasInventory(entity) && isAir(event.getPlayer().getInventory().getItemInMainHand().getType())) {
            if(!event.getHand().equals(EquipmentSlot.HAND)) return;

            if(entity instanceof ChestedHorse) {
                Inventory inventory = ((ChestedHorse)entity).getInventory();
                LinkedList<ItemStack> slotItems = Arrays.stream(inventory.getContents()).limit(2).collect(Collectors.toCollection(LinkedList::new));
                LinkedList<ItemStack> items = Arrays.stream(inventory.getContents()).skip(2).collect(Collectors.toCollection(LinkedList::new));

                sortInventory(items);

                if(slotItems.addAll(items)) {
                    inventory.setContents(slotItems.toArray(new ItemStack[0]));
                    event.getPlayer().sendMessage("[" + InventorySort.PLUGIN_NAME + "] " + entity.getType().name() + "のチェストをソートしました。");
                }
            } else if(entity instanceof Minecart) {
                event.setCancelled(true);
                Inventory inventory = ((InventoryHolder)entity).getInventory();
                LinkedList<ItemStack> items = new LinkedList<>(Arrays.asList(inventory.getContents()));

                sortInventory(items);

                inventory.setContents(items.toArray(new ItemStack[0]));
                event.getPlayer().sendMessage("[" + InventorySort.PLUGIN_NAME + "] " + entity.getType().name() + "のチェストをソートしました。");
            }
        }
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if(event.isCancelled()) return;

        // ソート条件
        //   ・スニーク中
        //   ・右クリック
        //   ・クリックした対象がチェスト
        //   ・メインハンドに何も持っていない
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || !event.getPlayer().isSneaking()) return;

        Block block = event.getClickedBlock();
        if(block == null) return;

        if(hasInventory(block) && isAir(event.getPlayer().getInventory().getItemInMainHand().getType())) {
            event.setCancelled(true);
            if(!event.getHand().equals(EquipmentSlot.HAND)) return;

            BlockState state = block.getState();
            Inventory inventory = null;

            if(block.getType().equals(Material.ENDER_CHEST)) {
                inventory =event.getPlayer().getEnderChest();
            } else if(state instanceof Container) {
                inventory =  ((Container)state).getInventory();
            }

            if (inventory == null) throw new AssertionError();
            LinkedList<ItemStack> items = new LinkedList<>(Arrays.asList(inventory.getContents()));
            sortInventory(items);
            inventory.setContents(items.toArray(new ItemStack[0]));
            event.getPlayer().sendMessage("[" + InventorySort.PLUGIN_NAME + "] " + block.getType().name() +  "の中身をソートしました。");
        }
    }

    void sortInventory(LinkedList<ItemStack> items)
    {
        items.sort(Comparator.nullsLast(Comparator.comparing(ItemStack::getType, Comparator.nullsLast(new MaterialComparator().reversed())))
                .thenComparing(ItemStack::getAmount)
                .thenComparing(ItemStack::getEnchantments, Comparator.nullsLast((o1, o2) -> o2.size() - o1.size()))
                .thenComparing(ItemStack::getDurability, Comparator.reverseOrder()));

        // スタックをまとめる
        for(int i = 0; i < items.size(); ++i) {
            int nextIndex = i + 1;
            if(nextIndex < items.size()) {
                if(items.get(i) != null && items.get(nextIndex) != null) {
                    if(items.get(i).getType().equals(items.get(nextIndex).getType()) && items.get(i).getMaxStackSize() != 1) {
                        int totalAmount = items.get(i).getAmount() + items.get(nextIndex).getAmount();
                        if(totalAmount > items.get(i).getMaxStackSize()) {
                            items.get(i).setAmount(items.get(i).getMaxStackSize());
                            items.get(nextIndex).setAmount(totalAmount % items.get(i).getMaxStackSize());
                        } else {
                            items.get(i).setAmount(totalAmount);
                            items.remove(nextIndex);
                            items.add(null);
                            --i;
                        }
                    }
                } else {
                    break;
                }
            }
        }


    }

    private boolean isAir(Material material)
    {
        if(material != null){
            switch (material){
                case AIR:
                case CAVE_AIR:
                case VOID_AIR:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    private  boolean hasInventory(Entity entity)
    {
        if(entity != null) {
            if(entity instanceof ChestedHorse) {
                return ((ChestedHorse)entity).isCarryingChest();
            } else return entity instanceof Minecart && entity instanceof InventoryHolder;
        }
        return false;
    }

    private boolean hasInventory(Block block)
    {
        if(block != null) {
            return block.getState() instanceof Container || block.getType().equals(Material.ENDER_CHEST);
        }
        return false;
    }
}
