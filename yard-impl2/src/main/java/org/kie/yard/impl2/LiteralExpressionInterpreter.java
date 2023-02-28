package org.kie.yard.impl2;

import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class LiteralExpressionInterpreter implements Firable {
    private final String name;
    private final QuotedExprParsed quoted;
    private final ScriptEngine engine;
    private final CompiledScript compiledScript;

    public LiteralExpressionInterpreter(String nameString, QuotedExprParsed quotedExprParsed) {
        this.name = nameString;
        this.quoted = quotedExprParsed;
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            engine = manager.getEngineByName("jshell");
            Compilable compiler = (Compilable) engine;
            compiledScript = compiler.compile(quoted.getRewrittenExpression());
        } catch (Exception e) {
            throw new IllegalArgumentException("parse error", e);
        }
    }

    @Override
    public int fire(Map<String, Object> context, YaRDDefinitions units) {
        Bindings bindings = engine.createBindings();
        // deliberately escape all symbols; a normal symbol will
        // never be in the detected-by-unquoting set, so this
        // set can't be used to selectively put in scope
        for (Entry<String, Object> inKV : context.entrySet()) {
            bindings.put(QuotedExprParsed.escapeIdentifier(inKV.getKey()), inKV.getValue());
        }
        for (Entry<String, StoreHandle<Object>> outKV : units.outs().entrySet()) {
            if (!outKV.getValue().isValuePresent()) {
                continue;
            }
            bindings.put(QuotedExprParsed.escapeIdentifier(outKV.getKey()), outKV.getValue().get());
        }
        try {
            var result = compiledScript.eval(bindings);
            units.outs().get(name).set(result);
            return 1;
        } catch (ScriptException e) {
            throw new RuntimeException("interpretation failed at runtime", e);
        }
    }
}
