/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.ref;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

public class ZeroStringifierTest
{

//    @Test
//    public void testStringifyNodeReference() throws Exception
//    {
//        {
//            NodeRef templateNodeRef = new NodeRef("workspace://SpacesStore/0d3b26ff-c4c1-4680-8622-8608ea7ab4b2");
//            NodeRef actualTemplateNode = new NodeRef("workspace://SpacesStore/b6843991-e06f-4ca6-9fe5-51105e2af99f");
//            NodeRef actualNode = new NodeRef("workspace://SpacesStore/67c8f11d-0936-4295-88a0-12b85764c76f");
//
//            Reference templateParentReference = ((VirtualProtocol) Protocols.VIRTUAL.protocol)
//                        .newReference(templateNodeRef,
//                                      "/1/2/3",
//                                      actualTemplateNode);
//
//            Reference templateNodeRefence = NodeProtocol.newReference(actualNode,
//                                                                      templateParentReference);
//
//            String encoding = new ZeroNodeStringifier().stringify(templateNodeRefence);
//
//            System.err.println(encoding);
//
//            Reference ref = new ZeroNodeReferenceParser().parse(encoding);
//            System.out.println(ref.encode(Encodings.PLAIN.encoding));
//        }
//        {
//            NodeRef templateNodeRef = new NodeRef("workspace://SpacesStore/0d3b26ff-c4c1-4680-8622-8608ea7ab4b2");
//            NodeRef actualTemplateNode = new NodeRef("workspace://SpacesStore/b6843991-e06f-4ca6-9fe5-51105e2af99f");
//            NodeRef actualNode = new NodeRef("workspace://SpacesStore/67c8f11d-0936-4295-88a0-12b85764c76f");
//
//            Reference templateParentReference = ((VirtualProtocol) Protocols.VIRTUAL.protocol)
//                        .newReference(new ClasspathResource("/com/alfresco/someTemplate.js"),
//                                      "/1/2/3",
//                                      actualTemplateNode,
//                                      Collections.<Parameter> emptyList());
//
//            Reference templateNodeRefence = NodeProtocol.newReference(actualNode,
//                                                                      templateParentReference);
//
//            ZeroConfiguredClasspathHasher.addHash("/com/alfresco/",
//                                                  "1");
//            String encoding = new ZeroNodeStringifier().stringify(templateNodeRefence);
//
//            System.err.println(encoding);
//            
//            Reference ref = new ZeroNodeReferenceParser().parse(encoding);
//            System.out.println(ref.encode(Encodings.PLAIN.encoding));
//
//        }
//    }

    @Test
    public void testStringifyReference() throws Exception
    {
        {
            Reference r = new Reference(Encodings.ZERO.encoding,
                                        Protocols.VIRTUAL.protocol,
                                        new ClasspathResource("/org/alfresco/"));
            assertEquals("5:/org/alfresco/",
                         r.encode(Encodings.ZERO.encoding));
        }

        {

            RepositoryResource rr1 = new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/0d3b26ff-c4c1-4680-8622-8608ea7ab4b2")));
            RepositoryResource rr2 = new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/b6843991-e06f-4ca6-9fe5-51105e2af99f")));
            List<Parameter> params = Arrays.<Parameter> asList(new StringParameter("/"),
                                                               new ResourceParameter(rr1),
                                                               new ResourceParameter(rr2));

            Reference r = new Reference(Encodings.ZERO.encoding,
                                        Protocols.VANILLA.protocol,
                                        new ClasspathResource("/org/alfresco/repo/virtual/node/vanilla.js"),
                                        params);
            assertEquals("2:/org/alfresco/repo/virtual/node/vanilla.js:s:/:1:0d3b26ff-c4c1-4680-8622-8608ea7ab4b2:1:b6843991-e06f-4ca6-9fe5-51105e2af99f",
                         r.encode(Encodings.ZERO.encoding));
        }
    }

