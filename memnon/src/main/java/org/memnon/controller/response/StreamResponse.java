package org.memnon.controller.response;

import org.memnon.exception.ControllerException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2015/4/15.
 */
public class StreamResponse extends ControllerResponse {
    private InputStream in;

    public StreamResponse(InputStream in) {
        this.in = in;
    }

    @Override
    public void doProcess() {

    }

    @Override
    public Object getResponse() {
        return null;
    }
}
