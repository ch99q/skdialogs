package com.ch99q.skdialogs.paper;

import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;

/**
 * Turns a Skript string into an Adventure component through Skript's own chat parser, so dialog
 * text supports the same format as {@code send}: colour codes, hex, and tags like
 * {@code <translate:...>}. The route goes via Bungee components because Skript's direct JSON
 * serializer reflects into {@code java.awt.Color}, which modern JVMs forbid. Falls back to plain
 * legacy parsing if Skript's parser rejects a string.
 */
public final class Text {
    private Text() {}

    public static Component component(String text) {
        try {
            String json = ComponentSerializer.toString(BungeeConverter.convert(ChatMessages.parse(text)));
            return GsonComponentSerializer.gson().deserialize(json);
        } catch (RuntimeException e) {
            return legacy(text);
        }
    }

    @SuppressWarnings("deprecation") // translateAlternateColorCodes is legacy but the correct, stable way to honour &-codes.
    private static Component legacy(String text) {
        String normalised = ChatColor.translateAlternateColorCodes('&', text);
        return LegacyComponentSerializer.legacySection().deserialize(normalised);
    }
}
