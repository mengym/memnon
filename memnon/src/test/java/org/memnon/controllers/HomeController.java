package org.memnon.controllers;

import org.memnon.context.Context;
import org.memnon.controller.AppController;
import org.memnon.response.JsonResponse;

/**
 * Created by Administrator on 2015/4/14.
 */
public class HomeController extends AppController {
    public void index() {
        JsonResponse res = new JsonResponse();
        res.setStatus(200);
        res.setResponse("This is Home Controller response.");
        Context.setControllerResponse(res);
    }
}
