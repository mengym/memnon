package org.memnon.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.memnon.util.Util.bytes;

/**
 * Always loads a class from a file. No caching of any kind, used in development mode only.
 */
public class DynamicClassLoader extends ClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(DynamicClassLoader.class);
    private String baseDir;


    DynamicClassLoader(ClassLoader parent, String baseDir) {
        super(parent);
        this.baseDir = baseDir;
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {

        try {
            if (name.startsWith("org.javalite.activeweb")) {
                return loadByParent(name);
            }

            if (name.endsWith("Controller") || name.contains("Controller$")
                    || name.equals("app.config.RouteConfig")) {

                String pathToClassFile = name.replace('.', '/') + ".class";

                byte[] classBytes = bytes(getResourceAsStream(pathToClassFile));
                Class<?> daClass = defineClass(name, classBytes, 0, classBytes.length);

                logger.debug("Loaded class: " + name);
                return daClass;
            } else {
                return loadByParent(name);
            }
        } catch (Exception e) {
            logger.debug("Failed to dynamically load class: " + name + ". Loading by parent class loader.");
            return loadByParent(name);
        }
    }

    private Class<?> loadByParent(String name) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        try {
            String pathToFile = baseDir + System.getProperty("file.separator") + name;
            return new FileInputStream(pathToFile);
        } catch (Exception e) {
            return super.getResourceAsStream(name);
        }
    }
}
