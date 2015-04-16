package org.memnon.route;

import org.httpkit.HttpMethod;
import org.memnon.context.Context;
import org.memnon.controller.AppController;
import org.memnon.controller.ControllerFactory;
import org.memnon.controller.DynamicClassFactory;
import org.memnon.exception.ClassLoadException;
import org.memnon.exception.ConfigurationException;
import org.memnon.exception.ControllerException;
import org.memnon.util.Configuration;
import org.memnon.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 路由创建
 */
public class RouteBuilder {
    private static Pattern USER_SEGMENT_PATTERN = Pattern.compile("\\{.*\\}");
    private AppController controller;
    private Class<? extends AppController> type;
    private String actionName, id, routeConfig;
    private List<Segment> segments = new ArrayList<Segment>();
    private List<HttpMethod> methods = new ArrayList<HttpMethod>();

    private String wildcardName = null;
    private String wildCardValue;

    private int mandatorySegmentCount = 0;

    /**
     * 使用标准的Restful创建
     *
     * @param controller controller
     * @param actionName action name
     * @param id         id
     */
    protected RouteBuilder(AppController controller, String actionName, String id) {
        this.controller = controller;
        this.actionName = actionName;
        this.id = id;
    }

    protected String getId() {
        return id;
    }

    protected String getActionName() {
        return actionName == null ? actionName = "index" : actionName;
    }

