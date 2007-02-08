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
package org.alfresco.filesys.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import javax.transaction.UserTransaction;

import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.auth.ClientInfo;
import org.alfresco.filesys.server.auth.acl.AccessControl;
import org.alfresco.filesys.server.auth.acl.AccessControlManager;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.server.core.SharedDeviceList;
import org.alfresco.filesys.server.filesys.AccessDeniedException;
import org.alfresco.filesys.server.filesys.AccessMode;
import org.alfresco.filesys.server.filesys.DiskDeviceContext;
import org.alfresco.filesys.server.filesys.DiskFullException;
import org.alfresco.filesys.server.filesys.DiskInterface;
import org.alfresco.filesys.server.filesys.DiskSharedDevice;
import org.alfresco.filesys.server.filesys.FileAction;
import org.alfresco.filesys.server.filesys.FileAttribute;
import org.alfresco.filesys.server.filesys.FileInfo;
import org.alfresco.filesys.server.filesys.FileOpenParams;
import org.alfresco.filesys.server.filesys.FileStatus;
import org.alfresco.filesys.server.filesys.FileType;
import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.server.filesys.NotifyChange;
import org.alfresco.filesys.server.filesys.SearchContext;
import org.alfresco.filesys.server.filesys.SrvDiskInfo;
import org.alfresco.filesys.server.filesys.TreeConnection;
import org.alfresco.filesys.server.filesys.TreeConnectionHash;
import org.alfresco.filesys.smb.server.repo.ContentContext;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * FTP Server Session Class
 * 
 * @author GKSpencer
 */
public class FTPSrvSession extends SrvSession implements Runnable
{

    // Debug logging

    private static final Log logger = LogFactory.getLog("org.alfresco.ftp.protocol");

    // Constants
    //
    // Debug flag values

    public static final int DBG_STATE = 0x00000001; // Session state changes

    public static final int DBG_SEARCH = 0x00000002; // File/directory search

    public static final int DBG_INFO = 0x00000004; // Information requests

    public static final int DBG_FILE = 0x00000008; // File open/close/info

    public static final int DBG_FILEIO = 0x00000010; // File read/write

    public static final int DBG_ERROR = 0x00000020; // Errors

    public static final int DBG_PKTTYPE = 0x00000040; // Received packet type

    public static final int DBG_TIMING = 0x00000080; // Time packet

    // processing

    public static final int DBG_DATAPORT = 0x00000100; // Data port

    public static final int DBG_DIRECTORY = 0x00000200; // Directory commands

    //  Enabled features
    
    protected static boolean FeatureUTF8   = false;
    protected static boolean FeatureMDTM   = true;
    protected static boolean FeatureSIZE   = true;
    protected static boolean FeatureMLST   = true;
    
    // Anonymous user name

    private static final String USER_ANONYMOUS = "anonymous";

    // Root directory and FTP directory seperator

    private static final String ROOT_DIRECTORY = "/";

    private static final String FTP_SEPERATOR = "/";

    private static final char FTP_SEPERATOR_CHAR = '/';

    // Share relative path directory seperator

    private static final String DIR_SEPERATOR = "\\";

    private static final char DIR_SEPERATOR_CHAR = '\\';

    // File transfer buffer size

    public static final int DEFAULT_BUFFERSIZE = 64000;

    // Carriage return/line feed combination required for response messages

    protected final static String CRLF = "\r\n";

    // LIST command options

    protected final static String LIST_OPTION_PREFIX = "-";

    protected final static char LIST_OPTION_HIDDEN   = 'a';

    //  Machine listing fact ids
    
    protected static final int MLST_SIZE      = 0x0001;
    protected static final int MLST_MODIFY    = 0x0002;
    protected static final int MLST_CREATE    = 0x0004;
    protected static final int MLST_TYPE      = 0x0008;
    protected static final int MLST_UNIQUE    = 0x0010;
    protected static final int MLST_PERM      = 0x0020;
    protected static final int MLST_MEDIATYPE = 0x0040;
    
    //  Default fact list to use for machine listing commands
    
    protected static final int MLST_DEFAULT   = MLST_SIZE + MLST_MODIFY + MLST_CREATE + MLST_TYPE + MLST_UNIQUE + MLST_PERM + MLST_MEDIATYPE;
    
    //  Machine listing fact names
    
    protected static final String _factNames[] = { "size", "modify", "create", "type", "unique", "perm", "media-type" };
    
    //  MLSD buffer size to allocate
    
    protected static final int MLSD_BUFFER_SIZE   = 4096;
    
    //  Modify date/time minimum date/time argument length
    
    protected static final int MDTM_DATETIME_MINLEN = 14; // YYYYMMDDHHMMSS
    
    // Flag to control whether data transfers use a seperate thread
    
    private static boolean UseThreadedDataTransfer = false;
    
    // Session socket

    private Socket m_sock;

    // Input/output streams to remote client

    private InputStream m_in;
    private byte[] m_inbuf;

    private OutputStreamWriter m_out;
    private StringBuffer m_outbuf;

    // Data connection

    private FTPDataSession m_dataSess;

    // Current working directory details
    //
    // First level is the share name then a path relative to the share root

    private FTPPath m_cwd;

    // Binary mode flag

    private boolean m_binary = false;

    // Restart position for binary file transfer

    private long m_restartPos = 0;

    //  Flag to indicate if UTF-8 paths are enabled
    
    private boolean m_utf8Paths = false;
    
    //  Machine listing fact list
    
    private int m_mlstFacts = MLST_DEFAULT;
    
    // Rename from path details

    private FTPPath m_renameFrom;

    // Filtered list of shared filesystems available to this session

    private SharedDeviceList m_shares;

    // List of shared device connections used by this session

    private TreeConnectionHash m_connections;

    /**
     * Static initializer
     */
    static
    {
    	try
    	{
        	// Check if the sun.text classes are available for UTF-8 conversion
        	
    		Class.forName( "sun.text.Normalizer");
    		
    		// Enable UTF-8 support
    		
    		FeatureUTF8 = true;
    	}
    	catch ( Exception ex)
    	{
    	}
    }
    
    /**
     * Class constructor
     * 
     * @param sock
     *            Socket
     * @param srv
     *            FTPServer
     */
    public FTPSrvSession(Socket sock, FTPNetworkServer srv)
    {
        super(-1, srv, "FTP", null);

        // Save the local socket

        m_sock = sock;

        // Set the socket linger options, so the socket closes immediately when
        // closed

        try
        {
            m_sock.setSoLinger(false, 0);
        }
        catch (SocketException ex)
        {
        }

        // Indicate that the user is not logged in

        setLoggedOn(false);

        // Allocate the FTP path

        m_cwd = new FTPPath();

        // Allocate the tree connection cache

        m_connections = new TreeConnectionHash();
    }

