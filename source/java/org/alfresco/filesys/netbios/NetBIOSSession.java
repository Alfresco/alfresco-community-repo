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
package org.alfresco.filesys.netbios;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;

import org.alfresco.filesys.smb.NetworkSession;
import org.alfresco.filesys.util.DataPacker;
import org.alfresco.filesys.util.HexDump;
import org.alfresco.filesys.util.StringList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * NetBIOS session class.
 */
public final class NetBIOSSession implements NetworkSession
{
    private static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol.netbios");

    // Constants
    //
    // Caller name template

    public static final int MaxCallerNameTemplateLength = 8;
    public static final char SessionIdChar = '#';
    public static final char JVMIdChar = '@';
    public static final String ValidTemplateChars = "@#_";

    // Default find name buffer size

    private static final int FindNameBufferSize = 2048;

    // Default socket timeout, in milliseconds

    private static int _defTimeout = RFCNetBIOSProtocol.TMO;

    // Remote socket to connect to, default is 139.

    private int m_remotePort;

    // Socket used to connect and read/write to remote host

    private Socket m_nbSocket;

    // Input and output data streams, from the socket network connection

    private DataInputStream m_nbIn;
    private DataOutputStream m_nbOut;

    // Send/receive timeout, in milliseconds

    private int m_tmo = _defTimeout;

    // Local and remote name types

    private char m_locNameType = NetBIOSName.FileServer;
    private char m_remNameType = NetBIOSName.FileServer;

    // Unique session identifier, used to generate a unique caller name when opening a new session

    private static int m_sessIdx = 0;

    // Unique JVM id, used to generate a unique caller name when multiple JVMs may be running on the
    // same
    // host

    private static int m_jvmIdx = 0;

    // Caller name template string. The template is used to create a unique caller name when opening
    // a new session.
    // The template is appended to the local host name, which may be truncated to allow room for the
    // template to be
    // appended and still be within the 16 character NetBIOS name limit.
    //
    // The caller name generation replaces '#' characters with a zero padded session index as a hex
    // value and '@'
    // characters with a zero padded JVM index. Multiple '#' and/or '@' characters can be specified
    // to indicate the
    // field width. Any other characters in the template are passed through to the final caller name
    // string.
    //
    // The maximum template string length is 8 characters to allow for at least 8 characters from
    // the host name.

    private static String m_callerTemplate = "_##";

    // Truncated host name, caller name generation appends the caller template result to this string

    private static String m_localNamePart;

    // Transaction identifier, used for datagrams

    private static short m_tranIdx = 1;

    // RFC NetBIOS name service datagram socket

    private static DatagramSocket m_dgramSock = null;

    // Debug enable flag

    private static boolean m_debug = false;

    // Subnet mask, required for broadcast name lookup requests

    private static String m_subnetMask = null;

    // WINS server address

    private static InetAddress m_winsServer;

    // Name lookup types

    public static final int DNSOnly = 1;
    public static final int WINSOnly = 2;
    public static final int WINSAndDNS = 3;

    // Flag to control whether name lookups use WINS/NetBIOS lookup or DNS

    private static int m_lookupType = WINSAndDNS;

    // NetBIOS name lookup timeout value.

    private static int m_lookupTmo = 500;

    // Flag to control use of the '*SMBSERVER' name when connecting to a file server

    private static boolean m_useWildcardFileServer = true;

    /**
     * NetBIOS session class constructor. Create a NetBIOS session with the default socket number
     * and no current network connection.
     */
    public NetBIOSSession()
    {
        m_remotePort = RFCNetBIOSProtocol.PORT;
        m_nbSocket = null;
    }

    /**
     * NetBIOS session class constructor
     * 
     * @param tmo Send/receive timeout value in milliseconds
     */
    public NetBIOSSession(int tmo)
    {
        m_tmo = tmo;
        m_remotePort = RFCNetBIOSProtocol.PORT;
        m_nbSocket = null;
    }

    /**
     * NetBIOS session class constructor
     * 
     * @param tmo Send/receive timeout value in milliseconds
     * @param port Remote port to connect to
     */
    public NetBIOSSession(int tmo, int port)
    {
        m_tmo = tmo;
        m_remotePort = port;
        m_nbSocket = null;
    }

    /**
     * Return the protocol name
     * 
     * @return String
     */
    public final String getProtocolName()
    {
        return "TCP/IP NetBIOS";
    }

    /**
     * Determine if the session is connected to a remote host
     * 
     * @return boolean
     */
    public final boolean isConnected()
    {

        // Check if the socket is valid

        if (m_nbSocket == null)
            return false;
        return true;
    }

    /**
     * Check if there is data available on this network session
     * 
     * @return boolean
     * @exception IOException
     */
    public final boolean hasData() throws IOException
    {

        // Check if the connection is active

        if (m_nbSocket == null || m_nbIn == null)
            return false;

        // Check if there is data available

        return m_nbIn.available() > 0 ? true : false;
    }

    /**
     * Convert a host name string into RFC NetBIOS format.
     * 
     * @param hostName Host name to be converted.
     * @return Converted host name string.
     */
    public static String ConvertName(String hostName)
    {
        return ConvertName(hostName, NetBIOSName.FileServer);
    }

