
package org.alfresco.repo.virtual.ref;

import java.util.Arrays;

import junit.framework.TestCase;

import org.junit.Test;

public class GetChildByIdMethodTest extends TestCase
{

    private String toChildPath(final String parentPath, String childName) throws ProtocolMethodException
    {
        StringParameter path = new StringParameter(parentPath);

        Reference ref = new Reference(Encodings.PLAIN.encoding,
                                      Protocols.VIRTUAL.protocol,
                                      new ClasspathResource("/a/class/path.js"),
                                      Arrays.asList(path));
        GetChildByIdMethod method = new GetChildByIdMethod(childName);

        Reference childRef = ref.execute(method);

        assertEquals(ref.getResource(),
                     childRef.getResource());
        assertEquals(ref.getProtocol(),
                     childRef.getProtocol());

        return childRef.execute(new GetTemplatePathMethod());
    }

    @Test
    public void testExecute() throws Exception
    {
        final String parentPath = "/root";
        final String childName = "aChid";
        String childPath = toChildPath(parentPath,
                                       childName);

        assertEquals(parentPath + "/" + childName,
                     childPath);
    }

    @Test
    public void testTrailingPath() throws Exception
    {
        String childPath = toChildPath("  /root/   ",
                                       "child");

        assertEquals("/root/child",
                     childPath);
    }
}
