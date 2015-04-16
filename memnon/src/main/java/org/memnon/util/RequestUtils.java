package org.memnon.util;

import org.memnon.context.AppContext;
import org.memnon.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Administrator on 2015/4/15.
 */
public class RequestUtils {
    private static Logger logger = LoggerFactory.getLogger(RequestUtils.class);

    /**
     * Returns value of routing user segment, or route wild card value, or request parameter.
     * If this name represents multiple values, this  call will result in {@link IllegalArgumentException}.
     *
     * @param name name of parameter.
     * @return value of routing user segment, or route wild card value, or request parameter.
     */
    public static String param(String name) {
        if (name.equals("id")) {
            return getId();
        } else if (Context.getRequestContext().getUserSegments().get(name) != null) {
            return Context.getRequestContext().getUserSegments().get(name);
        } else if (Context.getRequestContext().getWildCardName() != null
                && name.equals(Context.getRequestContext().getWildCardName())) {
            return Context.getRequestContext().getWildCardValue();
        } else {
            return Context.getQueryMap().get(name);
        }
    }

    /**
     * Returns value of ID if one is present on a URL. Id is usually a part of a URI, such as: <code>/controller/action/id</code>.
     * This depends on a type of a URI, and whether controller is RESTful or not.
     *
     * @return ID value from URI is one exists, null if not.
     */
    public static String getId() {
        String paramId = Context.getQueryMap().get("id");
        return Util.blank(paramId) ? null : paramId;
    }


    /**
     * Returns a format part of the URI, or null if URI does not have a format part.
     * A format part is defined as part of URI that is trailing after a last dot, as in:
     * <p/>
     * <code>/books.xml</code>, here "xml" is a format.
     *
     * @return format part of the URI, or nul if URI does not have it.
     */
    public static String format() {
        return Context.getFormat();
    }


    /**
     * Returns instance of {@link AppContext}.
     *
     * @return instance of {@link AppContext}.
     */
    public static AppContext appContext() {
        return Context.getAppContext();
    }

    /**
     * Returns a map where keys are names of all parameters, while values are the first value for each parameter, even
     * if such parameter has more than one value submitted.
     *
     * @return a map where keys are names of all parameters, while values are first value for each parameter, even
     * if such parameter has more than one value submitted.
     */
    public static Map<String, String> params1st() {
        return Context.getQueryMap();
    }

}
