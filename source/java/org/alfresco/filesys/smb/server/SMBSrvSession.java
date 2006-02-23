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
package org.alfresco.filesys.smb.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.alfresco.filesys.netbios.NetBIOSException;
import org.alfresco.filesys.netbios.NetBIOSName;
import org.alfresco.filesys.netbios.NetBIOSPacket;
import org.alfresco.filesys.netbios.NetBIOSSession;
import org.alfresco.filesys.netbios.RFCNetBIOSProtocol;
import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.auth.SrvAuthenticator;
import org.alfresco.filesys.server.core.DeviceInterface;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.server.filesys.DiskDeviceContext;
import org.alfresco.filesys.server.filesys.DiskInterface;
import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.server.filesys.SearchContext;
import org.alfresco.filesys.server.filesys.TooManyConnectionsException;
import org.alfresco.filesys.server.filesys.TreeConnection;
import org.alfresco.filesys.smb.Capability;
import org.alfresco.filesys.smb.DataType;
import org.alfresco.filesys.smb.Dialect;
import org.alfresco.filesys.smb.DialectSelector;
import org.alfresco.filesys.smb.NTTime;
import org.alfresco.filesys.smb.PacketType;
import org.alfresco.filesys.smb.SMBDate;
import org.alfresco.filesys.smb.SMBErrorText;
import org.alfresco.filesys.smb.SMBStatus;
import org.alfresco.filesys.smb.server.notify.NotifyRequest;
import org.alfresco.filesys.smb.server.notify.NotifyRequestList;
import org.alfresco.filesys.util.DataPacker;
import org.alfresco.filesys.util.StringList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SMB Session Class
 * 
 * <p>
 * The SMB server creates a server session object for each incoming session request.
 * <p>
 * The server session holds the context of a particular session, including the list of open files
 * and active searches.
 */
public class SMBSrvSession extends SrvSession implements Runnable
{
    // Debug logging
    private static Log logger = LogFactory.getLog("org.alfresco.smb.protocol");

    // Define the default receive buffer size to allocate.

    private static final int DefaultBufferSize = 0x010000 + RFCNetBIOSProtocol.HEADER_LEN;
    private static final int LanManBufferSize = 8192;

    // Default and maximum number of connection slots

    private static final int DefaultConnections = 4;
    private static final int MaxConnections = 16;

    // Tree ids are 16bit values

    private static final int TreeIdMask = 0x0000FFFF;

    // Default and maximum number of search slots

    private static final int DefaultSearches = 8;
    private static final int MaxSearches = 256;

    // Maximum multiplexed packets allowed (client can send up to this many SMBs before waiting for
    // a response)
    //
    // Setting NTMaxMultiplexed to one will disable asynchronous notifications on the client

    private static final int LanManMaxMultiplexed = 1;
    private static final int NTMaxMultiplexed = 4;

    // Maximum number of virtual circuits

    private static final int MaxVirtualCircuits = 0;

    // Packet handler used to send/receive SMB packets over a particular protocol

    private PacketHandler m_pktHandler;

    // Packet buffer for received data and received data length.

    private byte[] m_buf;
    private int m_rxlen;

    // SMB packet used for response

    private SMBSrvPacket m_smbPkt;

    // Protocol handler for this session, depends upon the negotiated SMB dialect

    private ProtocolHandler m_handler;

    // SMB session state.

    private int m_state = SMBSrvSessionState.NBSESSREQ;

    // SMB dialect that this session has negotiated to use.

    private int m_dialect = Dialect.Unknown;

    // Callers NetBIOS name and target name

    private String m_callerNBName;
    private String m_targetNBName;

    // Connected share list and next tree id

    private Hashtable<Integer, TreeConnection> m_connections;
    private int m_treeId;

    // Active search list for this session

    private SearchContext[] m_search;
    private int m_searchCount;

    // Active transaction details

    private SrvTransactBuffer m_transact;

    // Notify change requests and notifications pending flag

    private NotifyRequestList m_notifyList;
    private boolean m_notifyPending;

    // Default SMB/CIFS flags anf flags2, ORed with the SMB packet flags/flags2 before sending a
    // response to the client.

    private int m_defFlags;
    private int m_defFlags2;

    // Asynchrnous response packet queue
    //
    // Contains SMB response packets that could not be sent due to SMB requests being processed. The
    // asynchronous responses must be sent after any pending requests have been processed as the client may
    // disconnect the session.

    private Vector<SMBSrvPacket> m_asynchQueue;

    // Maximum client buffer size and multiplex count

    private int m_maxBufSize;
    private int m_maxMultiplex;

    // Client capabilities

    private int m_clientCaps;

    // Debug flag values

    public static final int DBG_NETBIOS =   0x00000001; // NetBIOS layer
    public static final int DBG_STATE =     0x00000002; // Session state changes
    public static final int DBG_NEGOTIATE = 0x00000004; // Protocol negotiate phase
    public static final int DBG_TREE =      0x00000008; // Tree connection/disconnection
    public static final int DBG_SEARCH =    0x00000010; // File/directory search
    public static final int DBG_INFO =      0x00000020; // Information requests
    public static final int DBG_FILE =      0x00000040; // File open/close/info
    public static final int DBG_FILEIO =    0x00000080; // File read/write
    public static final int DBG_TRAN =      0x00000100; // Transactions
    public static final int DBG_ECHO =      0x00000200; // Echo requests
    public static final int DBG_ERROR =     0x00000400; // Errors
    public static final int DBG_IPC =       0x00000800; // IPC$ requests
    public static final int DBG_LOCK =      0x00001000; // Lock/unlock requests
    public static final int DBG_PKTTYPE =   0x00002000; // Received packet type
    public static final int DBG_DCERPC =    0x00004000; // DCE/RPC
    public static final int DBG_STATECACHE = 0x00008000; // File state cache
    public static final int DBG_NOTIFY = 0x00010000; // Asynchronous change notification
    public static final int DBG_STREAMS = 0x00020000; // NTFS streams
    public static final int DBG_SOCKET = 0x00040000; // NetBIOS/native SMB socket connections

