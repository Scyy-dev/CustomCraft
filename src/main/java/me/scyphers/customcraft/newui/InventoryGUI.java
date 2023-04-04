package me.scyphers.customcraft.newui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public abstract class InventoryGUI implements InventoryHolder {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static final ItemStack BACKGROUND = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).build();

    private final Session session;

    private final String name;
    private final int size;

    private final Inventory inventory;

    private boolean update = false;
    private boolean close = false;

    public InventoryGUI(@NotNull Session session, @NotNull String name, int size) {
        this.session = session;
        this.name = name;
        this.size = size;
        this.inventory = session.getPlugin().getServer().createInventory(this, size, miniMessage.deserialize(name));
    }

    public abstract void draw();

    // Default behaviour is to block interaction
    public InventoryGUI onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        return this;
    }
    public void onClose(InventoryCloseEvent event) {

    }
    public InventoryGUI onDrag(InventoryDragEvent event) {
        event.setCancelled(true);
        return this;
    }

    public void open() {
        this.draw();
        session.getPlayer().openInventory(inventory);
    }

    public void close() {
        session.getPlayer().closeInventory();
    }

    public void update() {
        session.getPlayer().updateInventory();
    }

    public boolean shouldUpdate() {
        return update;
    }

    public boolean shouldClose() {
        return close;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public void setClose(boolean close) {
        this.close = close;
    }

    public @Nullable ItemStack getItem(int slot) {
        if (slot < 0 || slot >= size) return null;
        return inventory.getContents()[slot];
    }

    public void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    public void fill(ItemStack itemStack) {
        ItemStack[] items = inventory.getContents();
        Arrays.fill(items, itemStack);
        inventory.setContents(items);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public Session getSession() {
        return session;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

}
