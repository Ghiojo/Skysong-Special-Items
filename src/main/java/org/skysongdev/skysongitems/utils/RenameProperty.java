package org.skysongdev.skysongitems.utils;

public class RenameProperty extends Property {
    private String tag;
    public RenameProperty(String id, String type, String item, String name, String description, String tag) {
        super(id, type, item, name, description);
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String getPropType(){
        return "rename";
    }
}