    /**
     * Convert a host name string into RFC NetBIOS format.
     * 
     * @param hostName Host name to be converted.
     * @param nameType NetBIOS name type, added as the 16th byte of the name before conversion.
     * @return Converted host name string.
     */
    public static String ConvertName(String hostName, char nameType)
    {

        // Build the name string with the name type, make sure that the host
        // name is uppercase.

        StringBuffer hName = new StringBuffer(hostName.toUpperCase());

        if (hName.length() > 15)
            hName.setLength(15);

        // Space pad the name then add the NetBIOS name type

        while (hName.length() < 15)
            hName.append(' ');
        hName.append(nameType);

        // Convert the NetBIOS name string to the RFC NetBIOS name format

        String convstr = new String("ABCDEFGHIJKLMNOP");
        StringBuffer nameBuf = new StringBuffer(32);

        int idx = 0;

        while (idx < hName.length())
        {

            // Get the current character from the host name string

            char ch = hName.charAt(idx++);

            if (ch == ' ')
            {

                // Append an encoded <SPACE> character

                nameBuf.append("CA");
            }
            else
            {

                // Append octet for the current character

                nameBuf.append(convstr.charAt((int) ch / 16));
                nameBuf.append(convstr.charAt((int) ch % 16));
            }

        } // end while

        // Return the encoded string

        return nameBuf.toString();
    }

    /**
     * Convert an encoded NetBIOS name to a normal name string
     * 
     * @param buf Buffer that contains the NetBIOS encoded name
     * @param off Offset that the name starts within the buffer
     * @return Normal NetBIOS name string
     */
    public static String DecodeName(byte[] buf, int off)
    {

        // Convert the RFC NetBIOS name string to a normal NetBIOS name string

        String convstr = new String("ABCDEFGHIJKLMNOP");
        StringBuffer nameBuf = new StringBuffer(16);

        int idx = 0;
        char ch1, ch2;

        while (idx < 32)
        {

            // Get the current encoded character pair from the encoded name string

            ch1 = (char) buf[off + idx];
            ch2 = (char) buf[off + idx + 1];

            if (ch1 == 'C' && ch2 == 'A')
            {

                // Append a <SPACE> character

                nameBuf.append(' ');
            }
            else
            {

                // Convert back to a character code

                int val = convstr.indexOf(ch1) << 4;
                val += convstr.indexOf(ch2);

                // Append the current character to the decoded name

                nameBuf.append((char) (val & 0xFF));
            }

            // Update the encoded string index

            idx += 2;

        } // end while

        // Return the decoded string

        return nameBuf.toString();
    }

    /**
     * Convert an encoded NetBIOS name to a normal name string
     * 
     * @param encnam RFC NetBIOS encoded name
     * @return Normal NetBIOS name string
     */

    public static String DecodeName(String encnam)
    {

        // Check if the encoded name string is valid, must be 32 characters

        if (encnam == null || encnam.length() != 32)
            return "";

        // Convert the RFC NetBIOS name string to a normal NetBIOS name string

        String convstr = new String("ABCDEFGHIJKLMNOP");
        StringBuffer nameBuf = new StringBuffer(16);

        int idx = 0;
        char ch1, ch2;

        while (idx < 32)
        {

            // Get the current encoded character pair from the encoded name string

            ch1 = encnam.charAt(idx);
            ch2 = encnam.charAt(idx + 1);

            if (ch1 == 'C' && ch2 == 'A')
            {

                // Append a <SPACE> character

                nameBuf.append(' ');
            }
            else
            {

                // Convert back to a character code

                int val = convstr.indexOf(ch1) << 4;
                val += convstr.indexOf(ch2);

                // Append the current character to the decoded name

                nameBuf.append((char) (val & 0xFF));
            }

            // Update the encoded string index

            idx += 2;

        } // end while

        // Return the decoded string

        return nameBuf.toString();
    }

    /**
     * Convert a host name string into RFC NetBIOS format.
     * 
     * @param hostName Host name to be converted.
     * @param nameType NetBIOS name type, added as the 16th byte of the name before conversion.
     * @param buf Buffer to write the encoded name into.
     * @param off Offset within the buffer to start writing.
     * @return Buffer position
     */
    public static int EncodeName(String hostName, char nameType, byte[] buf, int off)
    {

        // Build the name string with the name type, make sure that the host
        // name is uppercase.

        StringBuffer hName = new StringBuffer(hostName.toUpperCase());

        if (hName.length() > 15)
            hName.setLength(15);

        // Space pad the name then add the NetBIOS name type

        while (hName.length() < 15)
            hName.append(' ');
        hName.append(nameType);

        // Convert the NetBIOS name string to the RFC NetBIOS name format

        String convstr = new String("ABCDEFGHIJKLMNOP");
        int idx = 0;
        int bufpos = off;

        // Set the name length byte

        buf[bufpos++] = 0x20;

        // Copy the encoded NetBIOS name to the buffer

        while (idx < hName.length())
        {

            // Get the current character from the host name string

            char ch = hName.charAt(idx++);

            if (ch == ' ')
            {

                // Append an encoded <SPACE> character

                buf[bufpos++] = (byte) 'C';
                buf[bufpos++] = (byte) 'A';
            }
            else
            {

                // Append octet for the current character

                buf[bufpos++] = (byte) convstr.charAt((int) ch / 16);
                buf[bufpos++] = (byte) convstr.charAt((int) ch % 16);
            }

        } // end while

        // Null terminate the string

        buf[bufpos++] = 0;
        return bufpos;
    }

    /**
     * Find a NetBIOS name on the network
     * 
     * @param nbname NetBIOS name to search for, not yet RFC encoded
     * @param nbType Name type, appended as the 16th byte of the name
     * @param tmo Timeout value for receiving incoming datagrams
     * @return NetBIOS name details
     * @exception java.io.IOException If an I/O error occurs
     */
    public static NetBIOSName FindName(String nbName, char nbType, int tmo) throws java.io.IOException
    {

        // Call the main FindName method

        return FindName(new NetBIOSName(nbName, nbType, false), tmo);
    }

