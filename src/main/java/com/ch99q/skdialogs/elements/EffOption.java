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
import com.ch99q.skdialogs.paper.Text;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Dialog Property")
@Description({
        "Sets a property of the dialog being created: the title, the number of button columns, " +
        "whether escape closes it, or what the dialog does after a button is clicked.",
        "'keep open' leaves the dialog on screen so a player can press a button several times; " +
        "'wait for response' greys it out until the server replies."
})
@Examples({
        "set title to \"Settings\"",
        "set columns to 2",
        "set close on escape to true",
        "set after click to keep open"
})
@Since("1.0.0")
public class EffOption extends DialogEffect {

    private static final int TITLE = 0, COLUMNS = 1, ESCAPE = 2, AFTER = 3, EXTERNAL = 4;

    static {
        Skript.registerEffect(EffOption.class,
                "set [the] title [of [the] dialog] to %string%",
                "set [the] columns [of [the] dialog] to %number%",
                "set (closing|close) on escape [of [the] dialog] to %boolean%",
                "set [the] after[ ]click [behaviour|behavior] [of [the] dialog] to (1¦close|2¦keep open|3¦wait[ for response])",
                "set [the] external title [of [the] dialog] to %string%");
    }

    private int property;
    private @Nullable Expression<?> value;
    private String afterAction;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!insideDialogSection()) {
            return false;
        }
        property = matchedPattern;
        if (property == AFTER) {
            afterAction = switch (parseResult.mark) {
                case 2 -> "NONE";
                case 3 -> "WAIT_FOR_RESPONSE";
                default -> "CLOSE";
            };
        } else {
            value = expressions[0];
        }
        return true;
    }

    @Override
    protected void apply(DialogSpec spec, Event event) {
        if (property == AFTER) {
            spec.afterAction = afterAction;
            return;
        }
        Object raw = value.getSingle(event);
        if (raw == null) {
            return;
        }
        switch (property) {
            case TITLE -> spec.title = Text.component((String) raw);
            case COLUMNS -> spec.columns = ((Number) raw).intValue();
            case ESCAPE -> spec.closeOnEscape = (Boolean) raw;
            case EXTERNAL -> spec.externalTitle = Text.component((String) raw);
            default -> { }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "set dialog option";
    }
}
