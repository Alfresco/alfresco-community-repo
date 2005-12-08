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

import org.alfresco.config.ConfigElement;
import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.config.InvalidConfigurationException;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.server.core.SharedDevice;

/**
 * <p>
 * An authenticator is used by the SMB server to authenticate users when in user level access mode
 * and authenticate requests to connect to a share when in share level access.
 */
public abstract class SrvAuthenticator
{

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

    // Server access mode

    private int m_accessMode = SHARE_MODE;

    // Use encrypted password

    private boolean m_encryptPwd = false;

    // Password encryption algorithms

    private PasswordEncryptor m_encryptor = new PasswordEncryptor();

    // Flag to enable/disable the guest account

    private boolean m_allowGuest;

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
    public abstract int authenticateShareConnect(ClientInfo client, SharedDevice share, String sharePwd, SrvSession sess);

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
    public abstract UserAccount getUserDetails(String user);

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
}