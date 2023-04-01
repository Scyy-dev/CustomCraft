package me.scyphers.customcraft.ui;

import me.scyphers.customcraft.CustomCraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

public abstract class InventoryGUI implements InventoryHolder, GUI<InventoryClickEvent> {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static final ItemStack BACKGROUND = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).build();

    private final CustomCraft plugin;
    private final Player player;
    private final UUID viewer;

    private final String name;
    private final int size;

    private final Inventory inventory;

    private boolean shouldClose;
    private boolean drawControls = true;

    public InventoryGUI(@NotNull CustomCraft plugin, @NotNull Player player, UUID viewer, @NotNull String name, int size) {
        this.plugin = plugin;
        this.player = player;
        this.viewer = viewer;
        this.name = name;
        this.size = size;
        this.inventory = Bukkit.createInventory(this, size, miniMessage.deserialize(name));
    }

    @Override
    public abstract @NotNull GUI<?> handleInteraction(InventoryClickEvent event);

    // left empty for GUIs that have no behaviour on closing
    // this event is only really needed for storage based GUIs
    public void onClose(InventoryCloseEvent event) {

    }

    public abstract void draw();

    @Override
    public void open() {
        this.draw();
        player.openInventory(inventory);
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
    public @NotNull CustomCraft getPlugin() {
        return plugin;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @NotNull
    @Override
    public UUID getViewer() {
        return viewer;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    @Override
    public boolean shouldClose() {
        return shouldClose;
    }

    public void setShouldClose(boolean shouldClose) {
        this.shouldClose = shouldClose;
    }
    
    @Override
    public boolean drawControls() {
        return drawControls;
    }

    public void setDrawControls(boolean drawControls) {
        this.drawControls = drawControls;
    }

    public abstract boolean allowPlayerInventoryEdits();

}
