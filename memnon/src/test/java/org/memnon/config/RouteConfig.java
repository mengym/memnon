package org.memnon.config;

import org.memnon.context.AppContext;
import org.memnon.controllers.HelloController;
import org.memnon.route.AbstractRouteConfig;

public class RouteConfig extends AbstractRouteConfig {
    public void init(AppContext appContext) {
        route("/abc").to(HelloController.class).action("hello");
//        route("/search").to(SearchController.class);
//        route("/{action}/{controller}/{id}");
//        route("/abc/xxx/123").to().action("");
//        route("/{action}/greeting/{name}").to(HelloController.class);
    }
}