    /**
     * Find a NetBIOS name on the network
     * 
     * @param nbname NetBIOS name to search for
     * @param tmo Timeout value for receiving incoming datagrams
     * @return NetBIOS name details
     * @exception java.io.IOException If an I/O error occurs
     */
    public static NetBIOSName FindName(NetBIOSName nbName, int tmo) throws java.io.IOException
    {

        // Get the local address details

        InetAddress locAddr = InetAddress.getLocalHost();

        // Create a datagram socket

        if (m_dgramSock == null)
        {

            // Create a datagram socket

            m_dgramSock = new DatagramSocket();
        }

        // Set the datagram socket timeout, in milliseconds

        m_dgramSock.setSoTimeout(tmo);

        // Create a name lookup NetBIOS packet

        NetBIOSPacket nbpkt = new NetBIOSPacket();
        nbpkt.buildNameQueryRequest(nbName, m_tranIdx++);

        // Get the local host numeric address

        String locIP = locAddr.getHostAddress();
        int dotIdx = locIP.indexOf('.');
        if (dotIdx == -1)
            return null;

        // If a WINS server has been configured the request is sent directly to the WINS server, if
        // not then a broadcast is done on the local subnet.

        InetAddress destAddr = null;

        if (hasWINSServer() == false)
        {

            // Check if the subnet mask has been set, if not then generate a subnet mask

            if (getSubnetMask() == null)
                GenerateSubnetMask(null);

            // Build a broadcast destination address

            destAddr = InetAddress.getByName(getSubnetMask());
        }
        else
        {

            // Use the WINS server address

            destAddr = getWINSServer();
        }

        // Build the name lookup request

        DatagramPacket dgram = new DatagramPacket(nbpkt.getBuffer(), nbpkt.getLength(), destAddr,
                RFCNetBIOSProtocol.NAME_PORT);

        // Allocate a receive datagram packet

        byte[] rxbuf = new byte[FindNameBufferSize];
        DatagramPacket rxdgram = new DatagramPacket(rxbuf, rxbuf.length);

        // Create a NetBIOS packet using the receive buffer

        NetBIOSPacket rxpkt = new NetBIOSPacket(rxbuf);

        // DEBUG

        if (m_debug)
            nbpkt.DumpPacket(false);

        // Send the find name datagram

        m_dgramSock.send(dgram);

        // Receive a reply datagram

        boolean rxOK = false;

        do
        {

            // Receive a datagram packet

            m_dgramSock.receive(rxdgram);

            // DEBUG

            if (logger.isDebugEnabled() && m_debug)
            {
                logger.debug("NetBIOS: Rx Datagram");
                rxpkt.DumpPacket(false);
            }

            // Check if this is a valid response datagram

            if (rxpkt.isResponse() && rxpkt.getOpcode() == NetBIOSPacket.RESP_QUERY)
                rxOK = true;

        } while (!rxOK);

        // Get the list of names from the response, should only be one name

        NetBIOSNameList nameList = rxpkt.getAnswerNameList();
        if (nameList != null && nameList.numberOfNames() > 0)
            return nameList.getName(0);
        return null;
    }

    /**
     * Build a list of nodes that own the specified NetBIOS name.
     * 
     * @param nbname NetBIOS name to search for, not yet RFC encoded
     * @param nbType Name type, appended as the 16th byte of the name
     * @param tmo Timeout value for receiving incoming datagrams
     * @return List of node name Strings
     * @exception java.io.IOException If an I/O error occurs
     */
    public static StringList FindNameList(String nbName, char nbType, int tmo) throws IOException
    {

        // Get the local address details

        InetAddress locAddr = InetAddress.getLocalHost();

        // Create a datagram socket

        if (m_dgramSock == null)
        {

            // Create a datagram socket

            m_dgramSock = new DatagramSocket();
        }

        // Set the datagram socket timeout, in milliseconds

        m_dgramSock.setSoTimeout(tmo);

        // Create a name lookup NetBIOS packet

        NetBIOSPacket nbpkt = new NetBIOSPacket();

        nbpkt.setTransactionId(m_tranIdx++);
        nbpkt.setOpcode(NetBIOSPacket.NAME_QUERY);
        nbpkt.setFlags(NetBIOSPacket.FLG_BROADCAST);
        nbpkt.setQuestionCount(1);
        nbpkt.setQuestionName(nbName, nbType, NetBIOSPacket.NAME_TYPE_NB, NetBIOSPacket.NAME_CLASS_IN);

        // Get the local host numeric address

        String locIP = locAddr.getHostAddress();
        int dotIdx = locIP.indexOf('.');
        if (dotIdx == -1)
            return null;

        // If a WINS server has been configured the request is sent directly to the WINS server, if
        // not then a broadcast is done on the local subnet.

        InetAddress destAddr = null;

        if (hasWINSServer() == false)
        {

            // Check if the subnet mask has been set, if not then generate a subnet mask

            if (getSubnetMask() == null)
                GenerateSubnetMask(null);

            // Build a broadcast destination address

            destAddr = InetAddress.getByName(getSubnetMask());
        }
        else
        {

            // Use the WINS server address

            destAddr = getWINSServer();
        }

        // Build the request datagram

        DatagramPacket dgram = new DatagramPacket(nbpkt.getBuffer(), nbpkt.getLength(), destAddr,
                RFCNetBIOSProtocol.NAME_PORT);

        // Allocate a receive datagram packet

        byte[] rxbuf = new byte[FindNameBufferSize];
        DatagramPacket rxdgram = new DatagramPacket(rxbuf, rxbuf.length);

        // Create a NetBIOS packet using the receive buffer

        NetBIOSPacket rxpkt = new NetBIOSPacket(rxbuf);

        // DEBUG

        if (m_debug)
            nbpkt.DumpPacket(false);

        // Create a vector to store the remote host addresses

        Vector<InetAddress> addrList = new Vector<InetAddress>();

        // Calculate the end time, to stop receiving datagrams

        long endTime = System.currentTimeMillis() + tmo;

        // Send the find name datagram

        m_dgramSock.send(dgram);

        // Receive reply datagrams

        do
        {

            // Receive a datagram packet

            try
            {
                m_dgramSock.receive(rxdgram);

                // DEBUG

                if (logger.isDebugEnabled() && m_debug)
                {
                    logger.debug("NetBIOS: Rx Datagram");
                    rxpkt.DumpPacket(false);
                }

                // Check if this is a valid response datagram

                if (rxpkt.isResponse() && rxpkt.getOpcode() == NetBIOSPacket.RESP_QUERY)
                {

                    // Get the address of the remote host for this datagram and add it to the list
                    // of responders

                    addrList.add(rxdgram.getAddress());
                }
            }
            catch (java.io.IOException ex)
            {

                // DEBUG

                if (logger.isDebugEnabled() && m_debug)
                    logger.debug(ex.toString());
            }

        } while (System.currentTimeMillis() < endTime);

        // Check if we received any replies

        if (addrList.size() == 0)
            return null;

        // Create a node name list

        StringList nameList = new StringList();

        // Convert the reply addresses to node names

        for (int i = 0; i < addrList.size(); i++)
        {

            // Get the current address from the list

            InetAddress addr = addrList.elementAt(i);

            // Convert the address to a node name string

            String name = NetBIOSName(addr.getHostName());

            // Check if the name is already in the name list

            if (!nameList.containsString(name))
                nameList.addString(name);
        }

        // Return the node name list

        return nameList;
    }

