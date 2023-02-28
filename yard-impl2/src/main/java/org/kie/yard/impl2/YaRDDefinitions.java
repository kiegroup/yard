package org.kie.yard.impl2;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.drools.ruleunits.api.SingletonStore;

public record YaRDDefinitions (Map<String, SingletonStore<Object>> ins, List<Firable> units, Map<String, StoreHandle<Object>> outs) {

    public Map<String, Object> evaluate(Map<String, Object> context) {
        Map<String, Object> results = new LinkedHashMap<>(context);
        for (String inputKey : ins.keySet()) {
            if (!context.containsKey(inputKey)) {
                throw new IllegalArgumentException("missing input key in context: "+inputKey);
            }
            Object inputValue = context.get(inputKey);
            ins.get(inputKey).set(inputValue);
        }
        for (Firable unit : units) {
            unit.fire(context, this);
        }
        for (Entry<String, StoreHandle<Object>> outputSets : outs.entrySet()) {
            results.put(outputSets.getKey(), outputSets.getValue().get());
        }
        reset();
        return results;
    }
    
    // TODO to be revised for better concern separation
    private void reset() {
        ins.forEach((k, v) -> v.clear());
        outs.forEach((k, v) -> v.clear()); 
    }
}