    /**
     * Class constructor.
     * 
     * @param handler Packet handler used to send/receive SMBs
     * @param srv Server that this session is associated with.
     */
    public SMBSrvSession(PacketHandler handler, SMBServer srv)
    {
        super(-1, srv, handler.isProtocolName(), null);

        // Set the packet handler

        m_pktHandler = handler;

        // Allocate a receive buffer

        m_buf = new byte[DefaultBufferSize];
        m_smbPkt = new SMBSrvPacket(m_buf);

        // If this is a TCPIP SMB or Win32 NetBIOS session then bypass the NetBIOS session setup
        // phase.

        if (isProtocol() == SMBSrvPacket.PROTOCOL_TCPIP || isProtocol() == SMBSrvPacket.PROTOCOL_WIN32NETBIOS)
        {

            // Advance to the SMB negotiate dialect phase

            setState(SMBSrvSessionState.SMBNEGOTIATE);

            // Check if the client name is available

            if (handler.hasClientName())
                m_callerNBName = handler.getClientName();
        }
    }

    /**
     * Return the session protocol type
     * 
     * @return int
     */
    public final int isProtocol()
    {
        return m_pktHandler.isProtocol();
    }

    /**
     * Add a new connection to this session. Return the allocated tree id for the new connection.
     * 
     * @return int Allocated tree id (connection id).
     * @param shrDev SharedDevice
     */
    protected int addConnection(SharedDevice shrDev) throws TooManyConnectionsException
    {

        // Check if the connection array has been allocated

        if (m_connections == null)
            m_connections = new Hashtable<Integer, TreeConnection>(DefaultConnections);

        // Allocate an id for the tree connection

        int treeId = 0;

        synchronized (m_connections)
        {

            // Check if the tree connection table is full

            if (m_connections.size() == MaxConnections)
                throw new TooManyConnectionsException();

            // Find a free slot in the connection array

            treeId = (m_treeId++ & TreeIdMask);
            Integer key = new Integer(treeId);

            while (m_connections.contains(key))
            {

                // Try another tree id for the new connection

                treeId = (m_treeId++ & TreeIdMask);
                key = new Integer(treeId);
            }

            // Store the new tree connection

            m_connections.put(key, new TreeConnection(shrDev));
        }

        // Return the allocated tree id

        return treeId;
    }

    /**
     * Allocate a slot in the active searches list for a new search.
     * 
     * @return int Search slot index, or -1 if there are no more search slots available.
     */
    protected final int allocateSearchSlot()
    {

        // Check if the search array has been allocated

        if (m_search == null)
            m_search = new SearchContext[DefaultSearches];

        // Find a free slot for the new search

        int idx = 0;

        while (idx < m_search.length && m_search[idx] != null)
            idx++;

        // Check if we found a free slot

        if (idx == m_search.length)
        {

            // The search array needs to be extended, check if we reached the limit.

            if (m_search.length >= MaxSearches)
                return -1;

            // Extend the search array

            SearchContext[] newSearch = new SearchContext[m_search.length * 2];
            System.arraycopy(m_search, 0, newSearch, 0, m_search.length);
            m_search = newSearch;
        }

        // Return the allocated search slot index

        m_searchCount++;
        return idx;
    }

    /**
     * Cleanup any resources owned by this session, close files, searches and change notification
     * requests.
     */
    protected final void cleanupSession()
    {

        // Debug

        if (logger.isDebugEnabled() && hasDebug(DBG_STATE))
            logger.debug("Cleanup session, searches=" + getSearchCount() + ", treeConns=" + getConnectionCount()
                    + ", changeNotify=" + getNotifyChangeCount());

        // Check if there are any active searches

        if (m_search != null)
        {

            // Close all active searches

            for (int idx = 0; idx < m_search.length; idx++)
            {

                // Check if the current search slot is active

                if (m_search[idx] != null)
                    deallocateSearchSlot(idx);
            }

            // Release the search context list, clear the search count

            m_search = null;
            m_searchCount = 0;
        }

        // Check if there are open tree connections

        if (m_connections != null)
        {

            synchronized (m_connections)
            {

                // Close all active tree connections

                Enumeration<TreeConnection> enm = m_connections.elements();

                while (enm.hasMoreElements())
                {

                    // Get the current tree connection

                    TreeConnection tree = enm.nextElement();
                    DeviceInterface devIface = tree.getInterface();

                    // Check if there are open files on the share

                    if (tree.openFileCount() > 0)
                    {

                        // Close the open files, release locks

                        for (int i = 0; i < tree.getFileTableLength(); i++)
                        {

                            // Get an open file

                            NetworkFile curFile = tree.findFile(i);
                            if (curFile != null && devIface instanceof DiskInterface)
                            {

                                // Access the disk share interface

                                DiskInterface diskIface = (DiskInterface) devIface;

                                try
                                {

                                    // Remove the file from the tree connection list

                                    tree.removeFile(i, this);

                                    // Close the file

                                    diskIface.closeFile(this, tree, curFile);
                                }
                                catch (Exception ex)
                                {
                                }
                            }
                        }
                    }
                    // Inform the driver that the connection has been closed

                    if (devIface != null)
                        devIface.treeClosed(this, tree);
                }

                // Clear the tree connection list

                m_connections.clear();
            }
        }

        // Commit, or rollback, any active user transaction
        
        try
        {
            // Commit or rollback the transaction

            endTransaction();
        }
        catch ( Exception ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Error committing transaction", ex);
        }
        
        // Check if there are active change notification requests

        if (m_notifyList != null && m_notifyList.numberOfRequests() > 0)
        {

            // Remove the notify requests from the associated device context notify list

            for (int i = 0; i < m_notifyList.numberOfRequests(); i++)
            {

                // Get the current change notification request and remove from the global notify
                // list

                NotifyRequest curReq = m_notifyList.getRequest(i);
                curReq.getDiskContext().getChangeHandler().removeNotifyRequests(this);
            }
        }

        // Delete any temporary shares that were created for this session

        getSMBServer().deleteTemporaryShares(this);
    }

    /**
     * Close the session socket
     */
    protected final void closeSocket()
    {

        // Indicate that the session is being shutdown

        setShutdown(true);

        // Close the packet handler

        try
        {
            m_pktHandler.closeHandler();
        }
        catch (Exception ex)
        {
        }
    }

