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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Dialog Body")
@Description({
        "Adds a line of content to the dialog being created: a plain message, or a displayed item.",
        "An item body can carry a description next to the item, hide its hover tooltip or its " +
        "decorations (the count and durability bar), and set the box the item renders in."
})
@Examples({
        "add body \"Pick a starter kit:\"",
        "add body \"Some long lore text.\" with width 300",
        "add item body player's tool",
        "add item body 12 bamboo with description \"A bunch of bamboo\" without decorations sized 24 by 24"
})
@Since("1.0.0")
public class EffBody extends DialogEffect {

    private static final int MESSAGE = 0, ITEM = 1;
    private static final int NO_TOOLTIP = 1, NO_DECORATIONS = 2;

    static {
        Skript.registerEffect(EffBody.class,
                "add [a] [plain] body %string% [with width %-number%]",
                "add [an] item body %itemstack% [with description %-string%]"
                        + " [(1¦without [the] tooltip)] [(2¦without [the] decorations)]"
                        + " [sized %-number% by %-number%]");
    }

    private int kind;
    private int marks;
    private Expression<?>[] parts;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!insideDialogSection()) {
            return false;
        }
        kind = matchedPattern;
        marks = parseResult.mark;
        parts = expressions;
        return true;
    }

    @Override
    protected void apply(DialogSpec spec, Event event) {
        Object raw = parts[0].getSingle(event);
        if (raw == null) {
            return;
        }
        if (kind == ITEM) {
            DialogSpec.Body body = DialogSpec.Body.item((ItemStack) raw);
            String description = string(1, event);
            if (description != null) {
                body.description = Text.component(description);
            }
            if ((marks & NO_TOOLTIP) != 0) {
                body.showTooltip = false;
            }
            if ((marks & NO_DECORATIONS) != 0) {
                body.showDecorations = false;
            }
            body.width = intOf(2, event);
            body.height = intOf(3, event);
            spec.bodies.add(body);
        } else {
            DialogSpec.Body body = DialogSpec.Body.message(Text.component((String) raw));
            body.width = intOf(1, event);
            spec.bodies.add(body);
        }
    }

    private @Nullable String string(int i, Event event) {
        if (parts.length <= i || parts[i] == null) {
            return null;
        }
        return (String) parts[i].getSingle(event);
    }

    private @Nullable Integer intOf(int i, Event event) {
        if (parts.length <= i || parts[i] == null) {
            return null;
        }
        Number n = (Number) parts[i].getSingle(event);
        return n == null ? null : n.intValue();
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "add dialog body";
    }
}
