package org.drools.yard.kdtable;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KdtableProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(KdtableProcessor.class);

    public static final String PROP_KEY = "kdtable";

    @Override
    public void process(Exchange exchange) throws Exception {
        LOG.info("for prop key {} value {}", PROP_KEY, exchange.getContext().getPropertiesComponent().resolveProperty(PROP_KEY));
    }
    
}
