/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.imap.exception.AlfrescoImapFolderException;
import org.alfresco.util.Utf7;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.icegreen.greenmail.imap.AuthorizationException;
import com.icegreen.greenmail.imap.ImapHostManager;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.user.GreenMailUser;

/**
 * This Host Manager is assumed to be session local, i.e. there is one instance per IMAP TCP connection. This means that
 * it can locally cache items being interacted with during the session and its knowledge of which folders / messages do
 * or do not exist will match that of the client.
 * 
 * @author Mike Shavnev
 * @author David Ward
 */
public class AlfrescoImapHostManager implements ImapHostManager
{
    private ImapService imapService;
    private Map<String, AlfrescoImapFolder> folderCache;
    
    private static Log logger = LogFactory.getLog(AlfrescoImapHostManager.class);
    
    /**
     * @param imapService
     */
    public AlfrescoImapHostManager(ImapService imapService)
    {
        this.imapService = imapService;
    }

    /**
     * Returns the hierarchy delimiter for mailboxes on this host.
     * 
     * @return The hierarchy delimiter character.
     */
    public char getHierarchyDelimiter()
    {
        return AlfrescoImapConst.HIERARCHY_DELIMITER;
    }

    /**
     * Returns an collection of mailboxes. Method searches mailboxes under mount points defined for a specific user.
     * Mount points include user's IMAP Virtualised Views and Email Archive Views. This method serves LIST command
     * of the IMAP protocol.
     * 
     * @param user User making the request
     * @param mailboxPattern String name of a mailbox possible including a wildcard.
     * @return Collection of mailboxes matching the pattern.
     * @throws com.icegreen.greenmail.store.FolderException
     */
    public Collection<MailFolder> listMailboxes(GreenMailUser user, String mailboxPattern) throws FolderException
    {
        try
        {
            AlfrescoImapUser alfrescoUser = new AlfrescoImapUser(user.getEmail(), user.getLogin(), user.getPassword());
            return registerMailboxes(imapService.listMailboxes(alfrescoUser, getUnqualifiedMailboxPattern(
                    alfrescoUser, mailboxPattern), false));
        }
        catch (Throwable e)
        {
            logger.debug(e.getMessage(), e);
            throw new FolderException(e.getMessage());
        }
    }

    /**
     * Returns an collection of subscribed mailboxes. To appear in search result mailboxes should have
     * {http://www.alfresco.org/model/imap/1.0}subscribed property specified for user. Method searches
     * subscribed mailboxes under mount points defined for a specific user. Mount points include user's
     * IMAP Virtualised Views and Email Archive Views. This method serves LSUB command of the IMAP protocol.
     * 
     * @param user User making the request
     * @param mailboxPattern String name of a mailbox possible including a wildcard.
     * @return Collection of mailboxes matching the pattern.
     * @throws com.icegreen.greenmail.store.FolderException
     */
    public Collection<MailFolder> listSubscribedMailboxes(GreenMailUser user, String mailboxPattern)
            throws FolderException
    {
        try
        {
            AlfrescoImapUser alfrescoUser = new AlfrescoImapUser(user.getEmail(), user.getLogin(), user.getPassword());
            return registerMailboxes(imapService.listMailboxes(alfrescoUser, getUnqualifiedMailboxPattern(
                    alfrescoUser, mailboxPattern), true));
        }
        catch (Throwable e)
        {
            logger.debug(e.getMessage(), e);
            throw new FolderException(e.getMessage());
        }
    }

    /**
     * Renames an existing mailbox. The specified mailbox must already exist, the requested name must not exist
     * already but must be able to be created and the user must have rights to delete the existing mailbox and
     * create a mailbox with the new name. Any inferior hierarchical names must also be renamed. If INBOX is renamed,
     * the contents of INBOX are transferred to a new mailbox with the new name, but INBOX is not deleted.
     * If INBOX has inferior mailbox these are not renamed. This method serves RENAME command of the IMAP
     * protocol. <p/> Method searches mailbox under mount points defined for a specific user. Mount points
     * include user's IMAP Virtualised Views and Email Archive Views.
     * 
     * @param user User making the request.
     * @param oldMailboxName String name of the existing folder
     * @param newMailboxName String target new name
     * @throws com.icegreen.greenmail.store.FolderException if an existing folder with the new name.
     * @throws AlfrescoImapFolderException if user does not have rights to create the new mailbox.
     */
    public void renameMailbox(GreenMailUser user, String oldMailboxName, String newMailboxName) throws FolderException, AuthorizationException
    {
        try
        {
            AlfrescoImapUser alfrescoUser = new AlfrescoImapUser(user.getEmail(), user.getLogin(), user.getPassword());
            String oldFolderPath = getUnqualifiedMailboxPattern(alfrescoUser,
                    oldMailboxName);
            String newFolderpath = getUnqualifiedMailboxPattern(alfrescoUser, newMailboxName);
            imapService.renameMailbox(alfrescoUser, oldFolderPath, newFolderpath);
            if (folderCache != null)
            {
                folderCache.remove(oldFolderPath);
                folderCache.remove(newFolderpath);
            }
        }
        catch (Throwable e)
        {
            logger.debug(e.getMessage(), e);
            throw new FolderException(e.getMessage());
        }
    }

