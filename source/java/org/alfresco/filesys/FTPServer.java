/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.filesys;

import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.ftp.FTPNetworkServer;
import org.alfresco.filesys.server.NetworkServer;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * FTP Server Class
 * 
 * <p>Create and start the server components required to run the FTP server.
 * 
 * @author GKSpencer
 */
public class FTPServer extends AbstractLifecycleBean
{
    private static final Log logger = LogFactory.getLog("org.alfresco.ftp.server");

    // Server configuration

    private ServerConfiguration filesysConfig;

    // The actual FTP server
    
    private FTPNetworkServer ftpServer;
    
    /**
     * Class constructor
     *
     * @param serverConfig ServerConfiguration
     */
    public FTPServer(ServerConfiguration serverConfig)
    {
        this.filesysConfig = serverConfig;
    }

    /**
     * Return the server configuration
     * 
     * @return ServerConfiguration
     */
    public final ServerConfiguration getConfiguration()
    {
        return filesysConfig;
    }

    /**
     * @return Returns true if the server started up without any errors
     */
    public boolean isStarted()
    {
        return (filesysConfig != null && filesysConfig.isFTPServerEnabled());
    }

    /**
     * Start the FTP server components
     * 
     * @exception SocketException If a network error occurs
     * @exception IOException If an I/O error occurs
     */
    public final void startServer() throws SocketException, IOException
    {
        try
        {
            // Create the FTP server, if enabled
            
            if (filesysConfig.isFTPServerEnabled())
            {
                // Create the FTP server
                
                ftpServer = new FTPNetworkServer(filesysConfig);
                filesysConfig.addServer(ftpServer);
            }


            // Start the server
            if(ftpServer != null)
            {
                // Start the FTP server
                if (logger.isInfoEnabled())
                    logger.info("Starting server " + ftpServer.getProtocolName() + " ...");

                ftpServer.startServer();
            }
        }
        catch (Throwable e)
        {
            filesysConfig = null;
            throw new AlfrescoRuntimeException("Failed to start FTP Server", e);
        }
        // success
    }

    /**
     * Stop the FTP server components
     */
    public final void stopServer()
    {
        if (filesysConfig == null)
        {
            // initialisation failed
            return;
        }
        
        // Shutdown the FTP server
        
        if ( ftpServer != null)
        {
            if (logger.isInfoEnabled())
                logger.info("Shutting server " + ftpServer.getProtocolName() + " ...");

            // Stop the server
            
            ftpServer.shutdownServer(false);
            
            // Remove the server from the global list
            
            getConfiguration().removeServer(ftpServer.getProtocolName());
            ftpServer = null;
        }
        
        // Clear the configuration
        
        filesysConfig = null;
    }

    /**
     * Runs the FTP server directly
     * 
     * @param args String[]
     */
    public static void main(String[] args)
    {
        PrintStream out = System.out;

        out.println("FTP Server Test");
        out.println("---------------");

        try
        {
            // Create the configuration service in the same way that Spring creates it
            
            ApplicationContext ctx = new ClassPathXmlApplicationContext("alfresco/application-context.xml");

            // Get the FTP server bean
            
            FTPServer server = (FTPServer) ctx.getBean("ftpServer");
            if (server == null)
            {
                throw new AlfrescoRuntimeException("Server bean 'ftpServer' not defined");
            }

            // Stop the CIFS server components, if running
            
            NetworkServer srv = server.getConfiguration().findServer("SMB");
            if ( srv != null)
                srv.shutdownServer(true);

            srv = server.getConfiguration().findServer("NetBIOS");
            if ( srv != null)
                srv.shutdownServer(true);
            
            // Only wait for shutdown if the FTP server is enabled
            
            if ( server.getConfiguration().isFTPServerEnabled())
            {
                
                // FTP server should have automatically started
                //
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
            throw new AlfrescoRuntimeException("Failed to start FTP server", e);
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Failed to start FTP server", e);
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        stopServer();
    }
    
}
