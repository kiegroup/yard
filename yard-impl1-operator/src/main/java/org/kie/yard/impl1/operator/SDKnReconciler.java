package org.kie.yard.impl1.operator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.knative.client.KnativeClient;
import io.fabric8.knative.serving.v1.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

@ControllerConfiguration
public class SDKnReconciler implements Reconciler<YaRD> {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory().disable(Feature.USE_NATIVE_TYPE_ID));

    @Inject
    KubernetesClient client;

    @Override
    public UpdateControl<YaRD> reconcile(YaRD resource, Context context) {
        String ns = resource.getMetadata().getNamespace();
        YaRDStatus status = resource.getStatus();

        YaRDSpec spec = resource.getSpec();
        org.kie.yard.api.model.YaRD yard = spec.getYard();

        if (yard != null) {
            yard.setName(serviceName(resource));
            System.out.println(yard);
            System.out.println(yard.getName());
            System.out.println(yard.getInputs());
            System.out.println(yard.getInputs().size());
        }
        final Optional<String> sdAsString = serializeYaRD(yard);
        System.out.println(sdAsString);

        final Service service = loadYaml(Service.class, "knative.yml"); // a YaRD Knative Service.
        service.getMetadata().setName(serviceName(resource));
        service.getSpec().getTemplate().getSpec().getContainers().get(0).setName(serviceName(resource));
        service.getMetadata().setNamespace(ns);
        sdAsString.ifPresent(s -> service.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(0).setValue(s));
        debugYaml(service);
        LOG.info("Creating Service {} in {}", service.getMetadata().getName(), ns);
        client.adapt(KnativeClient.class).services().inNamespace(ns).createOrReplace(service);

        // TODO owner

        resource.setStatus(status);
        return UpdateControl.updateStatus(resource);
    }
    
    private static void debugYaml(Object in) {
        try {
            System.out.println(MAPPER.writeValueAsString(in));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private static Optional<String> serializeYaRD(org.kie.yard.api.model.YaRD yard) {
        if (yard == null) {
            return Optional.empty();
        }
        try {
            final String ser = MAPPER.writeValueAsString(yard);
            return Optional.of(ser);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    private <T> T loadYaml(Class<T> clazz, String yaml) {
        try (InputStream is = getClass().getResourceAsStream(yaml)) {
            System.out.println(is);
            return Serialization.unmarshal(is, clazz);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot find yaml on classpath: " + yaml);
        }
    }

    private static String serviceName(YaRD resource) {
        return resource.getMetadata().getName();
    }
}
