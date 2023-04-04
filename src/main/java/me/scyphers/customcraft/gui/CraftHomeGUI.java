package me.scyphers.customcraft.gui;

import me.scyphers.customcraft.ui.InventoryGUI;
import me.scyphers.customcraft.ui.MenuGUI;
import me.scyphers.customcraft.ui.Session;
import me.scyphers.customcraft.ui.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;

public class CraftHomeGUI extends MenuGUI {

    private final NamespacedKey key;

    public CraftHomeGUI(@NotNull Session session, @NotNull NamespacedKey key) {
        super(session, "<dark_purple>Custom Crafting</dark_purple>", 27);
        this.key = key;
    }

    @Override
    public void draw() {
        super.draw();
        setItem(13, new ItemBuilder(Material.CRAFTING_TABLE).name("<gold>Add Crafting Recipe</gold>").build());
    }

    @Override
    public InventoryGUI onClick(int slot, ClickType type, InventoryAction action) {
        return slot == 13 ? new AddCraftingRecipeGUI(getSession(), key) : this;
    }

    @Override
    public InventoryGUI getPreviousGUI() {
        return this;
    }
}
