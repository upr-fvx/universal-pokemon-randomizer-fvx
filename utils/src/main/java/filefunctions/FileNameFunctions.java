package filefunctions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Functions relating to file names.
 */
public class FileNameFunctions {

    public static File fixFilename(File original, String defaultExtension) {
        return fixFilename(original, defaultExtension, new ArrayList<>());
    }

    // Behavior:
    // if file has no extension, add defaultExtension
    // if there are banned extensions & file has a banned extension, replace
    // with defaultExtension
    // else, leave as is
    public static File fixFilename(File original, String defaultExtension, List<String> bannedExtensions) {
        String absolutePath = original.getAbsolutePath();
        for (String bannedExtension: bannedExtensions) {
            if (absolutePath.endsWith("." + bannedExtension)) {
                absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf('.') + 1) + defaultExtension;
                break;
            }
        }
        if (!absolutePath.endsWith("." + defaultExtension)) {
            absolutePath += "." + defaultExtension;
        }
        return new File(absolutePath);
    }

}
