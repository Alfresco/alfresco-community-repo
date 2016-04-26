/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
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