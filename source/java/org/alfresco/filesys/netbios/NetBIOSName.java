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

import java.net.InetAddress;
import java.util.StringTokenizer;
import java.util.Vector;

import org.alfresco.filesys.util.IPAddress;

/**
 * NetBIOS Name Class.
 */
public class NetBIOSName
{
    // NetBIOS name length
    
    public static final int NameLength  = 16;
    
    // NetBIOS name types - <computername> + type

    public static final char WorkStation            = 0x00;
    public static final char Messenger              = 0x01;
    public static final char RemoteMessenger        = 0x03;
    public static final char RASServer              = 0x06;
    public static final char FileServer             = 0x20;
    public static final char RASClientService       = 0x21;
    public static final char MSExchangeInterchange  = 0x22;
    public static final char MSExchangeStore        = 0x23;
    public static final char MSExchangeDirectory    = 0x24;
    public static final char LotusNotesServerService= 0x2B;
    public static final char ModemSharingService    = 0x30;
    public static final char ModemSharingClient     = 0x31;
    public static final char McCaffeeAntiVirus      = 0x42;
    public static final char SMSClientRemoteControl = 0x43;
    public static final char SMSAdminRemoteControl  = 0x44;
    public static final char SMSClientRemoteChat    = 0x45;
    public static final char SMSClientRemoteTransfer= 0x46;
    public static final char DECPathworksService    = 0x4C;
    public static final char MSExchangeIMC          = 0x6A;
    public static final char MSExchangeMTA          = 0x87;
    public static final char NetworkMonitorAgent    = 0xBE;
    public static final char NetworkMonitorApp      = 0xBF;

    // <domainname> + type

    public static final char Domain                 = 0x00; // Group
    public static final char DomainMasterBrowser    = 0x1B;
    public static final char DomainControllers      = 0x1C; // Group
    public static final char MasterBrowser          = 0x1D;
    public static final char DomainAnnounce         = 0x1E;

    // Browse master - __MSBROWSE__ + type

    public static final char BrowseMasterGroup = 0x01;

    // Browse master NetBIOS name

    public static final String BrowseMasterName = "\u0001\u0002__MSBROWSE__\u0002";

    // NetBIOS names

    public static final String SMBServer = "*SMBSERVER";
    public static final String SMBServer2 = "*SMBSERV";

    //  Adapter status request name
    
    public static final String AdapterStatusName = "*";
    
    // Default time to live for name registrations

    public static final int DefaultTTL = 28800; // 8 hours

    // Name conversion string

    private static final String EncodeConversion = "ABCDEFGHIJKLMNOP";

    // Character set to use when converting the NetBIOS name string to a byte array

    private static String _nameConversionCharset = null;

    // Name string and type

    private String m_name;
    private char m_type;

    // Name scope

    private String m_scope;

    // Group name flag

    private boolean m_group = false;

    // Local name flag

    private boolean m_local = true;

    // IP address(es) of the owner of this name

    private Vector<byte[]> m_addrList;

    // Time that the name expires and time to live

    private long m_expiry;
    private int m_ttl; // seconds

    /**
     * Create a unique NetBIOS name.
     * 
     * @param name java.lang.String
     * @param typ char
     * @param group
     */
    public NetBIOSName(String name, char typ, boolean group)
    {
        setName(name);
        setType(typ);
        setGroup(group);
    }

    /**
     * Create a unique NetBIOS name.
     * 
     * @param name java.lang.String
     * @param typ char
     * @param group boolean
     * @param ipaddr byte[]
     */
    public NetBIOSName(String name, char typ, boolean group, byte[] ipaddr)
    {
        setName(name);
        setType(typ);
        setGroup(group);
        addIPAddress(ipaddr);
    }

    /**
     * Create a unique NetBIOS name.
     * 
     * @param name java.lang.String
     * @param typ char
     * @param group boolean
     * @param ipList Vector<byte[]>
     */
    public NetBIOSName(String name, char typ, boolean group, Vector<byte[]> ipList)
    {
        setName(name);
        setType(typ);
        setGroup(group);
        addIPAddresses(ipList);
    }

    /**
     * Create a unique NetBIOS name.
     * 
     * @param name java.lang.String
     * @param typ char
     * @param group boolean
     * @param ipaddr byte[]
     * @param ttl int
     */
    public NetBIOSName(String name, char typ, boolean group, byte[] ipaddr, int ttl)
    {
        setName(name);
        setType(typ);
        setGroup(group);
        addIPAddress(ipaddr);
        setTimeToLive(ttl);
    }

