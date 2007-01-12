/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
package org.alfresco.filesys.server;

import java.net.InetAddress;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.server.auth.AuthContext;
import org.alfresco.filesys.server.auth.ClientInfo;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.server.core.SharedDeviceList;
import org.alfresco.filesys.server.filesys.FilesysTransaction;
import org.alfresco.service.transaction.TransactionService;

/**
 * Server Session Base Class
 * <p>
 * Base class for server session implementations for different protocols.
 */
public abstract class SrvSession
{

    // Network server this session is associated with

    private NetworkServer m_server;

    // Session id/slot number

    private int m_sessId;

    // Unique session id string

    private String m_uniqueId;

    // Process id

    private int m_processId = -1;

    // Session/user is logged on/validated

    private boolean m_loggedOn;

    // Client details

    private ClientInfo m_clientInfo;

    // Debug flags for this session

    private int m_debug;

    // Session shutdown flag

    private boolean m_shutdown;

    // Protocol type

    private String m_protocol;

    // Remote client/host name

    private String m_remoteName;

    // Authentication token, used during logon
    
    private Object m_authToken;

    //  Authentication context, used during the initial session setup phase
    
    private AuthContext m_authContext;
    
    //  List of dynamic/temporary shares created for this session
    
    private SharedDeviceList m_dynamicShares;
    
    // Active transaction and read/write flag
    
    private ThreadLocal<FilesysTransaction> m_tx = new ThreadLocal<FilesysTransaction>();
    
//    UserTransaction m_transaction;
//    private boolean m_readOnlyTrans;
    
    // Request and transaction counts
    
    protected int m_reqCount;
    protected int m_transCount;
    protected int m_transConvCount;
    
    /**
     * Class constructor
     * 
     * @param sessId int
     * @param srv NetworkServer
     * @param proto String
     * @param remName String
     */
    public SrvSession(int sessId, NetworkServer srv, String proto, String remName)
    {
        m_sessId = sessId;
        m_server = srv;

        setProtocolName(proto);
        setRemoteName(remName);
    }

    /**
     * Add a dynamic share to the list of shares created for this session
     * 
     * @param shrDev SharedDevice
     */
    public final void addDynamicShare(SharedDevice shrDev) {
        
        //  Check if the dynamic share list must be allocated
        
        if ( m_dynamicShares == null)
            m_dynamicShares = new SharedDeviceList();
            
        //  Add the new share to the list
        
        m_dynamicShares.addShare(shrDev);
    }
    
    /**
     * Return the authentication token
     * 
     * @return Object
     */
    public final Object getAuthenticationToken()
    {
        return m_authToken;
    }
    
    /**
     * Determine if the authentication token is set
     * 
     * @return boolean
     */
    public final boolean hasAuthenticationToken()
    {
        return m_authToken != null ? true : false;
    }
    
    /**
     * Return the process id
     * 
     * @return int
     */
    public final int getProcessId()
    {
        return m_processId;
    }

    /**
     * Return the remote client network address
     * 
     * @return InetAddress
     */
    public abstract InetAddress getRemoteAddress();

    /**
     * Return the session id for this session.
     * 
     * @return int
     */
    public final int getSessionId()
    {
        return m_sessId;
    }

    /**
     * Return the server this session is associated with
     * 
     * @return NetworkServer
     */
    public final NetworkServer getServer()
    {
        return m_server;
    }

    /**
     * Check if the session has valid client information
     * 
     * @return boolean
     */
    public final boolean hasClientInformation()
    {
        return m_clientInfo != null ? true : false;
    }

    /**
     * Return the client information
     * 
     * @return ClientInfo
     */
    public final ClientInfo getClientInformation()
    {
        return m_clientInfo;
    }

    /**
     * Check if the session has an authentication context
     * 
     * @return boolean
     */
    public final boolean hasAuthenticationContext()
    {
        return m_authContext != null ? true : false;
    }
    
    /**
     * Return the authentication context for this sesion
     * 
     * @return AuthContext
     */
    public final AuthContext getAuthenticationContext()
    {
        return m_authContext;
    }
    
    /**
     * Determine if the session has any dynamic shares
     * 
     * @return boolean
     */
    public final boolean hasDynamicShares() {
        return m_dynamicShares != null ? true : false;
    }

