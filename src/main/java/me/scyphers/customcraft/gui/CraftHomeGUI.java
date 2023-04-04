package me.scyphers.customcraft.gui;

import me.scyphers.customcraft.CustomCraft;
import me.scyphers.customcraft.ui.GUI;
import me.scyphers.customcraft.ui.ItemBuilder;
import me.scyphers.customcraft.ui.MenuGUI;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CraftHomeGUI extends MenuGUI {

    private final NamespacedKey key;

    public CraftHomeGUI(@NotNull CustomCraft plugin, @NotNull Player player, UUID intendedViewer, NamespacedKey key) {
        super(plugin, player, intendedViewer, "<dark_purple>Custom Crafting</dark_purple>", 27);
        this.key = key;
    }

    @Override
    public void draw() {
        super.draw();
        setItem(13, new ItemBuilder(Material.CRAFTING_TABLE).name("<gold>Add Crafting Recipe</gold>").build());
    }

    @Override
    public GUI<?> onClick(int slot, ClickType type) {
        return slot == 13 ? new AddCraftingRecipeGUI(getPlugin(), getPlayer(), getViewer(), key) : this;
    }

    @Override
    public GUI<?> getPreviousGUI() {
        return this;
    }
}
