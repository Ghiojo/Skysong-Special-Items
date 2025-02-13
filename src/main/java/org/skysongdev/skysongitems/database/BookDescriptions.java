package org.skysongdev.skysongitems.database;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookDescriptions {
    private int id;
    private String name;
    private String bookcontent;

    public BookDescriptions(int id, String name, String bookcontent) {
        this.id = id;
        this.name = name;
        this.bookcontent = bookcontent;
    }
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBookcontent() {
        return bookcontent;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BookDescriptions(ItemStack inputbook){
        BookMeta inputmeta = (BookMeta) inputbook.getItemMeta();
        name = ChatColor.stripColor(inputmeta.getTitle());
        this.bookcontent = "";
        for(int i = 1; i <= inputmeta.getPageCount(); i++){
            this.bookcontent = this.bookcontent.concat(inputmeta.getPage(i));
            if(i != inputmeta.getPageCount())
                this.bookcontent = this.bookcontent.concat("[PageEnd]");
        }
    }
}
