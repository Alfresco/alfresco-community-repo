package org.alfresco.util.remote.server;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.UUID;

import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.remoting.rmi.RmiServiceExporter;

/**
 * Concrete implementation of a remoting InputStream based on RMI.
 * 
 * @author <a href="mailto:Michael.Shavnev@effective-soft.com">Michael Shavnev</a>
 * @since Alfresco 2.2
 */
public class RmiRemoteInputStreamServer extends AbstractRemoteInputStreamServer
{
    private RmiServiceExporter rmiServiceExporter;

    public RmiRemoteInputStreamServer(InputStream inputStream)
    {
        super(inputStream);
    }

    public String start(String host, int port) throws RemoteException
    {
        String name = inputStream.getClass().getName() + UUID.randomUUID();
        rmiServiceExporter = new RmiServiceExporter();
        rmiServiceExporter.setServiceName(name);
        rmiServiceExporter.setRegistryPort(port);
        rmiServiceExporter.setRegistryHost(host);
        rmiServiceExporter.setServiceInterface(RemoteInputStreamServer.class);
        rmiServiceExporter.setService(this);
        rmiServiceExporter.afterPropertiesSet();
        return name;
    }

    /**
     * Closes the stream and the RMI connection to the peer.
     */
    public void close() throws IOException
    {
        try
        {
            inputStream.close();
        }
        finally
        {
            if (rmiServiceExporter != null)
            {
                try
                {
                    rmiServiceExporter.destroy();
                }
                catch (Throwable e)
                {
                    throw new IOException(e.getMessage());
                }
            }
        }
    }

    /**
     * Utility method to lookup a remote stream peer over RMI.
     */
    public static RemoteInputStreamServer obtain(String host, int port, String name) throws RemoteException
    {
        RmiProxyFactoryBean rmiProxyFactoryBean = new RmiProxyFactoryBean();
        rmiProxyFactoryBean.setServiceUrl("rmi://" + host + ":" + port + "/" + name);
        rmiProxyFactoryBean.setServiceInterface(RemoteInputStreamServer.class);
        rmiProxyFactoryBean.setRefreshStubOnConnectFailure(true);
        try
        {
            rmiProxyFactoryBean.afterPropertiesSet();
        }
        catch (Exception e)
        {
            throw new RemoteException("Error create rmi proxy");
        }
        return (RemoteInputStreamServer) rmiProxyFactoryBean.getObject();
    }
}
