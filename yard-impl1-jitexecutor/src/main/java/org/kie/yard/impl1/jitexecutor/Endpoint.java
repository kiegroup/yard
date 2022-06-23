package org.kie.yard.impl1.jitexecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.dmn.api.marshalling.DMNMarshaller;
import org.kie.dmn.backend.marshalling.v1x.DMNMarshallerFactory;
import org.kie.dmn.core.internal.utils.MarshallingStubUtils;
import org.kie.dmn.model.api.Definitions;
import org.kie.kogito.jitexecutor.dmn.JITDMNService;
import org.kie.kogito.jitexecutor.dmn.responses.JITDMNResult;
import org.kie.yard.impl1.YaRDParser;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.PojoCloudEventDataMapper;

@Path("/yard")
public class Endpoint {

    @ConfigProperty(name = Consts.CONF_ENV_NAME, defaultValue = Consts.DUMMY_YARD)
    String yaml;

    @Inject
    JITDMNService jitdmnService;

    @Inject
    ObjectMapper mapper;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response evaluate(Map<String, Object> input) {
        try {
            System.out.println(input);
            Map<String, Object> restResulk = evaluateYard(input);
            System.out.println("replying");
            System.out.println(restResulk);
            return Response.ok(restResulk).header("ce-id", UUID.randomUUID().toString()).header("ce-specversion", "1.0").header("ce-type", "yard")
                    .header("ce-source", "yard").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/ce")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response evaluateCE(CloudEvent input) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> value = PojoCloudEventDataMapper.from(mapper, Map.class)
                    .map(input.getData())
                    .getValue();
            System.out.println(value);
            Map<String, Object> restResulk = evaluateYard(value);
            System.out.println("replying");
            CloudEvent build = CloudEventBuilder.from(input).withId(UUID.randomUUID().toString()).withType("yard/ce").withData(mapper.writeValueAsString(restResulk).getBytes()).build();
            System.out.println(build);
            return Response.ok(build).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    private Map<String, Object> evaluateYard(Map<String, Object> input) throws Exception {
        YaRDParser parser = new YaRDParser();
        Definitions definitions = parser.parse(yaml);
        DMNMarshaller dmnMarshaller = DMNMarshallerFactory.newDefaultMarshaller();
        final String xml = dmnMarshaller.marshal(definitions);
        JITDMNResult evaluateAll = jitdmnService.evaluateModel(xml, input);
        Map<String, Object> restResulk = new HashMap<>();
        for (Entry<String, Object> kv : evaluateAll.getContext().getAll().entrySet()) {
            restResulk.put(kv.getKey(), MarshallingStubUtils.stubDMNResult(kv.getValue(), String::valueOf));
        }
        return restResulk;
    }
}
