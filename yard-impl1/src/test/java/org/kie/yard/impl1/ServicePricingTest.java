package org.kie.yard.impl1;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.DMNRuntime;

public class ServicePricingTest extends AbstractDMNYamlTest {


    @Test
    public void testExecution() throws Exception {
		Map<String, Object> inputData = parseInputDataFromJson("{\"Customer Plan\": \"Premium\", \"Requests\": 40000}");
		DMNRuntime dmnRuntime = createDMNRuntimeFromYamlFile("/FEEL/managed-services-price.yml");

        Map<String, Object> outputJSONasMap  = performEvaluation(dmnRuntime, inputData);
        
        assertThat(outputJSONasMap).hasFieldOrPropertyWithValue("Price for the Month", 24000.0);
    }

}
