package org.kie.yard.api.boostrap.generator;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.CustomDefinition;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaKeyword;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;


public class Generator {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Missing one argument, the yard-api project.basedir");
            System.exit(-1);
        }
        final String yardApiProjectBasedir = args[0];
        System.out.println("Using yardApiProjectBasedir: " + yardApiProjectBasedir);
        final Class<?> YARD_CLASS = Class.forName("org.kie.yard.api.model.YaRD");
        final Class<?> YARD_INLINERULE_CLASS = Class.forName("org.kie.yard.api.model.DecisionTable$InlineRule");
        JacksonModule module = new JacksonModule(
                JacksonOption.FLATTENED_ENUMS_FROM_JSONVALUE,
                JacksonOption.RESPECT_JSONPROPERTY_REQUIRED,
                JacksonOption.RESPECT_JSONPROPERTY_ORDER
        );
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_7,
                OptionPreset.PLAIN_JSON)
                        .with(Option.DEFINITIONS_FOR_ALL_OBJECTS)
                        .with(module);
        configBuilder.forFields().withDefaultResolver(field -> {
            JsonProperty annotation = field.getAnnotationConsideringFieldAndGetter(JsonProperty.class);
            return annotation == null || annotation.defaultValue().isEmpty() ? null : annotation.defaultValue();
        });
        configBuilder.forTypesInGeneral().withCustomDefinitionProvider((javaType, context) -> {
            if (javaType.getErasedType().equals(YARD_INLINERULE_CLASS)) {
                SchemaGeneratorConfig config = context.getGeneratorConfig();
                return new CustomDefinition(context.getGeneratorConfig().createObjectNode()
                        .put(config.getKeyword(SchemaKeyword.TAG_TYPE), config.getKeyword(SchemaKeyword.TAG_TYPE_ARRAY))
                        .set(config.getKeyword(SchemaKeyword.TAG_ITEMS),
                                context.getGeneratorConfig().getObjectMapper().createObjectNode()));
            }
            return null;
        });
        SchemaGeneratorConfig config = configBuilder.build();
        SchemaGenerator generator = new SchemaGenerator(config);
        JsonNode jsonSchema = generator.generateSchema(YARD_CLASS);

        final String jsonSchemaAsString = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(jsonSchema);
        System.out.println(jsonSchemaAsString);
        Files.write(Paths.get(yardApiProjectBasedir + "/src/main/resources/YaRD-schema.json"), jsonSchemaAsString.getBytes());
    }
}
