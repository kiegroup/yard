package org.kie.yard.api.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonPropertyOrder({ "inputs" })
public class DecisionTable extends DecisionLogic { 
    @JsonProperty(required = true)
    private List<String> inputs;
    @JsonProperty()
    private List<String> outputComponents;
    @JsonProperty(required = true)
    private List<Rule> rules;

    public List<String> getInputs() {
        return inputs;
    }

    public List<String> getOutputComponents() {
        return outputComponents;
    }

    public List<Rule> getRules() {
        return rules;
    }

    /**
     * TODO: anything which is NOT a string, is checked for equality in the target expressionLanguage, 
     * a string is taken for the expression predicate AS-IS (in the target expressionLanguage)
     */
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = InlineRule.class)
    @JsonSubTypes({ @JsonSubTypes.Type(value=InlineRule.class), @JsonSubTypes.Type(value=WhenThenRule.class) })
    public static interface Rule {

    }

    public static class InlineRule implements Rule {
        @JsonValue
        public List<Object> def;
        
        @JsonCreator
        public InlineRule(List<Object> data) {
            this.def = data;
        }

        public List<Object> getDef() {
            return def;
        }
    }

    @JsonPropertyOrder({ "when", "then" })
    public static class WhenThenRule implements Rule {
        @JsonProperty(required = true)
        private List<Object> when;
        @JsonProperty(required = true)
        private Object then;

        public List<Object> getWhen() {
            return when;
        }

        public Object getThen() {
            return then;
        }
    }
}