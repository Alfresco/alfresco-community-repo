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

import java.io.IOException;
import java.util.List;
import java.util.NavigableMap;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Flags.Flag;
import javax.mail.internet.MimeMessage;

import org.alfresco.repo.imap.AlfrescoImapConst.ImapViewMode;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;

import com.icegreen.greenmail.store.SimpleStoredMessage;

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
            return name().toLowerCase().substring(name().indexOf("_") + 2 + "TEXT".length());
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
     * @param mailboxPattern String name of a mailbox, possible including a wildcard.
     * @param listSubscribed list only subscribed folders?
     * @return Collection of mailboxes matching the pattern.
     */
    public List<AlfrescoImapFolder> listMailboxes(AlfrescoImapUser user, String mailboxPattern, boolean listSubscribed);

    /**
     * Deletes an existing MailBox. Specified mailbox must already exist on this server, and the user must have rights to delete it. This method serves DELETE command of the IMAP
     * protocol.
     * 
     * @param user User making the request.
     * @param mailboxName String name of the target,
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
     * @param oldMailboxName String name of the existing folder
     * @param newMailboxName String target new name
     */
    public void renameMailbox(AlfrescoImapUser user, String oldMailboxName, String newMailboxName);

    /**
     * Returns a reference to a mailbox, either creating a new one or retrieving an existing one.
     * 
     * @param user
     *            User making the request.
     * @param mailboxName
     *            String name of the target.
     * @param mayExist
     * Is the mailbox allowed to exist already? If <code>false</code> and the mailbox already exists, an error will be thrown
     * @param mayCreate
     * If the mailbox does not exist, can one be created? If <code>false</code> then an error is thrown if the folder does not exist 
     * @return a Mailbox reference
     */
    public AlfrescoImapFolder getOrCreateMailbox(AlfrescoImapUser user, String mailboxName, boolean mayExist, boolean mayCreate);

    /**
     * Get the node ref of the user's imap home.   Will create it on demand if it 
     * does not already exist.
     * 
     * @param userName user name
     * @return user IMAP home reference and create it if it doesn't exist.
     */
    public NodeRef getUserImapHomeRef(final String userName);

    /**
     * Subscribes a user to a mailbox. The mailbox must exist locally and the user must have rights to modify it. <p/> This method serves SUBSCRIBE command of the IMAP protocol.
     * 
     * @param user User making the request
     * @param mailbox String representation of a mailbox name.
     */
    public void subscribe(AlfrescoImapUser user, String mailbox);

    /**
     * Unsubscribes from a given mailbox. <p/> This method serves UNSUBSCRIBE command of the IMAP protocol.
     * 
     * @param user User making the request
     * @param mailbox String representation of a mailbox name.
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
    public FolderStatus getFolderStatus(final String userName, final NodeRef contextNodeRef, ImapViewMode viewMode);

    /**
     * Gets a cached MIME message for the given file, complete with message body.
     * 
     * @param messageFileInfo imap file info.
     * @return a message.
     */
    public SimpleStoredMessage getMessage(FileInfo messageFileInfo) throws MessagingException;

    /**
     * Creates a MIME message for the given file
     * 
     * @param messageFileInfo imap file info.
     * @param generateBody Should the message body be generated?
     * @return a message.
     */
    public SimpleStoredMessage createImapMessage(FileInfo messageFileInfo, boolean generateBody) throws MessagingException;

    /**
     * Expunges (deletes) an IMAP message if its flags indicates
     * @param messageFileInfo imap file info.
     */
    public void expungeMessage(FileInfo messageFileInfo);    

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
     * @return Default To addreses
     */
    public String getDefaultToAddress();

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

  
    /**
     * Extract Attachments
     * 
     * @param parentFolder
     * @param messageFile the node ref of the message.
     * @param originalMessage
     * @throws IOException
     * @throws MessagingException
     */
    public NodeRef extractAttachments(
            NodeRef parentFolder,
            NodeRef messageFile,
            MimeMessage originalMessage)
            throws IOException, MessagingException;

    /**
     * Determines whether the IMAP server is enabled.
     * 
     * @return true if enabled
     */
    public boolean getImapServerEnabled();  
    
    static class FolderStatus
    {        
        public final int messageCount;
        public final int recentCount;
        public final int firstUnseen;
        public final int unseenCount;
        public final long uidValidity;
        public final String changeToken;
        public final NavigableMap<Long, FileInfo> search;

        public FolderStatus(int messageCount, int recentCount, int firstUnseen, int unseenCount, long uidValidity,
                String changeToken, NavigableMap<Long, FileInfo> search)
        {
            this.messageCount = messageCount;
            this.recentCount = recentCount;
            this.firstUnseen = firstUnseen;
            this.unseenCount = unseenCount;
            this.uidValidity = uidValidity;
            this.changeToken = changeToken;
            this.search = search;
        }
    }
}