    /**
     * Get the NetBIOS name list for the specified IP address
     * 
     * @param ipAddr String
     * @return NetBIOSNameList
     */
    public static NetBIOSNameList FindNamesForAddress(String ipAddr) throws UnknownHostException, SocketException
    {
    	return FindNamesForAddress( ipAddr, 1);
    }
    
    /**
     * Get the NetBIOS name list for the specified IP address
     * 
     * @param ipAddr String
     * @param retryCnt int
     * @return NetBIOSNameList
     */
    public static NetBIOSNameList FindNamesForAddress(String ipAddr, int retryCnt)
    	throws UnknownHostException, SocketException
    {

        // Create a datagram socket

        if (m_dgramSock == null)
        {

            // Create a datagram socket

            m_dgramSock = new DatagramSocket();
        }

        // Set the datagram socket timeout, in milliseconds

        m_dgramSock.setSoTimeout(5000);

        // Create a name lookup NetBIOS packet

        NetBIOSPacket nbpkt = new NetBIOSPacket();

        nbpkt.setTransactionId(m_tranIdx++);
        nbpkt.setOpcode(NetBIOSPacket.NAME_QUERY);
        nbpkt.setFlags(NetBIOSPacket.FLG_BROADCAST);
        nbpkt.setQuestionCount(1);
        nbpkt.setQuestionName("*\0\0\0\0\0\0\0\0\0\0\0\0\0\0", NetBIOSName.WorkStation, NetBIOSPacket.NAME_TYPE_NBSTAT,
                NetBIOSPacket.NAME_CLASS_IN);

        // Send the request to the specified address

        InetAddress destAddr = InetAddress.getByName(ipAddr);
        DatagramPacket dgram = new DatagramPacket(nbpkt.getBuffer(), nbpkt.getLength(), destAddr,
                RFCNetBIOSProtocol.NAME_PORT);

        // Allocate a receive datagram packet

        byte[] rxbuf = new byte[FindNameBufferSize];
        DatagramPacket rxdgram = new DatagramPacket(rxbuf, rxbuf.length);

        // Create a NetBIOS packet using the receive buffer

        NetBIOSPacket rxpkt = new NetBIOSPacket(rxbuf);

        // DEBUG

        if (logger.isDebugEnabled() && m_debug)
            nbpkt.DumpPacket(false);

        // Create a vector to store the remote hosts NetBIOS names

        NetBIOSNameList nameList = null;

        try
        {
        	// Loop until we get a valid reply or the retry count is zero
        	
        	while ( retryCnt-- > 0 && nameList == null)
        	{
	            // Send the name query datagram
	
	            m_dgramSock.send(dgram);
	
	            // Receive a datagram packet
	
	            m_dgramSock.receive(rxdgram);
	        	rxpkt.setLength( rxdgram.getLength());
	
	            // DEBUG
	
	            if (logger.isDebugEnabled() && m_debug)
	            {
	                logger.debug("NetBIOS: Rx Datagram");
	                rxpkt.DumpPacket(false);
	            }
	
	            // Check if this is a valid response datagram
	
	            if (rxpkt.isResponse() && rxpkt.getOpcode() == NetBIOSPacket.RESP_QUERY && rxpkt.getAnswerCount() >= 1)
	            {
	
	                // Get the received name list
	
	                nameList = rxpkt.getAdapterStatusNameList();
	                
	                // If the name list is valid update the names with the original address that was connected to
	                
	                if( nameList != null)
	                {
	                    for ( int i = 0; i < nameList.numberOfNames(); i++)
	                    {
	                        NetBIOSName nbName = nameList.getName(i);
	                        nbName.addIPAddress(destAddr.getAddress());
	                    }
	                }
	            }
        	}
        }
        catch (java.io.IOException ex)
        {

            // DEBUG

            if (logger.isDebugEnabled() && m_debug)
                logger.debug(ex.toString());

            // Unknown host

            throw new UnknownHostException(ipAddr);
        }

        // Return the NetBIOS name list

        return nameList;
    }

