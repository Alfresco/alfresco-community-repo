/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.virtual.ref;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

public class ProtocolTest extends TestCase
{

    private StringParameter p1;

    private ResourceParameter p2;

    private List<ValueParameter<? extends Object>> parameters;

    private Reference r;

    private Protocol protocol = new Protocol("unitTest");

    @Override
    protected void setUp() throws Exception
    {
        p1 = new StringParameter("sp1");
        p2 = new ResourceParameter(new ClasspathResource("/r/p.js"));

        parameters = Arrays.asList(p1,
                                   p2);
        r = new Reference(Encodings.PLAIN.encoding,
                          Protocols.VIRTUAL.protocol,
                          new ClasspathResource("/a/b/c.class"),
                          parameters);
    }

    @Test
    public void testGetParameter() throws Exception
    {
        Parameter tp1 = protocol.getParameter(r,
                                              0);
        assertEquals(p1,
                     tp1);
        Parameter tp2 = protocol.getParameter(r,
                                              1);
        assertEquals(p2,
                     tp2);

        try
        {
            protocol.getParameter(r,
                                  -1);
            fail("Out of bounds!");
        }
        catch (IndexOutOfBoundsException e)
        {
            e.printStackTrace();
        }

        try
        {
            protocol.getParameter(r,
                                  2);
            fail("Out of bounds!");
        }
        catch (IndexOutOfBoundsException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testReplaceParameter() throws Exception
    {
        final StringParameter newParameter = new StringParameter("rep1");
        Reference rr1 = protocol.replaceParameter(r,
                                                  0,
                                                  newParameter);
        assertEquals(newParameter,
                     protocol.getParameter(rr1,
                                           0));

        try
        {
            protocol.replaceParameter(r,
                                      2,
                                      newParameter);
            fail("Out of bounds!");
        }
        catch (IndexOutOfBoundsException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddParameter() throws Exception
    {
        final StringParameter newParameter = new StringParameter("rep1");
        Reference rr1 = protocol.addParameter(r,
                                              newParameter);
        assertEquals(newParameter,
                     protocol.getParameter(rr1,
                                           2));

    }

    @Test
    public void testDispatch() throws Exception
    {
        boolean sccess = protocol.dispatch(new ProtocolMethod<Boolean>()
                                           {

                                               @Override
                                               public Boolean execute(VanillaProtocol vanillaProtocol,
                                                           Reference reference) throws ProtocolMethodException
                                               {
                                                   fail("Invalid dispatch");
                                                   return false;
                                               }

                                               @Override
                                               public Boolean execute(VirtualProtocol virtualProtocol,
                                                           Reference reference) throws ProtocolMethodException
                                               {
                                                   fail("Invalid dispatch");
                                                   return false;
                                               }

                                               @Override
                                               public Boolean execute(NodeProtocol protocol, Reference reference)
                                                           throws ProtocolMethodException
                                               {
                                                   fail("Invalid dispatch");
                                                   return false;
                                               }

                                               @Override
                                               public Boolean execute(Protocol protocol, Reference reference)
                                                           throws ProtocolMethodException
                                               {
                                                   return true;
                                               }
                                           },
                                           r);

        assertTrue(sccess);
    }
}
