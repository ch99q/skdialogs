package com.ch99q.skdialogs.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import com.ch99q.skdialogs.Dialogs;
import com.ch99q.skdialogs.model.DialogSpec.Input;
import com.ch99q.skdialogs.paper.Text;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Dropdown Option")
@Description({
        "One choice inside a dropdown input section: its id, an optional shown label, and whether " +
        "it starts selected."
})
@Examples("option \"warrior\" labeled \"The Warrior\" as default")
@Since("1.0.0")
public class EffOptionEntry extends Effect {

    static {
        Skript.registerEffect(EffOptionEntry.class,
                "option %string% [labeled %-string%] [(1¦[as] [the] default)]");
    }

    private Expression<String> id;
    private @Nullable Expression<String> label;
    private boolean selected;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!ParserInstance.get().isCurrentSection(SecDropdown.class)) {
            Skript.error("'option' can only be used inside a dropdown input section.");
            return false;
        }
        id = (Expression<String>) expressions[0];
        label = (Expression<String>) expressions[1];
        selected = (parseResult.mark & 1) != 0;
        return true;
    }

    @Override
    protected void execute(Event event) {
        Input input = Dialogs.currentInput();
        String value = id.getSingle(event);
        if (input == null || value == null) {
            return;
        }
        Input.Option option = new Input.Option(value);
        String labelText = label != null ? label.getSingle(event) : null;
        if (labelText != null) {
            option.label = Text.component(labelText);
        }
        option.selected = selected;
        input.options.add(option);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "dropdown option";
    }
}