    /**
     * Close the session
     */
    public final void closeSession()
    {

        // Call the base class

        super.closeSession();

        try
        {

            // Set the session into a hangup state and indicate that we have shutdown the session

            setState(SMBSrvSessionState.NBHANGUP);
            setShutdown(true);

            // Close the packet handler

            m_pktHandler.closeHandler();
        }
        catch (Exception ex)
        {
        }

    }

    /**
     * Deallocate the specified search context/slot.
     * 
     * @param ctxId int
     */
    protected final void deallocateSearchSlot(int ctxId)
    {

        // Check if the search array has been allocated and that the index is valid

        if (m_search == null || ctxId >= m_search.length)
            return;

        // Close the search

        if (m_search[ctxId] != null)
            m_search[ctxId].closeSearch();

        // Free the specified search context slot

        m_searchCount--;
        m_search[ctxId] = null;
    }

    /**
     * Finalize, object is about to be garbage collected. Make sure resources are released.
     */
    public void finalize()
    {

        // Check if there are any active resources

        cleanupSession();

        // Make sure the socket is closed and deallocated

        closeSocket();
    }

    /**
     * Return the tree connection details for the specified tree id.
     * 
     * @param treeId int
     * @return TreeConnection
     */
    protected final TreeConnection findConnection(int treeId)
    {

        // Check if the tree id and connection array are valid

        if (m_connections == null)
            return null;

        // Get the required tree connection details

        return (TreeConnection) m_connections.get(new Integer(treeId));
    }

    /**
     * Return the input/output metwork buffer for this session.
     * 
     * @return byte[]
     */
    protected final byte[] getBuffer()
    {
        return m_buf;
    }

    /**
     * Return the count of active connections for this session.
     * 
     * @return int
     */
    public final int getConnectionCount()
    {
        return m_connections != null ? m_connections.size() : 0;
    }

    /**
     * Return the default flags SMB header value
     * 
     * @return int
     */
    public final int getDefaultFlags()
    {
        return m_defFlags;
    }

    /**
     * Return the default flags2 SMB header value
     * 
     * @return int
     */
    public final int getDefaultFlags2()
    {
        return m_defFlags2;
    }

    /**
     * Return the count of active change notification requests
     * 
     * @return int
     */
    public final int getNotifyChangeCount()
    {
        if (m_notifyList == null)
            return 0;
        return m_notifyList.numberOfRequests();
    }

    /**
     * Return the client maximum buffer size
     * 
     * @return int
     */
    public final int getClientMaximumBufferSize()
    {
        return m_maxBufSize;
    }

    /**
     * Return the client maximum muliplexed requests
     * 
     * @return int
     */
    public final int getClientMaximumMultiplex()
    {
        return m_maxMultiplex;
    }

    /**
     * Return the client capability flags
     * 
     * @return int
     */
    public final int getClientCapabilities()
    {
        return m_clientCaps;
    }

    /**
     * Determine if the client has the specified capability enabled
     * 
     * @param cap int
     * @return boolean
     */
    public final boolean hasClientCapability(int cap)
    {
        if ((m_clientCaps & cap) != 0)
            return true;
        return false;
    }

    /**
     * Return the SMB dialect type that the server/client have negotiated.
     * 
     * @return int
     */
    public final int getNegotiatedSMBDialect()
    {
        return m_dialect;
    }

    /**
     * Return the packet handler used by the session
     * 
     * @return PacketHandler
     */
    public final PacketHandler getPacketHandler()
    {
        return m_pktHandler;
    }

    /**
     * Return the receiver SMB packet.
     * 
     * @return SMBSrvPacket
     */
    public final SMBSrvPacket getReceivePacket()
    {
        return m_smbPkt;
    }

    /**
     * Return the remote NetBIOS name that was used to create the session.
     * 
     * @return java.lang.String
     */
    public final String getRemoteNetBIOSName()
    {
        return m_callerNBName;
    }

    /**
     * Check if the session has a target NetBIOS name
     * 
     * @return boolean
     */
    public final boolean hasTargetNetBIOSName()
    {
        return m_targetNBName != null ? true : false;
    }

    /**
     * Return the target NetBIOS name that was used to create the session
     * 
     * @return String
     */
    public final String getTargetNetBIOSName()
    {
        return m_targetNBName;
    }

    /**
     * Cehck if the clients remote address is available
     * 
     * @return boolean
     */
    public final boolean hasRemoteAddress()
    {
        return m_pktHandler.hasRemoteAddress();
    }

    /**
     * Return the client network address
     * 
     * @return InetAddress
     */
    public final InetAddress getRemoteAddress()
    {
        return m_pktHandler.getRemoteAddress();
    }

    /**
     * Return the search context for the specified search id.
     * 
     * @param srchId int
     * @return SearchContext
     */
    protected final SearchContext getSearchContext(int srchId)
    {

        // Check if the search array is valid and the search index is valid

        if (m_search == null || srchId >= m_search.length)
            return null;

        // Return the required search context

        return m_search[srchId];
    }

    /**
     * Return the number of active tree searches.
     * 
     * @return int
     */
    public final int getSearchCount()
    {
        return m_searchCount;
    }

    /**
     * Return the server that this session is associated with.
     * 
     * @return SMBServer
     */
    public final SMBServer getSMBServer()
    {
        return (SMBServer) getServer();
    }

    /**
     * Return the server name that this session is associated with.
     * 
     * @return java.lang.String
     */
    public final String getServerName()
    {
        return getSMBServer().getServerName();
    }

    /**
     * Hangup the session.
     * 
     * @param reason java.lang.String Reason the session is being closed.
     */
    private void hangupSession(String reason)
    {

        // Debug

        if (logger.isDebugEnabled() && hasDebug(DBG_NETBIOS))
            logger.debug("## Session closing - " + reason);

        // Set the session into a NetBIOS hangup state

        setState(SMBSrvSessionState.NBHANGUP);
    }

    /**
     * Check if the Macintosh exteniosn SMBs are enabled
     * 
     * @return boolean
     */
    public final boolean hasMacintoshExtensions()
    {
        return getSMBServer().getConfiguration().hasMacintoshExtensions();
    }

    /**
     * Check if there is a change notification update pending
     * 
     * @return boolean
     */
    public final boolean hasNotifyPending()
    {
        return m_notifyPending;
    }

    /**
     * Set the change notify pending flag
     * 
     * @param pend boolean
     */
    public final void setNotifyPending(boolean pend)
    {
        m_notifyPending = pend;
    }

