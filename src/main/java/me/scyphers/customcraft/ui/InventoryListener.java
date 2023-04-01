package me.scyphers.customcraft.ui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

public record InventoryListener(Plugin plugin) implements Listener {

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        // Verify if the inventory interacted with was an InventoryGUI
        // If the inventory interacted with is not a valid GUI then we do not handle this event
        if (!(event.getView().getTopInventory().getHolder() instanceof InventoryGUI oldGUI)) return;

        // Only allow the attached plugin to listen to this click event
        if (!oldGUI.getPlugin().getName().equals(plugin.getName())) return;

        // Check if the inventory allows player inventory edits, and if so, cancel the interaction
        if (!oldGUI.allowPlayerInventoryEdits() && event.getClickedInventory() instanceof PlayerInventory) {
            event.setCancelled(true);
            return;
        }

        // Handle the interact event and open the new inventory
        GUI<?> newGUI = oldGUI.handleInteraction(event);

        // If the GUI should close after handling the interaction
        if (oldGUI.shouldClose()) {
            plugin.getServer().getScheduler().runTask(plugin, () -> event.getWhoClicked().closeInventory());
            return;
        }

        // If the oldGUI is identical to the new GUI, then don't reopen the GUI, just redraw
        // the == comparison is relevant as the GUI will be identical if it returned 'this' from the interaction
        if (oldGUI == newGUI && event.getWhoClicked() instanceof Player player) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                oldGUI.draw();
                player.updateInventory();
            });
            return;
        }

        if (newGUI != null) {
            plugin.getServer().getScheduler().runTask(plugin, () -> newGUI.open());
        }


    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {

        // Verify if the inventory interacted with was an InventoryGUI
        // If the inventory interacted with is not a valid GUI then we do not handle this event
        if (!(event.getView().getTopInventory().getHolder() instanceof InventoryGUI gui)) return;

        // Only allow the attached plugin to listen to this click event
        if (!gui.getPlugin().getName().equals(plugin.getName())) return;

        // Only fire the close event if the GUI has been flagged to close
        gui.onClose(event);

    }

}
