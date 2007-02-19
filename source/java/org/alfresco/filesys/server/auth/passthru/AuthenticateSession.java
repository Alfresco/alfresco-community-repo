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
package org.alfresco.filesys.server.auth.passthru;

import java.io.IOException;

import org.alfresco.filesys.netbios.RFCNetBIOSProtocol;
import org.alfresco.filesys.server.auth.PasswordEncryptor;
import org.alfresco.filesys.server.auth.ntlm.NTLM;
import org.alfresco.filesys.server.auth.ntlm.Type1NTLMMessage;
import org.alfresco.filesys.server.auth.ntlm.Type2NTLMMessage;
import org.alfresco.filesys.server.auth.ntlm.Type3NTLMMessage;
import org.alfresco.filesys.smb.Capability;
import org.alfresco.filesys.smb.Dialect;
import org.alfresco.filesys.smb.NTTime;
import org.alfresco.filesys.smb.NetworkSession;
import org.alfresco.filesys.smb.PCShare;
import org.alfresco.filesys.smb.PacketType;
import org.alfresco.filesys.smb.SMBDate;
import org.alfresco.filesys.smb.SMBException;
import org.alfresco.filesys.smb.SMBStatus;
import org.alfresco.filesys.util.DataPacker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Authenticate Session Class
 * <p>
 * Used for passthru authentication mechanisms.
 */
public class AuthenticateSession
{

    // Debug logging

    private static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol.auth");

    // Default packet size

    private static final int DefaultPacketSize = 1024;

    // Session security mode

    public static final int SecurityModeUser = 1;
    public static final int SecurityModeShare = 2;

    // Tree identifier that indicates that the disk session has been closed

    protected final static int Closed = -1;

    // Default SMB packet size to allocate

    public static final int DEFAULT_BUFSIZE = 4096;

    // SMB dialect id and string for this session

    private int m_dialect;
    private String m_diaStr;

    // Network session

    private NetworkSession m_netSession;

    // SMB packet for protocol exhanges

    protected SMBPacket m_pkt;

    // Default packet flags

    private int m_defFlags = SMBPacket.FLG_CASELESS;
    private int m_defFlags2 = SMBPacket.FLG2_LONGFILENAMES;

    // Server connection details

    private PCShare m_remoteShr;

    // Domain name

    private String m_domain;

    // Remote operating system and LAN manager type

    private String m_srvOS;
    private String m_srvLM;

    // Security mode (user or share)

    private int m_secMode;

    // Challenge encryption key

    private byte[] m_encryptKey;

    // SMB session information

    private int m_sessIdx;
    private int m_userId;
    private int m_processId;

    // Tree identifier for this connection

    protected int m_treeid;

    // Device type that this session is connected to

    private int m_devtype;

    // Maximum transmit buffer size allowed

    private int m_maxPktSize;

    // Session capabilities

    private int m_sessCaps;

    // Maximum virtual circuits allowed on this session, and maximum multiplxed read/writes

    private int m_maxVCs;
    private int m_maxMPX;

    // Indicate if the session was created as a guest rather than using the supplied
    // username/password

    private boolean m_guest;

    // Flag to indicate extended security exchange is being used
    
    private boolean m_extendedSec;
    
    // Server GUID, if using extended security
    
    private byte[] m_serverGUID;
    
    // Type 2 security blob from the server
    
    private Type2NTLMMessage m_type2Msg;
    
    // Global session id

    private static int m_sessionIdx = 1;

    // Multiplex id

    private static int m_multiplexId = 1;

    /**
     * Class constructor
     * 
     * @param shr PCShare
     * @param sess NetworkSession
     * @param dialect int
     * @param pkt SMBPacket
     * @exception IOException If a network error occurs
     * @eception SMBException If a CIFS error occurs
     */
    protected AuthenticateSession(PCShare shr, NetworkSession sess, int dialect, SMBPacket pkt) throws IOException, SMBException
    {

        // Set the SMB dialect for this session

        m_dialect = dialect;

        // Save the remote share details

        m_remoteShr = shr;

        // Allocate a unique session index

        m_sessIdx = getNextSessionId();

        // Allocate an SMB protocol packet

        m_pkt = pkt;
        if (pkt == null)
            m_pkt = new SMBPacket(DEFAULT_BUFSIZE);

        // Save the session and packet

        setSession(sess);

        // Extract the details from the negotiate response packet

        processNegotiateResponse();
    }

    /**
     * Allocate an SMB packet for this session. The preferred packet size is specified, if a smaller
     * buffer size has been negotiated a smaller SMB packet will be returned.
     * 
     * @param pref Preferred SMB packet size
     * @return Allocated SMB packet
     */
    protected final SMBPacket allocatePacket(int pref)
    {

        // Check if the preferred size is larger than the maximum allowed packet
        // size for this session.

        if (pref > m_maxPktSize)
            return new SMBPacket(m_maxPktSize + RFCNetBIOSProtocol.HEADER_LEN);

        // Return the preferred SMB packet size

        return new SMBPacket(pref + RFCNetBIOSProtocol.HEADER_LEN);
    }

