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

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class PlainReferenceParserTest extends AbstractReferenceParserTest
{

    private static Log logger = LogFactory.getLog(PlainReferenceParserTest.class);

    @Test
    public void testRecursiveReferenceParser() throws Exception
    {
        assertRecursiveReferenceParser(Encodings.PLAIN.encoding);
    }

    @Test
    public void testParseClasspathResource() throws Exception
    {
        {
            Reference pr = new PlainReferenceParser().parse("virtual:classpath:/org/alfresco/");
            Reference r = new Reference(Encodings.PLAIN.encoding,
                                        Protocols.VIRTUAL.protocol,
                                        new ClasspathResource("/org/alfresco/"));
            assertEquals(r,
                         pr);
        }

        {
            Reference prp1 = new PlainReferenceParser()
            .parse("virtual:classpath:/org/alfresco/:r:repository:node:workspace:SpacesStore:0029-222-333-444");
            RepositoryResource rr1 = new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/0029-222-333-444")));
            List<Parameter> params1 = Arrays.<Parameter> asList(new ResourceParameter(rr1));
            Reference rp1 = new Reference(Encodings.PLAIN.encoding,
                                          Protocols.VIRTUAL.protocol,
                                          new ClasspathResource("/org/alfresco/"),
                                          params1);
            assertEquals(rp1,
                         prp1);
        }
        {
            Reference prp2 = new PlainReferenceParser()
            .parse("virtual:classpath:/org/alfresco/:r:repository:node:workspace:SpacesStore:0029-222-333-444:r:repository:path:/Data Dictionary/Virtual Folders/claim.json");
            RepositoryResource rr21 = new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/0029-222-333-444")));
            RepositoryResource rr22 = new RepositoryResource(new RepositoryPath("/Data Dictionary/Virtual Folders/claim.json"));
            List<Parameter> params2 = Arrays.<Parameter> asList(new ResourceParameter(rr21),
                                                                new ResourceParameter(rr22));
            Reference rp2 = new Reference(Encodings.PLAIN.encoding,
                                          Protocols.VIRTUAL.protocol,
                                          new ClasspathResource("/org/alfresco/"),
                                          params2);
            assertEquals(rp2,
                         prp2);
        }
    }

    @Test
    public void testParseRepositoryNodeRef() throws Exception
    {
        Reference reference = new PlainReferenceParser()
        .parse("virtual:repository:node:workspace:SpacesStore:0029-222-333-444");

        assertEquals(reference,
                     new Reference(Encodings.PLAIN.encoding,
                                   Protocols.VIRTUAL.protocol,
                                   new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/0029-222-333-444")))));

    }

    @Test
    public void testParseRepositoryPath() throws Exception
    {
        Reference reference = new PlainReferenceParser().parse("virtual:repository:path:/Foo/Bar");

        assertEquals(reference,
                     new Reference(Encodings.PLAIN.encoding,
                                   Protocols.VIRTUAL.protocol,
                                   new RepositoryResource(new RepositoryPath("/Foo/Bar"))));

    }

    @Test
    public void testParseNodeProtocol() throws Exception
    {
        // testing parse for node protocol with an repository node as reference
        // parameter
        Reference reference = new PlainReferenceParser()
        .parse("node:repository:node:workspace:SpacesStore:0029-222-333-444:r:repository:node:workspace:SpacesStore:0029-122-333-0023:ref:node:repository:node:workspace:SpacesStore:0029-222-333-444:r:repository:path:/Foo/Bar:s:smf_smartFolder:*");

        RepositoryResource rr = new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/0029-122-333-0023")));
        List<Parameter> params = Arrays.<Parameter> asList(new ResourceParameter(rr),
                                                           new ReferenceParameter(reference));
        /*
         * assertEquals(reference, new Reference(Encodings.PLAIN.encoding,
         * Protocols.NODE.protocol, new RepositoryResource(new
         * RepositoryNodeRef(new
         * NodeRef("workspace://SpacesStore/0029-222-333-444"))), params));
         */

        // testing parse for node protocol with an repository path as reference
        // parameter
        reference = new PlainReferenceParser()
        .parse("node:repository:node:workspace:SpacesStore:0029-222-333-444:r:repository:path:/Foo/Bar");

        rr = new RepositoryResource(new RepositoryPath("/Foo/Bar"));
        params = Arrays.<Parameter> asList(new ResourceParameter(rr));
        assertEquals(reference,
                     new Reference(Encodings.PLAIN.encoding,
                                   Protocols.NODE.protocol,
                                   new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/0029-222-333-444"))),
                                   params));

        // testing parse for node protocol with an repository path and a string
        // as reference parameters
        reference = new PlainReferenceParser()
        .parse("node:repository:node:workspace:SpacesStore:0029-222-333-444:r:repository:path:/Foo/Bar:s:smf_smartFolder");

        rr = new RepositoryResource(new RepositoryPath("/Foo/Bar"));
        params = Arrays.<Parameter> asList(new ResourceParameter(rr),
                                           new StringParameter("smf_smartFolder"));
        assertEquals(reference,
                     new Reference(Encodings.PLAIN.encoding,
                                   Protocols.NODE.protocol,
                                   new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/0029-222-333-444"))),
                                   params));
    }

    @Test
    public void testReferenceDelimiter()
    {
        Reference parsedRef = new PlainReferenceParser()
        .parse("vanilla:repository:path:/Company Home/Data Dictionary/:ref:vanilla:classpath:/org/alfresco/repo/virtual/node/vanilla.js:s:/Images/ JPG images/:r:repository:node:workspace:SpacesStore:67c8f11d-0936-4295-88a0-12b85764c76f:r:repository:node:workspace:SpacesStore:6428d7cc-feaa-4e32-a983-0b357439a994:*");
        List<Parameter> params = Arrays
                    .<Parameter> asList(new StringParameter("/Images/ JPG images/"),
                                        new ResourceParameter(new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/67c8f11d-0936-4295-88a0-12b85764c76f")))),
                                        new ResourceParameter(new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/6428d7cc-feaa-4e32-a983-0b357439a994")))));
        Reference innerRef = new Reference(Encodings.PLAIN.encoding,
                                           Protocols.VANILLA.protocol,
                                           new ClasspathResource("/org/alfresco/repo/virtual/node/vanilla.js"),
                                           params);

        List<Parameter> prms = Arrays.<Parameter> asList(new ReferenceParameter(innerRef));
        Reference createdRef = new Reference(Encodings.PLAIN.encoding,
                                             Protocols.VANILLA.protocol,
                                             new RepositoryResource(new RepositoryPath("/Company Home/Data Dictionary/")),
                                             prms);
        assertEquals(parsedRef,
                     createdRef);



        Reference parsedRef1 = new PlainReferenceParser()
        .parse("node:repository:path:/Company Home/Data Dictionary/:ref:vanilla:classpath:/org/alfresco/repo/virtual/node/vanilla.js:s:/My Documents:r:repository:node:workspace:SpacesStore:67c8f11d-0936-4295-88a0-12b85764c76f:r:repository:node:workspace:SpacesStore:6428d7cc-feaa-4e32-a983-0b357439a994:*:ref:vanilla:classpath:/org/alfresco/repo/virtual/node/vanilla.js:s:/My Documents:r:repository:node:workspace:SpacesStore:67c8f11d-0936-4295-88a0-12b85764c76f:r:repository:node:workspace:SpacesStore:6428d7cc-feaa-4e32-a983-0b357439a994:*");
        List<Parameter> params1 = Arrays
                    .<Parameter> asList(new StringParameter("/My Documents"),
                                        new ResourceParameter(new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/67c8f11d-0936-4295-88a0-12b85764c76f")))),
                                        new ResourceParameter(new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/6428d7cc-feaa-4e32-a983-0b357439a994")))));
        Reference innerRef1 = new Reference(Encodings.PLAIN.encoding,
                                            Protocols.VANILLA.protocol,
                                            new ClasspathResource("/org/alfresco/repo/virtual/node/vanilla.js"),
                                            params1);
        Reference innerRef2 = new Reference(Encodings.PLAIN.encoding,
                                            Protocols.VANILLA.protocol,
                                            new ClasspathResource("/org/alfresco/repo/virtual/node/vanilla.js"),
                                            params1);

        List<Parameter> prms1 = Arrays.<Parameter> asList(new ReferenceParameter(innerRef1),
                                                          new ReferenceParameter(innerRef2));
        Reference createdRef1 = new Reference(Encodings.PLAIN.encoding,
                                              Protocols.NODE.protocol,
                                              new RepositoryResource(new RepositoryPath("/Company Home/Data Dictionary/")),
                                              prms1);
        assertEquals(parsedRef1,
                     createdRef1);


        Reference parsedRef2 = new PlainReferenceParser()
        .parse("virtual:classpath:/Company Home/:ref:vanilla:classpath:/org/alfresco/repo/virtual/node/vanilla.js:s:/My Documents/+=?Folder:r:repository:path:/this/ repo/ path/:r:classpath:/org/alfresco/repo/virtual/template.js:*:ref:vanilla:classpath:virtual.js:*");
        List<Parameter> params2 = Arrays
                    .<Parameter> asList(new StringParameter("/My Documents/+=?Folder"),
                                        new ResourceParameter(new RepositoryResource(new RepositoryPath("/this/ repo/ path/"))),
                                        new ResourceParameter(new ClasspathResource("/org/alfresco/repo/virtual/template.js")));
        Reference innerRef3 = new Reference(Encodings.PLAIN.encoding,
                                            Protocols.VANILLA.protocol,
                                            new ClasspathResource("/org/alfresco/repo/virtual/node/vanilla.js"),
                                            params2);
        Reference innerRef4 = new Reference(Encodings.PLAIN.encoding,
                                            Protocols.VANILLA.protocol,
                                            new ClasspathResource("virtual.js"));

        List<Parameter> prms2 = Arrays.<Parameter> asList(new ReferenceParameter(innerRef3),
                                                          new ReferenceParameter(innerRef4));
        Reference createdRef2 = new Reference(Encodings.PLAIN.encoding,
                                              Protocols.VIRTUAL.protocol,
                                              new ClasspathResource("/Company Home/"),
                                              prms2);
        assertEquals(parsedRef2,
                     createdRef2);
    }

    protected void assertReferenceParseException(String reference)
    {
        try
        {
            new PlainReferenceParser().parse(reference);
            fail("\"" + reference + "\" should not be parsed!");
        }
        catch (ReferenceParseException e)
        {
            logger.info(e);
        }

    }

    @Test
    public void testParseError1() throws Exception
    {
        assertReferenceParseException("x:folder:classpath:/org/alfresco/");
        assertReferenceParseException("plain:folder:classpath:/org/alfresco/");
        assertReferenceParseException("folder:x:/org/alfresco/");
        assertReferenceParseException("folder:classpath:");
        assertReferenceParseException("folder:classpath:/org/alfresco/:x:u");

    }
}
