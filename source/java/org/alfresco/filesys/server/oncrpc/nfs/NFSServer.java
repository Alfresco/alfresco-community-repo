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
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.filesys.server.oncrpc.nfs;

import java.io.*;
import java.util.*;

import org.alfresco.filesys.server.ServerListener;
import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.auth.acl.AccessControl;
import org.alfresco.filesys.server.auth.acl.AccessControlManager;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.server.core.InvalidDeviceInterfaceException;
import org.alfresco.filesys.server.core.ShareType;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.server.core.SharedDeviceList;
import org.alfresco.filesys.server.filesys.AccessDeniedException;
import org.alfresco.filesys.server.filesys.AccessMode;
import org.alfresco.filesys.server.filesys.DiskDeviceContext;
import org.alfresco.filesys.server.filesys.DiskFullException;
import org.alfresco.filesys.server.filesys.DiskInterface;
import org.alfresco.filesys.server.filesys.DiskSizeInterface;
import org.alfresco.filesys.server.filesys.FileAction;
import org.alfresco.filesys.server.filesys.FileAttribute;
import org.alfresco.filesys.server.filesys.FileExistsException;
import org.alfresco.filesys.server.filesys.FileIdInterface;
import org.alfresco.filesys.server.filesys.FileInfo;
import org.alfresco.filesys.server.filesys.FileName;
import org.alfresco.filesys.server.filesys.FileOpenParams;
import org.alfresco.filesys.server.filesys.FileStatus;
import org.alfresco.filesys.server.filesys.FileType;
import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.server.filesys.NotifyChange;
import org.alfresco.filesys.server.filesys.SearchContext;
import org.alfresco.filesys.server.filesys.SrvDiskInfo;
import org.alfresco.filesys.server.filesys.SymbolicLinkInterface;
import org.alfresco.filesys.server.filesys.TreeConnection;
import org.alfresco.filesys.server.filesys.TreeConnectionHash;
import org.alfresco.filesys.server.oncrpc.AuthType;
import org.alfresco.filesys.server.oncrpc.MultiThreadedTcpRpcSessionHandler;
import org.alfresco.filesys.server.oncrpc.MultiThreadedUdpRpcDatagramHandler;
import org.alfresco.filesys.server.oncrpc.PortMapping;
import org.alfresco.filesys.server.oncrpc.Rpc;
import org.alfresco.filesys.server.oncrpc.RpcAuthenticationException;
import org.alfresco.filesys.server.oncrpc.RpcAuthenticator;
import org.alfresco.filesys.server.oncrpc.RpcNetworkServer;
import org.alfresco.filesys.server.oncrpc.RpcPacket;
import org.alfresco.filesys.server.oncrpc.RpcPacketPool;
import org.alfresco.filesys.server.oncrpc.RpcProcessor;
import org.alfresco.filesys.server.oncrpc.RpcRequestThreadPool;

/**
 * NFS Server Class
 * 
 * <p>Contains the main NFS server.
 * 
 * @author GKSpencer
 */
public class NFSServer extends RpcNetworkServer implements RpcProcessor {

	// Constants
	//
	// Debug flags

	public static final int DBG_RXDATA 		= 0x00000001; // Received data
	public static final int DBG_TXDATA	 	= 0x00000002; // Transmit data
	public static final int DBG_DUMPDATA 	= 0x00000004; // Dump data packets
	public static final int DBG_SEARCH 		= 0x00000008; // File/directory search
	public static final int DBG_INFO 		= 0x00000010; // Information requests
	public static final int DBG_FILE 		= 0x00000020; // File open/close/info
	public static final int DBG_FILEIO 		= 0x00000040; // File read/write
	public static final int DBG_ERROR 		= 0x00000080; // Errors
	public static final int DBG_TIMING 		= 0x00000100; // Time packet processing
	public static final int DBG_DIRECTORY 	= 0x00000200; // Directory commands 
	public static final int DBG_SESSION 	= 0x00000400; // Session  creation/deletion

	// Unix path seperator

	public static final String UNIX_SEPERATOR 		= "/";
	public static final char UNIX_SEPERATOR_CHAR 	= '/';
	public static final String DOS_SEPERATOR 		= "\\";
	public static final char DOS_SEPERATOR_CHAR 	= '\\';

	// Unix file modes

	public static final int MODE_STFILE 		= 0100000;
	public static final int MODE_STDIR 			= 0040000;
	public static final int MODE_STREAD 		= 0000555;
	public static final int MODE_STWRITE 		= 0000333;
	public static final int MODE_DIR_DEFAULT 	= MODE_STDIR + (MODE_STREAD | MODE_STWRITE);
	public static final int MODE_FILE_DEFAULT 	= MODE_STFILE + (MODE_STREAD | MODE_STWRITE);

	// Readdir/Readdirplus cookie masks/shift
	//	
	// 32bit cookies (required by Solaris)

	public static final long COOKIE_RESUMEID_MASK = 0x00FFFFFFL;
	public static final long COOKIE_SEARCHID_MASK = 0xFF000000L;
	public static final int COOKIE_SEARCHID_SHIFT = 24;

	// Cookie ids for . and .. directory entries

	public static final long COOKIE_DOT_DIRECTORY 		= 0x00FFFFFFL;
	public static final long COOKIE_DOTDOT_DIRECTORY 	= 0x00FFFFFEL;

	// ReadDir and ReadDirPlus reply header and per file fixed structure
	// lengths.
	//
	// Add file name length rounded to 4 byte boundary to the per file structure
	// length to get the actual length.

	public final static int READDIRPLUS_HEADER_LENGTH 	= 108;
	public final static int READDIRPLUS_ENTRY_LENGTH 	= 200;
	public final static int READDIR_HEADER_LENGTH 		= 108;
	public final static int READDIR_ENTRY_LENGTH 		= 24;

	// File id offset

	public static final long FILE_ID_OFFSET 	= 2L;

	// Maximum request size to accept

	public final static int MaxRequestSize = 0xFFFF;

	// Filesystem limits

	public static final int MaxReadSize 	= MaxRequestSize;
	public static final int PrefReadSize 	= MaxRequestSize;
	public static final int MultReadSize 	= 4096;
	public static final int MaxWriteSize 	= MaxRequestSize;
	public static final int PrefWriteSize 	= MaxRequestSize;
	public static final int MultWriteSize 	= 4096;
	public static final int PrefReadDirSize = 8192;
	public static final long MaxFileSize 	= 0x01FFFFFFF000L;

	// Thread pool and packet pool defaults

	private static final int DefaultThreadPoolSize = 8;
	private static final int DefaultPacketPoolSize = 50;

	// Incoming datagram handler for UDP requests

	private MultiThreadedUdpRpcDatagramHandler m_udpHandler;

	// Incoming session handler for TCP requests

	private MultiThreadedTcpRpcSessionHandler m_tcpHandler;

	// Share details hash

	private ShareDetailsHash m_shareDetails;

	// Tree connection hash

	private TreeConnectionHash m_connections;

	// Session tables for the various authentication types

	private NFSSessionTable m_sessAuthNull;

	private NFSSessionTable m_sessAuthUnix;

	// Session id generator

	private int m_sessId = 1;

	// Port to bind the NFS server to (UDP and TCP)

	private int m_port;

	// Shared thread pool, used by TCP and UDP request handlers

	private RpcRequestThreadPool m_threadPool;

	// Shared packet pool, usd by TCP and UDP request handlers

	private RpcPacketPool m_packetPool;

	// RPC authenticator, from the main server configuration

	private RpcAuthenticator m_rpcAuthenticator;

	// Write verifier, generated from the server start time

	private long m_writeVerifier;

	/**
	 * Class constructor
	 * 
	 * @param config
	 *            ServerConfiguration
	 */
	public NFSServer(ServerConfiguration config) {
		super("NFS", config);

		// Set the debug flags

		setDebugFlags(config.getNFSDebug());

		// Set the port to bind the server to

		if (config.getNFSServerPort() != 0)
			setPort(config.getNFSServerPort());
		else
			setPort(NFS.DefaultPort);

		// Set the RPC authenticator

		m_rpcAuthenticator = config.getRpcAuthenticator();

		// Generate the write verifier

		m_writeVerifier = System.currentTimeMillis();
	}

	/**
	 * Return the port to bind to
	 * 
	 * @return int
	 */
	public final int getPort() {
		return m_port;
	}

	/**
	 * Set the port to use
	 * 
	 * @param port
	 *            int
	 */
	public final void setPort(int port) {
		m_port = port;
	}

	/**
	 * Start the NFS server
	 */
	public void startServer() {

		try {

			// Allocate the share detail hash list and tree connection list, and
			// populate with the available share details

			m_shareDetails = new ShareDetailsHash();
			m_connections = new TreeConnectionHash();

			checkForNewShares();

			// Get the thread pool and packet pool sizes

			int threadPoolSize = DefaultThreadPoolSize;

			if (getConfiguration().getNFSThreadPoolSize() > 0)
				threadPoolSize = getConfiguration().getNFSThreadPoolSize();

			int packetPoolSize = DefaultPacketPoolSize;

			if (getConfiguration().getNFSPacketPoolSize() > 0)
				packetPoolSize = getConfiguration().getNFSPacketPoolSize();

			// Create the share thread pool for RPC processing

			m_threadPool = new RpcRequestThreadPool("NFS", threadPoolSize, this);

			// Create the shared packet pool

			m_packetPool = new RpcPacketPool(MaxRequestSize, packetPoolSize);

			// Create the UDP handler for accepting incoming requests

			m_udpHandler = new MultiThreadedUdpRpcDatagramHandler("Nfsd", "Nfs", this, this, null, getPort(), MaxRequestSize);

			// Use the shared thread pool and packet pool

			m_udpHandler.setThreadPool(m_threadPool);
			m_udpHandler.setPacketPool(m_packetPool);

			m_udpHandler.initializeSessionHandler(this);

			// Start the UDP request listener is a seperate thread

			Thread udpThread = new Thread(m_udpHandler);
			udpThread.setName("NFS_UDP");
			udpThread.start();

			// Create the TCP handler for accepting incoming requests

			m_tcpHandler = new MultiThreadedTcpRpcSessionHandler("Nfsd", "Nfs",
					this, this, null, getPort(), MaxRequestSize);

			// Use the shared thread pool and packet pool

			m_tcpHandler.setThreadPool(m_threadPool);
			m_tcpHandler.setPacketPool(m_packetPool);

			m_tcpHandler.initializeSessionHandler(this);

			// Start the UDP request listener is a seperate thread

			Thread tcpThread = new Thread(m_tcpHandler);
			tcpThread.setName("NFS_TCP");
			tcpThread.start();

			// Register the NFS server with the portmapper

			PortMapping[] mappings = new PortMapping[2];
			mappings[0] = new PortMapping(NFS.ProgramId, NFS.VersionId, Rpc.UDP, m_udpHandler.getPort());
			mappings[1] = new PortMapping(NFS.ProgramId, NFS.VersionId, Rpc.TCP, m_tcpHandler.getPort());

			registerRPCServer(mappings);
		}
		catch (Exception ex) {
			logger.error(ex);
		}
	}

	/**
	 * Shutdown the NFS server
	 * 
	 * @param immediate
	 *            boolean
	 */
	public void shutdownServer(boolean immediate) {

		// Unregister the NFS server with the portmapper

		try {
			PortMapping[] mappings = new PortMapping[2];
			mappings[0] = new PortMapping(NFS.ProgramId, NFS.VersionId,  Rpc.UDP, m_udpHandler.getPort());
			mappings[1] = new PortMapping(NFS.ProgramId, NFS.VersionId,  Rpc.TCP, m_tcpHandler.getPort());

			unregisterRPCServer(mappings);
		}
		catch (IOException ex) {
			logger.error(ex);
		}

		// Stop the RPC handlers

		if (m_udpHandler != null) {
			m_udpHandler.closeSessionHandler(this);
			m_udpHandler = null;
		}

		if (m_tcpHandler != null) {
			m_tcpHandler.closeSessionHandler(this);
			m_tcpHandler = null;
		}

		// Stop the thread pool

		m_threadPool.shutdownThreadPool();

		// Fire a shutdown notification event

		fireServerEvent(ServerListener.ServerShutdown);
	}

