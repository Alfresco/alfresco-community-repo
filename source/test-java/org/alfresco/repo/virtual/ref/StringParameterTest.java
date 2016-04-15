
package org.alfresco.repo.virtual.ref;

import org.junit.Test;

import junit.framework.TestCase;

public class StringParameterTest extends TestCase
{
    @Test
    public void testStringParameter() throws Exception
    {
        StringParameter strParam1 = new StringParameter("value1");
        assertEquals("value1",
                     strParam1.getValue());

        String strRepresentation = strParam1.stringify(new PlainStringifier());

        assertEquals("s:value1",
                     strRepresentation);
    }
}
