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
package org.alfresco.filesys.ftp;

import java.net.*;
import java.io.*;

import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.filesys.AccessMode;
import org.alfresco.filesys.server.filesys.DiskDeviceContext;
import org.alfresco.filesys.server.filesys.DiskInterface;
import org.alfresco.filesys.server.filesys.FileAction;
import org.alfresco.filesys.server.filesys.FileOpenParams;
import org.alfresco.filesys.server.filesys.FileStatus;
import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.server.filesys.NotifyChange;
import org.alfresco.filesys.server.filesys.TreeConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * FTP Data Session Class
 * <p>
 * A data connection is made when a PORT or PASV FTP command is received on the main control
 * session.
 * <p>
 * The PORT command will actively connect to the specified address/port on the client. The PASV
 * command will create a listening socket and wait for the client to connect.
 * 
 * @author GKSpencer
 */
public class FTPDataSession extends SrvSession implements Runnable
{
    // Debug logging

    private static final Log logger = LogFactory.getLog("org.alfresco.ftp.protocol");

    // Data session command types
    
    public enum DataCommand { StoreFile, ReturnFile };
    
    // FTP session that this data connection is associated with

    private FTPSrvSession m_cmdSess;

    // Connection details for active connection

    private InetAddress m_clientAddr;
    private int m_clientPort;

    // Local port to use

    private int m_localPort;

    // Active data session socket

    private Socket m_activeSock;

    // Passive data session socket

    private ServerSocket m_passiveSock;

    // Transfer in progress and abort file transfer flags

    private boolean m_transfer;
    private boolean m_abort;

    // Send/receive data byte count

    private long m_bytCount;
    
    // Data command type
    
    private DataCommand m_dataCmd;
    
    // Requested file name
    
    private String m_reqFileName;
    
    // Path to the local file
    
    private FTPPath m_ftpPath;

    // Restart position
    
    private long m_restartPos;
    
    //	Thread that runs the data command
    
    private Thread m_dataThread;
    
    /**
     * Class constructor
     * <p>
     * Create a data connection that listens for an incoming connection.
     * 
     * @param sess FTPSrvSession
     * @exception IOException
     */
    protected FTPDataSession(FTPSrvSession sess) throws IOException
    {
    	// Setup the base class
    	
    	super( -1, sess.getServer(), "FTPDATA", null);
    	
        // Set the associated command session

        m_cmdSess = sess;

        // Create a server socket to listen for the incoming connection

        m_passiveSock = new ServerSocket(0, 1, null);
    }

    /**
     * Class constructor
     * <p>
     * Create a data connection that listens for an incoming connection on the specified network
     * adapter and local port.
     * 
     * @param sess FTPSrvSession
     * @param localPort int
     * @param addr InetAddress
     * @exception IOException
     */
    protected FTPDataSession(FTPSrvSession sess, int localPort, InetAddress bindAddr) throws IOException
    {
    	// Setup the base class
    	
    	super( -1, sess.getServer(), "FTPDATA", null);

        // Set the associated command session

        m_cmdSess = sess;

        // Create a server socket to listen for the incoming connection on the specified network
        // adapter

        m_localPort = localPort;
        m_passiveSock = new ServerSocket(localPort, 1, bindAddr);
    }

    /**
     * Class constructor
     * <p>
     * Create a data connection that listens for an incoming connection on the specified network
     * adapter.
     * 
     * @param sess FTPSrvSession
     * @param addr InetAddress
     * @exception IOException
     */
    protected FTPDataSession(FTPSrvSession sess, InetAddress bindAddr) throws IOException
    {
    	// Setup the base class
    	
    	super( -1, sess.getServer(), "FTPDATA", null);

        // Set the associated command session

        m_cmdSess = sess;

        // Create a server socket to listen for the incoming connection on the specified network
        // adapter

        m_passiveSock = new ServerSocket(0, 1, bindAddr);
    }

