package org.kie.yard.quarkus;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import org.kie.yard.api.model.Input;
import org.kie.yard.quarkus.model.Configuration;
import org.kie.yard.quarkus.model.Output;
import org.kie.yard.quarkus.model.ScorecardConfiguration;

import java.util.*;

public class InputLoader {

    private final Map<String, RemoteSource> remoteSources = new HashMap<>();

    public Map<String, Object> resolve(final ScorecardConfiguration configuration,
                                       final List<Input> inputs) throws NotFoundException {
        // TODO store loaded values

        final Map<String, Loader> loaders = getLoaders(configuration, inputs);

        final HashMap<String, Object> result = new HashMap<>();
        for (final String key : loaders.keySet()) {
            result.put(key, loaders.get(key).load());
        }
        return result;
    }

    private Map<String, Loader> getLoaders(final ScorecardConfiguration configuration,
                                           final List<Input> inputs) throws NotFoundException {
        final HashMap<String, Loader> loaders = new HashMap<>();
        final List<Input> notFound = new ArrayList<>();

        for (Input input : inputs) {
            final Optional<Loader> o = contains(input, configuration.configuration());
            if (o.isPresent()) {
                loaders.put(input.getName(), o.get());
            } else {
                notFound.add(input);
            }
        }

        if (!notFound.isEmpty()) {
            if (configuration.parent().isPresent()) {
                loaders.putAll(getLoaders(configuration.parent().get(), notFound));
            } else {
                throw new NotFoundException(notFound);
            }
        }

        return loaders;
    }

    private Optional<Loader> contains(final Input input,
                                      final Configuration configuration) {

        for (Output output : configuration.outputs()) {
            if (Objects.equals(input.getName(), output.name())) {

                return Optional.of(
                        new Loader(
                                output,
                                getRemoteSource(configuration.api())));
            }
        }

        return Optional.empty();
    }

    private RemoteSource getRemoteSource(String api) {
        if (remoteSources.containsKey(api)) {
            return remoteSources.get(api);
        } else {
            return new RemoteSource(api);
        }
    }

    private static class Loader {

        private final RemoteSource remoteSource;
        private final Output output;

        public Loader(final Output output,
                      final RemoteSource remoteSource) {
            this.output = output;
            this.remoteSource = remoteSource;
        }

        public Object load() {
            return remoteSource.get(output.from());
        }
    }

    private static class RemoteSource {
        private final String api;
        private Map<String, Object> response;

        public RemoteSource(final String api) {
            this.api = api;
        }

        public Object get(String name) {
            if (response == null) {
                response =
                        ClientBuilder.newClient().
                                target(api).
                                request(MediaType.APPLICATION_JSON).
                                get(Map.class);
            }

            Object o = response.get(name);
            return o;
        }
    }
}
