
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
