package org.alfresco.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Helper class to provide static and common access to the Spring
 * {@link org.springframework.context.ApplicationContext application context}.
 * 
 * @author Derek Hulley
 */
public class ApplicationContextHelper extends BaseApplicationContextHelper
{   
    /** location of required configuration files */
    public static final String[] CONFIG_LOCATIONS = new String[] { "classpath:alfresco/application-context.xml" };
    
    /**
     * Provides a static, single instance of the default Alfresco application context.  This method can be
     * called repeatedly.
     * <p/>
     * If the configuration requested differs from one used previously, then the previously-created
     * context is shut down.
     * 
     * @return Returns an application context for the default Alfresco configuration
     */
    public synchronized static ApplicationContext getApplicationContext()
    {
        return BaseApplicationContextHelper.getApplicationContext(CONFIG_LOCATIONS);
    }
    
    /**
     * Provides a static, single instance of an application context represented by the given
     * array of config locations. This method can be called repeatedly.
     * <p/>
     * If the configuration requested differs from one used previously, then the previously-created
     * context is shut down.
     * 
     * @return Returns an application context for the given config locations
     */
    public synchronized static ApplicationContext getApplicationContext(String[] configLocations)
    {
        return BaseApplicationContextHelper.getApplicationContext(configLocations);
    }
    
    public static void main(String ... args)
    {
        ClassPathXmlApplicationContext ctx = (ClassPathXmlApplicationContext) getApplicationContext();
        synchronized (ctx)
        {
            try { ctx.wait(10000L); } catch (Throwable e) {}
        }
        ctx.close();
    }
}
