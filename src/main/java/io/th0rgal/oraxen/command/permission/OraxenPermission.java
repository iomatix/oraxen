package io.th0rgal.oraxen.command.permission;

import org.bukkit.command.CommandSender;

import io.th0rgal.oraxen.language.Message;
import io.th0rgal.oraxen.language.Variable;
import io.th0rgal.oraxen.settings.IPlaceable;
import io.th0rgal.oraxen.utils.general.Placeholder;

public enum OraxenPermission implements IPlaceable {

    //
    // General Permissions
    //
    ALL,

    //
    // Command Permissions
    //
    COMMAND_ALL(ALL),

    // Reload
    COMMAND_RELOAD(COMMAND_ALL),

    // Help
    COMMAND_HELP(COMMAND_ALL),

    // Debug
    COMMAND_DEBUG(COMMAND_ALL),

    // Inventory
    COMMAND_INVENTORY(COMMAND_ALL),

    // Repair
    COMMAND_REPAIR_ALL(COMMAND_ALL), COMMAND_REPAIR(COMMAND_REPAIR_ALL), COMMAND_REPAIR_EVERYTHING(COMMAND_REPAIR_ALL)

    //
    // END
    //
    ;

    public static final String PERMISSION_FORMAT = "%s.%s";

    private final String prefix;
    private final OraxenPermission parent;

    private OraxenPermission() {
        this.prefix = "oraxen";
        this.parent = null;
    }

    private OraxenPermission(OraxenPermission parent) {
        this.prefix = "oraxen";
        this.parent = parent;
    }

    private OraxenPermission(String prefix) {
        this.prefix = prefix;
        this.parent = null;
    }

    private OraxenPermission(String prefix, OraxenPermission parent) {
        this.prefix = prefix;
        this.parent = parent;
    }

    /*
     * Permission check
     */

    public boolean has(CommandSender sender) {
        return sender.hasPermission(asString()) ? true : ((parent == null) ? false : parent.has(sender));
    }

    public boolean required(CommandSender sender) {
        if (!has(sender)) {
            Message.NO_PERMISSION.send(sender, Variable.PREFIX.placeholder(), getPlaceholder());
            return false;
        }
        return true;
    }

    public boolean required(CommandSender sender, Placeholder... placeholders) {
        if (!has(sender)) {
            Message.NO_PERMISSION.send(sender, placeholders, Variable.PREFIX.placeholder(), getPlaceholder());
            return false;
        }
        return true;
    }

    /*
     * Placeholder creation
     */

    @Override
    public Placeholder getPlaceholder() {
        return new Placeholder("permission", asString());
    }

    /*
     * String functions
     */

    public String asString() {
        return toString();
    }

    @Override
    public String toString() {
        String name = name().toLowerCase();
        if (name.endsWith("_ALL"))
            name = name.substring(0, name.length() - 4) + '*';
        else if (name.equals("ALL"))
            name = "*";
        return String.format(PERMISSION_FORMAT, prefix, name);
    }

}
