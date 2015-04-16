package org.memnon.route;

import org.httpkit.HttpMethod;
import org.memnon.context.Context;
import org.memnon.controller.AppController;
import org.memnon.controller.ControllerRegistry;
import org.memnon.exception.ClassLoadException;
import org.memnon.exception.ControllerException;
import org.memnon.exception.RouteException;
import org.memnon.util.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.memnon.util.Util.map;
import static org.memnon.controller.ControllerFactory.getControllerClassName;
import static org.memnon.controller.ControllerFactory.createControllerInstance;

/**
 * Created by Melon on 2015/4/9.
 */
public class Router {
    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    public static final String CONTROLLER_NAME = "controller_name";
    public static final String PACKAGE_SUFFIX = "package_suffix";

    private String rootControllerName;
    private List<RouteBuilder> routes = new ArrayList<RouteBuilder>();
    private List<IgnoreSpec> ignoreSpecs;

    public Router(String rootControllerName) {
        this.rootControllerName = rootControllerName;
    }

    /**
     * Sets custom routes
     *
     * @param routes se of custom routes defined for app.
     */
    public void setRoutes(List<RouteBuilder> routes) {
        this.routes = routes;
    }

    /**
     * Finds a controller path from URI. Controller path includes a package prefix taken from URI, similar to:
     * <p/>
     * <code>http://host/context/admin/printers/show/1</code>, where "admin" is a "package_suffix", "printers" is a
     * "controller_name".
     * <p/>
     * for example above, the method will Map with two keys: "package_suffix" and "controller_name"
     *
     * @param uri this is a URI - the information after context : "controller/action/whatever".
     * @return map with two keys: "controller_name" and "package_suffix", both of which can be null.
     */
    protected Map<String, String> getControllerPath(String uri) {

        //判断是否是根路径
        boolean rootPath = uri.equals("/");

        //判断是否有根控制器
        boolean useRootController = rootPath && rootControllerName != null;

        if (useRootController) {
            return map(CONTROLLER_NAME, rootControllerName);
        } else if (rootControllerName == null && rootPath) {
            logger.warn("URI is: '/', but root controller not set");
            return new HashMap<String, String>();
        } else {
            //用包路径来配置路径
            String pack;
            if ((pack = findPackagePrefix(uri)) != null) {
                String controllerName = findControllerNamePart(pack, uri);
                return map(CONTROLLER_NAME, controllerName, Router.PACKAGE_SUFFIX, pack);
            } else {
                return map(CONTROLLER_NAME, uri.split("/")[1]);//no package suffix
            }
        }
    }


    /**
     * Generates a path to a controller based on its package and class name. The path always starts with a slash: "/".
     * Examples:
     * <p/>
     * <ul>
     * <li>For class: <code>app.controllers.Simple</code> the path will be: <code>/simple</code>.</li>
     * <li>For class: <code>app.controllers.admin.PeopleAdmin</code> the path will be: <code>/admin/people_admin</code>.</li>
     * <li>For class: <code>app.controllers.admin.simple.PeopleAdmin</code> the path will be: <code>/admin/simple/people_admin</code>.</li>
     * </ul>
     * <p/>
     * Class name looses the "Controller" suffix and gets converted to underscore format, while packages stay unchanged.
     *
     * @param controllerClass class of a controller.
     * @return standard path for a controller.
     */
    static <T extends AppController> String getControllerPath(Class<T> controllerClass) {
        String simpleName = controllerClass.getSimpleName();
        if (!simpleName.endsWith("Controller")) {
            throw new ControllerException("controller name must end with 'Controller' suffix");
        }

        String className = controllerClass.getName();
        if (!className.startsWith("app.controllers")) {
            throw new ControllerException("controller must be in the 'app.controllers' package");
        }
        String packageSuffix = className.substring("app.controllers".length(), className.lastIndexOf("."));
        packageSuffix = packageSuffix.replace(".", "/");
        if (packageSuffix.startsWith("/"))
            packageSuffix = packageSuffix.substring(1);

        return (packageSuffix.equals("") ? "" : "/" + packageSuffix) + "/" + Inflector.underscore(simpleName.substring(0, simpleName.lastIndexOf("Controller")));
    }

