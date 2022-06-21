package org.kie.yard.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import org.junit.Assert;
import org.junit.Test;
import org.kie.yard.api.model.YaRD;

public class MarshallingTest {
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());
    private static final JsonSchemaFactory FACTORY = JsonSchemaFactory
            .builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)).objectMapper(MAPPER).build();

    @Test
    public void test1() throws Exception {
        File file = new File("src/test/resources/traffic-violation.yml");
        checkSampleFile(file);
    }

    @Test
    public void test2() throws Exception {
        File file = new File("src/test/resources/service-price.yml");
        checkSampleFile(file);
    }

    private void checkSampleFile(File file) {
        try {
            YaRD sd = MAPPER.readValue(file, YaRD.class);
            System.out.println(sd);
            JsonNode readTree = MAPPER.readTree(file);
            System.out.println(readTree);
            Set<ValidationMessage> validate = FACTORY.getSchema(this.getClass().getResourceAsStream("/YaRD-schema.json")).validate(readTree);
            validate.forEach(System.out::println);
            assertThat(validate).isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
