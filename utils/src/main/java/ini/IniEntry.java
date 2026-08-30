package ini;

import java.util.HashMap;
import java.util.Map;

/**
 * An abstract class for any kind of "entries" which can be expected to be read from .ini files.<br>
 * The format is a non-standard one, NOT to be confused with TOML or YAML even though it may look
 * similar at a glance.
 */
public abstract class IniEntry {

    protected String name;
    protected Map<String, Integer> intValues = new HashMap<>();
    protected Map<String, String> stringValues = new HashMap<>();
    protected Map<String, int[]> arrayValues = new HashMap<>();

    public IniEntry(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getIntValue(String key) {
        if (!intValues.containsKey(key)) {
            intValues.put(key, 0);
        }
        return intValues.get(key);
    }

    public void putIntValue(String key, int value) {
        intValues.put(key, value);
    }

    public String getStringValue(String key) {
        if (!stringValues.containsKey(key)) {
            stringValues.put(key, "");
        }
        return stringValues.get(key);
    }

    public void putStringValue(String key, String value) {
        stringValues.put(key, value);
    }

    public int[] getArrayValue(String key) {
        if (!arrayValues.containsKey(key)) {
            arrayValues.put(key, new int[0]);
        }
        return arrayValues.get(key);
    }

    public void putArrayValue(String key, int[] value) {
        arrayValues.put(key, value);
    }

    public void copyFrom(IniEntry other) {
        intValues.putAll(other.intValues);
        stringValues.putAll(other.stringValues);
        arrayValues.putAll(other.arrayValues);
    }
}
