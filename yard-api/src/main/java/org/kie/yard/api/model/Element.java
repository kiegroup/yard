package org.kie.yard.api.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;

import io.fabric8.crd.generator.annotation.SchemaFrom;

@JsonPropertyOrder({ "name", "type" })
public class Element {
    @JsonProperty(required = true)
    private String name;
    @JsonProperty(required = true)
    private String type;
    @JsonProperty()
    private List<String> requirements;
    @JsonProperty(required = true)
    // TODO when this code is public, we may want to highlight this use case to the fabric8 team
    @JsonPropertyDescription("expects `x-kubernetes-preserve-unknown-fields: true` annotation in OpenAPI")
    @SchemaFrom(type=JsonNode.class)
    private DecisionLogic logic;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public DecisionLogic getLogic() {
        return logic;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLogic(DecisionLogic logic) {
        this.logic = logic;
    }
}