    /**
     * Return the list of dynamic shares created for this session
     * 
     * @return SharedDeviceList
     */
    public final SharedDeviceList getDynamicShareList() {
        return m_dynamicShares;
    }

    /**
     * Determine if the protocol type has been set
     * 
     * @return boolean
     */
    public final boolean hasProtocolName()
    {
        return m_protocol != null ? true : false;
    }

    /**
     * Return the protocol name
     * 
     * @return String
     */
    public final String getProtocolName()
    {
        return m_protocol;
    }

    /**
     * Determine if the remote client name has been set
     * 
     * @return boolean
     */
    public final boolean hasRemoteName()
    {
        return m_remoteName != null ? true : false;
    }

    /**
     * Return the remote client name
     * 
     * @return String
     */
    public final String getRemoteName()
    {
        return m_remoteName;
    }

    /**
     * Determine if the session is logged on/validated
     * 
     * @return boolean
     */
    public final boolean isLoggedOn()
    {
        return m_loggedOn;
    }

    /**
     * Determine if the session has been shut down
     * 
     * @return boolean
     */
    public final boolean isShutdown()
    {
        return m_shutdown;
    }

    /**
     * Return the unique session id
     * 
     * @return String
     */
    public final String getUniqueId()
    {
        return m_uniqueId;
    }

    /**
     * Determine if the specified debug flag is enabled.
     * 
     * @return boolean
     * @param dbg int
     */
    public final boolean hasDebug(int dbgFlag)
    {
        if ((m_debug & dbgFlag) != 0)
            return true;
        return false;
    }

    /**
     * Set the authentication token
     * 
     * @param authToken Object
     */
    public final void setAuthenticationToken(Object authToken)
    {
        m_authToken = authToken;
    }
    
    /**
     * Set the authentication context, used during the initial session setup phase
     * 
     * @param ctx AuthContext
     */
    public final void setAuthenticationContext( AuthContext ctx)
    {
        m_authContext = ctx;
    }
    
    /**
     * Set the client information
     * 
     * @param client ClientInfo
     */
    public final void setClientInformation(ClientInfo client)
    {
        m_clientInfo = client;
    }

    /**
     * Set the debug output interface.
     * 
     * @param flgs int
     */
    public final void setDebug(int flgs)
    {
        m_debug = flgs;
    }

    /**
     * Set the logged on/validated status for the session
     * 
     * @param loggedOn boolean
     */
    public final void setLoggedOn(boolean loggedOn)
    {
        m_loggedOn = loggedOn;
    }

    /**
     * Set the process id
     * 
     * @param id int
     */
    public final void setProcessId(int id)
    {
        m_processId = id;
    }

    /**
     * Set the protocol name
     * 
     * @param name String
     */
    public final void setProtocolName(String name)
    {
        m_protocol = name;
    }

    /**
     * Set the remote client name
     * 
     * @param name String
     */
    public final void setRemoteName(String name)
    {
        m_remoteName = name;
    }

    /**
     * Set the session id for this session.
     * 
     * @param id int
     */
    public final void setSessionId(int id)
    {
        m_sessId = id;
    }

    /**
     * Set the unique session id
     * 
     * @param unid String
     */
    public final void setUniqueId(String unid)
    {
        m_uniqueId = unid;
    }

    /**
     * Set the shutdown flag
     * 
     * @param flg boolean
     */
    protected final void setShutdown(boolean flg)
    {
        m_shutdown = flg;
    }

    /**
     * Close the network session
     */
    public void closeSession()
    {
        //  Release any dynamic shares owned by this session
        
        if ( hasDynamicShares()) {
            
            //  Close the dynamic shares
            
            getServer().getShareMapper().deleteShares(this);
        }
    }
    
    /**
     * Create a read transaction, if not already active
     * 
     * @param transService TransactionService
     * @return boolean
     * @exception AlfrescoRuntimeException
     */
    public final boolean beginReadTransaction( TransactionService transService)
    	throws AlfrescoRuntimeException
    {
    	return beginTransaction(transService, true);
    }
    
