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
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import com.ch99q.skdialogs.Dialogs;
import com.ch99q.skdialogs.model.DialogSpec;
import com.ch99q.skdialogs.model.DialogSpec.Input;
import com.ch99q.skdialogs.paper.Text;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Name("Dropdown Input Section")
@Description({
        "Adds a dropdown input whose choices are listed as 'option' lines in the section body, so " +
        "each choice can show a label distinct from the id you read back in the click event.",
        "For the simple case where the id is also the label, use the flat 'with options' form of " +
        "'add dropdown input' instead."
})
@Examples({
        "add dropdown input \"kit\" labeled \"Kit\":",
        "\toption \"warrior\" labeled \"The Warrior\" as default",
        "\toption \"mage\" labeled \"Battle Mage\""
})
@Since("1.0.0")
public class SecDropdown extends InlineSection {

    static {
        Skript.registerSection(SecDropdown.class, "add (dropdown|choice) input %string% [labeled %-string%]");
    }

    private Expression<String> key;
    private @Nullable Expression<String> label;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed,
                        ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        if (!ParserInstance.get().isCurrentSection(SecDialog.class)) {
            Skript.error("A dropdown section can only be used inside a 'create dialog' section.");
            return false;
        }
        key = (Expression<String>) expressions[0];
        label = (Expression<String>) expressions[1];
        loadCode(sectionNode);
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(Event event) {
        String name = key.getSingle(event);
        DialogSpec dialog = Dialogs.current();
        if (name != null && dialog != null) {
            Input input = new Input(Input.Kind.DROPDOWN, name);
            String labelText = label != null ? label.getSingle(event) : null;
            if (labelText != null) {
                input.label = Text.component(labelText);
            }
            Dialogs.beginInput(input);
            try {
                runBody(event);
            } finally {
                Dialogs.endInput();
            }
            dialog.inputs.add(input);
        }
        return getNext();
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "add dropdown input " + key.toString(event, debug);
    }
}
