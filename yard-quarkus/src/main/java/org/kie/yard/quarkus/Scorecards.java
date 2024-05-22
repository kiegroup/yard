package org.kie.yard.quarkus;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.kie.yard.quarkus.model.ScorecardModule;
import org.kie.yard.quarkus.model.result.Record;

import java.io.IOException;
import java.util.Collection;

@Path("/scorecards")
public class Scorecards {

    @Inject
    ResourceReader reader;

    @Inject
    ScorecardRunner runner;

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ScorecardModule> list() throws IOException {
        return reader.readModules();
    }

    @GET
    @Path("/run")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Record> run() throws IOException {
        return runner.run();
    }
}
