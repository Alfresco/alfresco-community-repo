package org.alfresco.repo.admin;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @see Log4JHierarchyInit
 * 
 * @author Derek Hulley
 * @since 2.2.3
 */
public class Log4JHierarchyInitTest extends TestCase
{
    private static ApplicationContext ctx = new ClassPathXmlApplicationContext(
            new String[] {"classpath:log4j/log4j-test-context.xml"}
            );

    public void setUp() throws Exception
    {
    }
    
    public void testSetUp() throws Throwable
    {
        // Check that the bean is present
        ctx.getBean("log4JHierarchyInit");
        // Make sure that the default log4j.properties is being picked up
        Log log = LogFactory.getLog("log4j.logger.org.alfresco");
        assertFalse("Expect log level ERROR for 'org.alfresco'.", log.isWarnEnabled());
    }
    
    public void testAddingLog4jProperties() throws Throwable
    {
        Log log = LogFactory.getLog(this.getClass());
        // We expect DEBUG to be on
        assertTrue("DEBUG was not enabled for logger " + this.getClass(), log.isDebugEnabled());
    }
}
