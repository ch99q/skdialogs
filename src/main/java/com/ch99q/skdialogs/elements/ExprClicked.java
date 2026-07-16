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
import com.ch99q.skdialogs.paper.PaperDialogs.Click;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Clicked Button / Dialog Id")
@Description("Inside a dialog click event: the id of the button that was pressed, or the id of the dialog itself.")
@Examples({
        "if the clicked button is \"confirm\":",
        "if the clicked dialog id is \"settings\":"
})
@Since("1.0.0")
public class ExprClicked extends SimpleExpression<String> {

    private static final int BUTTON = 0, DIALOG = 1;

    static {
        Skript.registerExpression(ExprClicked.class, String.class, ExpressionType.SIMPLE,
                "[the] clicked [dialog] button",
                "[the] [clicked] dialog id");
    }

    private int what;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        what = matchedPattern;
        return true;
    }

    @Override
    protected String[] get(Event event) {
        if (!(event instanceof PlayerCustomClickEvent customClick)) {
            return new String[0];
        }
        Click click = PaperDialogs.clickOf(customClick);
        if (click == null) {
            return new String[0];
        }
        return new String[] { what == DIALOG ? click.dialogId() : click.buttonId() };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return what == DIALOG ? "clicked dialog id" : "clicked dialog button";
    }
}