    /**
     * Returns a reference to a newly created mailbox. The request should specify a mailbox that does not
     * already exist on this server, that could exist on this server and that the user has rights to create.
     * This method serves CREATE command of the IMAP protocol.
     * 
     * @param user User making the request.
     * @param mailboxName String name of the target
     * @return an Mailbox reference.
     * @throws com.icegreen.greenmail.store.FolderException if mailbox already exists
     * @throws AlfrescoImapFolderException if user does not have rights to create the new mailbox.
     */
    public MailFolder createMailbox(GreenMailUser user, String mailboxName) throws AuthorizationException, FolderException
    {
        try
        {
            AlfrescoImapUser alfrescoUser = new AlfrescoImapUser(user.getEmail(), user.getLogin(), user.getPassword());
            return registerMailBox(imapService.getOrCreateMailbox(alfrescoUser, getUnqualifiedMailboxPattern(
                    alfrescoUser, mailboxName), false, true));
        }
        catch (Throwable e)
        {
            logger.debug(e.getMessage(), e);
            throw new FolderException(e.getMessage());
        }
    }

    /**
     * Deletes an existing MailBox. Specified mailbox must already exist on this server, and the user
     * must have rights to delete it. <p/> This method serves DELETE command of the IMAP protocol.
     * 
     * @param user User making the request.
     * @param mailboxName String name of the target
     * @throws com.icegreen.greenmail.store.FolderException if mailbox has a non-selectable store with children
     */
    public void deleteMailbox(GreenMailUser user, String mailboxName) throws FolderException, AuthorizationException
    {
        try
        {
            AlfrescoImapUser alfrescoUser = new AlfrescoImapUser(user.getEmail(), user.getLogin(), user.getPassword());
            String folderPath = getUnqualifiedMailboxPattern(alfrescoUser, mailboxName);
            imapService.deleteMailbox(alfrescoUser, folderPath);
            if (folderCache != null)
            {
                folderCache.remove(folderPath);
            }
        }
        catch (Throwable e)
        {
            logger.debug(e.getMessage(), e);
            throw new FolderException(e.getMessage());
        }
    }

    /**
     * Returns a reference to an existing Mailbox. The requested mailbox must already exists on this server and the
     * requesting user must have at least lookup rights. <p/> It is also can be used by to obtain hierarchy delimiter
     * by the LIST command: <p/> C: 2 list "" "" <p/> S: * LIST () "." "" <p/> S: 2 OK LIST completed.
     * <p/>
     * Method searches mailbox under mount points defined for a specific user. Mount points include user's IMAP
     * Virtualised Views and Email Archive Views.
     * 
     * @param user User making the request.
     * @param mailboxName String name of the target.
     * @return an Mailbox reference.
     */
    public MailFolder getFolder(GreenMailUser user, String mailboxName)
    {
        AlfrescoImapUser alfrescoUser = new AlfrescoImapUser(user.getEmail(), user.getLogin(), user.getPassword());
        String folderPath = getUnqualifiedMailboxPattern(
                alfrescoUser, mailboxName);
        // Warm up the cache if necessary
        if (folderCache == null)
        {
            registerMailboxes(imapService.listMailboxes(alfrescoUser, "*", true));
        }
        // Try to retrieve from the cache
        AlfrescoImapFolder result = folderCache.get(folderPath);
        if (result != null && result.reset())
        {
            return result;
        }
        
        // Look up and cache as a last resort
        return registerMailBox(imapService.getOrCreateMailbox(alfrescoUser, folderPath, true, false));
    }

    /**
     * Simply calls {@link #getFolder(GreenMailUser, String)}. <p/> Added to implement {@link ImapHostManager}.
     */
    public MailFolder getFolder(final GreenMailUser user, final String mailboxName, boolean mustExist)
            throws FolderException
    {
        return getFolder(user, mailboxName);
    }

