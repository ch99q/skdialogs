package com.ch99q.skdialogs;

import com.ch99q.skdialogs.model.DialogSpec;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime state shared across the dialog syntax: the dialog currently being assembled by a running
 * {@code create dialog} section, and every finished dialog kept by id for later showing.
 *
 * <p>The section runs its body synchronously on the server thread, so a thread-local stack is
 * enough to hand the in-progress dialog to the effects inside it without threading an id through
 * every line. A stack rather than a single slot lets one dialog section nest inside another.
 */
public final class Dialogs {
    private Dialogs() {}

    private static final ThreadLocal<Deque<DialogSpec>> BUILDING = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Deque<DialogSpec.Input>> INPUT = ThreadLocal.withInitial(ArrayDeque::new);
    private static final Map<String, DialogSpec> SPECS = new ConcurrentHashMap<>();

    /** Begins assembling a dialog, making it the target of the enclosing section's effects. */
    public static void begin(DialogSpec spec) {
        BUILDING.get().push(spec);
        SPECS.put(spec.id, spec);
    }

    /** Finishes assembling the innermost dialog. */
    public static void end() {
        Deque<DialogSpec> stack = BUILDING.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
    }

    /** The dialog being assembled right now, or {@code null} outside any dialog section. */
    public static DialogSpec current() {
        return BUILDING.get().peek();
    }

    /** Begins assembling one input, making it the target of the enclosing input section's options. */
    public static void beginInput(DialogSpec.Input input) {
        INPUT.get().push(input);
    }

    /** Finishes assembling the innermost input. */
    public static void endInput() {
        Deque<DialogSpec.Input> stack = INPUT.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
    }

    /** The input being assembled right now, or {@code null} outside any input section. */
    public static DialogSpec.Input currentInput() {
        return INPUT.get().peek();
    }

    /** The finished dialog registered under {@code id}, or {@code null} if none was created. */
    public static DialogSpec get(String id) {
        return SPECS.get(id);
    }
}
