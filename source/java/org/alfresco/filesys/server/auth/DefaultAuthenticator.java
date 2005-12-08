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

import org.alfresco.config.ConfigElement;
import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.config.InvalidConfigurationException;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.server.core.SharedDevice;

/**
 * <p>
 * Default authenticator class.
 * <p>
 * The default authenticator implementation enables user level security mode and allows any user to
 * connect to the server.
 */
public class DefaultAuthenticator extends SrvAuthenticator
{

    // Server configuration

    private ServerConfiguration m_config;

    /**
     * Class constructor
     */
    public DefaultAuthenticator()
    {
        setAccessMode(USER_MODE);
        setEncryptedPasswords(true);
    }

    /**
     * Allow any user to access the server
     * 
     * @param client Client details.
     * @param share Shared device the user is connecting to.
     * @param pwd Share level password.
     * @param sess Server session
     * @return int
     */
    public int authenticateShareConnect(ClientInfo client, SharedDevice share, String pwd, SrvSession sess)
    {
        return Writeable;
    }

    /**
     * Allow any user to access the server.
     * 
     * @param client Client details.
     * @param sess Server session
     * @param alg Encryption algorithm
     * @return int
     */
    public int authenticateUser(ClientInfo client, SrvSession sess, int alg)
    {
        return AUTH_ALLOW;
    }

    /**
     * The default authenticator does not use encrypted passwords.
     * 
     * @param sess SrvSession
     * @return byte[]
     */
    public byte[] getChallengeKey(SrvSession sess)
    {
        return null;
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
     * Initialzie the authenticator
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