    /**
     * Set the client maximum buffer size
     * 
     * @param maxBuf int
     */
    public final void setClientMaximumBufferSize(int maxBuf)
    {
        m_maxBufSize = maxBuf;
    }

    /**
     * Set the client maximum multiplexed
     * 
     * @param maxMpx int
     */
    public final void setClientMaximumMultiplex(int maxMpx)
    {
        m_maxMultiplex = maxMpx;
    }

    /**
     * Set the client capability flags
     * 
     * @param flags int
     */
    public final void setClientCapabilities(int flags)
    {
        m_clientCaps = flags;
    }

    /**
     * Set the default flags value to be ORed with outgoing response packet flags
     * 
     * @param flags int
     */
    public final void setDefaultFlags(int flags)
    {
        m_defFlags = flags;
    }

    /**
     * Set the default flags2 value to be ORed with outgoing response packet flags2 field
     * 
     * @param flags int
     */
    public final void setDefaultFlags2(int flags)
    {
        m_defFlags2 = flags;
    }

    /**
     * Set the SMB packet
     * 
     * @param pkt SMBSrvPacket
     */
    public final void setReceivePacket(SMBSrvPacket pkt)
    {
        m_smbPkt = pkt;
        m_buf = pkt.getBuffer();
    }

    /**
     * Store the seach context in the specified slot.
     * 
     * @param slot Slot to store the search context.
     * @param srch SearchContext
     */
    protected final void setSearchContext(int slot, SearchContext srch)
    {

        // Check if the search slot id is valid

        if (m_search == null || slot > m_search.length)
            return;

        // Store the context

        m_search[slot] = srch;
    }

    /**
     * Set the session state.
     * 
     * @param state int
     */
    protected void setState(int state)
    {

        // Debug

        if (logger.isDebugEnabled() && hasDebug(DBG_STATE))
            logger.debug("State changed to " + SMBSrvSessionState.getStateAsString(state));

        // Change the session state

        m_state = state;
    }

    /**
     * Process the NetBIOS session request message, either accept the session request and send back
     * a NetBIOS accept or reject the session and send back a NetBIOS reject and hangup the session.
     */
    protected void procNetBIOSSessionRequest() throws IOException, NetBIOSException
    {

        // Check if the received packet contains enough data for a NetBIOS session request packet.

        NetBIOSPacket nbPkt = new NetBIOSPacket(m_buf);

        if (m_rxlen < RFCNetBIOSProtocol.SESSREQ_LEN || nbPkt.getHeaderType() != RFCNetBIOSProtocol.SESSION_REQUEST)
            throw new NetBIOSException("NBREQ Invalid packet");

        // Do a few sanity checks on the received packet

        if (m_buf[4] != (byte) 32 || m_buf[38] != (byte) 32)
            throw new NetBIOSException("NBREQ Invalid NetBIOS name data");

        // Extract the from/to NetBIOS encoded names, and convert to normal strings.

        StringBuffer nbName = new StringBuffer(32);
        for (int i = 0; i < 32; i++)
            nbName.append((char) m_buf[5 + i]);
        String toName = NetBIOSSession.DecodeName(nbName.toString());
        toName = toName.trim();

        nbName.setLength(0);
        for (int i = 0; i < 32; i++)
            nbName.append((char) m_buf[39 + i]);
        String fromName = NetBIOSSession.DecodeName(nbName.toString());
        fromName = fromName.trim();

        // Debug

        if (logger.isDebugEnabled() && hasDebug(DBG_NETBIOS))
            logger.debug("NetBIOS CALL From " + fromName + " to " + toName);

        // Check that the request is for this server

        boolean forThisServer = false;

        if (toName.compareTo(getServerName()) == 0 || toName.compareTo(NetBIOSName.SMBServer) == 0
                || toName.compareTo(NetBIOSName.SMBServer2) == 0 || toName.compareTo("*") == 0)
        {

            // Request is for this server

            forThisServer = true;
        }
        else
        {

            // Check if the caller is using an IP address

            InetAddress[] srvAddr = getSMBServer().getServerAddresses();
            if (srvAddr != null)
            {

                // Check for an address match

                int idx = 0;

                while (idx < srvAddr.length && forThisServer == false)
                {

                    // Check the current IP address

                    if (srvAddr[idx++].getHostAddress().compareTo(toName) == 0)
                        forThisServer = true;
                }
            }
        }

        // If we did not find an address match then reject the session request

        if (forThisServer == false)
            throw new NetBIOSException("NBREQ Called name is not this server (" + toName + ")");

        // Debug

        if (logger.isDebugEnabled() && hasDebug(DBG_NETBIOS))
            logger.debug("NetBIOS session request from " + fromName);

        // Save the callers name and target name

        m_callerNBName = fromName;
        m_targetNBName = toName;

        // Set the remote client name

        setRemoteName(fromName);

        // Build a NetBIOS session accept message

        nbPkt.setHeaderType(RFCNetBIOSProtocol.SESSION_ACK);
        nbPkt.setHeaderFlags(0);
        nbPkt.setHeaderLength(0);

        // Output the NetBIOS session accept packet

        m_pktHandler.writePacket(m_buf, 0, 4);

        // Move the session to the SMB negotiate state

        setState(SMBSrvSessionState.SMBNEGOTIATE);
    }

    /**
     * Process an SMB dialect negotiate request.
     */
    protected void procSMBNegotiate() throws SMBSrvException, IOException
    {

        // Create an SMB server packet using the receive buffer

        m_smbPkt = new SMBSrvPacket(m_buf);

        // Initialize the NetBIOS header

        m_buf[0] = (byte) RFCNetBIOSProtocol.SESSION_MESSAGE;

        // Check if the received packet looks like a valid SMB

        if (m_smbPkt.getCommand() != PacketType.Negotiate || m_smbPkt.checkPacketIsValid(0, 2) == false)
        {
            sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand, SMBStatus.ErrSrv);
            return;
        }

        // Decode the data block into a list of requested SMB dialects

        int dataPos = m_smbPkt.getByteOffset();
        int dataLen = m_smbPkt.getByteCount();

        String diaStr = null;
        StringList dialects = new StringList();

