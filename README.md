Memnon &mdash; Java Business Process development framework
=========

Memnon is a Java Process framework inspired by Ruby on Rails. It is optimized to be sustainable,  productive, and familiar for Java developers. 

## Memnon design principles

* __simple__: simple configuration is required to develop Business Process applications, and some conventions are overridable.

## 5 minutes to understand Memnon
	
* __see__ memnon/memnon/src/test/  demo.

* __Main class__ is org.memnon.server.Server

### Router Config
    public class RouteConfig extends AbstractRouteConfig {
    	public void init(AppContext appContext) {
	        route("/hello").to(HelloController.class).action("hello");
	        route("/{action}/{controller}/{id}");
	        route("/package/package/{action}/{controller}/{id}");
    	}
	}


### Config Filter

    public class AppControllerConfig extends AbstractControllerConfig {
	    public void init(AppContext context) {
	        addGlobalFilters(new TimingFilter());
			add(new TimingFilter()).to(HelloController.class).forActions("hello");
	    }
	}

### Controller Code

    public class HelloController extends AppController {
	
	    @GET
	    public void hello() {
	        JsonResponse res = new JsonResponse();
	        res.setStatus(200);
	        res.setResponse(String.valueOf(Math.random()));
	        Context.setControllerResponse(res);
	    }
	}

### config file

**serverHost=0.0.0.0**

**serverPort=9998**

----------
activeReload=**false**

rootPackage=**org.memnon.controllers**

rootConfig=**org.memnon.config.RouteConfig**

controllerConfig=**org.memnon.config.AppControllerConfig**
   
## Documentation

## Acknowledgement