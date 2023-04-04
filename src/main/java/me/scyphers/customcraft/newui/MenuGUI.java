package me.scyphers.customcraft.newui;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.jetbrains.annotations.NotNull;

public abstract class MenuGUI extends InventoryGUI {

    private final int backSlot, exitSlot;

    public MenuGUI(Session session, @NotNull String name, int size) {
        super(session, name, size);

        this.backSlot = size - 9;
        this.exitSlot = size - 1;
    }

    public abstract InventoryGUI onClick(int slot, ClickType type, InventoryAction action);

    public abstract InventoryGUI getPreviousGUI();

    @Override
    public void draw() {
        this.fill(BACKGROUND);
        setItem(backSlot, new ItemBuilder(Material.COMPASS).name("<gold>Back</gold>").build());
        setItem(exitSlot, new ItemBuilder(Material.IRON_DOOR).name("<red>Exit</red>").build());
    }

    @Override
    public InventoryGUI onClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        event.setCancelled(true);

        if (slot == backSlot) return getPreviousGUI();
        if (slot == exitSlot) {
            setClose(true);
            return new StaticGUI(this);
        }

        return onClick(slot, event.getClick(), event.getAction());

    }

    @Override
    public final InventoryGUI onDrag(InventoryDragEvent event) {
        return super.onDrag(event);
    }
}
