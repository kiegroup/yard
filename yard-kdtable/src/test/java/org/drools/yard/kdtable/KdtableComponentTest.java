package org.drools.yard.kdtable;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

public class KdtableComponentTest extends CamelTestSupport {

    private static final String TEST_DT = "type: DecisionTable\n"
    +"inputs: ['Age', 'Previous incidents?']\n"
    +"rules:\n"
    +"- when: ['<21', false]\n"
    +"  then: 800\n"
    +"- when: ['<21', true]\n"
    +"  then: 1000\n"
    +"- when: ['>=21', false]\n"
    +"  then: 500\n"
    +"- when: ['>=21', true]\n"
    +"  then: 600\n";

    @Test
    public void test() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedBodyReceived().body().contains("\"kdtable\": 1000");

        template.sendBody("direct:start", "{ \"Age\": 19, \"Previous incidents?\": true }");

        mock.await();
        mock.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        KdtableProcessor processor = new KdtableProcessor();
        processor.setKdtable(TEST_DT); // here so that context+registry is available to bind bean, but right before route.
        context().getRegistry().bind("mmbean", processor);

        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                  .bean("mmbean")
                  .to("log:info")
                  .to("mock:result");
            }
        };
    }
}
