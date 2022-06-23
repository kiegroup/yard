package org.kie.yard.impl1.jitexecutor;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNType;
import org.kie.dmn.api.marshalling.DMNMarshaller;
import org.kie.dmn.backend.marshalling.v1x.DMNMarshallerFactory;
import org.kie.dmn.model.api.Definitions;
import org.kie.dmn.openapi.DMNOASGeneratorFactory;
import org.kie.dmn.openapi.model.DMNModelIOSets;
import org.kie.dmn.openapi.model.DMNOASResult;
import org.kie.kogito.jitexecutor.dmn.DMNEvaluator;
import org.kie.yard.impl1.YaRDParser;

import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.OpenApiStaticFile;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;

@Path("/openapi")
public class OpenAPIEndpoint {

    @ConfigProperty(name = Consts.CONF_ENV_NAME, defaultValue = Consts.DUMMY_YARD)
    String yaml;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String openapi() throws Exception {
        OpenAPI openAPI = loadOASTemplate();
        YaRDParser parser = new YaRDParser();
        Definitions definitions = parser.parse(yaml);
        DMNMarshaller dmnMarshaller = DMNMarshallerFactory.newDefaultMarshaller();
        final String xml = dmnMarshaller.marshal(definitions);
        DMNModel dmnModel = DMNEvaluator.fromXML(xml).getDmnModel();
        DMNOASResult dmnoas = DMNOASGeneratorFactory.generator(Collections.singletonList(dmnModel), "#/components/schemas/").build();
        for (Entry<DMNType, Schema> kv : dmnoas.getSchemas().entrySet()) {
            openAPI.getComponents().addSchema(dmnoas.getNamingPolicy().getName(kv.getKey()), kv.getValue());
        }
        DMNModelIOSets ioSets = dmnoas.lookupIOSetsByModel(dmnModel);
        DMNType identifyInputSet = ioSets.getInputSet();
        DMNType identifyOutputSet = ioSets.getOutputSet();
        String inputRef = dmnoas.getNamingPolicy().getRef(identifyInputSet);
        String outputRef = dmnoas.getNamingPolicy().getRef(identifyOutputSet);
        PathItem path = openAPI.getPaths().getPathItem("/yard");
        path.getPOST().getRequestBody().getContent().getMediaType("application/json").getSchema().type(null).ref(inputRef);
        path.getPOST().getResponses().getAPIResponse("200").getContent().getMediaType("application/json").getSchema().type(null).ref(outputRef);
        boolean asJSON = false;
        String content = OpenApiSerializer.serialize(openAPI, asJSON ? Format.JSON : Format.YAML);
        return content;
    }

    private OpenAPI loadOASTemplate() throws Exception {
        try (InputStream is = this.getClass().getResourceAsStream("/oasTemplate.yaml");
                OpenApiStaticFile openApiStaticFile = new OpenApiStaticFile(is, Format.YAML)) {
            return OpenApiProcessor.modelFromStaticFile(openApiStaticFile);
        }
    }
}