    /**
     * Determine the subnet mask from the local hosts TCP/IP address
     * 
     * @param addr TCP/IP address to set the subnet mask for, in 'nnn.nnn.nnn.nnn' format.
     */
    public static String GenerateSubnetMask(String addr) throws java.net.UnknownHostException
    {

        // Set the TCP/IP address string

        String localIP = addr;

        // Get the local TCP/IP address, if a null string has been specified

        if (localIP == null)
            localIP = InetAddress.getLocalHost().getHostAddress();

        // Find the location of the first dot in the TCP/IP address

        int dotPos = localIP.indexOf('.');
        if (dotPos != -1)
        {

            // Extract the leading IP address value

            String ipStr = localIP.substring(0, dotPos);
            int ipVal = Integer.valueOf(ipStr).intValue();

            // Determine the subnet mask to use

            if (ipVal <= 127)
            {

                // Class A address

                m_subnetMask = "" + ipVal + ".255.255.255";
            }
            else if (ipVal <= 191)
            {

                // Class B adddress

                dotPos++;
                while (localIP.charAt(dotPos) != '.' && dotPos < localIP.length())
                    dotPos++;

                if (dotPos < localIP.length())
                    m_subnetMask = localIP.substring(0, dotPos) + ".255.255";
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
                    m_subnetMask = localIP.substring(0, dotPos - 1) + ".255";
            }
        }

        // Check if the subnet mask has been set, if not then use a general
        // broadcast mask

        if (m_subnetMask == null)
        {

            // Invalid TCP/IP address string format, use a general broadcast mask
            // for now.

            m_subnetMask = "255.255.255.255";
        }

        // DEBUG

        if (logger.isDebugEnabled() && m_debug)
            logger.debug("NetBIOS: Set subnet mask to " + m_subnetMask);

        // Return the subnet mask string

        return m_subnetMask;
    }

    /**
     * Get the WINS/NetBIOS name lookup timeout, in milliseconds.
     * 
     * @return int
     */
    public static int getLookupTimeout()
    {
        return m_lookupTmo;
    }

    /**
     * Return the name lookup type that is used when setting up new sessions, valid values are
     * DNSOnly, WINSOnly, WINSAndDNS. DNSOnly is the default type.
     * 
     * @return int
     */
    public static int getLookupType()
    {
        return m_lookupType;
    }

    /**
     * Return the subnet mask string
     * 
     * @return Subnet mask string, in 'nnn.nnn.nnn.nnn' format
     */
    public static String getSubnetMask()
    {
        return m_subnetMask;
    }

    /**
     * Determine if the WINS server address is configured
     * 
     * @return boolean
     */
    public final static boolean hasWINSServer()
    {
        return m_winsServer != null ? true : false;
    }

    /**
     * Return the WINS server address
     * 
     * @return InetAddress
     */
    public final static InetAddress getWINSServer()
    {
        return m_winsServer;
    }

    /**
     * Determine if SMB session debugging is enabled
     * 
     * @return true if debugging is enabled, else false.
     */
    public static boolean isDebug()
    {
        return m_debug;
    }

    /**
     * Return the next session index
     * 
     * @return int
     */
    private final static synchronized int getSessionId()
    {
        return m_sessIdx++;
    }

    /**
     * Return the JVM unique id, used when generating caller names
     * 
     * @return int
     */
    public final static int getJVMIndex()
    {
        return m_jvmIdx;
    }

    /**
     * Convert the TCP/IP host name to a NetBIOS name string.
     * 
     * @return java.lang.String
     * @param hostName java.lang.String
     */
    public static String NetBIOSName(String hostName)
    {

        // Check if the host name contains a domain name

        String nbName = new String(hostName.toUpperCase());
        int pos = nbName.indexOf(".");

        if (pos != -1)
        {

            // Strip the domain name for the NetBIOS name

            nbName = nbName.substring(0, pos);
        }

        // Return the NetBIOS name string

        return nbName;
    }

    /**
     * Enable/disable NetBIOS session debugging
     * 
     * @param dbg true to enable debugging, else false
     */
    public static void setDebug(boolean dbg)
    {
        m_debug = dbg;
    }

    /**
     * Set the WINS/NetBIOS name lookup timeout value, in milliseconds.
     * 
     * @param tmo int
     */
    public static void setLookupTimeout(int tmo)
    {
        if (tmo >= 250)
            m_lookupTmo = tmo;
    }

    /**
     * Set the name lookup type(s) to be used when opening new sessions, valid values are DNSOnly,
     * WINSOnly, WINSAndDNS. DNSOnly is the default type.
     * 
     * @param typ int
     */
    public static void setLookupType(int typ)
    {
        if (typ >= DNSOnly && typ <= WINSAndDNS)
            m_lookupType = typ;
    }

    /**
     * Set the subnet mask string
     * 
     * @param subnet Subnet mask string, in 'nnn.nnn.nnn.nnn' format
     */
    public static void setSubnetMask(String subnet)
    {
        m_subnetMask = subnet;
    }

    /**
     * Set the WINS server address
     * 
     * @param addr InetAddress
     */
    public final static void setWINSServer(InetAddress addr)
    {
        m_winsServer = addr;
    }

    /**
     * Get the NetBIOS adapter status for the specified node.
     * 
     * @return java.util.Vector
     * @param nodeName java.lang.String
     */
    private static Vector AdapterStatus(String nodeName) throws java.io.IOException
    {

        // Create the socket

        DatagramSocket nameSock = new DatagramSocket();

        // Enable the timeout on the socket

        nameSock.setSoTimeout(2000);

        // Create an adapter status NetBIOS packet

        NetBIOSPacket nbpkt = new NetBIOSPacket();

        // nbpkt.setTransactionId( m_tranIdx++);
        nbpkt.setTransactionId(9999);
        nbpkt.setOpcode(NetBIOSPacket.NAME_QUERY);
        nbpkt.setFlags(NetBIOSPacket.FLG_BROADCAST);
        nbpkt.setQuestionCount(1);
        nbpkt.setQuestionName(nodeName, NetBIOSName.WorkStation, NetBIOSPacket.NAME_TYPE_NBSTAT,
                NetBIOSPacket.NAME_CLASS_IN);

        // Build a broadcast destination address

        InetAddress destAddr = InetAddress.getByName(nodeName);
        DatagramPacket dgram = new DatagramPacket(nbpkt.getBuffer(), nbpkt.getLength(), destAddr,
                RFCNetBIOSProtocol.NAME_PORT);

        // Allocate a receive datagram packet

        byte[] rxbuf = new byte[512];
        DatagramPacket rxdgram = new DatagramPacket(rxbuf, rxbuf.length);

        // Create a NetBIOS packet using the receive buffer

        NetBIOSPacket rxpkt = new NetBIOSPacket(rxbuf);

        // DEBUG

        if (logger.isDebugEnabled() && m_debug)
            nbpkt.DumpPacket(false);

        // Send the find name datagram

        nameSock.send(dgram);

        // Receive a reply datagram

        boolean rxOK = false;

        do
        {

            // Receive a datagram packet

            nameSock.receive(rxdgram);

            // DEBUG

            if (logger.isDebugEnabled() && m_debug)
            {
                logger.debug("NetBIOS: Rx Datagram");
                rxpkt.DumpPacket(false);
            }

            // Check if this is a valid response datagram

            if (rxpkt.isResponse() && rxpkt.getOpcode() == NetBIOSPacket.RESP_QUERY)
                rxOK = true;

        } while (!rxOK);

        // Return the remote host address

        return null;
    }

