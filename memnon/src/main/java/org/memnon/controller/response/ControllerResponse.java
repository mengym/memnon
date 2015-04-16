package org.memnon.controller.response;

import org.httpkit.HeaderMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/4/14.
 */
public abstract class ControllerResponse {
    private int status = 200;
    private String contentType;
    private HeaderMap header = new HeaderMap();

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void putHeader(String key, Object obj) {
        this.header.put(key, obj);
    }

    public final void process() {
        doProcess();
    }

    public abstract void doProcess();

    public abstract Object getResponse();
}
