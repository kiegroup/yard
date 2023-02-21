package org.kie.yard.impl1;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;

import org.drools.io.ReaderResource;
import org.drools.util.IoUtils;
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

public abstract class AbstractDMNYamlTest {


    private static final Logger LOG = LoggerFactory.getLogger(InsuranceBasePriceTest.class);
    
	private JsonMapper jsonMapper = JsonMapper.builder().build();

	public AbstractDMNYamlTest() {
		super();
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> readJSON(final String CONTEXT) throws JsonProcessingException, JsonMappingException {
	    return jsonMapper.readValue(CONTEXT, Map.class);
	}

	protected Map<String, Object> parseInputDataFromJson(String inputAsJsonString) throws JsonProcessingException, JsonMappingException {
	    LOG.info("INPUT:\n{}", inputAsJsonString);
	    Map<String, Object> readValue = readJSON(inputAsJsonString);
		return readValue;
	}

	protected Map<String, Object> performEvaluation(DMNRuntime dmnRuntime, Map<String, Object> inputData) throws JsonMappingException, JsonProcessingException {
		DMNContext dmnContext = createDMNContext(inputData, dmnRuntime);
	    
	    DMNResult dmnResult = dmnRuntime.evaluateAll(dmnRuntime.getModels().get(0), dmnContext);
	
	    Map<String, Object> outputJSONasMap = parseDMNResultIntoJson(dmnResult);
		return outputJSONasMap;
	}

	protected DMNRuntime createDMNRuntimeFromYamlFile(String yamlFile) throws Exception {
		String xml = parseDMNDecisionFile(yamlFile);        
	
	    return createDMNRuntime(xml);
	}

	private Map<String, Object> parseDMNResultIntoJson(DMNResult dmnResult) throws JsonProcessingException, JsonMappingException {
		Object serialized = MarshallingStubUtils.stubDMNResult(dmnResult.getContext().getAll(), Object::toString);
	    final String OUTPUT_JSON = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(serialized);
	    Map<String, Object> outputJSONasMap = readJSON(OUTPUT_JSON);
	
	    LOG.info("{}", OUTPUT_JSON);
		return outputJSONasMap;
	}


	private String parseDMNDecisionFile(String yamlFile) throws Exception {
	    String yamlDecision = new String(IoUtils.readBytesFromInputStream(this.getClass().getResourceAsStream(yamlFile), true));
		DMNMarshaller dmnMarshaller = DMNMarshallerFactory.newDefaultMarshaller();
	    YaRDParser parser = new YaRDParser();
	    Definitions definitions = parser.parse(yamlDecision);
	    String xml = dmnMarshaller.marshal(definitions);
	    LOG.debug("XML:\n{}", xml);
		return xml;
	}

	private DMNContext createDMNContext(Map<String, Object> inputData, DMNRuntime dmnRuntime) {
		return new DynamicDMNContextBuilder(dmnRuntime.newContext(), dmnRuntime.getModels().get(0))
	            .populateContextWith(inputData);
	}
	
	private DMNRuntime createDMNRuntime(String xml) {
		return DMNRuntimeBuilder.fromDefaults()
	            .setDecisionLogicCompilerFactory(new JSR223EvaluatorCompilerFactory())
	            .buildConfiguration()
	            .fromResources(Arrays.asList(new ReaderResource(new StringReader(xml))))
	            .getOrElseThrow(RuntimeException::new);
	}

}