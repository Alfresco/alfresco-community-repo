package org.alfresco.repo.management.subsystems;

/**
 * The property backed bean has a monitor object which is exposed via JMX.   The bean is introspected and 
 * read-only properties and methods are exposed via JMX.
 * 
 * @author mrogers
 * @since 4.2
 */
public interface PropertyBackedBeanWithMonitor 
{
    /**
     * Get the monitor object.  
     * 
     * @return the monitor object or null if there is no monitor 
     */
    public Object getMonitorObject();

}
