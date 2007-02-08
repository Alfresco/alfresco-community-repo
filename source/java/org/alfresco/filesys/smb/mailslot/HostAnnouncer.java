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
package org.alfresco.filesys.smb.mailslot;

import java.io.IOException;

import org.alfresco.filesys.netbios.NetBIOSName;
import org.alfresco.filesys.netbios.win32.WinsockNetBIOSException;
import org.alfresco.filesys.smb.ServerType;
import org.alfresco.filesys.smb.TransactionNames;
import org.alfresco.filesys.util.StringList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * The host announcer class periodically broadcasts a host announcement datagram to inform other
 * Windows networking hosts of the local hosts existence and capabilities.
 */
public abstract class HostAnnouncer extends Thread
{

    // Debug logging

    protected static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol.mailslot");

    // Shutdown announcement interval and message count

    public static final int SHUTDOWN_WAIT = 2000; // 2 seconds
    public static final int SHUTDOWN_COUNT = 3;

    // Starting announcement interval, doubles until it reaches the configured interval

    public static final long STARTING_INTERVAL = 5000; // 5 seconds

    // Local host name(s) to announce

    private StringList m_names;

    // Domain to announce to

    private String m_domain;

    // Server comment string

    private String m_comment;

    // Announcement interval in minutes

    private int m_interval;

    // Server type flags, see org.alfresco.filesys.smb.SMBServerInfo

    private int m_srvtype = ServerType.WorkStation + ServerType.Server;

    // SMB mailslot packet

    private SMBMailslotPacket m_smbPkt;

    // Update count for the host announcement packet

    private byte m_updateCount;

    // Error count
    
    private int m_errorCount;
    
    // Shutdown flag, host announcer should remove the announced name as it shuts down

    private boolean m_shutdown = false;

    // Debug output enable

    private boolean m_debug;

    /**
     * HostAnnouncer constructor.
     */
    public HostAnnouncer()
    {

        // Common constructor

        commonConstructor();
    }

    /**
     * Create a host announcer.
     * 
     * @param name Host name to announce
     * @param domain Domain name to announce to
     * @param intval Announcement interval, in minutes
     */
    public HostAnnouncer(String name, String domain, int intval)
    {

        // Common constructor

        commonConstructor();

        // Add the host to the list of names to announce

        addHostName(name);
        setDomain(domain);
        setInterval(intval);
    }

    /**
     * Common constructor code
     */
    private final void commonConstructor()
    {

        // Allocate the host name list

        m_names = new StringList();
    }

    /**
     * Return the server comment string.
     * 
     * @return java.lang.String
     */
    public final String getComment()
    {
        return m_comment;
    }

    /**
     * Return the domain name that the host announcement is directed to.
     * 
     * @return java.lang.String
     */
    public final String getDomain()
    {
        return m_domain;
    }

    /**
     * Return the number of names being announced
     * 
     * @return int
     */
    public final int numberOfNames()
    {
        return m_names.numberOfStrings();
    }

    /**
     * Return the error count
     * 
     * @return int
     */
    protected final int getErrorCount()
    {
    	return m_errorCount;
    }
    
    /**
     * Return the specified host name being announced.
     * 
     * @param idx int
     * @return java.lang.String
     */
    public final String getHostName(int idx)
    {
        if (idx < 0 || idx > m_names.numberOfStrings())
            return null;
        return m_names.getStringAt(idx);
    }

    /**
     * Return the announcement interval, in minutes.
     * 
     * @return int
     */
    public final int getInterval()
    {
        return m_interval;
    }

