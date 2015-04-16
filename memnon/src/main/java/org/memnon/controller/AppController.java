package org.memnon.controller;

import org.httpkit.HttpMethod;
import org.memnon.annotations.RESTful;
import org.memnon.exception.ActionNotFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2015/4/9.
 */
public class AppController extends HttpSupport {

    /**
     * Returns true if this controller is configured to be {@link org.memnon.annotations.RESTful}.
     *
     * @return true if this controller is restful, false if not.
     */
    public boolean restful() {
        return getClass().getAnnotation(RESTful.class) != null;
    }

    /**
     * Checks if the action supports an HTTP method, according to its configuration.
     *
     * @param actionMethodName name of action method.
     * @param httpMethod       http method
     * @return true if supports, false if does not.
     */
    public boolean actionSupportsHttpMethod(String actionMethodName, HttpMethod httpMethod) {
        return standardActionSupportsHttpMethod(actionMethodName, httpMethod);
    }

    protected boolean standardActionSupportsHttpMethod(String actionMethodName, HttpMethod httpMethod) {
        for (HttpMethod m : allowedActions(actionMethodName)) {
            if (m == httpMethod)
                return true;
        }
        return false;
    }

    protected List<HttpMethod> allowedActions(String actionMethodName) {
        try {
            Method method = getClass().getMethod(actionMethodName);
            Annotation[] annotations = method.getAnnotations();

            //default behavior: GET method!
            if (annotations.length == 0) {
                return Collections.singletonList(HttpMethod.GET);
            } else {
                List<HttpMethod> res = new ArrayList<HttpMethod>();
                for (Annotation annotation : annotations) {
                    res.add(HttpMethod.valueOf(annotation.annotationType().getSimpleName()));
                }
                return res;
            }
        } catch (NoSuchMethodException e) {
            throw new ActionNotFoundException(e);
        }
    }

}
