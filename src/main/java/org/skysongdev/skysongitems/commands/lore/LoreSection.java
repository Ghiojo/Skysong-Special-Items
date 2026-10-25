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
    PHYSICAL("physdesc", "skysongitems.pd", 0),
    FUNCTIONAL("funcdesc", "skysongitems.fd", 1),
    SKILL("skilldesc", "skysongitems.sd", 2);

    private final String commandName;
    private final String permission;
    private final int index;

    LoreSection(String commandName, String permission, int index) {
        this.commandName = commandName;
        this.permission = permission;
        this.index = index;
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
