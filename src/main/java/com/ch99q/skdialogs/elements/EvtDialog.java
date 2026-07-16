package com.ch99q.skdialogs.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ch99q.skdialogs.paper.PaperDialogs;
import com.ch99q.skdialogs.paper.PaperDialogs.Click;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EvtDialog extends SkriptEvent {

    static {
        Skript.registerEvent("Dialog Click", EvtDialog.class, PlayerCustomClickEvent.class,
                        "dialog (click|submit|button click)",
                        "dialog (click|submit) [(of|for)] %string%")
                .description("Fires when a player presses a button on a dialog this addon created. "
                                + "An optional id restricts the event to one dialog.",
                        "Inside it, 'event-player' is the player, 'the clicked button' is the pressed "
                                + "button's id, and 'dialog input \"key\"' reads a submitted input value.")
                .examples("on dialog click \"welcome\":",
                        "\tif the clicked button is \"warrior\":",
                        "\t\tsend \"You chose the warrior kit!\" to event-player")
                .since("1.0.0");
    }

    private @Nullable Literal<String> dialogId;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        if (args.length > 0) {
            dialogId = (Literal<String>) args[0];
        }
        return true;
    }

    @Override
    public boolean check(Event event) {
        if (!(event instanceof PlayerCustomClickEvent customClick)) {
            return false;
        }
        Click click = PaperDialogs.clickOf(customClick);
        if (click == null) {
            return false;
        }
        if (dialogId == null) {
            return true;
        }
        String wanted = dialogId.getSingle();
        return wanted == null || wanted.equals(click.dialogId());
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "on dialog click" + (dialogId != null ? " of " + dialogId.toString(event, debug) : "");
    }
}
