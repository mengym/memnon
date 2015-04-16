package org.memnon.config;

import org.memnon.context.AppContext;
import org.memnon.controllers.HelloController;
import org.memnon.route.AbstractRouteConfig;

public class RouteConfig extends AbstractRouteConfig {
    public void init(AppContext appContext) {
        route("/hello").to(HelloController.class).action("hello");
        route("/{action}/{controller}/{id}");
        route("/package/package/{action}/{controller}/{id}");
//        route("/search").to(SearchController.class);
//        route("/abc/xxx/123").to().action("");
//        route("/{action}/greeting/{name}").to(HelloController.class);
    }
}