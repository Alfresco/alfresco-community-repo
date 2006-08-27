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
package org.alfresco.filesys.server.auth;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.transaction.UserTransaction;

import net.sf.acegisecurity.Authentication;

import org.alfresco.config.ConfigElement;
import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.config.InvalidConfigurationException;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.server.filesys.DiskDeviceContext;
import org.alfresco.filesys.server.filesys.DiskInterface;
import org.alfresco.filesys.server.filesys.DiskSharedDevice;
import org.alfresco.filesys.server.filesys.SrvDiskInfo;
import org.alfresco.filesys.smb.Capability;
import org.alfresco.filesys.smb.Dialect;
import org.alfresco.filesys.smb.DialectSelector;
import org.alfresco.filesys.smb.SMBStatus;
import org.alfresco.filesys.smb.server.SMBSrvException;
import org.alfresco.filesys.smb.server.SMBSrvPacket;
import org.alfresco.filesys.smb.server.SMBSrvSession;
import org.alfresco.filesys.smb.server.SecurityMode;
import org.alfresco.filesys.smb.server.repo.ContentContext;
import org.alfresco.filesys.util.DataPacker;
import org.alfresco.filesys.util.HexDump;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.MD4PasswordEncoder;
import org.alfresco.repo.security.authentication.MD4PasswordEncoderImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CIFS Authenticator Class
 * 
 * <p>
 * An authenticator is used by the CIFS server to authenticate users when in user level access mode
 * and authenticate requests to connect to a share when in share level access.
 */
public abstract class CifsAuthenticator
{
    // Logging
    
    protected static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol.auth");

    // Encryption algorithm types

    public static final int LANMAN = PasswordEncryptor.LANMAN;
    public static final int NTLM1  = PasswordEncryptor.NTLM1;
    public static final int NTLM2  = PasswordEncryptor.NTLM2;

    // Authentication status values

    public static final int AUTH_ALLOW       = 0;
    public static final int AUTH_GUEST       = 0x10000000;
    public static final int AUTH_DISALLOW    = -1;
    public static final int AUTH_BADPASSWORD = -2;
    public static final int AUTH_BADUSER     = -3;

    // Share access permissions, returned by authenticateShareConnect()

    public static final int NoAccess  = 0;
    public static final int ReadOnly  = 1;
    public static final int Writeable = 2;

    // Standard encrypted password and challenge length

    public static final int STANDARD_PASSWORD_LEN   = 24;
    public static final int STANDARD_CHALLENGE_LEN  = 8;

    // Default guest user name
    
    protected static final String GUEST_USERNAME = "guest";

    // Default SMB dialects to enable

    private DialectSelector m_dialects;
    
    // Security mode flags
    
    private int m_securityMode = SecurityMode.UserMode + SecurityMode.EncryptedPasswords;
    
    // Password encryption algorithms

    private PasswordEncryptor m_encryptor = new PasswordEncryptor();

    // Flag to enable/disable the guest account, and control mapping of unknown users to the guest account

    private boolean m_allowGuest;
    private boolean m_mapToGuest;

    // Default guest user name
    
    private String m_guestUserName = GUEST_USERNAME;
    
    // Random number generator used to generate challenge keys

    protected Random m_random = new Random(System.currentTimeMillis());

    // Server configuration

    protected ServerConfiguration m_config;
    
    // Authentication component, used to access internal authentication functions
    
    protected AuthenticationComponent m_authComponent;

    // MD4 hash decoder
    
    protected MD4PasswordEncoder m_md4Encoder = new MD4PasswordEncoderImpl();
    
    // Various services required to get user information
    
    protected NodeService m_nodeService;
    protected PersonService m_personService;
    protected TransactionService m_transactionService;
    protected AuthenticationService m_authenticationService;
    
