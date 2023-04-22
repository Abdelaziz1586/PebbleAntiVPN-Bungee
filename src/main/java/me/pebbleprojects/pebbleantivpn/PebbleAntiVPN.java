package me.pebbleprojects.pebbleantivpn;

import net.md_5.bungee.api.plugin.Plugin;

import me.pebbleprojects.pebbleantivpn.engine.Handler;

public final class PebbleAntiVPN extends Plugin {

    @Override
    public void onEnable() {
        new Handler(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("§cDisabled §6PebbleAntiVPN");
    }
}
