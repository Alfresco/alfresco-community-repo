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

import junit.framework.TestCase;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

public class PlainStringifierTest extends TestCase
{
    @Test
    public void testEncode1() throws Exception
    {
        Reference r = new Reference(Encodings.ZERO.encoding,
                                    Protocols.VIRTUAL.protocol,
                                    new ClasspathResource("/org/alfresco/"));
        assertEquals("virtual:classpath:/org/alfresco/",
                     r.encode(Encodings.PLAIN.encoding));
    }

    @Test
    public void testStringifyReference() throws Exception
    {
        Reference r = new Reference(Encodings.PLAIN.encoding,
                                    Protocols.VIRTUAL.protocol,
                                    new ClasspathResource("/org/alfresco/"));

    }

    public void testStringifyRepositoryNodeRef() throws Exception
    {

        RepositoryResource rr1 = new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/0029-222-333-444")));
        Reference r = new Reference(Encodings.PLAIN.encoding,
                                    Protocols.VIRTUAL.protocol,
                                    rr1);
        assertEquals("virtual:repository:node:workspace:SpacesStore:0029-222-333-444",
                     r.encode(Encodings.PLAIN.encoding));

    }

    public void testStringifyRepositoryPath() throws Exception
    {
        RepositoryResource rr2 = new RepositoryResource(new RepositoryPath("/Data Dictionary/Virtual Folders/claim.json"));
        Reference r = new Reference(Encodings.PLAIN.encoding,
                                    Protocols.VIRTUAL.protocol,
                                    rr2);
        assertEquals("virtual:repository:path:/Data Dictionary/Virtual Folders/claim.json",
                     r.encode(Encodings.PLAIN.encoding));

    }

}
