package org.memnon.controller;

import com.google.inject.Injector;
import org.memnon.controller.filter.ControllerFilter;

import java.util.*;

/**
 * Created by Administrator on 2015/4/9.
 */
public class ControllerRegistry {

    /**
     * key - controller class name, value ControllerMetaData.
     */
    private Map<String, ControllerMetaData> metaDataMap = new HashMap<String, ControllerMetaData>();
    private List<FilterList> globalFilterLists = new ArrayList<FilterList>();
    private Injector injector;
    private boolean filtersInjected = false;
    private final Object token = new Object();


    // these are not full package names, just partial package names between "app.controllers"
    // and simple name of controller class
    private List<String> controllerPackages;

    public ControllerRegistry() {
        //加载用户自定义控制器
        controllerPackages = ControllerPackageLocator.locateControllerPackages();
    }

    public List<String> getControllerPackages() {
        return controllerPackages;
    }

    protected List<FilterList> getGlobalFilterLists() {
        return Collections.unmodifiableList(globalFilterLists);
    }

    public void addGlobalFilters(ControllerFilter... filters) {
        globalFilterLists.add(new FilterList(Arrays.asList(filters)));
    }

    public void addGlobalFilters(List<ControllerFilter> filters, List<Class<? extends AppController>> excludeControllerClasses) {
        globalFilterLists.add(new FilterList(filters, excludeControllerClasses));
    }

    /**
     * Returns controller metadata for a class.
     *
     * @param controllerClass controller class.
     * @return controller metadata for a controller class.
     */
    public ControllerMetaData getMetaData(Class<? extends AppController> controllerClass) {
        if (metaDataMap.get(controllerClass.getName()) == null) {
            metaDataMap.put(controllerClass.getName(), new ControllerMetaData());
        }
        return metaDataMap.get(controllerClass.getName());
    }

    static class FilterList {
        private final List<ControllerFilter> filters;
        private final List<Class<? extends AppController>> excludedControllers;

        private FilterList(List<ControllerFilter> filters, List<Class<? extends AppController>> excludedControllers) {
            this.filters = filters;
            this.excludedControllers = excludedControllers;
        }

        private FilterList(List<ControllerFilter> filters) {
            this(filters, Collections.<Class<? extends AppController>>emptyList());
        }

        public List<ControllerFilter> getFilters() {
            return Collections.unmodifiableList(filters);
        }

        public boolean excludesController(AppController controller) {

            for (Class<? extends AppController> clazz : excludedControllers) {
                //must use string here, because when controller re-compiles, class instance is different
                if (clazz.getName().equals(controller.getClass().getName()))
                    return true;
            }
            return false;
        }
    }

    protected void injectFilters() {

        if (!filtersInjected) {
            synchronized (token) {
                if (injector != null) {
                    //inject global filters:
                    for (FilterList filterList : globalFilterLists) {
                        List<ControllerFilter> filters = filterList.getFilters();
                        for (ControllerFilter controllerFilter : filters) {
                            injector.injectMembers(controllerFilter);
                        }
                    }
                    //inject specific controller filters:
                    for (String key : metaDataMap.keySet()) {
                        metaDataMap.get(key).injectFilters(injector);
                    }
                }
                filtersInjected = true;
            }
        }
    }


    protected void setInjector(Injector injector) {
        this.injector = injector;
    }

    protected Injector getInjector() {
        return injector;
    }
}
