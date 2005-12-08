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

import org.alfresco.filesys.util.StringList;

/**
 * User Account Class
 * <p>
 * Holds the details of a user account on the server.
 */
public class UserAccount
{

    // User name and password

    private String m_userName;
    private String m_password;

    // Real user name and comment

    private String m_realName;
    private String m_comment;

    // List of shares this user is allowed to use

    private StringList m_shares;

    // Administrator flag

    private boolean m_admin;

    // Home directory

    private String m_homeDir;

    /**
     * Default constructor
     */
    public UserAccount()
    {
        super();
    }

    /**
     * Create a user with the specified name and password.
     * 
     * @param user String
     * @param pwd String
     */
    public UserAccount(String user, String pwd)
    {
        setUserName(user);
        setPassword(pwd);
    }

    /**
     * Add the specified share to the list of allowed shares for this user.
     * 
     * @param shr java.lang.String
     */
    public final void addShare(String shr)
    {
        if (m_shares == null)
            m_shares = new StringList();
        m_shares.addString(shr);
    }

    /**
     * Determine if this user is allowed to access the specified share.
     * 
     * @return boolean
     * @param shr java.lang.String
     */
    public final boolean allowsShare(String shr)
    {
        if (m_shares == null)
            return true;
        else if (m_shares.containsString(shr))
            return true;
        return false;
    }

    /**
     * Check if the user has a home direectory configured
     * 
     * @return boolean
     */
    public final boolean hasHomeDirectory()
    {
        return m_homeDir != null ? true : false;
    }

    /**
     * Return the home directory for this user
     * 
     * @return String
     */
    public final String getHomeDirectory()
    {
        return m_homeDir;
    }

    /**
     * Return the password
     * 
     * @return java.lang.String
     */
    public final String getPassword()
    {
        return m_password;
    }

    /**
     * Return the user name.
     * 
     * @return java.lang.String
     */
    public final String getUserName()
    {
        return m_userName;
    }

    /**
     * Return the real user name
     * 
     * @return String
     */
    public final String getRealName()
    {
        return m_realName;
    }

    /**
     * Return the user comment
     * 
     * @return String
     */
    public final String getComment()
    {
        return m_comment;
    }

    /**
     * Check if the specified share is listed in the users allowed list.
     * 
     * @return boolean
     * @param shr java.lang.String
     */
    public final boolean hasShare(String shr)
    {
        if (m_shares != null && m_shares.containsString(shr) == false)
            return false;
        return true;
    }

    /**
     * Detemrine if this account is restricted to using certain shares only.
     * 
     * @return boolean
     */
    public final boolean hasShareRestrictions()
    {
        return m_shares == null ? false : true;
    }

    /**
     * Return the list of shares
     * 
     * @return StringList
     */
    public final StringList getShareList()
    {
        return m_shares;
    }

    /**
     * Determine if this user in an administrator.
     * 
     * @return boolean
     */
    public final boolean isAdministrator()
    {
        return m_admin;
    }

    /**
     * Remove all shares from the list of restricted shares.
     */
    public final void removeAllShares()
    {
        m_shares = null;
    }

    /**
     * Remove the specified share from the list of shares this user is allowed to access.
     * 
     * @param shr java.lang.String
     */
    public final void removeShare(String shr)
    {

        // Check if the share list has been allocated

        if (m_shares != null)
        {

            // Remove the share from the list

            m_shares.removeString(shr);

            // Check if the list is empty

            if (m_shares.numberOfStrings() == 0)
                m_shares = null;
        }
    }

    /**
     * Set the administrator flag.
     * 
     * @param admin boolean
     */
    public final void setAdministrator(boolean admin)
    {
        m_admin = admin;
    }

    /**
     * Set the user home directory
     * 
     * @param home String
     */
    public final void setHomeDirectory(String home)
    {
        m_homeDir = home;
    }

    /**
     * Set the password for this account.
     * 
     * @param pwd java.lang.String
     */
    public final void setPassword(String pwd)
    {
        m_password = pwd;
    }

    /**
     * Set the user name.
     * 
     * @param user java.lang.String
     */
    public final void setUserName(String user)
    {
        m_userName = user;
    }

    /**
     * Set the real user name
     * 
     * @param name String
     */
    public final void setRealName(String name)
    {
        m_realName = name;
    }

    /**
     * Set the comment
     * 
     * @param comment String
     */
    public final void setComment(String comment)
    {
        m_comment = comment;
    }

    /**
     * Return the user account as a string.
     * 
     * @return java.lang.String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("[");
        str.append(getUserName());
        str.append(":");
        str.append(getPassword());

        if (isAdministrator())
            str.append("(ADMIN)");

        str.append(",Real=");

        str.append(getRealName());
        str.append(",Comment=");
        str.append(getComment());
        str.append(",Allow=");

        if (m_shares == null)
            str.append("<ALL>");
        else
            str.append(m_shares);
        str.append("]");

        str.append(",Home=");
        if (hasHomeDirectory())
            str.append(getHomeDirectory());
        else
            str.append("None");

        return str.toString();
    }
}