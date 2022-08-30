package org.kie.yard.impl1;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.kie.yard.api.model.DecisionLogic;
import org.kie.yard.api.model.DecisionTable.InlineRule;
import org.kie.yard.api.model.DecisionTable.Rule;
import org.kie.yard.api.model.DecisionTable.WhenThenRule;
import org.kie.yard.api.model.Element;
import org.kie.yard.api.model.Input;
import org.kie.yard.api.model.YaRD;
import org.kie.dmn.feel.codegen.feel11.CodegenStringUtil;
import org.kie.dmn.model.api.DMNElementReference;
import org.kie.dmn.model.api.DRGElement;
import org.kie.dmn.model.api.Decision;
import org.kie.dmn.model.api.DecisionRule;
import org.kie.dmn.model.api.DecisionTable;
import org.kie.dmn.model.api.Definitions;
import org.kie.dmn.model.api.Expression;
import org.kie.dmn.model.api.HitPolicy;
import org.kie.dmn.model.api.InformationItem;
import org.kie.dmn.model.api.InformationRequirement;
import org.kie.dmn.model.api.InputClause;
import org.kie.dmn.model.api.InputData;
import org.kie.dmn.model.api.LiteralExpression;
import org.kie.dmn.model.api.OutputClause;
import org.kie.dmn.model.api.UnaryTests;
import org.kie.dmn.model.v1_3.KieDMNModelInstrumentedBase;
import org.kie.dmn.model.v1_3.TDMNElementReference;
import org.kie.dmn.model.v1_3.TDecision;
import org.kie.dmn.model.v1_3.TDecisionRule;
import org.kie.dmn.model.v1_3.TDecisionTable;
import org.kie.dmn.model.v1_3.TDefinitions;
import org.kie.dmn.model.v1_3.TInformationItem;
import org.kie.dmn.model.v1_3.TInformationRequirement;
import org.kie.dmn.model.v1_3.TInputClause;
import org.kie.dmn.model.v1_3.TInputData;
import org.kie.dmn.model.v1_3.TLiteralExpression;
import org.kie.dmn.model.v1_3.TOutputClause;
import org.kie.dmn.model.v1_3.TUnaryTests;

public class YaRDParser {

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public YaRDParser() {
        mapper.findAndRegisterModules();
    }

    public Definitions parse(String yaml) throws Exception {
        YaRD sd = mapper.readValue(yaml, YaRD.class);
        Definitions defs = new TDefinitions();
        enrichDefinitions(defs, Optional.ofNullable(sd.getName()).orElse("defaultName"));
        defs.setExpressionLanguage(Optional.ofNullable(sd.getExpressionLang()).orElse(KieDMNModelInstrumentedBase.URI_FEEL));
        appendInputData(defs, sd.getInputs());
        appendDecision(defs, sd.getElements());
        return defs;
    }

    private void appendDecision(Definitions definitions, List<Element> list) {
        for ( Element hi : list) {
            String nameString = (String) hi.getName();
            Decision decision = createDecisionWithWiring(definitions, nameString);
            Expression decisionLogic = createDecisionLogic(hi.getLogic());
            decision.setExpression(decisionLogic);
            definitions.getDrgElement().add(decision);
        }
    }

    private Expression createDecisionLogic(DecisionLogic decisionLogic) {
        if (decisionLogic instanceof org.kie.yard.api.model.DecisionTable) {
            return createDecisionTable((org.kie.yard.api.model.DecisionTable) decisionLogic);
        } else if (decisionLogic instanceof org.kie.yard.api.model.LiteralExpression) {
            return createLiteralExpression((org.kie.yard.api.model.LiteralExpression) decisionLogic);
        } else {
            throw new UnsupportedOperationException("TODO");
        }
    }

    private Expression createLiteralExpression(org.kie.yard.api.model.LiteralExpression logic) {
        LiteralExpression lt = new TLiteralExpression();
        lt.setText(logic.getExpression());
        return lt;
    }

    private Expression createDecisionTable(org.kie.yard.api.model.DecisionTable logic) {
        List<String> inputs = logic.getInputs();
        List<Rule> rules = logic.getRules();
        DecisionTable dt = new TDecisionTable();
        dt.setHitPolicy(HitPolicy.fromValue(logic.getHitPolicy()));
        for (String i : inputs) {
            InputClause ic = new TInputClause();
            ic.setLabel(i);
            LiteralExpression le = new TLiteralExpression();
            le.setText(i);
            ic.setInputExpression(le);
            dt.getInput().add(ic);
        }
        // TODO check if DT defines a set of outputsComponents.
        OutputClause oc = new TOutputClause();
        dt.getOutput().add(oc);
        for (Rule yamlRule : rules) {
            DecisionRule dr = createDecisionRule(yamlRule);
            dt.getRule().add(dr);
        }
        return dt;
    }

