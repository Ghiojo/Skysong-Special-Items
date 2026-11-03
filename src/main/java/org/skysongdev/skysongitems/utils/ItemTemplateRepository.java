package org.skysongdev.skysongitems.utils;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * Reads and writes item templates: the YAML files that describe a {@link SkysongItem}.
 *
 * <p>Saved templates go to the plugin's {@code GeneratedItems} folder, or a subfolder of
 * it, named after the item. A saved item's id is its path inside that folder without the
 * {@code .yml} extension, such as {@code sword} or {@code weapons/sword}. Loading takes any
 * file, because TheBrewingProject ingredients are read from the {@code brewing} folder with
 * the same format.
 */
public final class ItemTemplateRepository {
    private static final String EXTENSION = ".yml";
    /** One or more lowercase names joined by {@code /}, so a folder can never leave GeneratedItems. */
    private static final Pattern FOLDER = Pattern.compile("[a-z0-9_-]+(/[a-z0-9_-]+)*");

    private ItemTemplateRepository() {
        // Static methods only.
    }

    /** The folder saved templates are written to, created first if it does not exist. */
    public static File generatedItemsFolder(){
        File outputDir = new File(getPlugin().getDataFolder(), "GeneratedItems");
        if (!outputDir.exists()) {
            outputDir.mkdirs(); // Create the directory if it doesn't exist
        }
        return outputDir;
    }

    /** Saves the item to {@code GeneratedItems/<formatted name>.yml}, replacing any template of that name. */
    public static void save(SkysongItem item){
        save(item, "");
    }

    /**
     * Saves the item to {@code GeneratedItems/<folder>/<formatted name>.yml}, creating the
     * folder if needed and replacing any template of that name in it.
     *
     * @param folder a folder accepted by {@link #isValidFolder(String)}; empty saves to GeneratedItems itself
     * @return the saved item's id, which {@link #savedItemFile(String)} turns back into its file
     * @throws IllegalArgumentException if the folder is not valid
     */
    public static String save(SkysongItem item, String folder){
        if (!isValidFolder(folder)) {
            throw new IllegalArgumentException("Invalid saved item folder: " + folder);
        }
        String id = folder.isEmpty() ? item.getFormattedName() : folder + "/" + item.getFormattedName();
        File itemFile = savedItemFile(id);
        itemFile.getParentFile().mkdirs();
        YamlConfiguration yaml = toYaml(item);
        try {
            yaml.save(itemFile);
            getPlugin().getLogger().info("Item file saved: " + itemFile.getPath());
        } catch (IOException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Failed to save item file: " + e.getMessage(), e);
        }
        return id;
    }

    /**
     * Whether items may be saved to this folder: empty for GeneratedItems itself, or names
     * made of {@code a-z}, {@code 0-9}, {@code _} and {@code -} joined by {@code /}. That
     * rules out {@code ..}, absolute paths and backslashes.
     */
    public static boolean isValidFolder(String folder) {
        return folder.isEmpty() || FOLDER.matcher(folder).matches();
    }

    /** The file of the saved item with this id, such as {@code weapons/sword}. */
    public static File savedItemFile(String id) {
        return new File(generatedItemsFolder(), id + EXTENSION);
    }

    /** The ids of every saved item, subfolders included, in alphabetical order. */
    public static List<String> savedItemIds() {
        return walkGeneratedItems(path -> Files.isRegularFile(path) && path.toString().endsWith(EXTENSION)).stream()
                .map(id -> id.substring(0, id.length() - EXTENSION.length()))
                .toList();
    }

    /** Every subfolder of GeneratedItems, as {@code /}-separated paths in alphabetical order. */
    public static List<String> savedItemFolders() {
        return walkGeneratedItems(Files::isDirectory);
    }

