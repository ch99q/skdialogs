package com.ch99q.skdialogs.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.parser.ParserInstance;
import com.ch99q.skdialogs.Dialogs;
import com.ch99q.skdialogs.model.DialogSpec;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Base for the effects that configure a dialog from inside a {@code create dialog} section. It
 * rejects use outside such a section at parse time, and hands the dialog being assembled to
 * {@link #apply} at runtime.
 */
abstract class DialogEffect extends Effect {

    /** Call from {@code init} to fail parsing when the effect is written outside a dialog section. */
    protected static boolean insideDialogSection() {
        if (!ParserInstance.get().isCurrentSection(SecDialog.class)) {
            Skript.error("This can only be used inside a 'create dialog' section.");
            return false;
        }
        return true;
    }

    @Override
    protected final void execute(Event event) {
        DialogSpec spec = Dialogs.current();
        if (spec != null) {
            apply(spec, event);
        }
    }

    /** Applies this effect to the dialog currently being assembled. */
    protected abstract void apply(DialogSpec spec, Event event);

    /** The value of the optional expression at {@code i}, or {@code null} when absent or unset. */
    protected static @Nullable Object single(Expression<?>[] parts, int i, Event event) {
        if (parts.length <= i || parts[i] == null) {
            return null;
        }
        return parts[i].getSingle(event);
    }

    protected static @Nullable String string(Expression<?>[] parts, int i, Event event) {
        Object v = single(parts, i, event);
        return v == null ? null : (String) v;
    }

    protected static @Nullable Number number(Expression<?>[] parts, int i, Event event) {
        Object v = single(parts, i, event);
        return v == null ? null : (Number) v;
    }

    protected static @Nullable Integer intOf(Expression<?>[] parts, int i, Event event) {
        Number n = number(parts, i, event);
        return n == null ? null : n.intValue();
    }

    protected static @Nullable Boolean bool(Expression<?>[] parts, int i, Event event) {
        Object v = single(parts, i, event);
        return v == null ? null : (Boolean) v;
    }
}
