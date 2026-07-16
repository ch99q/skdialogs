package com.ch99q.skdialogs.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.ch99q.skdialogs.model.DialogSpec;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Dialog List Entry")
@Description({
        "Adds another dialog, by id, as an entry in this dialog's menu.",
        "A dialog with any list entries is shown as a list: each entry is a button that opens that dialog."
})
@Examples({
        "create dialog \"menu\":",
        "\tset title to \"Main menu\"",
        "\tadd dialog \"welcome\" to the list",
        "\tadd dialog \"settings\" to the list",
        "\tadd exit button \"close\" labeled \"Close\""
})
@Since("1.0.0")
public class EffListEntry extends DialogEffect {

    static {
        Skript.registerEffect(EffListEntry.class, "add dialog %string% to [the] list");
    }

    private Expression<String> id;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!insideDialogSection()) {
            return false;
        }
        id = (Expression<String>) expressions[0];
        return true;
    }

    @Override
    protected void apply(DialogSpec spec, Event event) {
        String referenced = id.getSingle(event);
        if (referenced != null) {
            spec.listDialogs.add(referenced);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "add dialog to list";
    }
}
