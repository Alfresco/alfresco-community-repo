/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.filesys;

import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketException;
import java.util.Vector;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.jlan.oncrpc.mount.MountServer;
import org.alfresco.jlan.oncrpc.nfs.NFSConfigSection;
import org.alfresco.jlan.oncrpc.nfs.NFSServer;
import org.alfresco.jlan.oncrpc.portmap.PortMapperServer;
import org.alfresco.jlan.server.NetworkServer;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * NFS Server Class
 * 
 * <p>Create and start the various server components required to run the NFS server.
 * 
 * @author GKSpencer
 */
public class NFSServerBean extends AbstractLifecycleBean
{
	// Debug logging
	
    private static final Log logger = LogFactory.getLog("org.alfresco.nfs.server");

    // Server configuration and sections
    
    private ServerConfiguration m_filesysConfig;
    private NFSConfigSection m_nfsConfig;

    private NfsServerNodeMonitor nodeMonitor;

    // List of NFS server components
    
    private Vector<NetworkServer> m_serverList = new Vector<NetworkServer>();
    
    /**
     * Class constructor
     *
     * @param serverConfig ServerConfiguration
     */
    public NFSServerBean(ServerConfiguration serverConfig)
    {
        m_filesysConfig = serverConfig;
    }

    /**
     * Return the server configuration
     * 
     * @return ServerConfiguration
     */
    public final ServerConfiguration getConfiguration()
    {
        return m_filesysConfig;
    }

    public void setNodeMonitor(NfsServerNodeMonitor nodeMonitor)
    {
        this.nodeMonitor = nodeMonitor;
    }

    /**
     * Check if the server is started/enabled
     * 
     * @return Returns true if the server started up without any errors
     */
    public boolean isStarted()
    {
        return (!m_serverList.isEmpty() && m_filesysConfig.isServerRunning( "NFS"));
    }

    /**
     * Start the NFS server components
     * 
     * @exception SocketException If a network error occurs
     * @exception IOException If an I/O error occurs
     */
    public final void startServer() throws SocketException, IOException
    {
        try
        {
            // Create the NFS, mount and portmapper servers, if enabled
            
            m_nfsConfig = (NFSConfigSection) m_filesysConfig.getConfigSection( NFSConfigSection.SectionName);
          
            if (m_nfsConfig != null)
            {
                // Create the portmapper server, if enabled
                
                if (m_nfsConfig.hasNFSPortMapper())
                    m_serverList.add(new PortMapperServer(m_filesysConfig));

                // Create the mount and main NFS servers
                
                m_serverList.add(new MountServer(m_filesysConfig));
                NFSServer nfsServer = new NFSServer(m_filesysConfig);
                m_serverList.add(nfsServer);
                if (null != nodeMonitor)
                {
                    nodeMonitor.setNfsServer(nfsServer);
                }
                
                // Add the servers to the configuration
                
                for (NetworkServer server : m_serverList)
                {
                    m_filesysConfig.addServer(server);
                }
            }

            // Start the server(s)

            for (NetworkServer server : m_serverList)
            {
                if (logger.isInfoEnabled())
                    logger.info("Starting server " + server.getProtocolName() + " ...");

                // Start the server

                server.startServer();
            }
        }
        catch (Throwable e)
        {
        	for (NetworkServer server : m_serverList)
            {
        		getConfiguration().removeServer(server.getProtocolName());
            }
            
        	m_serverList.clear();
            throw new AlfrescoRuntimeException("Failed to start NFS Server", e);
        }
    }

    /**
     * Stop the NFS server components
     */
    public final void stopServer()
    {
        if (null != nodeMonitor)
        {
            nodeMonitor.setEnabled(false);
        }

        if (m_filesysConfig == null)
        {
            // initialisation failed
            return;
        }
        
        // Shutdown the NFS server components, in reverse order

        for ( int i = m_serverList.size() - 1; i >= 0; i--)
        {
        	// Get the current server from the list
        	
        	NetworkServer server = m_serverList.get( i);
            if (logger.isInfoEnabled())
                logger.info("Shutting server " + server.getProtocolName() + " ...");

            // Stop the server
            
            server.shutdownServer(false);
            
            // Remove the server from the global list
            
            getConfiguration().removeServer(server.getProtocolName());
        }
        
        // Clear the server list
        
        m_serverList.clear();
    }

    /**
     * Runs the NFS server directly
     * 
     * @param args String[]
     */
    public static void main(String[] args)
    {
        PrintStream out = System.out;

        out.println("NFS Server Test");
        out.println("----------------");

        try
        {
            // Create the configuration service in the same way that Spring creates it
            
            ApplicationContext ctx = new ClassPathXmlApplicationContext("alfresco/application-context.xml");

            // Get the NFS server bean
            
            NFSServerBean server = (NFSServerBean) ctx.getBean("nfsServer");
            if (server == null)
            {
                throw new AlfrescoRuntimeException("Server bean 'nfsServer' not defined");
            }

            // Stop the FTP server, if running
            
            NetworkServer srv = server.getConfiguration().findServer("FTP");
            if ( srv != null)
                srv.shutdownServer(true);
            
            // Stop the CIFS server, if running
            
            srv = server.getConfiguration().findServer("SMB");
            if ( srv != null)
                srv.shutdownServer(true);
            
            // Only wait for shutdown if the NFS server is enabled
            
            if ( server.getConfiguration().hasConfigSection( NFSConfigSection.SectionName))
            {
                
                // NFS server should have automatically started
                // Wait for shutdown via the console
                
                out.println("Enter 'x' to shutdown ...");
                boolean shutdown = false;
    
                // Wait while the server runs, user may stop the server by typing a key
                
                while (shutdown == false)
                {
    
                    // Wait for the user to enter the shutdown key
    
                    int ch = System.in.read();
    
                    if (ch == 'x' || ch == 'X')
                        shutdown = true;
    
                    synchronized (server)
                    {
                        server.wait(20);
                    }
                }
    
                // Stop the server
                
                server.stopServer();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        System.exit(1);
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        try
        {
            startServer();
        }
        catch (SocketException e)
        {
            throw new AlfrescoRuntimeException("Failed to start NFS server", e);
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Failed to start NFS server", e);
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        stopServer();
        
        // Clear the configuration
        m_filesysConfig = null;
    }
}
