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
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.filesys.server.oncrpc.mount;

import java.io.*;
import java.util.*;

import org.alfresco.filesys.server.ServerListener;
import org.alfresco.filesys.server.auth.acl.AccessControl;
import org.alfresco.filesys.server.auth.acl.AccessControlManager;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.server.core.ShareType;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.server.core.SharedDeviceList;
import org.alfresco.filesys.server.filesys.DiskInterface;
import org.alfresco.filesys.server.filesys.FileInfo;
import org.alfresco.filesys.server.filesys.TreeConnection;
import org.alfresco.filesys.server.filesys.TreeConnectionHash;
import org.alfresco.filesys.server.oncrpc.PortMapping;
import org.alfresco.filesys.server.oncrpc.Rpc;
import org.alfresco.filesys.server.oncrpc.RpcAuthenticationException;
import org.alfresco.filesys.server.oncrpc.RpcAuthenticator;
import org.alfresco.filesys.server.oncrpc.RpcNetworkServer;
import org.alfresco.filesys.server.oncrpc.RpcPacket;
import org.alfresco.filesys.server.oncrpc.RpcProcessor;
import org.alfresco.filesys.server.oncrpc.TcpRpcSessionHandler;
import org.alfresco.filesys.server.oncrpc.UdpRpcDatagramHandler;
import org.alfresco.filesys.server.oncrpc.nfs.NFSHandle;
import org.alfresco.filesys.server.oncrpc.nfs.NFSSrvSession;

/**
 * Mount Server Class
 * 
 * <p>Contains the NFS mount server.
 * 
 * @author GKSpencer
 */
public class MountServer extends RpcNetworkServer implements RpcProcessor {

	// Constants
	//
	// Maximum request size to accept

	public final static int MaxRequestSize 			= 8192;

	// Unix path seperator

	public static final String UNIX_SEPERATOR 		= "/";
	public static final char UNIX_SEPERATOR_CHAR 	= '/';
	public static final String DOS_SEPERATOR 		= "\\";
	public static final char DOS_SEPERATOR_CHAR 	= '\\';

	// Incoming datagram handler for UDP requests

	private UdpRpcDatagramHandler m_udpHandler;

	// Incoming session handler for TCP requests

	private TcpRpcSessionHandler m_tcpHandler;

	// Tree connection hash

	private TreeConnectionHash m_connections;

	// List of active mounts

	private MountEntryList m_mounts;

	// Port number to listen on (UDP and TCP)

	private int m_port;

