package me.pebbleprojects.pebbleantivpn.engine;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;

public class IP {

    private ProxiedPlayer player;
    private String IP, json, time, alertQuery;

    public IP(final String IP, final ProxiedPlayer player) {
        if (Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.bypass-permission.enabled", false).toString()) && player.hasPermission(Handler.INSTANCE.getConfig("block-IP-system.bypass-permission.permission", false).toString())) {
            return;
        }
        this.IP = IP;
        this.player = player;
        final String dataIP = IP.replace(".", "_");
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern(Handler.INSTANCE.getConfig("IP-blockage-alert.date-time-formatter-pattern", false).toString());
        final LocalDateTime now = LocalDateTime.now();
        time = dtf.format(now);

        // Checking if IP has already been checked before.
        final Object o = Handler.INSTANCE.getData(dataIP);
        if (o != null) {
            json = o.toString();
            getBlockedMessageReady(false);
            return;
        }

        if (!Handler.INSTANCE.isCooldown()) {
            try {
                InputStream inputStream;
                final URL url = new URL("http://ip-api.com/json/" + IP + "?fields=continent,continentCode,country,countryCode,region,regionName,city,district,zip,lat,lon,timezone,offset,currency,isp,org,as,asname,mobile,proxy,hosting");
                final HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setConnectTimeout(1000);
                http.setReadTimeout(1000);
                http.setRequestProperty("Accept", "application/json");
                final int responseCode = http.getResponseCode();
                if (200 <= responseCode && responseCode <= 299) {
                    inputStream = http.getInputStream();
                } else {
                    inputStream = http.getErrorStream();
                }

                if (Integer.parseInt(http.getHeaderField("X-Rl")) == 0) {
                    Handler.INSTANCE.setCooldown(Integer.parseInt(http.getHeaderField("X-Ttl")));
                    player.getPendingConnection().disconnect(new TextComponent(Handler.INSTANCE.getConfig("block-IP-system.rate-limit-message", true).toString()));
                    return;
                }

                final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                final StringBuilder response = new StringBuilder();
                String currentLine;
                while ((currentLine = in.readLine()) != null)
                    response.append(currentLine);
                in.close();

                json = response.toString().replaceFirst("\\{", "{\"name\":\"" + player.getName() + "\",");
                Handler.INSTANCE.writeData(dataIP, json);
                getBlockedMessageReady(true);
            } catch (final Exception ignored) {
            }
            return;
        }
        player.getPendingConnection().disconnect(new TextComponent(Handler.INSTANCE.getConfig("block-IP-system.rate-limit-message", true).toString()));
    }

