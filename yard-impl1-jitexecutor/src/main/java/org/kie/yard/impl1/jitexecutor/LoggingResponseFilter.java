package org.kie.yard.impl1.jitexecutor;

import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import io.vertx.core.http.HttpServerRequest;

@Provider
public class LoggingResponseFilter implements ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(LoggingResponseFilter.class);

    @Context
    UriInfo info;

    @Context
    HttpServerRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        final String method = requestContext.getMethod();
        final String path = info.getPath();
        final String address = request.remoteAddress().toString();

        LOG.infof(" > Request %s %s from IP %s", method, path, address);

        final Map<?, ?> headers = requestContext.getHeaders();
        for (Object kv : headers.entrySet()) {
            LOG.infof(" > %s", kv);
        }

        LOG.infof("%s", responseContext.getStatus());
        final Map<?, ?> rHeaders = responseContext.getHeaders();
        for (Object kv : rHeaders.entrySet()) {
            LOG.infof(" < %s", kv);
        }

        // can't open the stream or else it would not reset anyway for the REST service :( 
        // try {
        //     final String content = new String(context.getEntityStream().readAllBytes());
        //     context.getEntityStream().reset();
        //     LOG.infof("%s", content);
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
    }
}