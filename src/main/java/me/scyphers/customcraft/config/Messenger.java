package me.scyphers.customcraft.config;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Nameable;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Set;

/**
 * Interface for sending any sort of text to an audience based on customisable text that can be replaced.
 */
public interface Messenger {

    /**
     * Gets a Component
     * @param key unique identifier for the component
     * @return the {@link Component} built from the key and replacements
     */
    default Component get(@NotNull String key) {
        return this.get(key, new String[0]);
    }

    /**
     * Gets a Component
     * @param key unique identifier for the component
     * @param replacements regex-replacement pair for the message
     * @return the {@link Component} built from the key and replacements
     */
    Component get(@NotNull String key, @NotNull String... replacements);

    default String getRaw(@NotNull String key) {
        return this.getRaw(key, new String[0]);
    }

    String getRaw(@NotNull String key, @NotNull String... replacements);

    /**
     * Checks if this messenger has a message for a given key
     * @param key the key to check
     * @return if this messenger has a message for the given key
     */
    boolean has(String key);

    /**
     * Gets a read-only set of registered keys for this messenger
     * @return the list of registered keys for this messenger
     */
    Set<String> getKeys();

    /**
     * Sends a chat message to the given audience, using a replacement pair
     * @param audience audience to send the chat message to
     * @param key unique identifier for the message to send
     */
    default void chat(@NotNull Audience audience, @NotNull  String key) {
        this.chat(audience, key, new String[0]);
    }

    /**
     * Sends a chat message to the given audience, using a replacement pair
     * @param audience audience to send the chat message to
     * @param key unique identifier for the message to send
     * @param replacements regex-replacement pairs for the message
     */
    void chat(@NotNull Audience audience, @NotNull  String key, @NotNull  String... replacements);

    default void actionBar(@NotNull Audience audience, @NotNull String key) {
        this.actionBar(audience, key, new String[0]);
    }

    void actionBar(@NotNull Audience audience, @NotNull String key, @NotNull String... replacements);

    default void bossBar(@NotNull Audience audience, @NotNull String key, float progress, @NotNull BossBar.Color color, @NotNull BossBar.Overlay overlay) {
        this.bossBar(audience, key, progress, color, overlay, new String[0]);
    }

    void bossBar(@NotNull Audience audience, @NotNull String key, float progress, @NotNull BossBar.Color color, @NotNull BossBar.Overlay overlay, @NotNull String... replacements);

    default void title(@NotNull Audience audience, @NotNull String titleKey, @NotNull String subtitleKey, @NotNull Duration fadeIn, @NotNull Duration stay, @NotNull Duration fadeOut) {
        this.title(audience, titleKey, subtitleKey, fadeIn, fadeOut, stay, new String[0]);
    }

    void title(@NotNull Audience audience, @NotNull String titleKey, @NotNull String subtitleKey, @NotNull Duration fadeIn, @NotNull Duration stay, @NotNull Duration fadeOut, @NotNull String... replacements);

    default void name(@NotNull Nameable nameable, @NotNull String key) {
        this.name(nameable, key, new String[0]);
    }

    void name(@NotNull Nameable nameable, @NotNull String key, @NotNull String... replacements);

	default void title(@NotNull CommandSender sender, String string) {
		this.title(sender, string, "", Duration.ofMillis(500), Duration.ofMillis(2500), Duration.ofMillis(1000));
	}

}
