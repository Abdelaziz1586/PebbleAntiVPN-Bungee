package me.pebbleprojects.pebbleantivpn.listeners;

import me.pebbleprojects.pebbleantivpn.engine.IP;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PostLogin implements Listener {

    @EventHandler
    public void onPostLogin(final PostLoginEvent event) {
        new Thread(() -> {
            final ProxiedPlayer player = event.getPlayer();
            new IP(player.getSocketAddress().toString().split(":")[0].split("/")[1], player);
        }).start();
    }

}
