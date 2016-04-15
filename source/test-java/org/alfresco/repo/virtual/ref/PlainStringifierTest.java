
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
