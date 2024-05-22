package org.kie.yard.quarkus.model;

import org.kie.yard.api.model.YaRD;
import org.kie.yard.core.YaRDRunner;

import java.io.IOException;
import java.util.Map;

public record ScorecardModule(ScorecardConfiguration configuration, YaRD model, String yaml) {


    public Map<String, Object> run(final Map<String, Object> map) throws IOException {
        final YaRDRunner runner = new YaRDRunner(model);
        return runner.evaluate(map);
    }
}

