package org.openspaces.itest.esb.mule.seda;

import org.mule.umo.UMOMessage;
import org.openspaces.itest.esb.mule.AbstractMuleTests;

/**
 * @author kimchy
 */
public class SimpleSedaTests extends AbstractMuleTests {

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/esb/mule/seda/simple.xml"};
    }

    public void testSimpleSedaHandling() throws Exception {
        muleClient.dispatch("vm://test1", "testme", null);

        UMOMessage message = muleClient.request("vm://test3", 5000000);
        assertEquals("testme", message.getPayload());
    }
}