        while (dataLen > 0)
        {

            // Decode an SMB dialect string from the data block, always ASCII strings

            diaStr = DataPacker.getDataString(DataType.Dialect, m_buf, dataPos, dataLen, false);
            if (diaStr != null)
            {

                // Add the dialect string to the list of requested dialects

                dialects.addString(diaStr);
            }
            else
            {

                // Invalid dialect block in the negotiate packet, send an error response and hangup
                // the session.

                sendErrorResponseSMB(SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
                setState(SMBSrvSessionState.NBHANGUP);
                return;
            }

            // Update the remaining data position and count

            dataPos += diaStr.length() + 2; // data type and null
            dataLen -= diaStr.length() + 2;
        }

        // Find the highest level SMB dialect that the server and client both support

        DialectSelector dia = getSMBServer().getSMBDialects();
        int diaIdx = -1;

        for (int i = 0; i < Dialect.Max; i++)
        {

            // Check if the current dialect is supported by the server

            if (dia.hasDialect(i))
            {

                // Check if the client supports the current dialect. If the current dialect is a
                // higher level dialect than the currently nominated dialect, update the nominated
                // dialect index.

                for (int j = 0; j < Dialect.SMB_PROT_MAXSTRING; j++)
                {

                    // Check if the dialect string maps to the current dialect index

                    if (Dialect.DialectType(j) == i && dialects.containsString(Dialect.DialectString(j)))
                    {

                        // Update the selected dialect type, if the current dialect is a newer
                        // dialect

                        if (i > diaIdx)
                            diaIdx = i;
                    }
                }
            }
        }

        // Debug

        if (logger.isDebugEnabled() && hasDebug(DBG_NEGOTIATE))
        {
            if (diaIdx == -1)
                logger.debug("Failed to negotiate SMB dialect");
            else
                logger.debug("Negotiated SMB dialect - " + Dialect.DialectTypeString(diaIdx));
        }

        // Check if we successfully negotiated an SMB dialect with the client

        if (diaIdx != -1)
        {

            // Store the negotiated SMB diialect type

            m_dialect = diaIdx;

            // Convert the dialect type to an index within the clients SMB dialect list

            diaIdx = dialects.findString(Dialect.DialectTypeString(diaIdx));

            // Allocate a protocol handler for the negotiated dialect, if we cannot get a protocol
            // handler then bounce the request.

            m_handler = ProtocolFactory.getHandler(m_dialect);
            if (m_handler != null)
            {

                // Debug

                if (logger.isDebugEnabled() && hasDebug(DBG_NEGOTIATE))
                    logger.debug("Assigned protocol handler - " + m_handler.getClass().getName());

                // Set the protocol handlers associated session

                m_handler.setSession(this);
            }
            else
            {

                // Could not get a protocol handler for the selected SMB dialect, indicate to the
                // client
                // that no suitable dialect available.

                diaIdx = -1;
            }
        }

        // Build the negotiate response SMB for Core dialect

        if (m_dialect == -1 || m_dialect <= Dialect.CorePlus)
        {

            // Core dialect negotiate response, or no valid dialect response

            m_smbPkt.setParameterCount(1);
            m_smbPkt.setParameter(0, diaIdx);
            m_smbPkt.setByteCount(0);

            m_smbPkt.setTreeId(0);
            m_smbPkt.setUserId(0);
        }
        else if (m_dialect <= Dialect.LanMan2_1)
        {

            // We are using case sensitive pathnames and long file names

            m_smbPkt.setFlags(SMBSrvPacket.FLG_CASELESS);
            m_smbPkt.setFlags2(SMBSrvPacket.FLG2_LONGFILENAMES);

            // Access the authenticator for this server and determine if the server is in share or
            // user level
            // security mode.

            SrvAuthenticator auth = getServer().getConfiguration().getAuthenticator();
            int secMode = 0;

            if (auth != null)
            {

                // Check if the server is in share or user level security mode

                if (auth.getAccessMode() == SrvAuthenticator.USER_MODE)
                    secMode = 1;

                // Check if encrypted passwords should be used by the client

                if (auth.hasEncryptPasswords())
                    secMode += 2;
            }

            // LanMan dialect negotiate response

            m_smbPkt.setParameterCount(13);
            m_smbPkt.setParameter(0, diaIdx);
            m_smbPkt.setParameter(1, secMode); // Security mode, encrypt passwords
            m_smbPkt.setParameter(2, LanManBufferSize);
            m_smbPkt.setParameter(3, LanManMaxMultiplexed); // maximum multiplexed requests
            m_smbPkt.setParameter(4, MaxVirtualCircuits); // maximum number of virtual circuits
            m_smbPkt.setParameter(5, 0); // read/write raw mode support

            // Create a session token, using the system clock

            m_smbPkt.setParameterLong(6, (int) (System.currentTimeMillis() & 0xFFFFFFFF));

            // Return the current server date/time

            SMBDate srvDate = new SMBDate(System.currentTimeMillis());
            m_smbPkt.setParameter(8, srvDate.asSMBTime());
            m_smbPkt.setParameter(9, srvDate.asSMBDate());

            // Server timezone offset from UTC

            m_smbPkt.setParameter(10, getServer().getConfiguration().getTimeZoneOffset());

            // Encryption key length

            m_smbPkt.setParameter(11, 8); // Encryption key length
            m_smbPkt.setParameter(12, 0);

            // Encryption key and primary domain string should be returned in the byte area

            setChallengeKey(auth.getChallengeKey(this));
            int pos = m_smbPkt.getByteOffset();
            byte[] buf = m_smbPkt.getBuffer();

            if (hasChallengeKey() == false)
            {

                // Return a dummy encryption key

                for (int i = 0; i < 8; i++)
                    buf[pos++] = 0;
            }
            else
            {

                // Store the encryption key

                byte[] key = getChallengeKey();
                for (int i = 0; i < key.length; i++)
                    buf[pos++] = key[i];
            }

            // Set the local domain name

            String domain = getServer().getConfiguration().getDomainName();
            if (domain != null)
                pos = DataPacker.putString(domain, buf, pos, true);

            m_smbPkt.setByteCount(pos - m_smbPkt.getByteOffset());

            m_smbPkt.setTreeId(0);
            m_smbPkt.setUserId(0);
        }
        else if (m_dialect == Dialect.NT)
        {

            // We are using case sensitive pathnames and long file names

            setDefaultFlags(SMBSrvPacket.FLG_CASELESS);
            setDefaultFlags2(SMBSrvPacket.FLG2_LONGFILENAMES + SMBSrvPacket.FLG2_UNICODE);

            // Access the authenticator for this server and determine if the server is in share or
            // user level security mode.

            SrvAuthenticator auth = getServer().getConfiguration().getAuthenticator();
            int secMode = 0;

            if (auth != null)
            {

                // Check if the server is in share or user level security mode

                if (auth.getAccessMode() == SrvAuthenticator.USER_MODE)
                    secMode = 1;

                // Check if encrypted passwords should be used by the client

                if (auth.hasEncryptPasswords())
                    secMode += 2;
            }

            // NT dialect negotiate response

            NTParameterPacker nt = new NTParameterPacker(m_smbPkt.getBuffer());

            m_smbPkt.setParameterCount(17);
            nt.packWord(diaIdx); // selected dialect index
            nt.packByte(secMode); // security mode
            nt.packWord(NTMaxMultiplexed); // maximum multiplexed requests
            // setting to 1 will disable change notify requests from the client
            nt.packWord(MaxVirtualCircuits); // maximum number of virtual circuits

            int maxBufSize = m_smbPkt.getBuffer().length - RFCNetBIOSProtocol.HEADER_LEN;
            nt.packInt(maxBufSize);

            nt.packInt(0); // maximum raw size

            // Create a session token, using the system clock

            nt.packInt((int) (System.currentTimeMillis() & 0xFFFFFFFFL));

            // Set server capabilities

            nt.packInt(Capability.Unicode + Capability.RemoteAPIs + Capability.NTSMBs + Capability.NTFind
                    + Capability.NTStatus + Capability.LargeFiles + Capability.LargeRead + Capability.LargeWrite);

            // Return the current server date/time, and timezone offset

            long srvTime = NTTime.toNTTime(new java.util.Date(System.currentTimeMillis()));

            nt.packLong(srvTime);
            nt.packWord(getServer().getConfiguration().getTimeZoneOffset());

            // Encryption key length

            nt.packByte(8); // encryption key length

            // Encryption key and primary domain string should be returned in the byte area

            setChallengeKey(auth.getChallengeKey(this));

            int pos = m_smbPkt.getByteOffset();
            byte[] buf = m_smbPkt.getBuffer();

            if (hasChallengeKey() == false)
            {

                // Return a dummy encryption key

                for (int i = 0; i < 8; i++)
                    buf[pos++] = 0;
            }
            else
            {

                // Store the encryption key

                byte[] key = getChallengeKey();

                for (int i = 0; i < key.length; i++)
                    buf[pos++] = key[i];
            }

            // Pack the local domain name

            String domain = getServer().getConfiguration().getDomainName();
            if (domain != null)
                pos = DataPacker.putUnicodeString(domain, buf, pos, true);

            // Pack the server name

            pos = DataPacker.putUnicodeString(getServerName(), buf, pos, true);

            // Set the packet length

            m_smbPkt.setByteCount(pos - m_smbPkt.getByteOffset());

            m_smbPkt.setFlags( getDefaultFlags());
            m_smbPkt.setFlags2( getDefaultFlags2());
            
            m_smbPkt.setTreeId(0);
            m_smbPkt.setUserId(0);
        }

        // Make sure the response flag is set

        if (m_smbPkt.isResponse() == false)
            m_smbPkt.setFlags(m_smbPkt.getFlags() + SMBPacket.FLG_RESPONSE);

        // Send the negotiate response

        m_pktHandler.writePacket(m_smbPkt, m_smbPkt.getLength());

        // Check if the negotiated SMB dialect supports the session setup command, if not then
        // bypass
        // the session setup phase.

        if (m_dialect == -1)
            setState(SMBSrvSessionState.NBHANGUP);
        else if (Dialect.DialectSupportsCommand(m_dialect, PacketType.SessionSetupAndX))
            setState(SMBSrvSessionState.SMBSESSSETUP);
        else
            setState(SMBSrvSessionState.SMBSESSION);

        // If a dialect was selected inform the server that the session has been opened

        if (m_dialect != -1)
            getSMBServer().sessionOpened(this);
    }

