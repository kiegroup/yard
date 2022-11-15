package org.kie.yard.impl1.cli;

import java.io.File;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.json.JsonMapper;

import org.drools.io.ReaderResource;
import org.drools.util.IoUtils;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.api.marshalling.DMNMarshaller;
import org.kie.dmn.backend.marshalling.v1x.DMNMarshallerFactory;
import org.kie.dmn.core.internal.utils.DMNRuntimeBuilder;
import org.kie.dmn.core.internal.utils.DynamicDMNContextBuilder;
import org.kie.dmn.core.internal.utils.MarshallingStubUtils;
import org.kie.dmn.model.api.Definitions;
import org.kie.yard.impl1.YaRDParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "yard",
    description = {"Evaluate a YaRD using the impl1 (Drools DMN Engine).",
                "The input Context must be provided as a JSON object.",
                "The result of the YaRD evaluation is emitted as a JSON object on STDOUT."},
    footer = "See also: https://drools.org",
    mixinStandardHelpOptions = true)
public class Yard implements Callable<Integer> {

    private static final Logger LOG = LoggerFactory.getLogger(Yard.class);

    @Parameters(
        index = "0",
        description = "The YaRD file to evaluate.",
        arity = "1")
    private File inputModel;

    @Parameters(
        index = "1",
        description = "The input Context as JSON, for evaluation. If left empty, will read from STDIN.",
        arity = "0..1"
    )
    private String context;

    private JsonMapper jsonMapper = JsonMapper.builder().build();

    public static void main(String[] args) throws Exception {
        int exitCode = new CommandLine(new Yard()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            initContext();
            String yamlDecision = IoUtils.readFileAsString(inputModel);
    
            DMNMarshaller dmnMarshaller = DMNMarshallerFactory.newDefaultMarshaller();
            YaRDParser parser = new YaRDParser();
            
            Definitions definitions = parser.parse(yamlDecision);
            String xml = dmnMarshaller.marshal(definitions);
            LOG.debug("{}", xml);
    
            DMNRuntime dmnRuntime = DMNRuntimeBuilder.fromDefaults()
                    .buildConfiguration()
                    .fromResources(Arrays.asList(new ReaderResource(new StringReader(xml))))
                    .getOrElseThrow(RuntimeException::new);
            Map<String, Object> readValue = readJSON(context);
            DMNContext dmnContext = new DynamicDMNContextBuilder(dmnRuntime.newContext(), dmnRuntime.getModels().get(0))
                    .populateContextWith(readValue);
            DMNResult dmnResult = dmnRuntime.evaluateAll(dmnRuntime.getModels().get(0), dmnContext);
            Map<String, Object> onlyOutputs = new LinkedHashMap<>();
            for (DMNDecisionResult r : dmnResult.getDecisionResults()) {
                onlyOutputs.put(r.getDecisionName(), r.getResult());
            }
            // TODO to make a config flag to retain also inputs? that was helpful lesson with dealing with CloudEvents and OB
            Object serialized = MarshallingStubUtils.stubDMNResult(onlyOutputs, Object::toString);
            final String OUTPUT_JSON = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(serialized);
            System.out.println(OUTPUT_JSON); // we really want to emit to sysout
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readJSON(final String CONTEXT) throws Exception {
        return jsonMapper.readValue(CONTEXT, Map.class);
    }

    /**
     * Init context from STDIN if necessary.
     */
    private void initContext() {
        if (context == null) {
            context = "{}";
            try (Scanner scanner = new Scanner(System.in).useDelimiter("\\A")) {
                if (scanner.hasNext()) {
                    context = scanner.next();
                }
            }
        }
    }
}