    /**
     * Class constructor
     * <p>
     * Create a data connection to the specified client address and port.
     * 
     * @param sess FTPSrvSession
     * @param addr InetAddress
     * @param port int
     */
    protected FTPDataSession(FTPSrvSession sess, InetAddress addr, int port)
    {
    	// Setup the base class
    	
    	super( -1, sess.getServer(), "FTPDATA", null);

        // Set the associated command session

        m_cmdSess = sess;

        // Save the client address/port details, the actual connection will be made later when
        // the client requests/sends a file

        m_clientAddr = addr;
        m_clientPort = port;
    }

    /**
     * Class constructor
     * <p>
     * Create a data connection to the specified client address and port, using the specified local
     * port.
     * 
     * @param sess FTPSrvSession
     * @param localPort int
     * @param addr InetAddress
     * @param port int
     */
    protected FTPDataSession(FTPSrvSession sess, int localPort, InetAddress addr, int port)
    {
    	// Setup the base class
    	
    	super( -1, sess.getServer(), "FTPDATA", null);

        // Set the associated command session

        m_cmdSess = sess;

        // Save the local port

        m_localPort = localPort;

        // Save the client address/port details, the actual connection will be made later when
        // the client requests/sends a file

        m_clientAddr = addr;
        m_clientPort = port;
    }

    /**
     * Return the associated command session
     * 
     * @return FTPSrvSession
     */
    public final FTPSrvSession getCommandSession()
    {
        return m_cmdSess;
    }

    /**
     * Return the local port
     * 
     * @return int
     */
    public final int getLocalPort()
    {
        if (m_passiveSock != null)
            return m_passiveSock.getLocalPort();
        else if (m_activeSock != null)
            return m_activeSock.getLocalPort();
        return -1;
    }

    /**
     * Return the port that was allocated to the data session
     * 
     * @return int
     */
    protected final int getAllocatedPort()
    {
        return m_localPort;
    }

    /**
     * Return the passive server socket address
     * 
     * @return InetAddress
     */
    public final InetAddress getPassiveAddress()
    {
        if (m_passiveSock != null)
        {

            // Get the server socket local address

            InetAddress addr = m_passiveSock.getInetAddress();
            if (addr.getHostAddress().compareTo("0.0.0.0") == 0)
            {
                try
                {
                    addr = InetAddress.getLocalHost();
                }
                catch (UnknownHostException ex)
                {
                }
            }
            return addr;
        }
        return null;
    }

    /**
     * Return the passive server socket port
     * 
     * @return int
     */
    public final int getPassivePort()
    {
        if (m_passiveSock != null)
            return m_passiveSock.getLocalPort();
        return -1;
    }

    /**
     * Determine if a file transfer is active
     * 
     * @return boolean
     */
    public final boolean isTransferActive()
    {
        return m_transfer;
    }

    /**
     * Determine if the transfer has been aborted
     * 
     * @return boolean
     */
    public final boolean isTransferAborted()
    {
    	return m_abort;
    }
    
    /**
     * Abort an in progress file transfer
     */
    public final void abortTransfer()
    {
        m_abort = true;
    }

    /**
     * Return the transfer byte count
     * 
     * @return long
     */
    public final synchronized long getTransferByteCount()
    {
        return m_bytCount;
    }

    /**
     * Return the data socket connected to the client
     * 
     * @return Socket
     * @exception IOException
     */
    public final Socket getSocket() throws IOException
    {

        // Check for a passive connection, get the incoming socket connection

        if (m_passiveSock != null)
            m_activeSock = m_passiveSock.accept();
        else
        {
            if (m_localPort != 0)
            {

                // Use the specified local port

                m_activeSock = new Socket(m_clientAddr, m_clientPort, null, m_localPort);
            }
            else
                m_activeSock = new Socket(m_clientAddr, m_clientPort);
        }

        // Set the socket to close immediately

        m_activeSock.setSoLinger(false, 0);
        m_activeSock.setTcpNoDelay(true);

        // Return the data socket

        return m_activeSock;
    }

