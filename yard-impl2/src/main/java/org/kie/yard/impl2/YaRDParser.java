package org.kie.yard.impl2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import org.drools.model.Index;
import org.drools.ruleunits.api.DataSource;
import org.drools.ruleunits.api.SingletonStore;
import org.drools.ruleunits.dsl.SyntheticRuleUnit;
import org.drools.ruleunits.dsl.SyntheticRuleUnitBuilder;
import org.drools.ruleunits.dsl.patterns.Pattern1Def;
import org.drools.ruleunits.dsl.patterns.Pattern2Def;
import org.kie.yard.api.model.DecisionLogic;
import org.kie.yard.api.model.DecisionTable.InlineRule;
import org.kie.yard.api.model.DecisionTable.Rule;
import org.kie.yard.api.model.DecisionTable.WhenThenRule;
import org.kie.yard.api.model.Element;
import org.kie.yard.api.model.Input;
import org.kie.yard.api.model.YaRD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YaRDParser {
    private static final Logger LOG = LoggerFactory.getLogger(YaRDParser.class);

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final YaRDRuleUnits definitions = new YaRDRuleUnits(new HashMap<>(), new ArrayList<>(), new HashMap<>());

    public YaRDParser() {
        yamlMapper.findAndRegisterModules();
        jsonMapper.findAndRegisterModules();
    }

    public YaRDRuleUnits parse(String yaml) throws Exception {
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
            SyntheticRuleUnit decisionLogic = createDecisionLogic(nameString, hi.getLogic());
            definitions.units().add(decisionLogic);
        }
    }

    private SyntheticRuleUnit createDecisionLogic(String nameString, DecisionLogic decisionLogic) {
        if (decisionLogic instanceof org.kie.yard.api.model.DecisionTable) {
            return createDecisionTable(nameString, (org.kie.yard.api.model.DecisionTable) decisionLogic);
        } else if (decisionLogic instanceof org.kie.yard.api.model.LiteralExpression) {
            throw new UnsupportedOperationException("Not implemented in impl2 / TODO");
        } else {
            throw new UnsupportedOperationException("Not implemented in impl2 / TODO");
        }
    }

    private SyntheticRuleUnit createDecisionTable(String nameString, org.kie.yard.api.model.DecisionTable logic) {
        List<String> inputs = logic.getInputs();
        if (inputs.isEmpty()) {
            throw new IllegalStateException("empty decision table?");
        }
        List<Rule> rules = logic.getRules();
        if (!(logic.getHitPolicy() == null || logic.getHitPolicy().equals("ANY"))) {
            throw new UnsupportedOperationException("Not implemented in impl2 / TODO");
        }
        SyntheticRuleUnitBuilder unit = SyntheticRuleUnitBuilder.build(nameString);
        for (Entry<String, SingletonStore<Object>> e : definitions.ins().entrySet()) {
            unit.registerDataSource(e.getKey(), e.getValue(), Object.class);
        }
        AtomicReference<Object> result = new AtomicReference<Object>();
        unit.registerGlobal(nameString, result);
        definitions.outs().put(nameString, result);
        var sru = unit.defineRules(rulesFactory -> {
            for (Rule rule : rules) {
                RuleCell r0 = parseGenericRuleCell(rule, 0);
                LOG.debug("r0 {} {}", r0.idxtype, r0.value);
                Pattern1Def<Object> building1 = rulesFactory.rule()
                    .on(definitions.ins().get(inputs.get(0)))
                    .filter(r0.idxtype, r0.value);
                if (inputs.size() > 1) { // TODO this current approach is not really scalable, will be reviewed.
                    RuleCell r1 = parseGenericRuleCell(rule, 1);
                    LOG.debug("r1 {} {}", r1.idxtype, r1.value);
                    Pattern2Def<Object, Object> building2 = building1.join(rr -> rr
                        .on(definitions.ins().get(inputs.get(1)))
                        .filter(r1.idxtype, r1.value)
                    );
                    if (inputs.size() > 2) {
                        throw new UnsupportedOperationException("unhandled DT with more than 2 columns");
                    } else {
                        building2.execute(result, (r, c1, c2) -> { r.set( parseGenericRuleThen(rule).value ); }); // TODO this does not need the join, maybe is the easiest for a generic DSL option.
                    }
                } else {
                    building1.execute(result, (r, c1) -> { r.set( parseGenericRuleThen(rule).value ); });
                }
            }
        });
        return sru;
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
