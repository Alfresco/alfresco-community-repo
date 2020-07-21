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

public abstract class AbstractReferenceParserTest extends TestCase
{
    @Test
    public void assertRecursiveReferenceParser(Encoding encoding) throws Exception
    {
        List<Parameter> params0 = Arrays
                    .<Parameter> asList(new StringParameter("/6"),
                                        new ResourceParameter(new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/67c8f11d-0936-4295-88a0-12b85764c76f")))));

        Reference innerRef0 = new Reference(Encodings.ZERO.encoding,
                                            Protocols.VANILLA.protocol,
                                            new ClasspathResource("/some/cp.json"),
                                            params0);

        List<Parameter> params1 = Arrays.<Parameter> asList(new StringParameter("/6"),
                                                            new ReferenceParameter(innerRef0));

        Reference innerRef1 = new Reference(Encodings.ZERO.encoding,
                                            Protocols.VANILLA.protocol,
                                            new ClasspathResource("/some/cp.json"),
                                            params1);

        List<Parameter> params2 = Arrays.<Parameter> asList(new ReferenceParameter(innerRef1));

        Reference innerRef2 = new Reference(Encodings.ZERO.encoding,
                                            Protocols.VANILLA.protocol,
                                            new ClasspathResource("/some/cp.json"),
                                            params2);

        List<Parameter> params = Arrays.<Parameter> asList(new ReferenceParameter(innerRef2),new ReferenceParameter(innerRef1),new StringParameter("AString"));

        Reference stringifiedReference = new Reference(Encodings.ZERO.encoding,
                                                       Protocols.NODE.protocol,
                                                       new RepositoryResource(new RepositoryPath("/A/repository/path/")),
                                                       params);

        String refString = encoding.stringifier.stringify(stringifiedReference);
        Reference parsedReference = encoding.parser.parse(refString);

        assertEquals(stringifiedReference,
                     parsedReference);
    }
}
