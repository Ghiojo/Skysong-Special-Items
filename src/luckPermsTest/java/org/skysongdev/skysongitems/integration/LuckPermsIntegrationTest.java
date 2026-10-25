package org.skysongdev.skysongitems.integration;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.data.NodeMap;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeBuilder;
import net.luckperms.api.node.NodeBuilderRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Runs the integration against a mocked LuckPerms API, recording which of the
 * user's node maps it changes: {@code data()} is saved by LuckPerms,
 * {@code transientData()} lasts until the player logs out.
 */
class LuckPermsIntegrationTest {
    private static final UUID PLAYER_ID = UUID.randomUUID();
    private static final String PERMISSION = "skysongitems.test";

    private final LuckPerms luckPerms = mock(LuckPerms.class);
    private final UserManager userManager = mock(UserManager.class);
    private final User user = mock(User.class);
    private final NodeMap savedNodes = mock(NodeMap.class);
    private final NodeMap transientNodes = mock(NodeMap.class);
    private final Node node = mock(Node.class);
    private LuckPermsIntegration integration;

    @BeforeEach
    void mockLuckPerms() {
        NodeBuilderRegistry builders = mock(NodeBuilderRegistry.class);
        NodeBuilder<?, ?> builder = mock(NodeBuilder.class);
        when(luckPerms.getNodeBuilderRegistry()).thenReturn(builders);
        doReturn(builder).when(builders).forKey(PERMISSION);
        doReturn(node).when(builder).build();
        when(luckPerms.getUserManager()).thenReturn(userManager);
        when(userManager.getUser(PLAYER_ID)).thenReturn(user);
        when(user.data()).thenReturn(savedNodes);
        when(user.transientData()).thenReturn(transientNodes);
        when(savedNodes.remove(node)).thenReturn(DataMutateResult.FAIL_LACKS);
        integration = new LuckPermsIntegration(luckPerms);
    }

    @Test
    void grantGivesATransientNodeThatEndsAtLogout() {
        integration.grant(PLAYER_ID, PERMISSION);

        verify(transientNodes).add(node);
    }

    @Test
    void grantNeverSavesTheNode() {
        integration.grant(PLAYER_ID, PERMISSION);

        verify(savedNodes, never()).add(any());
        verify(userManager, never()).saveUser(any());
    }

    @Test
    void revokeRemovesTheTransientNode() {
        integration.revoke(PLAYER_ID, PERMISSION);

        verify(transientNodes).remove(node);
        verify(userManager, never()).saveUser(any());
    }

    @Test
    void revokeRemovesAndSavesANodeSavedBeforeTheFix() {
        when(savedNodes.remove(node)).thenReturn(DataMutateResult.SUCCESS);

        integration.revoke(PLAYER_ID, PERMISSION);

        verify(savedNodes).remove(node);
        verify(userManager).saveUser(user);
    }

    @Test
    void offlinePlayerIsLeftUntouched() {
        UUID offlineId = UUID.randomUUID();

        integration.grant(offlineId, PERMISSION);
        integration.revoke(offlineId, PERMISSION);

        verify(user, never()).transientData();
        verify(user, never()).data();
    }
}
