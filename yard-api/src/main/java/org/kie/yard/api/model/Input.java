package org.kie.yard.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "name", "type" })
public class Input {
    @JsonProperty(required = true)
    private String name;
    @JsonProperty(required = true)
    private String type;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}