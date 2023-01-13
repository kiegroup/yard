package org.drools.yard.kdtable;

import java.util.Properties;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.spi.PropertiesComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KdtableProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(KdtableProcessor.class);

    public static final String PROP_KEY = "kdtable";

    private String kdtable;

    @Override
    public void process(Exchange exchange) throws Exception {
        LOG.info("this processor/bean field 'kdtable' was set to: {}", kdtable);
        PropertiesComponent pc = exchange.getContext().getPropertiesComponent();
        LOG.info("for prop key {} value {}", PROP_KEY, pc.resolveProperty(PROP_KEY));
        Properties kdtableProps = pc.loadProperties(p -> p.contains(PROP_KEY));
        LOG.info("kdtableProps: {}", kdtableProps);
    }

    public String getKdtable() {
        return kdtable;
    }

    public void setKdtable(String kdtable) {
        this.kdtable = kdtable;
    }
}
