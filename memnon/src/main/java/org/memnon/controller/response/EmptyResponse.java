package org.memnon.controller.response;

/**
 * Created by Administrator on 2015/4/15.
 */
public class EmptyResponse extends ControllerResponse {
    private String text;

    public EmptyResponse() {
        this.text = "empty response";
    }

    @Override
    public Object getResponse() {
        return this.text;
    }

    @Override
    public void doProcess() {
    }
}
