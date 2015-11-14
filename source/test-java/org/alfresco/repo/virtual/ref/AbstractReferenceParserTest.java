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
