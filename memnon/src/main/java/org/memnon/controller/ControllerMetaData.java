package org.memnon.controller;

import com.google.inject.Injector;
import org.memnon.controller.filter.ControllerFilter;

import java.util.*;

/**
 * Meta-data class to keep various things related to a controller.
 *
 * @author Igor Polevoy
 */
public class ControllerMetaData {
    private List<ControllerFilter> controllerFilters = new LinkedList<ControllerFilter>();
    private HashMap<String, List<ControllerFilter>> actionFilterMap = new HashMap<String, List<ControllerFilter>>();
    private HashMap<String, List<ControllerFilter>> excludedActionFilterMap = new HashMap<String, List<ControllerFilter>>();

    public void addFilters(ControllerFilter[] filters) {
        Collections.addAll(controllerFilters, filters);
    }

    public void addFilter(ControllerFilter filter) {
        controllerFilters.add(filter);
    }

    public void addFiltersWithExcludedActions(ControllerFilter[] filters, String[] excludedActions) {

        for (String action : excludedActions) {
            excludedActionFilterMap.put(action, Arrays.asList(filters));
        }
    }

    public void addFilters(ControllerFilter[] filters, String[] actionNames) {
        //here we need to remove filters added to this controller if we are adding these filters to actions
        // of this controller.
        controllerFilters.removeAll(Arrays.asList(filters));

        for (String action : actionNames) {
            actionFilterMap.put(action, Arrays.asList(filters));
        }
    }

    @SuppressWarnings("unchecked")
    protected List<ControllerFilter> getFilters(String action) {
        LinkedList result = new LinkedList();
        result.addAll(controllerFilters);

        List<ControllerFilter> actionFilters = actionFilterMap.get(action);
        if (actionFilters != null) {
            result.addAll(actionFilters);
        }

        List<ControllerFilter> excludedFilters = excludedActionFilterMap.get(action);
        if (excludedFilters != null) {
            for (ControllerFilter excludedFilter : excludedFilters) {
                result.remove(excludedFilter);
            }
        }
        return result;
    }

    protected List<ControllerFilter> getFilters() {
        List<ControllerFilter> allFilters = new LinkedList<ControllerFilter>();
        allFilters.addAll(controllerFilters);
        for (List<ControllerFilter> filters : actionFilterMap.values()) {
            allFilters.addAll(filters);
        }

        for (List<ControllerFilter> filters : excludedActionFilterMap.values()) {
            allFilters.addAll(filters);
        }
        return allFilters;
    }

    protected void injectFilters(Injector injector) {
        for (ControllerFilter controllerFilter : getFilters()) {
            injector.injectMembers(controllerFilter);
        }
    }
}