    /**
     * Determine if the session supports extended security
     * 
     * @return true if this session supports extended security, else false
     */
    public final boolean supportsExtendedSecurity()
    {
        return (m_sessCaps & Capability.ExtendedSecurity) != 0 ? true : false;
    }
    
    /**
     * Determine if the session supports raw mode read/writes
     * 
     * @return true if this session supports raw mode, else false
     */
    public final boolean supportsRawMode()
    {
        return (m_sessCaps & Capability.RawMode) != 0 ? true : false;
    }

    /**
     * Determine if the session supports Unicode
     * 
     * @return boolean
     */
    public final boolean supportsUnicode()
    {
        return (m_sessCaps & Capability.Unicode) != 0 ? true : false;
    }

    /**
     * Determine if the session supports large files (ie. 64 bit file offsets)
     * 
     * @return boolean
     */
    public final boolean supportsLargeFiles()
    {
        return (m_sessCaps & Capability.LargeFiles) != 0 ? true : false;
    }

    /**
     * Determine if the session supports NT specific SMBs
     * 
     * @return boolean
     */
    public final boolean supportsNTSmbs()
    {
        return (m_sessCaps & Capability.NTSMBs) != 0 ? true : false;
    }

    /**
     * Determine if the session supports RPC API requests
     * 
     * @return boolean
     */
    public final boolean supportsRPCAPIs()
    {
        return (m_sessCaps & Capability.RemoteAPIs) != 0 ? true : false;
    }

    /**
     * Determine if the session supports NT status codes
     * 
     * @return boolean
     */
    public final boolean supportsNTStatusCodes()
    {
        return (m_sessCaps & Capability.NTStatus) != 0 ? true : false;
    }

    /**
     * Determine if the session supports level 2 oplocks
     * 
     * @return boolean
     */
    public final boolean supportsLevel2Oplocks()
    {
        return (m_sessCaps & Capability.Level2Oplocks) != 0 ? true : false;
    }

    /**
     * Determine if the session supports lock and read
     * 
     * @return boolean
     */
    public final boolean supportsLockAndRead()
    {
        return (m_sessCaps & Capability.LockAndRead) != 0 ? true : false;
    }

    /**
     * Determine if the session supports NT find
     * 
     * @return boolean
     */
    public final boolean supportsNTFind()
    {
        return (m_sessCaps & Capability.NTFind) != 0 ? true : false;
    }

    /**
     * Close this connection with the remote server.
     * 
     * @exception java.io.IOException If an I/O error occurs.
     * @exception SMBException If an SMB level error occurs
     */
    public void CloseSession() throws java.io.IOException, SMBException
    {

        // If the session is valid then hangup the session

        if (isActive())
        {

            // Close the network session

            m_netSession.Close();

            // Clear the session

            m_netSession = null;
        }
    }

    /**
     * Return the default flags settings for this session
     * 
     * @return int
     */
    public final int getDefaultFlags()
    {
        return m_defFlags;
    }

    /**
     * Return the default flags2 settings for this session
     * 
     * @return int
     */
    public final int getDefaultFlags2()
    {
        return m_defFlags2;
    }

    /**
     * Get the device type that this session is connected to.
     * 
     * @return Device type for this session.
     */
    public final int getDeviceType()
    {
        return m_devtype;
    }

    /**
     * Get the SMB dialect property
     * 
     * @return SMB dialect that this session has negotiated.
     */
    public final int getDialect()
    {
        return m_dialect;
    }

    /**
     * Get the SMB dialect string
     * 
     * @return SMB dialect string for this session.
     */
    public final String getDialectString()
    {
        return m_diaStr;
    }

    /**
     * Get the servers primary domain name
     * 
     * @return Servers primary domain name, if knwon, else null.
     */
    public final String getDomain()
    {
        return m_domain;
    }

    /**
     * Determine if there is a challenge encryption key
     * 
     * @return boolean
     */
    public final boolean hasEncryptionKey()
    {
        return m_encryptKey != null ? true : false;
    }

    /**
     * Return the cahllenge encryption key
     * 
     * @return byte[]
     */
    public final byte[] getEncryptionKey()
    {
        return m_encryptKey;
    }

    /**
     * Get the servers LAN manager type
     * 
     * @return Servers LAN manager type, if known, else null.
     */
    public final String getLANManagerType()
    {
        return m_srvLM;
    }

    /**
     * Get the maximum number of multiplxed requests that are allowed
     * 
     * @return int
     */
    public final int getMaximumMultiplexedRequests()
    {
        return m_maxMPX;
    }