    /**
     * Create a unique NetBIOS name.
     * 
     * @param name java.lang.String
     * @param typ char
     * @param group boolean
     * @param ipList Vector<byte[]>
     * @param ttl int
     */
    public NetBIOSName(String name, char typ, boolean group, Vector<byte[]> ipList, int ttl)
    {
        setName(name);
        setType(typ);
        setGroup(group);
        addIPAddresses(ipList);
        setTimeToLive(ttl);
    }

    /**
     * Create a NetBIOS name from a byte array
     * 
     * @param buf byte[]
     * @param off int
     */
    public NetBIOSName(byte[] buf, int off)
    {
        setName(new String(buf, off, NameLength - 1));
        setType((char) buf[off + NameLength - 1]);
    }

    /**
     * Create a NetBIOS name from an encoded name string
     * 
     * @param name String
     */
    public NetBIOSName(String name)
    {
        setName(name.substring(0, NameLength - 1).trim());
        setType(name.charAt(NameLength - 1));
    }

    /**
     * Create a NetBIOS name from the specified name and scope
     * 
     * @param name String
     * @param scope String
     */
    protected NetBIOSName(String name, String scope)
    {
        setName(name.substring(0, NameLength - 1).trim());
        setType(name.charAt(NameLength - 1));

        if (scope != null && scope.length() > 0)
            setNameScope(scope);
    }

    /**
     * Compare objects for equality.
     * 
     * @return boolean
     * @param obj java.lang.Object
     */
    public boolean equals(Object obj)
    {

        // Check if the object is a NetBIOSName type object

        if (obj instanceof NetBIOSName)
        {

            // Check if the NetBIOS name, name type and local/remote flags are equal

            NetBIOSName nbn = (NetBIOSName) obj;
            if (nbn.getName().equals(getName()) && nbn.getType() == getType() && nbn.isLocalName() == isLocalName())
                return true;
        }

        // Objects are not equal

        return false;
    }

    /**
     * Return the system time that the NetBIOS name expires.
     * 
     * @return long
     */
    public final long getExpiryTime()
    {
        return m_expiry;
    }

    /**
     * Get the names time to live value, in seconds
     * 
     * @return int
     */
    public final int getTimeToLive()
    {
        return m_ttl;
    }

    /**
     * Return the number of addresses for this NetBIOS name
     * 
     * @return int
     */
    public final int numberOfAddresses()
    {
        return m_addrList != null ? m_addrList.size() : 0;
    }

    /**
     * Return the specified IP address that owns the NetBIOS name.
     * 
     * @param idx int
     * @return byte[]
     */
    public final byte[] getIPAddress(int idx)
    {
        if (m_addrList == null || idx < 0 || idx >= m_addrList.size())
            return null;
        return m_addrList.get(idx);
    }

    /**
     * Return the specified IP address that owns the NetBIOS name, as a string.
     * 
     * @param idx int
     * @return String
     */
    public final String getIPAddressString(int idx)
    {
        if (m_addrList == null || idx < 0 || idx >= m_addrList.size())
            return null;

        // Get the raw IP address and build the address string

        return IPAddress.asString(m_addrList.get(idx));
    }

    /**
     * Return the NetBIOS name.
     * 
     * @return java.lang.String
     */
    public final String getName()
    {
        return m_name;
    }

