package com.ch99q.skdialogs.paper;

import com.ch99q.skdialogs.Dialogs;
import com.ch99q.skdialogs.model.DialogSpec;
import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Turns a {@link DialogSpec} into a live Paper dialog, shows it, and reads its responses. All
 * contact with Paper's experimental dialog API lives in this file, so a breaking API change
 * surfaces here as a compile error against the pinned Paper version.
 *
 * <p>A button click is routed back by a generated key rather than by embedding the (arbitrary)
 * dialog and button ids in a key, whose character set is restricted.
 */
public final class PaperDialogs {
    private PaperDialogs() {}

    /** Namespace of every click key this plugin generates, so its events are told apart from other clicks. */
    public static final String NAMESPACE = "skdialogs";

    /**
     * A resolved click: which dialog and button the player pressed, whether it exits, and the id of
     * another dialog to open (for navigation buttons), or null.
     */
    public record Click(String dialogId, String buttonId, boolean exit, String showTarget) {}

    private static final ConcurrentHashMap<String, Click> CLICKS = new ConcurrentHashMap<>();
    private static final AtomicLong COUNTER = new AtomicLong();

    /** Builds a live dialog from its description, registering fresh click keys for its buttons. */
    public static Dialog build(DialogSpec spec) {
        CLICKS.values().removeIf(c -> c.dialogId().equals(spec.id));
        DialogBase base = buildBase(spec);
        DialogType type = buildType(spec);
        return Dialog.create(factory -> factory.empty().base(base).type(type));
    }

    private static DialogBase buildBase(DialogSpec spec) {
        DialogBase.Builder builder = DialogBase.builder(spec.title);
        if (spec.externalTitle != null) {
            builder.externalTitle(spec.externalTitle);
        }
        if (!spec.bodies.isEmpty()) {
            List<DialogBody> bodies = new ArrayList<>();
            for (DialogSpec.Body b : spec.bodies) {
                bodies.add(buildBody(b));
            }
            builder.body(bodies);
        }
        if (!spec.inputs.isEmpty()) {
            List<DialogInput> inputs = new ArrayList<>();
            for (DialogSpec.Input in : spec.inputs) {
                inputs.add(buildInput(in));
            }
            builder.inputs(inputs);
        }
        if (spec.closeOnEscape != null) {
            builder.canCloseWithEscape(spec.closeOnEscape);
        }
        if (spec.afterAction != null) {
            builder.afterAction(DialogBase.DialogAfterAction.valueOf(spec.afterAction));
        }
        // Pause only affects single-player and Paper rejects a paused dialog whose after-action
        // never unpauses (e.g. "keep open"), so a server plugin is always correct to leave it off.
        builder.pause(false);
        return builder.build();
    }

    private static DialogBody buildBody(DialogSpec.Body b) {
        if (b.kind == DialogSpec.Body.Kind.ITEM) {
            var builder = DialogBody.item(b.item);
            if (b.description != null) builder.description(DialogBody.plainMessage(b.description));
            if (b.showTooltip != null) builder.showTooltip(b.showTooltip);
            if (b.showDecorations != null) builder.showDecorations(b.showDecorations);
            if (b.width != null) builder.width(b.width);
            if (b.height != null) builder.height(b.height);
            return builder.build();
        }
        if (b.width != null) {
            return DialogBody.plainMessage(b.message, b.width);
        }
        return DialogBody.plainMessage(b.message);
    }

    private static DialogInput buildInput(DialogSpec.Input in) {
        switch (in.kind) {
            case TEXT -> {
                TextDialogInput.Builder builder = DialogInput.text(in.key, in.label);
                if (in.textDefault != null) builder.initial(in.textDefault);
                if (in.maxLength != null) builder.maxLength(in.maxLength);
                if (in.width != null) builder.width(in.width);
                if (Boolean.TRUE.equals(in.multiline)) {
                    builder.multiline(TextDialogInput.MultilineOptions.create(in.maxLines, null));
                }
                return builder.build();
            }
            case BOOLEAN -> {
                var builder = DialogInput.bool(in.key, in.label);
                if (in.booleanDefault != null) builder.initial(in.booleanDefault);
                if (in.onTrue != null) builder.onTrue(in.onTrue);
                if (in.onFalse != null) builder.onFalse(in.onFalse);
                return builder.build();
            }
            case SLIDER -> {
                float min = in.min != null ? in.min : 0f;
                float max = in.max != null ? in.max : 1f;
                var builder = DialogInput.numberRange(in.key, in.label, min, max);
                if (in.step != null) builder.step(in.step);
                // Vanilla starts an unset slider at the midpoint, which can sit between steps.
                builder.initial(in.sliderDefault != null ? in.sliderDefault : min);
                if (in.format != null) builder.labelFormat(labelFormat(in.format));
                if (in.width != null) builder.width(in.width);
                return builder.build();
            }
            case DROPDOWN -> {
                List<SingleOptionDialogInput.OptionEntry> entries = new ArrayList<>();
                for (DialogSpec.Input.Option opt : in.options) {
                    Component display = opt.label != null ? opt.label : Component.text(opt.id);
                    entries.add(SingleOptionDialogInput.OptionEntry.create(opt.id, display, opt.selected));
                }
                var builder = DialogInput.singleOption(in.key, in.label, entries);
                if (in.width != null) builder.width(in.width);
                return builder.build();
            }
            default -> throw new IllegalStateException("unknown input kind: " + in.kind);
        }
    }

    /** Turns the script-facing {@code {label}}/{@code {value}} markers into vanilla's format arguments. */
    private static String labelFormat(String format) {
        return format.replace("{label}", "%1$s").replace("{value}", "%2$s");
    }

