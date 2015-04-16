package org.memnon.server;

import org.httpkit.HeaderMap;
import org.httpkit.HttpUtils;
import org.httpkit.server.HttpRequest;
import org.httpkit.server.RespCallback;
import org.memnon.context.AppContext;
import org.memnon.context.Context;
import org.memnon.context.RequestContext;
import org.memnon.controller.ControllerRegistry;
import org.memnon.controller.ControllerRunner;
import org.memnon.controller.DynamicClassFactory;
import org.memnon.controller.response.EmptyResponse;
import org.memnon.controller.response.ControllerResponse;
import org.memnon.exception.ConfigurationException;
import org.memnon.exception.InitException;
import org.memnon.route.AbstractRouteConfig;
import org.memnon.route.AppConfig;
import org.memnon.route.Route;
import org.memnon.route.Router;
import org.memnon.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.httpkit.HttpUtils.HttpEncode;
import static org.memnon.util.Util.blank;
import static org.memnon.util.Util.getCauseMessage;

/**
 * User: melon
 * Date: 14-6-27
 * Time: 上午11:13
 */
class HttpHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    private HttpRequest request;
    //返回的回调
    private RespCallback callback;
    //应用的上下文
    private AppContext appContext;
    //控制器注册
    private ControllerRegistry registry;
    //控制器运行
    private ControllerRunner runner = new ControllerRunner();

    public HttpHandler(HttpRequest req, RespCallback cb) {
        this.request = req;
        this.callback = cb;
        //初始化控制器注册
        this.registry = new ControllerRegistry();
        //将会话控制器注册到当前会话上
        Context.setControllerRegistry(registry);
        //初始化应用上下文对象
        this.appContext = new AppContext();
        //初始化应用
        initApp(appContext);
    }

    protected void initApp(AppContext context) {
//        initAppConfig(Configuration.getBootstrapClassName(), context, true);
        //these are optional config classes:
        //加载控制器配置文件
        initAppConfig(Configuration.getControllerConfigClassName(), context);
//        initAppConfig(Configuration.getDbConfigClassName(), context, false);
    }

    /**
     * 配置文件加载
     *
     * @param configClassName 配置文件类名
     * @param context         应用上下文
     */
    private void initAppConfig(String configClassName, AppContext context) {
        AppConfig appConfig;
        try {
            Class c = Class.forName(configClassName);
            appConfig = (AppConfig) c.newInstance();
            appConfig.init(context);
            appConfig.completeInit();
        } catch (Throwable e) {
            throw new InitException("Failed to create and init a new instance of class: " + configClassName, e);
        }
    }

    /**
     * 主执行程序
     */
    public void run() {
        HeaderMap header = new HeaderMap();
        try {
            //请求路径
            String path = request.uri;
            String format = null;
            String uri;
            if (path.equals("/favicon.ico")) {
                callback.run(returnResult(header, new EmptyResponse()));
                return;
            } else if (path.contains(".")) {
                uri = path.substring(0, path.lastIndexOf('.'));
                format = path.substring(path.lastIndexOf('.') + 1);
            } else {
                uri = path;
            }
            if (blank(uri)) {
                uri = "/";
            }
            //将当前请求注册到会话上下文中
            Context.setTLs(path, getQueryMap(request.queryString), registry, appContext, new RequestContext(), format, request.method);
            //获取路由器
            Router router = getRouter(appContext);
            //根据当前请求的路径及请求methos 识别出是哪个路由请求
            Route route = router.recognize(uri, request.method);
            if (route != null) {
                Context.setRoute(route);
                logger.debug("================ New request: " + new Date() + " ================");
                runner.run(route);
            }
            callback.run(returnResult(header, Context.getControllerResponse()));
        } catch (Throwable e) {
            callback.run(HttpEncode(500, header, e.getMessage()));
            HttpUtils.printError(request.method + " " + request.uri, e);
        }
    }

    /**
     * 获取全部路由表数据
     *
     * @param context 应用上下文
     * @return 全部路由表
     */
    private Router getRouter(AppContext context) {
        //加载用户自定义router配置
        String routeConfigClassName = Configuration.getRouteConfigClassName();
        //默认router
        Router router = new Router("home");
        AbstractRouteConfig routeConfigLocal;
        try {
            //动态加载路由配置文件
            Class configClass = DynamicClassFactory.getCompiledClass(routeConfigClassName);
            //实例化
            routeConfigLocal = (AbstractRouteConfig) configClass.newInstance();
            routeConfigLocal.clear();
            //加载当前的上下文内容
            routeConfigLocal.init(context);
            //将用户自定义的Router加载到默认router中
            router.setRoutes(routeConfigLocal.getRoutes());
            //将用户自定义的忽略标示Route加载到默认router中
            router.setIgnoreSpecs(routeConfigLocal.getIgnoreSpecs());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            logger.debug("Did not find custom routes. Going with built in defaults: " + getCauseMessage(e));
        }
        return router;
    }

    /**
     * 获取url中的参数数据
     *
     * @param query 参数String ?后的数据
     * @return Map 参数key,value
     */
    public static Map<String, String> getQueryMap(String query) {
        if (query == null || query.length() == 0) {
            return new HashMap<String, String>();
        }
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    public ByteBuffer[] returnResult(HeaderMap headers, ControllerResponse response) {
        return HttpEncode(response.getStatus(), headers, response.getResponse());
    }
}