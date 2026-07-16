package com.ch99q.skdialogs.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.ch99q.skdialogs.paper.PaperDialogs;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Close Dialog")
@Description("Closes whatever dialog the given players currently have open.")
@Examples("close dialog for player")
@Since("1.0.0")
public class EffClose extends Effect {

    static {
        Skript.registerEffect(EffClose.class, "close [the] [current] dialog (for|of|to) %players%");
    }

    private Expression<Player> players;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        players = (Expression<Player>) expressions[0];
        return true;
    }

    @Override
    protected void execute(Event event) {
        for (Player player : players.getArray(event)) {
            PaperDialogs.close(player);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "close dialog";
    }
}