    /**
     * Close the data connection
     */
    public final void closeSession()
    {

        // If the data connection is active close it

        if (m_activeSock != null)
        {
            try
            {
                m_activeSock.close();
            }
            catch (Exception ex)
            {
            }
            m_activeSock = null;
        }

        // Close the listening socket for a passive connection

        if (m_passiveSock != null)
        {
            try
            {
                m_passiveSock.close();
            }
            catch (Exception ex)
            {
            }
            m_passiveSock = null;
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
    }

    /**
     * Store a file using a seperate thread to receive the data and write the file
     *
     * @param ftpPath FTPPath
     */
    public final void doStoreFile( FTPPath ftpPath, long restartPos, String reqFileName)
    {
    	// Set the transfer details
    	
    	m_dataCmd     = DataCommand.StoreFile;
    	m_ftpPath     = ftpPath;
    	m_restartPos  = restartPos;
    	m_reqFileName = reqFileName;
    	
    	// Run the transfer in a seperate thread
    	
    	m_dataThread = new Thread(this);
    	m_dataThread.setName(m_cmdSess.getUniqueId() + "_DATA_STORE");
    	m_dataThread.start();
    }
    
    /**
     * Return a file using a seperate thread to read the file and send the data
     *
     * @param ftpPath FTPPath
     */
    public final void doReturnFile( FTPPath ftpPath, long restartPos, String reqFileName)
    {
    	// Set the transfer details
    	
    	m_dataCmd     = DataCommand.ReturnFile;
    	m_ftpPath     = ftpPath;
    	m_restartPos  = restartPos;
    	m_reqFileName = reqFileName;
    	
    	// Run the transfer in a seperate thread
    	
    	m_dataThread = new Thread(this);
    	m_dataThread.setName(m_cmdSess.getUniqueId() + "_DATA_RETURN");
    	m_dataThread.start();
    }
    
    /**
     * Run a file send/receive in a seperate thread
     */
    public void run()
    {
    	// Setup the authentication context as we are running in a seperate thread from the main FTP session
    	
    	try
    	{
    		// Setup the authentication context for the thread
    		
    		m_cmdSess.authenticateDataSession();

    		// Run the required data command
        	
        	switch ( m_dataCmd)
        	{
        	// Store a file
        	
        	case StoreFile:
        		runStoreFile();
        		break;
        		
        	// Return a file
        		
        	case ReturnFile:
        		runReturnFile();
        		break;
        	}
    	}
    	catch ( org.alfresco.repo.security.authentication.AuthenticationException ex)
    	{
    		if ( logger.isErrorEnabled())
    			logger.error("Failed to authenticate FTP data session", ex);
    		
            // Close the data connection to the client
    		
            m_cmdSess.getFTPServer().releaseDataSession(this);
            closeSession();
    	}
    }
    
    /**
     * Return a file to the client
     */
    private final void runReturnFile()
    {
        // Send the file to the client

        OutputStream os = null;
        DiskInterface disk = null;
        TreeConnection tree = null;
        NetworkFile netFile = null;
        Socket dataSock = null;

        try
        {

            // Open an output stream to the client

        	dataSock = getSocket();
            os = dataSock.getOutputStream();

            // Create a temporary tree connection

            tree = m_cmdSess.getTreeConnection(m_ftpPath.getSharedDevice());

            // Check if the file exists and it is a file, if so then open the
            // file

            disk = (DiskInterface) m_ftpPath.getSharedDevice().getInterface();

            // Create the file open parameters

            FileOpenParams params = new FileOpenParams(m_ftpPath.getSharePath(), FileAction.OpenIfExists,
                    AccessMode.ReadOnly, 0);

            // Check if the file exists and it is a file

            int sts = disk.fileExists( this, tree, m_ftpPath.getSharePath());

            if (sts == FileStatus.FileExists)
            {

                // Open the file

                netFile = disk.openFile( this, tree, params);
            }

            // Check if the file has been opened

            if (netFile == null)
            {
                m_cmdSess.sendFTPResponse(550, "File " + m_reqFileName + " not available");
                return;
            }

            // Allocate the buffer for the file data

            byte[] buf = new byte[FTPSrvSession.DEFAULT_BUFFERSIZE];
            long filePos = m_restartPos;

            int len = -1;

            while (filePos < netFile.getFileSize())
            {

                // Read another block of data from the file

                len = disk.readFile( this, tree, netFile, buf, 0, buf.length, filePos);

                // DEBUG

                if (logger.isDebugEnabled() && m_cmdSess.hasDebug(FTPSrvSession.DBG_FILEIO))
                    logger.debug(" Write len=" + len + " bytes");

                // Write the current data block to the client, update the file position

                if (len > 0)
                {

                    // Write the data to the client

                    os.write(buf, 0, len);

                    // Update the file position

                    filePos += len;
                    
                    // Update the transfer byte count
                    
                    m_bytCount += len;
                }
                
                // Check if the transfer has been aborted
                
                if ( isTransferAborted())
                {
                    // DEBUG

                    if (logger.isDebugEnabled() && m_cmdSess.hasDebug(FTPSrvSession.DBG_FILE))
                        logger.debug(" Transfer aborted (RETR)");
                    
                	// Send a status to the client
                	
                	sendFTPResponse( 226, "Aborted data connection");
                	
                	// Finally block will cleanup
                	
                	return;
                }
            }

            // Close the output stream to the client

            os.close();
            os = null;

            // Indicate that the file has been transmitted

            sendFTPResponse(226, "Closing data connection");

            // Close the data session

            m_cmdSess.getFTPServer().releaseDataSession(this);

            // Close the network file

            disk.closeFile( this, tree, netFile);
            netFile = null;

            // DEBUG

            if (logger.isDebugEnabled() && m_cmdSess.hasDebug(FTPSrvSession.DBG_FILEIO))
                logger.debug(" Transfer complete, file closed");
        }
        catch (SocketException ex)
        {

            // DEBUG

            if (logger.isDebugEnabled() && m_cmdSess.hasDebug(FTPSrvSession.DBG_ERROR))
                logger.debug(" Error during transfer", ex);

            // Indicate that there was an error during transmission of the file
            // data

            sendFTPResponse(426, "Data connection closed by client");
        }
        catch (Exception ex)
        {

            // DEBUG

            if (logger.isDebugEnabled() && m_cmdSess.hasDebug(FTPSrvSession.DBG_ERROR))
                logger.debug(" Error during transfer", ex);

            // Indicate that there was an error during transmission of the file
            // data

            sendFTPResponse(426, "Error during transmission");
        }
        finally
        {
        	try
        	{
	            // Close the network file
	
	            if (netFile != null && disk != null && tree != null)
	                disk.closeFile(m_cmdSess, tree, netFile);
	
	            // Close the output stream to the client
	
	            if (os != null)
	                os.close();
	
	            // Close the data connection to the client
	
	            m_cmdSess.getFTPServer().releaseDataSession( this);
	            closeSession();
        	}
        	catch (Exception ex)
        	{
        		if ( logger.isErrorEnabled())
        			logger.error( "Error during FTP data session close", ex);
        	}
        }
    }
    
    /**
     * Store a file received from the client
     */
    private final void runStoreFile()
    {
        // Store the file from the client

        InputStream is = null;
        DiskInterface disk = null;
        TreeConnection tree = null;
        NetworkFile netFile = null;
        Socket dataSock = null;

        try
        {

            // Create a temporary tree connection

            tree = m_cmdSess.getTreeConnection(m_ftpPath.getSharedDevice());

            // Check if the session has the required access to the filesystem

            if (tree == null || tree.hasWriteAccess() == false)
            {

                // Session does not have write access to the filesystem

                sendFTPResponse(550, "Access denied");
                return;
            }

            // Check if the file exists

            disk = (DiskInterface) m_ftpPath.getSharedDevice().getInterface();
            int sts = disk.fileExists(this, tree, m_ftpPath.getSharePath());

            if (sts == FileStatus.DirectoryExists)
            {

                // Return an error status

                sendFTPResponse(500, "Invalid path (existing directory)");
                return;
            }

            // Create the file open parameters

            FileOpenParams params = new FileOpenParams(m_ftpPath.getSharePath(),
                    sts == FileStatus.FileExists ? FileAction.TruncateExisting : FileAction.CreateNotExist,
                    AccessMode.ReadWrite, 0);

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

            // Notify change listeners that a new file has been created

            DiskDeviceContext diskCtx = (DiskDeviceContext) tree.getContext();

            if (diskCtx.hasChangeHandler())
                diskCtx.getChangeHandler().notifyFileChanged(NotifyChange.ActionAdded, m_ftpPath.getSharePath());

            // Send the intermediate response

            sendFTPResponse(150, "File status okay, about to open data connection");

            // Get the data connection socket

            try
            {
                dataSock = getSocket();
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

            if (logger.isDebugEnabled() && m_cmdSess.hasDebug(FTPSrvSession.DBG_FILE))
                logger.debug("Storing ftp="
                        + m_ftpPath.getFTPPath() + ", share=" + m_ftpPath.getShareName() + ", path="
                        + m_ftpPath.getSharePath());

            // Allocate the buffer for the file data

            byte[] buf = new byte[FTPSrvSession.DEFAULT_BUFFERSIZE];
            long filePos = 0;
            int len = is.read(buf, 0, buf.length);

            while (len > 0)
            {

                // DEBUG

                if (logger.isDebugEnabled() && m_cmdSess.hasDebug(FTPSrvSession.DBG_FILEIO))
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

            if (logger.isDebugEnabled() && m_cmdSess.hasDebug(FTPSrvSession.DBG_FILEIO))
                logger.debug(" Transfer complete, file closed");
        }
        catch (SocketException ex)
        {

            // DEBUG

            if (logger.isDebugEnabled() && m_cmdSess.hasDebug(FTPSrvSession.DBG_ERROR))
                logger.debug(" Error during transfer", ex);

            // Indicate that there was an error during transmission of the file data

            sendFTPResponse(426, "Data connection closed by client");
        }
        catch (Exception ex)
        {

            // DEBUG

            if (logger.isDebugEnabled() && m_cmdSess.hasDebug(FTPSrvSession.DBG_ERROR))
                logger.debug(" Error during transfer", ex);

            // Indicate that there was an error during transmission of the file
            // data

            sendFTPResponse(426, "Error during transmission");
        }
        finally
        {
        	try
        	{
	            // Close the network file
	
	            if (netFile != null && disk != null && tree != null)
	                disk.closeFile( this, tree, netFile);
	
	            // Close the input stream to the client
	
	            if (is != null)
	                is.close();
	
	            // Close the data connection to the client
	
	            m_cmdSess.getFTPServer().releaseDataSession(this);
	            closeSession();
        	}
        	catch (Exception ex)
        	{
        		if ( logger.isErrorEnabled())
        			logger.error( "Error during FTP data session close", ex);
        	}
        }
    }
    
    /**
     * Send an FTP response to the client via the command session
     *
     * @param stsCode int
     * @param msg String
     */
   protected final void sendFTPResponse(int stsCode, String msg)
   {
	   try
	   {
		   m_cmdSess.sendFTPResponse( stsCode, msg);
	   }
	   catch (Exception ex)
	   {
	   }
   }

   /**
    * Return the client address
    * 
    * @return InetAddress
    */
   public InetAddress getRemoteAddress() {
	   return m_cmdSess.getRemoteAddress();
   }
}
