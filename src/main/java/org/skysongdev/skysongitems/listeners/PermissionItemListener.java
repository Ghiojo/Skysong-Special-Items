package org.skysongdev.skysongitems.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.utils.PermissionItems;

import java.util.Map;
import org.skysongdev.skysongitems.utils.ItemKeys;

public class PermissionItemListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent event){
        if(isItemAPermissionItem(event.getItemDrop().getItemStack())){
            String permission = event.getItemDrop().getItemStack().getItemMeta().getPersistentDataContainer().get(ItemKeys.PERMISSION, PersistentDataType.STRING);
            PermissionItems.revoke(event.getPlayer(), permission);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemPickup(EntityPickupItemEvent event){
        if(event.getEntity() instanceof Player player){
            if(isItemAPermissionItem(event.getItem().getItemStack())){
                String permission = event.getItem().getItemStack().getItemMeta().getPersistentDataContainer().get(ItemKeys.PERMISSION, PersistentDataType.STRING);
                PermissionItems.grant(player, permission);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onItemConsumption(PlayerItemConsumeEvent event){
        if(event.getItem().hasItemMeta() && event.getItem().getItemMeta().getPersistentDataContainer().has(ItemKeys.PERMISSION)){
            String permission = event.getItem().getItemMeta().getPersistentDataContainer().get(ItemKeys.PERMISSION, PersistentDataType.STRING);
            PermissionItems.revoke(event.getPlayer(), permission);
        }
    }

    private boolean willInventoryAcceptItem(ItemStack item, Inventory inventory){
        Map<Integer, ItemStack> leftover = inventory.addItem(item.clone());
        return leftover.isEmpty();
    }

    private boolean isItemAPermissionItem(ItemStack item){
        return item != null && item.getType() != Material.AIR && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(ItemKeys.PERMISSION);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemMove(InventoryClickEvent event){
        Player player;
        if(!(event.getWhoClicked() instanceof Player))
            return;
        player = (Player) event.getWhoClicked();

        if(event.getView().getTopInventory() == player.getInventory())
            return;

        if(event.getAction() == InventoryAction.HOTBAR_SWAP){
            ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
            if(event.getClickedInventory() == player.getInventory())
                return;
            if(isItemAPermissionItem(hotbarItem)){
                ItemMeta meta = hotbarItem.getItemMeta();
                PermissionItems.revoke(player, meta.getPersistentDataContainer().get(ItemKeys.PERMISSION, PersistentDataType.STRING));
            }
            if(isItemAPermissionItem(event.getCurrentItem())){
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                PermissionItems.grant(player, meta.getPersistentDataContainer().get(ItemKeys.PERMISSION, PersistentDataType.STRING));
            }
        }

        if(event.isShiftClick()){
            ItemMeta meta;
            if(isItemAPermissionItem(event.getCurrentItem())){
                meta = event.getCurrentItem().getItemMeta();
                if(event.getClickedInventory() == player.getInventory()) {
                    if (willInventoryAcceptItem(event.getCurrentItem(), event.getView().getTopInventory()))
                        PermissionItems.revoke(player, meta.getPersistentDataContainer().get(ItemKeys.PERMISSION, PersistentDataType.STRING));
                }
                else {
                    if(willInventoryAcceptItem(event.getCurrentItem(), player.getInventory()))
                        PermissionItems.grant(player, meta.getPersistentDataContainer().get(ItemKeys.PERMISSION, PersistentDataType.STRING));
                }
            }
        } else {
            if(event.getClickedInventory() != player.getInventory())
                return;
            ItemMeta meta;
            if(isItemAPermissionItem(event.getCurrentItem())){
                meta = event.getCurrentItem().getItemMeta();
                PermissionItems.revoke(player, meta.getPersistentDataContainer().get(ItemKeys.PERMISSION, PersistentDataType.STRING));
            }
            if(isItemAPermissionItem(event.getCursor())){
                meta = event.getCursor().getItemMeta();
                PermissionItems.grant(player, meta.getPersistentDataContainer().get(ItemKeys.PERMISSION, PersistentDataType.STRING));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDrag(InventoryDragEvent event){
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if(!isItemAPermissionItem(event.getCursor()))
            return;

        InventoryView view = event.getView();
        Inventory top = view.getTopInventory();
        int topSize = top.getSize();

        for (int slotIndex : event.getNewItems().keySet()) {
            if (slotIndex >= topSize) {
                PermissionItems.grant(player, event.getNewItems().get(slotIndex).getItemMeta().getPersistentDataContainer().get(ItemKeys.PERMISSION, PersistentDataType.STRING));
            }
        }
    }


}
