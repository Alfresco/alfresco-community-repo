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

import java.util.Random;

import org.alfresco.config.ConfigElement;
import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.config.InvalidConfigurationException;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.server.core.ShareType;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.smb.server.SMBSrvSession;
import org.alfresco.filesys.util.DataPacker;

/**
 * <p>
 * Local Authenticator Class.
 * <p>
 * The local authenticator implementation enables user level security mode and uses the user account
 * list that is part of the server configuration to determine if a user is allowed to access the
 * server/share.
 * <p>
 * Note: Switching off encrypted password support will cause later NT4 service pack releases and
 * Win2000 to refuse to connect to the server without a registry update on the client.
 */
public class LocalAuthenticator extends SrvAuthenticator
{

    // Random number generator used to generate challenge keys

    private Random m_random = new Random(System.currentTimeMillis());

    // Server configuration

    private ServerConfiguration m_config;

    /**
     * Local Authenticator Constructor
     * <p>
     * Default to user mode security with encrypted password support.
     */
    public LocalAuthenticator()
    {
        setAccessMode(SrvAuthenticator.USER_MODE);
        setEncryptedPasswords(true);
    }

    /**
     * Authenticate the connection to a share
     * 
     * @param client ClienInfo
     * @param share SharedDevice
     * @param pwd Share level password.
     * @param sess Server session
     * @return Authentication status.
     */
    public int authenticateShareConnect(ClientInfo client, SharedDevice share, String pwd, SrvSession sess)
    {

        // If the server is in share mode security allow the user access

        if (this.getAccessMode() == SHARE_MODE)
            return SrvAuthenticator.Writeable;

        // Check if the IPC$ share is being accessed

        if (share.getType() == ShareType.ADMINPIPE)
            return SrvAuthenticator.Writeable;

        // Check if the user is allowed to access the specified shared device
        //
        // If a user does not have access to the requested share the connection will still be
        // allowed
        // but any attempts to access files or search directories will result in a 'no access
        // rights'
        // error being returned to the client.

        UserAccount user = null;
        if (client != null)
            user = getUserDetails(client.getUserName());

        if (user == null)
        {

            // Check if the guest account is enabled

            return allowGuest() ? SrvAuthenticator.Writeable : SrvAuthenticator.NoAccess;
        }
        else if (user.hasShare(share.getName()) == false)
            return SrvAuthenticator.NoAccess;

        // Allow user to access this share

        return SrvAuthenticator.Writeable;
    }

    /**
     * Authenticate a user
     * 
     * @param client Client information
     * @param sess Server session
     * @param alg Encryption algorithm
     */
    public int authenticateUser(ClientInfo client, SrvSession sess, int alg)
    {

        // Check if the user exists in the user list

        UserAccount userAcc = getUserDetails(client.getUserName());
        if (userAcc != null)
        {

            // Validate the password

            boolean authSts = false;

            if (client.getPassword() != null)
            {

                // Validate using the Unicode password

                authSts = validatePassword(userAcc.getPassword(), client.getPassword(), sess.getChallengeKey(), alg);
            }
            else if (client.hasANSIPassword())
            {

                // Validate using the ANSI password with the LanMan encryption

                authSts = validatePassword(userAcc.getPassword(), client.getANSIPassword(), sess.getChallengeKey(),
                        SrvAuthenticator.LANMAN);
            }

            // Return the authentication status

            return authSts == true ? SrvAuthenticator.AUTH_ALLOW : SrvAuthenticator.AUTH_BADPASSWORD;
        }

        // Check if this is an SMB/CIFS null session logon.
        //
        // The null session will only be allowed to connect to the IPC$ named pipe share.

        if (client.isNullSession() && sess instanceof SMBSrvSession)
            return SrvAuthenticator.AUTH_ALLOW;

        // Unknown user

        return allowGuest() ? SrvAuthenticator.AUTH_GUEST : SrvAuthenticator.AUTH_DISALLOW;
    }

    /**
     * Generate a challenge key
     * 
     * @param sess SrvSession
     * @return byte[]
     */
    public byte[] getChallengeKey(SrvSession sess)
    {

        // Generate a new challenge key, pack the key and return

        byte[] key = new byte[8];

        DataPacker.putIntelLong(m_random.nextLong(), key, 0);
        return key;
    }

    /**
     * Search for the requried user account details in the defined user list
     * 
     * @param user String
     * @return UserAccount
     */
    public UserAccount getUserDetails(String user)
    {

        // Get the user account list from the configuration

        UserAccountList userList = m_config.getUserAccounts();
        if (userList == null || userList.numberOfUsers() == 0)
            return null;

        // Search for the required user account record

        return userList.findUser(user);
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

        // Save the server configuration so we can access the defined user list

        m_config = config;
    }
}