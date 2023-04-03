package me.scyphers.customcraft;

import me.scyphers.customcraft.command.CustomCraftCommand;
import me.scyphers.customcraft.config.Messenger;
import me.scyphers.customcraft.config.MessengerFile;
import me.scyphers.customcraft.ui.InventoryListener;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class CustomCraft extends JavaPlugin {

    private MessengerFile messenger;

    @Override
    public void onEnable() {

        this.messenger = new MessengerFile(this);
        try {
            messenger.load();
        } catch (Exception e) {
            getSLF4JLogger().error("Unable to load custom messages!", e);
        }

        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);

        PluginCommand craftCommand = Objects.requireNonNull(this.getCommand("customcrafting"));
        CustomCraftCommand command = new CustomCraftCommand(this, null, "customcraft");
        craftCommand.setExecutor(command);
        craftCommand.setTabCompleter(command);
    }

    public Messenger getMessenger() {
        return messenger;
    }

    public NamespacedKey createKey(String key) {
        return new NamespacedKey(this, key);
    }

}
