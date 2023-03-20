package org.kie.yard.impl2;

import java.util.Map;

public interface Firable {

    int fire(Map<String, Object> context, YaRDDefinitions units);

}