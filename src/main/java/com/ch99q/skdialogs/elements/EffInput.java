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
import com.ch99q.skdialogs.model.DialogSpec.Input;
import com.ch99q.skdialogs.paper.Text;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Dialog Input")
@Description({
        "Adds one input control to the dialog being created: a text field, a toggle, a slider, or " +
        "a dropdown. The string is the input's key; read its submitted value in the click event " +
        "with 'dialog input \"key\"'."
})
@Examples({
        "add text input \"name\" labeled \"Name\" with default \"Steve\" with max length 16",
        "add text input \"note\" labeled \"Note\" multiline up to 5 lines",
        "add toggle input \"pvp\" labeled \"Enable PvP?\" with default false",
        "add toggle input \"stack\" labeled \"Whole stack?\" with values \"64\" and \"1\"",
        "add slider input \"volume\" labeled \"Volume\" from 0 to 100 with step 5 with default 50",
        "add slider input \"count\" labeled \"Count\" from 1 to 64 with format \"{value} items\"",
        "add dropdown input \"kit\" labeled \"Kit\" with options \"warrior\", \"mage\" and \"archer\" with default \"warrior\""
})
@Since("1.0.0, 1.1.0 (widths, toggle values, slider formats, multiline lines)")
public class EffInput extends DialogEffect {

    private static final int TEXT = 0, BOOLEAN = 1, SLIDER = 2, DROPDOWN = 3;
    private static final int MULTILINE_MARK = 1;

    static {
        Skript.registerEffect(EffInput.class,
                "add text input %string% [labeled %-string%] [with default %-string%] [with max[imum] length %-number%]"
                        + " [with width %-number%] [(1¦multiline [up to %-number% lines])]",
                "add (boolean|toggle) input %string% [labeled %-string%] [with default %-boolean%]"
                        + " [with values %-string% and %-string%]",
                "add (slider|number) input %string% [labeled %-string%] (from|between) %number% (to|and) %number%"
                        + " [(by|with step) %-number%] [with default %-number%] [with format %-string%] [with width %-number%]",
                "add (dropdown|choice) input %string% [labeled %-string%] with options %strings% [with default %-string%]"
                        + " [with width %-number%]");
    }

    private int kind;
    private boolean multiline;
    private Expression<?>[] parts;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!insideDialogSection()) {
            return false;
        }
        kind = matchedPattern;
        parts = expressions;
        multiline = (parseResult.mark & MULTILINE_MARK) != 0;
        return true;
    }

    @Override
    protected void apply(DialogSpec spec, Event event) {
        String key = string(parts, 0, event);
        if (key == null) {
            return;
        }
        Input input = new Input(kindOf(), key);
        String label = string(parts, 1, event);
        if (label != null) {
            input.label = Text.component(label);
        }
        switch (kind) {
            case TEXT -> {
                input.textDefault = string(parts, 2, event);
                input.maxLength = intOf(parts, 3, event);
                input.width = intOf(parts, 4, event);
                input.multiline = multiline;
                input.maxLines = intOf(parts, 5, event);
            }
            case BOOLEAN -> {
                input.booleanDefault = bool(parts, 2, event);
                input.onTrue = string(parts, 3, event);
                input.onFalse = string(parts, 4, event);
            }
            case SLIDER -> {
                Number min = number(parts, 2, event);
                Number max = number(parts, 3, event);
                Number step = number(parts, 4, event);
                Number def = number(parts, 5, event);
                input.min = min != null ? min.floatValue() : 0f;
                input.max = max != null ? max.floatValue() : 1f;
                input.step = step != null ? step.floatValue() : null;
                input.sliderDefault = def != null ? def.floatValue() : null;
                input.format = string(parts, 6, event);
                input.width = intOf(parts, 7, event);
            }
            case DROPDOWN -> {
                String selected = string(parts, 3, event);
                for (String id : strings(2, event)) {
                    Input.Option option = new Input.Option(id);
                    option.selected = id.equals(selected);
                    input.options.add(option);
                }
                input.width = intOf(parts, 4, event);
            }
            default -> { }
        }
        spec.inputs.add(input);
    }

    private Input.Kind kindOf() {
        return switch (kind) {
            case BOOLEAN -> Input.Kind.BOOLEAN;
            case SLIDER -> Input.Kind.SLIDER;
            case DROPDOWN -> Input.Kind.DROPDOWN;
            default -> Input.Kind.TEXT;
        };
    }

    private List<String> strings(int i, Event event) {
        List<String> result = new ArrayList<>();
        if (parts.length > i && parts[i] != null) {
            for (Object o : parts[i].getArray(event)) {
                result.add((String) o);
            }
        }
        return result;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "add dialog input";
    }
}