    /**
     * Now that we know that this controller is under a package, need to find the controller short name.
     *
     * @param pack part of the package of the controller, taken from URI: value between "app.controllers" and controller name.
     * @param uri  uri from request
     * @return controller name
     */
    protected static String findControllerNamePart(String pack, String uri) {
        String temp = uri.startsWith("/") ? uri.substring(1) : uri;
        temp = temp.replace("/", ".");
        if (temp.length() > pack.length())
            temp = temp.substring(pack.length() + 1);

        if (temp.equals("") || temp.equals(pack))
            throw new ControllerException("You defined a controller package '" + pack + "', but this request does not specify controller name");

        return temp.split("\\.")[0];
    }

    /**
     * Finds a part of a package name which can be found in between "app.controllers" and short name of class.
     *
     * @param uri uri from request
     * @return a part of a package name which can be found in between "app.controllers" and short name of class, or null
     * if not found
     */
    protected String findPackagePrefix(String uri) {
        String temp = uri.startsWith("/") ? uri.substring(1) : uri;
        temp = temp.replace(".", "_");
        temp = temp.replace("/", ".");

        //find all matches
        List<String> candidates = new ArrayList<String>();
        ControllerRegistry r = Context.getControllerRegistry();


        for (String pack : Context.getControllerRegistry().getControllerPackages()) {
            if (temp.startsWith(pack)) {
                candidates.add(pack);
            }
        }
        int resultIndex = 0;
        int size = 0;
        //find the longest package
        for (int i = 0; i < candidates.size(); i++) {
            String candidate = candidates.get(i);
            if (candidate.length() > size) {
                size = candidate.length();
                resultIndex = i;
            }
        }
        return candidates.size() > 0 ? candidates.get(resultIndex) : null;
    }

    public void setIgnoreSpecs(List<IgnoreSpec> ignoreSpecs) {
        this.ignoreSpecs = ignoreSpecs;
    }

    /**
     * 这个是主方法，用来识别路由具体指向的哪个控制器
     *
     * @param uri        请求路径
     * @param httpMethod 请求method
     * @return 单路由实例
     */
    public Route recognize(String uri, HttpMethod httpMethod) throws ClassLoadException {
        //自定义路由匹配
        Route route = matchCustom(uri, httpMethod);

        //没有匹配上，用其它匹配方式
        if (route == null) { //proceed to built-in routes
            //DTO as map here
            logger.debug("-----------------------------");
            Map<String, String> controllerPath = getControllerPath(uri);

            String controllerName = controllerPath.get(Router.CONTROLLER_NAME);
            String packageSuffix = controllerPath.get(Router.PACKAGE_SUFFIX);

            logger.debug("controllerName = " + controllerName);
            logger.debug("packageSuffix = " + packageSuffix);
            if (controllerName == null) {
                return null;
            }
            String controllerClassName = getControllerClassName(controllerName, packageSuffix);
            logger.debug("controllerClassName = " + controllerClassName);
            AppController controller = createControllerInstance(controllerClassName);

            logger.debug("controller = " + controller);
            if (uri.equals("/") && rootControllerName != null && httpMethod.equals(HttpMethod.GET)) {
                route = new Route(controller, "index");
            } else {
                route = controller.restful() ? matchRestful(uri, controllerName, packageSuffix, httpMethod, controller) :
                        matchStandard(uri, controllerName, packageSuffix, controller);
            }
        }

        //加载忽略标示
        if (route != null) {
            route.setIgnoreSpecs(ignoreSpecs);
        } else {
            throw new RouteException("Failed to map resource to URI: " + uri);
        }
        return route;
    }

