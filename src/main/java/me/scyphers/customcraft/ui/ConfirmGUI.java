package me.scyphers.customcraft.ui;

import me.scyphers.customcraft.CustomCraft;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class ConfirmGUI extends MenuGUI {

    public ConfirmGUI(@NotNull CustomCraft plugin, @NotNull Player player, UUID intendedViewer, @NotNull String name) {
        super(plugin, player, intendedViewer, name, 27);
    }

    @Override
    public void draw() {
        super.draw();
        setItem(11, createConfirmOption());
        setItem(15, createCancelOption());
    }

    public abstract void onConfirm();

    public abstract ItemStack createConfirmOption();

    public abstract ItemStack createCancelOption();

    public boolean closeOnConfirm() {
        return false;
    }

    @Override
    public GUI<?> onClick(int slot, ClickType type) {

        if (slot == 11) {
            onConfirm();
            if (closeOnConfirm()) {
                this.setShouldClose(true);
                return new StaticGUI(this);
            }
            return getPreviousGUI();
        }

        if (slot == 15) {
            return getPreviousGUI();
        }

        return this;
    }

}
