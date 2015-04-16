package org.memnon.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by Administrator on 2015/4/13.
 */
public class Inflector {
    private static List<String[]> singulars, plurals, irregulars;
    private static List<String> uncountables;

    static {
        singulars = new ArrayList<String[]>();
        plurals = new ArrayList<String[]>();
        irregulars = new ArrayList<String[]>();
        uncountables = new ArrayList<String>();

        addPlural("$", "s");
        addPlural("s$", "s");
        addPlural("(ax|test)is$", "$1es");
        addPlural("(octop|vir)us$", "$1i");
        addPlural("(alias|status)$", "$1es");
        addPlural("(bu)s$", "$1ses");
        addPlural("(buffal|tomat)o$", "$1oes");
        addPlural("([ti])um$", "$1a");
        addPlural("sis$", "ses");
        addPlural("(?:([^f])fe|([lr])f)$", "$1$2ves");
        addPlural("(hive)$", "$1s");
        addPlural("([^aeiouy]|qu)y$", "$1ies");
        addPlural("(x|ch|ss|sh)$", "$1es");
        addPlural("(matr|vert|ind)(?:ix|ex)$", "$1ices");
        addPlural("([m|l])ouse$", "$1ice");
        addPlural("^(ox)$", "$1en");
        addPlural("(quiz)$", "$1zes");


        addSingular("s$", "");
        addSingular("(n)ews$", "$1ews");
        addSingular("([ti])a$", "$1um");
        addSingular("((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$", "$1sis");
        addSingular("(^analy)ses$", "$1sis");
        addSingular("([^f])ves$", "$1fe");
        addSingular("(hive)s$", "$1");
        addSingular("(tive)s$", "$1");
        addSingular("([lr])ves$", "$1f");
        addSingular("([^aeiouy]|qu)ies$", "$1y");
        addSingular("(s)eries$", "$1eries");
        addSingular("(m)ovies$", "$1ovie");
        addSingular("(x|ch|ss|sh)es$", "$1");
        addSingular("([m|l])ice$", "$1ouse");
        addSingular("(bus)es$", "$1");
        addSingular("(o)es$", "$1");
        addSingular("(shoe)s$", "$1");
        addSingular("(cris|ax|test)es$", "$1is");
        addSingular("(octop|vir)i$", "$1us");
        addSingular("(alias|status)es$", "$1");
        addSingular("^(ox)en", "$1");
        addSingular("(vert|ind)ices$", "$1ex");
        addSingular("(matr)ices$", "$1ix");
        addSingular("(quiz)zes$", "$1");
        addSingular("(database)s$", "$1");

        addIrregular("person", "people");
        addIrregular("man", "men");
        addIrregular("child", "children");
        addIrregular("sex", "sexes");
        addIrregular("move", "moves");

        uncountables = Arrays.asList("equipment", "information", "rice", "money", "species", "series", "fish", "sheep");
    }

    public static void addPlural(String rule, String replacement) {
        plurals.add(0, new String[]{rule, replacement});
    }

    public static void addSingular(String rule, String replacement) {
        singulars.add(0, new String[]{rule, replacement});
    }

    public static void addIrregular(String rule, String replacement) {
        irregulars.add(new String[]{rule, replacement});
    }

    public static String camelize(String underscore) {
        return camelize(underscore, true);
    }

    public static String camelize(String underscore, boolean capitalizeFirstChar) {
        String result = "";
        StringTokenizer st = new StringTokenizer(underscore, "_");
        while (st.hasMoreTokens()) {
            result += capitalize(st.nextToken());
        }
        return capitalizeFirstChar ? result : result.substring(0, 1).toLowerCase() + result.substring(1);
    }

    public static String capitalize(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    public static String underscore(String camel) {
        List<Integer> upper = new ArrayList<Integer>();
        byte[] bytes = camel.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            if (b < 97 || b > 122) {
                upper.add(i);
            }
        }
        StringBuffer b = new StringBuffer(camel);
        for (int i = upper.size() - 1; i >= 0; i--) {
            Integer index = upper.get(i);
            if (index != 0)
                b.insert(index, "_");
        }
        return b.toString().toLowerCase();
    }
}
