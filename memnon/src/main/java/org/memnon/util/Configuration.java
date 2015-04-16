package org.memnon.util;

import org.memnon.exception.InitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

import static org.memnon.util.Util.blank;

/**
 * Created by melon on 2015/4/13.
 * 配置文件加载器
 */
public class Configuration {

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private static final Configuration instance = new Configuration();

    private static Properties props = new Properties();

    private static boolean activeReload = !blank(System.getProperty("activeReload")) && System.getProperty("activeReload").equals("true");

    enum Params {
        serverHost, serverPort, rootPackage, activeReload, rootConfig, controllerConfig
//        templateManager, bootstrap, defaultLayout, targetDir, rootPackage, dbconfig, controllerConfig, rollback,
//        freeMarkerConfig, route_config, maxUploadSize,rootPackage
    }

    static {
        try {
            //load config
            props = new Properties();
            InputStream in1 = Configuration.class.getResourceAsStream("/memnon.properties");
            props.load(in1);

            checkInitProperties();
        } catch (Exception e) {
            throw new InitException(e);
        }
    }

    private static void checkInitProperties() {
        for (Params param : Params.values()) {
            if (props.get(param.toString()) == null) {
                throw new InitException("Must provide property: " + param);
            }
        }
    }

    public static String get(String name) {
        return props.getProperty(name);
    }

    public static String getServerHost() {
        return get(Params.serverHost.toString());
    }

    public static String getServerPort() {
        return get(Params.serverPort.toString());
    }

    public static String getRootPackage() {
        return get(Params.rootPackage.toString());
    }

    public static boolean getActiveReload() {
        return !blank(get("activeReload")) && get("activeReload").equals("true");
    }

    public static String getRootClass() {
//        return get(Params.rootPackage.toString()).replace(".", System.getProperty("file.separator"));
        return get(Params.rootPackage.toString());
    }

    public static String getRouteConfigClassName() {
        return get(Params.rootConfig.toString());
    }

    public static String getControllerConfigClassName() {
        return get(Params.controllerConfig.toString());
    }
}
