package com.ch99q.skdialogs.paper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

/** Turns a Skript string into an Adventure component, honouring both {@code &} and section colour codes. */
public final class Text {
    private Text() {}

    @SuppressWarnings("deprecation") // translateAlternateColorCodes is legacy but the correct, stable way to honour &-codes.
    public static Component component(String text) {
        String normalised = ChatColor.translateAlternateColorCodes('&', text);
        return LegacyComponentSerializer.legacySection().deserialize(normalised);
    }
}
