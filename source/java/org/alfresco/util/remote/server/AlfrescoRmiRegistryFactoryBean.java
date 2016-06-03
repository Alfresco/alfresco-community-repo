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
