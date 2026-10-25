package org.skysongdev.skysongitems.commands.lore;

/**
 * The three lore sections of a Skysong item that players edit with their own
 * command: /physdesc, /funcdesc and /skilldesc.
 * <p>
 * The index is the section's position in
 * {@link org.skysongdev.skysongitems.utils.ItemData#getLoreSections()}. The
 * command name and permission must match the ones declared in plugin.yml.
 */
public enum LoreSection {
    PHYSICAL("physdesc", "skysongitems.pd", 0, 0),
    FUNCTIONAL("funcdesc", "skysongitems.fd", 1, 1),
    // Known bug, kept as it was: /skilldesc dl deletes a line from the
    // functional section. The fix is to change the last argument to 2.
    SKILL("skilldesc", "skysongitems.sd", 2, 1);

    private final String commandName;
    private final String permission;
    private final int index;
    private final int deleteLineIndex;

    LoreSection(String commandName, String permission, int index, int deleteLineIndex) {
        this.commandName = commandName;
        this.permission = permission;
        this.index = index;
        this.deleteLineIndex = deleteLineIndex;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getPermission() {
        return permission;
    }

    public int getIndex() {
        return index;
    }

    /** The index of the section that "dl" deletes a line from. */
    public int getDeleteLineIndex() {
        return deleteLineIndex;
    }

    // The three commands started as copies of each other and drifted apart.
    // The two methods below keep each command's behaviour as it was; whether
    // to unify them is a separate decision.

    /**
     * Whether "add" or "set" without any text shows the command's usage.
     * Only /physdesc does; /funcdesc and /skilldesc silently do nothing.
     */
    public boolean showsUsageWhenTextIsMissing() {
        return PHYSICAL.equals(this);
    }

    /**
     * Whether "set" with an empty hand prefixes its message with the plugin
     * tag. All sections do except /physdesc. ("add" always does.)
     */
    public boolean tagsEmptyHandMessageOnSet() {
        return !PHYSICAL.equals(this);
    }
}
