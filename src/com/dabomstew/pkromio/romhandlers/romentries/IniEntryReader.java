package com.dabomstew.pkromio.romhandlers.romentries;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * An abstract class for any kind of {@link IniEntry} reading, presumably from an .ini file, though reading directly
 * from a {@link Scanner} is also supported.
 * <br>
 * {@code IniEntryReader} handles reading int, String, and int[] values as appropriate, and copying values from
 * another entry. These functions support the base {@code IniEntry} class. Subclasses of {@code IniEntryReader} may
 * also add functions relevant to whatever "entry" they are reading with
 * {@link #putSpecialKeyMethod(String, BiConsumer) putSpecialKeyMethod()},
 * {@link #putKeyPrefixMethod(String, BiConsumer) putKeyPrefixMethod()}, and
 * {@link #putKeySuffixMethod(String, BiConsumer) putKeySuffixMethod()}.
 *
 * @param <T>
 */
public abstract class IniEntryReader<T extends IniEntry> {

    public static int[] parseArray(String s) {
        String[] unparsed = s.substring(1, s.length() - 1).split(",");
        if (unparsed.length == 1 && unparsed[0].trim().isEmpty()) {
            return new int[0];
        } else {
            int[] value = new int[unparsed.length];
            int i = 0;
            for (String part : unparsed) {
                value[i++] = parseInt(part);
            }
            return value;
        }
    }

    public static int parseInt(String s) {
        int radix = 10;
        s = s.trim().toLowerCase();
        if (s.startsWith("0x") || s.startsWith("&h")) {
            radix = 16;
            s = s.substring(2);
        }
        try {
            return Integer.parseInt(s, radix);
        } catch (NumberFormatException ex) {
            System.err.println("invalid base " + radix + " number " + s);
            return 0;
        }
    }

    public static long parseLong(String s) {
        int radix = 10;
        s = s.trim().toLowerCase();
        if (s.startsWith("0x") || s.startsWith("&h")) {
            radix = 16;
            s = s.substring(2);
        }
        try {
            return Long.parseLong(s, radix);
        } catch (NumberFormatException ex) {
            System.err.println("invalid base " + radix + " number " + s);
            return 0;
        }
    }

    public static boolean parseBoolean(String s) {
        return (parseInt(s) > 0);
    }

    /**
     * An enum for dictating whether to read values as ints or Strings by default.
     * Protected so subclasses have to make the choice, instead of the code using them.
     */
    protected enum DefaultReadMode {INT, STRING}

    private static final String COMMENT_PREFIX = "//";

    private final DefaultReadMode defaultReadMode;
    private String fileName;
    private T current;
    private List<T> iniEntries;

    private final Map<String, BiConsumer<T, String[]>> keyPrefixMethods = new HashMap<>();
    private final Map<String, BiConsumer<T, String[]>> keySuffixMethods = new HashMap<>();
    private final Map<String, BiConsumer<T, String>> specialKeyMethods = new HashMap<>();

    public IniEntryReader(DefaultReadMode defaultReadMode) {
        this.defaultReadMode = defaultReadMode;
    }

    protected void putKeyPrefixMethod(String key, BiConsumer<T, String[]> method) {
        keyPrefixMethods.put(key, method);
    }

    protected void putKeySuffixMethod(String key, BiConsumer<T, String[]> method) {
        keySuffixMethods.put(key, method);
    }

    protected void putSpecialKeyMethod(String key, BiConsumer<T, String> method) {
        specialKeyMethods.put(key, method);
    }

    protected void putArrayAlias(String oldKey, String newKey) {
        putSpecialKeyMethod(oldKey, (entry, value) -> addArrayValue(entry, newKey, value));
    }

    protected void putIntAlias(String oldKey, String newKey) {
        putSpecialKeyMethod(oldKey, (entry, value) -> addIntValue(entry, newKey, value));
    }

    protected void putStringAlias(String oldKey, String newKey) {
        putSpecialKeyMethod(oldKey, (entry, value) -> addStringValue(entry, newKey, value));
    }

    protected void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<T> readEntriesFromFile(String fileName) throws IOException {
        Scanner scanner = new Scanner(Files.newInputStream(Paths.get(fileName)), "UTF-8");
        setFileName(fileName);
        return readEntriesFromScanner(scanner);
    }

    public List<T> readFromString(String string) {
        Scanner scanner = new Scanner(new StringReader(string));
        return readEntriesFromScanner(scanner);
    }

    public List<T> readEntriesFromScanner(Scanner scanner) {
        this.iniEntries = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            line = removeComments(line);

            if (line.isEmpty()) {
                continue;
            }

            if (isEntryStart(line)) {
                startNewEntry(line);
            } else if (current != null) {
                parseAndAddValuePair(line);
            }

        }
        return iniEntries;
    }

    private boolean isEntryStart(String line) {
        return line.startsWith("[") && line.endsWith("]");
    }

    private void startNewEntry(String line) {
        current = initiateEntry(line.substring(1, line.length() - 1));
        iniEntries.add(current);
        putSpecialKeyMethod("CopyFrom", this::copyFrom);
    }

    protected abstract T initiateEntry(String name);

    private String removeComments(String line) {
        if (line.contains(COMMENT_PREFIX)) {
            line = line.substring(0, line.indexOf(COMMENT_PREFIX)).trim();
        }
        return line;
    }

    private void parseAndAddValuePair(String line) {
        String[] valuePair = splitIntoValuePair(line);
        if (valuePair == null) {
            return;
        }

        BiConsumer<T, String> specialKeyMethod = specialKeyMethods.get(valuePair[0]);
        if (specialKeyMethod != null) {
            specialKeyMethod.accept(current, valuePair[1]);
            return;
        }

        BiConsumer<T, String[]> keyPrefixMethod = checkForKeyPrefixMethod(valuePair);
        if (keyPrefixMethod != null) {
            keyPrefixMethod.accept(current, valuePair);
            return;
        }

        BiConsumer<T, String[]> keySuffixMethod = checkForKeySuffixMethod(valuePair);
        if (keySuffixMethod != null) {
            keySuffixMethod.accept(current, valuePair);
            return;
        }

        addNormalValue(valuePair);
    }

    private BiConsumer<T, String[]> checkForKeyPrefixMethod(String[] valuePair) {
        for (Map.Entry<String, BiConsumer<T, String[]>> ksmEntry : keyPrefixMethods.entrySet()) {
            if (valuePair[0].startsWith(ksmEntry.getKey())) {
                return ksmEntry.getValue();
            }
        }
        return null;
    }

    private BiConsumer<T, String[]> checkForKeySuffixMethod(String[] valuePair) {
        for (Map.Entry<String, BiConsumer<T, String[]>> kfmEntry : keySuffixMethods.entrySet()) {
            if (valuePair[0].endsWith(kfmEntry.getKey())) {
                return kfmEntry.getValue();
            }
        }
        return null;
    }

    private String[] splitIntoValuePair(String line) {
        String[] valuePair = line.split("=", 2);
        if (valuePair.length == 1) {
            System.err.println("invalid line in " + fileName + "/" + current.getName() + ": " + line);
            return null;
        }
        if (valuePair[1].endsWith("\r\n")) {
            valuePair[1] = valuePair[1].substring(0, valuePair[1].length() - 2);
        }
        valuePair[1] = valuePair[1].trim();
        valuePair[0] = valuePair[0].trim();
        return valuePair;
    }

    private void addNormalValue(String[] valuePair) {
        if (isArrayValuePair(valuePair)) {
            addArrayValue(valuePair);
        } else {
            switch (defaultReadMode) {
                case INT:
                    addIntValue(valuePair);
                    break;
                case STRING:
                    addStringValue(valuePair); break;
            }
        }
    }

    private boolean isArrayValuePair(String[] valuePair) {
        return valuePair[1].startsWith("[") && valuePair[1].endsWith("]");
    }

    protected void addArrayValue(String[] valuePair) {
        addArrayValue(current, valuePair[0], valuePair[1]);
    }

    protected void addArrayValue(T entry, String[] valuePair) {
        addArrayValue(entry, valuePair[0], valuePair[1]);
    }

    protected void addArrayValue(T entry, String key, String valueString) {
        int[] value = parseArray(valueString);
        entry.putArrayValue(key, value);
    }

    protected void addIntValue(String[] valuePair) {
        addIntValue(current, valuePair[0], valuePair[1]);
    }

    protected void addIntValue(T entry, String[] valuePair) {
        addIntValue(entry, valuePair[0], valuePair[1]);
    }

    protected void addIntValue(T entry, String key, String valueString) {
        int value = parseInt(valueString);
        entry.putIntValue(key, value);
    }

    protected void addStringValue(String[] valuePair) {
        addStringValue(current, valuePair[0], valuePair[1]);
    }

    protected void addStringValue(T entry, String[] valuePair) {
        addStringValue(entry, valuePair[0], valuePair[1]);
    }

    protected void addStringValue(T entry, String key, String value) {
        entry.putStringValue(key, value);
    }

    private void copyFrom(T entry, String value) {
        for (T other : iniEntries) {
            if (matchesCopyFromValue(other, value)) {
                entry.copyFrom(other);
            }
        }
    }

    protected boolean matchesCopyFromValue(T other, String value) {
        return value.equalsIgnoreCase(other.getName());
    }


}