    /**
     * Return the NetBIOS name as a 16 character string with the name and type
     * 
     * @return byte[]
     */
    public final byte[] getNetBIOSName()
    {

        // Allocate a buffer to build the full name

        byte[] nameBuf = new byte[NameLength];

        // Get the name string bytes

        byte[] nameBytes = null;

        try
        {
            if (hasNameConversionCharacterSet())
                nameBytes = getName().getBytes(getNameConversionCharacterSet());
            else
                nameBytes = getName().getBytes();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        for (int i = 0; i < nameBytes.length; i++)
            nameBuf[i] = nameBytes[i];
        for (int i = nameBytes.length; i < NameLength; i++)
            nameBuf[i] = ' ';
        nameBuf[NameLength - 1] = (byte) (m_type & 0xFF);

        return nameBuf;
    }

    /**
     * Determine if the name has a name scope
     * 
     * @return boolean
     */
    public final boolean hasNameScope()
    {
        return m_scope != null ? true : false;
    }

    /**
     * Return the name scope
     * 
     * @return String
     */
    public final String getNameScope()
    {
        return m_scope;
    }

    /**
     * Return the NetBIOS name type.
     * 
     * @return char
     */
    public final char getType()
    {
        return m_type;
    }

    /**
     * Return a hash code for this object.
     * 
     * @return int
     */
    public int hashCode()
    {
        return getName().hashCode() + (int) getType();
    }

    /**
     * Returns true if this is a group type NetBIOS name.
     * 
     * @return boolean
     */
    public final boolean isGroupName()
    {
        return m_group;
    }

    /**
     * Determine if this is a local or remote NetBIOS name.
     * 
     * @return boolean
     */
    public final boolean isLocalName()
    {
        return m_local;
    }

    /**
     * Returns true if the NetBIOS name is a unique type name.
     * 
     * @return boolean
     */
    public final boolean isUniqueName()
    {
        return m_group ? false : true;
    }

    /**
     * Remove all TCP/IP addresses from the NetBIOS name
     */
    public final void removeAllAddresses()
    {
        m_addrList.removeAllElements();
    }

    /**
     * Set the system time that this NetBIOS name expires at.
     * 
     * @param expires long
     */
    public final void setExpiryTime(long expires)
    {
        m_expiry = expires;
    }

    /**
     * Set the names time to live, in seconds
     * 
     * @param ttl int
     */
    public final void setTimeToLive(int ttl)
    {
        m_ttl = ttl;
    }

    /**
     * Set/clear the group name flag.
     * 
     * @param flag boolean
     */
    public final void setGroup(boolean flag)
    {
        m_group = flag;
    }

    /**
     * Set the name scope
     * 
     * @param scope String
     */
    public final void setNameScope(String scope)
    {
        if (scope == null)
            m_scope = null;
        else if (scope.length() > 0 && scope.startsWith("."))
            m_scope = scope.substring(1);
        else
            m_scope = scope;
    }

    /**
     * Add an IP address to the list of addresses for this NetBIOS name
     * 
     * @param ipaddr byte[]
     */
    public final void addIPAddress(byte[] ipaddr)
    {
        if (m_addrList == null)
            m_addrList = new Vector<byte[]>();
        m_addrList.add(ipaddr);
    }

    /**
     * Add a list of IP addresses to the list of addresses for this NetBIOS name
     * 
     * @param ipaddr Vector<byte[]>
     */
    public final void addIPAddresses(Vector<byte[]> addrList)
    {
        if (m_addrList == null)
            m_addrList = new Vector<byte[]>();

        // Add the addresses

        for (int i = 0; i < addrList.size(); i++)
        {
            byte[] addr = addrList.get(i);
            m_addrList.add(addr);
        }
    }

    /**
     * Set the local/remote NetBIOS name flag.
     * 
     * @param local boolean
     */
    public final void setLocalName(boolean local)
    {
        m_local = local;
    }

    /**
     * Set the NetBIOS name.
     * 
     * @param name java.lang.String
     */
    public final void setName(String name)
    {

        // Check if the name contains a name scope, if so then split the name and scope id

        int pos = name.indexOf(".");
        if (pos != -1)
        {

            // Split the name and scope id

            setNameScope(name.substring(pos + 1));
            m_name = toUpperCaseName(name.substring(0, pos));
        }
        else
        {

            // Set the name

            m_name = toUpperCaseName(name);
        }
    }

    /**
     * Set the NetBIOS name type.
     * 
     * @param typ char
     */
    public final void setType(char typ)
    {
        m_type = typ;
    }

    /**
     * Convert a name to uppercase
     * 
     * @return String
     */
    public static String toUpperCaseName(String name)
    {

        // Trim the name, unless it looks like a special name

        if (name.length() > 2 && name.charAt(0) != 0x01 && name.charAt(1) != 0x02)
            name = name.trim();

        // Convert the string to uppercase

        if (name != null && name.length() > 0)
        {
            StringBuffer upperName = new StringBuffer(name.length());

            for (int i = 0; i < name.length(); i++)
            {
                char ch = name.charAt(i);
                if (ch >= 'a' && ch <= 'z')
                    upperName.append(Character.toUpperCase(ch));
                else
                    upperName.append(ch);
            }

            // Return the uppercased name

            return upperName.toString();
        }

        // Invalid or empty name

        return "";
    }

    /**
     * Determine if the name conversion character set has been configured
     * 
     * @return boolean
     */
    public final static boolean hasNameConversionCharacterSet()
    {
        return _nameConversionCharset != null ? true : false;
    }

    /**
     * Return the name conversion character set name
     * 
     * @return String
     */
    public final static String getNameConversionCharacterSet()
    {
        return _nameConversionCharset;
    }

    /**
     * Set the name conversion character set
     * 
     * @param charSet String
     */
    public final static void setNameConversionCharacterSet(String charSet)
    {
        _nameConversionCharset = charSet;
    }

    /**
     * Return the NetBIOS name as a string.
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append("[");
        str.append(m_name);

        if (hasNameScope())
        {
            str.append(".");
            str.append(m_scope);
        }

        str.append(":");
        str.append(TypeAsString(m_type));
        str.append(",");
        if (m_group == true)
            str.append("Group,");
        else
            str.append("Unique,");
        if (numberOfAddresses() > 0)
        {
            for (int i = 0; i < numberOfAddresses(); i++)
            {
                str.append(getIPAddressString(i));
                str.append("|");
            }
        }
        str.append("]");
        return str.toString();
    }

    /**
     * Convert a the NetBIOS name into RFC NetBIOS format.
     * 
     * @return byte[]
     */
    public byte[] encodeName()
    {

        // Build the name string with the name type, make sure that the host
        // name is uppercase.

        StringBuffer nbName = new StringBuffer(getName().toUpperCase());

        if (nbName.length() > NameLength - 1)
            nbName.setLength(NameLength - 1);

        // Space pad the name then add the NetBIOS name type

        while (nbName.length() < NameLength - 1)
            nbName.append(' ');
        nbName.append(getType());

        // Allocate the return buffer.
        //
        // Length byte + encoded NetBIOS name length + name scope length + name scope

        int len = 34;
        if (hasNameScope())
            len += getNameScope().length() + 1;

        byte[] encBuf = new byte[len];

        // Convert the NetBIOS name string to the RFC NetBIOS name format

        int pos = 0;
        encBuf[pos++] = (byte) 32;
        int idx = 0;

        while (idx < nbName.length())
        {

            // Get the current character from the host name string

            char ch = nbName.charAt(idx++);

            if (ch == ' ')
            {

                // Append an encoded <SPACE> character

                encBuf[pos++] = (byte) 'C';
                encBuf[pos++] = (byte) 'A';
            }
            else
            {

                // Append octet for the current character

                encBuf[pos++] = (byte) EncodeConversion.charAt((int) ch / 16);
                encBuf[pos++] = (byte) EncodeConversion.charAt((int) ch % 16);
            }
        }

        // Check if there is a NetBIOS name scope to be appended to the encoded name string

        if (hasNameScope())
        {

            // Get the name scope and uppercase

            StringTokenizer tokens = new StringTokenizer(getNameScope(), ".");

            while (tokens.hasMoreTokens())
            {

                // Get the current token

                String token = tokens.nextToken();

                // Append the name to the encoded NetBIOS name

                encBuf[pos++] = (byte) token.length();
                for (int i = 0; i < token.length(); i++)
                    encBuf[pos++] = (byte) token.charAt(i);
            }
        }

        // Terminate the encoded name string with a null section length

        encBuf[pos++] = (byte) 0;

        // Return the encoded NetBIOS name

        return encBuf;
    }

    /**
     * Find the best match address that the NetBIOS name is registered on that matches one of the
     * local TCP/IP addresses
     * 
     * @param addrList InetAddress[]
     * @return int
     */
    public final int findBestMatchAddress(InetAddress[] addrList)
    {

        // Check if the address list is valid

        if (addrList == null || addrList.length == 0 || numberOfAddresses() == 0)
            return -1;

        // If the NetBIOS name only has one address then just return the index

        if (numberOfAddresses() == 1)
            return 0;

        // Search for a matching subnet

        int topCnt = 0;
        int topIdx = -1;

        for (int localIdx = 0; localIdx < addrList.length; localIdx++)
        {

            // Get the address bytes for the current local address

            byte[] localAddr = addrList[localIdx].getAddress();

            // Match against the addresses that the NetBIOS name is registered against

            for (int addrIdx = 0; addrIdx < numberOfAddresses(); addrIdx++)
            {

                // Get the current remote address bytes

                byte[] remoteAddr = (byte[]) m_addrList.elementAt(addrIdx);
                int ipIdx = 0;

                while (ipIdx < 4 && remoteAddr[ipIdx] == localAddr[ipIdx])
                    ipIdx++;

                // Check if the current address is the best match so far

                if (ipIdx > topIdx)
                {

                    // Update the best match address

                    topIdx = addrIdx;
                    topCnt = ipIdx;
                }
            }
        }

        // Return the best match index, or -1 if no match found

        return topIdx;
    }

    /**
     * Decode a NetBIOS name string and create a new NetBIOSName object
     * 
     * @param buf byte[]
     * @param off int
     * @return NetBIOSName
     */
    public static NetBIOSName decodeNetBIOSName(byte[] buf, int off)
    {

        // Convert the RFC NetBIOS name string to a normal NetBIOS name string

        StringBuffer nameBuf = new StringBuffer(16);

        int nameLen = (int) buf[off++];
        int idx = 0;
        char ch1, ch2;

        while (idx < nameLen)
        {

            // Get the current encoded character pair from the encoded name string

            ch1 = (char) buf[off++];
            ch2 = (char) buf[off++];

            if (ch1 == 'C' && ch2 == 'A')
            {

                // Append a <SPACE> character

                nameBuf.append(' ');
            }
            else
            {

                // Convert back to a character code

                int val = EncodeConversion.indexOf(ch1) << 4;
                val += EncodeConversion.indexOf(ch2);

                // Append the current character to the decoded name

                nameBuf.append((char) (val & 0xFF));
            }

            // Update the encoded string index

            idx += 2;

        }

        // Decode the NetBIOS name scope, if specified

        StringBuffer scopeBuf = new StringBuffer(128);
        nameLen = (int) buf[off++];

        while (nameLen > 0)
        {

            // Append a name seperator if not the first name section

            if (scopeBuf.length() > 0)
                scopeBuf.append(".");

            // Copy the name scope section to the scope name buffer

            for (int i = 0; i < nameLen; i++)
                scopeBuf.append((char) buf[off++]);

            // Get the next name section length

            nameLen = (int) buf[off++];
        }

        // Create a NetBIOS name

        return new NetBIOSName(nameBuf.toString(), scopeBuf.toString());
    }

    /**
     * Decode a NetBIOS name string length
     * 
     * @param buf byte[]
     * @param off int
     * @return int
     */
    public static int decodeNetBIOSNameLength(byte[] buf, int off)
    {

        // Calculate the encoded NetBIOS name string length

        int totLen = 1;
        int nameLen = (int) buf[off++];

        while (nameLen > 0)
        {

            // Update the total encoded name length

            totLen += nameLen;
            off += nameLen;

            // Get the next name section length

            nameLen = (int) buf[off++];
            totLen++;
        }

        // Return the encoded NetBIOS name length

        return totLen;
    }

    /**
     * Return the NetBIOS name type as a string.
     * 
     * @param typ char
     * @return String
     */
    public final static String TypeAsString(char typ)
    {

        // Return the NetBIOS name type string

        String nameTyp = "";

        switch (typ)
        {
        case WorkStation:
            nameTyp = "WorkStation";
            break;
        case Messenger:
            nameTyp = "Messenger";
            break;
        case RemoteMessenger:
            nameTyp = "RemoteMessenger";
            break;
        case RASServer:
            nameTyp = "RASServer";
            break;
        case FileServer:
            nameTyp = "FileServer";
            break;
        case RASClientService:
            nameTyp = "RASClientService";
            break;
        case MSExchangeInterchange:
            nameTyp = "MSExchangeInterchange";
            break;
        case MSExchangeStore:
            nameTyp = "MSExchangeStore";
            break;
        case MSExchangeDirectory:
            nameTyp = "MSExchangeDirectory";
            break;
        case LotusNotesServerService:
            nameTyp = "LotusNotesServerService";
            break;
        case ModemSharingService:
            nameTyp = "ModemSharingService";
            break;
        case ModemSharingClient:
            nameTyp = "ModemSharingClient";
            break;
        case McCaffeeAntiVirus:
            nameTyp = "McCaffeeAntiVirus";
            break;
        case SMSClientRemoteControl:
            nameTyp = "SMSClientRemoteControl";
            break;
        case SMSAdminRemoteControl:
            nameTyp = "SMSAdminRemoteControl";
            break;
        case SMSClientRemoteChat:
            nameTyp = "SMSClientRemoteChat";
            break;
        case SMSClientRemoteTransfer:
            nameTyp = "SMSClientRemoteTransfer";
            break;
        case DECPathworksService:
            nameTyp = "DECPathworksService";
            break;
        case MSExchangeIMC:
            nameTyp = "MSExchangeIMC";
            break;
        case MSExchangeMTA:
            nameTyp = "MSExchangeMTA";
            break;
        case NetworkMonitorAgent:
            nameTyp = "NetworkMonitorAgent";
            break;
        case NetworkMonitorApp:
            nameTyp = "NetworkMonitorApp";
            break;
        case DomainMasterBrowser:
            nameTyp = "DomainMasterBrowser";
            break;
        case MasterBrowser:
            nameTyp = "MasterBrowser";
            break;
        case DomainAnnounce:
            nameTyp = "DomainAnnounce";
            break;
        case DomainControllers:
            nameTyp = "DomainControllers";
            break;
        default:
            nameTyp = "0x" + Integer.toHexString((int) typ);
            break;
        }

        return nameTyp;
    }
}