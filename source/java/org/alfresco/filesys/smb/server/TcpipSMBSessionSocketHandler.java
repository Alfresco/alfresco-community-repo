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
package org.alfresco.filesys.smb.server;

import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import org.alfresco.filesys.server.config.ServerConfiguration;

/**
 * Native SMB Session Socket Handler Class
 */
public class TcpipSMBSessionSocketHandler extends SessionSocketHandler
{
    // Session Thread group
    private static final ThreadGroup THREAD_GROUP_SESSION = new ThreadGroup("SMB_SESSION_GROUP");

    /**
     * Class constructor
     * 
     * @param srv SMBServer
     * @param port int
     * @param bindAddr InetAddress
     * @param debug boolean
     */
    public TcpipSMBSessionSocketHandler(SMBServer srv, int port, InetAddress bindAddr, boolean debug)
    {
        super("TCP-SMB", srv, port, bindAddr, debug);
    }

    /**
     * Run the native SMB session socket handler
     */
    public void run()
    {

        try
        {

            // Clear the shutdown flag

            clearShutdown();

            // Wait for incoming connection requests

            while (hasShutdown() == false)
            {

                // Debug

                if (logger.isDebugEnabled() && hasDebug())
                    logger.debug("Waiting for TCP-SMB session request ...");

                // Wait for a connection

                Socket sessSock = getSocket().accept();

                // Debug

                if (logger.isDebugEnabled() && hasDebug())
                    logger.debug("TCP-SMB session request received from "
                            + sessSock.getInetAddress().getHostAddress());

                try
                {

                    // Create a packet handler for the session

                    PacketHandler pktHandler = new TcpipSMBPacketHandler(sessSock);

                    // Create a server session for the new request, and set the session id.

                    SMBSrvSession srvSess = new SMBSrvSession(pktHandler, getServer());
                    srvSess.setSessionId(getNextSessionId());
                    srvSess.setUniqueId(pktHandler.getShortName() + srvSess.getSessionId());

                    // Add the session to the active session list

                    getServer().addSession(srvSess);

                    // Start the new session in a seperate thread

                    Thread srvThread = new Thread(THREAD_GROUP_SESSION, srvSess);
                    srvThread.setDaemon(true);
                    srvThread.setName("Sess_T" + srvSess.getSessionId() + "_"
                            + sessSock.getInetAddress().getHostAddress());
                    srvThread.start();
                }
                catch (Exception ex)
                {

                    // Debug

                    logger.error("TCP-SMB Failed to create session, ", ex);
                }
            }
        }
        catch (SocketException ex)
        {

            // Do not report an error if the server has shutdown, closing the server socket
            // causes an exception to be thrown.

            if (hasShutdown() == false)
                logger.error("TCP-SMB Socket error : ", ex);
        }
        catch (Exception ex)
        {

            // Do not report an error if the server has shutdown, closing the server socket
            // causes an exception to be thrown.

            if (hasShutdown() == false)
                logger.error("TCP-SMB Server error : ", ex);
        }

        // Debug

        if (logger.isDebugEnabled() && hasDebug())
            logger.debug("TCP-SMB session handler closed");
    }

    /**
     * Create the TCP/IP native SMB/CIFS session socket handlers for the main SMB/CIFS server
     * 
     * @param server SMBServer
     * @param sockDbg boolean
     * @exception Exception
     */
    public final static void createSessionHandlers(SMBServer server, boolean sockDbg) throws Exception
    {

        // Access the server configuration

        ServerConfiguration config = server.getConfiguration();

        // Create the NetBIOS SMB handler

        SessionSocketHandler sessHandler = new TcpipSMBSessionSocketHandler(server, config.getTcpipSMBPort(), config
                .getSMBBindAddress(), sockDbg);

        sessHandler.initialize();
        server.addSessionHandler(sessHandler);

        // Run the TCP/IP SMB session handler in a seperate thread

        Thread tcpThread = new Thread(sessHandler);
        tcpThread.setName("TcpipSMB_Handler");
        tcpThread.start();

        // DEBUG

        if (logger.isDebugEnabled() && sockDbg)
            logger.debug("Native SMB TCP session handler created on port " + config.getTcpipSMBPort());
    }
}
