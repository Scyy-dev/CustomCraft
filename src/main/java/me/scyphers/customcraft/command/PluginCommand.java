package me.scyphers.customcraft.command;

import me.scyphers.customcraft.CustomCraft;
import me.scyphers.customcraft.config.Messenger;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class PluginCommand implements TabExecutor {

    private final String name;
    private final String node;
    protected final Messenger messenger;
    private final String permission;
    private final Map<String, PluginCommand> commands = new HashMap<>();

    public PluginCommand(CustomCraft plugin, PluginCommand parent, String name) {
        String base = name.toLowerCase(Locale.ROOT);
        this.messenger = plugin.getMessenger();
        this.name = base;
        this.node = (parent != null ? parent.node + "." : "") + base;
        this.permission = "fruitprison.commands." + node;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command ignored, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(permission)) {
            messenger.chat(sender, "errorMessages.noPermission");
            return true;
        }

        if (args.length == 0) {
            noArgCommand(sender);
            return true;
        }

        if (commands.size() > 0) {
            String commandName = args[0].toLowerCase(Locale.ROOT);
            if (!commands.containsKey(commandName)) {
                messenger.chat(sender, "errorMessages.unknownCommand", "%command%", this.node + "." + args[0]);
                return true;
            }

            PluginCommand command = commands.get(commandName);
            String[] newArgs = subArgs(args);
            return command.onCommand(sender, ignored, label, newArgs);
        }

        onCommand(sender, args);
        return true;

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command ignored, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(permission)) return Collections.emptyList();

        if (commands.size() != 0) {

            if (args.length == 1) return commands.values().stream()
                    .filter(pluginCommand -> sender.hasPermission(pluginCommand.permission))
                    .map(pluginCommand -> pluginCommand.name)
                    .filter(command -> command.contains(args[0].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());

            String commandName = args[0].toLowerCase(Locale.ROOT);
            if (!commands.containsKey(commandName)) return Collections.emptyList();
            PluginCommand command = commands.get(commandName);
            String[] newArgs = subArgs(args);

            return command.onTabComplete(sender, ignored, label, newArgs);
        }

        List<String> options = onTabComplete(sender, args);
        return options.stream()
                .filter(option -> option.toLowerCase(Locale.ROOT).contains(args[args.length - 1].toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());

    }

    public void onCommand(CommandSender sender, String[] args) {}

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    public void noArgCommand(CommandSender sender) {

        if (this.commands.size() == 0) {
            this.sendHelpMessage(sender);
            return;
        }

        messenger.chat(sender, "helpMessages.spacer.initial", "%command%", name);
        for (String commandName : commands.keySet()) {
            commands.get(commandName).sendHelpMessage(sender);
        }
    }

    public void addCommand(PluginCommand pluginCommand) {
        String command = pluginCommand.name;
        if (commands.containsKey(command)) throw new IllegalArgumentException("Command already exists");
        this.commands.put(command, pluginCommand);
    }

    public void sendHelpMessage(CommandSender sender) {
        messenger.chat(sender, "helpMessages.commands." + node + ".master");
    }

    public static String[] subArgs(String[] args) {
        return args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
    }

    public static boolean validPlayer(OfflinePlayer player) {
        return player == null || !player.isOnline() || player.hasPlayedBefore();
    }

    public static List<String> onlinePlayers(Server server) {
        return server.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    public Messenger getMessenger() {
        return messenger;
    }

    public String getPermission() {
        return permission;
    }

}
