package org.drools.yard.kdtable;

import org.apache.camel.Category;
import org.apache.camel.Producer;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;
import org.apache.camel.support.ProcessorEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * kdtable component which does bla bla.
 *
 * TODO: Update one line description above what the component does.
 */
@UriEndpoint(firstVersion = "1.0-SNAPSHOT", scheme = "kdtable", title = "kdtable", syntax="kdtable:name",
             producerOnly = true,
             category = {Category.JAVA})
public class KdtableEndpoint extends ProcessorEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(KdtableEndpoint.class);

    @UriPath @Metadata(required = true)
    private String name;
    @UriParam(defaultValue = "10")
    private int option = 10;

    public KdtableEndpoint(String uri, KdtableComponent component) {
        super(uri, component);
        // TODO check with Camel SME
        setName(getEndpointBaseUri().replace("kdtable://", ""));
    }

    @Override
    public Producer createProducer() throws Exception {
        return new KdtableProducer(this);
    }

    /**
     * Some description of this option, and what it does
     */
    public void setName(String name) {
        LOG.debug("setName with {}", name);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Some description of this option, and what it does
     */
    public void setOption(int option) {
        this.option = option;
    }

    public int getOption() {
        return option;
    }
}
