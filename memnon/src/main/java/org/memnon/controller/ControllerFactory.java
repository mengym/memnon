package org.memnon.controller;

import org.memnon.exception.ClassLoadException;
import org.memnon.util.Configuration;
import org.memnon.util.Inflector;

/**
 * 控制器工厂类
 */
public class ControllerFactory {


    public static AppController createControllerInstance(String controllerClassName) throws ClassLoadException {
        return DynamicClassFactory.createInstance(controllerClassName, AppController.class);
    }

    public static String getControllerClassName(String controllerName, String packageSuffix) {
        String name = controllerName.replace('-', '_');
        System.out.println("RootClass = " + Configuration.getRootClass());
        String temp = Configuration.getRootClass();
        if (packageSuffix != null) {
            temp += "." + packageSuffix;
        }
        return temp + "." + Inflector.camelize(name) + "Controller";
    }

    /**
     * Expected paths: /controller, /package/controller, /package/package2/controller, /package/package2/package3/controller, etc.
     * For backwards compatibility, the  controller name alone without the preceding slash is allowed, but limits these controllers to only
     * default package: <code>app.controllers</code>
     *
     * @param controllerPath controller path.
     * @return name of controller class.
     */
    public static String getControllerClassName(String controllerPath) {

        if (!controllerPath.startsWith("/") && controllerPath.contains("/"))
            throw new IllegalArgumentException("must start with '/'");

        if (controllerPath.endsWith("/")) throw new IllegalArgumentException("must not end with '/'");

        String path = controllerPath.startsWith("/") ? controllerPath.substring(1) : controllerPath;
        String[] parts = path.split("/");

        String subPackage = null;
        String controller;
        if (parts.length == 0) {
            controller = path;
        } else if (parts.length == 1) {
            controller = parts[0];
        } else {
            subPackage = path.substring(0, path.lastIndexOf("/")).replace("/", ".");
            controller = path.substring(path.lastIndexOf("/") + 1);
        }
        String temp = Configuration.getRootPackage();
        temp += subPackage != null ? "." + subPackage : "";
        temp += "." + Inflector.camelize(controller.replace("-", "_"), true) + "Controller";
        return temp;
    }
}
