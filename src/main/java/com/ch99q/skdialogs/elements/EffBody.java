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
@Description("Adds a line of content to the dialog being created: a plain message, or a displayed item.")
@Examples({
        "add body \"Pick a starter kit:\"",
        "add item body player's tool"
})
@Since("1.0.0")
public class EffBody extends DialogEffect {

    private static final int MESSAGE = 0, ITEM = 1;

    static {
        Skript.registerEffect(EffBody.class,
                "add [a] [plain] body %string%",
                "add [an] item body %itemstack%");
    }

    private int kind;
    private Expression<?> value;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!insideDialogSection()) {
            return false;
        }
        kind = matchedPattern;
        value = expressions[0];
        return true;
    }

    @Override
    protected void apply(DialogSpec spec, Event event) {
        Object raw = value.getSingle(event);
        if (raw == null) {
            return;
        }
        if (kind == ITEM) {
            spec.bodies.add(DialogSpec.Body.item((ItemStack) raw));
        } else {
            spec.bodies.add(DialogSpec.Body.message(Text.component((String) raw)));
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "add dialog body";
    }
}
