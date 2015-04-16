package org.memnon.filter;

import org.memnon.context.Context;
import org.memnon.controller.filter.ControllerFilterAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2015/4/15.
 */
public class TimingFilter extends ControllerFilterAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TimingFilter.class);
    //must be threadlocal - filters are NOT thread safe!
    private static ThreadLocal<Long> time = new ThreadLocal<Long>();

    @Override
    public void before() {
        time.set(System.currentTimeMillis());
    }

    @Override
    public void after() {
        logger.debug("Processed request in: " + (System.currentTimeMillis() - time.get() + " milliseconds, path: " + Context.getPath() + ", method: " + Context.getHttpMethod().toString()));
    }
}