    /**
     * Get the maximum packet size allowed for this session
     * 
     * @return Maximum packet size, in bytes.
     */
    public final int getMaximumPacketSize()
    {
        return m_maxPktSize;
    }

    /**
     * Get the maximum virtual circuits allowed on this session
     * 
     * @return int
     */
    public final int getMaximumVirtualCircuits()
    {
        return m_maxVCs;
    }

    /**
     * Get the next multiplex id to uniquely identify a transaction
     * 
     * @return Unique multiplex id for a transaction
     */
    public final synchronized int getNextMultiplexId()
    {
        return m_multiplexId++;
    }

    /**
     * Get the next session id
     * 
     * @return int
     */
    protected final synchronized int getNextSessionId()
    {
        return m_sessionIdx++;
    }

    /**
     * Get the servers operating system type
     * 
     * @return Servers operating system, if known, else null.
     */
    public final String getOperatingSystem()
    {
        return m_srvOS;
    }

    /**
     * Get the remote share password string
     * 
     * @return Remote share password string
     */
    public final String getPassword()
    {
        return m_remoteShr.getPassword();
    }

    /**
     * Get the remote share details for this session
     * 
     * @return PCShare information for this session
     */
    public final PCShare getPCShare()
    {
        return m_remoteShr;
    }

    /**
     * Return the security mode of the session (user or share)
     * 
     * @return int
     */
    public final int getSecurityMode()
    {
        return m_secMode;
    }

    /**
     * Get the remote server name
     * 
     * @return Remote server name
     */
    public final String getServer()
    {
        return m_remoteShr.getNodeName();
    }

    /**
     * Access the associated network session
     * 
     * @return NetworkSession that the SMB session is using
     */
    public final NetworkSession getSession()
    {
        return m_netSession;
    }

    /**
     * Determine if the session has an associated type2 NTLM security blob
     * 
     * @return boolean
     */
    public final boolean hasType2NTLMMessage()
    {
        return m_type2Msg != null ? true : false;
    }
    
    /**
     * Return the type2 NTLM security blob that was received from the authentication server
     * 
     * @return Type2NTLMMessage 
     */
    public final Type2NTLMMessage getType2NTLMMessage()
    {
        return m_type2Msg;
    }
    
    /**
     * Return the session capability flags.
     * 
     * @return int
     */
    public final int getCapabilities()
    {
        return m_sessCaps;
    }

    /**
     * Get the process id for this session
     * 
     * @return int
     */
    public final int getProcessId()
    {
        return m_processId;
    }

    /**
     * Get the session identifier property
     * 
     * @return Session identifier
     */
    public final int getSessionId()
    {
        return m_sessIdx;
    }

    /**
     * Get the remote share name
     * 
     * @return Remote share name string
     */
    public final String getShareName()
    {
        return m_remoteShr.getShareName();
    }

    /**
     * Get the connected tree identifier.
     * 
     * @return Tree identifier.
     */
    public final int getTreeId()
    {
        return m_treeid;
    }

    /**
     * Return the assigned use id for this SMB session
     * 
     * @return Assigned user id
     */
    public final int getUserId()
    {
        return m_userId;
    }

    /**
     * Get the remote share user name string
     * 
     * @return Remote share user name string
     */
    public final String getUserName()
    {
        return m_remoteShr.getUserName();
    }

    /**
     * Check if there is data available in the network receive buffer
     * 
     * @return boolean
     * @exception IOException
     */
    public final boolean hasDataAvailable() throws IOException
    {
        return m_netSession.hasData();
    }

    /**
     * Determine if the session is valid, ie. still open.
     * 
     * @return true if the session is still active, else false.
     */
    public final boolean isActive()
    {
        return (m_netSession == null) ? false : true;
    }

    /**
     * Determine if the session has been created as a guest logon
     * 
     * @return boolean
     */
    public final boolean isGuest()
    {
        return m_guest;
    }

    /**
     * Determine if the Unicode flag is enabled
     * 
     * @return boolean
     */
    public final boolean isUnicode()
    {
        return (m_defFlags2 & SMBPacket.FLG2_UNICODE) != 0 ? true : false;
    }

    /**
     * Determine if extended security exchanges are being used
     * 
     * @return boolean
     */
    public final boolean isUsingExtendedSecurity()
    {
        return m_extendedSec;
    }
    
    /**
     * Send a single echo request to the server
     * 
     * @throws java.io.IOException
     * @throws SMBException
     */
    public final void pingServer() throws java.io.IOException, SMBException
    {

        // Send a single echo request to the server

        pingServer(1);
    }

