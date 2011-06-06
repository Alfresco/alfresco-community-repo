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

import java.util.List;

import javax.mail.Flags;
import javax.mail.Flags.Flag;

import org.alfresco.repo.imap.AlfrescoImapConst.ImapViewMode;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Arseny Kovalchuk
 * @since 3.2
 */
public interface ImapService
{
    
    /**
     * Helper enumeration to handle email body format text/html and text/plain for Alfresco/Share webapp
     */
    public static enum EmailBodyFormat
    {
        ALFRESCO_TEXT_PLAIN(AlfrescoImapConst.CLASSPATH_ALFRESCO_TEXT_PLAIN_TEMPLATE),
        SHARE_TEXT_PLAIN(AlfrescoImapConst.CLASSPATH_SHARE_TEXT_PLAIN_TEMPLATE),
        
        ALFRESCO_TEXT_HTML(AlfrescoImapConst.CLASSPATH_ALFRESCO_TEXT_HTML_TEMPLATE),
        SHARE_TEXT_HTML(AlfrescoImapConst.CLASSPATH_SHARE_TEXT_HTML_TEMPLATE);
        
        EmailBodyFormat(String templatePath)
        {
            this.templatePath = templatePath;
        }
        public String getSubtype()
        {
            return name().toLowerCase().substring(name().indexOf("_") + 1 + "TEXT".length());
        }

        public String getTypeSubtype()
        {
            return name().toLowerCase().substring(name().indexOf("_") + 1).replaceAll("_", "");
        }

        public String getMimeType()
        {
            return name().toLowerCase().substring(name().indexOf("_") + 1).replaceAll("_", "/");
        }
        
        public String getClasspathTemplatePath()
        {
            return this.templatePath;
        }
        
        public String getWebApp()
        {
            return name().toLowerCase().substring(0, name().indexOf("_"));
        }
        
        private String templatePath;

    }

    /**
     * Returns an collection of mailboxes. This method serves LIST command of the IMAP protocol.
     * 
     * @param user User making the request
     * @param mailboxPattern String name of a mailbox encoded in MUTF-7, possible including a wildcard.
     * @return Collection of mailboxes matching the pattern.
     */
    public List<AlfrescoImapFolder> listMailboxes(AlfrescoImapUser user, String mailboxPattern);

    /**
     * Returns an collection of subscribed mailboxes. This method serves LSUB command of the IMAP protocol.
     * 
     * @param user User making the request
     * @param mailboxPattern String name of a mailbox encoded in MUTF-7, possible including a wildcard.
     * @return Collection of mailboxes matching the pattern.
     */
    public List<AlfrescoImapFolder> listSubscribedMailboxes(AlfrescoImapUser user, String mailboxPattern);

    /**
     * Returns a reference to a newly created mailbox. The request should specify a mailbox that does not already exist on this server, that could exist on this server and that the
     * user has rights to create. This method serves CREATE command of the IMAP protocol.
     * 
     * @param user User making the request.
     * @param mailboxName String name of the target encoded in MUTF-7,
     * @return an Mailbox reference.
     */
    public AlfrescoImapFolder createMailbox(AlfrescoImapUser user, String mailboxName);

    /**
     * Deletes an existing MailBox. Specified mailbox must already exist on this server, and the user must have rights to delete it. This method serves DELETE command of the IMAP
     * protocol.
     * 
     * @param user User making the request.
     * @param mailboxName String name of the target encoded in MUTF-7,
     * @throws com.icegreen.greenmail.store.FolderException if mailbox has a non-selectable store with children
     */
    public void deleteMailbox(AlfrescoImapUser user, String mailboxName);

    /**
     * Renames an existing mailbox. The specified mailbox must already exist, the requested name must not exist already but must be able to be created and the user must have rights
     * to delete the existing mailbox and create a mailbox with the new name. Any inferior hierarchical names must also be renamed. If INBOX is renamed, the contents of INBOX are
     * transferred to a new mailbox with the new name, but INBOX is not deleted. If INBOX has inferior mailbox these are not renamed. This method serves RENAME command of the IMAP
     * protocol.
     * 
     * @param user User making the request.
     * @param oldMailboxName String name of the existing folder encoded in MUTF-7,
     * @param newMailboxName String target new name encoded in MUTF-7,
     */
    public void renameMailbox(AlfrescoImapUser user, String oldMailboxName, String newMailboxName);

