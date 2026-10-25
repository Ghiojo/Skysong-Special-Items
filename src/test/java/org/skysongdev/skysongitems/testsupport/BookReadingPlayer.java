package org.skysongdev.skysongitems.testsupport;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.List;

/** A mock player that remembers the books opened for it, which MockBukkit does not support. */
public final class BookReadingPlayer extends PlayerMock {
    private final List<ItemStack> openedBooks = new ArrayList<>();

    public BookReadingPlayer(ServerMock server, String name) {
        super(server, name);
    }

    @Override
    public void openBook(@NotNull ItemStack book) {
        openedBooks.add(book);
    }

    /** Every book opened for this player, oldest first. */
    public List<ItemStack> getOpenedBooks() {
        return openedBooks;
    }
}
