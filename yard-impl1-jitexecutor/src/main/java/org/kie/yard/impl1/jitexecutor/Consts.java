package org.kie.yard.impl1.jitexecutor;

public class Consts {
    
    public static final String CONF_ENV_NAME = "myYaRD";
    public static final String DUMMY_YARD =
            "specVersion: alpha\r\nkind: YaRD\r\nname: 'BasePrice'\r\nexpressionLang: alpha\r\ninputs:\r\n - name: 'Age'\r\n   type: number\r\n - name: 'Previous incidents?'\r\n   type: boolean\r\nelements:\r\n - name: 'Base price'\r\n   type: Decision\r\n   logic: \r\n     type: DecisionTable\r\n     inputs: ['Age', 'Previous incidents?']\r\n     rules:\r\n      - when: ['<21', false]\r\n        then: 800\r\n      - when: ['<21', true]\r\n        then: 1000\r\n      - when: ['>=21', false]\r\n        then: 500\r\n      - when: ['>=21', true]\r\n        then: 600\r\n---\r\n";

    private Consts() {

    }
}
