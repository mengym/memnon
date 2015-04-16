package org.memnon.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by melon on 2015/4/13.
 */
public class Util {

    public static String[] split(String input, char delimiter) {
        if (input == null) throw new NullPointerException("input cannot be null");

        List<String> tokens = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(input, new String(new byte[]{(byte) delimiter}));
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken().trim());
        }
        return tokens.toArray(new String[tokens.size()]);
    }

    public static byte[] bytes(InputStream in) throws IOException {
        if (in == null) throw new IllegalArgumentException("input stream cannot be null");


        ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
        byte[] bytes = new byte[128];

        for (int x = in.read(bytes); x != -1; x = in.read(bytes))
            bout.write(bytes, 0, x);

        return bout.toByteArray();
    }

    public static String join(Collection collection, String delimiter) {
        if (collection.size() == 0) return "";
        String tmp = "";
        for (Object o : collection) {
            tmp += o + delimiter;
        }
        return tmp.substring(0, tmp.length() - delimiter.length());
    }

    public static String join(String[] collection, String delimiter) {
        return join(Arrays.asList(collection), delimiter);
    }

    public static <T> List<T> list(T... values) {
        List result = new ArrayList<T>();
        for (T value : values) {
            result.add(value);
        }
        return result;
    }

    public static boolean blank(Object value) {
        return value == null || value.toString().trim().equals("");
    }

    public static <T, K> Map<T, K> map(Object... keysAndValues) {
        if (keysAndValues.length % 2 != 0) throw new IllegalArgumentException("number of arguments must be even");

        Map<T, K> result = new HashMap<T, K>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            result.put((T) keysAndValues[i], (K) keysAndValues[i + 1]);
        }
        return result;
    }

    public static String getCauseMessage(Throwable throwable) {
        List<Throwable> list = new ArrayList<Throwable>();
        while (throwable != null && list.contains(throwable) == false) {
            list.add(throwable);
            throwable = throwable.getCause();
        }
        return list.get(0).getMessage();
    }
}
