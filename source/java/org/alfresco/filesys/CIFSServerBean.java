/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys;

import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketException;
import java.util.Vector;

import org.alfresco.jlan.netbios.server.NetBIOSNameServer;
import org.alfresco.jlan.server.NetworkServer;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.smb.server.CIFSConfigSection;
import org.alfresco.jlan.smb.server.SMBServer;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * CIFS Server Class
 * 
 * <p>Create and start the various server components required to run the CIFS server.
 * 
 * @author GKSpencer
 */
public class CIFSServerBean extends AbstractLifecycleBean
{
    private static final Log logger = LogFactory.getLog("org.alfresco.smb.server");

    // Server configuration and sections
    
    private ServerConfiguration m_filesysConfig;
    private CIFSConfigSection m_cifsConfig;
    
    // List of CIFS server components
    
    private Vector<NetworkServer> serverList = new Vector<NetworkServer>();
    
    /**
     * Class constructor
     *
     * @param serverConfig ServerConfiguration
     */
    public CIFSServerBean(ServerConfiguration serverConfig)
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

    /**
     * @return Returns true if the server started up without any errors
     */
    public boolean isStarted()
    {
        return (!serverList.isEmpty() && m_filesysConfig.isServerRunning( "CIFS"));
    }

    /**
     * Start the CIFS server components
     * 
     * @exception SocketException If a network error occurs
     * @exception IOException If an I/O error occurs
     */
    public final void startServer() throws SocketException, IOException
    {
        try
        {
            // Create the SMB server and NetBIOS name server, if enabled
            
            m_cifsConfig = (CIFSConfigSection) m_filesysConfig.getConfigSection(CIFSConfigSection.SectionName);
            
            if (m_cifsConfig != null)
            {
                // Create the NetBIOS name server if NetBIOS SMB is enabled
                
                if (m_cifsConfig.hasNetBIOSSMB())
                    serverList.add(new NetBIOSNameServer(m_filesysConfig));

                // Create the SMB server
                
                serverList.add(new SMBServer(m_filesysConfig));
                
                // Add the servers to the configuration
                
                for (NetworkServer server : serverList)
                {
                    m_filesysConfig.addServer(server);
                }
            }

            // Start the CIFS server(s)

            for (NetworkServer server : serverList)
            {
                if (logger.isInfoEnabled())
                    logger.info("Starting server " + server.getProtocolName() + " ...");

                // Start the server
                @SuppressWarnings("unused")
                String serverName = server.getConfiguration().getServerName();
                server.startServer();
            }
        }
        catch (Throwable e)
        {
        	for (NetworkServer server : serverList)
            {
        		getConfiguration().removeServer(server.getProtocolName());
            }
        	
            serverList.clear();
            throw new AlfrescoRuntimeException("Failed to start CIFS Server", e);
        }
        // success
    }

    /**
     * Stop the CIFS server components
     */
    public final void stopServer()
    {
        if (m_filesysConfig == null)
        {
            // initialisation failed
            return;
        }
        
        // Shutdown the CIFs server components

        for ( NetworkServer server : serverList)
        {
            if (logger.isInfoEnabled())
                logger.info("Shutting server " + server.getProtocolName() + " ...");

            // Stop the server
            
            server.shutdownServer(false);
            
            // Remove the server from the global list
            
            getConfiguration().removeServer(server.getProtocolName());
        }
        
        // Clear the server list
        
        serverList.clear();
    }

    /**
     * Runs the CIFS server directly
     * 
     * @param args String[]
     */
    public static void main(String[] args)
    {
        PrintStream out = System.out;

        out.println("CIFS Server Test");
        out.println("----------------");

        try
        {
            // Create the configuration service in the same way that Spring creates it
            
            ApplicationContext ctx = new ClassPathXmlApplicationContext("alfresco/application-context.xml");

            // Get the CIFS server bean
            
            CIFSServerBean server = (CIFSServerBean) ctx.getBean("cifsServer");
            if (server == null)
            {
                throw new AlfrescoRuntimeException("Server bean 'cifsServer' not defined");
            }

            // Stop the FTP server, if running
            
            NetworkServer srv = server.getConfiguration().findServer("FTP");
            if ( srv != null)
                srv.shutdownServer(true);
            
            // Stop the NFS server, if running
            
            srv = server.getConfiguration().findServer("NFS");
            if ( srv != null)
                srv.shutdownServer(true);
            
            // Only wait for shutdown if the SMB/CIFS server is enabled
            
            if ( server.getConfiguration().hasConfigSection(CIFSConfigSection.SectionName))
            {
                
                // SMB/CIFS server should have automatically started
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
            throw new AlfrescoRuntimeException("Failed to start CIFS server", e);
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Failed to start CIFS server", e);
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
