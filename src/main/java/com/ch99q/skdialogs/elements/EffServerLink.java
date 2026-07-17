package com.ch99q.skdialogs.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.ch99q.skdialogs.paper.Text;
import org.bukkit.Bukkit;
import org.bukkit.ServerLinks;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

@Name("Server Link")
@Description({
        "Adds an entry to the server's link list, shown under \"Server Links\" in the pause menu " +
        "and in a 'server links' dialog. Links are server-wide, so 'on load' is the natural place.",
        "A typed link (website, news, ...) is labelled by the client in its own language. A " +
        "labeled link shows your text; use Skript's '<translate:...>' tag for a per-language label.",
        "'clear the server links' empties the list, so a reloading script can rebuild it without " +
        "adding duplicates."
})
@Examples({
        "on load:",
        "\tclear the server links",
        "\tadd server link labeled \"<translate:item.minecraft.filled_map>\" to \"https://map.example.com\"",
        "\tadd website server link to \"https://example.com\""
})
@Since("1.1.0")
public class EffServerLink extends Effect {

    private static final int LABELED = 0, TYPED = 1, CLEAR = 2;

    static {
        Skript.registerEffect(EffServerLink.class,
                "add [a] server link labeled %string% to [url] %string%",
                "add [a|the] (1¦bug report|2¦community guidelines|3¦support|4¦status|5¦feedback"
                        + "|6¦community|7¦website|8¦forum[s]|9¦news|10¦announcement[s]) server link to [url] %string%",
                "(clear|delete) [all] [the] server links");
    }

    private int action;
    private @Nullable ServerLinks.Type type;
    private @Nullable Expression<String> label;
    private @Nullable Expression<String> url;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        action = matchedPattern;
        if (action == LABELED) {
            label = (Expression<String>) expressions[0];
            url = (Expression<String>) expressions[1];
        } else if (action == TYPED) {
            type = switch (parseResult.mark) {
                case 1 -> ServerLinks.Type.REPORT_BUG;
                case 2 -> ServerLinks.Type.COMMUNITY_GUIDELINES;
                case 3 -> ServerLinks.Type.SUPPORT;
                case 4 -> ServerLinks.Type.STATUS;
                case 5 -> ServerLinks.Type.FEEDBACK;
                case 6 -> ServerLinks.Type.COMMUNITY;
                case 7 -> ServerLinks.Type.WEBSITE;
                case 8 -> ServerLinks.Type.FORUMS;
                case 9 -> ServerLinks.Type.NEWS;
                default -> ServerLinks.Type.ANNOUNCEMENTS;
            };
            url = (Expression<String>) expressions[0];
        }
        return true;
    }

    @Override
    protected void execute(Event event) {
        ServerLinks links = Bukkit.getServer().getServerLinks();
        if (action == CLEAR) {
            for (ServerLinks.ServerLink link : links.getLinks()) {
                links.removeLink(link);
            }
            return;
        }
        String address = url != null ? url.getSingle(event) : null;
        if (address == null) {
            return;
        }
        URI uri;
        try {
            uri = URI.create(address);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("[SkDialogs] invalid server link url '" + address + "'");
            return;
        }
        if (action == TYPED) {
            links.addLink(type, uri);
        } else {
            String labelText = label != null ? label.getSingle(event) : null;
            if (labelText != null) {
                links.addLink(Text.component(labelText), uri);
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return switch (action) {
            case CLEAR -> "clear server links";
            case TYPED -> "add typed server link";
            default -> "add server link";
        };
    }
}