    /**
     * 匹配自定义路由
     *
     * @param uri        请求路径
     * @param httpMethod 请求method
     * @return 单路由实例
     * @throws ClassLoadException 类加载异常
     */
    private Route matchCustom(String uri, HttpMethod httpMethod) throws ClassLoadException {
        for (RouteBuilder builder : routes) {
            if (builder.matches(uri, httpMethod)) {
                //匹配成功则返回匹配路由
                return new Route(builder);
            }
        }
        return null;
    }

    /**
     * Will match a restful route.
     *
     * @param uri            request URI
     * @param controllerName name of controller
     * @param packageSuffix  package suffix or null if none. .
     * @param httpMethod     http method of a request.
     * @return instance of a <code>Route</code> if one is found, null if not.
     */
    private Route matchRestful(String uri, String controllerName, String packageSuffix, HttpMethod httpMethod, AppController controller) {

        String theUri = uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
        String controllerPath = (packageSuffix != null ? "/" + packageSuffix.replace(".", "/") : "") + "/" + controllerName;
        String tail = theUri.length() > controllerPath.length() ? theUri.substring(controllerPath.length() + 1) : "";
        String[] parts = split(tail, "/");

        //GET 	/photos 	            index 	display a list of all photos
        if (controllerPath.equals(theUri) && httpMethod.equals(HttpMethod.GET)) {
            return new Route(controller, "index");
        }

        //GET 	/photos/new_form 	    new_form        return an HTML form for creating a new photo
        if (parts.length == 1 && httpMethod.equals(HttpMethod.GET) && parts[0].equalsIgnoreCase("new_form")) {
            return new Route(controller, "new_form");
        }

        //POST 	/photos 	            create 	        create a new photo
        if (parts.length == 0 && httpMethod.equals(HttpMethod.POST)) {
            return new Route(controller, "create");
        }

        //GET 	/photos/id 	        show            display a specific photo
        if (parts.length == 1 && httpMethod.equals(HttpMethod.GET)) {
            return new Route(controller, "show", parts[0]);
        }

        //GET 	/photos/id/edit_form   edit_form 	    return an HTML form for editing a photo
        if (parts.length == 2 && httpMethod.equals(HttpMethod.GET) && parts[1].equalsIgnoreCase("edit_form")) {
            return new Route(controller, "edit_form", parts[0]);
        }

        //PUT 	/photos/id 	        update          update a specific photo
        if (parts.length == 1 && httpMethod.equals(HttpMethod.PUT)) {
            return new Route(controller, "update", parts[0]);
        }

        //DELETE 	/photos/id 	        destroy         delete a specific photo
        if (parts.length == 1 && httpMethod.equals(HttpMethod.DELETE)) {
            return new Route(controller, "destroy", parts[0]);
        }
        logger.warn("Failed to find action for request: " + uri);
        return null;
    }

    //todo: write a regexp one day
    private static String[] split(String value, String delimeter) {
        StringTokenizer st = new StringTokenizer(value, delimeter);
        String[] res = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++) {
            res[i] = st.nextToken();
        }
        return res;
    }

    /**
     * Will match a standard, non-restful route.
     *
     * @param uri            request URI
     * @param controllerName name of controller
     * @param packageSuffix  package suffix or null if none. .
     * @return instance of a <code>Route</code> if one is found, null if not.
     */
    private Route matchStandard(String uri, String controllerName, String packageSuffix, AppController controller) {

        String controllerPath = (packageSuffix != null ? "/" + packageSuffix.replace(".", "/") : "") + "/" + controllerName;
        String theUri = uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;

        //ANY    /package_suffix/controller
        if (controllerPath.length() == theUri.length()) {
            return new Route(controller, "index");
        }

        String tail = theUri.substring(controllerPath.length() + 1);
        String[] parts = split(tail, "/");

        //ANY    /package_suffix/controller/action
        if (parts.length == 1) {
            return new Route(controller, parts[0]);
        }

        //ANY    /package_suffix/controller/action/id/
        if (parts.length == 2) {
            return new Route(controller, parts[0], parts[1]);
        }
        logger.warn("Failed to find action for request: " + uri);
        return null;
    }
}
