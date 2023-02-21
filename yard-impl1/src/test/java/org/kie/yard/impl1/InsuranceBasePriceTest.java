package org.kie.yard.impl1;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.DMNRuntime;

public class InsuranceBasePriceTest extends AbstractDMNYamlTest {


    @Test
    public void testExecution() throws Exception {
        Map<String, Object> inputData = parseInputDataFromJson("{\"Age\": 47, \"Previous incidents?\": false}");
        DMNRuntime dmnRuntime = createDMNRuntimeFromYamlFile("/FEEL/insurance-base-price.yml");

        Map<String, Object> outputJSONasMap  = performEvaluation(dmnRuntime, inputData);
        
        assertThat(outputJSONasMap).hasFieldOrPropertyWithValue("Base price", 500);
    }

    @Test
    public void testExecutionJQ() throws Exception {
        Map<String, Object> inputData = parseInputDataFromJson("{\"Age\": 47, \"Previous incidents?\": false}");
        DMNRuntime dmnRuntime = createDMNRuntimeFromYamlFile("/jq/insurance-base-price.yml");
        
        Map<String, Object> outputJSONasMap  = performEvaluation(dmnRuntime, inputData);
        
        assertThat(outputJSONasMap).hasFieldOrPropertyWithValue("Base price", 500);
    }
}
