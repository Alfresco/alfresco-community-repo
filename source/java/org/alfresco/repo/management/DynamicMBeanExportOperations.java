package org.alfresco.repo.management;

import javax.management.ObjectName;

/**
 * An interface that allows individual MBeans to be registered and unregistered over time.
 * 
 * @author dward
 */
public interface DynamicMBeanExportOperations
{
    /**
     * Unregisters an MBean
     * 
     * @param objectName
     *            the object name
     */
    public void unregisterMBean(ObjectName objectName);

    /**
     * Registers an MBean.
     * 
     * @param managedResource
     *            the managed resource
     * @param objectName
     *            the object name
     * @return the actual object name
     */
    public ObjectName registerMBean(Object managedResource, ObjectName objectName);

}