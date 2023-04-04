package me.scyphers.customcraft.ui;

import org.jetbrains.annotations.NotNull;

public final class StaticGUI extends InventoryGUI {

    private final InventoryGUI gui;

    public StaticGUI(@NotNull InventoryGUI gui) {
        super(gui.getSession(), gui.getName(), gui.getSize());
        this.gui = gui;
    }

    @Override
    public void draw() {
        gui.draw();
    }

}