    /**
     * Returns a reference to an existing Mailbox. The requested mailbox must already exists on this server and the requesting user must have at least lookup rights. <p/> It is
     * also can be used by to obtain hierarchy delimiter by the LIST command: <p/> C: 2 list "" "" <p/> S: * LIST () "." "" <p/> S: 2 OK LIST completed.
     * 
     * @param user User making the request.
     * @param mailboxName String name of the target encoded in MUTF-7,.
     * @return an Mailbox reference.
     */
    public AlfrescoImapFolder getFolder(AlfrescoImapUser user, String mailboxName);

    /**
     * Get root reference for the specified mailbox
     * 
     * @param mailboxName mailbox name in IMAP client.
     * @param userName
     * @return NodeRef of root reference for the specified mailbox
     */
    public NodeRef getMailboxRootRef(String mailboxName, String userName);

    /**
     * Subscribes a user to a mailbox. The mailbox must exist locally and the user must have rights to modify it. <p/> This method serves SUBSCRIBE command of the IMAP protocol.
     * 
     * @param user User making the request
     * @param mailbox String representation of a mailbox name encoded in MUTF-7,.
     */
    public void subscribe(AlfrescoImapUser user, String mailbox);

    /**
     * Unsubscribes from a given mailbox. <p/> This method serves UNSUBSCRIBE command of the IMAP protocol.
     * 
     * @param user User making the request
     * @param mailbox String representation of a mailbox name encoded in MUTF-7,.
     */
    public void unsubscribe(AlfrescoImapUser user, String mailbox);

//    /**
//     * Search for files in specified context
//     * 
//     * @param contextNodeRef context folder for search
//     * @param namePattern name pattern for search
//     * @param includeSubFolders include SubFolders
//     * @return list of files
//     */
//    public List<FileInfo> searchFiles(NodeRef contextNodeRef, String namePattern, boolean includeSubFolders);

//    /**
//     * Search for mailboxes in specified context
//     * 
//     * @param contextNodeRef context folder for search
//     * @param namePattern name pattern for search
//     * @param includeSubFolders include SubFolders
//     * @param viewMode (ARCHIVE, MIXED or VIRTUAL)
//     * @return list of mailboxes that are visible from specified view
//     */
//    public List<FileInfo> searchFolders(NodeRef contextNodeRef, String namePattern, boolean includeSubFolders, ImapViewMode viewMode);

    /**
     * Search for emails in specified folder depend on view mode.
     * 
     * @param contextNodeRef context folder for search
     * @param namePattern name pattern for search
     * @param viewMode (ARCHIVE, MIXED or VIRTUAL)
     * @param includeSubFolders includeSubFolders
     * @return list of emails that context folder contains.
     */
    public List<FileInfo> searchMails(NodeRef contextNodeRef, ImapViewMode viewMode);

    /**
     * Return flags that belong to the specified imap folder.
     * 
     * @param messageInfo imap folder info.
     * @return flags.
     */
    public Flags getFlags(FileInfo messageFileInfo);

    /**
     * Set flags to the specified imapFolder.
     * 
     * @param messageInfo FileInfo of imap Folder.
     * @param flags flags to set.
     * @param value value to set.
     */
    public void setFlags(FileInfo messageFileInfo, Flags flags, boolean value);

    /**
     * Set flag to the specified imapFolder.
     * 
     * @param messageInfo FileInfo of imap Folder
     * @param flag flag to set.
     * @param value value value to set.
     */
    public void setFlag(FileInfo messageFileInfo, Flag flag, boolean value);

    /**
     * @return Default From addreses
     */
    public String getDefaultFromAddress();

    /**
     * @return Path to the folder containing templates, that will be used for generating body of message in VIRTUAL and MIXED views.
     */
    public String getRepositoryTemplatePath();

    /**
     * @return Web application context url (e.g. http://localhost:8080/alfresco)
     */
    public String getWebApplicationContextUrl();

    /**
     * @return Web application context url for share (e.g. http://localhost:8080/share)
     */
    public String getShareApplicationContextUrl();

    /**
     * Returns a template for email body. It is either classpath path or NodeRef.toString().
     * This method trying to find a template on the path in the repository first
     * e.g. {@code "Data Dictionary > IMAP Templates >"}. This path should be set as the property of the "imapHelper" bean.
     * In this case it returns {@code NodeRef.toString()} of the template. If there are no template in the repository it
     * returns a default template on the classpath.
     * 
     * @param Type one of the possible body types text/html and text/plain
     * @return
     */
    public String getDefaultEmailBodyTemplate(EmailBodyFormat type);
    
    /**
     * Determine if provided node belongs to Sites.
     * 
     * @param nodeRef nodeRef
     * @return true if provided node belongs to sites.
     */
    public boolean isNodeInSitesLibrary(NodeRef nodeRef);

}
