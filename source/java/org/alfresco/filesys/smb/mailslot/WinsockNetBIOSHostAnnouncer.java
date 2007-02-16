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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.smb.mailslot;

import java.io.IOException;

import org.alfresco.filesys.netbios.NetBIOSName;
import org.alfresco.filesys.netbios.win32.NetBIOS;
import org.alfresco.filesys.netbios.win32.NetBIOSSocket;
import org.alfresco.filesys.netbios.win32.Win32NetBIOS;
import org.alfresco.filesys.smb.server.win32.Win32NetBIOSSessionSocketHandler;

/**
 * Winsock NetBIOS Host Announcer Class
 * 
 * <p>
 * The host announcer class periodically broadcasts a host announcement datagram to inform other
 * Windows networking hosts of the local hosts existence and capabilities.
 * 
 * <p>
 * The Win32 NetBIOS host announcer sends out the announcements using datagrams sent via Winsock calls.
 */
public class WinsockNetBIOSHostAnnouncer extends HostAnnouncer
{
	// Number of send errors before marking the LANA as offline
	
	private static final int SendErrorCount	= 3;
	
    // Associated session handler

    private Win32NetBIOSSessionSocketHandler m_handler;

    // Winsock NetBIOS datagram socket
    
    private NetBIOSSocket m_dgramSocket;
    
    /**
     * Create a host announcer.
     * 
     * @param sessHandler Win32NetBIOSSessionSocketHandler
     * @param domain Domain name to announce to
     * @param intval Announcement interval, in minutes
     */
    public WinsockNetBIOSHostAnnouncer(Win32NetBIOSSessionSocketHandler handler, String domain, int intval)
    {

        // Save the handler

        m_handler = handler;

        // Add the host to the list of names to announce

        addHostName(handler.getServerName());
        setDomain(domain);
        setInterval(intval);
    }

    /**
     * Return the LANA
     * 
     * @return int
     */
    public final int getLana()
    {
        return m_handler.getLANANumber();
    }

    /**
     * Initialize the host announcer.
     * 
     * @exception Exception
     */
    protected void initialize() throws Exception
    {
        // Set the thread name

        setName("WinsockHostAnnouncer_L" + getLana());
        
        // Create the Winsock NetBIOS datagram socket
        
        m_dgramSocket = NetBIOSSocket.createDatagramSocket(getLana());
    }

    /**
     * Determine if the network connection used for the host announcement is valid
     * 
     * @return boolean
     */
    public boolean isNetworkEnabled()
    {
        return m_handler.isLANAValid();
    }

    /**
     * Send an announcement broadcast.
     * 
     * @param hostName Host name being announced
     * @param buf Buffer containing the host announcement mailslot message.
     * @param offset Offset to the start of the host announcement message.
     * @param len Host announcement message length.
     */
    protected void sendAnnouncement(String hostName, byte[] buf, int offset, int len) throws Exception
    {

        // Build the destination NetBIOS name using the domain/workgroup name

        NetBIOSName destNbName = new NetBIOSName(getDomain(), NetBIOSName.MasterBrowser, false);

        // Send the host announce datagram via the Win32 Netbios() API call

        boolean txOK = false;
        
        try
        {
	        int sts = m_dgramSocket.sendDatagram(destNbName, buf, 0, len);
	        if ( sts == len)
	        	txOK = true;
        }
        catch ( IOException ex)
        {
        	// Log the error

        	if ( logger.isErrorEnabled())
        		logger.error("Host announce error, " + ex.getMessage() + ", (LANA " + getLana() + ")");
        }

        // Check if the send was successful

        if ( txOK == false)
        {
            // Update the error count
            
            if ( incrementErrorCount() == SendErrorCount)
            {
            	//	Mark the LANA as offline
            	
            	m_handler.lanaStatusChange( getLana(), false);

            	// Clear the error count
            	
            	clearErrorCount();
            	
            	// Log the error

            	if ( logger.isErrorEnabled())
            		logger.error("Marked LANA as unavailable due to send errors, (LANA " + getLana() + ")");
            }
        }
        else
        {
        	// Clear the error count
        	
        	clearErrorCount();
        }
    }
}