    /**
     * Authenticate a connection to a share.
     * 
     * @param client User/client details from the tree connect request.
     * @param share Shared device the client wants to connect to.
     * @param pwd Share password.
     * @param sess Server session.
     * @return int Granted file permission level or disallow status if negative. See the
     *         FilePermission class.
     */
    public int authenticateShareConnect(ClientInfo client, SharedDevice share, String sharePwd, SrvSession sess)
    {
        // Allow write access
        //
        // Main authentication is handled by authenticateUser()
        
        return CifsAuthenticator.Writeable;
    }

    /**
     * Authenticate a user. A user may be granted full access, guest access or no access.
     * 
     * @param client User/client details from the session setup request.
     * @param sess Server session
     * @param alg Encryption algorithm
     * @return int Access level or disallow status.
     */
    public int authenticateUser(ClientInfo client, SrvSession sess, int alg)
    {
        return CifsAuthenticator.AUTH_DISALLOW;
    }

    /**
     * Initialize the authenticator
     * 
     * @param config ServerConfiguration
     * @param params ConfigElement
     * @exception InvalidConfigurationException
     */
    public void initialize(ServerConfiguration config, ConfigElement params) throws InvalidConfigurationException
    {
        // Save the server configuration so we can access the authentication component

        m_config = config;
        
        // Check that the required authentication classes are available

        m_authComponent = m_config.getAuthenticationComponent();
        
        if ( m_authComponent == null)
            throw new InvalidConfigurationException("Authentication component not available");

        // Allocate the SMB dialect selector, and initialize using the default list of dialects

        m_dialects = new DialectSelector();

        m_dialects.AddDialect(Dialect.DOSLanMan1);
        m_dialects.AddDialect(Dialect.DOSLanMan2);
        m_dialects.AddDialect(Dialect.LanMan1);
        m_dialects.AddDialect(Dialect.LanMan2);
        m_dialects.AddDialect(Dialect.LanMan2_1);
        m_dialects.AddDialect(Dialect.NT);
        
        // Get hold of various services
        
        m_nodeService = config.getNodeService();
        m_personService = config.getPersonService();
        m_transactionService = config.getTransactionService();
        m_authenticationService = config.getAuthenticationService();
        
        // Set the guest user name
        
        setGuestUserName( m_authComponent.getGuestUserName());
        
        // Check that the authentication component is the required type for this authenticator
        
        if ( validateAuthenticationMode() == false)
            throw new InvalidConfigurationException("Required authentication mode not available");
    }

    /**
     * Validate that the authentication component supports the required mode
     * 
     * @return boolean
     */
    protected boolean validateAuthenticationMode()
    {
        return true;
    }
    
    /**
     * Encrypt the plain text password with the specified encryption key using the specified
     * encryption algorithm.
     * 
     * @param plainPwd String
     * @param encryptKey byte[]
     * @param alg int
     * @param userName String
     * @param domain String
     * @return byte[]
     */
    protected final byte[] generateEncryptedPassword(String plainPwd, byte[] encryptKey, int alg, String userName, String domain)
    {

        // Use the password encryptor

        byte[] encPwd = null;

        try
        {
            // Encrypt the password

            encPwd = m_encryptor.generateEncryptedPassword(plainPwd, encryptKey, alg, userName, domain);
        }
        catch (NoSuchAlgorithmException ex)
        {
        }
        catch (InvalidKeyException ex)
        {
        }

        // Return the encrypted password

        return encPwd;
    }

    /**
     * Return an authentication context for the new session
     * 
     * @return AuthContext
     */
    public AuthContext getAuthContext( SMBSrvSession sess)
    {
        AuthContext authCtx = null;
        
        if ( sess.hasAuthenticationContext() && sess.getAuthenticationContext() instanceof NTLanManAuthContext)
        {
            // Use the existing authentication context
            
            authCtx = (NTLanManAuthContext) sess.getAuthenticationContext();
        }
        else
        {
            // Create a new authentication context for the session

            authCtx = new NTLanManAuthContext();
            sess.setAuthenticationContext( authCtx);
        }
        
        // Return the authentication context
        
        return authCtx;
    }
    
