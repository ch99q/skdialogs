package com.ch99q.skdialogs.elements;

import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.TriggerItem;
import org.bukkit.event.Event;

/**
 * Base for sections that run their body once, on the spot, from {@code walk}. Skript links a
 * section's last item to the statement after the section, so walking the body directly would flow
 * past it; {@link #runBody} unlinks the tail first and relinks it afterwards.
 */
abstract class InlineSection extends Section {

    protected final void runBody(Event event) {
        if (first == null) {
            return;
        }
        TriggerItem after = getNext();
        if (last != null) {
            last.setNext(null);
        }
        TriggerItem.walk(first, event);
        if (last != null) {
            last.setNext(after);
        }
    }
}
