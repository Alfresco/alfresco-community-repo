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

import junit.framework.TestCase;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

public class ReferenceTest extends TestCase
{
    private static final String TEST_CLASSPATH = "/org/alfresco/";

    private static final String TEST_PLAIN_REFERENCE = "virtual:classpath:" + TEST_CLASSPATH;

    private static final String TEST_NODE_REF = "workspace://SpacesStore/vpdmlydHVhbDpjbGFzc3BhdGg6L29yZy9hbGZyZXNjby8=";

    private static final String TEST_NODE_REF_1 = "workspace://SpacesStore/vpdmFuaWxsYTpjbGFzc3BhdGg6L29yZy9hbGZyZXNjby9yZXBvL3ZpcnR1YWwvbm9kZS92YW5pbGxhLmpzOnM6LzAxIENvcnJlc3BvbmRlbmNlOnI6cmVwb3NpdG9yeTpub2RlOndvcmtzcGFjZTpTcGFjZXNTdG9yZTo2NWM2NjliMS00N2IzLTQzMmUtYjk0Zi1lZmU4YTcyYWEyMjA6cjpjbGFzc3BhdGg6L29yZy9hbGZyZXNjby9yZXBvL3ZpcnR1YWwvbm9kZS9jbGFpbXNEZW1vLmpzb24";

    private static final Reference TEST_REFERENCE = new Reference(Encodings.PLAIN.encoding,
                                                                  Protocols.VIRTUAL.protocol,
                                                                  new ClasspathResource(TEST_CLASSPATH));

    @Test
    public void testToNodeRef() throws Exception
    {
        Reference r = new PlainReferenceParser().parse(TEST_PLAIN_REFERENCE);
        NodeRef nodeRef = r.toNodeRef();
        assertEquals(new NodeRef(TEST_NODE_REF),
                     nodeRef);
    }

    @Test
    public void testFromNodeRef() throws Exception
    {
        Reference r = Reference.fromNodeRef(new NodeRef(TEST_NODE_REF));
        Reference expectedReference = TEST_REFERENCE;

        assertEquals(expectedReference,
                     r);

        String expected = "vanilla:classpath:/org/alfresco/repo/virtual/node/vanilla.js:s:/01 Correspondence:r:repository:node:workspace:SpacesStore:65c669b1-47b3-432e-b94f-efe8a72aa220:r:classpath:/org/alfresco/repo/virtual/node/claimsDemo.json";
        Reference fromNodeRef = Reference.fromNodeRef(new NodeRef(TEST_NODE_REF_1));
        assertNotNull(fromNodeRef);
        assertEquals(expected,
                     fromNodeRef.toString());
    }

    @Test
    public void testExecute() throws Exception
    {
        boolean sccess = TEST_REFERENCE.execute(new ProtocolMethod<Boolean>()
        {

            @Override
            public Boolean execute(VanillaProtocol vanillaProtocol, Reference reference) throws ProtocolMethodException
            {
                fail("Invalid dispatch");
                return false;
            }

            @Override
            public Boolean execute(VirtualProtocol virtualProtocol, Reference reference) throws ProtocolMethodException
            {
                return reference == TEST_REFERENCE;
            }

            @Override
            public Boolean execute(NodeProtocol protocol, Reference reference) throws ProtocolMethodException
            {
                fail("Invalid dispatch");
                return false;
            }

            @Override
            public Boolean execute(Protocol protocol, Reference reference) throws ProtocolMethodException
            {
                fail("Invalid dispatch");
                return false;
            }
        });

        assertTrue("Invalid method execution result!",
                   sccess);
    }

    @Test
    public void testEncode() throws Exception
    {
        Reference r = TEST_REFERENCE;
        assertEquals(TEST_PLAIN_REFERENCE,
                     r.encode());
    }

    @Test
    public void testEquals() throws Exception
    {
        assertTrue(TEST_REFERENCE.equals(TEST_REFERENCE));
        assertFalse(TEST_REFERENCE.equals(null));
        Reference testResourceReplica = new Reference(Encodings.PLAIN.encoding,
                                                      Protocols.VIRTUAL.protocol,
                                                      new ClasspathResource(TEST_CLASSPATH));

        assertTrue(TEST_REFERENCE.equals(testResourceReplica));
        Reference nullResRef = new Reference(Encodings.PLAIN.encoding,
                                             Protocols.VIRTUAL.protocol,
                                             null);
        Reference nullResRefReplica = new Reference(Encodings.PLAIN.encoding,
                                                    Protocols.VIRTUAL.protocol,
                                                    null);
        assertFalse(TEST_REFERENCE.equals(nullResRef));
        assertFalse(nullResRef.equals(TEST_REFERENCE));
        assertTrue(nullResRef.equals(nullResRefReplica));

        List<Parameter> params1 = Arrays.<Parameter> asList(new StringParameter("P1"));
        List<Parameter> params2 = Arrays.<Parameter> asList(new StringParameter("P1"),
                                                            new StringParameter("P2"));
        Reference testParams1Ref = new Reference(Encodings.PLAIN.encoding,
                                                 Protocols.VIRTUAL.protocol,
                                                 new ClasspathResource(TEST_CLASSPATH),
                                                 params1);
        Reference testParams1RefReplica = new Reference(Encodings.PLAIN.encoding,
                                                        Protocols.VIRTUAL.protocol,
                                                        new ClasspathResource(TEST_CLASSPATH),
                                                        params1);
        Reference testParams2Ref = new Reference(Encodings.PLAIN.encoding,
                                                 Protocols.VIRTUAL.protocol,
                                                 new ClasspathResource(TEST_CLASSPATH),
                                                 params2);
        assertTrue(testParams1Ref.equals(testParams1RefReplica));
        assertFalse(testParams2Ref.equals(testParams1Ref));
    }

}
