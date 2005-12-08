/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.filesys.smb.mailslot;

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

        int sts = m_dgramSocket.sendDatagram(destNbName, buf, 0, len);
        if ( sts != len)
            logger.debug("WinsockNetBIOS host announce error");
    }
}