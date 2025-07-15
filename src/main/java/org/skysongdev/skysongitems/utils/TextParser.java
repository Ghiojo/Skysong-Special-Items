package org.skysongdev.skysongitems.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class TextParser {
    private static final Map.Entry<Integer, String> PHYSCOLOR = new AbstractMap.SimpleEntry<>(0, "<gray>");
    private static final Map.Entry<Integer, String> FUNCCOLOR = new AbstractMap.SimpleEntry<>(1, "<#424242>");
    private static final Map.Entry<Integer, String> SKILLCOLOR = new AbstractMap.SimpleEntry<>(2, "<#81a871>");

    private static final Map<Integer, String> COLOR_MAP = Map.ofEntries(PHYSCOLOR, FUNCCOLOR, SKILLCOLOR);


    private static final Map.Entry<Integer, String> PREPHYSCOLOR = new AbstractMap.SimpleEntry<>(0, "&7");
    private static final Map.Entry<Integer, String> PREFUNCCOLOR = new AbstractMap.SimpleEntry<>(1, "&#424242");
    private static final Map.Entry<Integer, String> PRESKILLCOLOR = new AbstractMap.SimpleEntry<>(2, "&#81A871");

    private static final Map<Integer, String> PRE_COLOR_MAP = Map.ofEntries(PREPHYSCOLOR, PREFUNCCOLOR, PRESKILLCOLOR);

    public static final int MAX_CHARACTERS = 45;

    //TODO: Edit ParseText to become a Transition Function
    public static List<String> TranslateFromLore(List<String> inputlore, String input) {
        String deletedLines = (input == null) ? "000" : input;
        List<String> parsedList = new ArrayList<String>();
        int index = 0;
        String buffer = "";
        String colorBuffer = "";
        StringBuilder interpreter = new StringBuilder(deletedLines);
        parsedList.add(COLOR_MAP.get(0) + "[Not Set]");
        parsedList.add(COLOR_MAP.get(1) + "[Not Set]");
        parsedList.add(COLOR_MAP.get(2) + "[Not Set]");
        if(inputlore == null){
            return parsedList;
        }
        if(interpreter.charAt(0) == '1'){
            index++;
            if(interpreter.charAt(1) == '1'){
                index++;
                if(interpreter.charAt(2) == '1'){
                    return parsedList;
                }
            }
        }
        for(int i = 0; i < inputlore.size() && index < 3; i++){
            if(Objects.equals(inputlore.get(i), ChatColor.of("#190898").toString())){
                if(buffer.isEmpty())
                    buffer = COLOR_MAP.get(i) + "[Not Set]\n";
                parsedList.set(index, buffer.substring(0, buffer.length()-1));
                index++;
                if(index < 2 && interpreter.charAt(index) == '1')
                    index++;
                buffer = "";
                continue;
            }
            if(interpreter.charAt(index) == '1'){
                continue;
            }
            colorBuffer = revertColor(inputlore.get(i));
            buffer = buffer.concat(colorBuffer + "\n");
        }
        if(index < 3 && interpreter.charAt(index) == '0'){
            parsedList.set(index, buffer.substring(0, buffer.length()-1));
        }

        return parsedList;
    }

    //TODO: Same as above
    /*public static List<String> TranslateFromLore(List<String> inputlore) {
        List<String> parsedList = new ArrayList<String>();
        int index = 0;
        String buffer = "";
        String colorBuffer = "";

        parsedList.add(COLOR_MAP.get(0) + "[Not Set]");
        parsedList.add(COLOR_MAP.get(1) + "[Not Set]");
        parsedList.add(COLOR_MAP.get(2) + "[Not Set]");
        if(inputlore == null){
            return parsedList;
        }

        for(int i = 0; i < inputlore.size() && index < 3; i++){
            if(Objects.equals(inputlore.get(i), ChatColor.of("#190898").toString())){
                if(buffer.equals(""))
                    buffer = COLOR_MAP.get(i) + "[Not Set]";
                parsedList.set(index, buffer.substring(0, buffer.length()-1));
                buffer = "";
                index++;
                continue;
            }
            colorBuffer = revertColor(inputlore.get(i));
            buffer = buffer.concat(colorBuffer + "\n");
        }
        if(index < 3)
            parsedList.set(index, buffer.substring(0, buffer.length()-1));

        return parsedList;
    }*/

    //TODO: Edit TranslateToLore to use MiniMessage Formatting and return a List<Component>
    public static List<Component> TranslateToLore(List<String> inputParsedText, String deletionInput) {
        //Is the input null because the item that you got it from has no deletedLines tag? If so, set it to "000"
        String deletedLines = (deletionInput == null || deletionInput.isEmpty()) ? "000" : deletionInput;
        List<Component> translatedLore = new ArrayList<Component>();
        List<String> buffer;
        int index = 0;
        String colorBuffer = "";
        StringBuilder interpreter = new StringBuilder(deletedLines);

        for (int i = 0; i < inputParsedText.size(); i++) {
            if(interpreter.charAt(i) == '0'){
                buffer = Arrays.asList(inputParsedText.get(i).split("\n"));
                for (String s : buffer) {
                    colorBuffer = translateColorCodes(s);
                    colorBuffer = translateHexColorCodes("&#", "", colorBuffer);
                    translatedLore.add(getPlugin().getMiniMessage().deserialize(colorBuffer).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.byBoolean(false)));
                }
                translatedLore.add(getPlugin().getMiniMessage().deserialize(""));
            }
        }
        if(!translatedLore.isEmpty()){
            translatedLore.remove(translatedLore.size() - 1);
        }
        return translatedLore;
    }

    //TODO: Same as above
    /*public static List<Component> TranslateToLore(List<String> inputParsedText) {
        List<Component> translatedLore = new ArrayList<Component>();
        List<String> buffer;
        String colorBuffer = "";
        for (int i = 0; i < inputParsedText.size(); i++) {
            buffer = Arrays.asList(inputParsedText.get(i).split("\n"));
            for (String s : buffer) {
                colorBuffer = translateColorCodes(s);
                colorBuffer = translateHexColorCodes("&#", "", colorBuffer);
                translatedLore.add(getPlugin().getMiniMessage().deserialize(colorBuffer).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.byBoolean(false)));
            }
            translatedLore.add(getPlugin().getMiniMessage().deserialize(""));
        }
        translatedLore.remove(translatedLore.size() - 1);

        return translatedLore;
    }*/

    //TODO: Edit manipulateSection to use MiniMessage Formatting
    //Modes: 0 -> Replace, 1 -> Concatenate, 2 -> Append Below Last Line
    public static List<String> manipulateSection(List<String> startingList, String[] inputLore, int blockIndex, int mode){
        List<String> splitLore = Arrays.asList(startingList.get(blockIndex).split("\n"));
        String buffer = (mode == 1) ? splitLore.getLast() : "";
        int wordCount = (mode == 1) ? stripColor(buffer).length() : 0;
        String colorBuffer = null;
        final Pattern clearPattern = Pattern.compile("&-");

        for(String word : inputLore){
            if(clearPattern.matcher(word).find()){
                word = word.replaceAll("&-", "<reset>" + PRE_COLOR_MAP.get(blockIndex));
            }
            if(CheckForColor(word) != null){
                colorBuffer = CheckForColor(word);
                colorBuffer = translateHexColorCodes("&#", "", colorBuffer);
                colorBuffer = translateColorCodes(colorBuffer);
            }
            String hexedWord = TextParser.translateHexColorCodes("&#", "", word);
            hexedWord = translateColorCodes(hexedWord);
            String wordcopy = hexedWord;

            //Checks if this is the first word or if the sum of the current line and the word is greater than 45
            if((wordCount + stripColor(wordcopy).length() + 1) > 45 || buffer.isEmpty()){
                //It's greater or the first word, so we add a new line
                buffer = buffer.concat("\n" + COLOR_MAP.get(blockIndex));
                if(colorBuffer != null){
                    buffer = buffer.concat(colorBuffer);
                }
                wordCount = 0;
            } else{
                //It's not, so we add a space
                buffer = buffer.concat(" ");
                wordCount++;
            }

            //Either way you are about to add the word to the buffer
            buffer = buffer.concat(translateColorCodes(hexedWord));
            wordCount += stripColor(wordcopy).length();
        }

        switch(mode){
            case 0:
                startingList.set(blockIndex, COLOR_MAP.get(blockIndex) + buffer.substring(1));
                break;
            case 1:
                splitLore.set(splitLore.size()-1, buffer);
                startingList.set(blockIndex, String.join("\n", splitLore));
                break;
            case 2:
                startingList.set(blockIndex, startingList.get(blockIndex).concat(buffer));
                break;
            default:
                break;
        }

        return startingList;
    }

    //Same as above
    /*public static List<String> manipulateSectionWithConcat(List<String> startingList, String[] inputLore, int blockIndex){
        List<String> test = Arrays.asList(startingList.get(blockIndex).split("\n"));
        String tempword;
        String buffer = test.get(test.size()-1);
        String colorBuffer = null;
        final Pattern clearPattern = Pattern.compile("&-");

        if(inputLore.length > 0)
            inputLore[0] = "&-" + inputLore[0];

        List<String> newInput = new ArrayList<String>();
        boolean isBufferFull = false;
        for(String word : inputLore){
            if(clearPattern.matcher(word).find()) {
                word = word.replaceAll("&-", "<reset>" + PRE_COLOR_MAP.get(blockIndex));
            }
            tempword = TextParser.translateHexColorCodes("&#", "", word);
            tempword = translateColorCodes(tempword);
            String strippedBuffer = stripColor(buffer);
            String strippedWord = stripColor(tempword);
            if(strippedBuffer.length() + strippedWord.length() + 1 <= 45 && !isBufferFull) {
                if(CheckForColor(word) != null){
                    colorBuffer = CheckForColor(word);
                }
                word = TextParser.translateHexColorCodes("&#", "", word);
                word = translateColorCodes(word);
                buffer = buffer.concat(" " + word);
            }
            else{
                isBufferFull = true;
                newInput.add(word);
            }
                
        }
        test.set(test.size()-1, buffer);
        startingList.set(blockIndex, String.join("\n", test));

        if(colorBuffer != null && !newInput.isEmpty()){
            newInput.set(0, colorBuffer + newInput.get(0));
        }

        String[] arrayToSend = new String[newInput.size()];
        arrayToSend = newInput.toArray(arrayToSend);

        return manipulateSection(startingList, arrayToSend, blockIndex, false);
    }*/

    public static String translateHexColorCodes(String startTag, String endTag, String message)
    {
        final Pattern hexPattern = Pattern.compile(startTag + "([A-Fa-f0-9]{6})" + endTag);
        Matcher matcher = hexPattern.matcher(message);
        StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
        while (matcher.find())
        {
            String group = matcher.group(1);
            if(Objects.equals(group, "190898")){
                group = "190899";
            }
            matcher.appendReplacement(buffer, "<#" + group + ">");
        }
        return matcher.appendTail(buffer).toString();
    }

    public static String translateColorCodes(String message){
        Pattern colorPattern = Pattern.compile("&([A-Fa-fk-oK-O0-9])");
        Matcher matcher = colorPattern.matcher(message);
        StringBuilder buffer = new StringBuilder();
        while(matcher.find()){
            String group = matcher.group(1);
            switch(group.toLowerCase()){
                case "0":
                    matcher.appendReplacement(buffer, "<black>");
                    break;
                case "1":
                    matcher.appendReplacement(buffer, "<dark_blue>");
                    break;
                case "2":
                    matcher.appendReplacement(buffer, "<dark_green>");
                    break;
                case "3":
                    matcher.appendReplacement(buffer, "<dark_aqua>");
                    break;
                case "4":
                    matcher.appendReplacement(buffer, "<dark_red>");
                    break;
                case "5":
                    matcher.appendReplacement(buffer, "<dark_purple>");
                    break;
                case "6":
                    matcher.appendReplacement(buffer, "<gold>");
                    break;
                case "7":
                    matcher.appendReplacement(buffer, "<gray>");
                    break;
                case "8":
                    matcher.appendReplacement(buffer, "<dark_gray>");
                    break;
                case "9":
                    matcher.appendReplacement(buffer, "<blue>");
                    break;
                case "a":
                    matcher.appendReplacement(buffer, "<green>");
                    break;
                case "b":
                    matcher.appendReplacement(buffer, "<aqua>");
                    break;
                case "c":
                    matcher.appendReplacement(buffer, "<red>");
                    break;
                case "d":
                    matcher.appendReplacement(buffer, "<light_purple>");
                    break;
                case "e":
                    matcher.appendReplacement(buffer, "<yellow>");
                    break;
                case "f":
                    matcher.appendReplacement(buffer, "<white>");
                    break;
                case "k":
                    matcher.appendReplacement(buffer, "<obfuscated>");
                    break;
                case "l":
                    matcher.appendReplacement(buffer, "<bold>");
                    break;
                case "m":
                    matcher.appendReplacement(buffer, "<strikethrough>");
                    break;
                case "n":
                    matcher.appendReplacement(buffer, "<underline>");
                    break;
                case "o":
                    matcher.appendReplacement(buffer, "<italic>");
                    break;
                default:
                    break;
            }
        }
        return matcher.appendTail(buffer).toString();
    }

    public static List<String> deleteLine(int blockIndex, int lineToDelete, List<String> inputLore){
        String[] buffer = inputLore.get(blockIndex).split("\n");
        String[] toBuffer = new String[buffer.length-1];
        if(lineToDelete < 1 || lineToDelete > buffer.length){
            return inputLore;
        }

        for(int i = 0; i < buffer.length-1 ;i++){
            if(i >= lineToDelete-1){
                toBuffer[i] = buffer[i+1];
            } else{
                toBuffer[i] = buffer[i];
            }
        }

        inputLore.set(blockIndex, String.join("\n", toBuffer));

        return inputLore;
    }

    public static int countValidCharacters(String input){
        String buffer = input.replaceAll("&#" + "([A-Fa-f0-9]{6})", "$1");
        ChatColor.stripColor(buffer);
        int count = input.length();

        return count;
    }

    public static String CheckForColor(String word) {
        final Pattern colorPattern = Pattern.compile("(&#([A-Fa-f0-9]{6})|&([A-Fa-fK-Rk-r0-9]))(&([K-Rk-r]))?");

        if (colorPattern.matcher(word).find()) {
            Matcher matcher = colorPattern.matcher(word);
            String group = null;
            while (matcher.find()) {
                group = matcher.group();
            }
            return group;
        }
        return null;
    }

    public static String revertColor(String string){
        final Pattern hexPattern = Pattern.compile(ChatColor.COLOR_CHAR + "x" + ChatColor.COLOR_CHAR + "([A-Fa-f0-9])" + ChatColor.COLOR_CHAR + "([A-Fa-f0-9])" + ChatColor.COLOR_CHAR + "([A-Fa-f0-9])" + ChatColor.COLOR_CHAR + "([A-Fa-f0-9])" + ChatColor.COLOR_CHAR + "([A-Fa-f0-9])" + ChatColor.COLOR_CHAR + "([A-Fa-f0-9])");

        Matcher hexMatcher = hexPattern.matcher(string);
        StringBuffer buffer = new StringBuffer();

        while (hexMatcher.find()) {
            String hexColor = hexMatcher.group(1) + hexMatcher.group(2) + hexMatcher.group(3) + hexMatcher.group(4) + hexMatcher.group(5) + hexMatcher.group(6);
            hexMatcher.appendReplacement(buffer, "&#" + hexColor);
        }
        hexMatcher.appendTail(buffer);

        String intermediateString = buffer.toString();

        return translateColorCodes(intermediateString.replace(ChatColor.COLOR_CHAR, '&'));


    }

    public static String stripColor(String input){
        return getPlugin().getMiniMessage().stripTags(input);
    }

}
