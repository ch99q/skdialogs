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
import com.ch99q.skdialogs.model.DialogSpec.Button;
import com.ch99q.skdialogs.paper.Text;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Dialog Button")
@Description({
        "Adds a button to the dialog being created.",
        "Without an action clause the button fires 'on dialog click' and needs an id to match there. " +
        "With one (that runs/opens/copies/suggests/shows) it acts on its own and the id is optional. " +
        "'that runs' fills $(key) placeholders from the dialog's inputs at click time.",
        "The exit variant adds the single back button shown on multi-action and list dialogs."
})
@Examples({
        "add button \"confirm\" labeled \"Confirm\" with tooltip \"Save and close\"",
        "add button labeled \"Spawn\" that runs \"/spawn\"",
        "add button labeled \"Rename\" that runs \"/nick $(name)\"",
        "add button labeled \"Wiki\" that opens \"https://docs.papermc.io\"",
        "add button labeled \"Back\" that shows \"hub\"",
        "add exit button \"back\" labeled \"Back\""
})
@Since("1.0.0")
public class EffButton extends DialogEffect {

    private static final int EXIT_PATTERN = 1;
    private static final int RUN = 1, OPEN = 2, COPY = 3, SUGGEST = 4, SHOW = 5;

    static {
        String tail = "[%-string%] [labeled %-string%] [with tooltip %-string%] [with width %-number%]"
                + " [that (1¦runs [command]|2¦opens [[the] url]|3¦copies|4¦suggests [command]|5¦shows [dialog]) %-string%]";
        Skript.registerEffect(EffButton.class,
                "add [a] button " + tail,
                "add [a[n]] exit button " + tail);
    }

    private boolean exit;
    private int actionKind;
    private @Nullable Expression<String> id;
    private @Nullable Expression<String> label;
    private @Nullable Expression<String> tooltip;
    private @Nullable Expression<Number> width;
    private @Nullable Expression<String> actionValue;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!insideDialogSection()) {
            return false;
        }
        exit = matchedPattern == EXIT_PATTERN;
        actionKind = parseResult.mark;
        id = (Expression<String>) expressions[0];
        label = (Expression<String>) expressions[1];
        tooltip = (Expression<String>) expressions[2];
        width = (Expression<Number>) expressions[3];
        actionValue = (Expression<String>) expressions[4];

        if (actionKind == 0 && id == null) {
            Skript.error("An event button needs an id to match in 'on dialog click'. "
                    + "Give it an id, or an action like 'that runs \"/spawn\"'.");
            return false;
        }
        if (actionKind != 0 && actionValue == null) {
            Skript.error("This button action needs a value, e.g. 'that runs \"/spawn\"'.");
            return false;
        }
        return true;
    }

    @Override
    protected void apply(DialogSpec spec, Event event) {
        Button button = new Button(id != null ? id.getSingle(event) : null);
        String labelText = label != null ? label.getSingle(event) : null;
        if (labelText != null) {
            button.label = Text.component(labelText);
        }
        String tooltipText = tooltip != null ? tooltip.getSingle(event) : null;
        if (tooltipText != null) {
            button.tooltip = Text.component(tooltipText);
        }
        Number widthValue = width != null ? width.getSingle(event) : null;
        if (widthValue != null) {
            button.width = widthValue.intValue();
        }
        if (actionKind != 0) {
            button.action = switch (actionKind) {
                case OPEN -> Button.Action.URL;
                case COPY -> Button.Action.COPY;
                case SUGGEST -> Button.Action.SUGGEST;
                case SHOW -> Button.Action.SHOW;
                default -> Button.Action.COMMAND;
            };
            button.actionValue = actionValue.getSingle(event);
        }

        if (exit) {
            if (spec.exit != null) {
                Bukkit.getLogger().warning("[SkDialogs] dialog '" + spec.id
                        + "' has more than one exit button; keeping the first.");
                return;
            }
            spec.exit = button;
        } else {
            spec.buttons.add(button);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return exit ? "add dialog exit button" : "add dialog button";
    }
}
