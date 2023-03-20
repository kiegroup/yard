package org.kie.yard.impl2;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.drools.util.IoUtils;
import org.junit.jupiter.api.Test;
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
    public void testScenario1() throws Exception  {
        final String CTX = """
            {
              "Age": 47,
              "Previous incidents?": false
            }
            """;
        Map<String, Object> outputJSONasMap = evaluate(CTX);
        assertThat(outputJSONasMap).hasFieldOrPropertyWithValue("Base price", 500);
        assertThat(outputJSONasMap).hasFieldOrPropertyWithValue("Downpayment", 50.0);
    }

    @Test
    public void testScenario2() throws Exception  {
        final String CTX = """
            {
              "Age": 19,
              "Previous incidents?": true
            }
            """;
        Map<String, Object> outputJSONasMap = evaluate(CTX);
        assertThat(outputJSONasMap).hasFieldOrPropertyWithValue("Base price", 1000);
        assertThat(outputJSONasMap).hasFieldOrPropertyWithValue("Downpayment", 70.0);
    }

    private Map<String, Object> evaluate(String jsonInputCxt) throws Exception {
        String yamlDecision = new String(IoUtils.readBytesFromInputStream(this.getClass().getResourceAsStream("/insurance-base-price.yml"), true));
        LOG.info("INPUT:\n{}", jsonInputCxt);

        YaRDParser parser = new YaRDParser();
        YaRDDefinitions units = parser.parse(yamlDecision);

        Map<String, Object> inputContext = readJSON(jsonInputCxt);
        
        Map<String, Object> tempOutCtx = units.evaluate(inputContext);
        final String OUTPUT_JSON = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tempOutCtx);
        Map<String, Object> outputJSONasMap = readJSON(OUTPUT_JSON);

        LOG.info("{}", OUTPUT_JSON);
        return outputJSONasMap;
    }
}
