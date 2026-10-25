package org.skysongdev.skysongitems.utils;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Keeps each player's item permissions equal to the permissions of the
 * permission items they hold: a player has an item's permission while the item
 * is in their inventory, and loses it once no item carrying it is left.
 *
 * <p>Rather than following every way an item can enter or leave an inventory,
 * {@link #sync} compares what the player holds with what it granted them
 * before, so it can be called after any change, from any cause.
 */
public final class PermissionItemSync {
    private final BiConsumer<Player, String> grant;
    private final BiConsumer<Player, String> revoke;
    private final Map<UUID, Set<String>> granted = new HashMap<>();

    /** Grants and revokes through LuckPerms, see {@link PermissionItems}. */
    public PermissionItemSync() {
        this(PermissionItems::grant, PermissionItems::revoke);
    }

    /** Grants and revokes through the given calls, so tests can record them. */
    public PermissionItemSync(BiConsumer<Player, String> grant, BiConsumer<Player, String> revoke) {
        this.grant = grant;
        this.revoke = revoke;
    }

    /** Grants the permissions of the items the player now holds and revokes those of the items they no longer hold. */
    public void sync(Player player) {
        Set<String> held = new HashSet<>(PermissionItems.heldPermissions(player));
        Set<String> before = granted.getOrDefault(player.getUniqueId(), Set.of());
        for (String permission : held) {
            if (!before.contains(permission)) {
                grant.accept(player, permission);
            }
        }
        for (String permission : before) {
            if (!held.contains(permission)) {
                revoke.accept(player, permission);
            }
        }
        if (held.isEmpty()) {
            granted.remove(player.getUniqueId());
        } else {
            granted.put(player.getUniqueId(), held);
        }
    }

    /** Calls {@link #sync} for each of the players. */
    public void syncAll(Collection<? extends Player> players) {
        players.forEach(this::sync);
    }

    /**
     * Revokes every item permission granted to the player and forgets them.
     * Call this when the player quits, and for every online player when the plugin is disabled,
     * so no permission outlives the check that would take it away.
     */
    public void revokeAll(Player player) {
        Set<String> before = granted.remove(player.getUniqueId());
        if (before != null) {
            before.forEach(permission -> revoke.accept(player, permission));
        }
    }

    /** The item permissions currently granted to the player. */
    public Set<String> grantedTo(Player player) {
        return Set.copyOf(granted.getOrDefault(player.getUniqueId(), Set.of()));
    }
}
