package me.scyphers.customcraft.newui;

import me.scyphers.customcraft.CustomCraft;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.Executor;

public class Session implements Listener {

    private final Executor executor;

    private final CustomCraft plugin;
    private final Player player;
    private final UUID viewer;

    private InventoryGUI gui;

    public Session(CustomCraft plugin, Player player, UUID viewer) {
        this.plugin = plugin;
        this.player = player;
        this.viewer = viewer;

        this.executor = runnable -> plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    public Session with(InventoryGUI gui) {
        this.gui = gui;
        return this;
    }

    public void start() {
        if (this.gui == null) throw new IllegalArgumentException("Cannot start an empty session");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gui.open();
    }

    public void end() {
        executor.execute(() -> {
            System.out.println("Closing GUI");
            gui.close();
            unregister();
        });
    }

    private void unregister() {
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
        InventoryDragEvent.getHandlerList().unregister(this);
        System.out.println("All events unregistered");
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {

        InventoryGUI old = this.gui;

        System.out.println("Event called");

        if (this.gui != event.getView().getTopInventory().getHolder()) return;

        System.out.println("is correct GUI");

        this.gui = gui.onClick(event);

        // Closing is evaluated first to ensure the GUI is closed instead of opening the new GUI
        // If multiple clicks occur in a tick while closing a GUI,
        // the returned GUI from each click is evaluated before the GUI is closed
        if (old.shouldClose()) {
            System.out.println("GUI is closing");
            gui.setClose(false);
            this.end();
            return;
        }

        if (old != gui) {
            System.out.println("New GUI");
            if (old.getSize() != gui.getSize()) {
                executor.execute(() -> gui.open());
            } else {
                gui.draw();
                executor.execute(() -> gui.update());
            }
            return;
        }

        if (old.shouldUpdate()) {
            System.out.println("GUI is updating");
            gui.setUpdate(false);
            executor.execute(() -> gui.update());
            return;
        }

        System.out.println("GUI is doing nothing");

    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        if (this.gui != event.getView().getTopInventory().getHolder()) return;
        gui.onClose(event);
        this.end();
    }

    @EventHandler
    public void onInventoryDragEvent(InventoryDragEvent event) {

    }

    public void chat(String key, String... replacements) {
        plugin.getMessenger().chat(player, key, replacements);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getViewer() {
        return viewer;
    }
}
