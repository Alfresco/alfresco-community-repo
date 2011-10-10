/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.imap;

import javax.mail.internet.MimeMessage;

import com.icegreen.greenmail.mail.MovingMessage;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserException;

/**
 * Alfresco implementation of the GreenMailUser interface.
 * 
 * @author Arseny Kovalchuk
 */
public class AlfrescoImapUser implements GreenMailUser
{
    private String userName;
    private char[] password;
    private String email;

    public AlfrescoImapUser(String email, String login, String password)
    {
        this.email = email;
        this.userName = login;
        this.password = password == null ? null : password.toCharArray();
    }

    public void authenticate(String password) throws UserException
    {
        throw new UnsupportedOperationException();
        // This method is used in the POP3 greenmail implementation, so it is disabled for IMAP
        // See AlfrescoImapUserManager.test() method.
    }

    public void create() throws UserException
    {
        throw new UnsupportedOperationException();
    }

    public void delete() throws UserException
    {
        throw new UnsupportedOperationException();
    }

    public void deliver(MovingMessage msg) throws UserException
    {
        throw new UnsupportedOperationException();
    }

    public void deliver(MimeMessage msg) throws UserException
    {
        throw new UnsupportedOperationException();
    }

    public String getEmail()
    {
        return this.email;
    }

    public String getLogin()
    {
        return this.userName;
    }

    public String getPassword()
    {
        return new String(this.password);
    }

    public String getQualifiedMailboxName()
    {
        return userName;
    }

    public void setPassword(String password)
    {
        this.password = password.toCharArray();
    }

}
