package org.memnon.route;

import java.util.ArrayList;
import java.util.List;

/**
 * 路由控制器抽象类
 */
public abstract class AbstractRouteConfig extends AppConfig {
    private List<RouteBuilder> routes = new ArrayList<RouteBuilder>();

    //ignore some URLs
    private List<IgnoreSpec> ignoreSpecs = new ArrayList<IgnoreSpec>();

    public RouteBuilder route(String route) {
        RouteBuilder matchedRoute = new RouteBuilder(route);
        routes.add(matchedRoute);
        return matchedRoute;
    }

    public void clear() {
        routes = new ArrayList<RouteBuilder>();
    }

    public List<RouteBuilder> getRoutes() {
        return routes;
    }


    public final List<IgnoreSpec> getIgnoreSpecs() {
        return ignoreSpecs;
    }
}
