package org.kie.yard.quarkus.model;


import java.util.*;

public record ScorecardConfiguration(Optional<ScorecardConfiguration> parent,
                                     Configuration configuration) {

}
