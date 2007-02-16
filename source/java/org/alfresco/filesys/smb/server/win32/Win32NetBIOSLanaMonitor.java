/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
package org.alfresco.filesys.smb.server.win32;

import java.util.BitSet;

import org.alfresco.filesys.netbios.win32.NetBIOSSocket;
import org.alfresco.filesys.netbios.win32.Win32NetBIOS;
import org.alfresco.filesys.netbios.win32.WinsockNetBIOSException;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.smb.mailslot.Win32NetBIOSHostAnnouncer;
import org.alfresco.filesys.smb.server.SMBServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Win32 NetBIOS LANA Monitor Class
 * <p>
 * Monitors the available NetBIOS LANAs to check for new network interfaces coming online. A session
 * socket handler will be created for new LANAs as they appear.
 */
public class Win32NetBIOSLanaMonitor extends Thread
{
    // Constants
    //
    // Initial LANA listener array size
    
    private static final int LanaListenerArraySize  = 256;
    
    // Debug logging

    private static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol");

    // Global LANA monitor

    private static Win32NetBIOSLanaMonitor _lanaMonitor;

    // Available LANA list and current status

    private BitSet m_lanas;
    private BitSet m_lanaSts;

    // LANA status listeners
    
    private LanaListener[] m_listeners;
    
    // SMB/CIFS server to add new session handlers to

    private SMBServer m_server;

    // Wakeup interval

    private long m_wakeup;

    // Shutdown request flag

    private boolean m_shutdown;

    // Debug output enable

    private boolean m_debug;

    /**
     * Class constructor
     * 
     * @param server SMBServer
     * @param lanas int[]
     * @param wakeup long
     * @param debug boolean
     */
    Win32NetBIOSLanaMonitor(SMBServer server, int[] lanas, long wakeup, boolean debug)
    {

        // Set the SMB server and wakeup interval

        m_server = server;
        m_wakeup = wakeup;

        m_debug = debug;

        // Set the current LANAs in the available LANAs list

        m_lanas   = new BitSet();
        m_lanaSts = new BitSet();
        
        if (lanas != null)
        {

            // Set the currently available LANAs

            for (int i = 0; i < lanas.length; i++)
                m_lanas.set(lanas[i]);
        }

        // Initialize the online LANA status list
        
        int[] curLanas = Win32NetBIOS.LanaEnumerate();
        
        if ( curLanas != null)
        {
            for ( int i = 0; i < curLanas.length; i++)
                m_lanaSts.set(curLanas[i], true);
        }
        
        // Set the global LANA monitor, if not already set

        if (_lanaMonitor == null)
            _lanaMonitor = this;

        // Start the LANA monitor thread

        setDaemon(true);
        start();
    }

    /**
     * Return the global LANA monitor
     * 
     * @return Win32NetBIOSLanaMonitor
     */
    public static Win32NetBIOSLanaMonitor getLanaMonitor()
    {
        return _lanaMonitor;
    }

