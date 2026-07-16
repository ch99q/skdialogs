package com.ch99q.skdialogs;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.registrations.EventValues;
import com.ch99q.skdialogs.paper.PaperDialogs;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

/**
 * Plugin entry point: registers the addon's syntax with Skript, teaches Skript how to read the
 * submitting player out of a dialog click event, and closes a dialog when its exit button is pressed.
 */
public final class SkDialogs extends JavaPlugin {

    @Override
    public void onEnable() {
        SkriptAddon addon = Skript.registerAddon(this);
        try {
            addon.loadClasses("com.ch99q.skdialogs", "elements");
        } catch (IOException e) {
            getLogger().severe("Failed to load dialog syntax: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        registerPlayerValue();
        registerClickListener();
    }

    private void registerPlayerValue() {
        EventValues.registerEventValue(PlayerCustomClickEvent.class, Player.class,
                PaperDialogs::playerOf, EventValues.TIME_NOW);
    }

    /** Listens for dialog clicks so navigation buttons open their target and kept-open exits close. */
    private void registerClickListener() {
        Listener listener = new Listener() { };
        getServer().getPluginManager().registerEvent(
                PlayerCustomClickEvent.class, listener, EventPriority.MONITOR,
                (ignored, event) -> PaperDialogs.onClick((PlayerCustomClickEvent) event), this);
    }
}
