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