    /**
     * Return the enabled SMB dialects that the server will use when negotiating sessions.
     * 
     * @return DialectSelector
     */
    public final DialectSelector getEnabledDialects()
    {
        return m_dialects;
    }

    /**
     * Return the security mode flags
     * 
     * @return int
     */
    public final int getSecurityMode()
    {
        return m_securityMode;
    }
    
    /**
     * Generate the CIFS negotiate response packet, the authenticator should add authentication specific fields
     * to the response.
     * 
     * @param sess SMBSrvSession
     * @param respPkt SMBSrvPacket
     * @param extendedSecurity boolean
     * @exception AuthenticatorException
     */
    public void generateNegotiateResponse(SMBSrvSession sess, SMBSrvPacket respPkt, boolean extendedSecurity)
        throws AuthenticatorException
    {
        // Pack the negotiate response for NT/LanMan challenge/response authentication

        NTLanManAuthContext authCtx = (NTLanManAuthContext) getAuthContext( sess);
        
        // Encryption key and primary domain string should be returned in the byte area

        int pos = respPkt.getByteOffset();
        byte[] buf = respPkt.getBuffer();

        if ( authCtx.getChallenge() == null)
        {

            // Return a dummy encryption key

            for (int i = 0; i < 8; i++)
                buf[pos++] = 0;
        }
        else
        {

            // Store the encryption key

            byte[] key = authCtx.getChallenge();
            for (int i = 0; i < key.length; i++)
                buf[pos++] = key[i];
        }

        // Pack the local domain name

        String domain = sess.getServer().getConfiguration().getDomainName();
        if (domain != null)
            pos = DataPacker.putString(domain, buf, pos, true, true);

        // Pack the local server name
        
        pos = DataPacker.putString( sess.getServer().getServerName(), buf, pos, true, true);
        
        // Set the packet length
        
        respPkt.setByteCount(pos - respPkt.getByteOffset());
    }
    