    private void getBlockedMessageReady(final boolean isNew) {
        String s;
        boolean b;
        alertQuery = null;

        String blockMessage = null;
        if (!player.hasPermission(Handler.INSTANCE.getConfig("block-IP-system.proxy.bypass-permission", false).toString())) {
            b = Boolean.parseBoolean(getJSON("proxy"));
            if (b && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.proxy.enabled", false).toString())) {
                alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.proxy.alert-query", true).toString());
                blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.proxy.block-message", true).toString());
            }
        }

        if (!player.hasPermission(Handler.INSTANCE.getConfig("block-IP-system.mobile.bypass-permission", false).toString())) {
            b = Boolean.parseBoolean(getJSON("mobile"));
            if (blockMessage == null && b && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.mobile.enabled", false).toString())) {
                alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.mobile.alert-query", true).toString());
                blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.mobile.block-message", true).toString());
            }
        }

        if (!player.hasPermission(Handler.INSTANCE.getConfig("block-IP-system.hosting.bypass-permission", false).toString())) {
            b = Boolean.parseBoolean(getJSON("hosting"));
            if (blockMessage == null && b && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.hosting.enabled", false).toString())) {
                alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.hosting.alert-query", true).toString());
                blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.hosting.block-message", true).toString());
            }
        }

        if (!player.hasPermission(Handler.INSTANCE.getConfig("block-IP-system.country.bypass-permission", false).toString())) {
            s = getJSON("country");
            if (blockMessage == null && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.country.enabled", false).toString())) {
                if (Handler.INSTANCE.getConfigList("block-IP-system.country.names").contains(s)) {
                    alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.country.alert-query", true).toString());
                    blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.country.block-message", true).toString());
                }
            }

            s = getJSON("countryCode");
            if (blockMessage == null && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.country.enabled", false).toString())) {
                if (Handler.INSTANCE.getConfigList("block-IP-system.country.codes").contains(s)) {
                    alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.country.alert-query", true).toString());
                    blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.country.block-message", true).toString());
                }
            }
        }

        if (!player.hasPermission(Handler.INSTANCE.getConfig("block-IP-system.continent.bypass-permission", false).toString())) {
            s = getJSON("continent");
            if (blockMessage == null && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.continent.enabled", false).toString())) {
                if (Handler.INSTANCE.getConfigList("block-IP-system.continent.names").contains(s)) {
                    alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.continent.alert-query", true).toString());
                    blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.continent.block-message", true).toString());
                }
            }

            s = getJSON("continentCode");
            if (blockMessage == null && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.continent.enabled", false).toString())) {
                if (Handler.INSTANCE.getConfigList("block-IP-system.continent.codes").contains(s)) {
                    alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.continent.alert-query", true).toString());
                    blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.continent.block-message", true).toString());
                }
            }
        }

        if (!player.hasPermission(Handler.INSTANCE.getConfig("block-IP-system.region.bypass-permission", false).toString())) {
            s = getJSON("regionName");
            if (blockMessage == null && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.region.enabled", false).toString())) {
                if (Handler.INSTANCE.getConfigList("block-IP-system.region.names").contains(s)) {
                    alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.region.alert-query", true).toString());
                    blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.region.block-message", true).toString());
                }
            }

            s = getJSON("region");
            if (blockMessage == null && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.region.enabled", false).toString())) {
                if (Handler.INSTANCE.getConfigList("block-IP-system.region.codes").contains(s)) {
                    alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.region.alert-query", true).toString());
                    blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.region.block-message", true).toString());
                }
            }
        }

        if (!player.hasPermission(Handler.INSTANCE.getConfig("block-IP-system.city.bypass-permission", false).toString())) {
            s = getJSON("city");
            if (blockMessage == null && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.city.enabled", false).toString())) {
                if (Handler.INSTANCE.getConfigList("block-IP-system.city.names").contains(s)) {
                    alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.city.alert-query", true).toString());
                    blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.city.block-message", true).toString());
                }
            }
        }

        if (!player.hasPermission(Handler.INSTANCE.getConfig("block-IP-system.district.bypass-permission", false).toString())) {
            s = getJSON("district");
            if (blockMessage == null && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.district.enabled", false).toString())) {
                if (Handler.INSTANCE.getConfigList("block-IP-system.district.names").contains(s)) {
                    alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.district.alert-query", true).toString());
                    blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.district.block-message", true).toString());
                }
            }
        }

        if (!player.hasPermission(Handler.INSTANCE.getConfig("block-IP-system.zip.bypass-permission", false).toString())) {
            s = getJSON("zip");
            if (blockMessage == null && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.zip.enabled", false).toString())) {
                if (Handler.INSTANCE.getConfigList("block-IP-system.zip.codes").contains(s)) {
                    alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.zip.alert-query", true).toString());
                    blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.zip.block-message", true).toString());
                }
            }
        }

        if (!player.hasPermission(Handler.INSTANCE.getConfig("block-IP-system.timezone.bypass-permission", false).toString())) {
            s = getJSON("timezone");
            if (blockMessage == null && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.timezone.enabled", false).toString())) {
                if (Handler.INSTANCE.getConfigList("block-IP-system.timezone.timezones").contains(s)) {
                    alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.timezone.alert-query", true).toString());
                    blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.timezone.block-message", true).toString());
                }
            }
        }

        if (!player.hasPermission(Handler.INSTANCE.getConfig("block-IP-system.currency.bypass-permission", false).toString())) {
            s = getJSON("currency");
            if (blockMessage == null && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.currency.enabled", false).toString())) {
                if (Handler.INSTANCE.getConfigList("block-IP-system.currency.currencies").contains(s)) {
                    alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.currency.alert-query", true).toString());
                    blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.currency.block-message", true).toString());
                }
            }
        }

        if (!player.hasPermission(Handler.INSTANCE.getConfig("block-IP-system.ISP.bypass-permission", false).toString())) {
            s = getJSON("isp");
            if (blockMessage == null && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.ISP.enabled", false).toString())) {
                if (Handler.INSTANCE.getConfigList("block-IP-system.ISP.names").contains(s)) {
                    alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.ISP.alert-query", true).toString());
                    blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.ISP.block-message", true).toString());
                }
            }
        }

        if (!player.hasPermission(Handler.INSTANCE.getConfig("block-IP-system.ORG.bypass-permission", false).toString())) {
            s = getJSON("org");
            if (blockMessage == null && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.ORG.enabled", false).toString())) {
                if (Handler.INSTANCE.getConfigList("block-IP-system.ORG.names").contains(s)) {
                    alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.ORG.alert-query", true).toString());
                    blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.ORG.block-message", true).toString());
                }
            }
        }

        if (!player.hasPermission(Handler.INSTANCE.getConfig("block-IP-system.as.bypass-permission", false).toString())) {
            s = getJSON("as");
            if (blockMessage == null && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.as.enabled", false).toString())) {
                if (Handler.INSTANCE.getConfigList("block-IP-system.as.numbers").contains(s)) {
                    alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.as.alert-query", true).toString());
                    blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.as.block-message", true).toString());
                }
            }
            s = getJSON("asname");
            if (blockMessage == null && Boolean.parseBoolean(Handler.INSTANCE.getConfig("block-IP-system.as.enabled", false).toString())) {
                if (Handler.INSTANCE.getConfigList("block-IP-system.as.names").contains(s)) {
                    alertQuery = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.as.alert-query", true).toString());
                    blockMessage = translateVariables(Handler.INSTANCE.getConfig("block-IP-system.as.block-message", true).toString());
                }
            }
        }

        if (blockMessage != null) {

            final String finalBlockMessage = blockMessage;
            Handler.INSTANCE.runAsync(() -> player.getPendingConnection().disconnect(new TextComponent(finalBlockMessage)));
            if (isNew) {
                // Console
                if (Boolean.parseBoolean(Handler.INSTANCE.getConfig("IP-blockage-alert.console.enabled", false).toString())) {
                    Handler.INSTANCE.getLogger().info(translateVariables(Handler.INSTANCE.getConfig("IP-blockage-alert.console.message", true).toString()));
                }

                // Players
                if (Boolean.parseBoolean(Handler.INSTANCE.getConfig("IP-blockage-alert.players.enabled", false).toString())) {
                    for (final ProxiedPlayer p : Handler.INSTANCE.getPlayers()) {
                        if (p.hasPermission(Handler.INSTANCE.getConfig("IP-blockage-alert.players.permission", false).toString()))
                            p.sendMessage(new TextComponent(translateVariables(Handler.INSTANCE.getConfig("IP-blockage-alert.players.message", true).toString())));
                    }
                }

                // Discord Webhook
                if (Boolean.parseBoolean(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.enabled", false).toString())) {
                    final Webhook webhook = new Webhook(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.webhook-url", false).toString());
                    final boolean text = Boolean.parseBoolean(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.text-message.enabled", false).toString());
                    final boolean embed = Boolean.parseBoolean(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.enabled", false).toString());
                    if (!text && !embed)
                        return;

                    if (text) {
                        webhook.setContent(translateVariables(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.text-message.content", false).toString()));
                    }
                    if (embed) {
                        final Webhook.EmbedObject embedObject = new Webhook.EmbedObject();

                        if (Boolean.parseBoolean(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.title.enabled", false).toString())) {
                            embedObject.setTitle(translateVariables(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.title.text", false).toString()));
                        }

                        if (Boolean.parseBoolean(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.description.enabled", false).toString())) {
                            embedObject.setDescription(translateVariables(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.description.text", false).toString()));
                        }

                        if (Boolean.parseBoolean(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.color.random", false).toString())) {
                            embedObject.setColor(getRandomColor());
                        } else {
                            final int R = Integer.parseInt(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.color.rgb-colors.R", false).toString());
                            final int G = Integer.parseInt(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.color.rgb-colors.G", false).toString());
                            final int B = Integer.parseInt(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.color.rgb-colors.B", false).toString());
                            if ((R > 255 || R < 0) || (G > 255 || G < 0) || (B > 255 || B < 0)) {
                                embedObject.setColor(new Color(0, 0, 0));
                                Handler.INSTANCE.getLogger().warning("§cThe max value of each of the colors is 255 and the minimum value is 0");
                            } else {
                                embedObject.setColor(new Color(R, G, B));
                            }
                        }

                        if (Boolean.parseBoolean(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.fields.enabled", false).toString())) {
                            String value;
                            boolean inline;
                            for (final String field : Handler.INSTANCE.getConfigSection("IP-blockage-alert.discord-webhook.embed.fields.embed-fields").getKeys()) {
                                s = translateVariables(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.fields.embed-fields." + field + ".name", false).toString());
                                value = translateVariables(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.fields.embed-fields." + field + ".value", false).toString());
                                inline = (boolean) Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.fields.embed-fields." + field + ".inline", false);
                                embedObject.addField(s, value, inline);
                            }
                        }

                        if (Boolean.parseBoolean(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.footer.enabled", false).toString())) {
                            s = null;
                            if (Boolean.parseBoolean(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.footer.icon.enabled", false).toString())) {
                                s = translateVariables(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.footer.icon.url", false).toString());
                            }
                            embedObject.setFooter(translateVariables(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.footer.text", false).toString()), s);
                        }

                        if (Boolean.parseBoolean(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.thumbnail.enabled", false).toString())) {
                            embedObject.setThumbnail(translateVariables(Handler.INSTANCE.getConfig("IP-blockage-alert.discord-webhook.embed.thumbnail.thumbnail-url", false).toString()));
                        }
                        webhook.addEmbed(embedObject);
                    }

                    try {
                        webhook.execute();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private Color getRandomColor() {
        final Random random = new Random();
        return new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }

    private String getJSON(final String key) {
        try {
            final String s = json.split("\"" + key + "\":")[1].replace("\"", "");
            if (!s.contains(","))
                return s.replace("}", "");
            return s.split(",")[0];
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String translateVariables(String query) {
        query = query.replace("%ip%", IP)
                .replace("%time%", time)
                .replace("%player%", player.getName())
                .replace("%proxy%", Objects.requireNonNull(getJSON("proxy")))
                .replace("%mobile%", Objects.requireNonNull(getJSON("mobile")))
                .replace("%hosting%", Objects.requireNonNull(getJSON("hosting")))
                .replace("%country%", Objects.requireNonNull(getJSON("country")))
                .replace("%countryCode%", Objects.requireNonNull(getJSON("countryCode")))
                .replace("%continent%", Objects.requireNonNull(getJSON("continent")))
                .replace("%continentCode%", Objects.requireNonNull(getJSON("continentCode")))
                .replace("%region%", Objects.requireNonNull(getJSON("region")))
                .replace("%regionName%", Objects.requireNonNull(getJSON("regionName")))
                .replace("%city%", Objects.requireNonNull(getJSON("city")))
                .replace("%district%", Objects.requireNonNull(getJSON("district")))
                .replace("%zip%", Objects.requireNonNull(getJSON("zip")))
                .replace("%timezone%", Objects.requireNonNull(getJSON("timezone")))
                .replace("%currency%", Objects.requireNonNull(getJSON("currency")))
                .replace("%isp%", Objects.requireNonNull(getJSON("isp")))
                .replace("%org%", Objects.requireNonNull(getJSON("org")))
                .replace("%as%", Objects.requireNonNull(getJSON("asname")))
                .replace("%asNumber%", Objects.requireNonNull(getJSON("as")))
                .replace("%latitude%", Objects.requireNonNull(getJSON("lat")))
                .replace("%longitude%", Objects.requireNonNull(getJSON("lon")))
                .replace("%offset%", Objects.requireNonNull(getJSON("offset")));
        if (alertQuery != null)
            query = query.replace("%alert-query%", alertQuery);
        return query;
    }
}
