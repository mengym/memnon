package org.memnon.context;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class AppContext {
    private Map context = new HashMap();


    /**
     * Retrieves object by name
     *
     * @param name name of object
     * @return object by name;
     */
    public Object get(String name) {
        return context.get(name);
    }

    /**
     * Retrieves object by name. Convenience generic method.
     *
     * @param name name of object
     * @param type type requested.
     * @return object by name
     */
    public <T> T get(String name, Class<T> type) {
        Object o = context.get(name);
        return o == null ? null : (T) o;
    }

    /**
     * Sets an application - wide object by name.
     *
     * @param name   name of object
     * @param object - instance.
     */
    public void set(String name, Object object) {
        context.put(name, object);
    }
}
