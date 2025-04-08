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
import org.junit.Test;

import org.alfresco.service.cmr.repository.NodeRef;

public class ResourceParameterTest extends TestCase
{
    @Test
    public void testResourceParameter() throws Exception
    {
        RepositoryResource repoNodeRefResource = new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/0029-222-333-444")));
        RepositoryResource repoPathResource = new RepositoryResource(new RepositoryPath("/Foo/Bar"));
        ClasspathResource classpathResource = new ClasspathResource("/org/alfresco/");
        ResourceParameter repoNodeRefResourceParam = new ResourceParameter(repoNodeRefResource);
        ResourceParameter repoPathResourceParam = new ResourceParameter(repoPathResource);
        ResourceParameter classpathResourceParam = new ResourceParameter(classpathResource);

        //
        assertEquals(repoNodeRefResource,
                repoNodeRefResourceParam.getValue());

        String repoNodeRefResourceParamStrRepresentation = repoNodeRefResourceParam.stringify(new PlainStringifier());

        assertEquals("r:repository:node:workspace:SpacesStore:0029-222-333-444",
                repoNodeRefResourceParamStrRepresentation);

        //
        assertEquals(repoPathResource,
                repoPathResourceParam.getValue());

        String repoPathResourceParamStrRepresentation = repoPathResourceParam.stringify(new PlainStringifier());

        assertEquals("r:repository:path:/Foo/Bar",
                repoPathResourceParamStrRepresentation);

        //

        assertEquals(classpathResource,
                classpathResourceParam.getValue());

        String classpathResourceParamStrRepresentation = classpathResourceParam.stringify(new PlainStringifier());

        assertEquals("r:classpath:/org/alfresco/",
                classpathResourceParamStrRepresentation);
    }
}
