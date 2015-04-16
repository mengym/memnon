package org.memnon.controller.response;

/**
 * Created by Administrator on 2015/4/15.
 */
public class TestResponse extends ControllerResponse {
    private Object response;

    public TestResponse() {
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
