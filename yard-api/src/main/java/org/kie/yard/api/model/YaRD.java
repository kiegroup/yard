package org.kie.yard.api.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "specVersion", "kind", "name", "expressionLang", "inputs", "elements" })
public class YaRD {
    @JsonProperty(defaultValue = "alpha")
    private String specVersion = "alpha";
    @JsonProperty(defaultValue = "YaRD")
    private String kind = "YaRD";
    @JsonPropertyDescription("when not provided explicitly, implementation will attempt to deduce the name from the runtime context; if a name cannot be deduced it is an error.")
    @JsonProperty()
    private String name;
    @JsonPropertyDescription("An implementation is free to assume a default expressionLang if not explicitly set. For the purpose of a User sharing a YaRD definition, is best to valorise this field explicit.")
    @JsonProperty()
    private String expressionLang;
    @JsonProperty(required = true)
    private List<Input> inputs;
    @JsonProperty(required = true)
    private List<Element> elements;

    public String getName() {
        return name;
    }

    public String getExpressionLang() {
        return expressionLang;
    }

    public void setExpressionLang(String expressionLang) {
        this.expressionLang = expressionLang;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getSpecVersion() {
        return specVersion;
    }

    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public List<Element> getElements() {
        return elements;
    }
}