package org.memnon.route;


import org.memnon.context.AppContext;

/**
 * 路由控制器抽象类-基础类
 */
public abstract class AppConfig {
    public abstract void init(AppContext appContext);

    public void completeInit() {
    }
}