    private static DialogType buildType(DialogSpec spec) {
        ActionButton exit = spec.exit != null ? buildButton(spec.id, spec.exit, true) : null;

        if (!spec.listDialogs.isEmpty()) {
            return buildList(spec, exit);
        }
        if (spec.serverLinks) {
            return DialogType.serverLinks(exit, spec.columns != null ? spec.columns : 2, 150);
        }

        List<ActionButton> buttons = new ArrayList<>();
        for (DialogSpec.Button b : spec.buttons) {
            buttons.add(buildButton(spec.id, b, false));
        }
        if (buttons.isEmpty()) {
            return exit != null ? DialogType.notice(exit) : DialogType.notice();
        }
        if (buttons.size() == 1 && exit == null) {
            return DialogType.notice(buttons.get(0));
        }
        // One button plus an exit is vanilla's confirmation layout; an explicit column count opts
        // back into multi-action, the only type that carries columns.
        if (buttons.size() == 1 && spec.columns == null) {
            return DialogType.confirmation(buttons.get(0), exit);
        }
        var builder = DialogType.multiAction(buttons);
        if (spec.columns != null) builder.columns(spec.columns);
        if (exit != null) builder.exitAction(exit);
        return builder.build();
    }

    private static DialogType buildList(DialogSpec spec, ActionButton exit) {
        List<Dialog> dialogs = new ArrayList<>();
        for (String id : spec.listDialogs) {
            DialogSpec referenced = Dialogs.get(id);
            if (referenced != null) {
                dialogs.add(build(referenced));
            }
        }
        RegistrySet<Dialog> set = RegistrySet.valueSet(RegistryKey.DIALOG, dialogs);
        var builder = DialogType.dialogList(set);
        if (spec.columns != null) builder.columns(spec.columns);
        if (exit != null) builder.exitAction(exit);
        return builder.build();
    }

    private static ActionButton buildButton(String dialogId, DialogSpec.Button b, boolean exit) {
        ActionButton.Builder builder = ActionButton.builder(b.label);
        if (b.tooltip != null) builder.tooltip(b.tooltip);
        if (b.width != null) builder.width(b.width);
        builder.action(actionFor(dialogId, b, exit));
        return builder.build();
    }

    private static DialogAction actionFor(String dialogId, DialogSpec.Button b, boolean exit) {
        return switch (b.action) {
            case COMMAND -> commandAction(b.actionValue);
            case URL -> DialogAction.staticAction(ClickEvent.openUrl(b.actionValue));
            case COPY -> DialogAction.staticAction(ClickEvent.copyToClipboard(b.actionValue));
            case SUGGEST -> DialogAction.staticAction(ClickEvent.suggestCommand(b.actionValue));
            case SHOW -> customClick(dialogId, b.id, exit, b.actionValue);
            default -> customClick(dialogId, b.id, exit, null);
        };
    }

    /** {@code commandTemplate} substitutes {@code $(input)} macros but rejects a command without one. */
    private static DialogAction commandAction(String command) {
        if (command.contains("$(")) {
            return DialogAction.commandTemplate(command.startsWith("/") ? command.substring(1) : command);
        }
        return DialogAction.staticAction(ClickEvent.runCommand(command.startsWith("/") ? command : "/" + command));
    }

    private static DialogAction customClick(String dialogId, String buttonId, boolean exit, String showTarget) {
        String value = "c" + COUNTER.incrementAndGet();
        CLICKS.put(NAMESPACE + ":" + value, new Click(dialogId, buttonId, exit, showTarget));
        return DialogAction.customClick(Key.key(NAMESPACE, value), null);
    }

    public static void show(Player player, Dialog dialog) {
        player.showDialog(dialog);
    }

    /** Closes whatever dialog the player currently has open, leaving any prior screen intact. */
    public static void close(Player player) {
        player.closeDialog();
    }

    /**
     * Handles the addon's own reaction to a dialog click: opening a navigation button's target, and
     * closing a kept-open dialog's exit button. A normal exit button is left alone, because every
     * after-click behaviour other than "keep open" already closes natively, and a second close here
     * would pop one screen too many, e.g. dropping the dialog list a sub-dialog was opened from.
     */
    public static void onClick(PlayerCustomClickEvent event) {
        Click click = clickOf(event);
        if (click == null) {
            return;
        }
        Player player = playerOf(event);
        if (player == null) {
            return;
        }
        if (click.showTarget() != null) {
            DialogSpec target = Dialogs.get(click.showTarget());
            if (target != null) {
                show(player, build(target));
            }
            return;
        }
        if (click.exit()) {
            DialogSpec spec = Dialogs.get(click.dialogId());
            if (spec != null && DialogBase.DialogAfterAction.NONE.name().equals(spec.afterAction)) {
                close(player);
            }
        }
    }

    /** Resolves the dialog and button behind a click event, or {@code null} if it was not one of ours. */
    public static Click clickOf(PlayerCustomClickEvent event) {
        return CLICKS.get(event.getIdentifier().asString());
    }

    /** The player who submitted the dialog, or {@code null} if they were not in the game phase. */
    public static Player playerOf(PlayerCustomClickEvent event) {
        if (event.getCommonConnection() instanceof PlayerGameConnection game) {
            return game.getPlayer();
        }
        return null;
    }

    /** Reads one input's submitted value by key, returning a String, Boolean, or Float, or {@code null}. */
    public static Object inputOf(PlayerCustomClickEvent event, String key) {
        DialogResponseView view = event.getDialogResponseView();
        if (view == null) {
            return null;
        }
        String text = view.getText(key);
        if (text != null) return text;
        Boolean bool = view.getBoolean(key);
        if (bool != null) return bool;
        return view.getFloat(key);
    }
}