    /**
     * Process the CIFS session setup request packet and build the session setup response
     * 
     * @param sess SMBSrvSession
     * @param reqPkt SMBSrvPacket
     * @param respPkt SMBSrvPacket
     * @exception SMBSrvException
     */
    public void processSessionSetup(SMBSrvSession sess, SMBSrvPacket reqPkt, SMBSrvPacket respPkt)
        throws SMBSrvException
    {

        // Check that the received packet looks like a valid NT session setup andX request

        if (reqPkt.checkPacketIsValid(13, 0) == false)
        {
            throw new SMBSrvException(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
        }

        // Extract the session details

        int maxBufSize = reqPkt.getParameter(2);
        int maxMpx = reqPkt.getParameter(3);
        int vcNum = reqPkt.getParameter(4);
        int ascPwdLen = reqPkt.getParameter(7);
        int uniPwdLen = reqPkt.getParameter(8);
        int capabs = reqPkt.getParameterLong(11);

        // Extract the client details from the session setup request

        byte[] buf = reqPkt.getBuffer();

        // Determine if ASCII or unicode strings are being used

        boolean isUni = reqPkt.isUnicode();

        // Extract the password strings

        byte[] ascPwd = reqPkt.unpackBytes(ascPwdLen);
        byte[] uniPwd = reqPkt.unpackBytes(uniPwdLen);

        // Extract the user name string

        String user = reqPkt.unpackString(isUni);

        if (user == null)
        {
            throw new SMBSrvException(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
        }

        // Extract the clients primary domain name string

        String domain = "";

        if (reqPkt.hasMoreData())
        {

            // Extract the callers domain name

            domain = reqPkt.unpackString(isUni);

            if (domain == null)
            {
                throw new SMBSrvException(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            }
        }

        // Extract the clients native operating system

        String clientOS = "";

        if (reqPkt.hasMoreData())
        {

            // Extract the callers operating system name

            clientOS = reqPkt.unpackString(isUni);

            if (clientOS == null)
            {
                throw new SMBSrvException(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            }
        }

        // DEBUG

        if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_NEGOTIATE))
        {
            logger.debug("NT Session setup from user=" + user + ", password="
                    + (uniPwd != null ? HexDump.hexString(uniPwd) : "none") + ", ANSIpwd="
                    + (ascPwd != null ? HexDump.hexString(ascPwd) : "none") + ", domain=" + domain + ", os=" + clientOS
                    + ", VC=" + vcNum + ", maxBuf=" + maxBufSize + ", maxMpx=" + maxMpx
                    + ", authCtx=" + sess.getAuthenticationContext());
            logger.debug("  MID=" + reqPkt.getMultiplexId() + ", UID=" + reqPkt.getUserId() + ", PID="
                    + reqPkt.getProcessId());
        }

        // Store the client maximum buffer size, maximum multiplexed requests count and client
        // capability flags

        sess.setClientMaximumBufferSize(maxBufSize);
        sess.setClientMaximumMultiplex(maxMpx);
        sess.setClientCapabilities(capabs);

        // Create the client information and store in the session

        ClientInfo client = new ClientInfo(user, uniPwd);
        client.setANSIPassword(ascPwd);
        client.setDomain(domain);
        client.setOperatingSystem(clientOS);

        if (sess.hasRemoteAddress())
            client.setClientAddress(sess.getRemoteAddress().getHostAddress());

        // Check if this is a null session logon

        if (user.length() == 0 && domain.length() == 0 && uniPwdLen == 0 && ascPwdLen == 1)
            client.setLogonType(ClientInfo.LogonNull);

        // Authenticate the user

        boolean isGuest = false;

        int sts = authenticateUser(client, sess, CifsAuthenticator.NTLM1);

        if (sts > 0 && (sts & CifsAuthenticator.AUTH_GUEST) != 0)
        {

            // Guest logon

            isGuest = true;

            // DEBUG

            if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_NEGOTIATE))
                logger.debug("User " + user + ", logged on as guest");
        }
        else if (sts != CifsAuthenticator.AUTH_ALLOW)
        {

            // Check if the session already has valid client details and the new client details
            // have null username/password values

            if (sess.getClientInformation() != null && client.getUserName().length() == 0)
            {

                // Use the existing client information details

                client = sess.getClientInformation();

                // DEBUG

                if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_NEGOTIATE))
                    logger.debug("Null client information, reusing existing client=" + client);
            }
            else
            {
                // DEBUG

                if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_NEGOTIATE))
                    logger.debug("User " + user + ", access denied");

                // Invalid user, reject the session setup request

                throw new SMBSrvException(SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            }
        }
        else if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_NEGOTIATE))
        {

            // DEBUG

            logger.debug("User " + user + " logged on "
                    + (client != null ? " (type " + client.getLogonTypeString() + ")" : ""));
        }

        // Update the client information if not already set

        if (sess.getClientInformation() == null
                || sess.getClientInformation().getUserName().length() == 0)
        {

            // Set the client details for the session

            sess.setClientInformation(client);
        }

        // Set the guest flag for the client, indicate that the session is logged on

        client.setGuest(isGuest);
        sess.setLoggedOn(true);

        // Build the session setup response SMB

        respPkt.setParameterCount(3);
        respPkt.setParameter(0, 0); // No chained response
        respPkt.setParameter(1, 0); // Offset to chained response
        respPkt.setParameter(2, isGuest ? 1 : 0);
        respPkt.setByteCount(0);

        respPkt.setTreeId(0);
        respPkt.setUserId(0);

        // Set the various flags

        int flags = respPkt.getFlags();
        flags &= ~SMBSrvPacket.FLG_CASELESS;
        respPkt.setFlags(flags);

        int flags2 = SMBSrvPacket.FLG2_LONGFILENAMES;
        if (isUni)
            flags2 += SMBSrvPacket.FLG2_UNICODE;
        respPkt.setFlags2(flags2);

        // Pack the OS, dialect and domain name strings.

        int pos = respPkt.getByteOffset();
        buf = respPkt.getBuffer();

        if (isUni)
            pos = DataPacker.wordAlign(pos);

        pos = DataPacker.putString("Java", buf, pos, true, isUni);
        pos = DataPacker.putString("Alfresco CIFS Server " + sess.getServer().isVersion(), buf, pos, true, isUni);
        pos = DataPacker.putString(sess.getServer().getConfiguration().getDomainName(), buf, pos, true, isUni);

        respPkt.setByteCount(pos - respPkt.getByteOffset());
    }
    
    /**
     * Return the encryption key/challenge length
     * 
     * @return int
     */
    public int getEncryptionKeyLength()
    {
        return STANDARD_CHALLENGE_LEN;
    }
    
    /**
     * Return the server capability flags
     * 
     * @return int
     */
    public int getServerCapabilities()
    {
        return Capability.Unicode + Capability.RemoteAPIs + Capability.NTSMBs + Capability.NTFind +
               Capability.NTStatus + Capability.LargeFiles + Capability.LargeRead + Capability.LargeWrite;
    }
    
    /**
     * Determine if guest access is allowed
     * 
     * @return boolean
     */
    public final boolean allowGuest()
    {
        return m_allowGuest;
    }

    /**
     * Return the guest user name
     * 
     * @return String
     */
    public final String getGuestUserName()
    {
        return m_guestUserName;
    }
    
    /**
     * Determine if unknown users should be mapped to the guest account
     * 
     * @return boolean
     */
    public final boolean mapUnknownUserToGuest()
    {
        return m_mapToGuest;
    }
    
    /**
     * Enable/disable the guest account
     * 
     * @param ena Enable the guest account if true, only allow defined user accounts access if false
     */
    public final void setAllowGuest(boolean ena)
    {
        m_allowGuest = ena;
    }

    /**
     * Set the guest user name
     * 
     * @param guest String
     */
    public final void setGuestUserName( String guest)
    {
        m_guestUserName = guest;
    }
    
    /**
     * Enable/disable mapping of unknown users to the guest account
     * 
     * @param ena Enable mapping of unknown users to the guest if true
     */
    public final void setMapToGuest( boolean ena)
    {
        m_mapToGuest = ena;
    }
    
    /**
     * Set the security mode flags
     * 
     * @param flg int
     */
    protected final void setSecurityMode(int flg)
    {
        m_securityMode = flg;
    }
    
    /**
     * Close the authenticator, perform any cleanup
     */
    public void closeAuthenticator()
    {
        // Override if cleanup required
    }
    
    /**
     * Validate a password by encrypting the plain text password using the specified encryption key
     * and encryption algorithm.
     * 
     * @param plainPwd String
     * @param encryptedPwd String
     * @param encryptKey byte[]
     * @param alg int
     * @param userName String
     * @param domain String
     * @return boolean
     */
    protected final boolean validatePassword(String plainPwd, byte[] encryptedPwd, byte[] encryptKey, int alg, String userName, String domain)
    {

        // Generate an encrypted version of the plain text password

        byte[] encPwd = generateEncryptedPassword(plainPwd != null ? plainPwd : "", encryptKey, alg, userName, domain);

        // Compare the generated password with the received password

        if (encPwd != null && encryptedPwd != null && encPwd.length == STANDARD_PASSWORD_LEN
                && encryptedPwd.length == STANDARD_PASSWORD_LEN)
        {

            // Compare the password arrays

            for (int i = 0; i < STANDARD_PASSWORD_LEN; i++)
                if (encPwd[i] != encryptedPwd[i])
                    return false;

            // Password is valid

            return true;
        }

        // User or password is invalid

        return false;
    }

    /**
     * Convert the password string to a byte array
     * 
     * @param pwd String
     * @return byte[]
     */

    protected final byte[] convertPassword(String pwd)
    {

        // Create a padded/truncated 14 character string

        StringBuffer p14str = new StringBuffer();
        p14str.append(pwd);
        if (p14str.length() > 14)
            p14str.setLength(14);
        else
        {
            while (p14str.length() < 14)
                p14str.append((char) 0x00);
        }

        // Convert the P14 string to an array of bytes. Allocate the return 16 byte array.

        return p14str.toString().getBytes();
    }
    
    /**
     * Return the password encryptor
     * 
     * @return PasswordEncryptor
     */
    protected final PasswordEncryptor getEncryptor()
    {
        return m_encryptor;
    }
    
    /**
     * Return the authentication status as a string
     * 
     * @param sts int
     * @return String
     */
    protected final String getStatusAsString(int sts)
    {
        String str = null;
        
        switch ( sts)
        {
        case AUTH_ALLOW:
            str = "Allow";
            break;
        case AUTH_DISALLOW:
            str = "Disallow";
            break;
        case AUTH_GUEST:
            str = "Guest";
            break;
        case AUTH_BADPASSWORD:
            str = "BadPassword";
            break;
        case AUTH_BADUSER:
            str = "BadUser";
            break;
        }
        
        return str;
    }
    
    /**
     * Logon using the guest user account
     * 
     * @param client ClientInfo
     * @param sess SrvSession
     */
    protected final void doGuestLogon( ClientInfo client, SrvSession sess)
    {
        //  Get a guest authentication token
        
        m_authenticationService.authenticateAsGuest();
        Authentication authToken = m_authComponent.getCurrentAuthentication();
        
        client.setAuthenticationToken( authToken);
        
        // Set the home folder for the guest user
        
        client.setUserName( getGuestUserName());
        getHomeFolderForUser( client);
        
        // Mark the client as being a guest logon
        
        client.setGuest( true);

        // Create a dynamic share for the guest user
        // Create the disk driver and context
        
        DiskInterface diskDrv = m_config.getDiskInterface();
        DiskDeviceContext diskCtx = new ContentContext("", "", client.getHomeFolder());

        //  Default the filesystem to look like an 80Gb sized disk with 90% free space

        diskCtx.setDiskInformation(new SrvDiskInfo(2560, 64, 512, 2304));
        
        //  Create a temporary shared device for the users home directory
        
        sess.addDynamicShare( new DiskSharedDevice( client.getUserName(), diskDrv, diskCtx, SharedDevice.Temporary));
    }
    
    /**
     * Get the home folder for the user
     * 
     * @param client ClientInfo
     */
    protected final void getHomeFolderForUser(ClientInfo client)
    {
        // Get the home folder for the user
        
        UserTransaction tx = m_transactionService.getUserTransaction();
        NodeRef homeSpaceRef = null;
        
        try
        {
            tx.begin();
            homeSpaceRef = (NodeRef) m_nodeService.getProperty(m_personService.getPerson(client.getUserName()),
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
            catch (Throwable ex2)
            {
                logger.error("Failed to rollback transaction", ex2);
            }
            
             //          Re-throw the exception
            if (ex instanceof RuntimeException)
            {
                throw (RuntimeException) ex;
            }
            else
            {
                throw new RuntimeException("Error during execution of transaction.", ex);
            }
        }
    }
    
    /**
     * Map the case insensitive logon name to the internal person object user name
     * 
     * @param userName String
     * @return String
     */
    protected final String mapUserNameToPerson(String userName)
    {
        // Get the home folder for the user
        
        UserTransaction tx = m_transactionService.getUserTransaction();
        String personName = null;
        
        try
        {
            tx.begin();
            personName = m_personService.getUserIdentifier( userName);
            tx.commit();
        }
        catch (Throwable ex)
        {
            try
            {
                tx.rollback();
            }
            catch (Throwable ex2)
            {
                logger.error("Failed to rollback transaction", ex2);
            }
            
            // Re-throw the exception
            
            if (ex instanceof RuntimeException)
            {
                throw (RuntimeException) ex;
            }
            else
            {
                throw new RuntimeException("Error during execution of transaction.", ex);
            }
        }
        
        // Return the person name
        
        return personName;
    }
}