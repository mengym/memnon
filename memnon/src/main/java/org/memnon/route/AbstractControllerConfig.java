package org.memnon.route;

import org.memnon.context.Context;
import org.memnon.controller.AppController;
import org.memnon.controller.filter.ControllerFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2015/4/15.
 */

public abstract class AbstractControllerConfig extends AppConfig {
    //exclude some controllers from filters
    private List<ExcludeBuilder> excludeBuilders = new ArrayList<ExcludeBuilder>();

    public class FilterBuilder {
        private ControllerFilter[] filters;
        private Class<? extends AppController>[] controllerClasses;

        protected FilterBuilder(ControllerFilter[] filters) {
            this.filters = filters;
        }

        /**
         * Provides a list of controllers to which filters are added.
         *
         * @param controllerClasses list of controller classes to which filters are added.
         * @return self, usually to run a method {@link #forActions(String...)}.
         */
        public FilterBuilder to(Class<? extends AppController>... controllerClasses) {
            this.controllerClasses = controllerClasses;
            for (Class<? extends AppController> controllerClass : controllerClasses) {
                Context.getControllerRegistry().getMetaData(controllerClass).addFilters(filters);
            }
            return this;
        }

        /**
         * Adds a list of actions for which filters are configured.
         * <p/>
         * Example:
         * <pre>
         * add(mew TimingFilter(), new DBConnectionFilter()).to(PostsController.class).forActions("index", "show");
         * </pre>
         *
         * @param actionNames list of action names for which filters are configured.
         */
        public void forActions(String... actionNames) {
            if (controllerClasses == null)
                throw new IllegalArgumentException("controller classes not provided. Please call 'to(controllers)' before 'forActions(actions)'");

            for (Class<? extends AppController> controllerClass : controllerClasses) {
                Context.getControllerRegistry().getMetaData(controllerClass).addFilters(filters, actionNames);
            }
        }

        /**
         * Excludes actions from filter configuration. Opposite of {@link #forActions(String...)}.
         *
         * @param excludedActions list of actions for which this filter will not apply.
         */
        public void excludeActions(String... excludedActions) {
            if (controllerClasses == null)
                throw new IllegalArgumentException("controller classes not provided. Please call 'to(controllers)' before 'exceptAction(actions)'");

            for (Class<? extends AppController> controllerClass : controllerClasses) {
                Context.getControllerRegistry().getMetaData(controllerClass).addFiltersWithExcludedActions(filters, excludedActions);
            }
        }
    }

    /**
     * Adds a set of filters to a set of controllers.
     * The filters are invoked in the order specified.
     *
     * @param filters filters to be added.
     * @return object with <code>to()</code> method which accepts a controller class. The return type is not important and not used by itself.
     */
    protected FilterBuilder add(ControllerFilter... filters) {
        return new FilterBuilder(filters);
    }

    /**
     * Adds filters to all controllers globally.
     * Example of usage:
     * <pre>
     * ...
     *   addGlobalFilters(new TimingFilter(), new DBConnectionFilter());
     * ...
     * </pre>
     *
     * @param filters filters to be added.
     */
    protected ExcludeBuilder addGlobalFilters(ControllerFilter... filters) {
        ExcludeBuilder excludeBuilder = new ExcludeBuilder(filters);
        excludeBuilders.add(excludeBuilder);
        return excludeBuilder;
    }

    @Override
    public void completeInit() {
        for (ExcludeBuilder excludeBuilder : excludeBuilders) {
            Context.getControllerRegistry().addGlobalFilters(excludeBuilder.getFilters(), excludeBuilder.getExcludeControllerClasses());
        }
    }

    public class ExcludeBuilder {

        private List<Class<? extends AppController>> excludeControllerClasses = new ArrayList<Class<? extends AppController>>();
        private List<ControllerFilter> filters = new ArrayList<ControllerFilter>();

        public ExcludeBuilder(ControllerFilter[] filters) {
            this.filters.addAll(Arrays.asList(filters));
        }

        /**
         * Pass controllers to this method if you want to exclude supplied filters to be applied to them.
         *
         * @param excludeControllerClasses list of controllers to which these filters do not apply.
         */
        public void exceptFor(Class<? extends AppController>... excludeControllerClasses) {
            this.excludeControllerClasses.addAll(Arrays.asList(excludeControllerClasses));
        }

        public List<Class<? extends AppController>> getExcludeControllerClasses() {
            return excludeControllerClasses;
        }

        public List<ControllerFilter> getFilters() {
            return filters;
        }

    }

}
