package org.alfresco.repo.management;

import junit.framework.TestCase;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

public class JmxDumpUtilTest extends TestCase
{

    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private AuthenticationComponent authenticationComponent;

    public void testUpdateOSNameAttribute() throws Exception
    {
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().startsWith("linux"))
        {
            String attr = JmxDumpUtil.updateOSNameAttributeForLinux(osName);
            assertTrue(attr.toLowerCase().startsWith("linux ("));
        }
    }
}
