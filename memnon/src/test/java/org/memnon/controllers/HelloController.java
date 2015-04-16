package org.memnon.controllers;

import org.memnon.annotations.GET;
import org.memnon.context.Context;
import org.memnon.controller.AppController;
import org.memnon.response.JsonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2015/4/15.
 */
public class HelloController extends AppController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @GET
    public void hello() {
        JsonResponse res = new JsonResponse();
        res.setStatus(200);
        res.setResponse(String.valueOf(Math.random()));
        Context.setControllerResponse(res);
    }
}
