package org.memnon.context;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/4/9.
 */
public class RequestContext {
    private Map<String, Object> values = new HashMap<String, Object>();

    /**
     * User segments are values extracted from the URL if user segments were used on a URL,
     * They are available as regular parameters using param("name") inside controllers and filter.
     */
    private Map<String, String> userSegments = new HashMap<String, String>();


    protected Object get(String name) {
        return values.get(name);
    }

    public Map<String, String> getUserSegments() {
        return userSegments;
    }

    protected void set(String name, Object value) {
        values.put(name, value);
    }


    private String wildCardName, wildCardValue;

    public String getWildCardName() {
        return wildCardName;
    }

    public String getWildCardValue() {
        return wildCardValue;
    }

    public void setWildCardName(String wildCardName) {
        this.wildCardName = wildCardName;
    }

    public void setWildCardValue(String wildCardValue) {
        this.wildCardValue = wildCardValue;
    }
}