    /**
     * Send an echo request to the server
     * 
     * @param cnt Number of packets to echo from the remote server
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBException SMB error occurred
     */
    public final void pingServer(int cnt) throws java.io.IOException, SMBException
    {

        // Build a server ping SMB packet

        m_pkt.setCommand(PacketType.Echo);
        m_pkt.setFlags(0);
        m_pkt.setTreeId(getTreeId());
        m_pkt.setUserId(getUserId());
        m_pkt.setProcessId(getProcessId());
        m_pkt.setMultiplexId(1);

        // Set the parameter words

        m_pkt.setParameterCount(1);
        m_pkt.setParameter(0, cnt); // number of packets that the server should return
        String echoStr = "ECHO";
        m_pkt.setBytes(echoStr.getBytes());

        // Send the echo request

        m_pkt.SendSMB(this);

        // Receive the reply packets, if any

        while (cnt > 0)
        {

            // Receive a reply packet

            m_pkt.ReceiveSMB(this);

            // Decrement the reply counter

            cnt--;
        }
    }

    /**
     * Set the default SMB packet flags for this session
     * 
     * @param flg int
     */
    protected final void setDefaultFlags(int flg)
    {
        m_defFlags = flg;
    }

    /**
     * Set the SMB packet default flags2 for this session
     * 
     * @param flg2 int
     */
    protected final void setDefaultFlags2(int flg2)
    {
        m_defFlags2 = flg2;
    }

    /**
     * Set the device type for this session.
     * 
     * @param dev Device type for this session.
     */
    protected final void setDeviceType(int dev)
    {
        m_devtype = dev;
    }

    /**
     * Set the dialect for this session
     * 
     * @param dia SMB dialect that this session is using.
     */
    protected final void setDialect(int dia)
    {
        m_dialect = dia;
    }

    /**
     * Set the dialect string for this session
     * 
     * @param dia SMB dialect string
     */
    protected final void setDialectString(String dia)
    {
        m_diaStr = dia;
    }

    /**
     * Set the remote servers primary domain name
     * 
     * @param dom Servers primary domain name.
     */
    protected final void setDomain(String dom)
    {
        m_domain = dom;
    }

    /**
     * Set the encryption key
     * 
     * @param key byte[]
     */
    public final void setEncryptionKey(byte[] key)
    {

        // Set the challenge response encryption key

        m_encryptKey = key;
    }

    /**
     * Set the guest status for the session
     * 
     * @param sts boolean
     */
    protected final void setGuest(boolean sts)
    {
        m_guest = sts;
    }

    /**
     * Set the remote servers LAN manager type
     * 
     * @param lm Servers LAN manager type string.
     */
    protected final void setLANManagerType(String lm)
    {
        m_srvLM = lm;
    }

    /**
     * Set the maximum number of multiplexed requests allowed
     * 
     * @param maxMulti int
     */
    protected final void setMaximumMultiplexedRequests(int maxMulti)
    {
        m_maxMPX = maxMulti;
    }

    /**
     * Set the maximum packet size allowed on this session
     * 
     * @param siz Maximum allowed packet size.
     */
    protected final void setMaximumPacketSize(int siz)
    {
        m_maxPktSize = siz;
    }

    /**
     * Set the maximum number of virtual circuits allowed on this session
     * 
     * @param maxVC int
     */
    protected final void setMaximumVirtualCircuits(int maxVC)
    {
        m_maxVCs = maxVC;
    }

    /**
     * Set the remote servers operating system type
     * 
     * @param os Servers operating system type string.
     */
    protected final void setOperatingSystem(String os)
    {
        m_srvOS = os;
    }

    /**
     * Set the remote share password
     * 
     * @param pwd Remtoe share password string.
     */
    protected final void setPassword(String pwd)
    {
        m_remoteShr.setPassword(pwd);
    }

    /**
     * Set the session security mode (user or share)
     * 
     * @param secMode int
     */
    public final void setSecurityMode(int secMode)
    {
        m_secMode = secMode;
    }

    /**
     * Set the remote server name
     * 
     * @param srv Server name string
     */
    protected final void setServer(String srv)
    {
        m_remoteShr.setNodeName(srv);
    }

    /**
     * Set the network session that this SMB session is associated with
     * 
     * @param netSess Network session that this SMB session is to be associated with.
     */
    protected final void setSession(NetworkSession netSess)
    {
        m_netSession = netSess;
    }

    /**
     * Set the session capability flags
     * 
     * @param flg Capability flags.
     */
    protected final void setCapabilities(int caps)
    {
        m_sessCaps = caps;
    }

    /**
     * Set the remote share name
     * 
     * @param shr Remote share name string
     */
    protected final void setShareName(String shr)
    {
        m_remoteShr.setShareName(shr);
    }

    /**
     * Set the process id for this session
     * 
     * @param id
     */
    protected final void setProcessId(int id)
    {
        m_processId = id;
    }

    /**
     * Set the connected tree identifier for this session.
     * 
     * @param id Tree identifier for this session.
     */
    protected final void setTreeId(int id)
    {
        m_treeid = id;
    }

    /**
     * Set the user identifier for this session
     * 
     * @param uid User identifier
     */
    protected final void setUserId(int uid)
    {
        m_userId = uid;
    }

