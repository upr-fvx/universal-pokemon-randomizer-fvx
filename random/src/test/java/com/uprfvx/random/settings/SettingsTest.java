package com.uprfvx.random.settings;

import com.uprfvx.random.settings.definitions.SettingDefinition;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.uprfvx.random.settings.Settings.ALL_SETTINGS;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A class for tests on the set of SettingDefinitions.
 * Should test not the code, but the defined values.
 */
public class SettingsTest {

    @Test
    public void canTestEverySettingEnabledWithoutException() {
        SettingsManager manager = new SettingsManager();
        Collection<SettingState<?>> allSettings = manager.testGetAllSettings();

        for (SettingState<?> setting : allSettings) {
            try {
                setting.currentValueIsEnabled(manager);
            } catch (Exception e) {
                System.out.println("Setting had exception: " + setting.getDefinition().getName());
                throw e;
            }
        }
    }

    @Test
    public void allSettingsDefaultValuesAreInitiallyEnabled() {
        SettingsManager manager = new SettingsManager();
        Collection<SettingState<?>> allSettings = manager.testGetAllSettings();

        boolean passing = true;
        for (SettingState<?> setting : allSettings) {
            if(!setting.currentValueIsEnabled(manager)) {
                System.out.println("Initial value disabled for: " + setting.getDefinition().getName());
                passing = false;
            }
        }
        assertTrue(passing);
    }

    @Test
    public void everySettingHasUniqueName() {
        Map<Settings.Name, Settings.Category> namesToCategories = new HashMap<>();
        boolean passing = true;
        for (SettingDefinition<?> setting : ALL_SETTINGS) {
            if(namesToCategories.containsKey(setting.getName())) {
                passing = false;
                System.out.println("Duplicate setting name: " + setting.getName());
                System.out.println("Categories: " + setting.getCategory() + ", " + namesToCategories.get(setting.getName()));
            }
            namesToCategories.put(setting.getName(), setting.getCategory());
        }

        assertTrue(passing);
    }

    @Test
    public void noSettingHasInitialNullValue() {
        SettingsManager manager = new SettingsManager();
        Collection<SettingState<?>> allSettings = manager.testGetAllSettings();

        boolean passing = true;
        for (SettingState<?> setting : allSettings) {
            if(setting.getValue() == null) {
                System.out.println("Initial value is null for: " + setting.getDefinition().getName());
                passing = false;
            }
        }
        assertTrue(passing);
    }

    /**
     * Prints any Setting requirement loops. I.e. settings that somehow depend on their own state.
     * These are not inherently bad, the rest of the code knows how to handle them, but can be good to know about.
     */
    @Test
    public void printSettingRequirementLoops() {
        System.out.println("Setting requirement loops:");

        for (SettingDefinition<?> root : ALL_SETTINGS) {
            Set<SettingDefinition<?>> visited = new HashSet<>();
            boolean loop = false;
            Map<SettingDefinition<?>, SettingDefinition<?>> path = new HashMap<>();

            List<SettingDefinition<?>> queue = new LinkedList<>();
            queue.add(root);
            while (!queue.isEmpty()) {
                SettingDefinition<?> node = queue.removeFirst();
                visited.add(node);

                List<? extends SettingDefinition<?>> children = node.getSettingsDependentOn().stream()
                        .map(this::nameToSetting).toList();
                for (SettingDefinition<?> child : children) {
                    path.putIfAbsent(child, node);
                    queue.add(child);

                    if (child == root) {
                        loop = true;
                        queue.clear();
                    } else if (visited.contains(child)) { // also a loop, but we'll properly report it later
                        queue.clear();
                    }
                }
            }

            if (loop) {
                System.out.println(root.getName() + " depends on itself.");
                SettingDefinition<?> parent = path.get(root);
                do {
                    System.out.println("-> " + parent.getName());
                    parent = path.get(parent);
                } while (!parent.equals(root));
                System.out.println();

                queue.clear();
            }
        }
    }

    private SettingDefinition<?> nameToSetting(Settings.Name name) {
        for (SettingDefinition<?> setting : ALL_SETTINGS) {
            if (setting.getName() == name) {
                return setting;
            }
        }
        throw new IllegalArgumentException("No setting with name " + name);
    }
}
