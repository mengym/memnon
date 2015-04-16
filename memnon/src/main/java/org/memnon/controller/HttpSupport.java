package org.memnon.controller;

import org.memnon.context.Context;
import org.memnon.controller.response.ControllerResponse;
import org.memnon.controller.response.TestResponse;
import org.memnon.util.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2015/4/15.
 */
public class HttpSupport {
    private static final Logger logger = LoggerFactory.getLogger(HttpSupport.class);


    public class HttpBuilder {
        private ControllerResponse controllerResponse;

        private HttpBuilder(ControllerResponse controllerResponse) {
            this.controllerResponse = controllerResponse;
        }

        protected ControllerResponse getControllerResponse() {
            return controllerResponse;
        }

        /**
         * Sets content type of response.
         * These can be "text/html". Value "text/html" is set by default.
         *
         * @param contentType content type value.
         * @return instance of RenderBuilder
         */
        public HttpBuilder contentType(String contentType) {
            controllerResponse.setContentType(contentType);
            return this;
        }

        /**
         * Sets a HTTP header on response.
         *
         * @param name  name of header.
         * @param value value of header.
         * @return instance of RenderBuilder
         */
        public HttpBuilder header(String name, String value) {
            controllerResponse.putHeader(name, value);
            return this;
        }

        /**
         * Overrides HTTP status with a different value.
         * For values and more information, look here:
         * <a href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'>HTTP Status Codes</a>.
         * <p/>
         * By default, the status is set to 200, OK.
         *
         * @param status HTTP status code.
         */
        public void status(int status) {
            controllerResponse.setStatus(status);
        }
    }

    /**
     * This method will send the text to a client verbatim. It will not use any layouts. Use it to build app.services
     * and to support AJAX.
     *
     * @param text text of response.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected HttpBuilder respond(String text) {
        TestResponse resp = new TestResponse();
        resp.setResponse(text);
        Context.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }

    /**
     * Returns value of one named parameter from request. If this name represents multiple values, this
     * call will result in {@link IllegalArgumentException}.
     *
     * @param name name of parameter.
     * @return value of request parameter.
     * @see org.memnon.util.RequestUtils#param(String)
     */
    protected String param(String name) {
        return RequestUtils.param(name);
    }

    /**
     * Returns value of ID if one is present on a URL. Id is usually a part of a URI, such as: <code>/controller/action/id</code>.
     * This depends on a type of a URI, and whether controller is RESTful or not.
     *
     * @return ID value from URI is one exists, null if not.
     */
    protected String getId() {
        return RequestUtils.getId();
    }
}
