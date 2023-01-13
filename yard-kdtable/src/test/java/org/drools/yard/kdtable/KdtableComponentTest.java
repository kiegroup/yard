package org.drools.yard.kdtable;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

public class KdtableComponentTest extends CamelTestSupport {

    @Test
    public void test() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);

        context().getPropertiesComponent().addInitialProperty("kdtable", "this value was set with addInitialProperty");

        template.sendBody("direct:start", "Hello World");

        mock.await();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                  .process(new KdtableProcessor())
                  .to("mock:result");
            }
        };
    }
}
