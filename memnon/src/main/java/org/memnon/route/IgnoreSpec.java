package org.memnon.route;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by melon on 2015/4/10.
 */
public class IgnoreSpec {
    private List<Pattern> ignorePatterns = new ArrayList<Pattern>();

    IgnoreSpec(String[] ignores) {
        for (String ignore : ignores) {
            ignorePatterns.add(Pattern.compile(ignore));
        }
    }

    protected boolean ignores(String path) {
        boolean matches = false;
        for (Pattern pattern : ignorePatterns) {
            Matcher m = pattern.matcher(path);
            matches = m.matches();
        }
        return matches;
    }
}