    /**
     * Connect to a remote host.
     * 
     * @param remHost Remote host node name/NetBIOS name.
     * @param locName Local name/NetBIOS name.
     * @param remAddr Optional remote address, if null then lookup will be done to convert name to
     *            address
     * @exception java.io.IOException I/O error occurred.
     * @exception java.net.UnknownHostException Remote host is unknown.
     */
    public void Open(String remHost, String locName, String remAddr) throws java.io.IOException,
            java.net.UnknownHostException
    {

        // Debug mode

        if (logger.isDebugEnabled() && m_debug)
            logger.debug("NetBIOS: Call " + remHost);

        // Convert the remote host name to an address

        boolean dnsLookup = false;
        InetAddress addr = null;

        // Set the remote address is specified

        if (remAddr != null)
        {

            // Use the specified remote address

            addr = InetAddress.getByName(remAddr);
        }
        else
        {

            // Try a WINS/NetBIOS type name lookup, if enabled

            if (getLookupType() != DNSOnly)
            {
                try
                {
                    NetBIOSName netName = FindName(remHost, NetBIOSName.FileServer, 500);
                    if (netName != null && netName.numberOfAddresses() > 0)
                        addr = InetAddress.getByName(netName.getIPAddressString(0));
                }
                catch (Exception ex)
                {
                }
            }

            // Try a DNS type name lookup, if enabled

            if (addr == null && getLookupType() != WINSOnly)
            {
                addr = InetAddress.getByName(remHost);
                dnsLookup = true;
            }
        }

        // Check if we translated the remote host name to an address

        if (addr == null)
            throw new java.net.UnknownHostException(remHost);

        // Debug mode

        if (logger.isDebugEnabled() && m_debug)
            logger.debug("NetBIOS: Remote node hase address " + addr.getHostAddress() + " ("
                    + (dnsLookup ? "DNS" : "WINS") + ")");

        // Determine the remote name to call

        String remoteName = null;

        if (getRemoteNameType() == NetBIOSName.FileServer && useWildcardFileServerName() == true)
            remoteName = "*SMBSERVER";
        else
            remoteName = remHost;

        // Open a session to the remote server

        int resp = openSession(remoteName, addr);

        // Check the server response

        if (resp == RFCNetBIOSProtocol.SESSION_ACK)
            return;
        else if (resp == RFCNetBIOSProtocol.SESSION_REJECT)
        {

            // Try the connection again with the remote host name

            if (remoteName.equals(remHost) == false)
                resp = openSession(remHost, addr);

            // Check if we got a valid response this time

            if (resp == RFCNetBIOSProtocol.SESSION_ACK)
                return;

            // Server rejected the connection

            throw new java.io.IOException("NetBIOS session reject");
        }
        else if (resp == RFCNetBIOSProtocol.SESSION_RETARGET)
            throw new java.io.IOException("NetBIOS ReTarget");

        // Invalid session response, hangup the session

        Close();
        throw new java.io.IOException("Invalid NetBIOS response, 0x" + Integer.toHexString(resp));
    }

    /**
     * Open a NetBIOS session to a remote server
     * 
     * @param remoteName String
     * @param addr InetAddress
     * @return int
     * @exception IOException
     */
    private final int openSession(String remoteName, InetAddress addr) throws IOException
    {

        // Create the socket

        m_nbSocket = new Socket(addr, m_remotePort);

        // Enable the timeout on the socket, and disable Nagle algorithm

        m_nbSocket.setSoTimeout(m_tmo);
        m_nbSocket.setTcpNoDelay(true);

        // Attach input/output streams to the socket

        m_nbIn = new DataInputStream(m_nbSocket.getInputStream());
        m_nbOut = new DataOutputStream(m_nbSocket.getOutputStream());

        // Allocate a buffer to receive the session response

        byte[] inpkt = new byte[RFCNetBIOSProtocol.SESSRESP_LEN];

        // Create the from/to NetBIOS names

        NetBIOSName fromName = createUniqueCallerName();
        NetBIOSName toName = new NetBIOSName(remoteName, getRemoteNameType(), false);

        // Debug

        if (logger.isDebugEnabled() && m_debug)
            logger.debug("NetBIOS: Call from " + fromName + " to " + toName);

        // Build the session request packet

        NetBIOSPacket nbPkt = new NetBIOSPacket();
        nbPkt.buildSessionSetupRequest(fromName, toName);

        // Send the session request packet

        m_nbOut.write(nbPkt.getBuffer(), 0, nbPkt.getLength());

        // Allocate a buffer for the session request response, and read the response

        int resp = -1;

        if (m_nbIn.read(inpkt, 0, RFCNetBIOSProtocol.SESSRESP_LEN) >= RFCNetBIOSProtocol.HEADER_LEN)
        {

            // Check the session request response

            resp = (int) (inpkt[0] & 0xFF);

            // Debug mode

            if (logger.isDebugEnabled() && m_debug)
                logger.debug("NetBIOS: Rx " + NetBIOSPacket.getTypeAsString(resp));
        }

        // Check for a positive response

        if (resp != RFCNetBIOSProtocol.SESSION_ACK)
        {

            // Close the socket and streams

            m_nbIn.close();
            m_nbIn = null;

            m_nbOut.close();
            m_nbOut = null;

            m_nbSocket.close();
            m_nbSocket = null;
        }

        // Return the response code

        return resp;
    }