    /** The matching paths below GeneratedItems, relative to it and separated by {@code /}. */
    private static List<String> walkGeneratedItems(Predicate<Path> filter) {
        Path root = generatedItemsFolder().toPath();
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(path -> !Objects.equals(path, root))
                    .filter(filter)
                    .map(path -> root.relativize(path).toString().replace(File.separatorChar, '/'))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            getPlugin().getLogger().log(Level.WARNING, "Could not list the saved items: " + e.getMessage(), e);
            return List.of();
        }
    }

    /** Loads a template file, returning a skull or potion item when the template's type is one. */
    public static SkysongItem load(File file){
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        String name = yaml.getString("name");
        ArrayList<String> lore = new ArrayList<>(yaml.getStringList("lore"));
        String type = yaml.getString("type");
        NamespacedKey itemModel = readItemModel(yaml);
        CustomModelData customModelData = readCustomModelData(yaml);
        String deleted = yaml.getString("deleted", "");
        String properties = yaml.getString("properties", "");
        LinkedHashMap<NamespacedKey, String> tagValues = readTagValues(yaml, file);
        SkysongItem item = new SkysongItem(name, lore, type, itemModel, deleted, properties, tagValues)
                .withCustomModelData(customModelData);

        if(type.equals(Material.PLAYER_HEAD.toString())) {
            String id = yaml.getString("id", "");
            String playerName = yaml.getString("playername", "");
            String texture = yaml.getString("texture", "");
            return new SkysongSkullItem(item, id, playerName, texture);
        }

        if(isPotionType(type)){
            Color color = Color.fromRGB(yaml.getInt("potionColor"));
            return new SkysongPotionItem(item, color);
        }

        return item;
    }

    private static boolean isPotionType(String type) {
        return type.equals(Material.POTION.toString())
                || type.equals(Material.SPLASH_POTION.toString())
                || type.equals(Material.LINGERING_POTION.toString());
    }

    private static YamlConfiguration toYaml(SkysongItem item) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("name", item.getName());
        yaml.set("lore", item.getLore());
        yaml.set("type", item.getType());
        if(item.getItemModel() != null)
            yaml.set("itemModel", item.getItemModel().getNamespace() + ":" + item.getItemModel().getKey());
        yaml.set("deleted", item.getDeleted());
        yaml.set("properties", item.getProperties());
        yaml.set("tags", item.getTagsAsStrings());
        yaml.set("values", item.getValues());

        if(item instanceof SkysongSkullItem skullItem) {
            yaml.set("id", skullItem.getId());
            yaml.set("playername", skullItem.getPlayerName());
            yaml.set("texture", skullItem.getTexture());
        }

        if(item instanceof SkysongPotionItem potionItem) {
            yaml.set("potionColor", potionItem.getColor().asRGB());
        }

        if(item.getCustomModelData() != null) {
            writeCustomModelData(yaml, item.getCustomModelData());
        }
        return yaml;
    }

    private static void writeCustomModelData(YamlConfiguration yaml, CustomModelData customModelData) {
        yaml.set("customModelData.floats", customModelData.getFloats());
        yaml.set("customModelData.flags", customModelData.getFlags());
        yaml.set("customModelData.strings", customModelData.getStrings());
        List<String> rgbList = new ArrayList<>();
        for (Color color : customModelData.getColors()) {
            rgbList.add(color.getRed() + "," + color.getGreen() + "," + color.getBlue());
        }
        yaml.set("customModelData.colors", rgbList);
    }

    private static NamespacedKey readItemModel(YamlConfiguration yaml) {
        ArrayList<String> model = new ArrayList<>(Arrays.asList(yaml.getString("itemModel", "").split(":")));
        if(model.size() < 2 || model.get(0).isEmpty() || model.get(1).isEmpty()) {
            return null; // If no item model is specified, set it to null
        }
        return new NamespacedKey(model.get(0), model.get(1));
    }

    private static CustomModelData readCustomModelData(YamlConfiguration yaml) {
        List<Color> parsedColors = new ArrayList<>();
        for (String rgb : yaml.getStringList("customModelData.colors")) {
            Color color = parseRgb(rgb);
            if (color != null) {
                parsedColors.add(color);
            }
        }
        return new CustomModelData(yaml.getFloatList("customModelData.floats"), yaml.getBooleanList("customModelData.flags"), yaml.getStringList("customModelData.strings"), parsedColors);
    }

    /** Parses an "r,g,b" entry, logging a warning and returning null when it is malformed. */
    private static Color parseRgb(String rgb) {
        String[] parts = rgb.split(",");
        if (parts.length != 3) {
            getPlugin().getLogger().warning("Invalid RGB entry in customModelData.colors: " + rgb);
            return null;
        }
        try {
            int r = Integer.parseInt(parts[0].trim());
            int g = Integer.parseInt(parts[1].trim());
            int b = Integer.parseInt(parts[2].trim());
            return Color.fromRGB(r, g, b);
        } catch (NumberFormatException e) {
            getPlugin().getLogger().warning("Invalid RGB format in customModelData.colors: " + rgb);
            return null;
        }
    }

    private static LinkedHashMap<NamespacedKey, String> readTagValues(YamlConfiguration yaml, File file) {
        List<String> tagIds = yaml.getStringList("tags");
        List<String> values = yaml.getStringList("values");
        if (tagIds.size() != values.size()) {
            getPlugin().getLogger().warning("Item file " + file.getName() + " has " + tagIds.size()
                    + " tags but " + values.size() + " values; tags without a value are ignored.");
        }
        return SkysongItem.pairTagValues(tagIds, values);
    }
}