    private DecisionRule createDecisionRule(Rule yamlRule) {
        if (yamlRule instanceof WhenThenRule) {
            return createDecisionRuleWhenThen((WhenThenRule) yamlRule);
        } else if (yamlRule instanceof InlineRule) {
            throw new UnsupportedOperationException("Rule in simple array form not supported; use WhenThenRule form.");
        } else {
            throw new UnsupportedOperationException("?");
        }
    }

    private DecisionRule createDecisionRuleWhenThen(WhenThenRule yamlRule) {
        List<Object> when = yamlRule.getWhen();
        Object then = yamlRule.getThen();
        DecisionRule dr = new TDecisionRule();
        for (Object w : when) {
            UnaryTests ut = new TUnaryTests();
            ut.setText(w.toString());
            dr.getInputEntry().add(ut);
        }
        if (!(then instanceof Map)) {
            LiteralExpression le = new TLiteralExpression();
            le.setText(then.toString());
            dr.getOutputEntry().add(le);
        } else {
            throw new UnsupportedOperationException("TODO complex output type value not supported yet.");
        }
        return dr;
    }

    private Decision createDecisionWithWiring(Definitions definitions, String nameString) {
        Decision decision = new TDecision();
        decision.setName(nameString);
        decision.setId("d_" + CodegenStringUtil.escapeIdentifier(nameString));
        InformationItem variable = new TInformationItem();
        variable.setName(nameString);
        variable.setId("dvar_" + CodegenStringUtil.escapeIdentifier(nameString));
        variable.setTypeRef(new QName("Any"));
        decision.setVariable(variable);
        // TODO, for the moment, we hard-code the wiring of the requirement in the order of the YAML
        for (DRGElement drgElement : definitions.getDrgElement()) {
            InformationRequirement ir = new TInformationRequirement();
            DMNElementReference er = new TDMNElementReference();
            er.setHref("#" + drgElement.getId());
            if (drgElement instanceof InputData) {
                ir.setRequiredInput(er);
            } else if (drgElement instanceof Decision) {
                ir.setRequiredDecision(er);
            } else {
                throw new IllegalStateException();
            }
            decision.getInformationRequirement().add(ir);
        }
        return decision;
    }

    private void appendInputData(Definitions definitions, List<Input> list) {
        for ( Input hi : list) {
            String nameString = hi.getName();
            QName typeRef = processType(hi.getType());
            InputData id = createInputData(nameString, typeRef);
            definitions.getDrgElement().add(id);
        }
    }

    private InputData createInputData(String nameString, QName typeRef) {
        InputData id = new TInputData();
        id.setName(nameString);
        id.setId("id_"+CodegenStringUtil.escapeIdentifier(nameString));
        InformationItem variable = new TInformationItem();
        variable.setName(nameString);
        variable.setId("idvar_"+CodegenStringUtil.escapeIdentifier(nameString));
        variable.setTypeRef(typeRef);
        id.setVariable(variable);
        return id;
    }

    private QName processType(String string) {
        switch(string) {
            case "string" : return new QName("string");
            case "number" : return new QName("number");
            case "boolean" : return new QName("boolean");
            default: return new QName("Any"); // TODO currently does not resolve external JSON Schemas
        }
    }

    private void enrichDefinitions(Definitions defs, String modelName) {
        setDefaultNSContext(defs);
        defs.setId("dmnid_" + modelName);
        defs.setName(modelName);
        String namespace = this.getClass().getPackage().getName().replaceAll("\\.", "_") + "_" + UUID.randomUUID();
        defs.setNamespace(namespace);
        defs.getNsContext().put(XMLConstants.DEFAULT_NS_PREFIX, namespace);
        defs.setExporter(this.getClass().getName());
    }

    private void setDefaultNSContext(Definitions definitions) {
        Map<String, String> nsContext = definitions.getNsContext();
        nsContext.put("feel", KieDMNModelInstrumentedBase.URI_FEEL);
        nsContext.put("dmn", KieDMNModelInstrumentedBase.URI_DMN);
        nsContext.put("dmndi", KieDMNModelInstrumentedBase.URI_DMNDI);
        nsContext.put("di", KieDMNModelInstrumentedBase.URI_DI);
        nsContext.put("dc", KieDMNModelInstrumentedBase.URI_DC);
    }
}
