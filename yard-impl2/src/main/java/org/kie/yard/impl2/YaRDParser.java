package org.kie.yard.impl2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.drools.model.Index;
import org.drools.ruleunits.api.DataSource;
import org.drools.ruleunits.api.SingletonStore;
import org.drools.ruleunits.dsl.SyntheticRuleUnitBuilder;
import org.kie.yard.api.model.DecisionLogic;
import org.kie.yard.api.model.DecisionTable.InlineRule;
import org.kie.yard.api.model.DecisionTable.Rule;
import org.kie.yard.api.model.DecisionTable.WhenThenRule;
import org.kie.yard.api.model.Element;
import org.kie.yard.api.model.Input;
import org.kie.yard.api.model.LiteralExpression;
import org.kie.yard.api.model.YaRD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YaRDParser {
    private static final Logger LOG = LoggerFactory.getLogger(YaRDParser.class);

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final YaRDDefinitions definitions = new YaRDDefinitions(new HashMap<>(), new ArrayList<>(), new HashMap<>());

    public YaRDParser() {
        yamlMapper.findAndRegisterModules();
        jsonMapper.findAndRegisterModules();
    }

    public YaRDDefinitions parse(String yaml) throws Exception {
        YaRD sd = yamlMapper.readValue(yaml, YaRD.class);
        if (!sd.getExpressionLang().equals("Drools")) {
            throw new IllegalArgumentException("impl2 does support only `Drools` as expression language");
        }
        appendInputs(sd.getInputs());
        appendUnits(sd.getElements());
        return definitions;
    }

    private void appendUnits(List<Element> list) {
        for ( Element hi : list) {
            String nameString = (String) hi.getName();
            LOG.debug("parsing {}", nameString);
            Firable decisionLogic = createDecisionLogic(nameString, hi.getLogic());
            definitions.units().add(decisionLogic);
        }
    }

    private Firable createDecisionLogic(String nameString, DecisionLogic decisionLogic) {
        if (decisionLogic instanceof org.kie.yard.api.model.DecisionTable) {
            return createDecisionTable(nameString, (org.kie.yard.api.model.DecisionTable) decisionLogic);
        } else if (decisionLogic instanceof org.kie.yard.api.model.LiteralExpression) {
            return createLiteralExpression(nameString, (org.kie.yard.api.model.LiteralExpression) decisionLogic);
        } else {
            throw new UnsupportedOperationException("Not implemented in impl2 / TODO");
        }
    }

    private Firable createLiteralExpression(String nameString, LiteralExpression decisionLogic) {
        String expr = decisionLogic.getExpression();
        definitions.outs().put(nameString, StoreHandle.empty(Object.class));
        return new LiteralExpressionInterpreter(nameString, QuotedExprParsed.from(expr));
    }

    private Firable createDecisionTable(String nameString, org.kie.yard.api.model.DecisionTable logic) {
        List<String> inputs = logic.getInputs();
        if (inputs.isEmpty()) {
            throw new IllegalStateException("empty decision table?");
        }
        List<Rule> rules = logic.getRules();
        if (!(logic.getHitPolicy() == null || logic.getHitPolicy().equals("ANY"))) {
            throw new UnsupportedOperationException("Not implemented in impl2 / TODO");
        }
        // TODO looks to me SyntheticRuleUnit not fully thread safe as it's leaking outside the registerDataSource
        SyntheticRuleUnitBuilder unit = SyntheticRuleUnitBuilder.build(nameString); // TODO ensure unique key
        for (Entry<String, SingletonStore<Object>> e : definitions.ins().entrySet()) {
            unit.registerDataSource(e.getKey(), e.getValue(), Object.class);
        }
        // TODO wire-up the outs from previous RUs.
        StoreHandle<Object> result = StoreHandle.empty(Object.class);
        unit.registerGlobal(nameString, result);
        definitions.outs().put(nameString, result);
        var sru = unit.defineRules(rulesFactory -> {
            for (Rule rule : rules) {
                var ruleFactory = rulesFactory.rule(); // must be called before iterating on all alpha-constraints for EACH rule; see https://github.com/kiegroup/drools/pull/4999 
                for (int idx = 0; idx < inputs.size(); idx++) {
                    RuleCell cIdx = parseGenericRuleCell(rule, idx);
                    ruleFactory.on(definitions.ins().get(inputs.get(idx))).filter(cIdx.idxtype, cIdx.value);
                    ruleFactory.execute(result, r -> r.set( parseGenericRuleThen(rule).value ));
                }
            }
        });
        return new SyntheticRuleUnitWrapper(sru);
    }

    private RuleCell parseGenericRuleThen(Rule r) {
        if (r instanceof InlineRule) {
            InlineRule inlineRule = (InlineRule)r;
            return parseRuleCell(inlineRule.getDef().get(inlineRule.getDef().size()-1));
        } else if (r instanceof WhenThenRule) {
            return parseRuleCell(((WhenThenRule)r).getThen());
        } else {
            throw new IllegalStateException("unknown or unmanaged rule instance type?");
        }
    }

    private RuleCell parseGenericRuleCell(Rule r, int i) {
        if (r instanceof InlineRule) {
            return parseRuleCell(((InlineRule)r).getDef().get(i));
        } else if (r instanceof WhenThenRule) {
            return parseRuleCell(((WhenThenRule)r).getWhen().get(i));
        } else {
            throw new IllegalStateException("unknown or unmanaged rule instance type?");
        }
    }

    private RuleCell parseRuleCell(Object object) {
        if (object instanceof Boolean) {
            return new RuleCell(Index.ConstraintType.EQUAL, object);
        } else if (object instanceof Number) {
            return new RuleCell(Index.ConstraintType.EQUAL, object);
        } else if (object instanceof String) {
            String valueString = (String) object;
            if (valueString.startsWith("<=")) { // pay attention to ordering when not using a parser like in this case.
                return new RuleCell(Index.ConstraintType.LESS_OR_EQUAL, parseConstrainedCellString(valueString.substring(2)));
            } else if (valueString.startsWith(">=")) {
                return new RuleCell(Index.ConstraintType.GREATER_OR_EQUAL, parseConstrainedCellString(valueString.substring(2)));
            } else if (valueString.startsWith("<")) {
                return new RuleCell(Index.ConstraintType.LESS_THAN, parseConstrainedCellString(valueString.substring(1)));
            } else if (valueString.startsWith(">")) {
                return new RuleCell(Index.ConstraintType.GREATER_THAN, parseConstrainedCellString(valueString.substring(1)));
            } else {
                return new RuleCell(Index.ConstraintType.EQUAL, parseConstrainedCellString(valueString));
            }
        } else {
            throw new IllegalStateException("unmanaged case, please report!");
        }
    }

    private Object parseConstrainedCellString(String substring) {
        try {
            return jsonMapper.readValue(substring, Object.class);
        } catch (Exception e) {
            return new IllegalStateException("unable to parse substring as JSON: "+substring, e);
        }
    }

    public static record RuleCell(Index.ConstraintType idxtype, Object value) {};

    private void appendInputs(List<Input> list) {
        for ( Input hi : list) {
            String nameString = hi.getName();
            @SuppressWarnings("unused")
            Class<?> typeRef = processType(hi.getType());
            definitions.ins().put(nameString, DataSource.createSingleton());
        }
    }

    private Class<?> processType(String string) {
        switch(string) {
            case "string" : 
            case "number" : 
            case "boolean" : 
            default: return Object.class; // TODO currently does not resolve external JSON Schemas
        }
    }
}
