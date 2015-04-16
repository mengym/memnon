package org.memnon.controller;

import com.google.inject.Injector;
import org.httpkit.HttpMethod;
import org.memnon.context.Context;
import org.memnon.controller.filter.ControllerFilter;
import org.memnon.controller.response.ControllerResponse;
import org.memnon.controller.response.TestResponse;
import org.memnon.exception.ActionNotFoundException;
import org.memnon.exception.CommonException;
import org.memnon.exception.ControllerException;
import org.memnon.exception.FilterException;
import org.memnon.route.Route;
import org.memnon.util.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.memnon.util.Util.join;

/**
 * Created by Administrator on 2015/4/10.
 */
public class ControllerRunner {
    private static final Logger logger = LoggerFactory.getLogger(ControllerRunner.class.getName());

    public void run(Route route) throws Exception {
        ControllerRegistry controllerRegistry = Context.getControllerRegistry();
        List<ControllerRegistry.FilterList> globalFilterLists = controllerRegistry.getGlobalFilterLists();
        List<ControllerFilter> controllerFilters = controllerRegistry.getMetaData(route.getController().getClass()).getFilters(route.getActionName());

        Context.getControllerRegistry().injectFilters(); //will execute once, really filters are persistent

        try {
            filterBefore(route, globalFilterLists, controllerFilters);

            if (Context.getControllerResponse() == null) {//execute controller... only if a filter did not respond

                String actionMethod = Inflector.camelize(route.getActionName().replace('-', '_'), false);
                if (checkActionMethod(route.getController(), actionMethod)) {
                    //Configuration.getTemplateManager().
                    injectController(route.getController());
                    logger.debug("Executing controller: " + route.getController().getClass().getName() + "." + actionMethod);
                    executeAction(route.getController(), actionMethod);
                }
            }
//            renderResponse(route);
            //run filters in opposite order
            filterAfter(route, globalFilterLists, controllerFilters);
        } catch (ActionNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            Context.setControllerResponse(null);//must blow away, as this response is not valid anymore.

            if (exceptionHandled(e, route, globalFilterLists, controllerFilters)) {
                logger.debug("A filter has called render(..) method, proceeding to render it...");
                renderResponse(route);//a filter has created an instance of a controller response, need to render it.
            } else {
                throw e;//if exception was not handled by filter, re-throw
            }
        }
    }

    private void filterBefore(Route route, List<ControllerRegistry.FilterList> globalFilterLists, List<ControllerFilter>... filterGroups) {
        try {

            //first, process global filters and account for exceptions
            for (ControllerRegistry.FilterList filterList : globalFilterLists) {
                if (!filterList.excludesController(route.getController())) {
                    List<ControllerFilter> filters = filterList.getFilters();
                    for (ControllerFilter controllerFilter : filters) {
                        controllerFilter.before();
                    }
                }
            }

            //then process all other filters
            for (List<ControllerFilter> filterGroup : filterGroups) {
                for (ControllerFilter controllerFilter : filterGroup) {
                    logger.debug("Executing filter: " + controllerFilter.getClass().getName() + "#before");
                    controllerFilter.before();
                    if (Context.getControllerResponse() != null) return;//a filter responded!
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new FilterException(e);
        }
    }

    private void filterAfter(Route route, List<ControllerRegistry.FilterList> globalFilterLists, List<ControllerFilter>... filterGroups) {
        try {

            //first, process global filters and account for exceptions
            for (ControllerRegistry.FilterList filterList : globalFilterLists) {
                if (!filterList.excludesController(route.getController())) {
                    List<ControllerFilter> filters = filterList.getFilters();
                    for (ControllerFilter controllerFilter : filters) {
                        controllerFilter.after();
                    }
                }
            }

            for (List<ControllerFilter> filterGroup : filterGroups) {
                for (int i = filterGroup.size() - 1; i >= 0; i--) {
                    logger.debug("Executing filter: " + filterGroup.get(i).getClass().getName() + "#after");
                    filterGroup.get(i).after();
                }
            }
        } catch (Exception e) {
            throw new FilterException(e);
        }
    }

    private boolean checkActionMethod(AppController controller, String actionMethod) {
        HttpMethod method = HttpMethod.fromKeyword(Context.getHttpMethod().KEY);
        if (!controller.actionSupportsHttpMethod(actionMethod, method)) {
            TestResponse res = new TestResponse();
            //see http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
            res.setStatus(405);
            logger.warn("Requested action does not support HTTP method: " + method.name() + ", returning status code 405.");
            //see http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
            res.putHeader("Allow", join(controller.allowedActions(actionMethod), ", "));
            Context.setControllerResponse(res);
            return false;
        }
        return true;
    }

    /**
     * Injects controller with dependencies from Guice module.
     */
    private void injectController(AppController controller) {
        Injector injector = Context.getControllerRegistry().getInjector();
        if (injector != null) {
            injector.injectMembers(controller);
        }
    }

    private void executeAction(Object controller, String actionName) {
        try {
            Method m = controller.getClass().getMethod(actionName);
            m.invoke(controller);
        } catch (InvocationTargetException e) {
            if (e.getCause() != null && e.getCause() instanceof CommonException) {
                throw (CommonException) e.getCause();
            } else if (e.getCause() != null && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else if (e.getCause() != null) {
                throw new ControllerException(e.getCause());
            }
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    private void renderResponse(Route route) throws InstantiationException, IllegalAccessException {
        //set encoding. Priority: action, then controller
        ControllerResponse controllerResponse = Context.getControllerResponse();
        controllerResponse.process();
    }

    private boolean exceptionHandled(Exception e, Route route, List<ControllerRegistry.FilterList> globalFilterLists, List<ControllerFilter>... filterGroups) throws Exception {

        //first, process global filters and account for exceptions
        for (ControllerRegistry.FilterList filterList : globalFilterLists) {
            if (!filterList.excludesController(route.getController())) {
                List<ControllerFilter> filters = filterList.getFilters();
                for (ControllerFilter controllerFilter : filters) {
                    controllerFilter.onException(e);
                }
            }
        }

        for (List<ControllerFilter> filterGroup : filterGroups) {
            for (ControllerFilter controllerFilter : filterGroup) {
                controllerFilter.onException(e);
            }
        }
        return Context.getControllerResponse() != null;
    }
}
