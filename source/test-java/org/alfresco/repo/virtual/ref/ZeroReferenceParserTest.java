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

import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class ZeroReferenceParserTest extends AbstractReferenceParserTest
{

    private static Log logger = LogFactory.getLog(ZeroReferenceParserTest.class);

    private double compressionRatio(Reference ref, Encoding uncompressedEnc, Encoding compressedEnc) throws Exception
    {
        double ratio = (double) ref.encode(compressedEnc).length() / (double) ref.encode(uncompressedEnc).length()
                    * 100;
        return ratio;
    }

    private void compress(String referenceString, Encoding uncompressedEnc, Encoding compressedEnc, String trueMessage,
                String debugMessage) throws Exception
    {
        Reference ref = new ZeroReferenceParser().parse(referenceString);
        double testRatio = this.compressionRatio(ref,
                                                 uncompressedEnc,
                                                 compressedEnc);
        if (logger.isDebugEnabled())
        {
            logger.debug(testRatio + debugMessage);
        }
        assertTrue(trueMessage,
                   testRatio < 100);
    }

    @Test
    public void testCompressionRatio() throws Exception
    {
        String trueMessage = "Compression ratio should be lower than 100.";
        String debugMessage = " % of initial size";
        Encoding uncompressedEnc = Encodings.PLAIN.encoding, compressedEnc = Encodings.ZERO.encoding;
        this
                    .compress("2:/org/alfresco/repo/virtual/node/vanilla.js:s:/:1:0d3b26ff-c4c1-4680-8622-8608ea7ab4b2:1:b6843991-e06f-4ca6-9fe5-51105e2af99f",
                              uncompressedEnc,
                              compressedEnc,
                              trueMessage,
                              debugMessage);
        this
                    .compress("7:1a0b110f-1e09-4ca2-b367-fe25e4964a4e:r:2:/org/alfresco/repo/virtual/node/vanilla.js:s:/7/8:1:872d6250-913d-460e-9c88-e695f247d81c:1:b6843991-e06f-4ca6-9fe5-51105e2af99f",
                              uncompressedEnc,
                              compressedEnc,
                              trueMessage,
                              debugMessage);

        this
                    .compress("2:/org/alfresco/repo/virtual/node/vanilla.js:s:/6:1:872d6250-913d-460e-9c88-e695f247d81c:1:6869da5d-35a8-493a-91b5-a79c6f422122",
                              uncompressedEnc,
                              compressedEnc,
                              trueMessage,
                              debugMessage);

        this
                    .compress("7:7d71e00b-1838-4a3f-aff5-be24def2663c:r:2:/org/alfresco/repo/virtual/node/vanilla.js:s:/6:1:67c8f11d-0936-4295-88a0-12b85764c76f:1:6428d7cc-feaa-4e32-a983-0b357439a994",
                              uncompressedEnc,
                              compressedEnc,
                              trueMessage,
                              debugMessage);

        this
                    .compress("6:/Company Home/Data Dictionary/:r:2:/org/alfresco/repo/virtual/node/vanilla.js:s:/6:1:67c8f11d-0936-4295-88a0-12b85764c76f:1:6428d7cc-feaa-4e32-a983-0b357439a994",
                              uncompressedEnc,
                              compressedEnc,
                              trueMessage,
                              debugMessage);

    }

    @Test
    public void testReferenceDelimiter()
    {
        Reference parsedRef = new ZeroReferenceParser()
                    .parse("6:/Company Home/Data Dictionary/:r:2:/org/alfresco/repo/virtual/node/vanilla.js:s:/6:1:67c8f11d-0936-4295-88a0-12b85764c76f:1:6428d7cc-feaa-4e32-a983-0b357439a994:*");
        List<Parameter> params = Arrays
                    .<Parameter> asList(new StringParameter("/6"),
                                        new ResourceParameter(new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/67c8f11d-0936-4295-88a0-12b85764c76f")))),
                                        new ResourceParameter(new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/6428d7cc-feaa-4e32-a983-0b357439a994")))));
        Reference innerRef = new Reference(Encodings.ZERO.encoding,
                                           Protocols.VANILLA.protocol,
                                           new ClasspathResource("/org/alfresco/repo/virtual/node/vanilla.js"),
                                           params);

        List<Parameter> prms = Arrays.<Parameter> asList(new ReferenceParameter(innerRef));
        Reference createdRef = new Reference(Encodings.ZERO.encoding,
                                             Protocols.NODE.protocol,
                                             new RepositoryResource(new RepositoryPath("/Company Home/Data Dictionary/")),
                                             prms);
        assertEquals(parsedRef,
                     createdRef);

        Reference parsedRef1 = new ZeroReferenceParser()
                    .parse("6:/Company Home/Data Dictionary/:r:2:/org/alfresco/repo/virtual/node/vanilla.js:s:/6:1:67c8f11d-0936-4295-88a0-12b85764c76f:1:6428d7cc-feaa-4e32-a983-0b357439a994:*:r:2:/org/alfresco/repo/virtual/node/vanilla.js:s:/6:1:67c8f11d-0936-4295-88a0-12b85764c76f:1:6428d7cc-feaa-4e32-a983-0b357439a994:*");
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
        assertEquals(parsedRef1,
                     createdRef1);

        Reference parsedRef2 = new ZeroReferenceParser()
                    .parse("6:/Company Home/Data Dictionary/:r:2:/org/alfresco/repo/virtual/node/vanilla.js:s:/6:1:67c8f11d-0936-4295-88a0-12b85764c76f:1:6428d7cc-feaa-4e32-a983-0b357439a994:*:s:test:s:exit:r:2:/org/alfresco/repo/virtual/node/vanilla.js:s:/6:1:67c8f11d-0936-4295-88a0-12b85764c76f:1:6428d7cc-feaa-4e32-a983-0b357439a994:*:s:roro");
        List<Parameter> prms2 = Arrays.<Parameter> asList(new ReferenceParameter(innerRef1),
                                                          new StringParameter("test"),
                                                          new StringParameter("exit"),
                                                          new ReferenceParameter(innerRef2),
                                                          new StringParameter("roro"));
        Reference createdRef2 = new Reference(Encodings.ZERO.encoding,
                                              Protocols.NODE.protocol,
                                              new RepositoryResource(new RepositoryPath("/Company Home/Data Dictionary/")),
                                              prms2);
        assertEquals(parsedRef2,
                     createdRef2);

    }

    @Test
    public void testParseRepositoryNodeRef() throws Exception
    {
        Reference reference = new ZeroReferenceParser().parse("4:0029-222-333-444");
        assertEquals(reference,
                     new Reference(Encodings.ZERO.encoding,
                                   Protocols.VIRTUAL.protocol,
                                   new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/0029-222-333-444")))));

    }

    @Test
    public void testParseRepositoryPath() throws Exception
    {
        Reference reference = new ZeroReferenceParser().parse("3:/Foo/Bar");
        assertEquals(reference,
                     new Reference(Encodings.ZERO.encoding,
                                   Protocols.VIRTUAL.protocol,
                                   new RepositoryResource(new RepositoryPath("/Foo/Bar"))));

    }

    @Test
    public void testParseNodeProtocol() throws Exception
    {
        // testing parse for node protocol with an repository node as Resource
        // parameter
        Reference reference = new ZeroReferenceParser().parse("7:0029-222-333-444:1:0029-122-333-0023");

        RepositoryResource rr = new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/0029-122-333-0023")));
        List<Parameter> params = Arrays.<Parameter> asList(new ResourceParameter(rr));
        assertEquals(reference,
                     new Reference(Encodings.ZERO.encoding,
                                   Protocols.NODE.protocol,
                                   new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/0029-222-333-444"))),
                                   params));

        // testing parse for node protocol with an repository path as Resource
        // parameter
        reference = new ZeroReferenceParser().parse("7:0029-222-333-444:0:/Foo/Bar");

        rr = new RepositoryResource(new RepositoryPath("/Foo/Bar"));
        params = Arrays.<Parameter> asList(new ResourceParameter(rr));
        assertEquals(reference,
                     new Reference(Encodings.ZERO.encoding,
                                   Protocols.NODE.protocol,
                                   new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/0029-222-333-444"))),
                                   params));

        // testing parse for node protocol with an repository path and a string
        // as resource and respectively String parameters
        reference = new ZeroReferenceParser().parse("7:0029-222-333-444:0:/Foo/Bar:s:vm_virtual");

        rr = new RepositoryResource(new RepositoryPath("/Foo/Bar"));
        params = Arrays.<Parameter> asList(new ResourceParameter(rr),
                                           new StringParameter("vm_virtual"));
        assertEquals(reference,
                     new Reference(Encodings.ZERO.encoding,
                                   Protocols.NODE.protocol,
                                   new RepositoryResource(new RepositoryNodeRef(new NodeRef("workspace://SpacesStore/0029-222-333-444"))),
                                   params));

    }

    @Test
    public void testRecursiveReferenceParser() throws Exception
    {
        assertRecursiveReferenceParser(Encodings.ZERO.encoding);
    }
}