    /**
     * Set the remote share user name
     * 
     * @param user Remote share user name string
     */
    protected final void setUserName(String user)
    {
        m_remoteShr.setUserName(user);
    }

    /**
     * Output the session details as a string
     * 
     * @return Session details string
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("[\\\\");
        str.append(getServer());
        str.append("\\");
        str.append(getShareName());
        str.append(":");
        str.append(Dialect.DialectTypeString(m_dialect));
        str.append(",UserId=");
        str.append(getUserId());
        str.append("]");

        return str.toString();
    }

    /**
     * Perform a session setup to create a session on the remote server validating the user.
     * 
     * @param userName String
     * @param ascPwd ASCII password hash
     * @param uniPwd Unicode password hash
     * @exception IOException If a network error occurs
     * @exception SMBException If a CIFS error occurs
     */
    public final void doSessionSetup(String userName, byte[] ascPwd, byte[] uniPwd) throws IOException, SMBException
    {
        doSessionSetup(null, userName, null, ascPwd, uniPwd);
    }
    
    /**
     * Perform a session using the type3 NTLM response received from the client
     * 
     * @param type3 Type3NTLMMessage
     * @exception IOException If a network error occurs
     * @exception SMBException If a CIFS error occurs
     */
    public final void doSessionSetup(Type3NTLMMessage type3Msg) throws IOException, SMBException
    {
        doSessionSetup(type3Msg.getDomain(), type3Msg.getUserName(), type3Msg.getWorkstation(),
                type3Msg.getLMHash(), type3Msg.getNTLMHash());
    }
    
    /**
     * Perform a session setup to create a session on the remote server validating the user.
     * 
     * @param domain String
     * @param userName String
     * @param wksName String
     * @param ascPwd ASCII password hash
     * @param uniPwd Unicode password hash
     * @exception IOException If a network error occurs
     * @exception SMBException If a CIFS error occurs
     */
    public final void doSessionSetup(String domain, String userName, String wksName,
            byte[] ascPwd, byte[] uniPwd) throws IOException, SMBException
    {
        // Check if we are using extended security
        
        if ( isUsingExtendedSecurity())
        {
            // Run the second phase of the extended security session setup
        
            doExtendedSessionSetupPhase2(domain, userName, wksName, ascPwd, uniPwd);
            return;
        }
        
        // Create a session setup packet

        SMBPacket pkt = new SMBPacket();

        pkt.setCommand(PacketType.SessionSetupAndX);

        // Check if the negotiated SMB dialect is NT LM 1.2 or an earlier dialect

        if (getDialect() == Dialect.NT)
        {

            // NT LM 1.2 SMB dialect

            pkt.setParameterCount(13);
            pkt.setAndXCommand(0xFF); // no secondary command
            pkt.setParameter(1, 0); // offset to next command
            pkt.setParameter(2, DefaultPacketSize);
            pkt.setParameter(3, 1);
            pkt.setParameter(4, 0); // virtual circuit number
            pkt.setParameterLong(5, 0); // session key

            // Set the share password length(s)

            pkt.setParameter(7, ascPwd != null ? ascPwd.length : 0); // ANSI password length
            pkt.setParameter(8, uniPwd != null ? uniPwd.length : 0); // Unicode password length

            pkt.setParameter(9, 0); // reserved, must be zero
            pkt.setParameter(10, 0); // reserved, must be zero

            // Send the client capabilities

            int caps = Capability.LargeFiles + Capability.Unicode + Capability.NTSMBs + Capability.NTStatus
                    + Capability.RemoteAPIs;
            
            // Set the client capabilities
            
            pkt.setParameterLong(11, caps);

            // Get the offset to the session setup request byte data
            
            int pos = pkt.getByteOffset();
            pkt.setPosition(pos);

            // Store the ASCII password hash, if specified

            if (ascPwd != null)
                pkt.packBytes(ascPwd, ascPwd.length);

            // Store the Unicode password hash, if specified

            if (uniPwd != null)
                pkt.packBytes(uniPwd, uniPwd.length);

            // Pack the account/client details

            pkt.packString(userName, false);

            // Check if the share has a domain, if not then use the default domain string

            if (getPCShare().hasDomain())
                pkt.packString(getPCShare().getDomain(), false);
            else
                pkt.packString("?", false);

            pkt.packString("Java VM", false);
            pkt.packString("JLAN", false);
            
            // Set the packet length

            pkt.setByteCount(pkt.getPosition() - pos);
        }
        else
        {

            // Earlier SMB dialect

            pkt.setUserId(1);

            pkt.setParameterCount(10);
            pkt.setAndXCommand(0xFF);
            pkt.setParameter(1, 0);
            pkt.setParameter(2, DefaultPacketSize);
            pkt.setParameter(3, 1);
            pkt.setParameter(4, 0);
            pkt.setParameter(5, 0);
            pkt.setParameter(6, 0);
            pkt.setParameter(7, ascPwd != null ? ascPwd.length : 0);
            pkt.setParameter(8, 0);
            pkt.setParameter(9, 0);

            // Put the password into the SMB packet

            byte[] buf = pkt.getBuffer();
            int pos = pkt.getByteOffset();

            if (ascPwd != null)
            {
                for (int i = 0; i < ascPwd.length; i++)
                    buf[pos++] = ascPwd[i];
            }

            // Build the account/client details

            StringBuffer clbuf = new StringBuffer();

            clbuf.append(getPCShare().getUserName());
            clbuf.append((char) 0x00);

            // Check if the share has a domain, if not then use the unknown domain string

            if (getPCShare().hasDomain())
                clbuf.append(getPCShare().getDomain());
            else
                clbuf.append("?");
            clbuf.append((char) 0x00);

            clbuf.append("Java VM");
            clbuf.append((char) 0x00);

            clbuf.append("JLAN");
            clbuf.append((char) 0x00);

            // Copy the remaining data to the SMB packet

            byte[] byts = clbuf.toString().getBytes();
            for (int i = 0; i < byts.length; i++)
                buf[pos++] = byts[i];

            int pwdLen = ascPwd != null ? ascPwd.length : 0;
            pkt.setByteCount(pwdLen + byts.length);
        }

        // Exchange an SMB session setup packet with the remote file server

        pkt.ExchangeSMB(this, pkt, true);

        // Save the session user id

        setUserId(pkt.getUserId());

        // Check if the session was created as a guest

        if (pkt.getParameterCount() >= 3)
        {

            // Set the guest status for the session

            setGuest(pkt.getParameter(2) != 0 ? true : false);
        }

        // The response packet should also have the server OS, LAN Manager type
        // and primary domain name.

        if (pkt.getByteCount() > 0)
        {

            // Get the packet buffer and byte offset

            byte[] buf = pkt.getBuffer();
            int offset = pkt.getByteOffset();
            int maxlen = offset + pkt.getByteCount();

            // Get the server OS

            String srvOS = DataPacker.getString(buf, offset, maxlen);
            setOperatingSystem(srvOS);

            offset += srvOS.length() + 1;
            maxlen -= srvOS.length() + 1;

            // Get the LAN Manager type

            String lanman = DataPacker.getString(buf, offset, maxlen);
            setLANManagerType(lanman);

            // Check if we have the primary domain for this session

            if (getDomain() == null || getDomain().length() == 0)
            {

                // Get the domain name string

                offset += lanman.length() + 1;
                maxlen += lanman.length() + 1;

                String dom = DataPacker.getString(buf, offset, maxlen);
                setDomain(dom);
            }
        }

        // Check for a core protocol session, set the maximum packet size

        if (getDialect() == Dialect.Core || getDialect() == Dialect.CorePlus)
        {

            // Set the maximum packet size to be used on this session

            setMaximumPacketSize(pkt.getParameter(2));
        }
    }

