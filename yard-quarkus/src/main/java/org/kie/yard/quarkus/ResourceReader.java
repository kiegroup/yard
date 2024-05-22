package org.kie.yard.quarkus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.yard.api.model.YaRD;
import org.kie.yard.api.model.YaRD_YamlMapperImpl;
import org.kie.yard.quarkus.model.Configuration;
import org.kie.yard.quarkus.model.ScorecardConfiguration;
import org.kie.yard.quarkus.model.ScorecardModule;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@ApplicationScoped
public class ResourceReader {

    @ConfigProperty(name = "yard.scorecards.folder")
    String folderName;
    private static final PathMatcher FILE_TYPE_MATCHERS = FileSystems.getDefault().getPathMatcher("glob:*yard.yml");

    public Collection<ScorecardModule> readModules() {
        final Collection<ScorecardModule> modules = new ArrayList<>();
        try {

            Files.walkFileTree(Paths.get(folderName),
                    new SimpleFileVisitor<Path>() {

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

                            final Configuration configuration = readConfiguration(file);

                            if (FILE_TYPE_MATCHERS.matches(file.getFileName())) {

                                try {
                                    final String yaml = Files.readString(file);
                                    final YaRD model = new YaRD_YamlMapperImpl().read(yaml);
                                    modules.add(
                                            new ScorecardModule(
                                                    new ScorecardConfiguration(Optional.empty(), configuration),
                                                    model,
                                                    yaml));
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return modules;
    }

    private Configuration readConfiguration(Path file) {
        try {
            final Path confPath = file.getParent().resolve("configuration.yml");
            final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
            objectMapper.findAndRegisterModules();
            return objectMapper.readValue(confPath.toFile(), Configuration.class);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
