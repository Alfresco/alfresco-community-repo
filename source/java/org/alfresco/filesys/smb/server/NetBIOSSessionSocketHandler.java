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
import org.alfresco.filesys.smb.mailslot.TcpipNetBIOSHostAnnouncer;

/**
 * NetBIOS Socket Session Handler Class
 */
public class NetBIOSSessionSocketHandler extends SessionSocketHandler
{
    // Session Thread group
    private static final ThreadGroup THREAD_GROUP_SESSION = new ThreadGroup("NETBIOS_SESSION_GROUP");

    /**
     * Class constructor
     * 
     * @param srv SMBServer
     * @param port int
     * @param bindAddr InetAddress
     * @param debug boolean
     */
    public NetBIOSSessionSocketHandler(SMBServer srv, int port, InetAddress bindAddr, boolean debug)
    {
        super("NetBIOS", srv, port, bindAddr, debug);
    }

    /**
     * Run the NetBIOS session socket handler
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
                    logger.debug("Waiting for NetBIOS session request ...");

                // Wait for a connection

                Socket sessSock = getSocket().accept();

                // Debug

                if (logger.isDebugEnabled() && hasDebug())
                    logger.debug("NetBIOS session request received from "
                            + sessSock.getInetAddress().getHostAddress());

                try
                {

                    // Create a packet handler for the session

                    PacketHandler pktHandler = new NetBIOSPacketHandler(sessSock);

                    // Create a server session for the new request, and set the session id.

                    SMBSrvSession srvSess = new SMBSrvSession(pktHandler, getServer());
                    srvSess.setSessionId(getNextSessionId());
                    srvSess.setUniqueId(pktHandler.getShortName() + srvSess.getSessionId());

                    // Add the session to the active session list

                    getServer().addSession(srvSess);

                    // Start the new session in a seperate thread

                    Thread srvThread = new Thread(THREAD_GROUP_SESSION, srvSess);
                    srvThread.setDaemon(true);
                    srvThread.setName("Sess_N" + srvSess.getSessionId() + "_"
                            + sessSock.getInetAddress().getHostAddress());
                    srvThread.start();
                }
                catch (Exception ex)
                {

                    // Debug

                    logger.error("NetBIOS Failed to create session, ", ex);
                }
            }
        }
        catch (SocketException ex)
        {

            // Do not report an error if the server has shutdown, closing the server socket
            // causes an exception to be thrown.

            if (hasShutdown() == false)
                logger.error("NetBIOS Socket error : ", ex);
        }
        catch (Exception ex)
        {

            // Do not report an error if the server has shutdown, closing the server socket
            // causes an exception to be thrown.

            if (hasShutdown() == false)
                logger.error("NetBIOS Server error : ", ex);
        }

        // Debug

        if (logger.isDebugEnabled() && hasDebug())
            logger.debug("NetBIOS session handler closed");
    }

    /**
     * Create the TCP/IP NetBIOS session socket handlers for the main SMB/CIFS server
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

        SessionSocketHandler sessHandler = new NetBIOSSessionSocketHandler(server, config.getNetBIOSSessionPort(), config
                .getSMBBindAddress(), sockDbg);
        sessHandler.initialize();

        // Add the session handler to the list of active handlers

        server.addSessionHandler(sessHandler);

        // Run the NetBIOS session handler in a seperate thread

        Thread nbThread = new Thread(sessHandler);
        nbThread.setName("NetBIOS_Handler");
        nbThread.start();

        // DEBUG

        if (logger.isDebugEnabled() && sockDbg)
            logger.debug("TCP NetBIOS session handler created on port " + config.getNetBIOSSessionPort());

        // Check if a host announcer should be created

        if (config.hasEnableAnnouncer())
        {

            // Create the TCP NetBIOS host announcer

            TcpipNetBIOSHostAnnouncer announcer = new TcpipNetBIOSHostAnnouncer();

            // Set the host name to be announced

            announcer.addHostName(config.getServerName());
            announcer.setDomain(config.getDomainName());
            announcer.setComment(config.getComment());
            announcer.setBindAddress(config.getSMBBindAddress());
            announcer.setPort(config.getNetBIOSDatagramPort());

            // Set the announcement interval

            if (config.getHostAnnounceInterval() > 0)
                announcer.setInterval(config.getHostAnnounceInterval());

            try
            {
                announcer.setBroadcastAddress(config.getBroadcastMask());
            }
            catch (Exception ex)
            {
            }

            // Set the server type flags

            announcer.setServerType(config.getServerType());

            // Enable debug output

            if (config.hasHostAnnounceDebug())
                announcer.setDebug(true);

            // Add the host announcer to the SMS servers list

            server.addHostAnnouncer(announcer);

            // Start the host announcer thread

            announcer.start();

            // DEBUG

            if (logger.isDebugEnabled() && sockDbg)
                logger.debug("TCP NetBIOS host announcer created on port " + config.getNetBIOSDatagramPort());
        }
    }
}
