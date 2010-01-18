/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.authentication;

import org.alfresco.service.cmr.security.MutableAuthenticationService;

/**
 * The default implementation of {@link MutableAuthenticationService}.
 * 
 * @author dward
 */
public class MutableAuthenticationServiceImpl extends AuthenticationServiceImpl implements MutableAuthenticationService
{

    /** The authentication dao. */
    MutableAuthenticationDao authenticationDao;

    /**
     * Sets the authentication dao.
     * 
     * @param authenticationDao
     *            the authentication dao
     */
    public void setAuthenticationDao(MutableAuthenticationDao authenticationDao)
    {
        this.authenticationDao = authenticationDao;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.security.MutableAuthenticationService#createAuthentication(java.lang.String,
     * char[])
     */
    public void createAuthentication(String userName, char[] password) throws AuthenticationException
    {
        this.authenticationDao.createUser(userName, password);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.security.MutableAuthenticationService#updateAuthentication(java.lang.String,
     * char[], char[])
     */
    public void updateAuthentication(String userName, char[] oldPassword, char[] newPassword)
            throws AuthenticationException
    {
        // Need to preserve the run-as user
        String currentUser = AuthenticationUtil.getRunAsUser();
        try
        {
            authenticate(userName, oldPassword);
        }
        finally
        {
            AuthenticationUtil.setRunAsUser(currentUser);
        }
        this.authenticationDao.updateUser(userName, newPassword);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.security.MutableAuthenticationService#setAuthentication(java.lang.String, char[])
     */
    public void setAuthentication(String userName, char[] newPassword) throws AuthenticationException
    {
        this.authenticationDao.updateUser(userName, newPassword);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.security.MutableAuthenticationService#deleteAuthentication(java.lang.String)
     */
    public void deleteAuthentication(String userName) throws AuthenticationException
    {
        this.authenticationDao.deleteUser(userName);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.security.authentication.AuthenticationServiceImpl#getAuthenticationEnabled(java.lang.String)
     */
    @Override
    public boolean getAuthenticationEnabled(String userName) throws AuthenticationException
    {
        return this.authenticationDao.getEnabled(userName);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.security.MutableAuthenticationService#setAuthenticationEnabled(java.lang.String,
     * boolean)
     */
    public void setAuthenticationEnabled(String userName, boolean enabled) throws AuthenticationException
    {
        this.authenticationDao.setEnabled(userName, enabled);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.authentication.AuthenticationServiceImpl#authenticationExists(java.lang.String)
     */
    @Override
    public boolean authenticationExists(String userName)
    {
        return this.authenticationDao.userExists(userName);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.security.MutableAuthenticationService#isAuthenticationMutable(java.lang.String)
     */
    public boolean isAuthenticationMutable(String userName)
    {
        return authenticationExists(userName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.MutableAuthenticationService#isAuthenticationCreationAllowed()
     */
    public boolean isAuthenticationCreationAllowed()
    {
        return true;
    }
}
