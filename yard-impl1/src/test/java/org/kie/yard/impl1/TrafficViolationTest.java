package org.kie.yard.impl1;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.DMNRuntime;

public class TrafficViolationTest extends AbstractDMNYamlTest {
    
    @Test
    public void testExecution() throws Exception {
        DMNRuntime dmnRuntime = createDMNRuntimeFromYamlFile("/FEEL/traffic-violation.yml");
        Map<String, Object> inputData = parseInputDataFromJson("{\"Driver\": {\n" +
                "    \"Points\": 15\n" +
                "},\n" +
                "\"Violation\": {\n" +
                "    \"Type\": \"speed\",\n" +
                "    \"Actual Speed\": 135,\n" +
                "    \"Speed Limit\": 100\n" +
                "}}");

        Map<String, Object> outputJSONasMap  = performEvaluation(dmnRuntime, inputData);
        
        assertThat(outputJSONasMap).hasFieldOrPropertyWithValue("Should the driver be suspended?", "Yes");
    }

    @Test
    public void testExecutionJQ2() throws Exception {
        DMNRuntime dmnRuntime = createDMNRuntimeFromYamlFile("/jq/traffic-violation.yml");
        Map<String, Object> inputData = parseInputDataFromJson("{\"Driver\": {\n" +
                "    \"Points\": 15\n" +
                "},\n" +
                "\"Violation\": {\n" +
                "    \"Type\": \"speed\",\n" +
                "    \"Actual Speed\": 135,\n" +
                "    \"Speed Limit\": 100\n" +
                "}}");

        Map<String, Object> outputJSONasMap  = performEvaluation(dmnRuntime, inputData);
        
        assertThat(outputJSONasMap).hasFieldOrPropertyWithValue("Should the driver be suspended?", "Yes");
    }
        
}
