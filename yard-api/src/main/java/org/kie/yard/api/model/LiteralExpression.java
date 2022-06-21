package org.kie.yard.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "inputs" })
public class LiteralExpression extends DecisionLogic { 
    @JsonProperty(required = true)
    private String expression;

    public String getExpression() {
        return expression;
    }
}