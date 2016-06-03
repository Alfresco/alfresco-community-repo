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

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

public class NewVirtualReferenceMethodTest extends TestCase
{
    @Test
    public void testExecute() throws Exception
    {
        final NodeRef templateRef = new NodeRef("workspace://SpacesStore/0029-2222-333-4424");
        final String templatePath = "/new/ref/path";
        final NodeRef actualNodeRef = new NodeRef("workspace://SpacesStore/2229-1234-5678-9012");
        NewVirtualReferenceMethod newVirtualReferenceMethod = new NewVirtualReferenceMethod(templateRef,
                                                                                            templatePath,
                                                                                            actualNodeRef,
                                                                                            null);
        Reference ref = Protocols.VIRTUAL.protocol.dispatch(newVirtualReferenceMethod,
                                                            null);
        final List<Parameter> expectedParams = Arrays
                    .<Parameter> asList(new StringParameter(templatePath),
                                        new ResourceParameter(new RepositoryResource(new RepositoryNodeRef(actualNodeRef))));
        final Reference expectedRef = new Reference(Encodings.PLAIN.encoding,
                                                    Protocols.VIRTUAL.protocol,
                                                    new RepositoryResource(new RepositoryNodeRef(templateRef)),
                                                    expectedParams);

        assertEquals(expectedRef,
                     ref);
    }
}
