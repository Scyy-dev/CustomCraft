package me.scyphers.customcraft.ui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public final class StaticGUI extends InventoryGUI {

    private final InventoryGUI lastGUI;

    public StaticGUI(InventoryGUI gui) {
        super(gui.getPlugin(), gui.getPlayer(), gui.getViewer(), gui.getName(), gui.getSize());
        this.lastGUI = gui;
    }

    @Override
    public @NotNull GUI<?> handleInteraction(InventoryClickEvent event) {
        event.setCancelled(true);
        return this;
    }

    @Override
    public void draw() {
        this.getInventory().setContents(lastGUI.getInventory().getContents());
    }

    @Override
    public boolean allowPlayerInventoryEdits() {
        return false;
    }
}
