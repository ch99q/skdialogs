package com.ch99q.skdialogs.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.ch99q.skdialogs.paper.PaperDialogs;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Dialog Input Value")
@Description({
        "Inside a dialog click event: the value a player submitted for one input, by key. The " +
        "result is a text, a boolean, or a number depending on the input's type."
})
@Examples("set {_volume} to dialog input \"volume\"")
@Since("1.0.0")
public class ExprInputValue extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprInputValue.class, Object.class, ExpressionType.SIMPLE,
                "[the] [dialog] input [value] [(of|for)] %string%",
                "[the] [dialog] value of [input] %string%");
    }

    private Expression<String> key;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        key = (Expression<String>) expressions[0];
        return true;
    }

    @Override
    protected Object[] get(Event event) {
        if (!(event instanceof PlayerCustomClickEvent customClick)) {
            return new Object[0];
        }
        String name = key.getSingle(event);
        if (name == null) {
            return new Object[0];
        }
        Object value = PaperDialogs.inputOf(customClick, name);
        return value == null ? new Object[0] : new Object[] { value };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<Object> getReturnType() {
        return Object.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "dialog input " + key.toString(event, debug);
    }
}
