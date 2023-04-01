package me.scyphers.customcraft;

import me.scyphers.customcraft.config.Messenger;
import me.scyphers.customcraft.config.MessengerFile;
import me.scyphers.customcraft.ui.InventoryListener;
import org.bukkit.plugin.java.JavaPlugin;

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
    }

    public Messenger getMessenger() {
        return messenger;
    }

}
