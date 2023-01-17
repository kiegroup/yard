package org.drools.yard.kdtable;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.kie.yard.api.model.DecisionTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class KdtableProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(KdtableProcessor.class);

    private Object kdtable;
    private String expressionLang;
    private DecisionTable dt;

    @Override
    public void process(Exchange exchange) throws Exception {
        LOG.info("this processor/bean field 'kdtable' was set to: {}", kdtable);
        LOG.info("this processor/bean field 'kdtable' class is: {}", kdtable.getClass());
        LOG.info("the deserialized DT, rules in total are: {}", dt.getRules().size());
    }

    public Object getKdtable() {
        return kdtable;
    }

    public void setKdtable(Object kdtable) {
        this.kdtable = kdtable;
        if (!(kdtable instanceof String)) {
            throw new IllegalArgumentException();
        }
        try {
            dt = new ObjectMapper(new YAMLFactory()).readValue((String) kdtable, DecisionTable.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getExpressionLang() {
        return expressionLang;
    }

    public void setExpressionLang(String expressionLang) {
        this.expressionLang = expressionLang;
    }
}
