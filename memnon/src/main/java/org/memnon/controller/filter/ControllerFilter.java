package org.memnon.controller.filter;

/**
 * 控制器过滤器接口
 */
public interface ControllerFilter {
    void before();

    void after();

    void onException(Exception e);
}