	/**
	 * Class constructor
	 * 
	 * @param config
	 *            ServerConfiguration
	 */
	public MountServer(ServerConfiguration config) {
		super("Mount", config);

		// Enable/disable debug output

		setDebug(config.hasMountServerDebug());

		// Set the port to bind the server to

		setPort(config.getMountServerPort());
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
	 * Start the mount server
	 */
	public void startServer() {

		try {

			// Create the UDP handler for accepting incoming requests

			m_udpHandler = new UdpRpcDatagramHandler("Mountd", "Mnt", this, this, null, getPort(), MaxRequestSize);
			m_udpHandler.initializeSessionHandler(this);

			// Start the UDP request listener is a seperate thread

			Thread udpThread = new Thread(m_udpHandler);
			udpThread.setName("Mountd_UDP");
			udpThread.start();

			// Create the TCP handler for accepting incoming requests

			m_tcpHandler = new TcpRpcSessionHandler("Mountd", "Mnt", this, this, null, getPort(), MaxRequestSize);
			m_tcpHandler.initializeSessionHandler(this);

			// Start the UDP request listener is a seperate thread

			Thread tcpThread = new Thread(m_tcpHandler);
			tcpThread.setName("Mountd_TCP");
			tcpThread.start();

			// Register the mount server with the portmapper

			PortMapping[] mappings = new PortMapping[4];
			mappings[0] = new PortMapping(Mount.ProgramId, Mount.VersionId1, Rpc.UDP, m_udpHandler.getPort());
			mappings[1] = new PortMapping(Mount.ProgramId, Mount.VersionId3, Rpc.UDP, m_udpHandler.getPort());
			mappings[2] = new PortMapping(Mount.ProgramId, Mount.VersionId1, Rpc.TCP, m_tcpHandler.getPort());
			mappings[3] = new PortMapping(Mount.ProgramId, Mount.VersionId3, Rpc.TCP, m_tcpHandler.getPort());

			registerRPCServer(mappings);
		}
		catch (Exception ex) {
			logger.error(ex);
		}

		// Allocate the tree connection hash list and populate with the
		// available share names

		m_connections = new TreeConnectionHash();

		SharedDeviceList shareList = getConfiguration().getShareMapper()
				.getShareList(getConfiguration().getServerName(), null, false);
		Enumeration shares = shareList.enumerateShares();

		while (shares.hasMoreElements()) {

			// Get the shared device

			SharedDevice share = (SharedDevice) shares.nextElement();

			// Check if it is a disk type shared device, if so then add a
			// connection to the tree connection hash

			if (share != null && share.getType() == ShareType.DISK)
				m_connections.addConnection(new TreeConnection(share));
		}

		// Allocate the active mount list

		m_mounts = new MountEntryList();
	}

	/**
	 * Shutdown the mount server
	 * 
	 * @param immediate
	 *            boolean
	 */
	public void shutdownServer(boolean immediate) {

		// Unregister the mount server with the portmapper

		try {
			PortMapping[] mappings = new PortMapping[4];
			mappings[0] = new PortMapping(Mount.ProgramId, Mount.VersionId1,
					Rpc.UDP, m_udpHandler.getPort());
			mappings[1] = new PortMapping(Mount.ProgramId, Mount.VersionId3,
					Rpc.UDP, m_udpHandler.getPort());
			mappings[2] = new PortMapping(Mount.ProgramId, Mount.VersionId1,
					Rpc.TCP, m_tcpHandler.getPort());
			mappings[3] = new PortMapping(Mount.ProgramId, Mount.VersionId3,
					Rpc.TCP, m_tcpHandler.getPort());

			unregisterRPCServer(mappings);
		}
		catch (IOException ex) {
			logger.debug(ex);
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

		// Fire a shutdown notification event

		fireServerEvent(ServerListener.ServerShutdown);
	}

	/**
	 * Process an RPC request
	 * 
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 * @throws IOException
	 */
	public RpcPacket processRpc(RpcPacket rpc) throws IOException {

		// Validate the request

		int version = rpc.getProgramVersion();

		if (rpc.getProgramId() != Mount.ProgramId) {

			// Request is not for us

			rpc.buildAcceptErrorResponse(Rpc.StsProgUnavail);
			return rpc;
		}
		else if (version != Mount.VersionId1 && version != Mount.VersionId3) {

			// Request is not for this version of mount

			rpc.buildProgramMismatchResponse(Mount.VersionId1, Mount.VersionId3);
			return rpc;
		}

		// Authenticate the request

		NFSSrvSession sess = null;

		try {

			// Create a temporary session for the request

			sess = createTemporarySession(rpc);
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

		if (version == Mount.VersionId1) {

			// Version 1 requests

			switch (rpc.getProcedureId()) {

			// Null request

			case Mount.ProcNull1:
				response = procNull(rpc);
				break;

			// Mount request

			case Mount.ProcMnt1:
				response = procMount(sess, rpc, version);
				break;

			// Dump request

			case Mount.ProcDump1:
				response = procDump(sess, rpc, version);
				break;

			// Unmount request

			case Mount.ProcUMnt1:
				response = procUnMount(sess, rpc, version);
				break;

			// Unmount all request

			case Mount.ProcUMntAll1:
				response = procUnMountAll(sess, rpc, version);
				break;

			// Export request

			case Mount.ProcExport1:
				response = procExport(sess, rpc, version);
				break;

			// Export all request

			case Mount.ProcExportAll1:
				response = procExportAll(sess, rpc);
				break;
			}
		} else if (version == Mount.VersionId3) {

			// Version 1 requests

			switch (rpc.getProcedureId()) {

			// Null request

			case Mount.ProcNull3:
				response = procNull(rpc);
				break;

			// Mount request

			case Mount.ProcMnt3:
				response = procMount(sess, rpc, version);
				break;

			// Dump request

			case Mount.ProcDump3:
				response = procDump(sess, rpc, version);
				break;

			// Unmount request

			case Mount.ProcUMnt3:
				response = procUnMount(sess, rpc, version);
				break;

			// Unmount all request

			case Mount.ProcUMntAll3:
				response = procUnMountAll(sess, rpc, version);
				break;

			// Export request

			case Mount.ProcExport3:
				response = procExport(sess, rpc, version);
				break;
			}
		}

		// Return the RPC response

		return response;
	}

	/**
	 * Process the null request
	 * 
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procNull(RpcPacket rpc) {

		// Build the response

		rpc.buildResponseHeader();
		return rpc;
	}

	/**
	 * Process the mount request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @param version
	 *            int
	 * @return RpcPacket
	 */
	private final RpcPacket procMount(NFSSrvSession sess, RpcPacket rpc,
			int version) {

		// Get the request parameters

		String mountPath = rpc.unpackString();

		// DEBUG

		if (logger.isDebugEnabled())
			logger.debug("[Mount] Mount request from " + rpc.getClientDetails() + " path=" + mountPath);

		// Allocate the file handle

		byte[] handle = allocateFileHandle(version);

		// Mount the path

		int sts = mountPath(sess, mountPath, handle);

		// Pack mount the response

		rpc.buildResponseHeader();

		// Pack the file handle status structure, version 1 format

		if (version == 1) {

			// Version 1 response format

			rpc.packInt(sts);
			if (sts == Mount.StsSuccess)
				rpc.packByteArray(handle);
		}
		else if (version == 3) {

			// Version 3 response format

			rpc.packInt(sts);
			if (sts == Mount.StsSuccess)
				rpc.packByteArrayWithLength(handle);

			// Create an authentication flavours array

			rpc.packIntArrayWithLength(getConfiguration().getRpcAuthenticator()
					.getRpcAuthenticationTypes());
		}

		// Return the mount response

		rpc.setLength();
		return rpc;
	}

	/**
	 * Process the dump request, return the list of active mounts
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @param version
	 *            int
	 * @return RpcPacket
	 */
	private final RpcPacket procDump(NFSSrvSession sess, RpcPacket rpc,
			int version) {

		// DEBUG

		if (logger.isDebugEnabled())
			logger.debug("[Mount] Dump request from " + rpc.getClientDetails());

		// Take a snapshot of the active mount list

		MountEntryList activeList = null;

		synchronized (m_mounts) {

			// Check if there are active mounts, if so then copy the mount list

			if (m_mounts.numberOfEntries() > 0) {
				activeList = new MountEntryList();
				for (int i = 0; i < m_mounts.numberOfEntries(); i++)
					activeList.addEntry(m_mounts.getEntryAt(i));
			}
		}

		// Build the response header

		rpc.buildResponseHeader();

		// Pack the mount list structures into the response

		if (activeList != null) {

			// Pack the active mount entry details

			for (int i = 0; i < activeList.numberOfEntries(); i++) {

				// Get the current entry

				MountEntry mntEntry = activeList.getEntryAt(i);

				rpc.packInt(Rpc.True);
				rpc.packString(mntEntry.getPath());
				rpc.packString(mntEntry.getHost());
			}
		}

		// Mark the end of the mount list and set the response length

		rpc.packInt(Rpc.False);
		rpc.setLength();

		// Return the RPC response

		return rpc;
	}

	/**
	 * Process the unmount request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @param version
	 *            int
	 * @return RpcPacket
	 */
	private final RpcPacket procUnMount(NFSSrvSession sess, RpcPacket rpc,
			int version) {

		// Get the request parameters

		String mountPath = rpc.unpackString();

		// DEBUG

		if (logger.isDebugEnabled())
			logger.debug("[Mount] UnMount request from " + rpc.getClientDetails() + " path=" + mountPath);

		// Remove the mount details from the active mount list

		m_mounts.removeEntry(mountPath, sess.getRemoteName());

		// Build the RPC response

		rpc.buildResponseHeader();

		// Return the RPC response

		return rpc;
	}

	/**
	 * Process the unmoount all request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @param version
	 *            int
	 * @return RpcPacket
	 */
	private final RpcPacket procUnMountAll(NFSSrvSession sess, RpcPacket rpc,
			int version) {

		// DEBUG

		if (logger.isDebugEnabled())
			logger.debug("[Mount] UnMountAll request from " + rpc.getClientDetails());

		// Remove all the mount details for the specified host

		m_mounts.removeHostEntries(sess.getRemoteName());

		// Build the RPC response

		rpc.buildResponseHeader();

		// Return the RPC response

		return rpc;
	}

	/**
	 * Process the export request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @param version
	 *            int
	 * @return RpcPacket
	 */
	private final RpcPacket procExport(NFSSrvSession sess, RpcPacket rpc,
			int version) {

		// DEBUG

		if (logger.isDebugEnabled())
			logger.debug("[Mount] Export request from " + rpc.getClientDetails());

		// Get the share list from the server

		SharedDeviceList shareList = sess.getServer().getShareMapper()
				.getShareList(getConfiguration().getServerName(), sess, false);

		// Check if there is an access control manager configured

		if (sess.getServer().hasAccessControlManager()) {

			// Filter the list of available shares by applying any access
			// control rules

			AccessControlManager aclMgr = sess.getServer().getAccessControlManager();

			shareList = aclMgr.filterShareList(sess, shareList);
		}

		// Build the response header

		rpc.buildResponseHeader();

		// Add the visible shares to the export list

		Enumeration enm = shareList.enumerateShares();

		while (enm.hasMoreElements()) {

			// Get the current share

			SharedDevice share = (SharedDevice) enm.nextElement();

			// Add to the list of exports if it is a disk type share

			if (share.getType() == ShareType.DISK) {

				// Pack the share details

				rpc.packInt(Rpc.True);
				rpc.packString("/" + share.getName());

				// No group information

				rpc.packInt(Rpc.False);
			}
		}

		// Mark the end of the list

		rpc.packInt(Rpc.False);
		rpc.setLength();

		// Return the response

		return rpc;
	}

	/**
	 * Process the export all request
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procExportAll(NFSSrvSession sess, RpcPacket rpc) {
		return null;
	}

	/**
	 * Create a temporary session for a request. There is no need to cache
	 * sessions in the mount server as usually only single requests are made.
	 * 
	 * @param rpc
	 *            RpcPacket
	 * @return NFSSrvSession
	 * @exception RpcAuthenticationException
	 */
	private final NFSSrvSession createTemporarySession(RpcPacket rpc)
			throws RpcAuthenticationException {

		// Authenticate the request

		RpcAuthenticator rpcAuth = getConfiguration().getRpcAuthenticator();
		Object sessKey = rpcAuth.authenticateRpcClient( rpc.getCredentialsType(), rpc);

		// Create an NFS session for the request

		NFSSrvSession nfsSess = new NFSSrvSession(this, rpc.getClientAddress(), rpc.getClientPort(), rpc.getClientProtocol());

		// Set the client information for the request

		nfsSess.setClientInformation(rpcAuth.getRpcClientInformation(sessKey, rpc));

		// Return the session

		return nfsSess;
	}

	/**
	 * Mount a path. Used by the mount server to validate a path and initialize
	 * any NFS resources
	 * 
	 * @param sess
	 *            NFSSrvSession
	 * @param path
	 *            String
	 * @param handle
	 *            byte[]
	 * @return int
	 */
	protected final int mountPath(NFSSrvSession sess, String path, byte[] handle) {

		// Debug

		if (logger.isDebugEnabled())
			logger.debug("MountPath path=" + path);

		// Parse the path into share and additional path components

		if (path.startsWith(UNIX_SEPERATOR) && path.length() >= 2) {

			// Split the path into share and any additional path

			String shareName = null;
			String extraPath = null;
			int shareId = -1;

			int pos = path.indexOf(UNIX_SEPERATOR, 1);
			if (pos != -1) {
				shareName = path.substring(1, pos);
				extraPath = path.substring(pos);
			} else {
				shareName = path.substring(1);
			}

			// Search for a share with the specified name

			SharedDevice share = null;

			try {
				share = getConfiguration().getShareMapper().findShare(
						getConfiguration().getServerName(), shareName,
						ShareType.DISK, sess, false);
			}
			catch (Exception ex) {
			}

			// Check if the share exists

			if (share != null) {

				// Check if there is an access control manager configured

				if (getConfiguration().hasAccessControlManager()) {

					// Check the access control to the shared filesystem

					AccessControlManager aclMgr = getConfiguration().getAccessControlManager();

					if (aclMgr.checkAccessControl(sess, share) == AccessControl.NoAccess) {

						// DEBUG

						if (logger.isDebugEnabled())
							logger.debug("Failed to mount path=" + path + ", access denied");

						// Return a does not exist type error

						return Mount.StsNoEnt;
					}
				}

				// The share id is the hash of the share name

				shareId = shareName.hashCode();

				// Check if there is an extra path to validate

				if (extraPath != null) {

					// Convert the path to an SMB share relative path

					extraPath = extraPath.replace(UNIX_SEPERATOR_CHAR, DOS_SEPERATOR_CHAR);
					if (extraPath.endsWith(DOS_SEPERATOR))
						extraPath = extraPath.substring(0, extraPath.length() - 2);

					try {

						// Get the disk shared device

						TreeConnection conn = m_connections.findConnection(shareId);
						DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

						// Validate the path

						FileInfo finfo = disk.getFileInformation(sess, conn, extraPath);

						if (finfo == null)
							return Mount.StsNoEnt;
						else if (finfo.isDirectory() == false)
							return Mount.StsNotDir;

						// Fill in the handle for the directory

						NFSHandle.packDirectoryHandle(shareId, finfo.getFileId(), handle);
					}
					catch (Exception ex) {
					}
				}
				else {

					// Fill in the handle using a share type handle

					NFSHandle.packShareHandle(share.getName(), handle);
				}

				// Add a new entry to the active mount list

				m_mounts.addEntry(new MountEntry(sess.getRemoteName(), path));

				// DEBUG

				if (logger.isDebugEnabled())
					logger.debug("Mounted path=" + path + ", handle=" + NFSHandle.asString(handle));

				// Return a success status

				return Mount.StsSuccess;
			} else {

				// DEBUG

				if (logger.isDebugEnabled())
					logger.debug("Failed to mount path=" + path);

				// Indicate that the share does not exist

				return Mount.StsNoEnt;
			}
		}

		// Return an invalid path error

		return Mount.StsNoEnt;
	}

	/**
	 * Allocate a buffer for a file handle, the size depends on the RPC version
	 * 
	 * @param version
	 *            int
	 * @return byte[]
	 */
	private final byte[] allocateFileHandle(int version) {
		byte[] handle = null;
		if (version == 1)
			handle = new byte[Mount.FileHandleSize1];
		else if (version == 3)
			handle = new byte[Mount.FileHandleSize3];
		return handle;
	}
}
