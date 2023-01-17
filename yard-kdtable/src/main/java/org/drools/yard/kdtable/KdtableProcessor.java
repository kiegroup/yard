package org.drools.yard.kdtable;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.drools.io.ReaderResource;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.api.marshalling.DMNMarshaller;
import org.kie.dmn.backend.marshalling.v1x.DMNMarshallerFactory;
import org.kie.dmn.core.internal.utils.DMNRuntimeBuilder;
import org.kie.dmn.core.internal.utils.DynamicDMNContextBuilder;
import org.kie.dmn.core.internal.utils.MarshallingStubUtils;
import org.kie.dmn.model.api.Definitions;
import org.kie.yard.api.model.DecisionTable;
import org.kie.yard.api.model.Element;
import org.kie.yard.api.model.Input;
import org.kie.yard.api.model.YaRD;
import org.kie.yard.impl1.YaRDParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

public class KdtableProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(KdtableProcessor.class);

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private Object kdtable;
    private String expressionLang;
    private String resultKey = "kdtable";

    private DecisionTable dt;
    private DMNRuntime dmnRuntime;

    @Override
    public void process(Exchange exchange) throws Exception {
        LOG.info("this processor/bean field 'kdtable' was set to: {}", kdtable);
        LOG.info("this processor/bean field 'kdtable' class is: {}", kdtable.getClass());
        LOG.info("the deserialized DT, rules in total are: {}", dt.getRules().size());

        Object body = exchange.getIn().getBody();
        Map<String, Object> inContext = null;
        if (body instanceof String) {
            inContext = jsonMapper.readValue((String) body, new TypeReference<Map<String, Object>>(){});
        } else if (body instanceof ObjectNode) {
            inContext = jsonMapper.convertValue(body, new TypeReference<Map<String, Object>>(){});
        } else {
            throw new IllegalArgumentException("Exchange body not a String representing a JSON, or not ObjectNode either.");
        }
        LOG.info("inContext: {}", inContext);

        DMNContext dmnContext = new DynamicDMNContextBuilder(dmnRuntime.newContext(), dmnRuntime.getModels().get(0))
                .populateContextWith(inContext);
        DMNResult dmnResult = dmnRuntime.evaluateAll(dmnRuntime.getModels().get(0), dmnContext);
        if (dmnResult.getDecisionResults().size() != 1) {
            throw new IllegalStateException("was expecting == 1 decisionResults.");
        }
        Object serialized = MarshallingStubUtils.stubDMNResult(dmnResult.getContext().getAll(), Object::toString);
        final String OUTPUT_JSON = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(serialized);
        LOG.info("OUTPUT_JSON: {}", OUTPUT_JSON);

        if (body instanceof ObjectNode) {
            JsonNode resultAsJsonNode = jsonMapper.readTree(OUTPUT_JSON);
            exchange.getIn().setBody(resultAsJsonNode);
        } else {
            exchange.getIn().setBody(OUTPUT_JSON);
        }
    }

    public Object getKdtable() {
        return kdtable;
    }

    public void setKdtable(Object kdtable) {
        this.kdtable = kdtable;
        if (!(kdtable instanceof String)) {
            throw new IllegalArgumentException();
        }
        final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory().disable(Feature.USE_NATIVE_TYPE_ID));
        yamlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try {
            dt = yamlMapper.readValue((String) kdtable, DecisionTable.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        YaRD yard = new YaRD();
        yard.setSpecVersion("alpha");
        yard.setKind("YaRD");
        yard.setName("generated");
        if (expressionLang != null && !expressionLang.isEmpty()) { // deliberate kamelet spec camel-k definition default value empty string.
            yard.setExpressionLang(expressionLang);
        } else {
            yard.setExpressionLang(null);
        }
        Element decision = new Element();
        decision.setName(resultKey);
        decision.setType("Decision");
        decision.setLogic(dt);
        yard.setElements(Arrays.asList(decision));
        yard.setInputs(new ArrayList<>());
        for (String i : dt.getInputs()) {
            Input input = new Input();
            input.setName(i);
            input.setType("Any");
            yard.getInputs().add(input);
        }
        String yardYaml;
        try {
            yardYaml = yamlMapper.writeValueAsString(yard);
            LOG.info("generated YaRD yaml:\n{}", yardYaml);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }

        DMNMarshaller dmnMarshaller = DMNMarshallerFactory.newDefaultMarshaller();
        YaRDParser parser = new YaRDParser();
        
        Definitions definitions;
        try {
            definitions = parser.parse(yardYaml);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        String xml = dmnMarshaller.marshal(definitions);
        LOG.debug("{}", xml);

        dmnRuntime = DMNRuntimeBuilder.fromDefaults()
            .buildConfiguration()
            .fromResources(Arrays.asList(new ReaderResource(new StringReader(xml))))
            .getOrElseThrow(RuntimeException::new);
    }

    public String getExpressionLang() {
        return expressionLang;
    }

    public void setExpressionLang(String expressionLang) {
        this.expressionLang = expressionLang;
    }

    public String getResultKey() {
        return resultKey;
    }

    public void setResultKey(String resultKey) {
        this.resultKey = resultKey;
    }
}