    /**
     * Process the negotiate response SMB packet
     * 
     * @exception IOException If a network error occurs
     * @eception SMBException If a CIFS error occurs
     */
    private void processNegotiateResponse() throws IOException, SMBException
    {

        // Set the security mode flags

        int keyLen = 0;
        boolean unicodeStr = false;
        int encAlgorithm = PasswordEncryptor.LANMAN;
        int defFlags2 = 0;

        if (getDialect() == Dialect.NT)
        {

            // Read the returned negotiate parameters, for NT dialect the parameters are not aligned

            m_pkt.resetParameterPointer();
            m_pkt.skipBytes(2); // skip the dialect index

            setSecurityMode(m_pkt.unpackByte());

            // Set the maximum virtual circuits and multiplxed requests allowed by the server

            setMaximumMultiplexedRequests(m_pkt.unpackWord());
            setMaximumVirtualCircuits(m_pkt.unpackWord());

            // Set the maximum buffer size

            setMaximumPacketSize(m_pkt.unpackInt());

            // Skip the maximum raw buffer size and session key

            m_pkt.skipBytes(8);

            // Set the server capabailities

            setCapabilities(m_pkt.unpackInt());
            
            // Check if extended security is enabled
            
            if ( supportsExtendedSecurity())
                m_extendedSec = true;

            // Get the server system time and timezone

            SMBDate srvTime = NTTime.toSMBDate(m_pkt.unpackLong());
            int tzone = m_pkt.unpackWord();

            // Get the encryption key length

            keyLen = m_pkt.unpackByte();

            // Indicate that strings are UniCode

            unicodeStr = true;

            // Use NTLMv1 password encryption

            encAlgorithm = PasswordEncryptor.NTLM1;

            // Set the default flags for subsequent SMB requests

            defFlags2 = SMBPacket.FLG2_LONGFILENAMES + SMBPacket.FLG2_UNICODE + SMBPacket.FLG2_LONGERRORCODE;
            
            if ( isUsingExtendedSecurity())
                defFlags2 += SMBPacket.FLG2_EXTENDEDSECURITY;
        }
        else if (getDialect() > Dialect.CorePlus)
        {

            // Set the security mode and encrypted password mode

            int secMode = m_pkt.getParameter(1);
            setSecurityMode((secMode & 0x01) != 0 ? SecurityModeUser : SecurityModeShare);

            if (m_pkt.getParameterCount() >= 11)
                keyLen = m_pkt.getParameter(11) & 0xFF; // should always be 8

            // Set the maximum virtual circuits and multiplxed requests allowed by the server

            setMaximumMultiplexedRequests(m_pkt.getParameter(3));
            setMaximumVirtualCircuits(m_pkt.getParameter(4));

            // Check if Unicode strings are being used

            if (m_pkt.isUnicode())
                unicodeStr = true;

            // Set the default flags for subsequent SMB requests

            defFlags2 = SMBPacket.FLG2_LONGFILENAMES;
        }

        // Set the default packet flags for this session

        setDefaultFlags2(defFlags2);

        // Get the server details from the negotiate SMB packet

        if (m_pkt.getByteCount() > 0)
        {

            // Get the returned byte area length and offset
            
            int bytsiz = m_pkt.getByteCount();
            int bytpos = m_pkt.getByteOffset();
            byte[] buf = m_pkt.getBuffer();

            // Original format response
            
            if ( isUsingExtendedSecurity() == false)
            {
                // Extract the challenge response key, if specified
    
                if (keyLen > 0)
                {
    
                    // Allocate a buffer for the challenge response key
    
                    byte[] encryptKey = new byte[keyLen];
    
                    // Copy the challenge response key
    
                    for (int keyIdx = 0; keyIdx < keyLen; keyIdx++)
                        encryptKey[keyIdx] = buf[bytpos++];
    
                    // Set the sessions encryption key
    
                    setEncryptionKey(encryptKey);
                }
    
                // Extract the domain name
    
                String dom;
    
                if (unicodeStr == false)
                    dom = DataPacker.getString(buf, bytpos, bytsiz);
                else
                    dom = DataPacker.getUnicodeString(buf, bytpos, bytsiz / 2);
                setDomain(dom);
            }
            else
            {
                // Extract the server GUID
                
                m_serverGUID = new byte[16];
                System.arraycopy(buf, bytpos, m_serverGUID, 0, 16);
                
                // Run the first phase of the extended security session setup to get the challenge
                // from the server
                
                doExtendedSessionSetupPhase1();
            }
        }
    }
    
