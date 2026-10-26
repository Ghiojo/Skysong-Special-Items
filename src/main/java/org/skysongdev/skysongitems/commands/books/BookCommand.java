package org.skysongdev.skysongitems.commands.books;

import org.skysongdev.skysongitems.commands.DBReload;
import org.skysongdev.skysongitems.commands.Subcommand;
import org.skysongdev.skysongitems.commands.SubcommandRouter;
import org.skysongdev.skysongitems.database.BookQueries;

/**
 * {@code /si book}: the commands for the books stored in the database and the
 * descriptions bound to items.
 *
 * <ul>
 *     <li>{@code open <id>} opens a stored book</li>
 *     <li>{@code view} opens the book bound to the held item; anyone may use it</li>
 *     <li>{@code list [page]} lists the stored books, ten per page</li>
 *     <li>{@code save} stores the held written book</li>
 *     <li>{@code edit <id>} replaces a stored book with the held written book</li>
 *     <li>{@code delete <id>} deletes a stored book</li>
 *     <li>{@code bind <id>} and {@code unbind} bind a book to the held item or remove it</li>
 *     <li>{@code dmonly} and {@code rightclick} toggle whether the bound book is for DMs
 *     only and whether right-clicking the item opens it</li>
 *     <li>{@code reload} reconnects to the database</li>
 * </ul>
 */
public class BookCommand extends SubcommandRouter {
    public BookCommand(BookQueries bookQueries) {
        super("/si book");
        OpenBook openBook = new OpenBook(bookQueries);

        registerItemEdit(Subcommand.builder("open", openBook).playerOnly());
        register(Subcommand.builder("view", new ViewExtendedLore(openBook)).playerOnly().build());
        registerItemEdit(Subcommand.builder("list", new ListBooks(bookQueries)));
        registerItemEdit(Subcommand.builder("save", new SaveBook(bookQueries)).playerOnly());
        registerItemEdit(Subcommand.builder("edit", new SetBookContent(bookQueries)).playerOnly());
        registerItemEdit(Subcommand.builder("delete", new DeleteBook(bookQueries)));
        registerItemEdit(Subcommand.builder("bind", new BindBook(bookQueries)).playerOnly());
        registerItemEdit(Subcommand.builder("unbind", new UnbindBook()).playerOnly());
        registerItemEdit(Subcommand.builder("dmonly", new SetDMOnly()).playerOnly());
        registerItemEdit(Subcommand.builder("rightclick", new SetRightClickBook()).playerOnly());
        registerItemEdit(Subcommand.builder("reload", new DBReload()));
    }
}
