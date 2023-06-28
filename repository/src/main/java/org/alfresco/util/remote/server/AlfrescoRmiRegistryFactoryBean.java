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
package org.alfresco.util.remote.server;

import org.alfresco.util.remote.server.socket.HostConfigurableSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * This class controls the RMI connectivity via <code>alfresco.jmx.connector.enabled</code> property
 *
 * @author alex.mukha
 */
public class AlfrescoRmiRegistryFactoryBean implements FactoryBean<Registry>, DisposableBean
{
    private final Logger LOG = LoggerFactory.getLogger(AlfrescoRmiRegistryFactoryBean.class);

    private boolean created = false;

    private boolean enabled;

    private int port;

    private Registry registry;

    public AlfrescoRmiRegistryFactoryBean(boolean enabled, int port, HostConfigurableSocketFactory socketFactory) {
        this.enabled = enabled;
        this.port = port;
        if(this.enabled)
        {
            initRegistry(socketFactory);
        }
    }

    private void initRegistry(HostConfigurableSocketFactory socketFactory) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Looking for RMI registry at port '" + this.port + "', using custom socket factory");
        }
        synchronized (LocateRegistry.class) {
            try {
                // Retrieve existing registry.
                this.created = true;
                this.registry = LocateRegistry.getRegistry(null, this.port, socketFactory);
                testRegistry(this.registry);
            }
            catch (RemoteException ex) {
                LOG.trace("RMI registry access threw exception", ex);
                LOG.debug("Could not detect RMI registry - creating new one");
                // Assume no registry found -> create new one.
                this.created = true;
                try {
                    this.registry = LocateRegistry.createRegistry(this.port, socketFactory, socketFactory);
                }
                catch (RemoteException e)
                {
                    LOG.error("Unable to create RMI Registry");
                }
            }
        }
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public void destroy() throws Exception {
        if (this.created) {
            LOG.debug("Unexporting RMI registry");
            UnicastRemoteObject.unexportObject(this.registry, true);
        }
    }

    @Override
    public Registry getObject() throws Exception {
        return this.registry;
    }

    @Override
    public Class<?> getObjectType() {
        return (this.registry != null ? this.registry.getClass() : Registry.class);
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    private void testRegistry(Registry registry) throws RemoteException
    {
        registry.list();
    }
}
