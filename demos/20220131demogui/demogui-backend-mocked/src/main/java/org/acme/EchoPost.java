package org.acme;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.nio.file.Files;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;

@Path("/echo")
public class EchoPost {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String hello(String incoming) {
        return incoming;
    }
}