    protected AppController getController() {
        try {
            return controller == null ? controller = type.newInstance() : controller;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public String getWildcardName() {
        return wildcardName;
    }

    public String getWildCardValue() {
        return wildCardValue;
    }

    /**
     * 使用用户自定义路由
     *
     * @param routeConfig 用户自定义路由配置
     */
    protected RouteBuilder(String routeConfig) {
        String[] segmentsArr = Util.split(routeConfig, '/');
        for (String segmentStr : segmentsArr) {
            Segment segment = new Segment(segmentStr);
            segments.add(segment);
            if (segment.wildCard) {
                String wildCardSegment = segment.segment;
                wildcardName = wildCardSegment.substring(1);
                break; // break from loop, we are done!
            }
        }

        if (segmentsArr.length > segments.size()) {
            throw new ConfigurationException("Cannot have URI segments past wild card");
        }
        this.routeConfig = routeConfig;

        for (Segment segment : segments) {
            if (segment.mandatory) {
                mandatorySegmentCount++;
            }
        }
    }

    /**
     * 单路由中包含的段
     */
    private class Segment {
        private String segment, userSegmentName;
        private boolean controller, action, id, user, mandatory = true, staticSegment, wildCard;


        Segment(String segment) {
            this.segment = segment;
            //特殊段识别
            controller = segment.equals("{controller}");
            action = segment.equals("{action}");
            id = segment.equals("{id}");

            //判断是否是自定义路由
            if (!controller && !action && !id) {
                userSegmentName = getUserSegmentName(segment);
                user = userSegmentName != null;
            }

            //如果没有匹配上特殊段则为静态路由
            if (!controller && !action && !id && !user) {
                staticSegment = true;
            }

            //判断是否有通配符
            if (segment.startsWith("*")) {
                wildCard = true;
            }
        }

        boolean match(String requestSegment) throws ClassLoadException {

            if (staticSegment && requestSegment.equals(segment)) {
                return true;
            } else if (controller) {

                if (type == null) {//in case controller not provided in config, we infer it from the segment.
                    String controllerClassName = ControllerFactory.getControllerClassName("/" + requestSegment);
                    type = DynamicClassFactory.getCompiledClass(controllerClassName);
                    return true;
                }
                return requestSegment.equals(Router.getControllerPath(type).substring(1));
            } else if (action) {
                RouteBuilder.this.actionName = requestSegment;
                return true;
            } else if (id) {
                RouteBuilder.this.id = requestSegment;
                return true;
            } else if (user) {
                if (userSegmentName != null) {
                    Context.getRequestContext().getUserSegments().put(userSegmentName, requestSegment);
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * 匹配用户自定义路由配置。
     *
     * @param segment user segment, such as "{user_id}",  "{fav_color}", etc.
     * @return the name inside the braces, "user_id", "fav_color", etc.
     * Returns null if no pattern match: {xxx}.
     */
    protected String getUserSegmentName(String segment) {
        Matcher m = USER_SEGMENT_PATTERN.matcher(segment);
        if (m.find()) {
            String value = m.group(0);
            return value.substring(1, value.length() - 1);
        }
        return null;
    }

    /**
     * 判断根据uri和httpMethod是否成功匹配
     *
     * @param requestUri 请求路径
     * @param httpMethod 请求Method
     * @return true 成功匹配
     * @throws ClassLoadException 无法加载控制器
     */
    protected boolean matches(String requestUri, HttpMethod httpMethod) throws ClassLoadException {

        boolean match = false;

        String[] requestUriSegments = Util.split(requestUri, '/');

        if (isWildcard() && requestUriSegments.length >= segments.size() && wildSegmentsMatch(requestUriSegments)) {
            String[] tailArr = Arrays.copyOfRange(requestUriSegments, segments.size() - 1, requestUriSegments.length);
            wildCardValue = Util.join(tailArr, "/");
            match = true;
        } else if (segments.size() == 0 && requestUri.equals("/")) {
            //this is matching root path: "/"
            actionName = "index";
            match = true;
        } else if (requestUriSegments.length < mandatorySegmentCount || requestUriSegments.length > segments.size()) {
            //route("/greeting/{user_id}").to(HelloController.class).action("hi");
            match = false;
        } else {
            //there should be a more elegant way ...
            for (int i = 0; i < requestUriSegments.length; i++) {
                String requestUriSegment = requestUriSegments[i];
                match = segments.get(i).match(requestUriSegment);
                if (!match)
                    break;
            }
        }

        if (match && Configuration.getActiveReload()) {
            controller = reloadController();
        }

        return match && methodMatches(httpMethod);
    }

    /**
     * 判断是否匹配HttpMethod
     *
     * @param httpMethod 请求HttpMethod
     * @return ture 成功匹配
     */
    private boolean methodMatches(HttpMethod httpMethod) {
        return methods.size() == 0 && httpMethod.equals(HttpMethod.GET) || methods.contains(httpMethod);
    }

    private boolean wildSegmentsMatch(String[] requestUriSegments) throws ClassLoadException {
        for (int i = 0; i < segments.size() - 1; i++) {
            Segment segment = segments.get(i);
            if (!segment.match(requestUriSegments[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean isWildcard() {
        return wildcardName != null;
    }

    private AppController reloadController() throws ClassLoadException {
        try {
            return ControllerFactory.createControllerInstance(getController().getClass().getName());
        } catch (ClassLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ClassLoadException(e);
        }
    }

    /**
     * Allows to wire a route to a controller.
     *
     * @param type class of controller to which a route is mapped
     * @return instance of {@link RouteBuilder}.
     */
    public <T extends AppController> RouteBuilder to(Class<T> type) {
        boolean hasControllerSegment = false;
        for (Segment segment : segments) {
            hasControllerSegment = segment.controller;
        }

        if (type != null && hasControllerSegment) {
            throw new IllegalArgumentException("Cannot combine {controller} segment and .to(\"...\") method. Failed route: " + routeConfig);
        }

        this.type = type;
        return this;
    }

    /**
     * Name of action to which a route is mapped.
     *
     * @param action name of action.
     * @return instance of {@link RouteBuilder}.
     */
    public RouteBuilder action(String action) {
        boolean hasActionSegment = false;
        for (Segment segment : segments) {
            hasActionSegment = segment.action;
        }
        if (action != null && hasActionSegment) {
            throw new IllegalArgumentException("Cannot combine {action} segment and .action(\"...\") method. Failed route: " + routeConfig);
        }
        this.actionName = action;
        return this;
    }
}