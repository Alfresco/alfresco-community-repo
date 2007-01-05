/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
