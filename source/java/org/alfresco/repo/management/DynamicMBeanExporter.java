/*
 * Copyright 2005-2010 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.management;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.support.MBeanRegistrationSupport;

/**
 * An {@link MBeanExporter} that allows individual MBeans to be registered and unregistered over time.
 */
public class DynamicMBeanExporter extends MBeanExporter implements DynamicMBeanExportOperations
{
    static private ThreadLocal<MBeanServer> threadServer = new ThreadLocal<MBeanServer>(); 

    /**
     * Instantiates a new dynamic MBean exporter.
     */
    public DynamicMBeanExporter() 
    { 
        // For consistency, try to continue to use the last MBeanServer used in the same thread 
        MBeanServer server = threadServer.get(); 
        if (server != null) 
        { 
            setServer(server); 
        } 

        // Make replace existing the default registration behavior 
        setRegistrationBehavior(MBeanRegistrationSupport.REGISTRATION_REPLACE_EXISTING); 
        setAutodetectMode(MBeanExporter.AUTODETECT_NONE); 
    } 

    @Override
    public void setServer(MBeanServer server) 
    { 
        threadServer.set(server); 
        super.setServer(server); 
    } 
	
    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.enterprise.repo.management.DynamicMBeanExportOperations#unregisterMBean(javax.management.ObjectName)
     */
    public void unregisterMBean(ObjectName objectName)
    {
        doUnregister(objectName);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.enterprise.repo.management.DynamicMBeanExportOperations#registerMBean(java.lang.Object,
     * javax.management.ObjectName)
     */
    @SuppressWarnings("unchecked")
    public ObjectName registerMBean(Object managedResource, ObjectName objectName)
    {
        Object mbean;
        if (isMBean(managedResource.getClass()))
        {
            mbean = managedResource;
        }
        else
        {
            mbean = createAndConfigureMBean(managedResource, managedResource.getClass().getName());
        }
        ObjectName actualObjectName = objectName;
        try
        {
            doRegister(mbean, actualObjectName);
        }
        catch (JMException e)
        {
            throw new RuntimeException(e);
        }
        return actualObjectName;
    }
}