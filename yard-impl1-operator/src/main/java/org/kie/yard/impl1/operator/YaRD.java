package org.kie.yard.impl1.operator;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("yard.kie.org")
@Version("v1alpha1")
public class YaRD extends CustomResource<YaRDSpec, YaRDStatus> implements Namespaced {
}