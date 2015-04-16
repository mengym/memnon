package org.memnon.response;

import org.memnon.controller.response.ControllerResponse;

/**
 * Created by Administrator on 2015/4/15.
 */
public class JsonResponse extends ControllerResponse {
    private Object response;

    public JsonResponse() {
    }

    public void setResponse(Object object) {
        this.response = object;
    }

    @Override
    public void doProcess() {

    }

    @Override
    public Object getResponse() {
        return this.response;
    }
}
