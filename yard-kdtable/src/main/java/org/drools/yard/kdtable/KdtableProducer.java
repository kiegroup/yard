package org.drools.yard.kdtable;

import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KdtableProducer extends DefaultProducer {
    private static final Logger LOG = LoggerFactory.getLogger(KdtableProducer.class);
    private KdtableEndpoint endpoint;
    private Optional<String> buildtime;

    public KdtableProducer(KdtableEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
        LOG.info(endpoint.getName());
        buildtime = endpoint.getCamelContext().getPropertiesComponent().resolveProperty(endpoint.getName());
        LOG.info(buildtime.toString());
    }

    public void process(Exchange exchange) throws Exception {
        LOG.info("{} but runtime: {}", buildtime, exchange.getContext().getPropertiesComponent().resolveProperty(endpoint.getName()));
    }

}
