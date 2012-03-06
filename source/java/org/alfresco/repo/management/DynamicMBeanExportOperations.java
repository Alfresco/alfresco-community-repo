/*
 * Copyright 2005-2010 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
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