    /**
     * Create a write transaction, if not already active
     * 
     * @param transService TransactionService
     * @return boolean
     * @exception AlfrescoRuntimeException
     */
    public final boolean beginWriteTransaction( TransactionService transService)
    	throws AlfrescoRuntimeException
    {
    	return beginTransaction(transService, false);
    }
    
    /**
     * Create and start a transaction, if not already active
     * 
     * @param transService TransactionService
     * @param readOnly boolean
     * @return boolean
     * @exception AlfrescoRuntimeException
     */
    private final boolean beginTransaction(TransactionService transService, boolean readOnly)
        throws AlfrescoRuntimeException
    {
        boolean created = false;
        
        // Get the filesystem transaction
        
        FilesysTransaction filesysTx = m_tx.get();
        if ( filesysTx == null)
        {
        	filesysTx = new FilesysTransaction();
        	m_tx.set( filesysTx);
        }
        
        // If there is an active transaction check that it is the required type

        if ( filesysTx.hasTransaction())
        {
        	// Get the active transaction
        	
            UserTransaction tx = filesysTx.getTransaction();
            
            // Check if the current transaction is marked for rollback
            
            try
            {
                if ( tx.getStatus() == Status.STATUS_MARKED_ROLLBACK ||
                     tx.getStatus() == Status.STATUS_ROLLEDBACK ||
                     tx.getStatus() == Status.STATUS_ROLLING_BACK)
                {
                    //  Rollback the current transaction
                    
                    tx.rollback();
                }
            }
            catch ( Exception ex)
            {
            }
            
            // Check if the transaction is a write transaction, if write has been requested
            
            if ( readOnly == false && filesysTx.isReadOnly() == true)
            {
                // Commit the read-only transaction
                
                try
                {
                    tx.commit();
                    m_transConvCount++;
                }
                catch ( Exception ex)
                {
                    throw new AlfrescoRuntimeException("Failed to commit read-only transaction, " + ex.getMessage());
                }
                finally
                {
                    // Clear the active transaction

                    filesysTx.clearTransaction();
                }
            }
        }
        
        // Create the transaction
        
        if ( filesysTx.hasTransaction() == false)
        {
            try
            {
                UserTransaction userTrans = transService.getUserTransaction(readOnly);
                userTrans.begin();
                
                created = true;

                // Store the transaction
                
                filesysTx.setTransaction( userTrans, readOnly);
                m_transCount++;
            }
            catch (Exception ex)
            {
                throw new AlfrescoRuntimeException("Failed to create transaction, " + ex.getMessage());
            }
        }
        
        return created;
    }
    
    /**
     * End a transaction by either committing or rolling back
     * 
     * @exception AlfrescoRuntimeException
     */
    public final void endTransaction()
        throws AlfrescoRuntimeException
    {
        // Get the filesystem transaction
        
        FilesysTransaction filesysTx = m_tx.get();

        // Check if there is an active transaction
        
        if ( filesysTx != null && filesysTx.hasTransaction())
        {
        	// Get the active transaction
        	
            UserTransaction tx = filesysTx.getTransaction();
            
            try
            {
                // Commit or rollback the transaction
                
                if ( tx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
                {
                    // Transaction is marked for rollback
                    
                    tx.rollback();
                }
                else
                {
                    // Commit the transaction
                    
                    tx.commit();
                }
            }
            catch ( Exception ex)
            {
                throw new AlfrescoRuntimeException("Failed to end transaction", ex);
            }
            finally
            {
                // Clear the current transaction
                
                filesysTx.clearTransaction();
        }
    }
    
    }
    /**
     * Determine if the session has an active transaction
     * 
     * @return boolean
     */
    public final boolean hasUserTransaction()
    {
        // Get the filesystem transaction
        
        FilesysTransaction filesysTx = m_tx.get();
        if ( filesysTx != null)
        	return filesysTx.hasTransaction();
        return false;
    }
    
    /**
     * Get the active transaction and clear the stored transaction
     * 
     *  @return UserTransaction
     */
    public final UserTransaction getUserTransaction()
    {
        // Get the filesystem transaction

    	UserTransaction userTrans = null;
        FilesysTransaction filesysTx = m_tx.get();
        
        if ( filesysTx != null)
        {
	        userTrans = filesysTx.getTransaction();
	        filesysTx.clearTransaction();
        }
        
        return userTrans;
    }
}