    /**
     * Return the server type flags.
     * 
     * @return int
     */
    public final int getServerType()
    {
        return m_srvtype;
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
     * Enable/disable debug output
     * 
     * @param dbg true or false
     */
    public final void setDebug(boolean dbg)
    {
        m_debug = dbg;
    }

    /**
     * Initialize the host announcement SMB.
     * 
     * @param name String
     */
    protected final void initHostAnnounceSMB(String name)
    {

        // Allocate the transact SMB

        if (m_smbPkt == null)
            m_smbPkt = new SMBMailslotPacket();

        // Create the host announcement structure

        byte[] data = new byte[256];
        int pos = MailSlot.createHostAnnouncement(data, 0, name, m_comment, m_srvtype, m_interval, m_updateCount++);

        // Create the mailslot SMB

        m_smbPkt.initializeMailslotSMB(TransactionNames.MailslotBrowse, data, pos);
    }

    /**
     * Start the host announcer thread.
     */
    public void run()
    {

        // Initialize the host announcer

        try
        {

            // Initialize the host announcer datagram socket

            initialize();
        }
        catch (Exception ex)
        {

            // Debug

            logger.error("HostAnnouncer initialization error", ex);
            return;
        }

        // Clear the shutdown flag

        m_shutdown = false;

        // Send the host announcement datagram

        long sleepTime = STARTING_INTERVAL;
        long sleepNormal = getInterval() * 60 * 1000;

        while (m_shutdown == false)
        {

            try
            {

                // Check if the network connection is valid

                if (isNetworkEnabled())
                {

                    // Loop through the host names to be announced

                    for (int i = 0; i < m_names.numberOfStrings(); i++)
                    {

                        // Create a host announcement transact SMB

                        String hostName = getHostName(i);
                        initHostAnnounceSMB(hostName);

                        // Send the host announce datagram

                        sendAnnouncement(hostName, m_smbPkt.getBuffer(), 0, m_smbPkt.getLength());

                        // DEBUG

                        if (logger.isDebugEnabled() && hasDebug())
                            logger.debug("HostAnnouncer: Announced host " + hostName);
                    }
                }
                else
                {

                    // Reset the sleep interval to the starting interval as the network connection
                    // is not
                    // available

                    sleepTime = STARTING_INTERVAL;
                }

                // Sleep for a while

                sleep(sleepTime);

                // Update the sleep interval, if the network connection is enabled

                if (isNetworkEnabled() && sleepTime < sleepNormal)
                {

                    // Double the sleep interval until it exceeds the configured announcement
                    // interval.
                    // This is to send out more broadcasts when the server first starts.

                    sleepTime *= 2;
                    if (sleepTime > sleepNormal)
                        sleepTime = sleepNormal;
                }
            }
            catch (WinsockNetBIOSException ex)
            {
                // Debug

                if (m_shutdown == false)
                    logger.error("HostAnnouncer error", ex);
                m_shutdown = true;
            }
            catch ( IOException ex)
            {
                // Debug

                if (m_shutdown == false)
                {
                    logger.error("HostAnnouncer error", ex);
                    logger.error(" Check <broadcast> setting in file-servers.xml");
                }
                m_shutdown = true;
            }
            catch (Exception ex)
            {
                // Debug

                if (m_shutdown == false)
                    logger.error("HostAnnouncer error", ex);
                m_shutdown = true;
            }
        }

        // Set the announcement interval to zero to indicate that the host is leaving Network
        // Neighborhood

        setInterval(0);

        // Clear the server flag in the announced host type

        if ((m_srvtype & ServerType.Server) != 0)
            m_srvtype -= ServerType.Server;

        // Send out a number of host announcement to remove the host name(s) from Network
        // Neighborhood

        for (int j = 0; j < SHUTDOWN_COUNT; j++)
        {

            // Loop through the host names to be announced

            for (int i = 0; i < m_names.numberOfStrings(); i++)
            {

                // Create a host announcement transact SMB

                String hostName = getHostName(i);
                initHostAnnounceSMB(hostName);

                // Send the host announce datagram

                try
                {

                    // Send the host announcement

                    sendAnnouncement(hostName, m_smbPkt.getBuffer(), 0, m_smbPkt.getLength());
                }
                catch (Exception ex)
                {
                }
            }

            // Sleep for a while

            try
            {
                sleep(SHUTDOWN_WAIT);
            }
            catch (InterruptedException ex)
            {
            }
        }
    }

    /**
     * Initialize the host announcer.
     * 
     * @exception Exception
     */
    protected void initialize() throws Exception
    {
    }

    /**
     * Determine if the network connection used for the host announcement is valid
     * 
     * @return boolean
     */
    public abstract boolean isNetworkEnabled();

    /**
     * Send an announcement broadcast.
     * 
     * @param hostName Host name being announced
     * @param buf Buffer containing the host announcement mailslot message.
     * @param offset Offset to the start of the host announcement message.
     * @param len Host announcement message length.
     */
    protected abstract void sendAnnouncement(String hostName, byte[] buf, int offset, int len) throws Exception;

    /**
     * Set the server comment string.
     * 
     * @param comment java.lang.String
     */
    public final void setComment(String comment)
    {
        m_comment = comment;
        if (m_comment != null && m_comment.length() > 80)
            m_comment = m_comment.substring(0, 80);
    }

    /**
     * Set the domain name that the host announcement are directed to.
     * 
     * @param name java.lang.String
     */
    public final void setDomain(String name)
    {
        m_domain = name.toUpperCase();
    }

    /**
     * Add a host name to the list of names to announce
     * 
     * @param name java.lang.String
     */
    public final void addHostName(String name)
    {
        m_names.addString(NetBIOSName.toUpperCaseName(name));
    }

    /**
     * Add a list of names to the announcement list
     * 
     * @param names StringList
     */
    public final void addHostNames(StringList names)
    {
        m_names.addStrings(names);
    }

    /**
     * Set the announcement interval, in minutes.
     * 
     * @param intval int
     */
    public final void setInterval(int intval)
    {
        m_interval = intval;
    }

    /**
     * Set the server type flags.
     * 
     * @param typ int
     */
    public final void setServerType(int typ)
    {
        m_srvtype = typ;
    }

    /**
     * Increment the error count
     * 
     * @return int
     */
    protected final int incrementErrorCount()
    {
    	return ++m_errorCount;
    }
    
    /**
     * Clear the error count
     */
    protected final void clearErrorCount()
    {
    	m_errorCount = 0;
    }
    
    /**
     * Shutdown the host announcer and remove the announced name from Network Neighborhood.
     */
    public final synchronized void shutdownAnnouncer()
    {

        // Set the shutdown flag and wakeup the main host announcer thread

        m_shutdown = true;
        interrupt();

        try
        {
            join(2000);
        }
        catch (InterruptedException ex)
        {
        }
    }
}