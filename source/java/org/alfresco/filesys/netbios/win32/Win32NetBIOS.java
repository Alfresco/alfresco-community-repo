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
package org.alfresco.filesys.netbios.win32;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.alfresco.filesys.netbios.NetBIOSName;
import org.alfresco.filesys.util.DataBuffer;
import org.alfresco.filesys.util.IPAddress;
import org.alfresco.filesys.util.X64;

/**
 * Win32 NetBIOS Native Call Wrapper Class
 */
public class Win32NetBIOS
{

    // Constants
    //
    // FIND_NAME_BUFFER structure length

    protected final static int FindNameBufferLen = 33;

    // Exception if the native code DLL load failed

    private static Throwable m_loadDLLException;

    /**
     * Check if the native code was loaded successfully
     * 
     * @return boolean
     */
    public static final boolean isInitialized()
    {
        return m_loadDLLException == null ? true : false;
    }

    /**
     * Return the native code load exception
     * 
     * @return Throwable
     */
    public static final Throwable getInitializationException()
    {
        return m_loadDLLException;
    }

    /**
     * Check if NetBIOS is enabled on any network adapters
     * 
     * @return boolean
     */
    public static final boolean isAvailable() {
        
        // Check if the DLL was loaded successfully
        
        if ( isInitialized() == false)
            return false;
        
        // Check if there are any valid LANAs, if not then NetBIOS is not enabled or network
        // adapters that have NetBIOS enabled are not currently enabled
        
        int[] lanas = LanaEnum();
        if ( lanas != null && lanas.length > 0)
            return true;
        return false;
    }
    
    /**
     * Add a NetBIOS name to the local name table
     * 
     * @param lana int
     * @param name byte[]
     * @return int
     */
    public static native int AddName(int lana, byte[] name);

    /**
     * Add a group NetBIOS name to the local name table
     * 
     * @param lana int
     * @param name byte[]
     * @return int
     */
    public static native int AddGroupName(int lana, byte[] name);

    /**
     * Find a NetBIOS name, return the name buffer
     * 
     * @param lana int
     * @param name byte[]
     * @param nameBuf byte[]
     * @param bufLen int
     * @return int
     */
    public static native int FindNameRaw(int lana, byte[] name, byte[] nameBuf, int bufLen);

    /**
     * Find a NetBIOS name
     * 
     * @param lana int
     * @param name NetBIOSName
     * @return int
     */
    public static int FindName(int lana, NetBIOSName nbName)
    {

        // Allocate a buffer to receive the name details

        byte[] nameBuf = new byte[nbName.isGroupName() ? 65535 : 4096];

        // Get the raw NetBIOS name data

        int sts = FindNameRaw(lana, nbName.getNetBIOSName(), nameBuf, nameBuf.length);

        if (sts != NetBIOS.NRC_GoodRet)
            return -sts;

        // Unpack the FIND_NAME_HEADER structure

        DataBuffer buf = new DataBuffer(nameBuf, 0, nameBuf.length);

        int nodeCount = buf.getShort();
        buf.skipBytes(1);
        boolean isGroupName = buf.getByte() == 0 ? false : true;

        // Unpack the FIND_NAME_BUFFER structures

        int curPos = buf.getPosition();

        for (int i = 0; i < nodeCount; i++)
        {

            // FIND_NAME_BUFFER:
            // UCHAR length
            // UCHAR access_control
            // UCHAR frame_control
            // UCHAR destination_addr[6]
            // UCHAR source_addr[6]
            // UCHAR routing_info[18]

            // Skip to the source_addr field

            buf.skipBytes(9);

            // Source address field format should be 0.0.n.n.n.n for TCP/IP address

            if (buf.getByte() == 0 && buf.getByte() == 0)
            {

                // Looks like a TCP/IP format address, unpack it

                byte[] ipAddr = new byte[4];

                ipAddr[0] = (byte) buf.getByte();
                ipAddr[1] = (byte) buf.getByte();
                ipAddr[2] = (byte) buf.getByte();
                ipAddr[3] = (byte) buf.getByte();

                // Add the address to the list of TCP/IP addresses for the NetBIOS name

                nbName.addIPAddress(ipAddr);

                // Skip to the start of the next FIND_NAME_BUFFER structure

                curPos += FindNameBufferLen;
                buf.setPosition(curPos);
            }
        }

        // Return the node count

        return nodeCount;
    }