    /**
     * Remove the specified tree connection from the active connection list.
     * 
     * @param treeId int
     */
    protected void removeConnection(int treeId)
    {

        // Check if the tree id is valid

        if (m_connections == null)
            return;

        // Close the connection and remove from the connection list

        synchronized (m_connections)
        {

            // Get the connection

            Integer key = new Integer(treeId);
            TreeConnection tree = (TreeConnection) m_connections.get(key);

            // Close the connection, release resources

            if (tree != null)
            {

                // Close the connection

                tree.closeConnection(this);

                // Remove the connection from the connection list

                m_connections.remove(key);
            }
        }
    }

    /**
     * Start the SMB server session in a seperate thread.
     */
    public void run()
    {
        try
        {
            // Debug

            if (logger.isDebugEnabled() && hasDebug(SMBSrvSession.DBG_NEGOTIATE))
                logger.debug("Server session started");

            // The server session loops until the NetBIOS hangup state is set.

            while (m_state != SMBSrvSessionState.NBHANGUP)
            {

                // Set the current receive length to -1 to indicate that the session thread is not
                // currently processing an SMB packet. This is used by the asynchronous response code
                // to determine when it can send the response.

                m_rxlen = -1;

                // Wait for a data packet

                m_rxlen = m_pktHandler.readPacket(m_smbPkt);

                // Check for an empty packet

                if (m_rxlen == 0)
                    continue;

                // Check if there is no more data, the other side has dropped the connection

                if (m_rxlen == -1)
                {
                    hangupSession("Remote disconnect");
                    continue;
                }

                // Store the received data length

                m_smbPkt.setReceivedLength(m_rxlen);

                // Update the request count
        
                m_reqCount++;
                
                // Process the received packet

                switch (m_state)
                {

                // NetBIOS session request pending

                case SMBSrvSessionState.NBSESSREQ:
                    procNetBIOSSessionRequest();
                    break;

                // SMB dialect negotiate

                case SMBSrvSessionState.SMBNEGOTIATE:
                    procSMBNegotiate();
                    break;

                // SMB session setup

                case SMBSrvSessionState.SMBSESSSETUP:
                    m_handler.runProtocol();
                    break;

                // SMB session main request processing

                case SMBSrvSessionState.SMBSESSION:

                    // Run the main protocol handler

                    runHandler();
                    break;

                } // end switch session state

                // Commit, or rollback, any active user transaction
                
                try
                {
                    // Commit or rollback the transaction

                    endTransaction();
                }
                catch ( Exception ex)
                {
                    // Debug
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("Error committing transaction", ex);
                }

                // Give up the CPU

                Thread.yield();

            } // end while state
        }
        catch (SocketException ex)
        {

            // DEBUG

            logger.error("Socket closed by remote client");
        }
        catch (Exception ex)
        {

            // Output the exception details

            if (isShutdown() == false)
                logger.error("Closing session due to exception", ex);
        }
        catch (Throwable ex)
        {
            logger.error("Closing session due to throwable", ex);
        }
        finally
        {
            // If there is an active transaction then roll it back
            
            if ( hasUserTransaction())
            {
                try
                {
                    getUserTransaction().rollback();
                }
                catch (Exception ex)
                {
                    logger.warn("Failed to rollback transaction", ex);
                }
            }                
        }

        // Cleanup the session, make sure all resources are released

        cleanupSession();

        // Debug

        if (logger.isDebugEnabled() && hasDebug(DBG_STATE))
            logger.debug("Server session closed");

        // Close the session

        closeSocket();

        // Notify the server that the session has closed

        getSMBServer().sessionClosed(this);
    }

