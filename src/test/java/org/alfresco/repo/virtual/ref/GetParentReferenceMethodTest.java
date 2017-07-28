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

public class GetParentReferenceMethodTest extends TestCase
{
    private GetParentReferenceMethod method;

    @Override
    protected void setUp() throws Exception
    {
        method = new GetParentReferenceMethod();
    }

    @Test
    public void testSpacePath() throws Exception
    {
        final String path = "/Media types/Images";
        assertEquals("/Media types",
                     toParentPath(path));
    }

    @Test
    public void testTrailingPath() throws Exception
    {
        final String path = "/Media types/Images/";
        assertEquals("/Media types",
                     toParentPath(path));
    }

    @Test
    public void testFirsLevelPath() throws Exception
    {
        final String path = "/Media types";
        assertEquals("/",
                     toParentPath(path));
    }

    @Test
    public void testTrailingRoot() throws Exception
    {
        final String path = "/ ";
        assertNull(toParentPath(path));
    }

    @Test
    public void testRootPath() throws Exception
    {
        final String path = "/";
        assertNull(toParentPath(path));
    }

    private String toParentPath(final String path) throws ProtocolMethodException
    {
        List<Parameter> params = Arrays.<Parameter> asList(new StringParameter(path));
        Reference ref = new Reference(Encodings.PLAIN.encoding,
                                      Protocols.VIRTUAL.protocol,
                                      new ClasspathResource("/some/class/path.js"),
                                      params);
        Reference parent = ref.execute(method);
        if (parent == null)
        {
            return null;
        }
        else
        {
            StringParameter parentPath = (StringParameter) parent.getParameters().get(0);
            return parentPath.getValue();
        }
    }

}