    /**
     * Return the local NetBIOS name type.
     * 
     * @return char
     */
    public char getLocalNameType()
    {
        return m_locNameType;
    }

    /**
     * Return the remote NetBIOS name type.
     * 
     * @return char
     */
    public char getRemoteNameType()
    {
        return m_remNameType;
    }

    /**
     * Get the session timeout value
     * 
     * @return NetBIOS session timeout value
     */
    public int getTimeout()
    {
        return m_tmo;
    }

    /**
     * Close the NetBIOS session.
     * 
     * @exception IOException If an I/O error occurs
     */
    public void Close() throws IOException
    {

        // Debug mode

        if (logger.isDebugEnabled() && m_debug)
            logger.debug("NetBIOS: HangUp");

        // Close the session if active

        if (m_nbSocket != null)
        {
            m_nbSocket.close();
            m_nbSocket = null;
        }
    }

    /**
     * Receive a data packet from the remote host.
     * 
     * @param buf Byte buffer to receive the data into.
     * @param tmo Receive timeout in milliseconds, or zero for no timeout
     * @return Length of the received data.
     * @exception java.io.IOException I/O error occurred.
     */
    public int Receive(byte[] buf, int tmo) throws java.io.IOException
    {

        // Set the read timeout

        if (tmo != m_tmo)
        {
            m_nbSocket.setSoTimeout(tmo);
            m_tmo = tmo;
        }

        // Read a data packet, dump any session keep alive packets

        int pkttyp;
        int rdlen;

        do
        {

            // Read a packet header

            rdlen = m_nbIn.read(buf, 0, RFCNetBIOSProtocol.HEADER_LEN);

            // Debug mode

            if (logger.isDebugEnabled() && m_debug)
                logger.debug("NetBIOS: Read " + rdlen + " bytes");

            // Check if a header was received

            if (rdlen < RFCNetBIOSProtocol.HEADER_LEN)
                throw new java.io.IOException("NetBIOS Short Read");

            // Get the packet type from the header

            pkttyp = (int) (buf[0] & 0xFF);

        } while (pkttyp == RFCNetBIOSProtocol.SESSION_KEEPALIVE);

        // Debug mode

        if (logger.isDebugEnabled() && m_debug)
            logger.debug("NetBIOS: Rx Pkt Type = " + pkttyp + ", " + Integer.toHexString(pkttyp));

        // Check that the packet is a session data packet

        if (pkttyp != RFCNetBIOSProtocol.SESSION_MESSAGE)
            throw new java.io.IOException("NetBIOS Unknown Packet Type, " + pkttyp);

        // Extract the data size from the packet header

        int pktlen = (int) DataPacker.getShort(buf, 2);
        if (logger.isDebugEnabled() && m_debug)
            logger.debug("NetBIOS: Rx Data Len = " + pktlen);

        // Check if the user buffer is long enough to contain the data

        if (buf.length < (pktlen + RFCNetBIOSProtocol.HEADER_LEN))
        {

            // Debug mode

            logger.debug("NetBIOS: Rx Pkt Type = " + pkttyp + ", " + Integer.toHexString(pkttyp));
            logger.debug("NetBIOS: Rx Buf Too Small pkt=" + pktlen + " buflen=" + buf.length);
            HexDump.Dump(buf, 16, 0);

            throw new java.io.IOException("NetBIOS Recv Buffer Too Small (pkt=" + pktlen + "/buf=" + buf.length + ")");
        }

        // Read the data part of the packet into the users buffer, this may take
        // several reads

        int totlen = 0;
        int offset = RFCNetBIOSProtocol.HEADER_LEN;

        while (pktlen > 0)
        {

            // Read the data

            rdlen = m_nbIn.read(buf, offset, pktlen);

            // Update the received length and remaining data length

            totlen += rdlen;
            pktlen -= rdlen;

            // Update the user buffer offset as more reads will be required
            // to complete the data read

            offset += rdlen;

        } // end while reading data

        // Return the received data length, not including the NetBIOS header

        return totlen;
    }

    /**
     * Send a data packet to the remote host.
     * 
     * @param data Byte array containing the data to be sent.
     * @param siz Length of the data to send.
     * @return true if the data was sent successfully, else false.
     * @exception java.io.IOException I/O error occurred.
     */
    public boolean Send(byte[] data, int siz) throws java.io.IOException
    {

        // Check that the session is valid

        if (m_nbSocket == null)
            return false;

        // Debug mode

        if (logger.isDebugEnabled() && m_debug)
            logger.debug("NetBIOS: Tx " + siz + " bytes");

        // Fill in the NetBIOS message header, this is already allocated as
        // part of the users buffer.

        data[0] = (byte) RFCNetBIOSProtocol.SESSION_MESSAGE;
        data[1] = (byte) 0;

        DataPacker.putShort((short) siz, data, 2);

        // Output the data packet

        int bufSiz = siz + RFCNetBIOSProtocol.HEADER_LEN;
        m_nbOut.write(data, 0, bufSiz);
        return true;
    }

    /**
     * Set the local NetBIOS name type for this session.
     * 
     * @param nameType int
     */
    public void setLocalNameType(char nameType)
    {
        m_locNameType = nameType;
    }

