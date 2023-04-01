package me.scyphers.customcraft.ui;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This GUI system represents a template system for creating GUIs.<br>
 * Every time the player computes an action that requires a new GUI page with different handling,
 * it's recommended to produce a new GUI instance to keep interaction handling clean and concise.
 *
 * @param <T> The event responsible for handling interaction within this GUI
 */
public interface GUI<T extends Event> {

    @NotNull GUI<?> handleInteraction(T event);

    void open();

    @NotNull Plugin getPlugin();

    @NotNull Player getPlayer();

    default @NotNull UUID getViewer() {
        return getPlayer().getUniqueId();
    }

    boolean shouldClose();

	boolean drawControls();

}