    /**
     * Handle a session message, receive all data and run the SMB protocol handler.
     */
    protected final void runHandler() throws IOException, SMBSrvException, TooManyConnectionsException
    {

        // Make sure we received at least a NetBIOS header

        if (m_rxlen < NetBIOSPacket.MIN_RXLEN)
            return;

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug(DBG_PKTTYPE))
            logger.debug("Rx packet type - " + m_smbPkt.getPacketTypeString() + ", SID=" + m_smbPkt.getSID());

        // Call the protocol handler

        if (m_handler.runProtocol() == false)
        {

            // The sessions protocol handler did not process the request, return an unsupported
            // SMB error status.

            sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
        }

        // Check if there are any pending asynchronous response packets

        while (hasAsynchResponse())
        {

            // Remove the current asynchronous response SMB packet and send to the client

            SMBSrvPacket asynchPkt = removeFirstAsynchResponse();
            sendResponseSMB(asynchPkt, asynchPkt.getLength());

            // DEBUG

            if (logger.isDebugEnabled() && hasDebug(DBG_NOTIFY))
                logger.debug("Sent queued asynch response type=" + asynchPkt.getPacketTypeString() + ", mid="
                        + asynchPkt.getMultiplexId() + ", pid=" + asynchPkt.getProcessId());
        }
    }

    /**
     * Send an SMB response
     * 
     * @param pkt SMBSrvPacket
     * @exception IOException
     */
    public final void sendResponseSMB(SMBSrvPacket pkt) throws IOException
    {
        sendResponseSMB(pkt, pkt.getLength());
    }

    /**
     * Send an SMB response
     * 
     * @param pkt SMBSrvPacket
     * @param len int
     * @exception IOException
     */
    public synchronized final void sendResponseSMB(SMBSrvPacket pkt, int len) throws IOException
    {

        // Make sure the response flag is set

        if (pkt.isResponse() == false)
            pkt.setFlags(pkt.getFlags() + SMBSrvPacket.FLG_RESPONSE);

        // Add default flags/flags2 values

        pkt.setFlags(pkt.getFlags() | getDefaultFlags());

        // Mask out certain flags that the client may have sent

        int flags2 = pkt.getFlags2() | getDefaultFlags2();
        flags2 &= ~(SMBPacket.FLG2_EXTENDEDATTRIB + SMBPacket.FLG2_EXTENDNEGOTIATE + SMBPacket.FLG2_DFSRESOLVE + SMBPacket.FLG2_SECURITYSIGS);

        pkt.setFlags2(flags2);

        // Send the response packet

        m_pktHandler.writePacket(pkt, len);
        m_pktHandler.flushPacket();
    }

    /**
     * Send a success response SMB
     * 
     * @exception IOException If a network error occurs
     */
    public final void sendSuccessResponseSMB() throws IOException
    {

        // Make sure the response flag is set

        if (m_smbPkt.isResponse() == false)
            m_smbPkt.setFlags(m_smbPkt.getFlags() + SMBSrvPacket.FLG_RESPONSE);

        // Add default flags/flags2 values

        m_smbPkt.setFlags(m_smbPkt.getFlags() | getDefaultFlags());
        m_smbPkt.setFlags2(m_smbPkt.getFlags2() | getDefaultFlags2());

        // Clear the parameter and byte counts

        m_smbPkt.setParameterCount(0);
        m_smbPkt.setByteCount(0);

        if (m_smbPkt.isLongErrorCode())
            m_smbPkt.setLongErrorCode(SMBStatus.NTSuccess);
        else
        {
            m_smbPkt.setErrorClass(SMBStatus.Success);
            m_smbPkt.setErrorCode(SMBStatus.Success);
        }

        // Return the success response to the client

        sendResponseSMB(m_smbPkt, m_smbPkt.getLength());
    }

    /**
     * Send an error response SMB. The returned code depends on the client long error code flag
     * setting.
     * 
     * @param ntCode 32bit error code
     * @param stdCode Standard error code
     * @param StdClass Standard error class
     */
    public final void sendErrorResponseSMB(int ntCode, int stdCode, int stdClass) throws java.io.IOException
    {

        // Check if long error codes are required by the client

        if (m_smbPkt.isLongErrorCode())
        {

            // Return the long/NT status code

            sendErrorResponseSMB(ntCode, SMBStatus.NTErr);
        }
        else
        {

            // Return the standard/DOS error code

            sendErrorResponseSMB(stdCode, stdClass);
        }
    }

    /**
     * Send an error response SMB.
     * 
     * @param errCode int Error code.
     * @param errClass int Error class.
     */
    public final void sendErrorResponseSMB(int errCode, int errClass) throws java.io.IOException
    {

        // Make sure the response flag is set

        if (m_smbPkt.isResponse() == false)
            m_smbPkt.setFlags(m_smbPkt.getFlags() + SMBSrvPacket.FLG_RESPONSE);

        // Set the error code and error class in the response packet

        m_smbPkt.setParameterCount(0);
        m_smbPkt.setByteCount(0);

        // Add default flags/flags2 values

        m_smbPkt.setFlags(m_smbPkt.getFlags() | getDefaultFlags());
        m_smbPkt.setFlags2(m_smbPkt.getFlags2() | getDefaultFlags2());

        // Check if the error is a NT 32bit error status

        if (errClass == SMBStatus.NTErr)
        {

            // Enable the long error status flag

            if (m_smbPkt.isLongErrorCode() == false)
                m_smbPkt.setFlags2(m_smbPkt.getFlags2() + SMBSrvPacket.FLG2_LONGERRORCODE);

            // Set the NT status code

            m_smbPkt.setLongErrorCode(errCode);
        }
        else
        {

            // Disable the long error status flag

            if (m_smbPkt.isLongErrorCode() == true)
                m_smbPkt.setFlags2(m_smbPkt.getFlags2() - SMBSrvPacket.FLG2_LONGERRORCODE);

            // Set the error status/class

            m_smbPkt.setErrorCode(errCode);
            m_smbPkt.setErrorClass(errClass);
        }

        // Return the error response to the client

        sendResponseSMB(m_smbPkt, m_smbPkt.getLength());

        // Debug

        if (logger.isDebugEnabled() && hasDebug(DBG_ERROR))
            logger.debug("Error : Cmd = " + m_smbPkt.getPacketTypeString() + " - "
                    + SMBErrorText.ErrorString(errClass, errCode));
    }

    /**
     * Send, or queue, an asynchronous response SMB
     * 
     * @param pkt SMBSrvPacket
     * @param len int
     * @return true if the packet was sent, or false if it was queued
     * @exception IOException If an I/O error occurs
     */
    public final boolean sendAsynchResponseSMB(SMBSrvPacket pkt, int len) throws IOException
    {

        // Check if there is an SMB currently being processed or pending data from the client

        boolean sts = false;

        if (m_rxlen == -1 && m_pktHandler.availableBytes() == 0)
        {

            // Send the asynchronous response immediately

            sendResponseSMB(pkt, len);
            m_pktHandler.flushPacket();

            // Indicate that the SMB response has been sent

            sts = true;
        }
        else
        {

            // Queue the packet to send out when current SMB requests have been processed

            queueAsynchResponseSMB(pkt);
        }

        // Return the sent/queued status

        return sts;
    }

    /**
     * Queue an asynchronous response SMB for sending when current SMB requests have been processed.
     * 
     * @param pkt SMBSrvPacket
     */
    protected final synchronized void queueAsynchResponseSMB(SMBSrvPacket pkt)
    {

        // Check if the asynchronous response queue has been allocated

        if (m_asynchQueue == null)
        {

            // Allocate the asynchronous response queue

            m_asynchQueue = new Vector<SMBSrvPacket>();
        }

        // Add the SMB response packet to the queue

        m_asynchQueue.addElement(pkt);
    }

    /**
     * Check if there are any asynchronous requests queued
     * 
     * @return boolean
     */
    protected final synchronized boolean hasAsynchResponse()
    {

        // Check if the queue is valid

        if (m_asynchQueue != null && m_asynchQueue.size() > 0)
            return true;
        return false;
    }

    /**
     * Remove an asynchronous response packet from the head of the list
     * 
     * @return SMBSrvPacket
     */
    protected final synchronized SMBSrvPacket removeFirstAsynchResponse()
    {

        // Check if there are asynchronous response packets queued

        if (m_asynchQueue == null || m_asynchQueue.size() == 0)
            return null;

        // Return the SMB packet from the head of the queue

        return m_asynchQueue.remove(0);
    }

    /**
     * Find the notify request with the specified ids
     * 
     * @param mid int
     * @param tid int
     * @param uid int
     * @param pid int
     * @return NotifyRequest
     */
    public final NotifyRequest findNotifyRequest(int mid, int tid, int uid, int pid)
    {

        // Check if the local notify list is valid

        if (m_notifyList == null)
            return null;

        // Find the matching notify request

        return m_notifyList.findRequest(mid, tid, uid, pid);
    }

    /**
     * Find an existing notify request for the specified directory and filter
     * 
     * @param dir NetworkFile
     * @param filter int
     * @param watchTree boolean
     * @return NotifyRequest
     */
    public final NotifyRequest findNotifyRequest(NetworkFile dir, int filter, boolean watchTree)
    {

        // Check if the local notify list is valid

        if (m_notifyList == null)
            return null;

        // Find the matching notify request

        return m_notifyList.findRequest(dir, filter, watchTree);
    }

    /**
     * Add a change notification request
     * 
     * @param req NotifyRequest
     * @param ctx DiskDeviceContext
     */
    public final void addNotifyRequest(NotifyRequest req, DiskDeviceContext ctx)
    {

        // Check if the local notify list has been allocated

        if (m_notifyList == null)
            m_notifyList = new NotifyRequestList();

        // Add the request to the local list and the shares global list

        m_notifyList.addRequest(req);
        ctx.addNotifyRequest(req);
    }

    /**
     * Remove a change notification request
     * 
     * @param req NotifyRequest
     */
    public final void removeNotifyRequest(NotifyRequest req)
    {

        // Check if the local notify list has been allocated

        if (m_notifyList == null)
            return;

        // Remove the request from the local list and the shares global list

        m_notifyList.removeRequest(req);
        if (req.getDiskContext() != null)
            req.getDiskContext().removeNotifyRequest(req);
    }

    /**
     * Check if there is an active transaction
     * 
     * @return boolean
     */
    protected final boolean hasTransaction()
    {
        return m_transact != null ? true : false;
    }

    /**
     * Return the active transaction buffer
     * 
     * @return TransactBuffer
     */
    protected final SrvTransactBuffer getTransaction()
    {
        return m_transact;
    }

    /**
     * Set the active transaction buffer
     * 
     * @param buf TransactBuffer
     */
    protected final void setTransaction(SrvTransactBuffer buf)
    {
        m_transact = buf;
    }
}