    /**
     * Delete a NetBIOS name from the local name table
     * 
     * @param lana int
     * @param name byte[]
     * @return int
     */
    public static native int DeleteName(int lana, byte[] name);

    /**
     * Enumerate the available LANAs
     * 
     * @return int[]
     */
    public static int[] LanaEnumerate()
    {
        // Make sure that there is an active network adapter as making calls to the LanaEnum native call
        // causes problems when there are no active network adapters.
        
        boolean adapterAvail = false;
        
        try
        {
            // Enumerate the available network adapters and check for an active adapter, not including
            // the loopback adapter
            
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            
            while ( nis.hasMoreElements() && adapterAvail == false)
            {
                NetworkInterface ni = nis.nextElement();
                if ( ni.getName().equals("lo") == false)
                {
                    // Make sure the adapter has a valid IP address
                    
                    Enumeration<InetAddress> addrs = ni.getInetAddresses();
                    if ( addrs.hasMoreElements())
                        adapterAvail = true;
                }
            }
            
        }
        catch ( SocketException ex)
        {
        }
        
        // Check if there are network adapter(s) available
        
        if ( adapterAvail == false)
            return null;
        
        // Call the native code to return the available LANA list
        
        return LanaEnum();
    }
    
    /**
     * Enumerate the available LANAs
     * 
     * @return int[]
     */
    private static native int[] LanaEnum();

    /**
     * Reset the NetBIOS environment
     * 
     * @param lana int
     * @return int
     */
    public static native int Reset(int lana);

    /**
     * Listen for an incoming session request
     * 
     * @param lana int
     * @param toName byte[]
     * @param fromName byte[]
     * @param callerName byte[]
     * @return int
     */
    public static native int Listen(int lana, byte[] toName, byte[] fromName, byte[] callerName);

    /**
     * Receive a data packet on a session
     * 
     * @param lana int
     * @param lsn int
     * @param buf byte[]
     * @param off int
     * @param maxLen int
     * @return int
     */
    public static native int Receive(int lana, int lsn, byte[] buf, int off, int maxLen);

    /**
     * Send a data packet on a session
     * 
     * @param lana int
     * @param lsn int
     * @param buf byte[]
     * @param off int
     * @param len int
     * @return int
     */
    public static native int Send(int lana, int lsn, byte[] buf, int off, int len);

    /**
     * Send a datagram to a specified name
     * 
     * @param lana int
     * @param srcNum int
     * @param destName byte[]
     * @param buf byte[]
     * @param off int
     * @param len int
     * @return int
     */
    public static native int SendDatagram(int lana, int srcNum, byte[] destName, byte[] buf, int off, int len);

    /**
     * Send a broadcast datagram
     * 
     * @param lana
     * @param buf byte[]
     * @param off int
     * @param len int
     * @return int
     */
    public static native int SendBroadcastDatagram(int lana, byte[] buf, int off, int len);

    /**
     * Receive a datagram on a specified name
     * 
     * @param lana int
     * @param nameNum int
     * @param buf byte[]
     * @param off int
     * @param maxLen int
     * @return int
     */
    public static native int ReceiveDatagram(int lana, int nameNum, byte[] buf, int off, int maxLen);

    /**
     * Receive a broadcast datagram
     * 
     * @param lana int
     * @param nameNum int
     * @param buf byte[]
     * @param off int
     * @param maxLen int
     * @return int
     */
    public static native int ReceiveBroadcastDatagram(int lana, int nameNum, byte[] buf, int off, int maxLen);

    /**
     * Hangup a session
     * 
     * @param lsn int
     * @return int
     */
    public static native int Hangup(int lana, int lsn);

    /**
     * Return the local computers NetBIOS name
     * 
     * @return String
     */
    public static native String GetLocalNetBIOSName();

