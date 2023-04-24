package me.pebbleprojects.pebbleantivpn.engine;

import me.pebbleprojects.pebbleantivpn.PebbleAntiVPN;
import me.pebbleprojects.pebbleantivpn.filters.FilterManager;
import me.pebbleprojects.pebbleantivpn.listeners.PostLogin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Handler {

    private boolean cooldown;
    private final Logger logger;
    public static Handler INSTANCE;
    private final PebbleAntiVPN main;
    private Configuration data, config;
    private final TaskScheduler scheduler;
    private final File configFile, dataFile;

    public Handler(final PebbleAntiVPN main) {
        INSTANCE = this;
        this.main = main;
        logger = main.getLogger();
        scheduler = main.getProxy().getScheduler();
        logger.info("§eLoading §6PebbleAntiVPN§e...");
        cooldown = false;
        new FilterManager();
        if (!main.getDataFolder().exists()) {
            if (main.getDataFolder().mkdir()) logger.info("§aCreated plugin folder");
        }

        configFile = new File(main.getDataFolder().getPath(), "config.yml");
        if (!configFile.exists()) {
            InputStream config = main.getResourceAsStream("config.yml");
            copyFromIDE(config, configFile);
        }

        updateConfig();
        dataFile = new File(main.getDataFolder().getPath(), "data.yml");
        if (!dataFile.exists()) {
            try {
                if (dataFile.createNewFile()) logger.info("§aCreated data.yml");
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        updateData();
        final PluginManager pm = main.getProxy().getPluginManager();
        pm.registerListener(main, new PostLogin());
        pm.registerCommand(main, new Command(this));
        logger.info("§aLoaded §6PebbleAntiVPN§a!");
    }

    public void updateConfig() {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateData() {
        try {
            data = ConfigurationProvider.getProvider(YamlConfiguration.class).load(dataFile);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeData(final String key, final Object value) {
        data.set(key, value);
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(data, dataFile);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCooldown(final int time) {
        if (!cooldown) {
            cooldown = true;
            scheduler.schedule(main, () -> cooldown = false, time, TimeUnit.SECONDS);
        }
    }

    public boolean isCooldown() {
        return cooldown;
    }

    public final Logger getLogger() {
        return logger;
    }

    public boolean whitelistIP(final String IP, final boolean add) {
        if (data.contains(IP)) {
            writeData(IP, Objects.requireNonNull(data.getString(IP)).replaceFirst("\\{", "{\"whitelisted\":" + add + ","));
            return true;
        }
        return false;
    }

    public boolean whitelistPlayer(final String playerName, final boolean add) {
        try {
            String s;
            for (final String IP : Objects.requireNonNull(data.getKeys())) {
                s = data.getString(IP);
                if (s != null) {
                    if (s.contains("\"name\":\"" + playerName)) {
                        writeData(IP, s.replaceFirst("\\{", "{\"whitelisted\":" + add + ","));
                        return true;
                    }
                }
            }
            return false;
        } catch (final Exception ignored) {
            return false;
        }
    }

    public void runAsync(final Runnable runnable) {
        scheduler.runAsync(main, runnable);
    }

    public Collection<? extends ProxiedPlayer> getPlayers() {
        return main.getProxy().getPlayers();
    }

    public Object getConfig(final String key, final boolean translate) {
        if (config.contains(key)) {
            if (translate) {
                return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString(key))).replace("%nl%", "\n");
            }
            return config.get(key);
        }
        return null;
    }

    public Configuration getConfigSection(final String key) {
        return config.contains(key) ? config.getSection(key) : null;
    }

    public List<?> getConfigList(final String key) {
        return config.contains(key) ? config.getList(key) : null;
    }

    public Object getData(final String key) {
        return data.contains(key) ? data.get(key) : null;
    }

    private void copyFromIDE(final InputStream from, final File to) {
        try {
            Files.copy(from, to.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}