    /**
     * Add a LANA listener
     * 
     * @param lana int
     * @param listener LanaListener
     */
    public synchronized final void addLanaListener(int lana, LanaListener l)
    {
        // Range check the LANA id
        
        if ( lana < 0 || lana > 255)
            return;
        
        // Check if the listener array has been allocated
        
        if ( m_listeners == null)
            m_listeners = new LanaListener[LanaListenerArraySize];
        
        //  Add the LANA listener
        
        m_listeners[lana] = l;

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug())
            logger.debug("Win32 NetBIOS register listener for LANA " + lana);
    }

    /**
     * Remove a LANA listener
     * 
     * @param lana int
     */
    public synchronized final void removeLanaListener(int lana)
    {
        // Validate the LANA id
        
        if ( m_listeners == null || lana < 0 || lana >= m_listeners.length)
            return;
        
        m_listeners[lana] = null;
    }
    
    /**
     * Thread method
     */
    public void run()
    {
        // Clear the shutdown flag

        m_shutdown = false;

        // If Winsock NetBIOS is not enabled then initialize the sockets interface
        
        ServerConfiguration config = m_server.getConfiguration();
        
        if ( config.useWinsockNetBIOS() == false)
        {
            try
            {
                NetBIOSSocket.initializeSockets();
            }
            catch (WinsockNetBIOSException ex)
            {
                // DEBUG

                if (logger.isDebugEnabled() && hasDebug())
                    logger.debug("Win32 NetBIOS initialization error", ex);
                
                // Shutdown the LANA monitor thread
                
                m_shutdown = true;
            }
        }
        
        // Loop until shutdown

        BitSet curLanas = new BitSet();
        
        while (m_shutdown == false)
        {

            // Wait for a network address change event

            Win32NetBIOS.waitForNetworkAddressChange();
            
            // Check if the monitor has been closed
            
            if ( m_shutdown == true)
                continue;

            // Clear the current active LANA bit set
            
            curLanas.clear();
            
            // Get the available LANA list

            int[] lanas = Win32NetBIOS.LanaEnumerate();
            if (lanas != null)
            {

                // Check if there are any new LANAs available

                Win32NetBIOSSessionSocketHandler sessHandler = null;

                for (int i = 0; i < lanas.length; i++)
                {

                    // Get the current LANA id, check if it's a known LANA

                    int lana = lanas[i];
                    curLanas.set(lana, true);
                    
                    if (m_lanas.get(lana) == false)
                    {

                        // DEBUG

                        if (logger.isDebugEnabled() && hasDebug())
                            logger.debug("Win32 NetBIOS found new LANA, " + lana);

                        // Create a single Win32 NetBIOS session handler using the specified LANA

                        sessHandler = new Win32NetBIOSSessionSocketHandler(m_server, lana, hasDebug());

                        try
                        {
                            sessHandler.initialize();
                        }
                        catch (Exception ex)
                        {

                            // DEBUG

                            if (logger.isDebugEnabled() && hasDebug())
                                logger.debug("Win32 NetBIOS failed to create session handler for LANA " + lana,
                                        ex);

                            // Clear the session handler

                            sessHandler = null;
                        }

                        // If the session handler was initialized successfully add it to the
                        // SMB/CIFS server

                        if (sessHandler != null)
                        {

                            // Add the session handler to the SMB/CIFS server

                            m_server.addSessionHandler(sessHandler);

                            // Run the NetBIOS session handler in a seperate thread

                            Thread nbThread = new Thread(sessHandler);
                            nbThread.setName("Win32NB_Handler_" + lana);
                            nbThread.start();

                            // DEBUG

                            if (logger.isDebugEnabled() && hasDebug())
                                logger.debug("Win32 NetBIOS created session handler on LANA " + lana);

                            // Check if a host announcer should be enabled

                            if (config.hasWin32EnableAnnouncer())
                            {

                                // Create a host announcer

                                Win32NetBIOSHostAnnouncer hostAnnouncer = new Win32NetBIOSHostAnnouncer(sessHandler,
                                        config.getDomainName(), config.getWin32HostAnnounceInterval());

                                // Add the host announcer to the SMB/CIFS server list

                                m_server.addHostAnnouncer(hostAnnouncer);
                                hostAnnouncer.start();

                                // DEBUG

                                if (logger.isDebugEnabled() && hasDebug())
                                    logger.debug("Win32 NetBIOS host announcer enabled on LANA " + lana);
                            }

                            // Set the LANA in the available LANA list, and set the current status to online

                            m_lanas.set(lana);
                            m_lanaSts.set(lana, true);
                            
                            // Add a listener for the new LANA
                            
                            addLanaListener( sessHandler.getLANANumber(), sessHandler);
                        }
                    }
                    else
                    {
                        // Check if the LANA has just come back online
                        
                        if ( m_lanaSts.get(lana) == false)
                        {
                            // Change the LANA status to indicate the LANA is back online
                            
                            m_lanaSts.set(lana, true);
                            
                            // Inform the listener that the LANA is back online

                            if ( m_listeners != null && lana < m_listeners.length &&
                                    m_listeners[lana] != null)
                                m_listeners[lana].lanaStatusChange(lana, true);
                            
                            // DEBUG

                            if (logger.isDebugEnabled() && hasDebug())
                                logger.debug("Win32 NetBIOS LANA online - " + lana);
                        }
                    }
                }
                
                // Check if there are any LANAs that have gone offline

                for ( int i = 0; i < m_lanaSts.length(); i++)
                {
                    if ( curLanas.get(i) == false && m_lanaSts.get(i) == true)
                    {
                        // DEBUG

                        if (logger.isDebugEnabled() && hasDebug())
                            logger.debug("Win32 NetBIOS LANA offline - " + i);

                        // Change the LANA status
                        
                        m_lanaSts.set(i, false);
                        
                        // Check if there is an associated listener for the LANA
                        
                        if ( m_listeners != null && m_listeners[i] != null)
                        {
                            // Notify the LANA listener that the LANA is now offline
                            
                            m_listeners[i].lanaStatusChange(i, false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Determine if debug output is enabled
     * 
     * @return boolean
     */
    public final boolean hasDebug()
    {
        return m_debug;
    }

    /**
     * Request the LANA monitor thread to shutdown
     */
    public final void shutdownRequest()
    {
        m_shutdown = true;
        
        // If Winsock NetBIOS is being used shutdown the Winsock interface
        
        if ( m_server.getConfiguration().useWinsockNetBIOS())
            NetBIOSSocket.shutdownSockets();
    }
}