    /**
     * Return the local domain name
     * 
     * @return String
     */
    public static native String GetLocalDomainName();

    /**
     * Return a comma delimeted list of WINS server TCP/IP addresses, or null if no WINS servers are
     * configured.
     * 
     * @return String
     */
    public static native String getWINSServerList();

    /**
     * Find the TCP/IP address for a LANA
     * 
     * @param lana int
     * @return String
     */
    public static final String getIPAddressForLANA(int lana)
    {

        // Get the local NetBIOS name

        String localName = GetLocalNetBIOSName();
        if (localName == null)
            return null;

        // Create a NetBIOS name for the local name

        NetBIOSName nbName = new NetBIOSName(localName, NetBIOSName.WorkStation, false);

        // Get the local NetBIOS name details

        int sts = FindName(lana, nbName);

        if (sts == -NetBIOS.NRC_EnvNotDef)
        {

            // Reset the LANA then try the name lookup again

            Reset(lana);
            sts = FindName(lana, nbName);
        }

        // Check if the name lookup was successful

        String ipAddr = null;

        if (sts >= 0)
        {

            // Get the first IP address from the list

            ipAddr = nbName.getIPAddressString(0);
        }

        // Return the TCP/IP address for the LANA

        return ipAddr;
    }

    /**
     * Find the adapter name for a LANA
     * 
     * @param lana int
     * @return String
     */
    public static final String getAdapterNameForLANA(int lana)
    {

        // Get the TCP/IP address for a LANA

        String ipAddr = getIPAddressForLANA(lana);
        if (ipAddr == null)
            return null;

        // Get the list of available network adapters

        Hashtable<String, NetworkInterface> adapters = getNetworkAdapterList();
        String adapterName = null;

        if (adapters != null)
        {

            // Find the network adapter for the TCP/IP address

            NetworkInterface ni = adapters.get(ipAddr);
            if (ni != null)
                adapterName = ni.getDisplayName();
        }

        // Return the adapter name for the LANA

        return adapterName;
    }

    /**
     * Find the LANA for a TCP/IP address
     * 
     * @param addr String
     * @return int
     */
    public static final int getLANAForIPAddress(String addr)
    {

        // Check if the address is a numeric TCP/IP address

        if (IPAddress.isNumericAddress(addr) == false)
            return -1;

        // Get a list of the available NetBIOS LANAs

        int[] lanas = LanaEnum();
        if (lanas == null || lanas.length == 0)
            return -1;

        // Search for the LANA with the matching TCP/IP address

        for (int i = 0; i < lanas.length; i++)
        {

            // Get the current LANAs TCP/IP address

            String curAddr = getIPAddressForLANA(lanas[i]);
            if (curAddr != null && curAddr.equals(addr))
                return lanas[i];
        }

        // Failed to find the LANA for the specified TCP/IP address

        return -1;
    }

    /**
     * Find the LANA for a network adapter
     * 
     * @param name String
     * @return int
     */
    public static final int getLANAForAdapterName(String name)
    {

        // Get the list of available network adapters

        Hashtable<String, NetworkInterface> niList = getNetworkAdapterList();

        // Search for the address of the specified network adapter

        Enumeration<String> niEnum = niList.keys();

        while (niEnum.hasMoreElements())
        {

            // Get the current TCP/IP address

            String ipAddr = niEnum.nextElement();
            NetworkInterface ni = niList.get(ipAddr);

            if (ni.getDisplayName().equalsIgnoreCase(name))
            {

                // Return the LANA for the network adapters TCP/IP address

                return getLANAForIPAddress(ipAddr);
            }
        }

        // Failed to find matching network adapter

        return -1;
    }

