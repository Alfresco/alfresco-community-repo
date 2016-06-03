package org.alfresco.repo.search;

import java.util.Iterator;

import org.jaxen.UnsupportedAxisException;
import junit.framework.TestCase;

public class DocumentNavigatorTest extends TestCase
{
    public void testGetChildAxisIterator() throws UnsupportedAxisException
    {
        try
        {
            // Check true
            DocumentNavigator docNav = new DocumentNavigator(null, null, null, null, false, false);
            Iterator nodeIter = docNav.getChildAxisIterator(null, "true", "", null);
            assertNotNull(nodeIter);
            boolean value = (Boolean) nodeIter.next();
            assertTrue("The true value should be returned. See MNT-10730", value);

            // Check false
            nodeIter = docNav.getChildAxisIterator(null, "false", "", null);
            assertNotNull(nodeIter);
            value = (Boolean) nodeIter.next();
            assertFalse("The false value should be returned. See MNT-10730", value);
        }
        catch (NullPointerException exp)
        {
            fail("The boolean value should be returned. See MNT-10730");
        }
    }
}