	/**
	 * Process an RPC request to the NFS or mount server
	 * 
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 * @throws IOException
	 */
	public RpcPacket processRpc(RpcPacket rpc) throws IOException {

		// Dump the request data

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_DUMPDATA))
			logger.debug("NFS Req=" + rpc.toString());

		// Validate the request

		int version = rpc.getProgramVersion();

		if (rpc.getProgramId() != NFS.ProgramId) {

			// Request is not for us

			rpc.buildAcceptErrorResponse(Rpc.StsProgUnavail);
			return rpc;
		}
		else if (version != NFS.VersionId) {

			// Request is not for this version of NFS

			rpc.buildProgramMismatchResponse(NFS.VersionId, NFS.VersionId);
			return rpc;
		}

		// Find the associated session object for the request, or create a new
		// session

		NFSSrvSession nfsSess = null;

		try {

			// Find the associated session, or create a new session

			nfsSess = findSessionForRequest(rpc);
		}
		catch (RpcAuthenticationException ex) {

			// Failed to authenticate the RPC client

			rpc.buildAuthFailResponse(ex.getAuthenticationErrorCode());
			return rpc;
		}

		// Position the RPC buffer pointer at the start of the call parameters

		rpc.positionAtParameters();

		// Process the RPC request

		RpcPacket response = null;

		try
		{
			switch (rpc.getProcedureId()) {
	
			// Null request
	
			case NFS.ProcNull:
				response = procNull(nfsSess, rpc);
				break;
	
			// Get attributes request
	
			case NFS.ProcGetAttr:
				response = procGetAttr(nfsSess, rpc);
				break;
	
			// Set attributes request
	
			case NFS.ProcSetAttr:
				response = procSetAttr(nfsSess, rpc);
				break;
	
			// Lookup request
	
			case NFS.ProcLookup:
				response = procLookup(nfsSess, rpc);
				break;
	
			// Access request
	
			case NFS.ProcAccess:
				response = procAccess(nfsSess, rpc);
				break;
	
			// Read symbolic link request
	
			case NFS.ProcReadLink:
				response = procReadLink(nfsSess, rpc);
				break;
	
			// Read file request
	
			case NFS.ProcRead:
				response = procRead(nfsSess, rpc);
				break;
	
			// Write file request
	
			case NFS.ProcWrite:
				response = procWrite(nfsSess, rpc);
				break;
	
			// Create file request
	
			case NFS.ProcCreate:
				response = procCreate(nfsSess, rpc);
				break;
	
			// Create directory request
	
			case NFS.ProcMkDir:
				response = procMkDir(nfsSess, rpc);
				break;
	
			// Create symbolic link request
	
			case NFS.ProcSymLink:
				response = procSymLink(nfsSess, rpc);
				break;
	
			// Create special device request
	
			case NFS.ProcMkNode:
				response = procMkNode(nfsSess, rpc);
				break;
	
			// Delete file request
	
			case NFS.ProcRemove:
				response = procRemove(nfsSess, rpc);
				break;
	
			// Delete directory request
	
			case NFS.ProcRmDir:
				response = procRmDir(nfsSess, rpc);
				break;
	
			// Rename request
	
			case NFS.ProcRename:
				response = procRename(nfsSess, rpc);
				break;
	
			// Create hard link request
	
			case NFS.ProcLink:
				response = procLink(nfsSess, rpc);
				break;
	
			// Read directory request
	
			case NFS.ProcReadDir:
				response = procReadDir(nfsSess, rpc);
				break;
	
			// Read directory plus request
	
			case NFS.ProcReadDirPlus:
				response = procReadDirPlus(nfsSess, rpc);
				break;
	
			// Filesystem status request
	
			case NFS.ProcFsStat:
				response = procFsStat(nfsSess, rpc);
				break;
	
			// Filesystem information request
	
			case NFS.ProcFsInfo:
				response = procFsInfo(nfsSess, rpc);
				break;
	
			// Retrieve POSIX information request
	
			case NFS.ProcPathConf:
				response = procPathConf(nfsSess, rpc);
				break;
	
			// Commit request
	
			case NFS.ProcCommit:
				response = procCommit(nfsSess, rpc);
				break;
			}
	
			// Dump the response
	
			if (logger.isDebugEnabled() && hasDebugFlag(DBG_DUMPDATA))
				logger.debug("NFS Resp=" + (rpc != null ? rpc.toString() : "<Null>"));
	
	        // Commit, or rollback, any active user transaction
	        
	        try
	        {
	            // Commit or rollback the transaction
	
	            nfsSess.endTransaction();
	        }
	        catch ( Exception ex)
	        {
	            // Debug
	            
	            if ( logger.isDebugEnabled())
	                logger.debug("Error committing transaction", ex);
	        }
		}
		finally
		{
            // If there is an active transaction then roll it back
            
            if ( nfsSess.hasUserTransaction())
            {
                try
                {
                    nfsSess.getUserTransaction().rollback();
                }
                catch (Exception ex)
                {
                    logger.warn("Failed to rollback transaction", ex);
                }
            }                
		}
		
		// Return the RPC response

		return response;
	}

	/**
	 * Process the null request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procNull(NFSSrvSession sess, RpcPacket rpc) {

		// Build the response

		rpc.buildResponseHeader();
		return rpc;
	}

	/**
	 * Process the get attributes request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procGetAttr(NFSSrvSession sess, RpcPacket rpc) {

		// Get the handle from the request

		byte[] handle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(handle);

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_INFO))
			logger.debug("GetAttr request from " + rpc.getClientDetails()
					+ ", handle=" + NFSHandle.asString(handle));

		// Check if the handle is valid

		if (NFSHandle.isValid(handle) == false) {

			// Return an error status

			rpc.buildErrorResponse(NFS.StsBadHandle);
			return rpc;
		}

		// Build the response header

		rpc.buildResponseHeader();

		// Check if this is a share handle

		int shareId = -1;
		String path = null;
		int errorSts = NFS.StsSuccess;

		// Call the disk share driver to get the file information for the path

		try {

			// Get the share id and path

			shareId = getShareIdFromHandle(handle);
			TreeConnection conn = getTreeConnection(sess, shareId);

			// Check if the session has the required access to the shared
			// filesystem

			if (conn.hasReadAccess() == false)
				throw new AccessDeniedException();

			// Get the path from the handle

			path = getPathForHandle(sess, handle, conn);

			// Check if the session has the required access to the shared
			// filesystem

			if (conn.hasReadAccess() == false)
				throw new AccessDeniedException();

			// Get the disk interface from the disk driver

			DiskInterface disk = (DiskInterface) conn.getSharedDevice()
					.getInterface();

			// Get the file information for the specified path

			FileInfo finfo = disk.getFileInformation(sess, conn, path);
			if (finfo != null) {

				// Pack the file information into the NFS attributes structure

				rpc.packInt(NFS.StsSuccess);
				packAttributes3(rpc, finfo, shareId);

				// DEBUG

				if (logger.isDebugEnabled() && hasDebugFlag(DBG_INFO))
					logger.debug("GetAttr path=" + path + ", info="
							+ finfo);
			}
		} catch (BadHandleException ex) {
			errorSts = NFS.StsBadHandle;
		} catch (StaleHandleException ex) {
			errorSts = NFS.StsStale;
		} catch (AccessDeniedException ex) {
			errorSts = NFS.StsAccess;
		} catch (Exception ex) {
			errorSts = NFS.StsServerFault;

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("GetAttr Exception: " + ex.toString());
			logger.debug(ex);
		}

		// Error status

		if (errorSts != NFS.StsSuccess) {

			// Pack the error response

			rpc.buildErrorResponse(errorSts);

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("GetAttr error="
						+ NFS.getStatusString(errorSts));
		}

		// Return the attributes

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the set attributes request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procSetAttr(NFSSrvSession sess, RpcPacket rpc) {

		// Unpack the set attributes parameters

		byte[] handle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(handle);

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_INFO))
			logger.debug("SetAttr request from " + rpc.getClientDetails());

		// Check if the handle is valid

		if (NFSHandle.isValid(handle) == false) {
			rpc.buildErrorResponse(NFS.StsBadHandle);
			return rpc;
		}

		// Check if this is a share handle

		int shareId = -1;
		String path = null;
		int errorSts = NFS.StsSuccess;

		// Call the disk share driver to get the file information for the path

		try {

			// Get the share id and path

			shareId = getShareIdFromHandle(handle);
			TreeConnection conn = getTreeConnection(sess, shareId);

			// Check if the session has the required access to the shared
			// filesystem

			if (conn.hasWriteAccess() == false)
				throw new AccessDeniedException();

			// Get the path from the handle

			path = getPathForHandle(sess, handle, conn);

			// Get the disk interface from the disk driver

			DiskInterface disk = (DiskInterface) conn.getSharedDevice()
					.getInterface();

			// Get the current file information

			FileInfo oldInfo = disk.getFileInformation(sess, conn, path);

			// Get the values to be set for the file/folder

			int setFlags = 0;
			int gid = -1;
			int uid = -1;
			int mode = -1;
			long fsize = -1L;
			long atime = -1L;
			long mtime = -1L;

			// Check if the file mode has been specified

			if (rpc.unpackInt() == Rpc.True) {
				mode = rpc.unpackInt();
				setFlags += FileInfo.SetMode;
			}

			// Check if the file owner uid has been specified

			if (rpc.unpackInt() == Rpc.True) {
				uid = rpc.unpackInt();
				setFlags += FileInfo.SetUid;
			}

			// Check if the file group gid has been specified

			if (rpc.unpackInt() == Rpc.True) {
				gid = rpc.unpackInt();
				setFlags += FileInfo.SetGid;
			}

			// Check if a new file size has been specified

			if (rpc.unpackInt() == Rpc.True) {
				fsize = rpc.unpackLong();
				setFlags += FileInfo.SetFileSize;
			}

			// Check if the access date/time should be set. It may be set to a
			// client specified time
			// or using the server time

			int setTime = rpc.unpackInt();

			if (setTime == NFS.SetTimeClient) {
				atime = (long) rpc.unpackInt();
				atime *= 1000L;
				rpc.skipBytes(4); // nanoseconds
				setFlags += FileInfo.SetAccessDate;
			} else if (setTime == NFS.SetTimeServer) {
				atime = System.currentTimeMillis();
				setFlags += FileInfo.SetAccessDate;
			}

			// Check if the modify date/time should be set. It may be set to a
			// client specified time
			// or using the server time

			setTime = rpc.unpackInt();

			if (setTime == NFS.SetTimeClient) {
				mtime = (long) rpc.unpackInt();
				mtime *= 1000L;
				rpc.skipBytes(4); // nanoseconds
				setFlags += FileInfo.SetModifyDate;
			} else if (setTime == NFS.SetTimeServer) {
				mtime = System.currentTimeMillis();
				setFlags += FileInfo.SetModifyDate;
			}

			// Check if any of the file times should be updated

			if (setFlags != 0) {

				// Set the file access/modify date/times

				FileInfo finfo = new FileInfo();
				finfo.setFileInformationFlags(setFlags);

				if (atime != -1L)
					finfo.setAccessDateTime(atime);

				if (mtime != -1L)
					finfo.setModifyDateTime(mtime);

				// Check if the group id should be set

				if (gid != -1) {

					// Set the group id in the file information

					finfo.setGid(gid);
				}

				// Check if the user id should be set

				if (uid != -1) {

					// Set the user id in the file information

					finfo.setUid(uid);
				}

				// Check if the mode should be set

				if (mode != -1) {

					// Set the mode in the file information

					finfo.setMode(mode);
				}

				// Set the file information

				disk.setFileInformation(sess, conn, path, finfo);

				// DEBUG

				if (logger.isDebugEnabled() && hasDebugFlag(DBG_INFO))
					logger.debug("SetAttr handle="
							+ NFSHandle.asString(handle) + ", accessTime="
							+ finfo.getAccessDateTime() + ", modifyTime="
							+ finfo.getModifyDateTime() + ", mode=" + mode
							+ ", gid/uid=" + gid + "/" + uid);
			}

			// Check if the file size should be updated

			if (fsize != -1L) {

				// Open the file, may be cached

				NetworkFile netFile = getNetworkFileForHandle(sess, handle,
						conn, false);

				synchronized (netFile) {

					// Open the network file

					netFile.openFile(false);

					// Change the file size

					disk.truncateFile(sess, conn, netFile, fsize);

					// Close the file

					netFile.close();
				}

				// DEBUG

				if (logger.isDebugEnabled() && hasDebugFlag(DBG_INFO))
				logger.debug("SetAttr handle=" + NFSHandle.asString(handle) + ", newSize=" + fsize);
			}

			// Get the updated file information

			FileInfo newInfo = disk.getFileInformation(sess, conn, path);

			// Pack the response

			rpc.buildResponseHeader();
			rpc.packInt(NFS.StsSuccess);

			packWccData(rpc, oldInfo);
			packPostOpAttr(sess, newInfo, shareId, rpc);
		} catch (BadHandleException ex) {
			errorSts = NFS.StsBadHandle;
		} catch (StaleHandleException ex) {
			errorSts = NFS.StsStale;
		} catch (AccessDeniedException ex) {
			errorSts = NFS.StsAccess;
		} catch (DiskFullException ex) {
			errorSts = NFS.StsDQuot;
		} catch (Exception ex) {
			errorSts = NFS.StsServerFault;

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("SetAttr Exception: " + ex.toString());
		}

		// Check for a failure status

		if (errorSts != NFS.StsSuccess) {

			// Pack the error response

			rpc.buildErrorResponse(errorSts);
			packWccData(rpc, null);

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("SetAttr error="
						+ NFS.getStatusString(errorSts));
		}

		// Return a the set status

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the lookup request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procLookup(NFSSrvSession sess, RpcPacket rpc) {

		// Unpack the lookup arguments

		byte[] handle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(handle);

		String fileName = rpc.unpackString();

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_SEARCH))
			logger.debug("Lookup request from " + rpc.getClientDetails() + ", handle=" + NFSHandle.asString(handle) + ", name=" + fileName);

		// Check if the handle is valid

		if (NFSHandle.isValid(handle) == false) {
			rpc.buildErrorResponse(NFS.StsBadHandle);
			return rpc;
		}

		// Call the disk share driver to get the file information for the path

		int shareId = -1;
		String path = null;
		int errorSts = NFS.StsSuccess;

		try {

			// Get the share id and path

			shareId = getShareIdFromHandle(handle);
			TreeConnection conn = getTreeConnection(sess, shareId);

			// Check if the session has the required access to the shared
			// filesystem

			if (conn.hasReadAccess() == false)
				throw new AccessDeniedException();

			// Get the path from the handle

			path = getPathForHandle(sess, handle, conn);

			// Get the disk interface from the disk driver

			DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

			// Build the full path string

			String lookupPath = generatePath(path, fileName);

			// Check if the file/directory exists

			if (disk.fileExists(sess, conn, lookupPath) != FileStatus.NotExist) {

				// Get file information for the path

				FileInfo finfo = disk
						.getFileInformation(sess, conn, lookupPath);

				if (finfo != null) {

					// Pack the response

					rpc.buildResponseHeader();
					rpc.packInt(NFS.StsSuccess);

					// Pack the file handle

					if (finfo.isDirectory())
						NFSHandle.packDirectoryHandle(shareId, finfo.getFileId(), rpc, NFS.FileHandleSize);
					else
						NFSHandle.packFileHandle(shareId, getFileIdForHandle(handle), finfo.getFileId(), rpc, NFS.FileHandleSize);

					// Pack the file attributes

					packPostOpAttr(sess, finfo, shareId, rpc);

					// Add a cache entry for the path

					ShareDetails details = m_shareDetails.findDetails(shareId);

					details.getFileIdCache().addPath(finfo.getFileId(),
							lookupPath);

					// Check if the file path is a file name only, if so then
					// get the parent directory details

					if (pathHasDirectories(fileName) == false
							|| fileName.equals("..")) {

						// Get the parent directory file information

						FileInfo dirInfo = disk.getFileInformation(sess, conn,
								path);
						packPostOpAttr(sess, dirInfo, shareId, rpc);

						// Add the path to the file id cache, if the filesystem
						// does not support id lookups

						if (details.hasFileIdSupport() == false)
							details.getFileIdCache().addPath(dirInfo.getFileId(), path);
					}

					// DEBUG

					if (logger.isDebugEnabled() && hasDebugFlag(DBG_SEARCH))
						logger.debug("Lookup path=" + lookupPath + ", finfo=" + finfo.toString());
				}
			} else {

				// File does not exist

				errorSts = NFS.StsNoEnt;
			}
		}
		catch (BadHandleException ex) {
			errorSts = NFS.StsBadHandle;
		}
		catch (StaleHandleException ex) {
			errorSts = NFS.StsStale;
		}
		catch (AccessDeniedException ex) {
			errorSts = NFS.StsAccess;
		}
		catch (Exception ex) {
			errorSts = NFS.StsServerFault;

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Lookup Exception: " + ex.toString());
		}

		// Check if an error is being returned

		if (errorSts != NFS.StsSuccess) {

			// Pack the response

			rpc.buildErrorResponse(errorSts);
			packPostOpAttr(sess, null, shareId, rpc);

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Lookup error=" + NFS.getStatusString(errorSts));
		}

		// Return the response

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the access request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procAccess(NFSSrvSession sess, RpcPacket rpc) {

		// Get the parameters from the request

		byte[] handle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(handle);

		int accessMode = rpc.unpackInt();

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_INFO))
			logger.debug("Access request from " + rpc.getClientDetails()
					+ ", handle=" + NFSHandle.asString(handle) + ", access=0x"
					+ Integer.toHexString(accessMode));

		// Check if the handle is valid

		if (NFSHandle.isValid(handle) == false) {

			// Return an error status

			rpc.buildErrorResponse(NFS.StsBadHandle);
			return rpc;
		}

		// Check if this is a share handle

		int shareId = -1;
		String path = null;
		int errorSts = NFS.StsSuccess;

		// Call the disk share driver to get the file information for the path

		try {

			// Get the share id and path

			shareId = getShareIdFromHandle(handle);
			TreeConnection conn = getTreeConnection(sess, shareId);
			path = getPathForHandle(sess, handle, conn);

			// Get the disk interface from the disk driver

			DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

			// Get the file information for the specified path

			FileInfo finfo = disk.getFileInformation(sess, conn, path);
			if (finfo != null) {

				// Check the access that the session has to the filesystem

				int mask = 0;

				if (conn.hasWriteAccess()) {

					// Set the mask to allow all operations

					mask = NFS.AccessAll;
				}
				else if (conn.hasReadAccess()) {

					// Set the mask for read-only operations

					mask = NFS.AccessRead + NFS.AccessLookup + NFS.AccessExecute;
				}

				// Pack the response

				rpc.buildResponseHeader();
				rpc.packInt(NFS.StsSuccess);

				packPostOpAttr(sess, finfo, shareId, rpc);
				rpc.packInt(accessMode & mask);

				// DEBUG

				if (logger.isDebugEnabled() && hasDebugFlag(DBG_INFO))
					logger.debug("Access path=" + path + ", info=" + finfo);
			} else {

				// Return an error status

				errorSts = NFS.StsNoEnt;
			}
		} catch (BadHandleException ex) {
			errorSts = NFS.StsBadHandle;
		} catch (StaleHandleException ex) {
			errorSts = NFS.StsStale;
		} catch (Exception ex) {
			errorSts = NFS.StsServerFault;

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Access3 Exception: " + ex.toString());
		}

		// Check for an error status

		if (errorSts != NFS.StsSuccess) {
			rpc.buildErrorResponse(errorSts);
			packPostOpAttr(sess, null, shareId, rpc);

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Access error=" + NFS.getStatusString(errorSts));
		}

		// Return the response

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the read link request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procReadLink(NFSSrvSession sess, RpcPacket rpc) {

		// Unpack the read link arguments

		byte[] handle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(handle);

		// Check if the handle is valid

		if (NFSHandle.isValid(handle) == false) {
			rpc.buildErrorResponse(NFS.StsBadHandle);
			return rpc;
		}

		// Build the response header

		rpc.buildResponseHeader();

		// Call the disk share driver to read the symbolic link data

		int shareId = -1;
		String path = null;
		int errorSts = NFS.StsSuccess;

		try {

			// Get the share id and path

			shareId = getShareIdFromHandle(handle);

			TreeConnection conn = getTreeConnection(sess, shareId);
			path = getPathForHandle(sess, handle, conn);

			// Check if the filesystem supports symbolic links

			if ((conn.getInterface() instanceof SymbolicLinkInterface) == false) {

				// Symbolic links not supported on this filesystem

				rpc.buildErrorResponse(NFS.StsNotSupp);
				packPostOpAttr(sess, null, 0, rpc);
				packWccData(rpc, null);

				rpc.setLength();
				return rpc;
			}

			// Get the disk interface from the disk driver

			DiskInterface disk = (DiskInterface) conn.getSharedDevice()
					.getInterface();

			// Get the file information for the symbolic link

			FileInfo finfo = disk.getFileInformation(sess, conn, path);
			if (finfo != null && finfo.isFileType() == FileType.SymbolicLink) {

				// Get the symbolic link data

				SymbolicLinkInterface symLinkInterface = (SymbolicLinkInterface) disk;
				String linkData = symLinkInterface.readSymbolicLink(sess, conn,
						path);

				// Pack the read link response

				rpc.packInt(NFS.StsSuccess);
				packPostOpAttr(sess, finfo, shareId, rpc);
				rpc.packString(linkData);
			} else {

				// Return an error status, not a symbolic link

				errorSts = NFS.StsInVal;
			}

		} catch (BadHandleException ex) {
			errorSts = NFS.StsBadHandle;
		} catch (StaleHandleException ex) {
			errorSts = NFS.StsStale;
		} catch (AccessDeniedException ex) {
			errorSts = NFS.StsAccess;
		} catch (Exception ex) {
			errorSts = NFS.StsServerFault;

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("ReadLink Exception: " + ex.toString());
		}

		// Error status

		if (errorSts != NFS.StsSuccess) {

			// Pack the error response

			rpc.buildErrorResponse(errorSts);

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("ReadLink error="
						+ NFS.getStatusString(errorSts));
		}

		// Return the response

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the read file request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procRead(NFSSrvSession sess, RpcPacket rpc) {

		// Unpack the read parameters

		byte[] handle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(handle);

		long offset = rpc.unpackLong();
		int count = rpc.unpackInt();

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_FILEIO))
			logger.debug("[NFS] Read request " + rpc.getClientDetails()
					+ ", count=" + count + ", pos=" + offset);

		// Call the disk share driver to read the file

		int shareId = -1;
		NetworkFile netFile = null;
		int errorSts = NFS.StsSuccess;

		try {

			// Get the share id and associated shared device

			shareId = getShareIdFromHandle(handle);
			TreeConnection conn = getTreeConnection(sess, shareId);

			// Check if the session has the required access to the shared
			// filesystem

			if (conn.hasReadAccess() == false)
				throw new AccessDeniedException();

			// Get the network file, it may be cached

			netFile = getNetworkFileForHandle(sess, handle, conn, true);

			// Get the disk interface from the disk driver

			DiskInterface disk = (DiskInterface) conn.getSharedDevice()
					.getInterface();

			// Pack the start of the response

			rpc.buildResponseHeader();
			rpc.packInt(NFS.StsSuccess);

			// Get file information for the path and pack into the reply

			FileInfo finfo = disk.getFileInformation(sess, conn, netFile.getFullName());
			packPostOpAttr(sess, finfo, shareId, rpc);

			// Save the current position in the response buffer to fill in the
			// length and end of file flag after
			// the read.

			int bufPos = rpc.getPosition();

			// Read the network file

			int rdlen = -1;

			synchronized (netFile) {

				// Make sure the network file is open

				if (netFile.isClosed())
					netFile.openFile(false);

				// Read a block of data from the file

				rdlen = disk.readFile(sess, conn, netFile, rpc.getBuffer(),
						bufPos + 12, count, offset);
			}

			// Set the read length and end of file flag

			rpc.packInt(rdlen);
			rpc.packInt(rdlen < count ? Rpc.True : Rpc.False);
			rpc.packInt(rdlen);

			// Set the response length

			rpc.setLength(bufPos + 12 + ((rdlen + 3) & 0xFFFFFFFC));

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_FILEIO))
				logger.debug("Read fid=" + netFile.getFileId() + ", name=" + netFile.getName() + ", rdlen=" + rdlen);
		}
		catch (BadHandleException ex) {
			errorSts = NFS.StsBadHandle;
		}
		catch (StaleHandleException ex) {
			errorSts = NFS.StsStale;
		}
		catch (AccessDeniedException ex) {
			errorSts = NFS.StsAccess;
		}
		catch (Exception ex) {
			errorSts = NFS.StsServerFault;

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR)) {
				logger.debug("Read Exception: netFile=" + netFile + ", cache=" + sess.getFileCache().numberOfEntries());
				logger.debug(ex);
			}
		}

		// Check for an error status

		if (errorSts != NFS.StsSuccess) {

			// Pack the error response

			rpc.buildErrorResponse(errorSts);
			packPostOpAttr(sess, null, shareId, rpc);

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Read error=" + NFS.getStatusString(errorSts));
		}

		// Return the response

		return rpc;
	}

	/**
	 * Process the write file request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procWrite(NFSSrvSession sess, RpcPacket rpc) {

		// Unpack the read parameters

		byte[] handle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(handle);

		long offset = rpc.unpackLong();
		int count = rpc.unpackInt();
		int stable = rpc.unpackInt();

		// Skip the second write length, position at the start of the data to
		// write

		rpc.skipBytes(4);

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_FILEIO))
			logger.debug("Write request from " + rpc.getClientDetails()	+ " , count=" + count + ", offset=" + offset);

		// Call the disk share driver to write to the file

		int shareId = -1;
		String path = null;
		NetworkFile netFile = null;
		int errorSts = NFS.StsSuccess;

		try {

			// Get the share id and associated shared device

			shareId = getShareIdFromHandle(handle);
			TreeConnection conn = getTreeConnection(sess, shareId);

			// Check if the session has the required access to the shared
			// filesystem

			if (conn.hasWriteAccess() == false)
				throw new AccessDeniedException();

			// Get the network file, it may be cached

			netFile = getNetworkFileForHandle(sess, handle, conn, false);

			// Get the file path

			path = getPathForHandle(sess, handle, conn);

			// Get the disk interface from the disk driver

			DiskInterface disk = (DiskInterface) conn.getSharedDevice()
					.getInterface();

			// Check if threaded writes should be used

			FileInfo preInfo = null;

			synchronized (netFile) {

				// Make sure the network file is open

				if (netFile.isClosed())
					netFile.openFile(false);

				// Get the pre-operation file details

				preInfo = disk.getFileInformation(sess, conn, path);

				// Write to the network file

				disk.writeFile(sess, conn, netFile, rpc.getBuffer(), rpc
						.getPosition(), count, offset);
			}

			// Get file information for the path and pack the response

			FileInfo finfo = disk.getFileInformation(sess, conn, path);

			rpc.buildResponseHeader();
			rpc.packInt(NFS.StsSuccess);

			packPreOpAttr(sess, preInfo, rpc);
			packPostOpAttr(sess, finfo, shareId, rpc);

			rpc.packInt(count);
			rpc.packInt(stable);
			rpc.packLong(m_writeVerifier); // verifier

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_FILEIO))
				logger.debug("Write fid=" + netFile.getFileId()	+ ", name=" + netFile.getName() + ", wrlen=" + count);
		}
		catch (BadHandleException ex) {
			errorSts = NFS.StsBadHandle;
		}
		catch (StaleHandleException ex) {
			errorSts = NFS.StsStale;
		}
		catch (AccessDeniedException ex) {
			errorSts = NFS.StsAccess;
		}
		catch (DiskFullException ex) {
			errorSts = NFS.StsNoSpc;
		}
		catch (Exception ex) {
			errorSts = NFS.StsServerFault;

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR)) {
				logger.debug("Write Exception: netFile=" + netFile + ", cache=" + sess.getFileCache().numberOfEntries());
				logger.debug(ex);
			}
		}

		// Check for a failure status

		if (errorSts != NFS.StsSuccess) {

			// Pack the error response

			rpc.buildErrorResponse(errorSts);
			packWccData(rpc, null); // before attributes
			packWccData(rpc, null); // after attributes

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Write error="
						+ NFS.getStatusString(errorSts));
		}

		// Return the write response

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the create file request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procCreate(NFSSrvSession sess, RpcPacket rpc) {

		// Unpack the create arguments

		byte[] handle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(handle);

		String fileName = rpc.unpackString();

		int createMode = rpc.unpackInt();

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_FILE))
			logger.debug("Create request from " + rpc.getClientDetails() + ", name=" + fileName);

		// Check if the handle is valid

		if (NFSHandle.isValid(handle) == false) {
			rpc.buildErrorResponse(NFS.StsBadHandle);
			return rpc;
		}

		// Call the disk share driver to create the new file

		int shareId = -1;
		String path = null;
		int errorSts = NFS.StsSuccess;

		try {

			// Get the share id and path

			shareId = getShareIdFromHandle(handle);

			TreeConnection conn = getTreeConnection(sess, shareId);
			path = getPathForHandle(sess, handle, conn);

			// Check if the session has the required access to the shared
			// filesystem

			if (conn.hasWriteAccess() == false)
				throw new AccessDeniedException();

			// Get the disk interface from the disk driver

			DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

			// Get the pre-operation state for the parent directory

			FileInfo preInfo = disk.getFileInformation(sess, conn, path);

			// Build the full path string

			StringBuffer str = new StringBuffer();
			str.append(path);

			if (path.endsWith("\\") == false)
				str.append("\\");
			str.append(fileName);

			String filePath = str.toString();

			// Check if the file exists

			int existSts = disk.fileExists(sess, conn, filePath);
			if (existSts == FileStatus.FileExists) {
				errorSts = NFS.StsExist;
			}
			else if (existSts == FileStatus.DirectoryExists) {
				errorSts = NFS.StsIsDir;
			}
			else {

				// Get the file permissions

				int gid = -1;
				int uid = -1;
				int mode = -1;

				if (rpc.unpackInt() == Rpc.True)
					mode = rpc.unpackInt();

				if (rpc.unpackInt() == Rpc.True)
					uid = rpc.unpackInt();

				if (rpc.unpackInt() == Rpc.True)
					gid = rpc.unpackInt();

				// Create a new file

				FileOpenParams params = new FileOpenParams(filePath, FileAction.CreateNotExist, AccessMode.ReadWrite, 0, gid, uid, mode);
				NetworkFile netFile = disk.createFile(sess, conn, params);

				// DEBUG

				if (logger.isDebugEnabled() && hasDebugFlag(DBG_FILE))
					logger.debug("  Create file params=" + params);

				// Get file information for the path

				FileInfo finfo = disk.getFileInformation(sess, conn, filePath);

				if (finfo != null) {

					// Pack the response

					rpc.buildResponseHeader();
					rpc.packInt(NFS.StsSuccess);

					if (finfo.isDirectory())
						packDirectoryHandle(shareId, finfo.getFileId(), rpc);
					else
						packFileHandle(shareId, getFileIdForHandle(handle),	finfo.getFileId(), rpc);

					// Pack the file attributes

					packPostOpAttr(sess, finfo, shareId, rpc);

					// Add a cache entry for the path

					ShareDetails details = m_shareDetails.findDetails(shareId);
					details.getFileIdCache().addPath(finfo.getFileId(), filePath);

					// Add a cache entry for the network file

					sess.getFileCache().addFile(netFile, conn, sess);

					// Pack the wcc data structure for the directory

					packPreOpAttr(sess, preInfo, rpc);

					FileInfo postInfo = disk.getFileInformation(sess, conn, path);
					packPostOpAttr(sess, postInfo, shareId, rpc);

					// DEBUG

					if (logger.isDebugEnabled() && hasDebugFlag(DBG_FILE))
						logger.debug("Create path=" + filePath
								+ ", finfo=" + finfo.toString());

					// Notify change listeners that a new file has been created

					DiskDeviceContext diskCtx = (DiskDeviceContext) conn.getContext();

					if (diskCtx.hasChangeHandler())
						diskCtx.getChangeHandler().notifyFileChanged( NotifyChange.ActionAdded, filePath);
				}
			}
		}
		catch (BadHandleException ex) {
			errorSts = NFS.StsBadHandle;
		}
		catch (StaleHandleException ex) {
			errorSts = NFS.StsStale;
		}
		catch (AccessDeniedException ex) {
			errorSts = NFS.StsAccess;
		}
		catch (Exception ex) {
			errorSts = NFS.StsServerFault;

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Create Exception: " + ex.toString());
		}

		// Check for a failure status

		if (errorSts != NFS.StsSuccess) {

			// Pack the error response

			rpc.buildErrorResponse(errorSts);
			packWccData(rpc, null); // before attributes
			packWccData(rpc, null); // after attributes

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Create error="
						+ NFS.getStatusString(errorSts));
		}

		// Return the response

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the create directory request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procMkDir(NFSSrvSession sess, RpcPacket rpc) {

		// Unpack the mkdir arguments

		byte[] handle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(handle);

		String dirName = rpc.unpackString();

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_DIRECTORY))
			logger.debug("MkDir request from " + rpc.getClientDetails() + ", name=" + dirName);

		// Check if the handle is valid

		if (NFSHandle.isValid(handle) == false) {
			rpc.buildErrorResponse(NFS.StsBadHandle);
			return rpc;
		}

		// Call the disk share driver to create the new directory

		int shareId = -1;
		String path = null;
		int errorSts = NFS.StsSuccess;

		try {

			// Get the share id and path

			shareId = getShareIdFromHandle(handle);
			TreeConnection conn = getTreeConnection(sess, shareId);
			path = getPathForHandle(sess, handle, conn);

			// Check if the session has the required access to the shared
			// filesystem

			if (conn.hasWriteAccess() == false)
				throw new AccessDeniedException();

			// Get the disk interface from the disk driver

			DiskInterface disk = (DiskInterface) conn.getSharedDevice()
					.getInterface();

			// Get the pre-operation state for the parent directory

			FileInfo preInfo = disk.getFileInformation(sess, conn, path);

			// Build the full path string

			StringBuffer str = new StringBuffer();
			str.append(path);
			if (path.endsWith("\\") == false)
				str.append("\\");
			str.append(dirName);
			String dirPath = str.toString();

			// Check if the file exists

			int existSts = disk.fileExists(sess, conn, dirPath);
			if (existSts != FileStatus.NotExist) {
				errorSts = NFS.StsExist;
			} else {

				// Get the user id, group id and mode for the new directory

				int gid = -1;
				int uid = -1;
				int mode = -1;

				if (rpc.unpackInt() == Rpc.True)
					mode = rpc.unpackInt();

				if (rpc.unpackInt() == Rpc.True)
					uid = rpc.unpackInt();

				if (rpc.unpackInt() == Rpc.True)
					gid = rpc.unpackInt();

				// Directory creation parameters

				FileOpenParams params = new FileOpenParams(dirPath, FileAction.CreateNotExist, AccessMode.ReadWrite,
						FileAttribute.NTDirectory, gid, uid, mode);

				// Create a new directory

				disk.createDirectory(sess, conn, params);

				// Get file information for the new directory

				FileInfo finfo = disk.getFileInformation(sess, conn, dirPath);

				if (finfo != null) {

					// Pack the response

					rpc.buildResponseHeader();
					rpc.packInt(NFS.StsSuccess);

					packDirectoryHandle(shareId, finfo.getFileId(), rpc);

					// Pack the file attributes

					packPostOpAttr(sess, finfo, shareId, rpc);

					// Add a cache entry for the path

					ShareDetails details = m_shareDetails.findDetails(shareId);

					details.getFileIdCache().addPath(finfo.getFileId(), dirPath);

					// Pack the post operation details for the parent directory

					packWccData(rpc, preInfo);
					packPostOpAttr(sess, conn, handle, rpc);

					// Notify change listeners that a new directory has been
					// created

					DiskDeviceContext diskCtx = (DiskDeviceContext) conn
							.getContext();

					if (diskCtx.hasChangeHandler())
						diskCtx.getChangeHandler().notifyFileChanged( NotifyChange.ActionAdded, dirPath);

					// DEBUG

					if (logger.isDebugEnabled() && hasDebugFlag(DBG_DIRECTORY))
						logger.debug("Mkdir path=" + dirPath + ", finfo=" + finfo.toString());
				}
			}
		}
		catch (BadHandleException ex) {
			errorSts = NFS.StsBadHandle;
		}
		catch (StaleHandleException ex) {
			errorSts = NFS.StsStale;
		}
		catch (AccessDeniedException ex) {
			errorSts = NFS.StsAccess;
		}
		catch (Exception ex) {
			errorSts = NFS.StsServerFault;

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Mkdir Exception: " + ex.toString());
		}

		// Check for an error status

		if (errorSts != NFS.StsSuccess) {

			// Pack the error response

			rpc.buildErrorResponse(errorSts);
			packWccData(rpc, null);
			packWccData(rpc, null);

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Mkdir error=" + NFS.getStatusString(errorSts));
		}

		// Return the response

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the create symbolic link request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procSymLink(NFSSrvSession sess, RpcPacket rpc) {

		// Unpack the create symbolic link arguments

		byte[] handle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(handle);

		String fileName = rpc.unpackString();

		// Check if the handle is valid

		if (NFSHandle.isValid(handle) == false) {
			rpc.buildErrorResponse(NFS.StsBadHandle);
			return rpc;
		}

		// Call the disk share driver to create the symbolic link

		int shareId = -1;
		String path = null;
		int errorSts = NFS.StsSuccess;

		try {

			// Get the share id and path

			shareId = getShareIdFromHandle(handle);

			TreeConnection conn = getTreeConnection(sess, shareId);
			path = getPathForHandle(sess, handle, conn);

			// Check if the filesystem supports symbolic links

			if ((conn.getInterface() instanceof SymbolicLinkInterface) == false) {

				// Symbolic links not supported on this filesystem

				rpc.buildErrorResponse(NFS.StsNotSupp);
				packPostOpAttr(sess, null, 0, rpc);
				packWccData(rpc, null);

				rpc.setLength();
				return rpc;
			}

			// Check if the session has the required access to the shared
			// filesystem

			if (conn.hasWriteAccess() == false)
				throw new AccessDeniedException();

			// Get the symbolic link attributes

			int setFlags = 0;
			int gid = -1;
			int uid = -1;
			int mode = -1;
			long fsize = -1L;
			long atime = -1L;
			long mtime = -1L;

			// Check if the file mode has been specified

			if (rpc.unpackInt() == Rpc.True) {
				mode = rpc.unpackInt();
				setFlags += FileInfo.SetMode;
			}

			// Check if the file owner uid has been specified

			if (rpc.unpackInt() == Rpc.True) {
				uid = rpc.unpackInt();
				setFlags += FileInfo.SetUid;
			}

			// Check if the file group gid has been specified

			if (rpc.unpackInt() == Rpc.True) {
				gid = rpc.unpackInt();
				setFlags += FileInfo.SetGid;
			}

			// Check if a new file size has been specified

			if (rpc.unpackInt() == Rpc.True) {
				fsize = rpc.unpackLong();
				setFlags += FileInfo.SetFileSize;
			}

			// Check if the access date/time should be set. It may be set to a
			// client specified time
			// or using the server time

			int setTime = rpc.unpackInt();

			if (setTime == NFS.SetTimeClient) {
				atime = (long) rpc.unpackInt();
				atime *= 1000L;
				rpc.skipBytes(4); // nanoseconds
				setFlags += FileInfo.SetAccessDate;
			}
			else if (setTime == NFS.SetTimeServer) {
				atime = System.currentTimeMillis();
				setFlags += FileInfo.SetAccessDate;
			}

			// Check if the modify date/time should be set. It may be set to a
			// client specified time
			// or using the server time

			setTime = rpc.unpackInt();

			if (setTime == NFS.SetTimeClient) {
				mtime = (long) rpc.unpackInt();
				mtime *= 1000L;
				rpc.skipBytes(4); // nanoseconds
				setFlags += FileInfo.SetModifyDate;
			}
			else if (setTime == NFS.SetTimeServer) {
				mtime = System.currentTimeMillis();
				setFlags += FileInfo.SetModifyDate;
			}

			// Get the symbolic link name

			String linkName = rpc.unpackString();

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_FILE))
				logger.debug("Symbolic link request from " + rpc.getClientDetails() + ", name=" + fileName + ", link=" + linkName);

			// Get the disk interface from the disk driver

			DiskInterface disk = (DiskInterface) conn.getSharedDevice()
					.getInterface();

			// Get the pre-operation state for the parent directory

			FileInfo preInfo = disk.getFileInformation(sess, conn, path);

			// Build the full path string

			StringBuffer str = new StringBuffer();
			str.append(path);

			if (path.endsWith("\\") == false)
				str.append("\\");
			str.append(fileName);

			String filePath = str.toString();

			// Check if the file exists

			int existSts = disk.fileExists(sess, conn, filePath);
			if (existSts == FileStatus.FileExists) {
				errorSts = NFS.StsExist;
			}
			else if (existSts == FileStatus.DirectoryExists) {
				errorSts = NFS.StsIsDir;
			}
			else {

				// Create a new symbolic

				FileOpenParams params = new FileOpenParams(filePath, FileAction.CreateNotExist, AccessMode.ReadWrite, 0, gid, uid, mode);
				params.setSymbolicLink(linkName);

				NetworkFile netFile = disk.createFile(sess, conn, params);

				// DEBUG

				if (logger.isDebugEnabled() && hasDebugFlag(DBG_FILE))
					logger.debug("  Symbolic link params=" + params);

				// Get file information for the path

				FileInfo finfo = disk.getFileInformation(sess, conn, filePath);

				if (finfo != null) {

					// Pack the response

					rpc.buildResponseHeader();
					rpc.packInt(NFS.StsSuccess);

					packFileHandle(shareId, getFileIdForHandle(handle), finfo
							.getFileId(), rpc);

					// Pack the file attributes

					packPostOpAttr(sess, finfo, shareId, rpc);

					// Add a cache entry for the path

					ShareDetails details = m_shareDetails.findDetails(shareId);
					details.getFileIdCache().addPath(finfo.getFileId(),	filePath);

					// Add a cache entry for the network file

					sess.getFileCache().addFile(netFile, conn, sess);

					// Pack the wcc data structure for the directory

					packPreOpAttr(sess, preInfo, rpc);

					FileInfo postInfo = disk.getFileInformation(sess, conn, path);
					packPostOpAttr(sess, postInfo, shareId, rpc);

					// DEBUG

					if (logger.isDebugEnabled() && hasDebugFlag(DBG_FILE))
						logger.debug("Symbolic link path=" + filePath + ", finfo=" + finfo.toString());
				}
			}
		}
		catch (BadHandleException ex) {
			errorSts = NFS.StsBadHandle;
		}
		catch (StaleHandleException ex) {
			errorSts = NFS.StsStale;
		}
		catch (AccessDeniedException ex) {
			errorSts = NFS.StsAccess;
		}
		catch (Exception ex) {
			errorSts = NFS.StsServerFault;

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("SymbolicLink Exception: " + ex.toString());
		}

		// Error status

		if (errorSts != NFS.StsSuccess) {

			// Pack the error response

			rpc.buildErrorResponse(errorSts);

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("SymLink error="
						+ NFS.getStatusString(errorSts));
		}

		// Return the response

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the make special device request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procMkNode(NFSSrvSession sess, RpcPacket rpc) {

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_DIRECTORY))
			logger.debug("MkNode request from " + rpc.getClientDetails());

		// Return an error status

		rpc.buildErrorResponse(NFS.StsNotSupp);
		packPostOpAttr(sess, null, 0, rpc);
		packWccData(rpc, null);

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the delete file request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procRemove(NFSSrvSession sess, RpcPacket rpc) {

		// Unpack the remove arguments

		byte[] handle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(handle);

		String fileName = rpc.unpackString();

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_FILE))
			logger.debug("Remove request from " + rpc.getClientDetails() + ", name=" + fileName);

		// Call the disk share driver to delete the file

		int shareId = -1;
		String path = null;
		int errorSts = NFS.StsSuccess;

		try {

			// Get the share id and path

			shareId = getShareIdFromHandle(handle);
			ShareDetails details = m_shareDetails.findDetails(shareId);
			TreeConnection conn = getTreeConnection(sess, shareId);

			path = getPathForHandle(sess, handle, conn);

			// Check if the session has the required access to the shared
			// filesystem

			if (conn.hasWriteAccess() == false)
				throw new AccessDeniedException();

			// Get the disk interface from the disk driver

			DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

			// Get the pre-operation details for the directory

			FileInfo preInfo = disk.getFileInformation(sess, conn, path);

			// Build the full path string

			StringBuffer str = new StringBuffer();
			str.append(path);
			if (path.endsWith("\\") == false)
				str.append("\\");
			str.append(fileName);
			String delPath = str.toString();

			// Check if the file exists

			int existSts = disk.fileExists(sess, conn, delPath);
			if (existSts == FileStatus.NotExist) {
				errorSts = NFS.StsNoEnt;
			}
			else if (existSts == FileStatus.DirectoryExists) {
				errorSts = NFS.StsIsDir;
			}
			else {

				// Get the file information for the file to be deleted

				FileInfo finfo = disk.getFileInformation(sess, conn, delPath);

				// Delete the file

				disk.deleteFile(sess, conn, delPath);

				// Remove the path from the cache

				if (finfo != null)
					details.getFileIdCache().deletePath(finfo.getFileId());

				// Get the post-operation details for the directory

				FileInfo postInfo = disk.getFileInformation(sess, conn, path);

				// Pack the response

				rpc.buildResponseHeader();
				rpc.packInt(NFS.StsSuccess);

				packPreOpAttr(sess, preInfo, rpc);
				packPostOpAttr(sess, postInfo, shareId, rpc);

				// Check if there are any file/directory change notify requests
				// active

				DiskDeviceContext diskCtx = (DiskDeviceContext) conn
						.getContext();
				if (diskCtx.hasChangeHandler())
					diskCtx.getChangeHandler().notifyFileChanged( NotifyChange.ActionRemoved, delPath);
			}
		}
		catch (BadHandleException ex) {
			errorSts = NFS.StsBadHandle;
		}
		catch (StaleHandleException ex) {
			errorSts = NFS.StsStale;
		}
		catch (SecurityException ex) {
			errorSts = NFS.StsAccess;
		}
		catch (AccessDeniedException ex) {
			errorSts = NFS.StsAccess;
		}
		catch (Exception ex) {
			errorSts = NFS.StsServerFault;

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("GetAttr Exception: " + ex.toString());
		}

		// Check for an error status

		if (errorSts != NFS.StsSuccess) {

			// Pack the error response

			rpc.buildErrorResponse(errorSts);
			packWccData(rpc, null);
			packWccData(rpc, null);

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Remove error=" + NFS.getStatusString(errorSts));
		}

		// Return the remove repsonse

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the delete directory request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procRmDir(NFSSrvSession sess, RpcPacket rpc) {

		// Unpack the rmdir arguments

		byte[] handle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(handle);

		String dirName = rpc.unpackString();

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_DIRECTORY))
			logger.debug("RmDir request from " + rpc.getClientDetails() + ", name=" + dirName);

		// Check if the handle is valid

		if (NFSHandle.isValid(handle) == false) {
			rpc.buildErrorResponse(NFS.StsBadHandle);
			return rpc;
		}

		int shareId = -1;
		String path = null;
		int errorSts = NFS.StsSuccess;

		try {

			// Get the share id and path

			shareId = getShareIdFromHandle(handle);
			ShareDetails details = m_shareDetails.findDetails(shareId);
			TreeConnection conn = getTreeConnection(sess, shareId);

			// Check if the session has the required access to the shared
			// filesystem

			if (conn.hasWriteAccess() == false)
				throw new AccessDeniedException();

			// Build the pre-operation part of the response

			rpc.buildResponseHeader();
			rpc.packInt(NFS.StsSuccess);

			// Pack the pre operation attributes for the parent directory

			packPreOpAttr(sess, conn, handle, rpc);

			// Get the path to be removed

			path = getPathForHandle(sess, handle, conn);

			// Get the disk interface from the disk driver

			DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

			// Build the full path string

			StringBuffer str = new StringBuffer();
			str.append(path);

			if (path.endsWith("\\") == false)
				str.append("\\");
			str.append(dirName);

			String delPath = str.toString();

			// Check if the file exists

			int existSts = disk.fileExists(sess, conn, delPath);
			if (existSts == FileStatus.NotExist) {
				errorSts = NFS.StsNoEnt;
			}
			else if (existSts == FileStatus.FileExists) {
				errorSts = NFS.StsNoEnt;
			}
			else {

				// Get the file information for the directory to be deleted

				FileInfo finfo = disk.getFileInformation(sess, conn, delPath);

				// Delete the directory

				disk.deleteDirectory(sess, conn, delPath);

				// Remove the path from the cache

				if (finfo != null)
					details.getFileIdCache().deletePath(finfo.getFileId());

				// Pack the post operation attributes for the parent directory

				packPostOpAttr(sess, conn, handle, rpc);

				// Check if there are any file/directory change notify requests
				// active

				DiskDeviceContext diskCtx = (DiskDeviceContext) conn.getContext();
				if (diskCtx.hasChangeHandler())
					diskCtx.getChangeHandler().notifyFileChanged( NotifyChange.ActionRemoved, delPath);
			}
		}
		catch (BadHandleException ex) {
			errorSts = NFS.StsBadHandle;
		}
		catch (StaleHandleException ex) {
			errorSts = NFS.StsStale;
		}
		catch (SecurityException ex) {
			errorSts = NFS.StsAccess;
		}
		catch (AccessDeniedException ex) {
			errorSts = NFS.StsAccess;
		}
		catch (Exception ex) {
			errorSts = NFS.StsServerFault;

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Rmdir Exception: " + ex.toString());
		}

		// Check if an error status is being returned

		if (errorSts != NFS.StsSuccess) {

			// Pack the error response

			rpc.buildErrorResponse(errorSts);
			packWccData(rpc, null);
			packWccData(rpc, null);

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Rmdir error=" + NFS.getStatusString(errorSts));
		}

		// Return the response

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the rename file request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procRename(NFSSrvSession sess, RpcPacket rpc) {

		// Unpack the rename arguments

		byte[] fromHandle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(fromHandle);

		String fromName = rpc.unpackString();

		byte[] toHandle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(toHandle);

		String toName = rpc.unpackString();

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_FILE)) {
			logger.debug("Rename request from " + rpc.getClientDetails() + ", fromHandle=" + NFSHandle.asString(fromHandle)
					+ ", fromname=" + fromName);
			logger.debug("               tohandle=" + NFSHandle.asString(toHandle) + ", toname=" + toName);
		}

		// Call the disk share driver to rename the file/directory

		int shareId = -1;
		String fromPath = null;
		String toPath = null;
		int errorSts = NFS.StsSuccess;

		try {

			// Get the share id and path

			shareId = getShareIdFromHandle(fromHandle);
			ShareDetails details = m_shareDetails.findDetails(shareId);
			TreeConnection conn = getTreeConnection(sess, shareId);

			// Check if the session has the required access to the shared
			// filesystem

			if (conn.hasWriteAccess() == false)
				throw new AccessDeniedException();

			// Get paths from the handles

			fromPath = getPathForHandle(sess, fromHandle, conn);
			toPath = getPathForHandle(sess, toHandle, conn);

			// Build the full path string for the old name

			StringBuffer str = new StringBuffer();
			str.append(fromPath);

			if (fromPath.endsWith("\\") == false)
				str.append("\\");
			str.append(fromName);

			String oldPath = str.toString();

			// Build the full path string for the new name

			str.setLength(0);
			str.append(toPath);

			if (toPath.endsWith("\\") == false)
				str.append("\\");
			str.append(toName);

			String newPath = str.toString();

			// Get the disk interface from the disk driver

			DiskInterface disk = (DiskInterface) conn.getSharedDevice()
					.getInterface();

			// Get the pre-operation details for the parent directories

			FileInfo preFromInfo = disk.getFileInformation(sess, conn, fromPath);
			FileInfo preToInfo = null;

			if (NFSHandle.unpackDirectoryId(fromHandle) == NFSHandle.unpackDirectoryId(toHandle))
				preToInfo = preFromInfo;
			else
				preToInfo = disk.getFileInformation(sess, conn, toPath);

			// Check if the from path exists

			int existSts = disk.fileExists(sess, conn, oldPath);

			if (existSts == FileStatus.NotExist) {
				errorSts = NFS.StsNoEnt;
			} else {

				// DEBUG

				if (logger.isDebugEnabled() && hasDebugFlag(DBG_FILE))
					logger.debug("Rename from=" + oldPath + ", to="	+ newPath);

				// Get the file details for the file/folder being renamed

				FileInfo finfo = disk.getFileInformation(sess, conn, oldPath);

				// Rename the file/directory

				disk.renameFile(sess, conn, oldPath, newPath);

				// Remove the original path from the cache

				if (finfo != null && finfo.getFileId() != -1)
					details.getFileIdCache().deletePath(finfo.getFileId());

				// Get the file id for the new file/directory

				finfo = disk.getFileInformation(sess, conn, newPath);
				if (finfo != null)
					details.getFileIdCache().addPath(finfo.getFileId(), newPath);

				// Check if there are any file/directory change notify requests
				// active

				DiskDeviceContext diskCtx = (DiskDeviceContext) conn.getContext();
				if (diskCtx.hasChangeHandler())
					diskCtx.getChangeHandler().notifyRename(oldPath, newPath);

				// Get the post-operation details for the parent directories

				FileInfo postFromInfo = disk.getFileInformation(sess, conn,	fromPath);
				FileInfo postToInfo = null;

				if (NFSHandle.unpackDirectoryId(fromHandle) == NFSHandle.unpackDirectoryId(toHandle))
					postToInfo = postFromInfo;
				else
					postToInfo = disk.getFileInformation(sess, conn, toPath);

				// Pack the rename response

				rpc.buildResponseHeader();
				rpc.packInt(NFS.StsSuccess);

				packWccData(rpc, preFromInfo);
				packPostOpAttr(sess, postFromInfo, shareId, rpc);

				packWccData(rpc, preToInfo);
				packPostOpAttr(sess, postToInfo, shareId, rpc);
			}
		}
		catch (BadHandleException ex) {
			errorSts = NFS.StsBadHandle;
		}
		catch (StaleHandleException ex) {
			errorSts = NFS.StsStale;
		}
		catch (SecurityException ex) {
			errorSts = NFS.StsAccess;
		}
		catch (AccessDeniedException ex) {
			errorSts = NFS.StsAccess;
		}
		catch (FileExistsException ex) {
			errorSts = NFS.StsExist;
		}
		catch (Exception ex) {
			errorSts = NFS.StsServerFault;

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Rename Exception: " + ex.toString());
		}

		// Check for an error status

		if (errorSts != NFS.StsSuccess) {

			// Pack the error response

			rpc.buildErrorResponse(errorSts);

			// Pack the from dir WCC data

			packWccData(rpc, null);
			packWccData(rpc, null);

			// Pack the to dir WCC data

			packWccData(rpc, null);
			packWccData(rpc, null);

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Rename error=" + NFS.getStatusString(errorSts));
		}

		// Return the rename response

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the create hard link request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procLink(NFSSrvSession sess, RpcPacket rpc) {

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_RXDATA))
			logger.debug("Link request from " + rpc.getClientDetails());

		// Return an error status

		rpc.buildErrorResponse(NFS.StsAccess);
		packPostOpAttr(sess, null, 0, rpc);
		packWccData(rpc, null);
		packWccData(rpc, null);

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the read directory request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procReadDir(NFSSrvSession sess, RpcPacket rpc) {

		// Unpack the read directory arguments

		byte[] handle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(handle);

		long cookie = rpc.unpackLong();
		long cookieVerf = rpc.unpackLong();

		int maxCount = rpc.unpackInt();

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_SEARCH))
			logger.debug("ReadDir request from " + rpc.getClientDetails()
					+ " handle=" + NFSHandle.asString(handle) + ", count="
					+ maxCount);

		// Check if this is a share handle

		int shareId = -1;
		String path = null;

		int errorSts = NFS.StsSuccess;

		// Call the disk share driver to get the file information for the path

		try {

			// Get the share id and path

			shareId = getShareIdFromHandle(handle);
			ShareDetails details = m_shareDetails.findDetails(shareId);
			TreeConnection conn = getTreeConnection(sess, shareId);

			// Check if the session has the required access to the shared
			// filesystem

			if (conn.hasReadAccess() == false)
				throw new AccessDeniedException();

			// Get the disk interface from the disk driver

			DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

			// Get the path from the handle

			path = getPathForHandle(sess, handle, conn);

			// If the filesystem driver cannot convert file ids to relative
			// paths we need to build a relative path for
			// every file and sub-directory in the search

			StringBuffer pathBuf = null;
			int pathLen = 0;
			FileIdCache fileCache = details.getFileIdCache();

			if (details.hasFileIdSupport() == false) {

				// Allocate the buffer for building the relative paths

				pathBuf = new StringBuffer(256);
				pathBuf.append(path);
				if (path.endsWith("\\") == false)
					pathBuf.append("\\");

				// Set the length of the search path portion of the string

				pathLen = pathBuf.length();
			}

			// Build the response header

			rpc.buildResponseHeader();
			rpc.packInt(NFS.StsSuccess);

			// Get the root directory information

			FileInfo dinfo = disk.getFileInformation(sess, conn, path);
			packPostOpAttr(sess, dinfo, shareId, rpc);

			// Generate the search path

			String searchPath = generatePath(path, "*");

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_SEARCH))
				logger.debug("ReadDir searchPath=" + searchPath	+ ", cookie=" + cookie);

			// Check if this is the start of a search

			SearchContext search = null;
			long searchId = -1;

			if (cookie == 0) {

				// Start a new search, allocate a search id

				search = disk.startSearch(sess, conn, searchPath, FileAttribute.Directory + FileAttribute.Normal);

				// Allocate a search id for the new search

				searchId = sess.allocateSearchSlot(search);

				// Set the cookie verifier

				cookieVerf = dinfo.getModifyDateTime();

				// DEBUG

				if (logger.isDebugEnabled() && hasDebugFlag(DBG_SEARCH))
					logger.debug("ReadDir allocated searchId=" + searchId);
			} else {

				// Check if the cookie verifier is valid

				if (cookieVerf != 0L && cookieVerf != dinfo.getModifyDateTime())
					throw new BadCookieException();

				// Retrieve the search from the active search cache

				searchId = (cookie & COOKIE_SEARCHID_MASK) >> COOKIE_SEARCHID_SHIFT;

				// Get the active search

				search = sess.getSearchContext((int) searchId);

				// Check if the search has been closed, if so then restart the
				// search

				if (search == null) {

					// Restart the search

					search = disk.startSearch(sess, conn, searchPath, FileAttribute.Directory + FileAttribute.Normal);

					// Allocate a search id for the new search

					searchId = sess.allocateSearchSlot(search);

					// Set the cookie verifier

					cookieVerf = dinfo.getModifyDateTime();

					// DEBUG

					if (logger.isDebugEnabled() && hasDebugFlag(DBG_SEARCH))
						logger.debug("ReadDir restarted search, searchId=" + searchId);
				}

				// Check if the search is at the required restart point

				int resumeId = (int) (cookie & COOKIE_RESUMEID_MASK);
				if (search.getResumeId() != resumeId)
					search.restartAt(resumeId);
			}

			// Pack the cookie verifier

			rpc.packLong(cookieVerf);

			// Check if the search id is valid

			if (searchId == -1)
				throw new Exception("Bad search id");

			// Search id is masked into the top of the file index to make the
			// resume cookie

			long searchMask = ((long) searchId) << COOKIE_SEARCHID_SHIFT;

			// Build the return file list

			int entCnt = 0;

			// Loop until the return buffer is full or there are no more files

			FileInfo finfo = new FileInfo();

			// Check if this is the start of a search, if so then add the '.'
			// and '..' entries

			if (cookie == 0) {

				// Add the search directory details, the '.' directory

				rpc.packInt(Rpc.True);
				rpc.packLong(dinfo.getFileIdLong() + FILE_ID_OFFSET);
				rpc.packString(".");
				rpc.packLong(COOKIE_DOT_DIRECTORY);

				// Get the file information for the parent directory

				String parentPath = generatePath(path, "..");
				FileInfo parentInfo = disk.getFileInformation(sess, conn,
						parentPath);

				// Add the parent of the search directory, the '..' directory

				rpc.packInt(Rpc.True);
				rpc.packLong(parentInfo.getFileIdLong() + FILE_ID_OFFSET);
				rpc.packString("..");
				rpc.packLong(COOKIE_DOTDOT_DIRECTORY);

				// Update the entry count and current used reply buffer count

				entCnt = 2;
			}

			// Add file/sub-directory entries until there are no more entries or
			// the buffer is full

			boolean replyFull = false;

			while (entCnt++ < maxCount && replyFull == false && search.nextFileInfo(finfo)) {

				// Check if the new file entry will fit into the reply buffer
				// without exceeding the clients maximum
				// reply size

				int entryLen = READDIR_ENTRY_LENGTH + ((finfo.getFileName().length() + 3) & 0xFFFFFFFC);

				if (entryLen > rpc.getAvailableLength() || (rpc.getPosition() + entryLen > maxCount)) {
					replyFull = true;
					break;
				}

				// Fill in the entry details

				rpc.packInt(Rpc.True);
				rpc.packLong(finfo.getFileIdLong() + FILE_ID_OFFSET);
				rpc.packString(finfo.getFileName());
				rpc.packLong(search.getResumeId() + searchMask);

				// Check if the relative path should be added to the file id
				// cache

				if (details.hasFileIdSupport() == false && fileCache.findPath(finfo.getFileId()) == null) {

					// Create a relative path for the current file/sub-directory
					// and add to the file id cache

					pathBuf.setLength(pathLen);
					pathBuf.append(finfo.getFileName());

					fileCache.addPath(finfo.getFileId(), pathBuf.toString());
				}
			}

			// Indicate no more file entries in this response

			rpc.packInt(Rpc.False);

			// Check if the search is complete

			if (search.hasMoreFiles()) {

				// Indicate that there are more files to be returned

				rpc.packInt(Rpc.False);
			}
			else {

				// Set the end of search flag

				rpc.packInt(Rpc.True);

				// Close the search, release the search slot

				search.closeSearch();
				sess.deallocateSearchSlot((int) searchId);

				// DEBUG

				if (logger.isDebugEnabled() && hasDebugFlag(DBG_SEARCH))
					logger.debug("ReadDir released searchId=" + searchId);
			}

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_SEARCH))
				logger.debug("ReadDir return entries=" + entCnt + ", eof="
						+ search.hasMoreFiles());
		}
		catch (BadHandleException ex) {
			errorSts = NFS.StsBadHandle;
		}
		catch (BadCookieException ex) {
			errorSts = NFS.StsBadCookie;
		}
		catch (AccessDeniedException ex) {
			errorSts = NFS.StsAccess;
		}
		catch (Exception ex) {
			errorSts = NFS.StsServerFault;

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR)) {
				logger.debug("ReadDir Exception: " + ex.toString());
				logger.debug(ex);
			}
		}

		// Check for an error status

		if (errorSts != NFS.StsSuccess) {

			// Pack the error response

			rpc.buildErrorResponse(errorSts);
			packPostOpAttr(sess, null, shareId, rpc);

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("ReadDir error=" + NFS.getStatusString(errorSts));
		}

		// Return the read directory response

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the read directory plus request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procReadDirPlus(NFSSrvSession sess, RpcPacket rpc) {

		// Unpack the read directory arguments

		byte[] handle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(handle);

		long cookie = rpc.unpackLong();
		long cookieVerf = rpc.unpackLong();

		int maxDir = rpc.unpackInt();
		int maxCount = rpc.unpackInt();

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_SEARCH))
			logger.debug("ReadDir request from " + rpc.getClientDetails()
					+ " handle=" + NFSHandle.asString(handle) + ", dir="
					+ maxDir + ", count=" + maxCount);

		// Check if this is a share handle

		int shareId = -1;
		String path = null;

		int errorSts = NFS.StsSuccess;

		// Call the disk share driver to get the file information for the path

		try {

			// Get the share id and path

			shareId = getShareIdFromHandle(handle);
			ShareDetails details = m_shareDetails.findDetails(shareId);
			TreeConnection conn = getTreeConnection(sess, shareId);

			// Check if the session has the required access to the shared
			// filesystem

			if (conn.hasReadAccess() == false)
				throw new AccessDeniedException();

			// Get the disk interface from the disk driver

			DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

			// Get the path from the handle

			path = getPathForHandle(sess, handle, conn);

			// If the filesystem driver cannot convert file ids to relative
			// paths we need to build a relative path for
			// every file and sub-directory in the search

			StringBuffer pathBuf = null;
			int pathLen = 0;
			FileIdCache fileCache = details.getFileIdCache();

			if (details.hasFileIdSupport() == false) {

				// Allocate the buffer for building the relative paths

				pathBuf = new StringBuffer(256);
				pathBuf.append(path);
				if (path.endsWith("\\") == false)
					pathBuf.append("\\");

				// Set the length of the search path portion of the string

				pathLen = pathBuf.length();
			}

			// Build the response header

			rpc.buildResponseHeader();
			rpc.packInt(NFS.StsSuccess);

			// Get the root directory information

			FileInfo dinfo = disk.getFileInformation(sess, conn, path);
			packPostOpAttr(sess, dinfo, shareId, rpc);

			// Generate the search path

			String searchPath = generatePath(path, "*.*");

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_SEARCH))
				logger.debug("ReadDirPlus searchPath=" + searchPath
						+ ", cookie=" + cookie);

			// Check if this is the start of a search

			SearchContext search = null;
			long searchId = -1;

			if (cookie == 0L) {

				// Start a new search, allocate a search id

				search = disk.startSearch(sess, conn, searchPath, FileAttribute.Directory + FileAttribute.Normal);

				// Allocate a search id for the new search

				searchId = sess.allocateSearchSlot(search);

				// Set the cookie verifier

				cookieVerf = dinfo.getModifyDateTime();

				// DEBUG

				if (logger.isDebugEnabled() && hasDebugFlag(DBG_SEARCH))
					logger.debug("ReadDirPlus allocated searchId="
							+ searchId);
			}
			else {

				// Check if the cookie verifier is valid

				if (cookieVerf != 0L && cookieVerf != dinfo.getModifyDateTime()) {
					logger.debug("Bad cookie verifier, verf=0x"
							+ Long.toHexString(cookieVerf) + ", modTime=0x"
							+ Long.toHexString(dinfo.getModifyDateTime()));
					throw new BadCookieException();
				}

				// Retrieve the search from the active search cache

				searchId = (cookie & COOKIE_SEARCHID_MASK) >> COOKIE_SEARCHID_SHIFT;

				// Get the active search

				search = sess.getSearchContext((int) searchId);

				// Check if the search has been closed, if so then restart the
				// search

				if (search == null) {

					// Restart the search

					search = disk.startSearch(sess, conn, searchPath, FileAttribute.Directory + FileAttribute.Normal);

					// Allocate a search id for the new search

					searchId = sess.allocateSearchSlot(search);

					// Set the cookie verifier

					cookieVerf = dinfo.getModifyDateTime();

					// DEBUG

					if (logger.isDebugEnabled() && hasDebugFlag(DBG_SEARCH))
						logger.debug("ReadDirPlus restarted search, searchId="	+ searchId);
				}

				// Get the search resume id from the cookie

				int resumeId = (int) (cookie & COOKIE_RESUMEID_MASK);
				if (search != null && search.getResumeId() != resumeId)
					search.restartAt(resumeId);
			}

			// Pack the cookie verifier

			rpc.packLong(cookieVerf);

			// Check if the search id is valid

			if (searchId == -1)
				throw new Exception("Bad search id");

			// Search id is masked into the top of the file index to make the
			// resume cookie

			long searchMask = ((long) searchId) << COOKIE_SEARCHID_SHIFT;

			// Build the return file list

			int entCnt = 0;

			// Loop until the return buffer is full or there are no more files

			FileInfo finfo = new FileInfo();

			// Check if this is the start of a search, if so then add the '.'
			// and '..' entries

			if (cookie == 0) {

				// Add the search directory details, the '.' directory

				rpc.packInt(Rpc.True);
				rpc.packLong(dinfo.getFileIdLong() + FILE_ID_OFFSET);
				rpc.packString(".");
				rpc.packLong(COOKIE_DOT_DIRECTORY);

				// Fill in the file attributes

				rpc.packInt(Rpc.True);
				packAttributes3(rpc, dinfo, shareId);

				// Fill in the file handle

				packDirectoryHandle(shareId, dinfo.getFileId(), rpc);

				// Get the file information for the parent directory

				String parentPath = generatePath(path, "..");
				FileInfo parentInfo = disk.getFileInformation(sess, conn,
						parentPath);

				// Add the parent of the search directory, the '..' directory

				rpc.packInt(Rpc.True);
				rpc.packLong(parentInfo.getFileIdLong() + FILE_ID_OFFSET);
				rpc.packString("..");
				rpc.packLong(COOKIE_DOTDOT_DIRECTORY);

				// Fill in the file attributes

				rpc.packInt(Rpc.True);
				packAttributes3(rpc, parentInfo, shareId);

				// Fill in the file handle

				packDirectoryHandle(shareId, parentInfo.getFileId(), rpc);

				// Update the entry count and current used reply buffer count

				entCnt = 2;
			}

			// Pack the file entries

			boolean replyFull = false;

			while (entCnt++ < maxDir && replyFull == false && search.nextFileInfo(finfo)) {

				// Check if the new file entry will fit into the reply buffer
				// without exceeding the clients maximum
				// reply size

				int entryLen = READDIRPLUS_ENTRY_LENGTH + ((finfo.getFileName().length() + 3) & 0xFFFFFFFC);

				if (entryLen > rpc.getAvailableLength() || (rpc.getPosition() + entryLen > maxCount)) {
					replyFull = true;
					break;
				}

				// Fill in the entry details

				rpc.packInt(Rpc.True);
				rpc.packLong(finfo.getFileIdLong() + FILE_ID_OFFSET);
				rpc.packString(finfo.getFileName());
				rpc.packLong(search.getResumeId() + searchMask);

				// Fill in the file attributes

				rpc.packInt(Rpc.True);
				packAttributes3(rpc, finfo, shareId);

				// Fill in the file or directory handle

				if (finfo.isDirectory())
					packDirectoryHandle(shareId, finfo.getFileId(), rpc);
				else
					packFileHandle(shareId, dinfo.getFileId(), finfo.getFileId(), rpc);

				// Check if the relative path should be added to the file id
				// cache

				if (details.hasFileIdSupport() == false && fileCache.findPath(finfo.getFileId()) == null) {

					// Create a relative path for the current file/sub-directory
					// and add to the file id cache

					pathBuf.setLength(pathLen);
					pathBuf.append(finfo.getFileName());

					fileCache.addPath(finfo.getFileId(), pathBuf.toString());
				}

				// Reset the file type

				finfo.setFileType(FileType.RegularFile);
			}

			// Indicate that there are no more file entries in this response

			rpc.packInt(Rpc.False);

			// Check if the search is complete

			if (search.hasMoreFiles()) {

				// Indicate that there are more files to be returned

				rpc.packInt(Rpc.False);
			} else {

				// Set the end of search flag

				rpc.packInt(Rpc.True);

				// Close the search, release the search slot

				search.closeSearch();
				sess.deallocateSearchSlot((int) searchId);
			}

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_SEARCH))
				logger.debug("ReadDirPlus return entries=" + entCnt + ", eof=" + (search.hasMoreFiles() ? false : true));
		}
		catch (BadHandleException ex) {
			errorSts = NFS.StsBadHandle;
		}
		catch (BadCookieException ex) {
			errorSts = NFS.StsBadCookie;
		}
		catch (AccessDeniedException ex) {
			errorSts = NFS.StsAccess;
		}
		catch (Exception ex) {
			errorSts = NFS.StsServerFault;

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR)) {
				logger.debug("ReadDirPlus Exception: " + ex.toString());
				logger.debug(ex);
			}
		}

		// Check for an error status

		if (errorSts != NFS.StsSuccess) {

			// Pack the error response

			rpc.buildErrorResponse(errorSts);
			packPostOpAttr(sess, null, shareId, rpc);

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("ReadDir error=" + NFS.getStatusString(errorSts));
		}

		// Return the read directory plus response

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the filesystem status request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procFsStat(NFSSrvSession sess, RpcPacket rpc) {

		// Get the handle from the request

		byte[] handle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(handle);

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_INFO))
			logger.debug("FsInfo request from " + rpc.getClientDetails());

		// Call the disk share driver to get the disk size information

		int shareId = -1;
		int errorSts = NFS.StsSuccess;

		try {

			// Get the share id

			shareId = getShareIdFromHandle(handle);

			// Get the required disk driver/tree connection

			TreeConnection conn = getTreeConnection(sess, shareId);

			// Check if the session has the required access to the shared
			// filesystem

			if (conn.hasReadAccess() == false)
				throw new AccessDeniedException();

			// Get the static disk information from the context, if available

			DiskDeviceContext diskCtx = (DiskDeviceContext) conn.getContext();
			SrvDiskInfo diskInfo = diskCtx.getDiskInformation();

			// If we did not get valid disk information from the device context
			// check
			// if the driver implements the
			// disk sizing interface

			if (diskInfo == null)
				diskInfo = new SrvDiskInfo();

			// Get the disk interface from the disk driver

			DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

			// Check if the driver implements the dynamic sizing interface to
			// get
			// realtime disk size information

			if (disk instanceof DiskSizeInterface) {

				// Get the dynamic disk sizing information

				DiskSizeInterface sizeInterface = (DiskSizeInterface) disk;
				sizeInterface.getDiskInformation(diskCtx, diskInfo);
			}

			// Calculate the disk size information

			// int unitSize = diskInfo.getBlockSize() *
			// diskInfo.getBlocksPerAllocationUnit();

			// Get the file details for the root directory

			String rootPath = getPathForHandle(sess, handle, conn);
			FileInfo rootInfo = disk.getFileInformation(sess, conn, rootPath);

			// Pack the response

			rpc.buildResponseHeader();
			rpc.packInt(NFS.StsSuccess);

			packPostOpAttr(sess, rootInfo, shareId, rpc);

			// Calculate the total/free disk space in bytes

			long totalSize = ((long) diskInfo.getDiskSizeKb()) * 1024L;
			long freeSize = ((long) diskInfo.getDiskFreeSizeKb()) * 1024L;

			// Pack the total size, free size and space available to the user

			rpc.packLong(totalSize);
			rpc.packLong(freeSize);
			rpc.packLong(freeSize);

			// Total/free file slots in the file system, assume one file per 1Kb
			// of
			// space

			long totalSlots = diskInfo.getDiskSizeKb();
			long freeSlots = diskInfo.getDiskFreeSizeKb();

			// Pack the total slots, free slots and user slots available

			rpc.packLong(totalSlots);
			rpc.packLong(freeSlots);
			rpc.packLong(freeSlots);

			// Pack the number of seconds for which the file system in not
			// expected to
			// change

			rpc.packInt(0);
		}
		catch (SecurityException ex) {
			errorSts = NFS.StsAccess;
		}
		catch (AccessDeniedException ex) {
			errorSts = NFS.StsAccess;
		}
		catch (Exception ex) {
			errorSts = NFS.StsServerFault;
		}

		// Check for an error status

		if (errorSts != NFS.StsSuccess) {

			// Pack the error response

			rpc.buildErrorResponse(errorSts);
			packPostOpAttr(sess, null, shareId, rpc);

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("FsStat error=" + NFS.getStatusString(errorSts));
		}

		// Return the response

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the filesystem information request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procFsInfo(NFSSrvSession sess, RpcPacket rpc) {

		// Get the handle from the request

		byte[] handle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(handle);

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_INFO))
			logger.debug("[NFS] FsInfo request from " + rpc.getClientDetails());

		// Check if the handle is valid

		if (NFSHandle.isValid(handle) == false) {

			// Return an error status

			rpc.buildErrorResponse(NFS.StsBadHandle);
			return rpc;
		}

		// Build the response header

		rpc.buildResponseHeader();

		// Check if this is a share handle

		int shareId = -1;
		int errorSts = NFS.StsSuccess;

		// Pack the filesystem information for the filesystem

		try {

			// Get the share id and path

			shareId = getShareIdFromHandle(handle);
			TreeConnection conn = getTreeConnection(sess, shareId);

			// Check if the session has the required access to the shared
			// filesystem

			if (conn.hasReadAccess() == false)
				throw new AccessDeniedException();

			// Pack the status code and post op attributes

			rpc.packInt(NFS.StsSuccess);
			packPostOpAttr(sess, conn, handle, rpc);

			// Pack the filesystem information
			//
			// Maximum/preferred read request supported by the server

			rpc.packInt(MaxReadSize);
			rpc.packInt(PrefReadSize);
			rpc.packInt(MultReadSize);

			// Maximum/preferred write request supported by the server

			rpc.packInt(MaxWriteSize);
			rpc.packInt(PrefWriteSize);
			rpc.packInt(MultWriteSize);

			// Preferred READDIR request size

			rpc.packInt(PrefReadDirSize);

			// Maximum file size supported

			rpc.packLong(MaxFileSize);

			// Server time resolution, indicate to nearest second

			rpc.packInt(1); // seconds
			rpc.packInt(0); // nano-seconds

			// Server properties, check if the filesystem supports symbolic
			// links

			int fileSysProps = NFS.FileSysHomogeneuos + NFS.FileSysCanSetTime;
			if (conn.getInterface() instanceof SymbolicLinkInterface)
				fileSysProps += NFS.FileSysSymLink;

			rpc.packInt(fileSysProps);
		}
		catch (BadHandleException ex) {
			errorSts = NFS.StsBadHandle;
		}
		catch (StaleHandleException ex) {
			errorSts = NFS.StsStale;
		}
		catch (Exception ex) {
			errorSts = NFS.StsServerFault;

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Access Exception: " + ex.toString());
		}

		// Check for an error status

		if (errorSts != NFS.StsSuccess) {
			rpc.buildErrorResponse(errorSts);
			packPostOpAttr(sess, null, shareId, rpc);

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Access error=" + NFS.getStatusString(errorSts));
		}

		// Return the response

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the retrieve POSIX information request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procPathConf(NFSSrvSession sess, RpcPacket rpc) {

		// Unpack the pathconf arguments

		byte[] handle = new byte[NFS.FileHandleSize];
		rpc.unpackByteArrayWithLength(handle);

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_SEARCH))
			logger.debug("PathConf request from " + rpc.getClientDetails()
					+ " handle=" + NFSHandle.asString(handle));

		// Call the disk share driver to get the file information for the path

		int shareId = -1;
		String path = null;
		int errorSts = NFS.StsSuccess;

		try {

			// Get the share id and path

			shareId = getShareIdFromHandle(handle);
			TreeConnection conn = getTreeConnection(sess, shareId);

			// Check if the session has the required access to the shared
			// filesystem

			if (conn.hasReadAccess() == false)
				throw new AccessDeniedException();

			// Get the path from the handle

			path = getPathForHandle(sess, handle, conn);

			// Get the disk interface from the disk driver

			DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

			// Check if the file/directory exists

			if (disk.fileExists(sess, conn, path) != FileStatus.NotExist) {

				// Get file information for the path

				FileInfo finfo = disk.getFileInformation(sess, conn, path);

				// Build the response

				rpc.buildResponseHeader();
				rpc.packInt(NFS.StsSuccess);

				packPostOpAttr(sess, finfo, shareId, rpc);

				// Pack the filesystem options

				rpc.packInt(32767);
				rpc.packInt(255);

				rpc.packInt(Rpc.True); // truncate over size names
				rpc.packInt(Rpc.True); // chown restricted
				rpc.packInt(Rpc.True); // case insensitive
				rpc.packInt(Rpc.True); // case preserving

				// DEBUG

				if (logger.isDebugEnabled() && hasDebugFlag(DBG_SEARCH))
					logger.debug("Pathconf path=" + path + ", finfo=" + (finfo != null ? finfo.toString() : "<null>"));
			}
			else {

				// File does not exist

				errorSts = NFS.StsNoEnt;
			}
		}
		catch (BadHandleException ex) {
			errorSts = NFS.StsBadHandle;
		}
		catch (StaleHandleException ex) {
			errorSts = NFS.StsStale;
		}
		catch (AccessDeniedException ex) {
			errorSts = NFS.StsAccess;
		}
		catch (Exception ex) {
			errorSts = NFS.StsServerFault;

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Pathconf Exception: " + ex.toString());
		}

		// Check if an error is being returned

		if (errorSts != NFS.StsSuccess) {

			// Pack the error response

			rpc.buildErrorResponse(errorSts);
			packPostOpAttr(sess, null, shareId, rpc);

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_ERROR))
				logger.debug("Pathconf error=" + NFS.getStatusString(errorSts));
		}

		// Return the path information response

		rpc.setLength();
		return rpc;
	}

	/**
	 * Commit request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procCommit(NFSSrvSession sess, RpcPacket rpc) {

		// DEBUG

		if (logger.isDebugEnabled() && hasDebugFlag(DBG_FILEIO))
			logger.debug("Commit request from " + rpc.getClientDetails());

		// Pack the response

		rpc.buildResponseHeader();

		rpc.packInt(NFS.StsSuccess);
		packWccData(rpc, null);
		packPostOpAttr(sess, null, 0, rpc);

		// Pack the write verifier, indicates if the server has been restarted
		// since the file write requests

		rpc.packLong(m_writeVerifier);

		// Return the response

		rpc.setLength();
		return rpc;
	}

	/**
	 * Find, or create, the session for the specified RPC request.
	 * 
	 * @param rpc
	 *            RpcPacket
	 * @return NFSSrvSession
	 * @exception RpcAuthenticationException
	 */
	private final NFSSrvSession findSessionForRequest(RpcPacket rpc)
			throws RpcAuthenticationException {

		// Check the authentication type and search the appropriate session
		// table for an existing session

		int authType = rpc.getCredentialsType();

		// Authenticate the request

		Object sessKey = getRpcAuthenticator().authenticateRpcClient(authType, rpc);

		NFSSrvSession sess = null;

		switch (authType) {

		// Null authentication

		case AuthType.Null:
			sess = findAuthNullSession(rpc, sessKey);
			break;

		// Unix authentication

		case AuthType.Unix:
			sess = findAuthUnixSession(rpc, sessKey);
			break;
		}

		// Setup the authentication context for the request
		
		getRpcAuthenticator().setCurrentUser( sess, sess.getClientInformation());
		
		// Return the server session

		return sess;
	}

	/**
	 * Find, or create, a null authentication session for the specified request
	 * 
	 * @param rpc
	 *            RpcPacket
	 * @param sessKey
	 *            Object
	 * @return NFSSrvSession
	 */
	private final NFSSrvSession findAuthNullSession(RpcPacket rpc,
			Object sessKey) {

		// Check if the null authentication session table is valid

		NFSSrvSession sess = null;

		if (m_sessAuthNull != null) {

			// Search for the required session using the client IP address

			sess = m_sessAuthNull.findSession(sessKey);
		}
		else {

			// Allocate the null authentication session table

			m_sessAuthNull = new NFSSessionTable();
		}

		// Check if we found the required session object

		if (sess == null) {

			// Create a new session for the request

			sess = new NFSSrvSession(this, rpc.getClientAddress(), rpc.getClientPort(), rpc.getClientProtocol());
			sess.setAuthIdentifier(sessKey);

			// Get the client information from the RPC

			sess.setClientInformation(getRpcAuthenticator().getRpcClientInformation(sessKey, rpc));

			// Add the new session to the session table

			m_sessAuthNull.addSession(sess);

			// Set the session id and debug output prefix

			sess.setUniqueId("" + sessKey.hashCode());
			sess.setDebug(getConfiguration().getNFSDebug());

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_SESSION))
				logger.debug("[NFS] Added Null session " + sess.getUniqueId());
		}

		// Return the session

		return sess;
	}

	/**
	 * Find, or create, a Unix authentication session for the specified request
	 * 
	 * @param rpc
	 *            RpcPacket
	 * @param sessKey
	 *            Object
	 * @return NFSSrvSession
	 */
	private final NFSSrvSession findAuthUnixSession(RpcPacket rpc,
			Object sessKey) {

		// Check if the Unix authentication session table is valid

		NFSSrvSession sess = null;

		if (m_sessAuthUnix != null) {

			// Search for the required session using the client IP address + gid
			// + uid

			sess = m_sessAuthUnix.findSession(sessKey);
		}
		else {

			// Allocate the Unix authentication session table

			m_sessAuthUnix = new NFSSessionTable();
		}

		// Check if we found the required session object

		if (sess == null) {

			// Create a new session for the request

			sess = new NFSSrvSession(this, rpc.getClientAddress(), rpc.getClientPort(), rpc.getClientProtocol());
			sess.setAuthIdentifier(sessKey);

			// Set the session id and debug output prefix

			sess.setUniqueId("" + sessKey.hashCode());
			sess.setDebug(getConfiguration().getNFSDebug());

			// Get the client information from the RPC

			sess.setClientInformation(getRpcAuthenticator().getRpcClientInformation(sessKey, rpc));

			// Add the new session to the session table

			m_sessAuthUnix.addSession(sess);

			// DEBUG

			if (logger.isDebugEnabled() && hasDebugFlag(DBG_SESSION))
				logger.debug("[NFS] Added Unix session " + sess.getUniqueId());
		}

		// Return the session

		return sess;
	}

	/**
	 * Pack the NFS v3 file attributes structure using the file information
	 * 
	 * @param rpc
	 *            RpcPacket
	 * @param finfo
	 *            FileInfo
	 * @param fileSysId
	 *            int
	 */
	protected final void packAttributes3(RpcPacket rpc, FileInfo finfo,
			int fileSysId) {

		// Pack the NFS format file attributes

		if (finfo.isDirectory()) {

			// Pack the directory information

			rpc.packInt(NFS.FileTypeDir);
			if (finfo.hasMode())
				rpc.packInt(finfo.getMode());
			else
				rpc.packInt(MODE_DIR_DEFAULT);
		} else {

			// Pack the file information

			if (finfo.isFileType() == FileType.SymbolicLink)
				rpc.packInt(NFS.FileTypeLnk);
			else
				rpc.packInt(NFS.FileTypeReg);

			if (finfo.hasMode())
				rpc.packInt(finfo.getMode());
			else
				rpc.packInt(MODE_FILE_DEFAULT);
		}

		// Set various Unix fields

		rpc.packInt(1); // number of links

		rpc.packInt(finfo.hasUid() ? finfo.getUid() : 0);
		rpc.packInt(finfo.hasGid() ? finfo.getGid() : 0);

		// Set the size for the file

		if (finfo.isDirectory()) {

			// Pack the directory size/allocation

			rpc.packLong(512L);
			rpc.packLong(1024L);
		} else {

			// Pack the file size/allocation

			rpc.packLong(finfo.getSize());
			if (finfo.getAllocationSize() != 0)
				rpc.packLong(finfo.getAllocationSize());
			else
				rpc.packLong(finfo.getSize());
		}

		// Pack the rdev field

		rpc.packInt(0); // specdata1
		rpc.packInt(0); // specdata2

		// Pack the file id

		long fid = ((long) finfo.getFileId()) & 0x0FFFFFFFFL;
		fid += FILE_ID_OFFSET;

		rpc.packLong(fileSysId);
		rpc.packLong(fid); // fid

		// Pack the file times

		if (finfo.hasAccessDateTime()) {
			rpc.packInt((int) (finfo.getAccessDateTime() / 1000L));
			rpc.packInt(0);
		} else
			rpc.packLong(0);

		if (finfo.hasModifyDateTime()) {
			rpc.packInt((int) (finfo.getModifyDateTime() / 1000L));
			rpc.packInt(0);
		} else
			rpc.packLong(0);

		if (finfo.hasChangeDateTime()) {
			rpc.packInt((int) (finfo.getChangeDateTime() / 1000L));
			rpc.packInt(0);
		} else
			rpc.packLong(0);
	}

	/**
	 * Pack a share handle
	 * 
	 * @param shareName
	 *            String
	 * @param rpc
	 *            RpcPacket
	 */
	protected final void packShareHandle(String shareName, RpcPacket rpc) {

		// Indicate that a handle follows, pack the handle

		rpc.packInt(Rpc.True);
		NFSHandle.packShareHandle(shareName, rpc, NFS.FileHandleSize);
	}

	/**
	 * Pack a directory handle
	 * 
	 * @param shareId
	 *            int
	 * @param dirId
	 *            int
	 * @param rpc
	 *            RpcPacket
	 */
	protected final void packDirectoryHandle(int shareId, int dirId,
			RpcPacket rpc) {

		// Indicate that a handle follows, pack the handle

		rpc.packInt(Rpc.True);
		NFSHandle.packDirectoryHandle(shareId, dirId, rpc, NFS.FileHandleSize);
	}

	/**
	 * Pack a directory handle
	 * 
	 * @param shareId
	 *            int
	 * @param dirId
	 *            int
	 * @param fileId
	 *            int
	 * @param rpc
	 *            RpcPacket
	 */
	protected final void packFileHandle(int shareId, int dirId, int fileId,
			RpcPacket rpc) {

		// Indicate that a handle follows, pack the handle

		rpc.packInt(Rpc.True);
		NFSHandle.packFileHandle(shareId, dirId, fileId, rpc,
				NFS.FileHandleSize);
	}

	/**
	 * Get the share id from the specified handle
	 * 
	 * @param handle
	 *            byte[]
	 * @return int
	 * @exception BadHandleException
	 */
	protected final int getShareIdFromHandle(byte[] handle)
			throws BadHandleException {

		// Check if this is a share handle

		int shareId = NFSHandle.unpackShareId(handle);

		// Check if the share id is valid

		if (shareId == -1)
			throw new BadHandleException();

		// Return the share id

		return shareId;
	}

	/**
	 * Get the path for the specified handle
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param handle
	 *            byte[]
	 * @param tree
	 *            TreeConnection
	 * @return String
	 * @exception BadHandleException
	 * @exception StaleHandleException
	 */
	protected final String getPathForHandle(NFSSrvSession sess, byte[] handle,
			TreeConnection tree) throws BadHandleException,
			StaleHandleException {

		// Get the share details via the share id hash

		ShareDetails details = m_shareDetails
				.findDetails(getShareIdFromHandle(handle));

		// Check if this is a share handle

		String path = null;

		int dirId = -1;
		int fileId = -1;

		if (NFSHandle.isShareHandle(handle)) {

			// Use the root path

			path = "\\";
		} else if (NFSHandle.isDirectoryHandle(handle)) {

			// Get the directory id from the handle and get the associated path

			dirId = NFSHandle.unpackDirectoryId(handle);
			path = details.getFileIdCache().findPath(dirId);
		} else if (NFSHandle.isFileHandle(handle)) {

			// Get the file id from the handle and get the associated path

			fileId = NFSHandle.unpackFileId(handle);
			path = details.getFileIdCache().findPath(fileId);
		} else
			throw new BadHandleException();

		// Check if the path is valid. The path may not be valid if the server has
		// been restarted as the file id cache will not contain the required path.

		if (path == null) {

			// Check if the filesystem driver supports converting file ids to
			// paths

			if (details.hasFileIdSupport()) {

				// Get the file and directory ids from the handle

				dirId = NFSHandle.unpackDirectoryId(handle);
				fileId = NFSHandle.unpackFileId(handle);

				// If the file id is not valid the handle is to a directory, use the
				// directory id as the file id

				if (fileId == -1) {
					fileId = dirId;
					dirId = -1;
				}

				// Convert the file id to a path

				FileIdInterface fileIdInterface = (FileIdInterface) tree
						.getInterface();
				try {

					// Convert the file id to a path

					path = fileIdInterface.buildPathForFileId(sess, tree,
							dirId, fileId);

					// Add the path to the cache

					details.getFileIdCache().addPath(fileId, path);
				} catch (FileNotFoundException ex) {
				}
			} else if (NFSHandle.isDirectoryHandle(handle) && dirId == 0) {

				// Path is the root directory

				path = "\\";

				// Add an entry to the cache

				details.getFileIdCache().addPath(dirId, path);
			}
		}

		// Check if the path is valid, filesystem driver may not support converting
		// file ids to paths or the file/directory may have been deleted.

		if (path == null)
			throw new StaleHandleException();

		// Return the path

		return path;
	}

	/**
	 * Get the file id from the specified handle
	 * 
	 * @param handle
	 *            byte[]
	 * @return String
	 * @exception BadHandleException
	 */
	protected final int getFileIdForHandle(byte[] handle)
			throws BadHandleException {

		// Check the handle type

		int fileId = -1;

		if (NFSHandle.isShareHandle(handle)) {

			// Root file id

			fileId = 0;
		}
		else if (NFSHandle.isDirectoryHandle(handle)) {

			// Get the directory id from the handle

			fileId = NFSHandle.unpackDirectoryId(handle);
		}
		else if (NFSHandle.isFileHandle(handle)) {

			// Get the file id from the handle

			fileId = NFSHandle.unpackFileId(handle);
		}

		// Check if the file id is valid

		if (fileId == -1)
			throw new BadHandleException();

		// Return the file id

		return fileId;
	}

	/**
	 * Find, or open, the required network file using the file handle
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param handle
	 *            byte[]
	 * @param conn
	 *            TreeConnection
	 * @param readOnly
	 *            boolean
	 * @return NetworkFile
	 * @exception BadHandleException
	 *                If the handle is not valid
	 * @exception StaleHandleException
	 *                If the file id cannot be converted to a path
	 */
	protected final NetworkFile getNetworkFileForHandle(NFSSrvSession sess, byte[] handle, TreeConnection conn, boolean readOnly)
			throws BadHandleException, StaleHandleException {

		// Check if the handle is a file handle

		if (NFSHandle.isFileHandle(handle) == false)
			throw new BadHandleException("Not a file handle");

		// Get the file id from the handle

		int fileId = getFileIdForHandle(handle);

		// Get the per session network file cache, use this to synchronize

		NetworkFileCache fileCache = sess.getFileCache();
		NetworkFile file = null;

		synchronized (fileCache) {

			// Check the file cache, file may already be open

			file = fileCache.findFile(fileId, sess);
			if (file == null) {

				// Get the path for the file

				String path = getPathForHandle(sess, handle, conn);
				if (path == null)
					throw new StaleHandleException();

				try {

					// Get the disk interface from the connection

					DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

					// Open the network file

					FileOpenParams params = new FileOpenParams(path, FileAction.OpenIfExists, AccessMode.ReadWrite, 0);
					file = disk.openFile(sess, conn, params);

					// Add the file to the active file cache

					if (file != null)
						fileCache.addFile(file, conn, sess);
				}
				catch (AccessDeniedException ex) {
					if (hasDebug())
						logger.debug(ex);
				}
				catch (Exception ex) {
					logger.debug(ex);
				}
			}
		}

		// Return the network file

		return file;
	}

	/**
	 * Return the tree connection for the specified share index
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param shareId
	 *            int
	 * @return TreeConnection
	 * @exception BadHandleException
	 */
	protected final TreeConnection getTreeConnection(NFSSrvSession sess,
			int shareId) throws BadHandleException {

		// Get the required tree connection from the session

		TreeConnection conn = sess.findConnection(shareId);
		if (conn == null) {

			// Get a template tree connection from the global list

			TreeConnection template = m_connections.findConnection(shareId);
			if (template == null) {

				// Check if any new shares have been added and try to find the
				// required connection again

				if (checkForNewShares() > 0)
					template = m_connections.findConnection(shareId);
			}

			// Matching tree connection not found, handle is not valid

			if (template == null)
				throw new BadHandleException();

			// Check if there is an access control manager configured

			if (hasAccessControlManager()) {

				// Check if the session has access to the shared filesystem

				AccessControlManager aclMgr = getAccessControlManager();

				int sharePerm = aclMgr.checkAccessControl(sess, template
						.getSharedDevice());

				if (sharePerm == AccessControl.NoAccess) {

					// Session does not have access to the shared filesystem,
					// mount should
					// have failed or permissions may have changed.

					throw new BadHandleException();
				} else if (sharePerm == AccessControl.Default)
					sharePerm = AccessControl.ReadWrite;

				// Create a new tree connection from the template

				conn = new TreeConnection(template.getSharedDevice());
				conn.setPermission(sharePerm);

				// Add the tree connection to the active list for the session

				sess.addConnection(conn);
			}
		}

		// Return the tree connection

		return conn;
	}

	/**
	 * Pack a weak cache consistency structure
	 * 
	 * @param rpc
	 *            RpcPacket
	 * @param finfo
	 *            FileInfo
	 */
	protected final void packWccData(RpcPacket rpc, FileInfo finfo) {

		// Pack the weak cache consistency data

		if (finfo != null) {

			// Indicate that data follows

			rpc.packInt(Rpc.True);

			// Pack the file size

			if (finfo.isDirectory())
				rpc.packLong(512L);
			else
				rpc.packLong(finfo.getSize());

			// Pack the file times

			if (finfo.hasModifyDateTime()) {
				rpc.packInt((int) (finfo.getModifyDateTime() / 1000L));
				rpc.packInt(0);
			} else
				rpc.packLong(0);

			if (finfo.hasChangeDateTime()) {
				rpc.packInt((int) (finfo.getChangeDateTime() / 1000L));
				rpc.packInt(0);
			} else
				rpc.packLong(0);
		} else
			rpc.packInt(Rpc.False);
	}

	/**
	 * Check if a file path contains any directory components
	 * 
	 * @param fpath
	 *            String
	 * @return boolean
	 */
	protected final boolean pathHasDirectories(String fpath) {

		// Check if the file path is valid

		if (fpath == null || fpath.length() == 0)
			return false;

		// Check if the file path starts with a directory component

		if (fpath.startsWith("\\") || fpath.startsWith("/")
				|| fpath.startsWith(".."))
			return true;

		// Check if the file path contains directory components

		if (fpath.indexOf("\\") != -1 || fpath.indexOf("/") != -1)
			return true;

		// File path does not have any directory components

		return false;
	}

	/**
	 * Pack the pre operation weak cache consistency data for the specified
	 * file/directory
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param finfo
	 *            FileInfo
	 * @param rpc
	 *            RpcPacket
	 */
	protected final void packPreOpAttr(NFSSrvSession sess, FileInfo finfo,
			RpcPacket rpc) {

		// Pack the file information

		if (finfo != null)
			packWccData(rpc, finfo);
		else
			rpc.packInt(Rpc.False);
	}

	/**
	 * Pack the pre operation weak cache consistency data for the specified
	 * file/directory
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param conn
	 *            TreeConnection
	 * @param fhandle
	 *            byte[]
	 * @param rpc
	 *            RpcPacket
	 * @throws BadHandleException
	 * @throws StaleHandleException
	 * @throws InvalidDeviceInterfaceException
	 * @throws IOException
	 */
	protected final void packPreOpAttr(NFSSrvSession sess, TreeConnection conn,
			byte[] fhandle, RpcPacket rpc) throws BadHandleException,
			StaleHandleException, InvalidDeviceInterfaceException, IOException {

		// Get the path

		String path = getPathForHandle(sess, fhandle, conn);

		// Get the disk interface from the disk driver

		DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

		// Get the file information for the path

		FileInfo finfo = disk.getFileInformation(sess, conn, path);

		// Pack the file information

		packWccData(rpc, finfo);
	}

	/**
	 * Pack the post operation weak cache consistency data for the specified
	 * file/directory
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param conn
	 *            TreeConnection
	 * @param fhandle
	 *            byte[]
	 * @param rpc
	 *            RpcPacket
	 * @throws BadHandleException
	 * @throws StaleHandleException
	 * @throws InvalidDeviceInterfaceException
	 * @throws IOException
	 */
	protected final void packPostOpAttr(NFSSrvSession sess,
			TreeConnection conn, byte[] fhandle, RpcPacket rpc)
			throws BadHandleException, StaleHandleException,
			InvalidDeviceInterfaceException, IOException {

		// Get the path

		String path = getPathForHandle(sess, fhandle, conn);

		// Get the disk interface from the disk driver

		DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

		// Get the file information for the path

		FileInfo finfo = disk.getFileInformation(sess, conn, path);

		// Pack the file information

		if (finfo != null) {
			rpc.packInt(Rpc.True);
			packAttributes3(rpc, finfo, getShareIdFromHandle(fhandle));
		} else
			rpc.packInt(Rpc.False);
	}

	/**
	 * Pack the post operation weak cache consistency data for the specified
	 * file/directory
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param finfo
	 *            FileInfo
	 * @param fileSysId
	 *            int
	 * @param rpc
	 *            RpcPacket
	 */
	protected final void packPostOpAttr(NFSSrvSession sess, FileInfo finfo,
			int fileSysId, RpcPacket rpc) {

		// Pack the file information

		if (finfo != null) {

			// Pack the post operation attributes

			rpc.packInt(Rpc.True);
			packAttributes3(rpc, finfo, fileSysId);
		} else
			rpc.packInt(Rpc.False);
	}

	/**
	 * Generate a share relative path from the directory path and argument path.
	 * The argument path may contain the value '..' in which case the directory
	 * path will be stipped back one level.
	 * 
	 * @param dirPath
	 *            String
	 * @param argPath
	 *            String
	 * @return String
	 */
	protected final String generatePath(String dirPath, String argPath) {

		// If the argument path is '..', if so then strip the directory path
		// back a
		// level

		StringBuffer pathBuf = new StringBuffer();

		if (argPath.equals("..")) {

			// Split the path into component directories

			String[] dirs = FileName.splitAllPaths(dirPath);

			// Rebuild the path without the last directory

			pathBuf.append("\\");
			int dirCnt = dirs.length - 1;

			if (dirCnt > 0) {

				// Add the paths

				for (int i = 0; i < dirCnt; i++) {
					pathBuf.append(dirs[i]);
					pathBuf.append("\\");
				}
			}

			// Remove the trailing slash

			if (pathBuf.length() > 1)
				pathBuf.setLength(pathBuf.length() - 1);
		} else {

			// Add the share relative path

			pathBuf.append(dirPath);
			if (dirPath.endsWith("\\") == false)
				pathBuf.append("\\");
			pathBuf.append(argPath);
		}

		// Return the path

		return pathBuf.toString();
	}

	/**
	 * Check for new shared devices and add them to the share and tree
	 * connection lists
	 * 
	 * @return int
	 */
	protected final int checkForNewShares() {

		// Scan the shared device list and check for new shared devices

		SharedDeviceList shareList = getConfiguration().getShareMapper().getShareList(getConfiguration().getServerName(), null, false);
		Enumeration shares = shareList.enumerateShares();

		int newShares = 0;

		while (shares.hasMoreElements()) {

			// Get the shared device

			SharedDevice share = (SharedDevice) shares.nextElement();

			// Check if it is a disk type shared device, if so then add a connection
			// to the tree connection hash

			if (share != null && share.getType() == ShareType.DISK) {

				// Check if the filesystem driver has file id support

				boolean fileIdSupport = false;
				try {
					if (share.getInterface() instanceof FileIdInterface)
						fileIdSupport = true;
				} catch (InvalidDeviceInterfaceException ex) {
				}

				// Check if the share is already in the share/tree connection
				// lists

				if (m_shareDetails.findDetails(share.getName()) == null) {

					// Add the new share details

					m_shareDetails.addDetails(new ShareDetails(share.getName(), fileIdSupport));
					m_connections.addConnection(new TreeConnection(share));

					// Update the new share count

					newShares++;
				}
			}
		}

		// Return the count of new shares added

		return newShares;
	}

	/**
	 * Return the next session id
	 * 
	 * @return int
	 */
	protected final synchronized int getNextSessionId() {
		return m_sessId++;
	}

	/**
	 * Return the configured RPC authenticator
	 * 
	 * @return RpcAuthenticator
	 */
	protected final RpcAuthenticator getRpcAuthenticator() {
		return m_rpcAuthenticator;
	}

	/**
	 * Inform session listeners that a new session has been created
	 * 
	 * @param sess
	 *            SrvSession
	 */
	protected final void fireSessionOpened(SrvSession sess) {
		fireSessionOpenEvent(sess);
	}

	/**
	 * Inform session listeners that a session has been closed
	 * 
	 * @param sess
	 *            SrvSession
	 */
	protected final void fireSessionClosed(SrvSession sess) {
		fireSessionClosedEvent(sess);
	}
}