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
        "A dialog with any list entries is shown as a list: each entry is a button that opens that " +
        "dialog. 'server links' instead presents the link list the server advertises."
})
@Examples({
        "create dialog \"menu\":",
        "\tset title to \"Main menu\"",
        "\tadd dialog \"welcome\" to the list",
        "\tadd dialog \"settings\" to the list",
        "\tadd exit button \"close\" labeled \"Close\"",
        "",
        "create dialog \"links\":",
        "\tadd server links to the list",
        "\tadd exit button \"back\" labeled \"Back\""
})
@Since("1.0.0, 1.1.0 (server links)")
public class EffListEntry extends DialogEffect {

    static {
        Skript.registerEffect(EffListEntry.class,
                "add dialog %string% to [the] list",
                "add [the] server links to [the] list");
    }

    private static final int SERVER_LINKS = 1;

    private boolean serverLinks;
    private @Nullable Expression<String> id;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!insideDialogSection()) {
            return false;
        }
        serverLinks = matchedPattern == SERVER_LINKS;
        if (!serverLinks) {
            id = (Expression<String>) expressions[0];
        }
        return true;
    }

    @Override
    protected void apply(DialogSpec spec, Event event) {
        if (serverLinks) {
            spec.serverLinks = true;
            return;
        }
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
