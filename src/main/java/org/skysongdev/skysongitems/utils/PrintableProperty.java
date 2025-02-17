package org.skysongdev.skysongitems.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.SkysongItems;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class PrintableProperty extends Property {
    private String printable;
    private ArrayList<String> valuetags;
    private int weight;

    public PrintableProperty(String id, String type, String item, String name, String description, String printable, ArrayList<String> valuetags, int weight) {
        super(id, type, item, name, description);
        this.printable = printable;
        this.valuetags = valuetags;
        this.weight = weight;
    }

    public String getParsedPrinting(ItemStack item){
        String parsed = printable;
        String regex = "\\$[1-9]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(parsed);
        StringBuffer result = new StringBuffer();

        while(matcher.find()){
            int index = Integer.parseInt(matcher.group().substring(1)) - 1;
            if (index >= 0 && index < valuetags.size()) {
                PersistentDataType<?,?> type = getPlugin().getUtils().tagList.get(new NamespacedKey(getPlugin(), valuetags.get(index)));
                if(item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(getPlugin(), valuetags.get(index)), type)) {
                    String value = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(getPlugin(), valuetags.get(index)), type).toString();
                    matcher.appendReplacement(result, value);
                }
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public int getWeight(){
        return weight;
    }

    public ArrayList<String> getValuetags(){
        return valuetags;
    }

    @Override
    public String getType(){
        return "printable";
    }
}