    /**
     * Set the remote NetBIOS name type.
     * 
     * @param param char
     */
    public void setRemoteNameType(char nameType)
    {
        m_remNameType = nameType;
    }

    /**
     * Set the session timeout value
     * 
     * @param tmo Session timeout value
     */
    public void setTimeout(int tmo)
    {
        m_tmo = tmo;
    }

    /**
     * Set the caller session name template string that is appended to the local host name to create
     * a unique caller name.
     * 
     * @param template String
     * @exception NameTemplateExcepition
     */
    public final static void setCallerNameTemplate(String template) throws NameTemplateException
    {

        // Check if the template string is valid, is not too long

        if (template == null || template.length() == 0 || template.length() > MaxCallerNameTemplateLength)
            throw new NameTemplateException("Invalid template string, " + template);

        // Template must contain at least one session id template character

        if (template.indexOf(SessionIdChar) == -1)
            throw new NameTemplateException("No session id character in template");

        // Check if the template contains any invalid characters

        for (int i = 0; i < template.length(); i++)
        {
            if (ValidTemplateChars.indexOf(template.charAt(i)) == -1)
                throw new NameTemplateException("Invalid character in template, '" + template.charAt(i) + "'");
        }

        // Set the caller name template string

        m_callerTemplate = template;

        // Clear the local name part string so that it will be regenerated to match the new template
        // string

        m_localNamePart = null;
    }

    /**
     * Set the JVM index, used to generate unique caller names when multiple JVMs are run on the
     * same host.
     * 
     * @param jvmIdx int
     */
    public final static void setJVMIndex(int jvmIdx)
    {
        if (jvmIdx >= 0)
            m_jvmIdx = jvmIdx;
    }

    /**
     * Create a unique caller name for a new NetBIOS session. The unique name contains the local
     * host name plus an index that is unique for this JVM, plus an optional JVM index.
     * 
     * @return NetBIOSName
     */
    private final NetBIOSName createUniqueCallerName()
    {

        // Check if the local name part has been set

        if (m_localNamePart == null)
        {

            String localName = null;

            try
            {
                localName = InetAddress.getLocalHost().getHostName();
            }
            catch (Exception ex)
            {
            }

            // Check if the name contains a domain

            int pos = localName.indexOf(".");

            if (pos != -1)
                localName = localName.substring(0, pos);

            // Truncate the name if the host name plus the template is longer than 15 characters.

            int nameLen = 16 - m_callerTemplate.length();

            if (localName.length() > nameLen)
                localName = localName.substring(0, nameLen - 1);

            // Set the local host name part

            m_localNamePart = localName.toUpperCase();
        }

        // Get a unique session id and the unique JVM id

        int sessId = getSessionId();
        int jvmId = getJVMIndex();

        // Build the NetBIOS name string

        StringBuffer nameBuf = new StringBuffer(16);

        nameBuf.append(m_localNamePart);

        // Process the caller name template string

        int idx = 0;
        int len = -1;

        while (idx < m_callerTemplate.length())
        {

            // Get the current template character

            char ch = m_callerTemplate.charAt(idx++);

            switch (ch)
            {

            // Session id

            case SessionIdChar:
                len = findRepeatLength(m_callerTemplate, idx, SessionIdChar);
                appendZeroPaddedHexValue(sessId, len, nameBuf);
                idx += len - 1;
                break;

            // JVM id

            case JVMIdChar:
                len = findRepeatLength(m_callerTemplate, idx, JVMIdChar);
                appendZeroPaddedHexValue(jvmId, len, nameBuf);
                idx += len - 1;
                break;

            // Pass any other characters through to the name string

            default:
                nameBuf.append(ch);
                break;
            }
        }

        // Create the NetBIOS name object

        return new NetBIOSName(nameBuf.toString(), getLocalNameType(), false);
    }

    /**
     * Find the length of the character block in the specified string
     * 
     * @param str String
     * @param pos int
     * @param ch char
     * @return int
     */
    private final int findRepeatLength(String str, int pos, char ch)
    {
        int len = 1;

        while (pos < str.length() && str.charAt(pos++) == ch)
            len++;
        return len;
    }

    /**
     * Append a zero filled hex string to the specified string
     * 
     * @param val int
     * @param len int
     * @param str StringBuffer
     */
    private final void appendZeroPaddedHexValue(int val, int len, StringBuffer str)
    {

        // Create the hex string of the value

        String hex = Integer.toHexString(val);

        // Pad the final string as required

        for (int i = 0; i < len - hex.length(); i++)
            str.append("0");
        str.append(hex);
    }

    /**
     * Return the default socket timeout value
     * 
     * @return int
     */
    public static final int getDefaultTimeout()
    {
        return _defTimeout;
    }

    /**
     * Set the default socket timeout for new sessions
     * 
     * @param tmo int
     */
    public static final void setDefaultTimeout(int tmo)
    {
        _defTimeout = tmo;
    }

    /**
     * Return the use wildcard file server name flag status. If true the target name when conencting
     * to a remote file server will be '*SMBSERVER', if false the remote name will be used.
     * 
     * @return boolean
     */
    public static final boolean useWildcardFileServerName()
    {
        return m_useWildcardFileServer;
    }

    /**
     * Set the use wildcard file server name flag. If true the target name when conencting to a
     * remote file server will be '*SMBSERVER', if false the remote name will be used.
     * 
     * @param useWildcard boolean
     */
    public static final void setWildcardFileServerName(boolean useWildcard)
    {
        m_useWildcardFileServer = useWildcard;
    }

    /**
     * Finalize the NetBIOS session object
     */
    protected void finalize()
    {

        // Close the socket

        if (m_nbSocket != null)
        {
            try
            {
                m_nbSocket.close();
            }
            catch (java.io.IOException ex)
            {
            }
            m_nbSocket = null;
        }
    }
}