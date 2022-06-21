package org.kie.yard.api.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME, 
      include = As.PROPERTY, 
      property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value=DecisionTable.class), @JsonSubTypes.Type(value=LiteralExpression.class) })
public abstract class DecisionLogic {
}