    /**
     * Send the first stage of the extended security session setup
     * 
     * @exception IOException If a network error occurs
     * @eception SMBException If a CIFS error occurs
     */
    private final void doExtendedSessionSetupPhase1() throws IOException, SMBException
    {
        // Create a session setup packet

        SMBPacket pkt = new SMBPacket();

        pkt.setCommand(PacketType.SessionSetupAndX);
        
        pkt.setFlags(getDefaultFlags());
        pkt.setFlags2(getDefaultFlags2());

        // Build the extended session setup phase 1 request
        
        pkt.setParameterCount(12);
        pkt.setAndXCommand(0xFF);   // no secondary command
        pkt.setParameter(1, 0);     // offset to next command
        pkt.setParameter(2, DefaultPacketSize);
        pkt.setParameter(3, 1);
        pkt.setParameter(4, 0);     // virtual circuit number
        pkt.setParameterLong(5, 0); // session key

        // Clear the security blob length and reserved area

        pkt.setParameter(7, 0);     // security blob length
        pkt.setParameterLong(8, 0); // reserved

        // Send the client capabilities

        int caps = Capability.LargeFiles + Capability.Unicode + Capability.NTSMBs + Capability.NTStatus
                + Capability.RemoteAPIs + Capability.ExtendedSecurity;
        
        // Set the client capabilities
        
        pkt.setParameterLong(10, caps);

        // Get the offset to the session setup request byte data
        
        int pos = pkt.getByteOffset();
        pkt.setPosition(pos);

        // Create a type 1 NTLM message using the session setup request buffer
        
        Type1NTLMMessage type1Msg = new Type1NTLMMessage(pkt.getBuffer(), pos, 0);

        int type1Flags = getPCShare().getExtendedSecurityFlags();
        if ( type1Flags == 0)
            type1Flags = NTLM.FlagNegotiateUnicode + NTLM.FlagNegotiateNTLM + NTLM.FlagRequestTarget;
        
        type1Msg.buildType1(type1Flags, null, null);
        
        // Update the request buffer position
        
        pkt.setPosition(pos + type1Msg.getLength());

        // Set the security blob length
        
        pkt.setParameter(7, type1Msg.getLength());
        
        // Pack the OS details
        
        pkt.packString("Java VM", true);
        pkt.packString("JLAN", true);

        pkt.packString("", true);
            
        // Set the packet length

        pkt.setByteCount(pkt.getPosition() - pos);

        // Exchange an SMB session setup packet with the remote file server

        pkt.ExchangeSMB(this, pkt, false);

        // Check the error status, should be a warning status to indicate more processing required
        
        if ( pkt.isLongErrorCode() == false || pkt.getLongErrorCode() != SMBStatus.NTMoreProcessingRequired)
            pkt.checkForError();
        
        // Save the session user id

        setUserId(pkt.getUserId());

        // The response packet should also have the type 2 security blob

        int type2Len = pkt.getParameter(3);
        if (pkt.getByteCount() > 0)
        {

            // Get the packet buffer and byte offset

            byte[] buf = pkt.getBuffer();
            int offset = pkt.getByteOffset();
            int maxlen = offset + pkt.getByteCount();

            // Take a copy of the type 2 security blob
            
            m_type2Msg = new Type2NTLMMessage();
            m_type2Msg.copyFrom(buf, offset, type2Len);
            
            // Get the encryption key from the security blob
            
            m_encryptKey = m_type2Msg.getChallenge();
            
            // Update the byte area offset and align
            
            offset = DataPacker.wordAlign(offset + type2Len);
            maxlen -= type2Len;
            
            // Get the server OS

            String srvOS = DataPacker.getString(buf, offset, maxlen);
            setOperatingSystem(srvOS);

            offset += srvOS.length() + 1;
            maxlen -= srvOS.length() + 1;

            // Get the LAN Manager type

            String lanman = DataPacker.getString(buf, offset, maxlen);
            setLANManagerType(lanman);
        }
    }
    
