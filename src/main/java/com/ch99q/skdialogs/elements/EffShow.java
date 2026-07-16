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
import com.ch99q.skdialogs.Dialogs;
import com.ch99q.skdialogs.model.DialogSpec;
import com.ch99q.skdialogs.paper.PaperDialogs;
import io.papermc.paper.dialog.Dialog;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Show Dialog")
@Description("Shows a previously created dialog to one or more players.")
@Examples({
        "show dialog \"welcome\" to player",
        "show dialog \"announcement\" to all players"
})
@Since("1.0.0")
public class EffShow extends Effect {

    static {
        Skript.registerEffect(EffShow.class, "(show|open) dialog %string% (to|for) %players%");
    }

    private Expression<String> id;
    private Expression<Player> players;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        id = (Expression<String>) expressions[0];
        players = (Expression<Player>) expressions[1];
        return true;
    }

    @Override
    protected void execute(Event event) {
        String name = id.getSingle(event);
        if (name == null) {
            return;
        }
        DialogSpec spec = Dialogs.get(name);
        if (spec == null) {
            Bukkit.getLogger().warning("[SkDialogs] no dialog named '" + name + "' has been created.");
            return;
        }
        try {
            Dialog dialog = PaperDialogs.build(spec);
            for (Player player : players.getArray(event)) {
                PaperDialogs.show(player, dialog);
            }
        } catch (RuntimeException e) {
            Bukkit.getLogger().warning("[SkDialogs] failed to show dialog '" + name + "': " + e.getMessage());
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "show dialog " + id.toString(event, debug);
    }
}