    /**
     * Return a hashtable of NetworkInterfaces indexed by TCP/IP address
     * 
     * @return Hashtable<String,NetworkInterface>
     */
    private static final Hashtable<String, NetworkInterface> getNetworkAdapterList()
    {

        // Get a list of the local network adapters

        Hashtable<String, NetworkInterface> niList = new Hashtable<String, NetworkInterface>();

        try
        {

            // Enumerate the available network adapters

            Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces();

            while (niEnum.hasMoreElements())
            {

                // Get the current network interface details

                NetworkInterface ni = niEnum.nextElement();
                Enumeration<InetAddress> addrEnum = ni.getInetAddresses();

                while (addrEnum.hasMoreElements())
                {

                    // Get the address and add the adapter to the list indexed via the numeric IP
                    // address string

                    InetAddress addr = addrEnum.nextElement();
                    niList.put(addr.getHostAddress(), ni);
                }
            }
        }
        catch (Exception ex)
        {
        }

        // Return the network adapter list

        return niList;
    }

    //---------- Winsock based NetBIOS interface ----------//
    
    /**
     * Initialize the NetBIOS socket interface
     * 
     * @exception WinsockNetBIOSException   If a Winsock error occurs
     */
    protected static native void InitializeSockets()
        throws WinsockNetBIOSException;
    
    /**
     * Shutdown the NetBIOS socket interface
     */
    protected static native void ShutdownSockets();
    
    /**
     * Create a NetBIOS socket
     * 
     * @param lana int
     * @return int
     * @exception WinsockNetBIOSException   If a Winsock error occurs
     */
    protected static native int CreateSocket(int lana)
        throws WinsockNetBIOSException;
    
    /**
     * Create a NetBIOS datagram socket
     * 
     * @param lana int
     * @return int
     * @exception WinsockNetBIOSException   If a Winsock error occurs
     */
    protected static native int CreateDatagramSocket(int lana)
        throws WinsockNetBIOSException;
    
    /**
     * Bind a NetBIOS socket to a name to listen for incoming sessions
     * 
     * @param sockPtr int
     * @param name byte[]
     * @exception WinsockNetBIOSException   If a Winsock error occurs
     */
    protected static native int BindSocket(int sockPtr, byte[] name)
        throws WinsockNetBIOSException;
    
    /**
     * Listen for an incoming connection
     * 
     * @param sockPtr int
     * @param callerName byte[]
     * @return int
     * @exception WinsockNetBIOSException   If a Winsock error occurs
     */
    protected static native int ListenSocket(int sockPtr, byte[] callerName)
        throws WinsockNetBIOSException;

    /**
     * Close a NetBIOS socket
     * 
     * @param sockPtr int
     */
    protected static native void CloseSocket(int sockPtr);
    
    /**
     * Send data on a session socket
     * 
     * @param sockPtr int
     * @param buf byte[]
     * @param off int
     * @param len int
     * @return int
     * @exception WinsockNetBIOSException   If a Winsock error occurs
     */
    protected static native int SendSocket(int sockPtr, byte[] buf, int off, int len)
        throws WinsockNetBIOSException;

    /**
     * Receive data on a session socket
     * 
     * @param sockPtr int
     * @param toName byte[]
     * @param buf byte[]
     * @param off int
     * @param maxLen int
     * @return int
     * @exception WinsockNetBIOSException   If a Winsock error occurs
     */
    protected static native int ReceiveSocket(int sockPtr, byte[] buf, int off, int maxLen)
        throws WinsockNetBIOSException;

    /**
     * Send data on a datagram socket
     * 
     * @param sockPtr int
     * @param toName byte[]
     * @param buf byte[]
     * @param off int
     * @param len int
     * @return int
     * @exception WinsockNetBIOSException   If a Winsock error occurs
     */
    protected static native int SendSocketDatagram(int sockPtr, byte[] toName, byte[] buf, int off, int len)
        throws WinsockNetBIOSException;
    
    /**
     * Wait for a network address change event, block until a change occurs or the Winsock NetBIOS
     * interface is shut down
     */
    public static native void waitForNetworkAddressChange();
    
    /**
     * Static initializer used to load the native code library
     */
    static
    {
        // Check if we are running under 64 bit Windows
        
        String dllName = "Win32NetBIOS";
        
        if ( X64.isWindows64())
            dllName = "Win32NetBIOSx64";
        
        // Load the Win32 NetBIOS interface library

        try
        {
            System.loadLibrary( dllName);
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();
            
            // Save the native code load exception

            m_loadDLLException = ex;
        }
    }
}
