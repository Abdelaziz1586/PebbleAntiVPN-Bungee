package me.pebbleprojects.pebbleantivpn.filters;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.log.ConciseFormatter;

import java.util.logging.Logger;

public class FilterManager {

    public FilterManager() {
        final ProxyServer proxyServer = ProxyServer.getInstance();
        if (!proxyServer.getVersion().contains("BungeeCord")) {
            new Log4J(this);
            return;
        }
        final Logger logger = proxyServer.getLogger();
        logger.setFilter(record -> logMessage((new ConciseFormatter(false)).formatMessage(record).trim()));
    }

    public boolean logMessage(final String message) {
        if(message.contains("Event ConnectionInitEvent(remoteAddress=")) return false;
        return !message.contains(" <-> InitialHandler");
    }

}

