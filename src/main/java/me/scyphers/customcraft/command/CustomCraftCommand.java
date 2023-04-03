package me.scyphers.customcraft.command;

import me.scyphers.customcraft.CustomCraft;
import me.scyphers.customcraft.gui.CraftHomeGUI;
import me.scyphers.customcraft.ui.GUI;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class CustomCraftCommand extends PluginCommand {

    private final CustomCraft plugin;

    public CustomCraftCommand(CustomCraft plugin, PluginCommand parent, String name) {
        super(plugin, parent, name);
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        if (!(sender instanceof Player player)) {
            plugin.getMessenger().chat(sender, "error.mustBePlayer");
            return;
        }

        String recipeKey = args[0].toLowerCase(Locale.ROOT);
        NamespacedKey key = plugin.createKey(recipeKey);
        if (plugin.getServer().getRecipe(key) != null) {
            plugin.getMessenger().chat(sender, "error.recipeAlreadyExists", "%key%", recipeKey);
        }

        GUI<?> gui = new CraftHomeGUI(plugin, player, player.getUniqueId(), key);
        gui.open();
    }

    @Override
    public void noArgCommand(CommandSender sender) {
        plugin.getMessenger().chat(sender, "help.customCraftCommand");
    }
}
