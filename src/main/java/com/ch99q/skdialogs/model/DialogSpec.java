package com.ch99q.skdialogs.model;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * A plain description of a dialog, assembled by the {@code create dialog} section and turned into
 * a live Paper dialog by {@link com.ch99q.skdialogs.paper.PaperDialogs}. It holds no Paper dialog
 * types itself, so it stays valid when that experimental API changes.
 */
public final class DialogSpec {
    public final String id;
    public Component title;
    public Integer columns;
    public Boolean closeOnEscape;
    /** Paper {@code DialogAfterAction} constant name (CLOSE, NONE, WAIT_FOR_RESPONSE), or null for the default. */
    public String afterAction;

    public final List<Body> bodies = new ArrayList<>();
    public final List<Input> inputs = new ArrayList<>();
    public final List<Button> buttons = new ArrayList<>();
    /** Ids of other dialogs to present as a menu; when non-empty this dialog is a list. */
    public final List<String> listDialogs = new ArrayList<>();
    /** The single back/cancel button, shown for multi-action and list dialogs. */
    public Button exit;

    public DialogSpec(String id) {
        this.id = id;
        this.title = Component.text(id);
    }

    /** A line of dialog content: either a message or a shown item. */
    public static final class Body {
        public enum Kind { MESSAGE, ITEM }
        public final Kind kind;
        public Component message;
        public ItemStack item;

        private Body(Kind kind) { this.kind = kind; }

        public static Body message(Component message) {
            Body b = new Body(Kind.MESSAGE);
            b.message = message;
            return b;
        }

        public static Body item(ItemStack item) {
            Body b = new Body(Kind.ITEM);
            b.item = item;
            return b;
        }
    }

    /** One input control. Only the fields relevant to {@link #kind} are read. */
    public static final class Input {
        public enum Kind { TEXT, BOOLEAN, SLIDER, DROPDOWN }
        public final Kind kind;
        public final String key;
        public Component label;

        public String textDefault;
        public Integer maxLength;
        public Boolean multiline;
        public Boolean booleanDefault;
        public Float min;
        public Float max;
        public Float step;
        public Float sliderDefault;
        public final List<Option> options = new ArrayList<>();

        public Input(Kind kind, String key) {
            this.kind = kind;
            this.key = key;
            this.label = Component.text(key);
        }

        /** One dropdown choice: an id, an optional shown label, and whether it starts selected. */
        public static final class Option {
            public final String id;
            public Component label;
            public boolean selected;

            public Option(String id) {
                this.id = id;
            }
        }
    }

    /** One action button. */
    public static final class Button {
        public enum Action { EVENT, COMMAND, URL, COPY, SUGGEST, SHOW }

        /** The click id echoed to the dialog event; null for action buttons that never fire it. */
        public final String id;
        public Component label;
        public Component tooltip;
        public Action action = Action.EVENT;
        /** The command, url, or text the non-event actions carry. */
        public String actionValue;

        public Button(String id) {
            this.id = id;
            this.label = Component.text(id != null ? id : "");
        }
    }
}
