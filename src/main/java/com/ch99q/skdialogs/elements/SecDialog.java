package com.ch99q.skdialogs.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import com.ch99q.skdialogs.Dialogs;
import com.ch99q.skdialogs.model.DialogSpec;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Name("Create Dialog")
@Description({
        "Defines a dialog and registers it under an id.",
        "The section runs its body once, on the spot: the effects inside it (title, bodies, inputs, " +
        "buttons) configure the new dialog. The finished dialog stays registered under its id, so " +
        "'show dialog' can display it any number of times."
})
@Examples({
        "create dialog \"welcome\":",
        "\tset title to \"Welcome!\"",
        "\tadd body \"Pick a kit:\"",
        "\tadd button \"warrior\" labeled \"Warrior\"",
        "\tadd button \"mage\" labeled \"Mage\"",
        "show dialog \"welcome\" to player"
})
@Since("1.0.0")
public class SecDialog extends InlineSection {

    static {
        Skript.registerSection(SecDialog.class, "(create|make) dialog [(named|with id)] %string%");
    }

    private Expression<String> id;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed,
                        ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        id = (Expression<String>) expressions[0];
        loadCode(sectionNode);
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(Event event) {
        String name = id.getSingle(event);
        if (name != null) {
            Dialogs.begin(new DialogSpec(name));
            try {
                runBody(event);
            } finally {
                Dialogs.end();
            }
        }
        return getNext();
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "create dialog " + id.toString(event, debug);
    }
}
