package org.kie.yard.impl2;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import org.drools.ruleunits.api.RuleUnitInstance;
import org.drools.ruleunits.api.RuleUnitProvider;
import org.drools.ruleunits.api.SingletonStore;
import org.drools.ruleunits.dsl.SyntheticRuleUnit;

public record YaRDRuleUnits (Map<String, SingletonStore<Object>> ins, List<SyntheticRuleUnit> units, Map<String, AtomicReference<Object>> outs) {

    public Map<String, Object> evaluate(Map<String, Object> context) {
        Map<String, Object> results = new LinkedHashMap<>(context);
        for (String inputKey : ins.keySet()) {
            if (!context.containsKey(inputKey)) {
                throw new IllegalArgumentException("missing input key in context: "+inputKey);
            }
            Object inputValue = context.get(inputKey);
            ins.get(inputKey).set(inputValue);
        }
        for (SyntheticRuleUnit unit : units) {
            RuleUnitInstance<SyntheticRuleUnit> unitInstance = RuleUnitProvider.get().createRuleUnitInstance(unit);
            unitInstance.fire();
        }
        for (Entry<String, AtomicReference<Object>> outputSets : outs.entrySet()) {
            results.put(outputSets.getKey(), outputSets.getValue());
        }
        return results;
    }
}
