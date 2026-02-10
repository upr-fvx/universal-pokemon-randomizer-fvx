package text;

/*----------------------------------------------------------------------------*/
/*--  UnicodeParser.java - maintains the poke<->unicode text table          --*/
/*--  Code loosely derived from "thenewpoketext", copyright (C) loadingNOW  --*/
/*--  Ported to Java and customized by Dabomstew                            --*/
/*----------------------------------------------------------------------------*/

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class UnicodeParser {

    private static final String TABLE_PATH = "text/Generation4.tbl";

    public static String[] tb = new String[65536];
    public static Map<String, Integer> d = new HashMap<>();

    static {
        InputStream is = UnicodeParser.class.getResourceAsStream(TABLE_PATH);
        if (is == null) {
            throw new RuntimeException("Couldn't find " + TABLE_PATH);
        }
        Scanner sc = new Scanner(is, "UTF-8");
        while (sc.hasNextLine()) {
            String q = sc.nextLine();
            if (!q.trim().isEmpty()) {
                String[] r = q.split("=", 2);
                if (r[1].endsWith("\r\n")) {
                    r[1] = r[1].substring(0, r[1].length() - 2);
                }
                tb[Integer.parseInt(r[0], 16)] = r[1];
                d.put(r[1], Integer.parseInt(r[0], 16));
            }
        }
        sc.close();
    }

}
