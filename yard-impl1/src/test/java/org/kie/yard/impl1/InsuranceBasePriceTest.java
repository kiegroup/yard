package org.kie.yard.impl1;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;

import org.drools.util.IoUtils;
import org.drools.io.ReaderResource;
import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.api.marshalling.DMNMarshaller;
import org.kie.dmn.backend.marshalling.v1x.DMNMarshallerFactory;
import org.kie.dmn.core.internal.utils.DMNRuntimeBuilder;
import org.kie.dmn.core.internal.utils.DynamicDMNContextBuilder;
import org.kie.dmn.core.internal.utils.MarshallingStubUtils;
import org.kie.dmn.core.jsr223.JSR223EvaluatorCompilerFactory;
import org.kie.dmn.model.api.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class InsuranceBasePriceTest {

    private static final Logger LOG = LoggerFactory.getLogger(InsuranceBasePriceTest.class);

    private JsonMapper jsonMapper = JsonMapper.builder().build();

    @SuppressWarnings("unchecked")
    private Map<String, Object> readJSON(final String CONTEXT) throws JsonProcessingException, JsonMappingException {
        return jsonMapper.readValue(CONTEXT, Map.class);
    }

    @Test
    public void testExecution() throws Exception {
        String yamlDecision = new String(IoUtils.readBytesFromInputStream(this.getClass().getResourceAsStream("/FEEL/insurance-base-price.yml"), true));

        final String CONTEXT = "{\"Age\": 47, \"Previous incidents?\": false}";
        LOG.info("INPUT:\n{}", CONTEXT);

        DMNMarshaller dmnMarshaller = DMNMarshallerFactory.newDefaultMarshaller();
        YaRDParser parser = new YaRDParser();
        Definitions definitions = parser.parse(yamlDecision);
        String xml = dmnMarshaller.marshal(definitions);
        LOG.debug("XML:\n{}", xml);

        DMNRuntime dmnRuntime = DMNRuntimeBuilder.fromDefaults()
                .setDecisionLogicCompilerFactory(new JSR223EvaluatorCompilerFactory())
                .buildConfiguration()
                .fromResources(Arrays.asList(new ReaderResource(new StringReader(xml))))
                .getOrElseThrow(RuntimeException::new);
        Map<String, Object> readValue = readJSON(CONTEXT);
        DMNContext dmnContext = new DynamicDMNContextBuilder(dmnRuntime.newContext(), dmnRuntime.getModels().get(0))
                .populateContextWith(readValue);
        DMNResult dmnResult = dmnRuntime.evaluateAll(dmnRuntime.getModels().get(0), dmnContext);
        Object serialized = MarshallingStubUtils.stubDMNResult(dmnResult.getContext().getAll(), Object::toString);
        final String OUTPUT_JSON = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(serialized);
        Map<String, Object> outputJSONasMap = readJSON(OUTPUT_JSON);

        LOG.info("{}", OUTPUT_JSON);
        assertThat(outputJSONasMap).hasFieldOrPropertyWithValue("Base price", 500);
    }

    @Test
    public void testExecutionJQ() throws Exception {
        String yamlDecision = new String(IoUtils.readBytesFromInputStream(this.getClass().getResourceAsStream("/jq/insurance-base-price.yml"), true));

        final String CONTEXT = "{\"Age\": 47, \"Previous incidents?\": false}";
        LOG.info("INPUT:\n{}", CONTEXT);

        DMNMarshaller dmnMarshaller = DMNMarshallerFactory.newDefaultMarshaller();
        YaRDParser parser = new YaRDParser();
        Definitions definitions = parser.parse(yamlDecision);
        String xml = dmnMarshaller.marshal(definitions);
        LOG.info("XML:\n{}", xml);

        DMNRuntime dmnRuntime = DMNRuntimeBuilder.fromDefaults()
                .setDecisionLogicCompilerFactory(new JSR223EvaluatorCompilerFactory())
                .buildConfiguration()
                .fromResources(Arrays.asList(new ReaderResource(new StringReader(xml))))
                .getOrElseThrow(RuntimeException::new);
        Map<String, Object> readValue = readJSON(CONTEXT);
        DMNContext dmnContext = new DynamicDMNContextBuilder(dmnRuntime.newContext(), dmnRuntime.getModels().get(0))
                .populateContextWith(readValue);
        DMNResult dmnResult = dmnRuntime.evaluateAll(dmnRuntime.getModels().get(0), dmnContext);
        Object serialized = MarshallingStubUtils.stubDMNResult(dmnResult.getContext().getAll(), Object::toString);
        final String OUTPUT_JSON = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(serialized);
        Map<String, Object> outputJSONasMap = readJSON(OUTPUT_JSON);

        LOG.info("{}", OUTPUT_JSON);
        assertThat(outputJSONasMap).hasFieldOrPropertyWithValue("Base price", 500);
    }
}
