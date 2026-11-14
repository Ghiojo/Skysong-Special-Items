package org.skysongdev.skysongitems.utils;

public class RenameTrait extends Trait {
    private String tag;
    public RenameTrait(String id, String type, String item, String name, String description, String tag) {
        super(id, type, item, name, description);
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String getType(){
        return "rename";
    }
}