    @Test
    public void testReferenceDelimiter()
    {
        List<Parameter> params1 = Arrays
                    .<Parameter> asList(new StringParameter("/6"),
                                        new ResourceParameter(new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/67c8f11d-0936-4295-88a0-12b85764c76f")))),
                                        new ResourceParameter(new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/6428d7cc-feaa-4e32-a983-0b357439a994")))));
        Reference innerRef1 = new Reference(Encodings.ZERO.encoding,
                                            Protocols.VANILLA.protocol,
                                            new ClasspathResource("/org/alfresco/repo/virtual/node/vanilla.js"),
                                            params1);
        Reference innerRef2 = new Reference(Encodings.ZERO.encoding,
                                            Protocols.VANILLA.protocol,
                                            new ClasspathResource("/org/alfresco/repo/virtual/node/vanilla.js"),
                                            params1);

        List<Parameter> prms1 = Arrays.<Parameter> asList(new ReferenceParameter(innerRef1),
                                                          new ReferenceParameter(innerRef2));
        Reference createdRef1 = new Reference(Encodings.ZERO.encoding,
                                              Protocols.NODE.protocol,
                                              new RepositoryResource(new RepositoryPath("/Company Home/Data Dictionary/")),
                                              prms1);
        assertEquals("6:/Company Home/Data Dictionary/:r:2:/org/alfresco/repo/virtual/node/vanilla.js:s:/6:1:67c8f11d-0936-4295-88a0-12b85764c76f:1:6428d7cc-feaa-4e32-a983-0b357439a994:*:r:2:/org/alfresco/repo/virtual/node/vanilla.js:s:/6:1:67c8f11d-0936-4295-88a0-12b85764c76f:1:6428d7cc-feaa-4e32-a983-0b357439a994:*",
                     createdRef1.encode());

        List<Parameter> prms2 = Arrays
                    .<Parameter> asList(new StringParameter("ado+8"),
                                        new ReferenceParameter(innerRef1),
                                        new ReferenceParameter(innerRef2),
                                        new ResourceParameter(new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/67c8f11d-0936-4295-88a0-12b85764c76f")))));
        Reference createdRef2 = new Reference(Encodings.ZERO.encoding,
                                              Protocols.NODE.protocol,
                                              new RepositoryResource(new RepositoryPath("/Company Home/Data Dictionary/")),
                                              prms2);
        assertEquals("6:/Company Home/Data Dictionary/:s:ado+8:r:2:/org/alfresco/repo/virtual/node/vanilla.js:s:/6:1:67c8f11d-0936-4295-88a0-12b85764c76f:1:6428d7cc-feaa-4e32-a983-0b357439a994:*:r:2:/org/alfresco/repo/virtual/node/vanilla.js:s:/6:1:67c8f11d-0936-4295-88a0-12b85764c76f:1:6428d7cc-feaa-4e32-a983-0b357439a994:*:1:67c8f11d-0936-4295-88a0-12b85764c76f",
                     createdRef2.encode());

    }

    @Test
    public void testStringifyRepositoryNodeRef() throws Exception
    {

        RepositoryResource rr1 = new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/0029-222-333-444")));
        Reference r = new Reference(Encodings.ZERO.encoding,
                                    Protocols.VIRTUAL.protocol,
                                    rr1);
        assertEquals("4:0029-222-333-444",
                     r.encode(Encodings.ZERO.encoding));
    }

    public void testStringifyRepositoryPath() throws Exception
    {
        RepositoryResource rr2 = new RepositoryResource(new RepositoryPath("/Data Dictionary/Virtual Folders/claim.json"));
        Reference r = new Reference(Encodings.ZERO.encoding,
                                    Protocols.VIRTUAL.protocol,
                                    rr2);
        assertEquals("3:/Data Dictionary/Virtual Folders/claim.json",
                     r.encode(Encodings.ZERO.encoding));
    }
}
