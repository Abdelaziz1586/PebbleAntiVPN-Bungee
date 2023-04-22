package me.pebbleprojects.pebbleantivpn.engine;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class Command extends net.md_5.bungee.api.plugin.Command {

    private final Handler handler;

    public Command(final Handler handler) {
        super("pav");
        this.handler = handler;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        new Thread(() -> {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.hasPermission(handler.getConfig("command.reload.permission", false).toString())) {
                        handler.updateData();
                        handler.updateConfig();
                        sender.sendMessage(new TextComponent(handler.getConfig("command.reload.message", true).toString()));
                    }
                    return;
                }

                if (args[0].equalsIgnoreCase("whitelist")) {
                    if (sender.hasPermission(handler.getConfig("command.whitelist.permission", false).toString())) {
                        if (args.length > 2) {
                            if (args[1].equalsIgnoreCase("add")) {
                                if (handler.whitelistIP(args[2], true)) {
                                    sender.sendMessage(new TextComponent(handler.getConfig("command.whitelist.messages.IP.add", true).toString().replace("%ip%", args[2])));
                                    return;
                                }

                                if (handler.whitelistPlayer(args[2], true)) {
                                    sender.sendMessage(new TextComponent(handler.getConfig("command.whitelist.messages.playerName.add", true).toString().replace("%player%", args[2])));
                                    return;
                                }

                                sender.sendMessage(new TextComponent(handler.getConfig("command.whitelist.messages.failed", true).toString().replace("%query%", args[2])));
                                return;
                            }

                            if (args[1].equalsIgnoreCase("remove")) {
                                if (handler.whitelistIP(args[2], false)) {
                                    sender.sendMessage(new TextComponent(handler.getConfig("command.whitelist.messages.IP.remove", true).toString().replace("%ip%", args[2])));
                                    return;
                                }

                                if (handler.whitelistPlayer(args[2], false)) {
                                    sender.sendMessage(new TextComponent(handler.getConfig("command.whitelist.messages.playerName.remove", true).toString().replace("%player%", args[2])));
                                    return;
                                }

                                sender.sendMessage(new TextComponent(handler.getConfig("command.whitelist.messages.failed", true).toString().replace("%query%", args[2])));
                                return;
                            }
                        }
                        sender.sendMessage(new TextComponent(handler.getConfig("command.whitelist.no-given-query", true).toString()));
                        return;
                    }
                }
                sender.sendMessage(new TextComponent(handler.getConfig("command.incomplete-arguments", true).toString()));
            }

        }).start();
    }
}
