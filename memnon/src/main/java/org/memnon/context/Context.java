package org.memnon.context;

import org.httpkit.HttpMethod;
import org.memnon.controller.ControllerRegistry;
import org.memnon.controller.response.ControllerResponse;
import org.memnon.route.Route;

import java.util.HashMap;
import java.util.Map;

/**
 * 上下文内存
 */
public class Context {

    private static ThreadLocal<String> path = new ThreadLocal<String>();
    private static ThreadLocal<Map<String, String>> queryMap = new ThreadLocal<Map<String, String>>();
    private static ThreadLocal<ControllerRegistry> registry = new ThreadLocal<ControllerRegistry>();
    private static ThreadLocal<RequestContext> requestContext = new ThreadLocal<RequestContext>();
    private static ThreadLocal<AppContext> appContext = new ThreadLocal<AppContext>();
    private static ThreadLocal<String> format = new ThreadLocal<String>();
    private static ThreadLocal<Route> route = new ThreadLocal<Route>();
    private static ThreadLocal<Map<String, Object>> values = new ThreadLocal<Map<String, Object>>();
    private static ThreadLocal<ControllerResponse> controllerResponse = new ThreadLocal<ControllerResponse>();
    private static ThreadLocal<HttpMethod> method = new ThreadLocal<HttpMethod>();

    public static ControllerRegistry getControllerRegistry() {
        return registry.get();
    }

    public static void setControllerRegistry(ControllerRegistry controllerRegistry) {
        registry.set(controllerRegistry);
    }

    public static void setTLs(String path, Map<String, String> queryMap, ControllerRegistry reg, AppContext context, RequestContext requestContext, String format, HttpMethod method) {
        setPath(path);
        setQueryMap(queryMap);
        setControllerRegistry(reg);
        setAppContext(context);
        setRequestContext(requestContext);
        setFormat(format);
        setHttpMethod(method);
    }

    public static void clear() {
        registry.set(null);
        path.set(null);
        requestContext.set(null);
        format.set(null);
        appContext.set(null);
    }

    public static void setRoute(Route route) throws InstantiationException, IllegalAccessException {
        if (route == null)
            throw new IllegalArgumentException("Route could not be null");
        if (route.getId() != null) {
            getQueryMap().put("id", route.getId());
        }

        if (route.isWildCard()) {
            requestContext.get().setWildCardName(route.getWildCardName());
            requestContext.get().setWildCardValue(route.getWildCardValue());
        }
        Context.route.set(route);
        Context.values.set(new HashMap<String, Object>());
    }

    public static String getPath() {
        return path.get();
    }

    public static void setPath(String format) {
        Context.path.set(format);
    }

    public static AppContext getAppContext() {
        return appContext.get();
    }

    public static void setAppContext(AppContext appContext) {
        Context.appContext.set(appContext);
    }

    public static RequestContext getRequestContext() {
        return requestContext.get();
    }

    public static void setRequestContext(RequestContext requestContext) {
        Context.requestContext.set(requestContext);
    }

    public static String getFormat() {
        return format.get();
    }

    public static void setFormat(String format) {
        Context.format.set(format);
    }

    public static ControllerResponse getControllerResponse() {
        return controllerResponse.get();
    }

    public static void setControllerResponse(ControllerResponse resp) {
        controllerResponse.set(resp);
    }

    public static HttpMethod getHttpMethod() {
        return method.get();
    }

    public static void setHttpMethod(HttpMethod httpMethod) {
        method.set(httpMethod);
    }

    public static Map<String, String> getQueryMap() {
        return queryMap.get();
    }

    public static void setQueryMap(Map<String, String> query) {
        queryMap.set(query);
    }
}
