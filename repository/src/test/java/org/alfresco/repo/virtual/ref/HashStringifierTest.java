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

import java.util.Collections;

import junit.framework.TestCase;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

public class HashStringifierTest extends TestCase
{
    private HashStore cpHashStore;

    public void setUp()
    {

        cpHashStore = HashStoreConfiguration.getInstance().getClasspathHashStore();
        cpHashStore.put("/com/alfresco",
                        "1");

    }

    private Reference stringifyParse(Reference reference)
    {
        String encodedReferenceString = new HashStringifier().stringify(reference);
        Reference paresedReference = new HashReferenceParser().parse(encodedReferenceString);

        return paresedReference;
    }

    @Test
    public void testStringifyVirtualReference_nodeRef()
    {
        NodeRef templateNodeRef = new NodeRef("workspace://SpacesStore/0d3b26ff-c4c1-4680-8622-8608ea7ab4b2");
        NodeRef actualNode = new NodeRef("workspace://SpacesStore/67c8f11d-0936-4295-88a0-12b85764c76f");

        Reference virtualReference = ((VirtualProtocol) Protocols.VIRTUAL.protocol).newReference(templateNodeRef,
                                                                                                 "/1/2/3",
                                                                                                 actualNode);
        Reference spReference = stringifyParse(virtualReference);

        assertEquals(virtualReference,
                     spReference);

    }

    @Test
    public void testStringifyVirtualReference_classpath()
    {
        ClasspathResource classpathTemplate = new ClasspathResource("/com/alfresco/template.js");
        NodeRef actualNode = new NodeRef("workspace://SpacesStore/67c8f11d-0936-4295-88a0-12b85764c76f");
        RepositoryResource actualNodeResource = new RepositoryResource(new RepositoryNodeRef(actualNode));
        Reference virtualReference = ((VirtualProtocol) Protocols.VIRTUAL.protocol)
                    .newReference(Encodings.PLAIN.encoding,
                                  classpathTemplate,
                                  "/1/2/3",
                                  actualNodeResource,
                                  Collections.<Parameter> emptyList());
        Reference spReference = stringifyParse(virtualReference);

        assertEquals(virtualReference,
                     spReference);

    }

    @Test
    public void testStringifyVirtualReference_actualRepositoryPath()
    {
        ClasspathResource classpathTemplate = new ClasspathResource("/com/alfresco/template.js");
        RepositoryResource actualNodeResource = new RepositoryResource(new RepositoryPath("/app:company_home/cm:aVirtualFolder"));
        Reference virtualReference = ((VirtualProtocol) Protocols.VIRTUAL.protocol)
                    .newReference(Encodings.PLAIN.encoding,
                                  classpathTemplate,
                                  "/1/2/3",
                                  actualNodeResource,
                                  Collections.<Parameter> emptyList());
        Reference spReference = stringifyParse(virtualReference);

        assertEquals(virtualReference,
                     spReference);

    }

    @Test
    public void testStringifyVirtualReference_root()
    {
        NodeRef templateNodeRef = new NodeRef("workspace://SpacesStore/0d3b26ff-c4c1-4680-8622-8608ea7ab4b2");
        NodeRef actualNode = new NodeRef("workspace://SpacesStore/67c8f11d-0936-4295-88a0-12b85764c76f");

        Reference virtualReference = ((VirtualProtocol) Protocols.VIRTUAL.protocol).newReference(templateNodeRef,
                                                                                                 "/",
                                                                                                 actualNode);
        Reference spReference = stringifyParse(virtualReference);

        assertEquals(virtualReference,
                     spReference);

    }

    @Test
    public void testStringifyNodeReference_actualRepositoryPath()
    {
        NodeRef templateNodeRef = new NodeRef("workspace://SpacesStore/0d3b26ff-c4c1-4680-8622-8608ea7ab4b2");
        NodeRef actualTemplateNode = new NodeRef("workspace://SpacesStore/67c8f11d-0936-4295-88a0-12b85764c76f");

        Reference virtualReference = ((VirtualProtocol) Protocols.VIRTUAL.protocol).newReference(templateNodeRef,
                                                                                                 "/1/2/3",
                                                                                                 actualTemplateNode);
        RepositoryResource actualNodeResource = new RepositoryResource(new RepositoryPath("/app:company_home/cm:aVirtualFolder"));
        Reference nodeReference = NodeProtocol.newReference(Encodings.PLAIN.encoding,
                                                            actualNodeResource,
                                                            virtualReference);
        Reference spReference = stringifyParse(nodeReference);

        assertEquals(nodeReference,
                     spReference);
    }

    @Test
    public void testStringifyNodeReference_actualNodeRef()
    {
        NodeRef templateNodeRef = new NodeRef("workspace://SpacesStore/0d3b26ff-c4c1-4680-8622-8608ea7ab4b2");
        NodeRef actualTemplateNode = new NodeRef("workspace://SpacesStore/67c8f11d-0936-4295-88a0-12b85764c76f");

        Reference virtualReference = ((VirtualProtocol) Protocols.VIRTUAL.protocol).newReference(templateNodeRef,
                                                                                                 "/1/2/3",
                                                                                                 actualTemplateNode);
        NodeRef actualNode = new NodeRef("workspace://SpacesStore/00c8f11d-0936-4295-88a0-12b85764c76f");
        Reference nodeReference = NodeProtocol.newReference(actualNode,
                                                            virtualReference);
        Reference spReference = stringifyParse(nodeReference);

        assertEquals(nodeReference,
                     spReference);

    }

    @Test
    public void testStringifyVanillaReference_classpath()
    {
        ClasspathResource virtualClasspathTemplate = new ClasspathResource("/com/alfresco/template.js");
        NodeRef actualNode = new NodeRef("workspace://SpacesStore/67c8f11d-0936-4295-88a0-12b85764c76f");
        RepositoryResource actualNodeResource = new RepositoryResource(new RepositoryNodeRef(actualNode));
        ClasspathResource vanillaClasspathTemplate = new ClasspathResource("/com/alfresco/vanilla-template.js");

        Reference vanillaReference = ((VanillaProtocol) Protocols.VANILLA.protocol)
                    .newReference(Encodings.PLAIN.encoding,
                                  virtualClasspathTemplate,
                                  "/1/2/3",
                                  actualNodeResource,
                                  vanillaClasspathTemplate,
                                  Collections.<Parameter> emptyList());

        Reference spReference = stringifyParse(vanillaReference);

        assertEquals(vanillaReference,
                     spReference);

    }

}
