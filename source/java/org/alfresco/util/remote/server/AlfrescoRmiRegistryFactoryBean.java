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

import org.springframework.remoting.rmi.RmiRegistryFactoryBean;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

/**
 * This class controls the RMI connectivity via <code>alfresco.jmx.connector.enabled</code> property
 *
 * @author alex.mukha
 */
public class AlfrescoRmiRegistryFactoryBean extends RmiRegistryFactoryBean
{
    private static final String ERR_MSG_NOT_ENABLED = "The RMI registry factory is disabled.";

    private boolean enabled = true;

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (enabled)
        {
            super.afterPropertiesSet();
        }
    }

    @Override
    protected Registry getRegistry(
            String registryHost,
            int registryPort,
            RMIClientSocketFactory clientSocketFactory,
            RMIServerSocketFactory serverSocketFactory) throws RemoteException
    {
        if(enabled)
        {
            return super.getRegistry(registryHost, registryPort, clientSocketFactory, serverSocketFactory);
        }
        else
        {
            throw new RemoteException(ERR_MSG_NOT_ENABLED);
        }
    }

    @Override
    protected Registry getRegistry(
            int registryPort,
            RMIClientSocketFactory clientSocketFactory,
            RMIServerSocketFactory serverSocketFactory) throws RemoteException
    {
        if(enabled)
        {
            return super.getRegistry(registryPort, clientSocketFactory, serverSocketFactory);
        }
        else
        {
            throw new RemoteException(ERR_MSG_NOT_ENABLED);
        }
    }

    @Override
    protected Registry getRegistry(int registryPort) throws RemoteException
    {
        if(enabled)
        {
            return super.getRegistry(registryPort);
        }
        else
        {
            throw new RemoteException(ERR_MSG_NOT_ENABLED);
        }
    }
}
