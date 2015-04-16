package org.memnon.controller;

import org.memnon.controller.filter.FilterConfig;
import org.memnon.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 控制器本地加载
 */
public class ControllerPackageLocator {

    private static final Logger logger = LoggerFactory.getLogger(ControllerPackageLocator.class);

    /**
     * 获取本地控制器所在包
     *
     * @return List
     */
    public static List<String> locateControllerPackages() {
        String controllerPath = System.getProperty("file.separator") + Configuration.getRootPackage() + System.getProperty("file.separator") + "controllers";
        List<String> controllerPackages = new ArrayList<String>();
        List<URL> urls = getUrls();
        for (URL url : urls) {
            File f = new File(url.getFile());
            if (f.isDirectory()) {
                try {
                    discoverInDirectory(f.getCanonicalPath() + controllerPath, controllerPackages, "");
                } catch (Exception ignore) {
                }
            } else {//assuming jar file
                discoverInJar(f, controllerPackages);
            }
        }
        return controllerPackages;
    }

    /**
     * 获取当前环境的所有JAR包路径
     *
     * @return List JAR包URL
     */
    private static List<URL> getUrls() {
        URL[] urls;
        try {
            urls = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();
            return Arrays.asList(urls);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * @param directoryPath
     * @param controllerPackages
     * @param parent
     */
    private static void discoverInDirectory(String directoryPath, List<String> controllerPackages, String parent) {
        try {
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                //nothing
            } else {
                File[] files = directory.listFiles();
                for (File file : files) {
                    if (file.isDirectory()) {
                        controllerPackages.add(parent + (parent.equals("") ? "" : ".") + file.getName());
                        discoverInDirectory(file.getCanonicalPath(), controllerPackages, parent + (parent.equals("") ? "" : ".") + file.getName());
                    }
                }
            }
        } catch (Exception ignore) {
        }
    }

    protected static void discoverInJar(File file, List<String> controllerPackages) {
        String base = Configuration.getRootPackage();
        try {
            JarFile jarFile = new JarFile(file);

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();

                String path = jarEntry.toString();
                if (path.startsWith(base) && !path.endsWith(".class") && !path.equals(base)) {
                    controllerPackages.add(path.substring(base.length(), path.length() - 1).replace("/", "."));
                }
            }
        } catch (Exception ignore) {
        }
    }
}