    /**
     * Returns a reference to the user's INBOX.
     * 
     * @param user The user making the request.
     * @return The user's Inbox.
     */
    public MailFolder getInbox(GreenMailUser user) throws FolderException
    {
        return getFolder(new AlfrescoImapUser(user.getEmail(), user.getLogin(), user.getPassword()), AlfrescoImapConst.INBOX_NAME);
    }

    /**
     * Not supported. May be used by GreenMailUser.create() method. <p/> Added to implement {@link ImapHostManager}.
     */
    public void createPrivateMailAccount(GreenMailUser user) throws FolderException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Subscribes a user to a mailbox. The mailbox must exist locally and the user must have rights to modify it.
     * <p/>
     * This method serves SUBSCRIBE command of the IMAP protocol.
     * 
     * @param user User making the request
     * @param mailbox String representation of a mailbox name.
     */
    public void subscribe(GreenMailUser user, String mailbox) throws FolderException
    {
        try
        {
            AlfrescoImapUser alfrescoUser = new AlfrescoImapUser(user.getEmail(), user.getLogin(), user.getPassword());
            imapService.subscribe(alfrescoUser, getUnqualifiedMailboxPattern(alfrescoUser, mailbox));
        }
        catch (Throwable e)
        {
            logger.debug(e.getMessage(), e);
            throw new FolderException(e.getMessage());
        }
    }

    /**
     * Unsubscribes from a given mailbox. <p/> This method serves UNSUBSCRIBE command of the IMAP protocol.
     * 
     * @param user User making the request
     * @param mailbox String representation of a mailbox name.
     */
    public void unsubscribe(GreenMailUser user, String mailbox) throws FolderException
    {
        try
        {
            AlfrescoImapUser alfrescoUser = new AlfrescoImapUser(user.getEmail(), user.getLogin(), user.getPassword());
            imapService.unsubscribe(alfrescoUser, getUnqualifiedMailboxPattern(alfrescoUser, mailbox));
        }
        catch (Throwable e)
        {
            logger.debug(e.getMessage(), e);
            throw new FolderException(e.getMessage());
        }
    }

    /**
     * Not supported. Used by GreenMail class.
     */
    public List<?> getAllMessages()
    {
        throw new UnsupportedOperationException();
    }    

    private String getUnqualifiedMailboxPattern(AlfrescoImapUser user, String mailboxPattern)
    {
        mailboxPattern = Utf7.decode(mailboxPattern, Utf7.UTF7_MODIFIED);
        if (mailboxPattern.startsWith(AlfrescoImapConst.NAMESPACE_PREFIX))
        {
            int sepIndex = mailboxPattern.indexOf(AlfrescoImapConst.HIERARCHY_DELIMITER);
            return sepIndex == -1 ? mailboxPattern.endsWith("*") ? "*" : "" : mailboxPattern.substring(sepIndex + 1);
        }
        return mailboxPattern;
    }


    /**
     * Caches returned mail folder collection and converts to a generic MailFolder collection.
     * 
     * @param mailboxes
     *            a Collection of Alfresco mailboxes
     * @return a generic MailFolder collection associated with the session
     */
    private Collection<MailFolder> registerMailboxes(Collection<AlfrescoImapFolder> mailboxes)
    {
        int size = mailboxes.size();
        if (size == 0)
        {
            return Collections.emptyList();
        }
        Collection<MailFolder> result = new ArrayList<MailFolder>(size);
        if (folderCache == null)
        {
            folderCache = new HashMap<String, AlfrescoImapFolder>(size * 2);
        }
        for (AlfrescoImapFolder mailbox : mailboxes)
        {
            result.add(registerMailBox(mailbox));
        }
        return result;
    }
    
    /**
     * Caches a returned mail folder and converts to a generic MailFolder.
     * 
     * @param mailbox
     *            an Alfresco mailbox
     * @return a generic MailFolder associated with the session
     */
    private MailFolder registerMailBox(AlfrescoImapFolder mailbox)
    {
        String folderPath = mailbox.getFolderPath();
        if ((mailbox.isSelectable() || folderPath.isEmpty()) && folderCache != null)
        {
            AlfrescoImapFolder oldFolder = folderCache.get(folderPath);
            if (oldFolder != null
                    && oldFolder.getFolderInfo().getNodeRef().equals(mailbox.getFolderInfo().getNodeRef())
                    && oldFolder.reset())
            {
                mailbox = oldFolder;
            }
            else
            {
                folderCache.put(folderPath, mailbox);
            }
        }
        return mailbox;
    }
}
