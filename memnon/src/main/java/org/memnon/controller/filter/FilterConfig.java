package org.memnon.controller.filter;

import java.util.Enumeration;

/**
 * Created by Administrator on 2015/4/15.
 */
public interface FilterConfig {
    public String getFilterName();

//    public ServletContext getServletContext();

    public String getInitParameter(String name);

    public Enumeration getInitParameterNames();
}