   /**
    * Send the second stage of the extended security session setup
    * 
    * @param domain String
    * @param userName String
    * @param wksName String
    * @param lmPwd byte[]
    * @param ntlmPwd byte[]
    * @exception IOException If a network error occurs
    * @eception SMBException If a CIFS error occurs
    */
   private final void doExtendedSessionSetupPhase2(String domain, String userName, String wksName,
           byte[] lmPwd, byte[] ntlmPwd) throws IOException, SMBException
   {
       // Check if the domain name has been specified, if not then use the domain name from the
       // original connection details or the servers domain name
       
       if ( domain == null)
       {
           if ( getPCShare().hasDomain() && getPCShare().getDomain().length() > 0)
               domain = getPCShare().getDomain();
           else
               domain = m_type2Msg.getTarget();
       }
       
       // Create a session setup packet

       SMBPacket pkt = new SMBPacket();

       pkt.setCommand(PacketType.SessionSetupAndX);
       
       pkt.setFlags(getDefaultFlags());
       pkt.setFlags2(getDefaultFlags2());
       
       pkt.setUserId(getUserId());

       // Build the extended session setup phase 2 request
       
       pkt.setParameterCount(12);
       pkt.setAndXCommand(0xFF);   // no secondary command
       pkt.setParameter(1, 0);     // offset to next command
       pkt.setParameter(2, DefaultPacketSize);
       pkt.setParameter(3, 1);
       pkt.setParameter(4, 0);     // virtual circuit number
       pkt.setParameterLong(5, 0); // session key

       // Clear the security blob length and reserved area

       pkt.setParameter(7, 0);     // security blob length
       pkt.setParameterLong(8, 0); // reserved

       // Send the client capabilities

       int caps = Capability.LargeFiles + Capability.Unicode + Capability.NTSMBs + Capability.NTStatus
               + Capability.RemoteAPIs + Capability.ExtendedSecurity;
       
       // Set the client capabilities
       
       pkt.setParameterLong(10, caps);

       // Get the offset to the session setup request byte data
       
       int pos = pkt.getByteOffset();
       pkt.setPosition(pos);

       // Create a type 3 NTLM message using the session setup request buffer
       
       Type3NTLMMessage type3Msg = new Type3NTLMMessage(pkt.getBuffer(), pos, 0, true);
       
       type3Msg.buildType3(lmPwd, ntlmPwd, domain, userName, wksName != null ? wksName : "", null, m_type2Msg.getFlags());
       
       // Update the request buffer position
       
       pkt.setPosition(pos + type3Msg.getLength());

       // Set the security blob length
       
       pkt.setParameter(7, type3Msg.getLength());
       
       // Pack the OS details
       
       pkt.packString("Java VM", true);
       pkt.packString("JLAN", true);

       pkt.packString("", true);
           
       // Set the packet length

       pkt.setByteCount(pkt.getPosition() - pos);

       // Exchange an SMB session setup packet with the remote file server

       pkt.ExchangeSMB(this, pkt, true);
       
       // Set the guest status for the session

       setGuest(pkt.getParameter(2) != 0 ? true : false);
   }
}
