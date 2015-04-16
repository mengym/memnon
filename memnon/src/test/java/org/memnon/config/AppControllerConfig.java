package org.memnon.config;

import org.memnon.context.AppContext;
import org.memnon.filter.TimingFilter;
import org.memnon.route.AbstractControllerConfig;

/**
 * Created by Administrator on 2015/4/15.
 */
public class AppControllerConfig extends AbstractControllerConfig {
    public void init(AppContext context) {
        addGlobalFilters(new TimingFilter());
    }
}
