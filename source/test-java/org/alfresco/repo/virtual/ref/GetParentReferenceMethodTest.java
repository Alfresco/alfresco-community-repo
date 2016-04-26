
package org.alfresco.repo.virtual.ref;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

public class GetParentReferenceMethodTest extends TestCase
{
    private GetParentReferenceMethod method;

    @Override
    protected void setUp() throws Exception
    {
        method = new GetParentReferenceMethod();
    }

    @Test
    public void testSpacePath() throws Exception
    {
        final String path = "/Media types/Images";
        assertEquals("/Media types",
                     toParentPath(path));
    }

    @Test
    public void testTrailingPath() throws Exception
    {
        final String path = "/Media types/Images/";
        assertEquals("/Media types",
                     toParentPath(path));
    }

    @Test
    public void testFirsLevelPath() throws Exception
    {
        final String path = "/Media types";
        assertEquals("/",
                     toParentPath(path));
    }

    @Test
    public void testTrailingRoot() throws Exception
    {
        final String path = "/ ";
        assertNull(toParentPath(path));
    }

    @Test
    public void testRootPath() throws Exception
    {
        final String path = "/";
        assertNull(toParentPath(path));
    }

    private String toParentPath(final String path) throws ProtocolMethodException
    {
        List<Parameter> params = Arrays.<Parameter> asList(new StringParameter(path));
        Reference ref = new Reference(Encodings.PLAIN.encoding,
                                      Protocols.VIRTUAL.protocol,
                                      new ClasspathResource("/some/class/path.js"),
                                      params);
        Reference parent = ref.execute(method);
        if (parent == null)
        {
            return null;
        }
        else
        {
            StringParameter parentPath = (StringParameter) parent.getParameters().get(0);
            return parentPath.getValue();
        }
    }

}
