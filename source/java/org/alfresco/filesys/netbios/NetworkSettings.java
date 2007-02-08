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
package org.alfresco.filesys.netbios;

import java.net.InetAddress;

/**
 * The network settings class contains various Windows Networking settings that are needed by the
 * NetBIOS and SMB layers.
 */
public class NetworkSettings
{

    // Broadcast mask for broadcast messages

    private static String m_broadcastMask;

    // Domain name/workgroup that this node is part of

    private static String m_domain;

    // Subnet mask address

    private static InetAddress m_subnetAddr;

    /**
     * Determine the boradcast mask from the local hosts TCP/IP address
     * 
     * @param addr TCP/IP address to set the broadcast mask for, in 'nnn.nnn.nnn.nnn' format.
     */
    public static String GenerateBroadcastMask(String addr) throws java.net.UnknownHostException
    {

        // Check if the broadcast mask has already been set

        if (m_broadcastMask != null)
            return m_broadcastMask;

        // Set the TCP/IP address string

        String localIP = addr;

        if (localIP == null)
            localIP = InetAddress.getLocalHost().getHostAddress();

        // Find the location of the first dot in the TCP/IP address

        int dotPos = localIP.indexOf('.');
        if (dotPos != -1)
        {

            // Extract the leading IP address value

            String ipStr = localIP.substring(0, dotPos);
            int ipVal = Integer.valueOf(ipStr).intValue();

            // Determine the broadcast mask to use

            if (ipVal <= 127)
            {

                // Class A address

                m_broadcastMask = "" + ipVal + ".255.255.255";
            }
            else if (ipVal <= 191)
            {

                // Class B adddress

                dotPos++;
                while (localIP.charAt(dotPos) != '.' && dotPos < localIP.length())
                    dotPos++;

                if (dotPos < localIP.length())
                    m_broadcastMask = localIP.substring(0, dotPos) + ".255.255";
            }
            else if (ipVal <= 223)
            {

                // Class C address

                dotPos++;
                int dotCnt = 1;

                while (dotCnt < 3 && dotPos < localIP.length())
                {

                    // Check if the current character is a dot

                    if (localIP.charAt(dotPos++) == '.')
                        dotCnt++;
                }

                if (dotPos < localIP.length())
                    m_broadcastMask = localIP.substring(0, dotPos - 1) + ".255";
            }
        }

        // Check if the broadcast mask has been set, if not then use a general
        // broadcast mask

        if (m_broadcastMask == null)
        {

            // Invalid TCP/IP address string format, use a general broadcast mask
            // for now.

            m_broadcastMask = "255.255.255.255";
        }

        // Return the broadcast mask string

        return m_broadcastMask;
    }

    /**
     * Return the broadcast mask as an address.
     * 
     * @return java.net.InetAddress
     */
    public final static InetAddress getBroadcastAddress() throws java.net.UnknownHostException
    {

        // Check if the subnet address is valid

        if (m_subnetAddr == null)
        {

            // Generate the subnet mask

            String subnet = GenerateBroadcastMask(null);
            m_subnetAddr = InetAddress.getByName(subnet);
        }

        // Return the subnet mask address

        return m_subnetAddr;
    }

    /**
     * Get the broadcast mask.
     * 
     * @return java.lang.String
     */
    public static String getBroadcastMask()
    {
        return m_broadcastMask;
    }

    /**
     * Get the local domain/workgroup name.
     */
    public static String getDomain()
    {
        return m_domain;
    }

    /**
     * Determine if the broadcast mask has been setup.
     */
    public static boolean hasBroadcastMask()
    {
        if (m_broadcastMask == null)
            return false;
        return true;
    }

    /**
     * Set the broadcast mask to be used for broadcast packets.
     * 
     * @param mask java.lang.String
     */
    public static void setBroadcastMask(String mask)
    {
        m_broadcastMask = mask;
    }

    /**
     * Set the local domain/workgroup name.
     * 
     * @param domain java.lang.String
     */
    public static void setDomain(String domain)
    {
        m_domain = domain;
    }
}