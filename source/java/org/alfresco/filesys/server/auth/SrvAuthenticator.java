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
package org.alfresco.filesys.server.auth;

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
import org.alfresco.filesys.smb.server.repo.ContentContext;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.MD4PasswordEncoder;
import org.alfresco.repo.security.authentication.MD4PasswordEncoderImpl;
import org.alfresco.repo.security.authentication.NTLMMode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * An authenticator is used by the SMB server to authenticate users when in user level access mode
 * and authenticate requests to connect to a share when in share level access.
 */
public abstract class SrvAuthenticator
{
    // Logging
    
    protected static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol.auth");

    // Encryption algorithm types

    public static final int LANMAN = PasswordEncryptor.LANMAN;
    public static final int NTLM1 = PasswordEncryptor.NTLM1;
    public static final int NTLM2 = PasswordEncryptor.NTLM2;

    // Authentication status values

    public static final int AUTH_ALLOW = 0;
    public static final int AUTH_GUEST = 0x10000000;
    public static final int AUTH_DISALLOW = -1;
    public static final int AUTH_BADPASSWORD = -2;
    public static final int AUTH_BADUSER = -3;

    // Share access permissions, returned by authenticateShareConnect()

    public static final int NoAccess = 0;
    public static final int ReadOnly = 1;
    public static final int Writeable = 2;

    // Server access mode

    public static final int SHARE_MODE = 0;
    public static final int USER_MODE = 1;

    // Standard encrypted password length

    public static final int STANDARD_PASSWORD_LEN = 24;

    // Default guest user name
    
    protected static final String GUEST_USERNAME = "guest";

    // Server access mode

    private int m_accessMode = SHARE_MODE;

    // Use encrypted password

    private boolean m_encryptPwd = false;

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
        
        return SrvAuthenticator.Writeable;
    }

    /**
     * Authenticate a user. A user may be granted full access, guest access or no access.
     * 
     * @param client User/client details from the session setup request.
     * @param sess Server session
     * @param alg Encryption algorithm
     * @return int Access level or disallow status.
     */
    public abstract int authenticateUser(ClientInfo client, SrvSession sess, int alg);

    /**
     * Return the user account details for the specified user
     * 
     * @param user String
     * @return UserAccount
     */
    public UserAccount getUserDetails(String user)
    {
        return null;
    }

    /**
     * Authenticate a user using a plain text password.
     * 
     * @param client User/client details from the session setup request.
     * @param sess Server session
     * @return int Access level or disallow status.
     * @throws InvalidConfigurationException
     */
    public final int authenticateUserPlainText(ClientInfo client, SrvSession sess)
    {

        // Get a challenge key

        sess.setChallengeKey(getChallengeKey(sess));

        if (sess.hasChallengeKey() == false)
            return SrvAuthenticator.AUTH_DISALLOW;

        // Get the plain text password

        String textPwd = client.getPasswordAsString();
        if (textPwd == null)
            textPwd = client.getANSIPasswordAsString();

        // Encrypt the password

        byte[] encPwd = generateEncryptedPassword(textPwd, sess.getChallengeKey(), SrvAuthenticator.NTLM1);
        client.setPassword(encPwd);

        // Authenticate the user

        return authenticateUser(client, sess, SrvAuthenticator.NTLM1);
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
        
        if ( m_authComponent.getNTLMMode() != NTLMMode.MD4_PROVIDER &&
                m_authComponent.getNTLMMode() != NTLMMode.PASS_THROUGH)
            throw new InvalidConfigurationException("Required authentication mode not available");
        
        // Get hold of various services
        
        m_nodeService = config.getNodeService();
        m_personService = config.getPersonService();
        m_transactionService = config.getTransactionService();
        m_authenticationService = config.getAuthenticationService();
        
        // Set the guest user name
        
        setGuestUserName( m_authComponent.getGuestUserName());
    }

    /**
     * Encrypt the plain text password with the specified encryption key using the specified
     * encryption algorithm.
     * 
     * @return byte[]
     * @param plainPwd java.lang.String
     * @param encryptKey byte[]
     * @param alg int
     */
    protected final byte[] generateEncryptedPassword(String plainPwd, byte[] encryptKey, int alg)
    {

        // Use the password encryptor

        byte[] encPwd = null;

        try
        {

            // Encrypt the password

            encPwd = m_encryptor.generateEncryptedPassword(plainPwd, encryptKey, alg);
        }
        catch (NoSuchAlgorithmException ex)
        {
        }

        // Return the encrypted password

        return encPwd;
    }

    /**
     * Return the access mode of the server, either SHARE_MODE or USER_MODE.
     * 
     * @return int
     */
    public final int getAccessMode()
    {
        return m_accessMode;
    }

    /**
     * Get a challenge encryption key, when encrypted passwords are enabled.
     * 
     * @param sess SrvSession
     * @return byte[]
     */
    public abstract byte[] getChallengeKey(SrvSession sess);

    /**
     * Determine if encrypted passwords should be used.
     * 
     * @return boolean
     */
    public final boolean hasEncryptPasswords()
    {
        return m_encryptPwd;
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
     * Set the access mode of the server.
     * 
     * @param mode Either SHARE_MODE or USER_MODE.
     */
    public final void setAccessMode(int mode)
    {
        m_accessMode = mode;
    }

    /**
     * Set/clear the encrypted passwords flag.
     * 
     * @param encFlag Encrypt passwords if true, use plain text passwords if false.
     */
    public final void setEncryptedPasswords(boolean encFlag)
    {
        m_encryptPwd = encFlag;
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
     * @return boolean
     * @param plainPwd java.lang.String
     * @param encryptedPwd java.lang.String
     * @param encryptKey byte[]
     * @param alg int
     */
    protected final boolean validatePassword(String plainPwd, byte[] encryptedPwd, byte[] encryptKey, int alg)
    {

        // Generate an encrypted version of the plain text password

        byte[] encPwd = generateEncryptedPassword(plainPwd != null ? plainPwd : "", encryptKey, alg);

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
    
}