    /**
     * Close the FTP session, and associated data socket if active
     */
    public final void closeSession()
    {

        // Call the base class

        super.closeSession();

        // Close the data connection, if active

        if (m_dataSess != null)
        {
        	// Abort any active transfer
        	
        	m_dataSess.abortTransfer();
        	
        	// Remove the data session
        	
            getFTPServer().releaseDataSession(m_dataSess);
            m_dataSess = null;
        }

        // Close the socket first, if the client is still connected this should
        // allow the input/output streams to be closed

        if (m_sock != null)
        {
            try
            {
                m_sock.close();
            }
            catch (Exception ex)
            {
            }
            m_sock = null;
        }

        // Close the input/output streams

        if (m_in != null)
        {
            try
            {
                m_in.close();
            }
            catch (Exception ex)
            {
            }
            m_in = null;
        }

        if (m_out != null)
        {
            try
            {
                m_out.close();
            }
            catch (Exception ex)
            {
            }
            m_out = null;
        }

        // Remove session from server session list

        getFTPServer().removeSession(this);

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug(DBG_STATE))
            logger.debug("Session closed, " + getSessionId());
    }

    /**
     * Return the current working directory
     * 
     * @return String
     */
    public final String getCurrentWorkingDirectory()
    {
        return m_cwd.getFTPPath();
    }

    /**
     * Return the server that this session is associated with.
     * 
     * @return FTPServer
     */
    public final FTPNetworkServer getFTPServer()
    {
        return (FTPNetworkServer) getServer();
    }

    /**
     * Return the client network address
     * 
     * @return InetAddress
     */
    public final InetAddress getRemoteAddress()
    {
        return m_sock.getInetAddress();
    }

    /**
     * Check if there is a current working directory
     * 
     * @return boolean
     */
    public final boolean hasCurrentWorkingDirectory()
    {
        return m_cwd != null ? true : false;
    }

    /**
     * Check if UTF-8 filenames are enabled
     * 
     * @return boolean
     */
    public final boolean isUTF8Enabled()
    {
    	return m_utf8Paths;
    }
    
    /**
     * Set the default path for the session
     * 
     * @param rootPath FTPPath
     */
    public final void setRootPath(FTPPath rootPath)
    {

        // Initialize the current working directory using the root path

        m_cwd = new FTPPath(rootPath);
        if ( rootPath.hasSharedDevice())
            m_cwd.setSharedDevice( rootPath.getSharedDevice());
        else
            m_cwd.setSharedDevice(getShareList(), this);
    }

    /**
     * Get the path details for the current request
     * 
     * @param req FTPRequest
     * @param filePath boolean
     * @return FTPPath
     */
    protected final FTPPath generatePathForRequest(FTPRequest req, boolean filePath)
    {
        return generatePathForRequest(req, filePath, true);
    }

    /**
     * Get the path details for the current request
     * 
     * @param req FTPRequest
     * @param filePath boolean
     * @param checkExists boolean
     * @return FTPPath
     */
    protected final FTPPath generatePathForRequest(FTPRequest req, boolean filePath, boolean checkExists)
    {
        // Use the global share list for normal connections and the per session list for guest access
        
        SharedDeviceList shareList = null;
        
        if ( getClientInformation().isGuest())
            shareList = getDynamicShareList();
        else
            shareList = getShareList();
        
        // Convert the path to an FTP format path

        String path = convertToFTPSeperators( req.getArgument());

        // Check if the path is the root directory and there is a default root
        // path configured

        FTPPath ftpPath = null;

        if (path.compareTo(ROOT_DIRECTORY) == 0)
        {

            // Check if the FTP server has a default root directory configured

            FTPNetworkServer ftpSrv = (FTPNetworkServer) getServer();
            if (ftpSrv.hasRootPath())
                ftpPath = ftpSrv.getRootPath();
            else
            {
                try
                {
                    ftpPath = new FTPPath("/");
                }
                catch (Exception ex)
                {
                }
                return ftpPath;
            }
        }

        // Check if the path is relative

        else if (FTPPath.isRelativePath(path) == false)
        {

            // Create a new path for the directory

            try
            {
                ftpPath = new FTPPath(path);
            }
            catch (InvalidPathException ex)
            {
                return null;
            }

            // Find the associated shared device

            if (ftpPath.setSharedDevice( shareList, this) == false)
                return null;
        }
        else
        {

            // Check for the special '.' directory, just return the current
            // working directory

            if (path.equals("."))
                return m_cwd;

            // Check for the special '..' directory, if already at the root
            // directory return an
            // error

            if (path.equals(".."))
            {

                // Check if we are already at the root path

                if (m_cwd.isRootPath() == false)
                {

                    // Remove the last directory from the path

                    m_cwd.removeDirectory();
                    m_cwd.setSharedDevice( shareList, this);
                    return m_cwd;
                }
                else
                    return null;
            }

            // Create a copy of the current working directory and append the new
            // file/directory name

            ftpPath = new FTPPath(m_cwd);

            // Check if the root directory/share has been set

            if (ftpPath.isRootPath())
            {

                // Path specifies the share name

                try
                {
                    ftpPath.setSharePath(path, null);
                }
                catch (InvalidPathException ex)
                {
                    return null;
                }
            }
            else
            {
                if (filePath)
                    ftpPath.addFile(path);
                else
                    ftpPath.addDirectory(path);
            }

            // Find the associated shared device, if not already set

            if (ftpPath.hasSharedDevice() == false && ftpPath.setSharedDevice( shareList, this) == false)
                return null;
        }

        // Check if the generated path exists

        if (checkExists)
        {

            // Check if the new path exists and is a directory

            DiskInterface disk = null;
            TreeConnection tree = null;

            try
            {

                // Create a temporary tree connection

                tree = getTreeConnection(ftpPath.getSharedDevice());

                // Access the virtual filesystem driver

                disk = (DiskInterface) ftpPath.getSharedDevice().getInterface();

                // Check if the path exists

                int sts = disk.fileExists(this, tree, ftpPath.getSharePath());

                if (sts == FileStatus.NotExist)
                {

                    // Get the path string, check if there is a leading
                    // seperator

                    String pathStr = req.getArgument();
                    if (pathStr.startsWith(FTP_SEPERATOR) == false)
                        pathStr = FTP_SEPERATOR + pathStr;

                    // Create the root path

                    ftpPath = new FTPPath(pathStr);

                    // Find the associated shared device

                    if (ftpPath.setSharedDevice(getShareList(), this) == false)
                        ftpPath = null;
                    else
                    {
                        // Recheck if the path exists
                        
                        sts = disk.fileExists(this, tree, ftpPath.getSharePath());
                        if ( sts == FileStatus.NotExist)
                            ftpPath = null;
                    }
                }
                else if ((sts == FileStatus.FileExists && filePath == false)
                        || (sts == FileStatus.DirectoryExists && filePath == true))
                {

                    // Path exists but is the wrong type (directory or file)

                    ftpPath = null;
                }
            }
            catch (Exception ex)
            {
                // DEBUG
                
                if ( logger.isErrorEnabled())
                    logger.error("Error generating FTP path", ex);
                
                ftpPath = null;
            }
        }

        // Return the new path

        return ftpPath;
    }

    /**
     * Convert a path string from share path seperators to FTP path seperators
     * 
     * @param path String
     * @return String
     */
    protected final String convertToFTPSeperators(String path)
    {

        // Check if the path is valid

        if (path == null || path.indexOf(DIR_SEPERATOR) == -1)
            return path;

        // Replace the path seperators

        return path.replace(DIR_SEPERATOR_CHAR, FTP_SEPERATOR_CHAR);
    }

    /**
     * Find the required disk shared device
     * 
     * @param name String
     * @return DiskSharedDevice
     */
    protected final DiskSharedDevice findShare(String name)
    {

        // Check if the name is valid

        if (name == null)
            return null;

        // Find the required disk share

        SharedDevice shr = getFTPServer().getShareList().findShare(m_cwd.getShareName());

        if (shr != null && shr instanceof DiskSharedDevice)
            return (DiskSharedDevice) shr;

        // Disk share not found

        return null;
    }

    /**
     * Set the binary mode flag
     * 
     * @param bin boolean
     */
    protected final void setBinary(boolean bin)
    {
        m_binary = bin;
    }

    /**
     * Send an FTP command response
     * 
     * @param stsCode int
     * @param msg String
     * @exception IOException
     */
    protected final void sendFTPResponse(int stsCode, String msg) throws IOException
    {

        // Build the output record

        m_outbuf.setLength(0);
        m_outbuf.append(stsCode);
        m_outbuf.append(" ");

        if (msg != null)
            m_outbuf.append(msg);

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug(DBG_ERROR) && stsCode >= 500)
            logger.debug("Error status=" + stsCode + ", msg=" + msg);

        // Add the CR/LF

        m_outbuf.append(CRLF);

        // Output the FTP response

        if (m_out != null)
        {
            m_out.write(m_outbuf.toString());
            m_out.flush();
        }
    }

    /**
     * Send an FTP command response
     * 
     * @param msg StringBuffer
     * @exception IOException
     */
    protected final void sendFTPResponse(StringBuffer msg) throws IOException
    {

        // Output the FTP response

        if (m_out != null)
        {
            m_out.write(msg.toString());
            m_out.write(CRLF);
            m_out.flush();
        }
    }

    /**
     * Send an FTP command response
     * 
     * @param msg String
     * @exception IOException
     */
    protected final void sendFTPResponse(String msg) throws IOException
    {

        // Output the FTP response

        if (m_out != null)
        {
            m_out.write(msg);
            m_out.write(CRLF);
            m_out.flush();
        }
    }

    /**
     * Process a user command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procUser(FTPRequest req) throws IOException
    {

        // Clear the current client information

        setClientInformation(null);
        setLoggedOn(false);

        // Check if a user name has been specified

        if (req.hasArgument() == false)
        {
            sendFTPResponse(501, "Syntax error in parameters or arguments");
            return;
        }

        // Check for an anonymous login

        if (getFTPServer().allowAnonymous() == true
                && req.getArgument().equalsIgnoreCase(getFTPServer().getAnonymousAccount()))
        {

            // Anonymous login, create guest client information

            ClientInfo cinfo = new ClientInfo(getFTPServer().getAnonymousAccount(), null);
            cinfo.setGuest(true);
            setClientInformation(cinfo);

            // Return the anonymous login response

            sendFTPResponse(331, "Guest login ok, send your complete e-mail address as password");
            return;
        }

        // Create client information for the user

        setClientInformation(new ClientInfo(req.getArgument(), null));

        // Valid user, wait for the password

        sendFTPResponse(331, "User name okay, need password for " + req.getArgument());
    }

    /**
     * Process a password command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procPassword(FTPRequest req) throws IOException
    {

        // Check if the client information has been set, this indicates a user
        // command has been received

        if (hasClientInformation() == false)
        {
            sendFTPResponse(500, "Syntax error, command "
                    + FTPCommand.getCommandName(req.isCommand()) + " unrecognized");
            return;
        }

        // Check for an anonymous login, accept any password string

        ClientInfo cInfo = getClientInformation();
        
        if (cInfo.isGuest())
        {
            if ( getFTPServer().allowAnonymous() == true)
            {
                // Authenticate as the guest user
                
                AuthenticationComponent authComponent = getServer().getConfiguration().getAuthenticationComponent();
                cInfo.setUserName( authComponent.getGuestUserName());
            }
            else
            {
                // Return an access denied error
    
                sendFTPResponse(530, "Access denied");
    
                // DEBUG
    
                if (logger.isDebugEnabled() && hasDebug(DBG_STATE))
                    logger.debug("Anonymous logon not allowed");
    
                // Close the connection
    
                closeSession();
            }
        }

        // Get the client information and store the received plain text password
    
        cInfo.setPassword(req.getArgument());

        // Start a transaction
        
        beginReadTransaction( getServer().getConfiguration().getTransactionService());
        
        // Use the normal authentication service as we have the plaintext password
        
        AuthenticationService authService = getServer().getConfiguration().getAuthenticationService();
        
        try
        {
            // Authenticate the user

            if ( cInfo.isGuest())
            {
                // Authenticate as the guest user
                
                authService.authenticateAsGuest();
            }
            else
            {
                // Authenticate as a normal user

                authService.authenticate( cInfo.getUserName(), cInfo.getPasswordAsCharArray());
            }

            // User successfully logged on

            sendFTPResponse(230, "User logged in, proceed");
            setLoggedOn(true);

            // Save the client info
            
            setClientInformation( cInfo);
            
            // DEBUG

            if (logger.isDebugEnabled() && hasDebug(DBG_STATE))
                logger.debug("User " + getClientInformation().getUserName() + ", logon successful");
        }
        catch (org.alfresco.repo.security.authentication.AuthenticationException ex)
        {
            // DEBUG
            
            if ( logger.isDebugEnabled())
                logger.debug("Logon failed for user " + cInfo.getUserName());
        }

        // Check if the logon was successful
        
        if ( isLoggedOn() == true)
        {
            // If the user has successfully logged on to the FTP server then inform listeners

            getFTPServer().sessionLoggedOn(this);
            
            // If this is a guest logon then we need to set the root folder to the guest user home folder
            // as guest is not allowed access to other areas
            
            if ( cInfo.isGuest())
            {
                // Generate a dynamic share with the guest users home folder as the root
                
                DiskSharedDevice guestShare = createHomeDiskShare( cInfo);
                addDynamicShare( guestShare);
                
                // Set the root path for the guest logon to the guest share
                
                StringBuilder rootPath = new StringBuilder();
                
                rootPath.append(FTP_SEPERATOR);
                rootPath.append( guestShare.getName());
                rootPath.append(FTP_SEPERATOR);
                
                FTPPath guestRoot = null;
                
                try 
                {
                    // Set the root path for this FTP session

                    guestRoot = new FTPPath( rootPath.toString());
                    guestRoot.setSharedDevice( guestShare);
                    setRootPath( guestRoot);
                }
                catch ( InvalidPathException ex)
                {
                    if ( logger.isErrorEnabled())
                        logger.error("Error setting guest FTP root path", ex);
                }
                
                // DEBUG

                if (logger.isDebugEnabled() && hasDebug(DBG_STATE))
                    logger.debug("  Using root path " + guestRoot);
            }
        }
        else
        {

            // Return an access denied error

            sendFTPResponse(530, "Access denied");

            // DEBUG

            if (logger.isDebugEnabled() && hasDebug(DBG_STATE))
                logger.debug("User " + getClientInformation().getUserName() + ", logon failed");

            // Close the connection

            closeSession();
        }
    }

    /**
     * Process a port command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procPort(FTPRequest req) throws IOException
    {

        // Check if the user is logged in

        if (isLoggedOn() == false)
        {
            sendFTPResponse(500, "");
            return;
        }

        // Check if the parameter has been specified

        if (req.hasArgument() == false)
        {
            sendFTPResponse(501, "Required argument missing");
            return;
        }

        // Parse the address/port string into a IP address and port

        StringTokenizer token = new StringTokenizer(req.getArgument(), ",");
        if (token.countTokens() != 6)
        {
            sendFTPResponse(501, "Invalid argument");
            return;
        }

        // Parse the client address

        String addrStr = token.nextToken()
                + "." + token.nextToken() + "." + token.nextToken() + "." + token.nextToken();
        InetAddress addr = null;

        try
        {
            addr = InetAddress.getByName(addrStr);
        }
        catch (UnknownHostException ex)
        {
            sendFTPResponse(501, "Invalid argument (address)");
            return;
        }

        // Parse the client port

        int port = -1;

        try
        {
            port = Integer.parseInt(token.nextToken()) * 256;
            port += Integer.parseInt(token.nextToken());
        }
        catch (NumberFormatException ex)
        {
            sendFTPResponse(501, "Invalid argument (port)");
            return;
        }

        // Create an active data session, the actual socket connection will be
        // made later

        m_dataSess = getFTPServer().allocateDataSession(this, addr, port);

        // Return a success response to the client

        sendFTPResponse(200, "Port OK");

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug(DBG_DATAPORT))
            logger.debug("Port open addr=" + addr + ", port=" + port);
    }

    /**
     * Process a passive command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procPassive(FTPRequest req) throws IOException
    {

        // Check if the user is logged in

        if (isLoggedOn() == false)
        {
            sendFTPResponse(500, "");
            return;
        }

        // Create a passive data session

        try
        {
            m_dataSess = getFTPServer().allocateDataSession(this, null, 0);
        }
        catch (IOException ex)
        {
            m_dataSess = null;
        }

        // Check if the data session is valid

        if (m_dataSess == null)
        {
            sendFTPResponse(550, "Requested action not taken");
            return;
        }

        // Get the passive connection address/port and return to the client

        int pasvPort = m_dataSess.getPassivePort();

        StringBuffer msg = new StringBuffer();

        msg.append("227 Entering Passive Mode (");
        msg.append(getFTPServer().getLocalFTPAddressString());
        msg.append(",");
        msg.append(pasvPort >> 8);
        msg.append(",");
        msg.append(pasvPort & 0xFF);
        msg.append(")");

        sendFTPResponse(msg);

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug(DBG_DATAPORT))
            logger.debug("Passive open addr=" + getFTPServer().getLocalFTPAddressString() + ", port=" + pasvPort);
    }

    /**
     * Process a print working directory command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procPrintWorkDir(FTPRequest req) throws IOException
    {

        // Check if the user is logged in

        if (isLoggedOn() == false)
        {
            sendFTPResponse(500, "");
            return;
        }

        // Return the current working directory virtual path

        sendFTPResponse(257, "\"" + m_cwd.getFTPPath() + "\"");

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug(DBG_DIRECTORY))
            logger.debug("Pwd ftp="
                    + m_cwd.getFTPPath() + ", share=" + m_cwd.getShareName() + ", path=" + m_cwd.getSharePath());
    }

    /**
     * Process a change working directory command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procChangeWorkDir(FTPRequest req) throws IOException
    {

        // Check if the user is logged in

        if (isLoggedOn() == false)
        {
            sendFTPResponse(500, "");
            return;
        }

        // Check if the request has a valid argument

        if (req.hasArgument() == false)
        {
            sendFTPResponse(501, "Path not specified");
            return;
        }

        // Create the new working directory path

        FTPPath newPath = generatePathForRequest(req, false);
        if (newPath == null)
        {
            sendFTPResponse(550, "Invalid path " + req.getArgument());
            return;
        }

        // Set the new current working directory

        m_cwd = newPath;

        // Return a success status

        sendFTPResponse(250, "Requested file action OK");

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug(DBG_DIRECTORY))
            logger.debug("Cwd ftp="
                    + m_cwd.getFTPPath() + ", share=" + m_cwd.getShareName() + ", path=" + m_cwd.getSharePath());
    }

    /**
     * Process a change directory up command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procCdup(FTPRequest req) throws IOException
    {

        // Check if the user is logged in

        if (isLoggedOn() == false)
        {
            sendFTPResponse(500, "");
            return;
        }

        // Check if there is a current working directory path

        if (m_cwd.isRootPath())
        {

            // Already at the root directory, return an error status

            sendFTPResponse(550, "Already at root directory");
            return;
        }
        else
        {

            // Remove the last directory from the path

            m_cwd.removeDirectory();
            if (m_cwd.isRootPath() == false && m_cwd.getSharedDevice() == null)
                m_cwd.setSharedDevice(getShareList(), this);
        }

        // Return a success status

        sendFTPResponse(250, "Requested file action OK");

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug(DBG_DIRECTORY))
            logger.debug("Cdup ftp="
                    + m_cwd.getFTPPath() + ", share=" + m_cwd.getShareName() + ", path=" + m_cwd.getSharePath());
    }

    /**
     * Process a long directory listing command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procList(FTPRequest req) throws IOException
    {

        // Check if the user is logged in

        if (isLoggedOn() == false)
        {
            sendFTPResponse(500, "");
            return;
        }

        // Check if the client has requested hidden files, via the '-a' option

        boolean hidden = false;

        if (req.hasArgument() && req.getArgument().startsWith(LIST_OPTION_PREFIX))
        {
        	// We only support the hidden files option
          
        	String arg = req.getArgument();
        	if ( arg.indexOf( LIST_OPTION_HIDDEN) != -1)
        	{
	            // Indicate that we want hidden files in the listing
	
	            hidden = true;
	        }
	
	        // Remove the option from the command argument, and update the
	        // request
	
	        int pos = arg.indexOf(" ");
	        if (pos > 0)
	        	arg = arg.substring(pos + 1);
	        else
	            arg = null;
	
	        req.updateArgument(arg);
        }

        // Create the path for the file listing

        FTPPath ftpPath = m_cwd;
        if ( req.hasArgument())
            ftpPath = generatePathForRequest(req, true);

        if (ftpPath == null)
        {
            sendFTPResponse(500, "Invalid path");
            return;
        }

        // Check if the session has the required access

        if (ftpPath.isRootPath() == false)
        {

            // Check if the session has access to the filesystem

            TreeConnection tree = getTreeConnection(ftpPath.getSharedDevice());
            if (tree == null || tree.hasReadAccess() == false)
            {

                // Session does not have access to the filesystem

                sendFTPResponse(550, "Access denied");
                return;
            }
        }

        // Send the intermediate response

        sendFTPResponse(150, "File status okay, about to open data connection");

        // Check if there is an active data session

        if (m_dataSess == null)
        {
            sendFTPResponse(425, "Can't open data connection");
            return;
        }

        // Get the data connection socket

        Socket dataSock = null;

        try
        {
            dataSock = m_dataSess.getSocket();
        }
        catch (Exception ex)
        {
            logger.debug(ex);
        }

        if (dataSock == null)
        {
            sendFTPResponse(426, "Connection closed; transfer aborted");
            return;
        }

        // Output the directory listing to the client

        Writer dataWrt = null;

        try
        {

            // Open an output stream to the client

            if ( isUTF8Enabled())
                dataWrt = new OutputStreamWriter(dataSock.getOutputStream(), "UTF-8");
            else
                dataWrt = new OutputStreamWriter(dataSock.getOutputStream());

            // Check if a path has been specified to list

            Vector<FileInfo> files = null;

            if (req.hasArgument())
            {
            }

            // Get a list of file information objects for the current directory

            files = listFilesForPath(ftpPath, false, hidden);

            // Output the file list to the client

            if (files != null)
            {

                // DEBUG

                if (logger.isDebugEnabled() && hasDebug(DBG_SEARCH))
                    logger.debug("List found " + files.size() + " files in " + ftpPath.getFTPPath());

                // Output the file information to the client

                StringBuilder str = new StringBuilder(256);

                for (FileInfo finfo : files)
                {
                    // Build the output record

                    str.setLength(0);

                    str.append(finfo.isDirectory() ? "d" : "-");
                    str.append("rw-rw-rw-   1 user group ");
                    str.append(finfo.getSize());
                    str.append(" ");

                    FTPDate.packUnixDate(str, new Date(finfo.getModifyDateTime()));

                    str.append(" ");
                    str.append(finfo.getFileName());
                    str.append(CRLF);

                    // Output the file information record

                    dataWrt.write(str.toString());
                }

                // Flush the data stream

                dataWrt.flush();
            }

            // Close the data stream and socket

            dataWrt.close();
            dataWrt = null;

            getFTPServer().releaseDataSession(m_dataSess);
            m_dataSess = null;

            // End of file list transmission

            sendFTPResponse(226, "Closing data connection");
        }
        catch (Exception ex)
        {

            // Failed to send file listing

            sendFTPResponse(451, "Error reading file list");
        } finally
        {

            // Close the data stream to the client

            if (dataWrt != null)
                dataWrt.close();

            // Close the data connection to the client

            if (m_dataSess != null)
            {
                getFTPServer().releaseDataSession(m_dataSess);
                m_dataSess = null;
            }
        }
    }

    /**
     * Process a short directory listing command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procNList(FTPRequest req) throws IOException
    {

        // Check if the user is logged in

        if (isLoggedOn() == false)
        {
            sendFTPResponse(500, "");
            return;
        }

        // Create the path for the file listing

        FTPPath ftpPath = m_cwd;
        if (req.hasArgument())
            ftpPath = generatePathForRequest(req, true);

        if (ftpPath == null)
        {
            sendFTPResponse(500, "Invalid path");
            return;
        }

        // Check if the session has the required access

        if (ftpPath.isRootPath() == false)
        {

            // Check if the session has access to the filesystem

            TreeConnection tree = getTreeConnection(ftpPath.getSharedDevice());
            if (tree == null || tree.hasReadAccess() == false)
            {

                // Session does not have access to the filesystem

                sendFTPResponse(550, "Access denied");
                return;
            }
        }

        // Send the intermediate response

        sendFTPResponse(150, "File status okay, about to open data connection");

        // Check if there is an active data session

        if (m_dataSess == null)
        {
            sendFTPResponse(425, "Can't open data connection");
            return;
        }

        // Get the data connection socket

        Socket dataSock = null;

        try
        {
            dataSock = m_dataSess.getSocket();
        }
        catch (Exception ex)
        {
            logger.error("Data socket error", ex);
        }

        if (dataSock == null)
        {
            sendFTPResponse(426, "Connection closed; transfer aborted");
            return;
        }

        // Output the directory listing to the client

        Writer dataWrt = null;

        try
        {

            // Open an output stream to the client

            if ( isUTF8Enabled())
                dataWrt = new OutputStreamWriter(dataSock.getOutputStream(), "UTF-8");
              else
                dataWrt = new OutputStreamWriter(dataSock.getOutputStream());

            // Check if a path has been specified to list

            Vector<FileInfo> files = null;

            if (req.hasArgument())
            {
            }

            // Get a list of file information objects for the current directory

            files = listFilesForPath(ftpPath, false, false);

            // Output the file list to the client

            if (files != null)
            {

                // DEBUG

                if (logger.isDebugEnabled() && hasDebug(DBG_SEARCH))
                    logger.debug("List found " + files.size() + " files in " + ftpPath.getFTPPath());

                // Output the file information to the client

                for (FileInfo finfo : files)
                {

                    // Output the file information record

                    dataWrt.write(finfo.getFileName());
                    dataWrt.write(CRLF);
                }
            }

            // End of file list transmission

            sendFTPResponse(226, "Closing data connection");
        }
        catch (Exception ex)
        {

            // Failed to send file listing

            sendFTPResponse(451, "Error reading file list");
        } finally
        {

            // Close the data stream to the client

            if (dataWrt != null)
                dataWrt.close();

            // Close the data connection to the client

            if (m_dataSess != null)
            {
                getFTPServer().releaseDataSession(m_dataSess);
                m_dataSess = null;
            }
        }
    }

    /**
     * Process a system status command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procSystemStatus(FTPRequest req) throws IOException
    {

        // Return the system type

        sendFTPResponse(215, "UNIX Type: Java FTP Server");
    }

    /**
     * Process a server status command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procServerStatus(FTPRequest req) throws IOException
    {

        // Return server status information

        sendFTPResponse(211, "JLAN Server - Java FTP Server");
    }

    /**
     * Process a help command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procHelp(FTPRequest req) throws IOException
    {

        // Return help information

        sendFTPResponse(211, "HELP text");
    }

    /**
     * Process a no-op command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procNoop(FTPRequest req) throws IOException
    {

        // Return a response

        sendFTPResponse(200, "");
    }

    /**
	 * Process an options request
	 * 
	 * @param req
	 *            FTPRequest
	 * @exception IOException
	 */
	protected final void procOptions(FTPRequest req) throws IOException {

		// Check if the user is logged in

		if (isLoggedOn() == false)
		{
			sendFTPResponse(500, "");
			return;
		}

		// Check if the parameter has been specified

		if (req.hasArgument() == false)
		{
			sendFTPResponse(501, "Required argument missing");
			return;
		}

		// Parse the argument to get the sub-command and arguments

		StringTokenizer token = new StringTokenizer(req.getArgument(), " ");
		if (token.hasMoreTokens() == false)
		{
			sendFTPResponse(501, "Invalid argument");
			return;
		}

		// Get the sub-command

		String optsCmd = token.nextToken();

		// UTF8 enable/disable command

		if (FeatureUTF8 && optsCmd.equalsIgnoreCase("UTF8"))
		{

			// Get the next argument

			if (token.hasMoreTokens())
			{
				String optsArg = token.nextToken();
				if (optsArg.equalsIgnoreCase("ON"))
				{

					// Enable UTF-8 file names

					m_utf8Paths = true;
				}
				else if (optsArg.equalsIgnoreCase("OFF"))
				{

					// Disable UTF-8 file names

					m_utf8Paths = false;
				}
				else
				{

					// Invalid argument

					sendFTPResponse(501, "OPTS UTF8 Invalid argument");
					return;
				}

				// Report the new setting back to the client

				sendFTPResponse(200, "OPTS UTF8 " + (isUTF8Enabled() ? "ON" : "OFF"));

				// DEBUG

				if (logger.isDebugEnabled() && hasDebug(DBG_FILE))
					logger.debug("UTF8 options utf8=" + (isUTF8Enabled() ? "ON" : "OFF"));
			}
		}

		// MLST/MLSD fact list command

		else if (FeatureMLST && optsCmd.equalsIgnoreCase("MLST"))
		{

			// Check if the fact list argument is valid

			if (token.hasMoreTokens() == false)
			{

				// Invalid fact list argument

				sendFTPResponse(501, "OPTS MLST Invalid argument");
				return;
			}

			// Parse the supplied fact names

			int mlstFacts = 0;
			StringTokenizer factTokens = new StringTokenizer(token.nextToken(),
					";");
			StringBuffer factStr = new StringBuffer();

			while (factTokens.hasMoreTokens())
			{
				// Get the current fact name and validate

				String factName = factTokens.nextToken();
				int factIdx = -1;
				int idx = 0;

				while (idx < _factNames.length && factIdx == -1)
				{
					if (_factNames[idx].equalsIgnoreCase(factName))
						factIdx = idx;
					else
						idx++;
				}

				// Check if the fact name is valid, ignore invalid names

				if (factIdx != -1)
				{
					// Add the fact name to the reply tring

					factStr.append(_factNames[factIdx]);
					factStr.append(";");

					// Add the fact to the fact bit mask

					mlstFacts += (1 << factIdx);
				}
			}

			// check if any valid fact names were found

			if (mlstFacts == 0)
			{
				sendFTPResponse(501, "OPTS MLST Invalid Argument");
				return;
			}

			// Update the MLST enabled fact list for this session

			m_mlstFacts = mlstFacts;

			// Send the response

			sendFTPResponse(200, "MLST OPTS " + factStr.toString());

			// DEBUG

			if (logger.isDebugEnabled() && hasDebug(DBG_SEARCH))
				logger.debug("MLst options facts=" + factStr.toString());
		}
		else
		{
			// Invalid options command, or feature not enabled
			
			sendFTPResponse(501, "Invalid options commands");
		}
	}

    /**
	 * Process a quit command
	 * 
	 * @param req
	 *            FTPRequest
	 * @exception IOException
	 */
    protected final void procQuit(FTPRequest req) throws IOException
    {

        // Return a response

        sendFTPResponse(221, "Bye");

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug(DBG_STATE))
            logger.debug("Quit closing connection(s) to client");

        // Close the session(s) to the client

        closeSession();
    }

    /**
	 * Process a type command
	 * 
	 * @param req
	 *            FTPRequest
	 * @exception IOException
	 */
    protected final void procType(FTPRequest req) throws IOException
    {

        // Check if an argument has been specified

        if (req.hasArgument() == false)
        {
            sendFTPResponse(501, "Syntax error, parameter required");
            return;
        }

        // Check if ASCII or binary mode is enabled

        String arg = req.getArgument().toUpperCase();
        if (arg.startsWith("A"))
            setBinary(false);
        else if (arg.startsWith("I") || arg.startsWith("L"))
            setBinary(true);
        else
        {

            // Invalid argument

            sendFTPResponse(501, "Syntax error, invalid parameter");
            return;
        }

        // Return a success status

        sendFTPResponse(200, "Command OK");

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug(DBG_STATE))
            logger.debug("Type arg=" + req.getArgument() + ", binary=" + m_binary);
    }

    /**
     * Process a restart command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procRestart(FTPRequest req) throws IOException
    {

        // Check if the user is logged in

        if (isLoggedOn() == false)
        {
            sendFTPResponse(500, "");
            return;
        }

        // Check if an argument has been specified

        if (req.hasArgument() == false)
        {
            sendFTPResponse(501, "Syntax error, parameter required");
            return;
        }

        // Validate the restart position

        try
        {
            m_restartPos = Integer.parseInt(req.getArgument());
        }
        catch (NumberFormatException ex)
        {
            sendFTPResponse(501, "Invalid restart position");
            return;
        }

        // Return a success status

        sendFTPResponse(350, "Restart OK");

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug(DBG_FILEIO))
            logger.debug("Restart pos=" + m_restartPos);
    }

    /**
     * Process a return file command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procReturnFile(FTPRequest req) throws IOException
    {

        // Check if the user is logged in

        if (isLoggedOn() == false)
        {
            sendFTPResponse(500, "");
            return;
        }

        // Check if an argument has been specified

        if (req.hasArgument() == false)
        {
            sendFTPResponse(501, "Syntax error, parameter required");
            return;
        }

        // Create the path for the file listing

        FTPPath ftpPath = generatePathForRequest(req, true);
        if (ftpPath == null)
        {
            sendFTPResponse(500, "Invalid path");
            return;
        }

        // Check if the path is the root directory

        if (ftpPath.isRootPath() || ftpPath.isRootSharePath())
        {
            sendFTPResponse(550, "That is a directory");
            return;
        }

        // Send the intermediate response

        sendFTPResponse(150, "Connection accepted");

        // Check if there is an active data session

        if (m_dataSess == null)
        {
            sendFTPResponse(425, "Can't open data connection");
            return;
        }

        // Check if a seperate thread should be used for the data transfer
        
        if ( UseThreadedDataTransfer == true)
        {
	        // DEBUG
        	
	        if (logger.isDebugEnabled() && hasDebug(DBG_FILE))
	            logger.debug("Returning (threaded) ftp="
	                    + ftpPath.getFTPPath() + ", share=" + ftpPath.getShareName() + ", path=" + ftpPath.getSharePath());

            // Start the transfer in a seperate thread
            
            m_dataSess.doReturnFile( ftpPath, m_restartPos, req.getArgument());
        }
        else
        {
	        // Get the data connection socket
	
	        Socket dataSock = null;
	
	        try
	        {
	            dataSock = m_dataSess.getSocket();
	        }
	        catch (Exception ex)
	        {
	        }
	
	        if (dataSock == null)
	        {
	            sendFTPResponse(426, "Connection closed; transfer aborted");
	            return;
	        }
	
	        // DEBUG
	
	        if (logger.isDebugEnabled() && hasDebug(DBG_FILE))
	            logger.debug("Returning ftp="
	                    + ftpPath.getFTPPath() + ", share=" + ftpPath.getShareName() + ", path=" + ftpPath.getSharePath());
	
	        // Send the file to the client
	
	        OutputStream os = null;
	        DiskInterface disk = null;
	        TreeConnection tree = null;
	        NetworkFile netFile = null;
	
	        try
	        {
	
	            // Open an output stream to the client
	
	            os = dataSock.getOutputStream();
	
	            // Create a temporary tree connection
	
	            tree = getTreeConnection(ftpPath.getSharedDevice());
	
	            // Check if the file exists and it is a file, if so then open the
	            // file
	
	            disk = (DiskInterface) ftpPath.getSharedDevice().getInterface();
	
	            // Create the file open parameters
	
	            FileOpenParams params = new FileOpenParams(ftpPath.getSharePath(), FileAction.OpenIfExists,
	                    AccessMode.ReadOnly, 0);
	
	            // Check if the file exists and it is a file
	
	            int sts = disk.fileExists(this, tree, ftpPath.getSharePath());
	
	            if (sts == FileStatus.FileExists)
	            {
	
	                // Open the file
	
	                netFile = disk.openFile(this, tree, params);
	            }

	            // Commit any current transaction
	            
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
	            
	            // Check if the file has been opened
	
	            if (netFile == null)
	            {
	                sendFTPResponse(550, "File " + req.getArgument() + " not available");
	                return;
	            }
	
	            // Allocate the buffer for the file data
	
	            byte[] buf = new byte[DEFAULT_BUFFERSIZE];
	            long filePos = m_restartPos;
	
	            int len = -1;
	
	            while (filePos < netFile.getFileSize())
	            {
	
	                // Read another block of data from the file
	
	                len = disk.readFile(this, tree, netFile, buf, 0, buf.length, filePos);
	
	                // DEBUG
	
	                if (logger.isDebugEnabled() && hasDebug(DBG_FILEIO))
	                    logger.debug(" Write len=" + len + " bytes");
	
	                // Write the current data block to the client, update the file
	                // position
	
	                if (len > 0)
	                {
	
	                    // Write the data to the client
	
	                    os.write(buf, 0, len);
	
	                    // Update the file position
	
	                    filePos += len;
	                }
	            }
	
	            // Close the output stream to the client
	
	            os.close();
	            os = null;
	
	            // Indicate that the file has been transmitted
	
	            sendFTPResponse(226, "Closing data connection");
	
	            // Close the data session
	
	            getFTPServer().releaseDataSession(m_dataSess);
	            m_dataSess = null;
	
	            // Close the network file

	            disk.closeFile(this, tree, netFile);
	            netFile = null;
	
	            // DEBUG
	
	            if (logger.isDebugEnabled() && hasDebug(DBG_FILEIO))
	                logger.debug(" Transfer complete, file closed");
	        }
	        catch (SocketException ex)
	        {
	
	            // DEBUG
	
	            if (logger.isDebugEnabled() && hasDebug(DBG_ERROR))
	                logger.debug(" Error during transfer", ex);
	
	            // Close the data socket to the client
	
	            if (m_dataSess != null)
	            {
	                m_dataSess.closeSession();
	                m_dataSess = null;
	            }
	
	            // Indicate that there was an error during transmission of the file
	            // data
	
	            sendFTPResponse(426, "Data connection closed by client");
	        }
	        catch (Exception ex)
	        {
	
	            // DEBUG
	
	            if (logger.isDebugEnabled() && hasDebug(DBG_ERROR))
	                logger.debug(" Error during transfer", ex);
	
	            // Indicate that there was an error during transmission of the file
	            // data
	
	            sendFTPResponse(426, "Error during transmission");
	        }
	        finally
	        {
	
	            // Close the network file
	
	            if (netFile != null && disk != null && tree != null)
	                disk.closeFile(this, tree, netFile);
	
	            // Close the output stream to the client
	
	            if (os != null)
	                os.close();
	
	            // Close the data connection to the client
	
	            if (m_dataSess != null)
	            {
	                getFTPServer().releaseDataSession(m_dataSess);
	                m_dataSess = null;
	            }
	        }
        }
    }

    /**
     * Process a store file command
     * 
     * @param req FTPRequest
     * @param append boolean
     * @exception IOException
     */
    protected final void procStoreFile(FTPRequest req, boolean append) throws IOException
    {

        // Check if the user is logged in

        if (isLoggedOn() == false)
        {
            sendFTPResponse(500, "");
            return;
        }

        // Check if an argument has been specified

        if (req.hasArgument() == false)
        {
            sendFTPResponse(501, "Syntax error, parameter required");
            return;
        }

        // Create the path for the file listing

        FTPPath ftpPath = generatePathForRequest(req, true, false);
        if (ftpPath == null)
        {
            sendFTPResponse(500, "Invalid path");
            return;
        }

        // Check if a seperate thread should be used for the data transfer
        
        if ( UseThreadedDataTransfer == true)
        {
            // DEBUG
        	
            if (logger.isDebugEnabled() && hasDebug(DBG_FILE))
                logger.debug("Storing (threaded) ftp="
                        + ftpPath.getFTPPath() + ", share=" + ftpPath.getShareName() + ", path="
                        + ftpPath.getSharePath());

            // Start the transfer in a seperate thread
            
            m_dataSess.doStoreFile( ftpPath, m_restartPos, req.getArgument());
        }
        else
        {
	        // Send the file to the client
	
	        InputStream is = null;
	        DiskInterface disk = null;
	        TreeConnection tree = null;
	        NetworkFile netFile = null;
	
	        try
	        {
	
	            // Create a temporary tree connection
	
	            tree = getTreeConnection(ftpPath.getSharedDevice());
	
	            // Check if the session has the required access to the filesystem
	
	            if (tree == null || tree.hasWriteAccess() == false)
	            {
	
	                // Session does not have write access to the filesystem
	
	                sendFTPResponse(550, "Access denied");
	                return;
	            }
	
	            // Check if the file exists
	
	            disk = (DiskInterface) ftpPath.getSharedDevice().getInterface();
	            int sts = disk.fileExists(this, tree, ftpPath.getSharePath());
	
	            if (sts == FileStatus.DirectoryExists)
	            {
	
	                // Return an error status
	
	                sendFTPResponse(500, "Invalid path (existing directory)");
	                return;
	            }
	
	            // Create the file open parameters
	
	            int openAction = FileAction.CreateNotExist;
	            if ( sts == FileStatus.FileExists)
	              openAction = append == false ? FileAction.TruncateExisting : FileAction.OpenIfExists;
	            
	            FileOpenParams params = new FileOpenParams(ftpPath.getSharePath(), openAction, AccessMode.ReadWrite, 0);
	
	            // Create a new file to receive the data
	
	            if (sts == FileStatus.FileExists)
	            {
	
	                // Overwrite the existing file
	
	                netFile = disk.openFile(this, tree, params);
	            }
	            else
	            {
	
	                // Create a new file
	
	                netFile = disk.createFile(this, tree, params);
	            }
	
	            // Commit any current transaction
	            
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
	            
	            // Notify change listeners that a new file has been created
	
	            DiskDeviceContext diskCtx = (DiskDeviceContext) tree.getContext();
	
	            if (diskCtx.hasChangeHandler())
	                diskCtx.getChangeHandler().notifyFileChanged(NotifyChange.ActionAdded, ftpPath.getSharePath());
	
	            // Send the intermediate response
	
	            sendFTPResponse(150, "File status okay, about to open data connection");
	
	            // Check if there is an active data session
	
	            if (m_dataSess == null)
	            {
	                sendFTPResponse(425, "Can't open data connection");
	                return;
	            }
	
	            // Get the data connection socket
	
	            Socket dataSock = null;
	
	            try
	            {
	                dataSock = m_dataSess.getSocket();
	            }
	            catch (Exception ex)
	            {
	            }
	
	            if (dataSock == null)
	            {
	                sendFTPResponse(426, "Connection closed; transfer aborted");
	                return;
	            }
	
	            // Open an input stream from the client
	
	            is = dataSock.getInputStream();
	
	            // DEBUG
	
	            if (logger.isDebugEnabled() && hasDebug(DBG_FILE))
	                logger.debug("Storing ftp="
	                        + ftpPath.getFTPPath() + ", share=" + ftpPath.getShareName() + ", path="
	                        + ftpPath.getSharePath());
	
	            // Allocate the buffer for the file data
	
	            byte[] buf = new byte[DEFAULT_BUFFERSIZE];
	            long filePos = 0;
	            int len = is.read(buf, 0, buf.length);
	
	            //  If the data is to be appended then set the starting file position to the end of the file
	            
	            if ( append == true)
	              filePos = netFile.getFileSize();
	            
	            while (len > 0)
	            {
	
	                // DEBUG
	
	                if (logger.isDebugEnabled() && hasDebug(DBG_FILEIO))
	                    logger.debug(" Receive len=" + len + " bytes");
	
	                // Write the current data block to the file, update the file
	                // position
	
	                disk.writeFile(this, tree, netFile, buf, 0, len, filePos);
	                filePos += len;
	
	                // Read another block of data from the client
	
	                len = is.read(buf, 0, buf.length);
	            }
	
	            // Close the input stream from the client
	
	            is.close();
	            is = null;
	
	            // Close the network file
	
	            disk.closeFile(this, tree, netFile);
	            netFile = null;
	
	            // Indicate that the file has been received
	
	            sendFTPResponse(226, "Closing data connection");
	
	            // DEBUG
	
	            if (logger.isDebugEnabled() && hasDebug(DBG_FILEIO))
	                logger.debug(" Transfer complete, file closed");
	        }
	        catch( AccessDeniedException ex)
	        {
	            // DEBUG
	
	            if (logger.isDebugEnabled() && hasDebug(DBG_ERROR))
	                logger.debug(" Access denied", ex);
	
	            // Session does not have write access to the filesystem
	
	            sendFTPResponse(550, "Access denied");
	        }
	        catch (SocketException ex)
	        {
	            // DEBUG
	
	            if (logger.isDebugEnabled() && hasDebug(DBG_ERROR))
	                logger.debug(" Error during transfer", ex);
	
	            // Close the data socket to the client
	
	            if (m_dataSess != null)
	            {
	                getFTPServer().releaseDataSession(m_dataSess);
	                m_dataSess = null;
	            }
	
	            // Indicate that there was an error during transmission of the file
	            // data
	
	            sendFTPResponse(426, "Data connection closed by client");
	        }
	        catch (DiskFullException ex)
	        {
	
	            // DEBUG
	
	            if (logger.isDebugEnabled() && hasDebug(DBG_ERROR))
	                logger.debug(" Error during transfer", ex);
	
	            // Close the data socket to the client
	
	            if (m_dataSess != null)
	            {
	                getFTPServer().releaseDataSession(m_dataSess);
	                m_dataSess = null;
	            }
	
	            // Indicate that there was an error during writing of the file
	
	            sendFTPResponse(451, "Disk full");
	        }
	        catch (Exception ex)
	        {
	
	            // DEBUG
	
	            if (logger.isDebugEnabled() && hasDebug(DBG_ERROR))
	                logger.debug(" Error during transfer", ex);
	
	            // Indicate that there was an error during transmission of the file
	            // data
	
	            sendFTPResponse(426, "Error during transmission");
	        }
	        finally
	        {
	
	            // Close the network file
	
	            if (netFile != null && disk != null && tree != null)
	                disk.closeFile(this, tree, netFile);
	
	            // Close the input stream to the client
	
	            if (is != null)
	                is.close();
	
	            // Close the data connection to the client
	
	            if (m_dataSess != null)
	            {
	                getFTPServer().releaseDataSession(m_dataSess);
	                m_dataSess = null;
	            }
	        }
        }
    }

    /**
     * Process a delete file command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procDeleteFile(FTPRequest req) throws IOException
    {

        // Check if the user is logged in

        if (isLoggedOn() == false)
        {
            sendFTPResponse(500, "");
            return;
        }

        // Check if an argument has been specified

        if (req.hasArgument() == false)
        {
            sendFTPResponse(501, "Syntax error, parameter required");
            return;
        }

        // Create the path for the file

        FTPPath ftpPath = generatePathForRequest(req, true);
        if (ftpPath == null)
        {
            sendFTPResponse(550, "Invalid path specified");
            return;
        }

        // Delete the specified file

        DiskInterface disk = null;
        TreeConnection tree = null;

        try
        {

            // Create a temporary tree connection

            tree = getTreeConnection(ftpPath.getSharedDevice());

            // Check if the session has the required access to the filesystem

            if (tree == null || tree.hasWriteAccess() == false)
            {

                // Session does not have write access to the filesystem

                sendFTPResponse(550, "Access denied");
                return;
            }

            // Check if the file exists and it is a file

            disk = (DiskInterface) ftpPath.getSharedDevice().getInterface();
            int sts = disk.fileExists(this, tree, ftpPath.getSharePath());

            if (sts == FileStatus.FileExists)
            {

                // Delete the file

                disk.deleteFile(this, tree, ftpPath.getSharePath());

                // Check if there are any file/directory change notify requests
                // active

                DiskDeviceContext diskCtx = (DiskDeviceContext) tree.getContext();
                if (diskCtx.hasChangeHandler())
                    diskCtx.getChangeHandler().notifyFileChanged(NotifyChange.ActionRemoved, ftpPath.getSharePath());

                // DEBUG

                if (logger.isDebugEnabled() && hasDebug(DBG_FILE))
                    logger.debug("Deleted ftp="
                            + ftpPath.getFTPPath() + ", share=" + ftpPath.getShareName() + ", path="
                            + ftpPath.getSharePath());
            }
            else
            {

                // File does not exist or is a directory

                sendFTPResponse(550, "File "
                        + req.getArgument() + (sts == FileStatus.NotExist ? " not available" : " is a directory"));
                return;
            }
        }
        catch (AccessDeniedException ex)
        {
            // DEBUG

            if (logger.isDebugEnabled() && hasDebug(DBG_ERROR))
                logger.debug(" Access denied", ex);

            // Session does not have write access to the filesystem

            sendFTPResponse(550, "Access denied");
            return;
        }
        catch (Exception ex)
        {
            sendFTPResponse(450, "File action not taken");
            return;
        }

        // Return a success status

        sendFTPResponse(250, "File " + req.getArgument() + " deleted");
    }

    /**
     * Process a rename from command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procRenameFrom(FTPRequest req) throws IOException
    {

        // Check if the user is logged in

        if (isLoggedOn() == false)
        {
            sendFTPResponse(500, "");
            return;
        }

        // Check if an argument has been specified

        if (req.hasArgument() == false)
        {
            sendFTPResponse(501, "Syntax error, parameter required");
            return;
        }

        // Clear the current rename from path details, if any

        m_renameFrom = null;

        // Create the path for the file/directory

        FTPPath ftpPath = generatePathForRequest(req, false, false);
        if (ftpPath == null)
        {
            sendFTPResponse(550, "Invalid path specified");
            return;
        }

        // Check that the file exists, and it is a file

        DiskInterface disk = null;
        TreeConnection tree = null;

        try
        {

            // Create a temporary tree connection

            tree = getTreeConnection(ftpPath.getSharedDevice());

            // Check if the session has the required access to the filesystem

            if (tree == null || tree.hasWriteAccess() == false)
            {

                // Session does not have write access to the filesystem

                sendFTPResponse(550, "Access denied");
                return;
            }

            // Check if the file exists and it is a file

            disk = (DiskInterface) ftpPath.getSharedDevice().getInterface();
            int sts = disk.fileExists(this, tree, ftpPath.getSharePath());

            if (sts != FileStatus.NotExist)
            {

                // Save the rename from file details, rename to command should
                // follow

                m_renameFrom = ftpPath;

                // DEBUG

                if (logger.isDebugEnabled() && hasDebug(DBG_FILE))
                    logger.debug("RenameFrom ftp="
                            + ftpPath.getFTPPath() + ", share=" + ftpPath.getShareName() + ", path="
                            + ftpPath.getSharePath());
            }
            else
            {

                // File/directory does not exist

                sendFTPResponse(550, "File "
                        + req.getArgument() + (sts == FileStatus.NotExist ? " not available" : " is a directory"));
                return;
            }
        }
        catch (Exception ex)
        {
            sendFTPResponse(450, "File action not taken");
            return;
        }

        // Return a success status

        sendFTPResponse(350, "File " + req.getArgument() + " OK");
    }

    /**
     * Process a rename to command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procRenameTo(FTPRequest req) throws IOException
    {

        // Check if the user is logged in

        if (isLoggedOn() == false)
        {
            sendFTPResponse(500, "");
            return;
        }

        // Check if an argument has been specified

        if (req.hasArgument() == false)
        {
            sendFTPResponse(501, "Syntax error, parameter required");
            return;
        }

        // Check if the rename from has already been set

        if (m_renameFrom == null)
        {
            sendFTPResponse(550, "Rename from not set");
            return;
        }

        // Create the path for the new file name

        FTPPath ftpPath = generatePathForRequest(req, true, false);
        if (ftpPath == null)
        {
            sendFTPResponse(550, "Invalid path specified");
            return;
        }

        // Check that the rename is on the same share

        if (m_renameFrom.getShareName().compareTo(ftpPath.getShareName()) != 0)
        {
            sendFTPResponse(550, "Cannot rename across shares");
            return;
        }

        // Rename the file

        DiskInterface disk = null;
        TreeConnection tree = null;

        try
        {

            // Create a temporary tree connection

            tree = getTreeConnection(ftpPath.getSharedDevice());

            // Check if the session has the required access to the filesystem

            if (tree == null || tree.hasWriteAccess() == false)
            {

                // Session does not have write access to the filesystem

                sendFTPResponse(550, "Access denied");
                return;
            }

            // Check if the file exists and it is a file

            disk = (DiskInterface) ftpPath.getSharedDevice().getInterface();
            int sts = disk.fileExists(this, tree, ftpPath.getSharePath());

            if (sts == FileStatus.NotExist)
            {

                // Rename the file/directory

                disk.renameFile(this, tree, m_renameFrom.getSharePath(), ftpPath.getSharePath());

                // Check if there are any file/directory change notify requests
                // active

                DiskDeviceContext diskCtx = (DiskDeviceContext) tree.getContext();
                if (diskCtx.hasChangeHandler())
                    diskCtx.getChangeHandler().notifyRename(m_renameFrom.getSharePath(), ftpPath.getSharePath());

                // DEBUG

                if (logger.isDebugEnabled() && hasDebug(DBG_FILE))
                    logger.debug("RenameTo ftp="
                            + ftpPath.getFTPPath() + ", share=" + ftpPath.getShareName() + ", path="
                            + ftpPath.getSharePath());
            }
            else
            {

                // File does not exist or is a directory

                sendFTPResponse(550, "File "
                        + req.getArgument() + (sts == FileStatus.NotExist ? " not available" : " is a directory"));
                return;
            }
        }
        catch (Exception ex)
        {
            sendFTPResponse(450, "File action not taken");
            return;
        }
        finally
        {

            // Clear the rename details

            m_renameFrom = null;
        }

        // Return a success status

        sendFTPResponse(250, "File renamed OK");
    }

    /**
     * Process a create directory command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procCreateDirectory(FTPRequest req) throws IOException
    {

        // Check if the user is logged in

        if (isLoggedOn() == false)
        {
            sendFTPResponse(500, "");
            return;
        }

        // Check if an argument has been specified

        if (req.hasArgument() == false)
        {
            sendFTPResponse(501, "Syntax error, parameter required");
            return;
        }

        // Check if the new directory contains multiple directories

        FTPPath ftpPath = generatePathForRequest(req, false, false);
        if (ftpPath == null)
        {
            sendFTPResponse(550, "Invalid path " + req.getArgument());
            return;
        }

        // Create the new directory

        DiskInterface disk = null;
        TreeConnection tree = null;

        try
        {

            // Create a temporary tree connection

            tree = getTreeConnection(ftpPath.getSharedDevice());

            // Check if the session has the required access to the filesystem

            if (tree == null || tree.hasWriteAccess() == false)
            {

                // Session does not have write access to the filesystem

                sendFTPResponse(550, "Access denied");
                return;
            }

            // Check if the directory exists

            disk = (DiskInterface) ftpPath.getSharedDevice().getInterface();
            int sts = disk.fileExists(this, tree, ftpPath.getSharePath());

            if (sts == FileStatus.NotExist)
            {

                // Create the new directory

                FileOpenParams params = new FileOpenParams(ftpPath.getSharePath(), FileAction.CreateNotExist,
                        AccessMode.ReadWrite, FileAttribute.NTDirectory);

                disk.createDirectory(this, tree, params);

                // Notify change listeners that a new directory has been created

                DiskDeviceContext diskCtx = (DiskDeviceContext) tree.getContext();

                if (diskCtx.hasChangeHandler())
                    diskCtx.getChangeHandler().notifyFileChanged(NotifyChange.ActionAdded, ftpPath.getSharePath());

                // DEBUG

                if (logger.isDebugEnabled() && hasDebug(DBG_DIRECTORY))
                    logger.debug("CreateDir ftp="
                            + ftpPath.getFTPPath() + ", share=" + ftpPath.getShareName() + ", path="
                            + ftpPath.getSharePath());
            }
            else
            {

                // File/directory already exists with that name, return an error

                sendFTPResponse(450, sts == FileStatus.FileExists ? "File exists with that name"
                        : "Directory already exists");
                return;
            }
        }
        catch (Exception ex)
        {
            sendFTPResponse(450, "Failed to create directory");
            return;
        }

        // Return the FTP path to the client

        sendFTPResponse(250, ftpPath.getFTPPath());
    }

    /**
     * Process a delete directory command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procRemoveDirectory(FTPRequest req) throws IOException
    {

        // Check if the user is logged in

        if (isLoggedOn() == false)
        {
            sendFTPResponse(500, "");
            return;
        }

        // Check if an argument has been specified

        if (req.hasArgument() == false)
        {
            sendFTPResponse(501, "Syntax error, parameter required");
            return;
        }

        // Check if the directory path contains multiple directories

        FTPPath ftpPath = generatePathForRequest(req, false);
        if (ftpPath == null)
        {
            sendFTPResponse(550, "Invalid path " + req.getArgument());
            return;
        }

        // Check if the path is the root directory, cannot delete directories
        // from the root directory as it maps to the list of available disk shares.

        if (ftpPath.isRootPath() || ftpPath.isRootSharePath())
        {
            sendFTPResponse(550, "Access denied, cannot delete directory in root");
            return;
        }

        // Delete the directory

        DiskInterface disk = null;
        TreeConnection tree = null;

        try
        {

            // Create a temporary tree connection

            tree = getTreeConnection(ftpPath.getSharedDevice());

            // Check if the session has the required access to the filesystem

            if (tree == null || tree.hasWriteAccess() == false)
            {

                // Session does not have write access to the filesystem

                sendFTPResponse(550, "Access denied");
                return;
            }

            // Check if the directory exists

            disk = (DiskInterface) ftpPath.getSharedDevice().getInterface();
            int sts = disk.fileExists(this, tree, ftpPath.getSharePath());

            if (sts == FileStatus.DirectoryExists)
            {

                // Delete the new directory

                disk.deleteDirectory(this, tree, ftpPath.getSharePath());

                // Check if there are any file/directory change notify requests active

                DiskDeviceContext diskCtx = (DiskDeviceContext) tree.getContext();
                if (diskCtx.hasChangeHandler())
                    diskCtx.getChangeHandler().notifyFileChanged(NotifyChange.ActionRemoved, ftpPath.getSharePath());

                // DEBUG

                if (logger.isDebugEnabled() && hasDebug(DBG_DIRECTORY))
                    logger.debug("DeleteDir ftp="
                            + ftpPath.getFTPPath() + ", share=" + ftpPath.getShareName() + ", path="
                            + ftpPath.getSharePath());
            }
            else
            {

                // File already exists with that name or directory does not
                // exist return an error

                sendFTPResponse(550, sts == FileStatus.FileExists ? "File exists with that name"
                        : "Directory does not exist");
                return;
            }
        }
        catch (Exception ex)
        {
            sendFTPResponse(550, "Failed to delete directory");
            return;
        }

        // Return a success status

        sendFTPResponse(250, "Directory deleted OK");
    }

    /**
	 * Process a machine listing request, single folder
	 * 
	 * @param req
	 *            FTPRequest
	 * @exception IOException
	 */
	protected final void procMachineListing(FTPRequest req) throws IOException {

		// Check if the user is logged in

		if (isLoggedOn() == false) {
			sendFTPResponse(500, "Not logged in");
			return;
		}

		// Check if an argument has been specified

		if (req.hasArgument() == false) {
			sendFTPResponse(501, "Syntax error, parameter required");
			return;
		}

		// Create the path to be listed

		FTPPath ftpPath = generatePathForRequest(req, false, true);
		if (ftpPath == null) {
			sendFTPResponse(500, "Invalid path");
			return;
		}

		// Get the file information

		DiskInterface disk = null;
		TreeConnection tree = null;

		try {

			// Create a temporary tree connection

			tree = getTreeConnection(ftpPath.getSharedDevice());

			// Access the virtual filesystem driver

			disk = (DiskInterface) ftpPath.getSharedDevice().getInterface();

			// Get the file information

			FileInfo finfo = disk.getFileInformation(this, tree, ftpPath
					.getSharePath());

			if (finfo == null) {
				sendFTPResponse(550, "Path " + req.getArgument() + " not available");
				return;
			} else if (finfo.isDirectory() == false) {
				sendFTPResponse(501, "Path " + req.getArgument() + " is not a directory");
				return;
			}

			// Return the folder details

			sendFTPResponse("250- Listing " + req.getArgument());

			StringBuffer mlstStr = new StringBuffer(80);
			mlstStr.append(" ");

			generateMlstString(finfo, m_mlstFacts, mlstStr, true);
			mlstStr.append(CRLF);

			sendFTPResponse(mlstStr.toString());
			sendFTPResponse("250 End");

			// DEBUG

			if ( logger.isDebugEnabled() && hasDebug(DBG_FILE))
				logger.debug("Mlst ftp=" + ftpPath.getFTPPath() + ", share=" + ftpPath.getShareName() + ", info=" + finfo);
		} catch (Exception ex) {
			sendFTPResponse(550, "Error retrieving file information");
		}
	}

	/**
	 * Process a machine listing request, folder contents
	 * 
	 * @param req
	 *            FTPRequest
	 * @exception IOException
	 */
	protected final void procMachineListingContents(FTPRequest req)
			throws IOException {

		// Check if the user is logged in

		if (isLoggedOn() == false) {
			sendFTPResponse(500, "");
			return;
		}

		// Check if the request has an argument, if not then use the current
		// working directory

		if (req.hasArgument() == false)
			req.updateArgument(".");

		// Create the path for the file listing

		FTPPath ftpPath = m_cwd;
		if (req.hasArgument())
			ftpPath = generatePathForRequest(req, true);

		if (ftpPath == null) {
			sendFTPResponse(500, "Invalid path");
			return;
		}

		// Check if the session has the required access

		if (ftpPath.isRootPath() == false) {

			// Check if the session has access to the filesystem

			TreeConnection tree = getTreeConnection(ftpPath.getSharedDevice());
			if (tree == null || tree.hasReadAccess() == false) {

				// Session does not have access to the filesystem

				sendFTPResponse(550, "Access denied");
				return;
			}
		}

		// Send the intermediate response

		sendFTPResponse(150, "File status okay, about to open data connection");

		// Check if there is an active data session

		if (m_dataSess == null) {
			sendFTPResponse(425, "Can't open data connection");
			return;
		}

		// Get the data connection socket

		Socket dataSock = null;

		try {
			dataSock = m_dataSess.getSocket();
		} catch (Exception ex) {
			logger.error(ex);
		}

		if (dataSock == null) {
			sendFTPResponse(426, "Connection closed; transfer aborted");
			return;
		}

		// Output the directory listing to the client

		Writer dataWrt = null;

		try {

			// Open an output stream to the client

			if ( isUTF8Enabled())
		        dataWrt = new OutputStreamWriter(dataSock.getOutputStream(), "UTF-8");
		    else
		        dataWrt = new OutputStreamWriter(dataSock.getOutputStream());

			// Get a list of file information objects for the current directory

			Vector files = null;

			files = listFilesForPath(ftpPath, false, false);

			// Output the file list to the client

			if (files != null) {

				// DEBUG

				if (logger.isDebugEnabled() && hasDebug(DBG_SEARCH))
					logger.debug("MLsd found " + files.size() + " files in " + ftpPath.getFTPPath());

				// Output the file information to the client

				StringBuffer str = new StringBuffer(MLSD_BUFFER_SIZE);

				for (int i = 0; i < files.size(); i++) {

					// Get the current file information

					FileInfo finfo = (FileInfo) files.elementAt(i);

					generateMlstString(finfo, m_mlstFacts, str, false);
					str.append(CRLF);

					// Output the file information record when the buffer is
					// full

					if (str.length() >= MLSD_BUFFER_SIZE) {

						// Output the file data records

						dataWrt.write(str.toString());

						// Reset the buffer

						str.setLength(0);
					}
				}

				// Flush any remaining file record data

				if (str.length() > 0)
					dataWrt.write(str.toString());
			}

			// End of file list transmission

			sendFTPResponse(226, "Closing data connection");
		} catch (Exception ex) {

			// Failed to send file listing

			sendFTPResponse(451, "Error reading file list");
		} finally {

			// Close the data stream to the client

			if (dataWrt != null)
				dataWrt.close();

			// Close the data connection to the client

			if (m_dataSess != null) {
				getFTPServer().releaseDataSession(m_dataSess);
				m_dataSess = null;
			}
		}
	}

	/**
	 * Process a modify date/time command
	 * 
	 * @param req
	 *            FTPRequest
	 * @exception IOException
	 */
	protected final void procModifyDateTime(FTPRequest req) throws IOException {

		// Check if the user is logged in

		if (isLoggedOn() == false) {
			sendFTPResponse(500, "");
			return;
		}

		// Check if an argument has been specified

		if (req.hasArgument() == false) {
			sendFTPResponse(501, "Syntax error, parameter required");
			return;
		}

		// Check the format of the argument to detemine if this is a get or set
		// modify date/time request
		//
		// Get format is just the filename/path
		// Set format is YYYYMMDDHHMMSS <path>

		String path = req.getArgument();
		long modifyDateTime = 0L;

		if (path.length() > MDTM_DATETIME_MINLEN && path.indexOf(' ') != -1) {

			// Check if the first argument looks like a date/time value

			boolean settime = true;
			for (int i = 0; i < MDTM_DATETIME_MINLEN; i++) {
				if (Character.isDigit(path.charAt(i)) == false)
					settime = false;
			}

			// Looks like a date/time value

			if (settime == true) {

				try {

					// Parse the various fields

					int year = Integer.valueOf(path.substring(0, 4)).intValue();
					int month = Integer.valueOf(path.substring(4, 6)).intValue();
					int day = Integer.valueOf(path.substring(6, 8)).intValue();

					int hours = Integer.valueOf(path.substring(8, 10)).intValue();
					int mins = Integer.valueOf(path.substring(10, 12)).intValue();
					int secs = Integer.valueOf(path.substring(12, 14)).intValue();

					// Check if the date/time includes milliseconds

					int millis = 0;
					int sep = path.indexOf(' ', MDTM_DATETIME_MINLEN);

					if (path.charAt(MDTM_DATETIME_MINLEN) == '.') {

						// Find the seperator between the date/time and path

						millis = Integer.valueOf(path.substring(MDTM_DATETIME_MINLEN + 1, sep))
								.intValue();
					}

					// Create the modify date/time

					Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

					cal.set(year, month, day, hours, mins, secs);
					if (millis != 0)
						cal.set(Calendar.MILLISECOND, millis);

					// Get the modify date/time

					modifyDateTime = cal.getTimeInMillis();

					// Remove the date/time from the request argument

					path = path.substring(sep + 1);
					req.updateArgument(path);

					// DEBUG

					if (logger.isDebugEnabled() && hasDebug(DBG_FILE))
						logger.debug("Modify date/time arg=" + path	+ ", utcTime=" + modifyDateTime);
				} catch (NumberFormatException ex) {
				}
			}
		}

		// Create the path for the file listing

		FTPPath ftpPath = generatePathForRequest(req, true);
		if (ftpPath == null) {
			sendFTPResponse(550, "Invalid path");
			return;
		}

		// Get the file information

		DiskInterface disk = null;
		TreeConnection tree = null;

		try {

			// Create a temporary tree connection

			tree = getTreeConnection(ftpPath.getSharedDevice());

			// Access the virtual filesystem driver

			disk = (DiskInterface) ftpPath.getSharedDevice().getInterface();

			// Check if the modify date/time should be set

			if (modifyDateTime != 0L) {

				// Set the file/folder modification date/time

				FileInfo finfo = new FileInfo();
				finfo.setModifyDateTime(modifyDateTime);
				finfo.setFileInformationFlags(FileInfo.SetModifyDate);

				disk.setFileInformation(this, tree, ftpPath.getSharePath(),
						finfo);
			}

			// Get the file information

			FileInfo finfo = disk.getFileInformation(this, tree, ftpPath
					.getSharePath());

			if (finfo == null) {
				sendFTPResponse(550, "File " + req.getArgument()
						+ " not available");
				return;
			}

			// Return the file modification date/time

			if (finfo.hasModifyDateTime())
				sendFTPResponse(213, FTPDate.packMlstDateTime(finfo
						.getModifyDateTime()));
			else
				sendFTPResponse(550,
						"Modification date/time not available for "
								+ finfo.getFileName());

			// DEBUG

			if (logger.isDebugEnabled() && hasDebug(DBG_FILE))
				logger.debug("File modify date/time ftp="
						+ ftpPath.getFTPPath() + ", share="
						+ ftpPath.getShareName() + ", modified="
						+ finfo.getModifyDateTime());
		} catch (Exception ex) {
			sendFTPResponse(550, "Error retrieving file modification date/time");
		}
	}

	/**
	 * Process a server features request
	 * 
	 * @param req
	 *            FTPRequest
	 * @exception IOException
	 */
	protected final void procFeatures(FTPRequest req) throws IOException {

		// Return the list of supported server features

		sendFTPResponse("211-Features supported");

		// MOdify date/time and size commands supported

		if ( FeatureMDTM)
			sendFTPResponse(" MDTM");
		
		if ( FeatureSIZE)
			sendFTPResponse(" SIZE");
		
		if ( FeatureUTF8)
			sendFTPResponse(" UTF8");

		// Machine listing supported, build the fact list

		if ( FeatureMLST)
		{
			StringBuffer mlstStr = new StringBuffer();
	
			mlstStr.append(" MLST ");
	
			for (int i = 0; i < _factNames.length; i++) {
	
				// Output the fact name
	
				mlstStr.append(_factNames[i]);
	
				// Check if the fact is enabled by default
	
				if ((MLST_DEFAULT & (1 << i)) != 0)
					mlstStr.append("*");
				mlstStr.append(";");
			}
	
			sendFTPResponse(mlstStr.toString());
			sendFTPResponse(" MLSD");
		}
		
		sendFTPResponse(211, "END");
	}
    
    /**
	 * Process a file size command
	 * 
	 * @param req
	 *            FTPRequest
	 * @exception IOException
	 */
    protected final void procFileSize(FTPRequest req) throws IOException
    {

        // Check if the user is logged in

        if (isLoggedOn() == false)
        {
            sendFTPResponse(500, "");
            return;
        }

        // Check if an argument has been specified

        if (req.hasArgument() == false)
        {
            sendFTPResponse(501, "Syntax error, parameter required");
            return;
        }

        // Create the path for the file listing

        FTPPath ftpPath = generatePathForRequest(req, true);
        if (ftpPath == null)
        {
            sendFTPResponse(550, "Invalid path");
            return;
        }

        // Get the file information

        DiskInterface disk = null;
        TreeConnection tree = null;

        try
        {

            // Create a temporary tree connection

            tree = getTreeConnection(ftpPath.getSharedDevice());

            // Access the virtual filesystem driver

            disk = (DiskInterface) ftpPath.getSharedDevice().getInterface();

            // Get the file information

            FileInfo finfo = disk.getFileInformation(this, tree, ftpPath.getSharePath());

            if (finfo == null)
            {
                sendFTPResponse(550, "File " + req.getArgument() + " not available");
                return;
            }

            // Return the file size

            sendFTPResponse(213, "" + finfo.getSize());

            // DEBUG

            if (logger.isDebugEnabled() && hasDebug(DBG_FILE))
                logger.debug("File size ftp="
                        + ftpPath.getFTPPath() + ", share=" + ftpPath.getShareName() + ", size=" + finfo.getSize());
        }
        catch (Exception ex)
        {
            sendFTPResponse(550, "Error retrieving file size");
        }
    }

    /**
     * Process a site specific command
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procSite(FTPRequest req)
      	throws IOException
    {
      //  Check if the user is logged in

      if (isLoggedOn() == false) {
    	  sendFTPResponse(500, "");
    	  return;
      }

      // Check if the FTP server has a site interface
      
      if ( getFTPServer().hasSiteInterface()) {
        
	      // Pass the request to the site interface
	        
	      FTPSiteInterface siteInterface = getFTPServer().getSiteInterface();
	        
	      siteInterface.processFTPSiteCommand( this, req);
      }
      else {
        
	      // SITE command not implemented
	        
	      sendFTPResponse( 501, "SITE commands not implemented");
      }
    }
    
    /**
     * Process a structure command. This command is obsolete.
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procStructure(FTPRequest req) throws IOException
    {

        // Check for the file structure argument

        if (req.hasArgument() && req.getArgument().equalsIgnoreCase("F"))
            sendFTPResponse(200, "OK");

        // Return an error response

        sendFTPResponse(504, "Obsolete");
    }

    /**
     * Process a mode command. This command is obsolete.
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procMode(FTPRequest req) throws IOException
    {

        // Check for the stream transfer mode argument

        if (req.hasArgument() && req.getArgument().equalsIgnoreCase("S"))
            sendFTPResponse(200, "OK");

        // Return an error response

        sendFTPResponse(504, "Obsolete");
    }

    /**
     * Abort an active file transfer
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procAbort(FTPRequest req) throws IOException
    {
    	// Check if threaded transfers are enabled
    	
    	if ( UseThreadedDataTransfer == true)
    	{
    		// Check if there is an active data connection
    		
    		if ( m_dataSess != null)
    		{
    			// Abort the data transfer
    			
    			m_dataSess.abortTransfer();
    		}
    		else
    		{
    			// Send an error status, no transfer in progress
    			
    			sendFTPResponse( 226, "Data connection not active");
    		}
    	}
    	else
    	{
    		// Abort not implemented for inline transfers
    		
    		sendFTPResponse( 502, "Abort not implemented");
    	}
    }
    
    /**
     * Process an allocate command. This command is obsolete.
     * 
     * @param req FTPRequest
     * @exception IOException
     */
    protected final void procAllocate(FTPRequest req) throws IOException
    {

        // Return a response

        sendFTPResponse(202, "Obsolete");
    }

    /**
     * Build a list of file name or file information objects for the specified
     * server path
     * 
     * @param path FTPPath
     * @param nameOnly boolean
     * @param hidden boolean
     * @return Vector<FileInfo>
     */
    protected final Vector<FileInfo> listFilesForPath(FTPPath path, boolean nameOnly, boolean hidden)
    {

        // Check if the path is valid

        if (path == null)
            return null;

        // Check if the path is the root path

        Vector<FileInfo> files = new Vector<FileInfo>();

        if (path.hasSharedDevice() == false)
        {

            // The first level of directories are mapped to the available shares
            // Guest users only see their own list of shares

            SharedDeviceList shares = null;
            if ( getClientInformation().isGuest())
                shares = getDynamicShareList();
            else
                shares = getShareList();
            
            if (shares != null)
            {

                // Search for disk shares

                Enumeration<SharedDevice> enm = shares.enumerateShares();

                while (enm.hasMoreElements())
                {

                    // Get the current shared device

                    SharedDevice shr = enm.nextElement();

                    // Add the share name or full information to the list

                    if (nameOnly == false)
                    {

                        // Create a file information object for the top level
                        // directory details

                        FileInfo finfo = new FileInfo(shr.getName(), 0L, FileAttribute.Directory);
                        files.add(finfo);
                    }
                    else
                        files.add(new FileInfo(shr.getName(), 0L, FileAttribute.Directory));
                }
            }
        }
        else
        {

            // Append a wildcard to the search path

            String searchPath = path.getSharePath();

            if (path.isDirectory())
                searchPath = path.makeSharePathToFile("*");

            // Create a temporary tree connection

            TreeConnection tree = new TreeConnection(path.getSharedDevice());

            // Start a search on the specified disk share

            DiskInterface disk = null;
            SearchContext ctx = null;

            int searchAttr = FileAttribute.Directory + FileAttribute.Normal;
            if (hidden)
                searchAttr += FileAttribute.Hidden;

            try
            {
                disk = (DiskInterface) path.getSharedDevice().getInterface();
                ctx = disk.startSearch(this, tree, searchPath, searchAttr);
            }
            catch (Exception ex)
            {
            }

            // Add the files to the list

            if (ctx != null)
            {

                // Get the file names/information

            	FileInfo finfo = new FileInfo();
            	
                while (ctx.hasMoreFiles())
                {
                	// Get the next file details
                	
                	if ( ctx.nextFileInfo( finfo) == false)
                		break;
                	
                	// Filter out link nodes
                	
                	if ( finfo.isFileType() != FileType.SymbolicLink)
                	{
	                    // Check if a file name or file information is required
	
	                    if (nameOnly)
	                    {
	                        // Add a file name to the list
	
	                        files.add(new FileInfo(ctx.nextFileName(), 0L, 0));
	                    }
	                    else
	                    {
	                    	// add the file information
	                    	
	                        if (finfo.getFileName() != null)
	                            files.add(finfo);
	                    }
	                    
	                    // Allocate a new file information object
	                    
	                    finfo = new FileInfo();
                	}
                }
            }
        }

        // Return the list of file names/information

        return files;
    }

    /**
     * Get the list of filtered shares that are available to this session
     * 
     * @return SharedDeviceList
     */
    protected final SharedDeviceList getShareList()
    {

        // Check if the filtered share list has been initialized

        if (m_shares == null)
        {

            // Get a list of shared filesystems

            SharedDeviceList shares = getFTPServer().getShareMapper().getShareList(getFTPServer().getServerName(),
                    this, false);

            // Search for disk shares

            m_shares = new SharedDeviceList();
            Enumeration enm = shares.enumerateShares();

            while (enm.hasMoreElements())
            {

                // Get the current shared device

                SharedDevice shr = (SharedDevice) enm.nextElement();

                // Check if the share is a disk share

                if (shr instanceof DiskSharedDevice)
                    m_shares.addShare(shr);
            }

            // Check if there is an access control manager available, if so then
            // filter the list of
            // shared filesystems

            if (getServer().hasAccessControlManager())
            {

                // Get the access control manager

                AccessControlManager aclMgr = getServer().getAccessControlManager();

                // Filter the list of shared filesystems

                m_shares = aclMgr.filterShareList(this, m_shares);
            }
        }

        // Return the filtered shared filesystem list

        return m_shares;
    }

    /**
     * Get a tree connection for the specified shared device. Creates and caches
     * a new tree connection if required.
     * 
     * @param share SharedDevice
     * @return TreeConnection
     */
    protected final TreeConnection getTreeConnection(SharedDevice share)
    {

        // Check if the share is valid

        if (share == null)
            return null;

        // Check if there is a tree connection in the cache

        TreeConnection tree = m_connections.findConnection(share.getName());
        if (tree == null)
        {

            // Create a new tree connection, do not add dynamic shares to the connection cache

            tree = new TreeConnection(share);
            if ( share.isTemporary() == false)
                m_connections.addConnection(tree);

            // Set the access permission for the shared filesystem

            if (getServer().hasAccessControlManager())
            {

                // Set the access permission to the shared filesystem

                AccessControlManager aclMgr = getServer().getAccessControlManager();

                int access = aclMgr.checkAccessControl(this, share);
                if (access != AccessControl.Default)
                    tree.setPermission(access);
            }
        }

        // Return the connection

        return tree;
    }

    /**
     * Create a disk share for the home folder
     * 
     * @param client ClientInfo
     * @return DiskSharedDevice
     */
    private final DiskSharedDevice createHomeDiskShare(ClientInfo client)
    {
        // Check if the home folder has been set for the user
        
        if ( client.hasHomeFolder() == false)
        {
            // Get the required services
            
            NodeService nodeService = getServer().getConfiguration().getNodeService();
            PersonService personService = getServer().getConfiguration().getPersonService();
            TransactionService transService = getServer().getConfiguration().getTransactionService();
            
            // Get the home folder for the user
            
            UserTransaction tx = transService.getUserTransaction();
            NodeRef homeSpaceRef = null;
            
            try
            {
                tx.begin();
                homeSpaceRef = (NodeRef) nodeService.getProperty( personService.getPerson(client.getUserName()),
                        ContentModel.PROP_HOMEFOLDER);
                client.setHomeFolder( homeSpaceRef);
                tx.commit();
            }
            catch (Throwable ex)
            {
                try
                {
                    tx.rollback();
                }
                catch (Exception ex2)
                {
                    logger.error("Failed to rollback transaction", ex2);
                }
                
                if(ex instanceof RuntimeException)
                {
                    throw (RuntimeException)ex;
                }
                else
                {
                    throw new RuntimeException("Failed to get home folder", ex);
                }
            }
        }
        
        //  Create the disk driver and context
        
        DiskInterface diskDrv = getServer().getConfiguration().getDiskInterface();
        DiskDeviceContext diskCtx = new ContentContext( client.getUserName(), "", "", client.getHomeFolder());

        //  Default the filesystem to look like an 80Gb sized disk with 90% free space

        diskCtx.setDiskInformation(new SrvDiskInfo(2560, 64, 512, 2304));
        
        //  Create a temporary shared device for the users home directory
        
        return new DiskSharedDevice( client.getUserName(), diskDrv, diskCtx, SharedDevice.Temporary);
    }
    
    /**
     * Start the FTP session in a seperate thread
     */
    public void run()
    {
        
        try
        {

            // Debug

            if (logger.isDebugEnabled() && hasDebug(DBG_STATE))
                logger.debug("FTP session started");

            // Create the input/output streams

            m_in  = m_sock.getInputStream();
            m_out = new OutputStreamWriter(m_sock.getOutputStream());

            m_inbuf  = new byte[512];
            m_outbuf = new StringBuffer(256);

            // Return the initial response

            sendFTPResponse(220, "FTP server ready");

            // Start/end times if timing debug is enabled

            long startTime = 0L;
            long endTime = 0L;

            // Create an FTP request to hold command details

            FTPRequest ftpReq = new FTPRequest();

            // The server session loops until the NetBIOS hangup state is set.

            int rdlen = -1;
            String cmd = null;

            while (m_sock != null)
            {

                // Wait for a data packet

                rdlen = m_in.read(m_inbuf);

                // Check if there is no more data, the other side has dropped
                // the connection

                if (rdlen == -1)
                {
                    closeSession();
                    continue;
                }

                // Trim the trailing <CR><LF>

                if (rdlen > 0)
                {
                    while (rdlen > 0 && m_inbuf[rdlen - 1] == '\r' || m_inbuf[rdlen - 1] == '\n')
                        rdlen--;
                }

                // Get the command string, decode as UTF-8 if enabled

                if ( isUTF8Enabled())
                {
                	// Convert the string from UTF-8
                	
               		cmd = sun.text.Normalizer.compose( new String(m_inbuf, 0, rdlen, "UTF-8"), false, 0);
                }
                else
                {
                	// Convert the request using the configured character set

                	cmd = new String(m_inbuf, 0, rdlen, getFTPServer().getCharacterSet());
                }
                
                // Debug

                if (logger.isDebugEnabled() && hasDebug(DBG_TIMING))
                    startTime = System.currentTimeMillis();

                if (logger.isDebugEnabled() && hasDebug(DBG_PKTTYPE))
                    logger.debug("Cmd " + ftpReq);

                // Parse the received command, and validate

                ftpReq.setCommandLine(cmd);
                m_reqCount++;

                switch (ftpReq.isCommand())
                {
                // User command

                case FTPCommand.User:
                    procUser(ftpReq);
                    break;

                // Password command

                case FTPCommand.Pass:
                    procPassword(ftpReq);
                    break;

                // Quit command

                case FTPCommand.Quit:
                    procQuit(ftpReq);
                    break;

                // Type command

                case FTPCommand.Type:
                    procType(ftpReq);
                    break;

                // Port command

                case FTPCommand.Port:
                    procPort(ftpReq);
                    break;

                // Passive command

                case FTPCommand.Pasv:
                    procPassive(ftpReq);
                    break;

                // Restart position command

                case FTPCommand.Rest:
                    procRestart(ftpReq);
                    break;

                // Return file command

                case FTPCommand.Retr:
                    procReturnFile(ftpReq);

                    // Reset the restart position

                    m_restartPos = 0;
                    break;

                // Store file command

                case FTPCommand.Stor:
                    procStoreFile(ftpReq, false);
                    break;

                // Append file command
                    
                case FTPCommand.Appe:
                  procStoreFile(ftpReq, true);
                  break;
                  
                // Print working directory command

                case FTPCommand.Pwd:
                case FTPCommand.XPwd:
                    procPrintWorkDir(ftpReq);
                    break;

                // Change working directory command

                case FTPCommand.Cwd:
                case FTPCommand.XCwd:
                    procChangeWorkDir(ftpReq);
                    break;

                // Change to previous directory command

                case FTPCommand.Cdup:
                case FTPCommand.XCup:
                    procCdup(ftpReq);
                    break;

                // Full directory listing command

                case FTPCommand.List:
                    procList(ftpReq);
                    break;

                // Short directory listing command

                case FTPCommand.Nlst:
                    procNList(ftpReq);
                    break;

                // Delete file command

                case FTPCommand.Dele:
                    procDeleteFile(ftpReq);
                    break;

                // Rename file from command

                case FTPCommand.Rnfr:
                    procRenameFrom(ftpReq);
                    break;

                // Rename file to comand

                case FTPCommand.Rnto:
                    procRenameTo(ftpReq);
                    break;

                // Create new directory command

                case FTPCommand.Mkd:
                case FTPCommand.XMkd:
                    procCreateDirectory(ftpReq);
                    break;

                // Delete directory command

                case FTPCommand.Rmd:
                case FTPCommand.XRmd:
                    procRemoveDirectory(ftpReq);
                    break;

                // Return file size command

                case FTPCommand.Size:
                    procFileSize(ftpReq);
                    break;

                // Set modify date/time command

                case FTPCommand.Mdtm:
                    procModifyDateTime(ftpReq);
                    break;

                // System status command

                case FTPCommand.Syst:
                    procSystemStatus(ftpReq);
                    break;

                // Server status command

                case FTPCommand.Stat:
                    procServerStatus(ftpReq);
                    break;

                // Help command

                case FTPCommand.Help:
                    procHelp(ftpReq);
                    break;

                // No-op command

                case FTPCommand.Noop:
                    procNoop(ftpReq);
                    break;

                // Structure command (obsolete)

                case FTPCommand.Stru:
                    procStructure(ftpReq);
                    break;

                // Mode command (obsolete)

                case FTPCommand.Mode:
                    procMode(ftpReq);
                    break;

                // Allocate command (obsolete)

                case FTPCommand.Allo:
                    procAllocate(ftpReq);
                    break;

                // Abort an active file data transfer
                    
                case FTPCommand.Abor:
                	procAbort(ftpReq);
                	break;
                	
                // Return the list of features that this server supports
                    
                case FTPCommand.Feat:
                	procFeatures(ftpReq);
                	break;
                	
                //  Options command
                  
                case FTPCommand.Opts:
                  procOptions(ftpReq);
                  break;
                  
                //  Machine listing, single folder
                  
                case FTPCommand.MLst:
                  procMachineListing(ftpReq);
                  break;
                  
                //  Machine listing, folder contents
                  
                case FTPCommand.MLsd:
                  procMachineListingContents(ftpReq);
                  break;
                  
                //  Site specific commands
                  
                case FTPCommand.Site:
                  procSite( ftpReq);
                  break;
                  
                // Unknown/unimplemented command

                default:
                    if (ftpReq.isCommand() != FTPCommand.InvalidCmd)
                        sendFTPResponse(502, "Command "
                                + FTPCommand.getCommandName(ftpReq.isCommand()) + " not implemented");
                    else
                        sendFTPResponse(502, "Command not implemented");
                    break;
                }

                // Debug

                if (logger.isDebugEnabled() && hasDebug(DBG_TIMING))
                {
                    endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    if (duration > 20)
                        logger.debug("Processed cmd "
                                + FTPCommand.getCommandName(ftpReq.isCommand()) + " in " + duration + "ms");
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
                
            } // end while state
        }
        catch (SocketException ex)
        {

            // DEBUG

            if (logger.isErrorEnabled() && hasDebug(DBG_STATE))
                logger.error("Socket closed by remote client");
        }
        catch (Exception ex)
        {

            // Output the exception details

            if (isShutdown() == false)
            {
                logger.debug(ex);
            }
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

        closeSession();

        // Debug

        if (hasDebug(DBG_STATE))
            logger.debug("Server session closed");
    }
    
    /**
     * Authenticate an associated FTP data session using the same credentials as the main FTP session
     * 
     * @exception AuthenticationException
     */
    protected void authenticateDataSession() throws org.alfresco.repo.security.authentication.AuthenticationException
    {
        // Use the normal authentication service as we have the plaintext password
        
        AuthenticationService authService = getServer().getConfiguration().getAuthenticationService();
        
        // Authenticate the user

        ClientInfo cInfo = getClientInformation();
        
        if ( cInfo.isGuest())
        {
            // Authenticate as the guest user
            
            authService.authenticateAsGuest();
        }
        else
        {
            // Authenticate as a normal user

            authService.authenticate( cInfo.getUserName(), cInfo.getPasswordAsCharArray());
        }
    }
    
    /**
     * Generate a machine listing string for the specified file/folder information
     * 
     * @param finfo FileInfo
     * @param mlstFlags int
     * @param buf StringBuffer
     * @param isMlsd boolean
     */
    protected final void generateMlstString(FileInfo finfo, int mlstFlags, StringBuffer buf, boolean isMlsd)
    {
    	// Create the machine listing record

		for (int i = 0; i < _factNames.length; i++) {

			// Check if the current fact is enabled

			int curFact = 1 << i;

			if ((mlstFlags & curFact) != 0) {

				// Output the fact value

				switch (curFact) {

				// File size

				case MLST_SIZE:
					buf.append(_factNames[i]);
					buf.append("=");
					buf.append(finfo.getSize());
					buf.append(";");
					break;

				// Modify date/time

				case MLST_MODIFY:
					if (finfo.hasModifyDateTime()) {
						buf.append(_factNames[i]);
						buf.append("=");
						buf.append(FTPDate.packMlstDateTime(finfo
								.getModifyDateTime()));
						buf.append(";");
					}
					break;

				// Creation date/time

				case MLST_CREATE:
					if (finfo.hasCreationDateTime()) {
						buf.append(_factNames[i]);
						buf.append("=");
						buf.append(FTPDate.packMlstDateTime(finfo
								.getCreationDateTime()));
						buf.append(";");
					}
					break;

				// Type

				case MLST_TYPE:
					buf.append(_factNames[i]);

					if (finfo.isDirectory() == false) {
						buf.append("=file;");
					} else {
						buf.append("=dir;");
					}
					break;

				// Unique identifier

				case MLST_UNIQUE:
					if (finfo.getFileId() != -1) {
						buf.append(_factNames[i]);
						buf.append("=");
						buf.append(finfo.getFileId());
						buf.append(";");
					}
					break;

				// Permissions

				case MLST_PERM:
					buf.append(_factNames[i]);
					buf.append("=");
					if (finfo.isDirectory()) {
						buf.append(finfo.isReadOnly() ? "el" : "ceflmp");
					} else {
						buf.append(finfo.isReadOnly() ? "r" : "rwadf");
					}
					buf.append(";");
					break;

				// Media-type

				case MLST_MEDIATYPE:
					break;
				}
			}
		}

		// Add the file name

		buf.append(" ");
		buf.append(finfo.getFileName());
	}
}