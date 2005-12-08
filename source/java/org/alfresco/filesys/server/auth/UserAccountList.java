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

import java.util.Vector;

/**
 * User Account List Class
 */
public class UserAccountList
{

    // User account list

    private Vector<UserAccount> m_users;

    /**
     * Create a user account list.
     */
    public UserAccountList()
    {
        m_users = new Vector<UserAccount>();
    }

    /**
     * Add a user to the list of accounts.
     * 
     * @param user UserAccount
     */
    public final void addUser(UserAccount user)
    {

        // Check if the user exists on the list

        removeUser(user);
        m_users.add(user);
    }

    /**
     * Find the required user account details.
     * 
     * @param user java.lang.String
     * @return UserAccount
     */
    public final UserAccount findUser(String user)
    {

        // Search for the specified user account

        for (int i = 0; i < m_users.size(); i++)
        {
            UserAccount acc = m_users.get(i);
            if (acc.getUserName().equalsIgnoreCase(user))
                return acc;
        }

        // User not found

        return null;
    }

    /**
     * Determine if the specified user account exists in the list.
     * 
     * @return boolean
     * @param user java.lang.String
     */
    public final boolean hasUser(String user)
    {

        // Search for the specified user account

        for (int i = 0; i < m_users.size(); i++)
        {
            UserAccount acc = m_users.get(i);
            if (acc.getUserName().compareTo(user) == 0)
                return true;
        }

        // User not found

        return false;
    }

    /**
     * Return the specified user account details
     * 
     * @param idx int
     * @return UserAccount
     */
    public final UserAccount getUserAt(int idx)
    {
        if (idx >= m_users.size())
            return null;
        return m_users.get(idx);
    }

    /**
     * Return the number of defined user accounts.
     * 
     * @return int
     */
    public final int numberOfUsers()
    {
        return m_users.size();
    }

    /**
     * Remove all user accounts from the list.
     */
    public final void removeAllUsers()
    {
        m_users.removeAllElements();
    }

    /**
     * Remvoe the specified user account from the list.
     * 
     * @param userAcc UserAccount
     */
    public final void removeUser(UserAccount userAcc)
    {

        // Search for the specified user account

        for (int i = 0; i < m_users.size(); i++)
        {
            UserAccount acc = m_users.get(i);
            if (acc.getUserName().compareTo(userAcc.getUserName()) == 0)
            {
                m_users.remove(i);
                return;
            }
        }
    }

    /**
     * Remvoe the specified user account from the list.
     * 
     * @param user java.lang.String
     */
    public final void removeUser(String user)
    {

        // Search for the specified user account

        for (int i = 0; i < m_users.size(); i++)
        {
            UserAccount acc = m_users.get(i);
            if (acc.getUserName().compareTo(user) == 0)
            {
                m_users.remove(i);
                return;
            }
        }
    }

    /**
     * Return the user account list as a string.
     * 
     * @return java.lang.String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("[");
        str.append(m_users.size());
        str.append(":");

        for (int i = 0; i < m_users.size(); i++)
        {
            UserAccount acc = m_users.get(i);
            str.append(acc.getUserName());
            str.append(",");
        }
        str.append("]");

        return str.toString();
    }
}