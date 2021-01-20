/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
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
import org.alfresco.service.cmr.repository.ChildAssociationRef;
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
     * Returns a reference to a mailbox, either creating a new one or retrieving an existing one.<br />
     * <br />
     * <code>mailbox</code> parameter may specify absolute or relative path to a folder. Absolute path uniquely identifies some directory, whereas relative path implies that root
     * folder should be <code>IMAP home</code> directory for the specified <code>user</code> rather than <code>IMAP root</code> (i.e. <code>IMAP mount point</code>). Mailbox will
     * be found or created (<code>mayCreate=true</code>, <code>mayExist=false</code> or ) in <code>user</code>'s <code>IMAP home</code> directory if relative path is specified.<br />
     * <br />
     * <code>mayExist</code> and mayCreate parameters' combinations and results:
     * <ul>
     * <li><code>mayCreate=false</code>, <code>mayExist=true</code> - mailbox is found and not created if it doesn't exist. Error is thrown if mailbox doesn't not exist;</li>
     * <li><code>mayCreate=true</code>, <code>mayExist=true</code> - mailbox is created if it doesn't exist or it is just found in other case. No error is thrown;</li>
     * <li><code>mayCreate=true</code>, <code>mayExist=false</code> - mailbox is created if it doesn't exist. Error is thrown if it is already created;</li>
     * <li><code>mayCreate=false</code>, <code>mayExist=false</code> - error is thrown that mailbox cannot be created if doesn't exist. Error is thrown that mailbox should not
     * exist in other case.<br />
     * <b>It's a very shady combination!</b></li>
     * </ul>
     * 
     * @param user User making the request.
     * @param mailboxName String name of the target.
     * @param mayExist Is the mailbox allowed to exist already? If <code>false</code> and the mailbox already exists, an error will be thrown
     * @param mayCreate If the mailbox does not exist, can one be created? If <code>false</code> then an error is thrown if the folder does not exist
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
     * @param userName user name
     * @param contextNodeRef context NodeRef
     * @param viewMode (ARCHIVE, MIXED or VIRTUAL)
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
     * @param messageFileInfo imap folder info.
     * @return flags.
     */
    public Flags getFlags(FileInfo messageFileInfo);

    /**
     * Set flags to the specified imapFolder.
     * 
     * @param messageFileInfo FileInfo of imap Folder.
     * @param flags flags to set.
     * @param value value to set.
     */
    public void setFlags(FileInfo messageFileInfo, Flags flags, boolean value);

    /**
     * Set flag to the specified imapFolder.
     * 
     * @param messageFileInfo FileInfo of imap Folder
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
     * @param type one of the possible body types text/html and text/plain
     * @return String
     */
    public String getDefaultEmailBodyTemplate(EmailBodyFormat type);
    
    /**
     * Get the node's site container if it belongs to Sites.
     * 
     * @param nodeRef nodeRef
     * @return nodeRef of the node's site container or null if provided node does not belong to sites.
     */
    public NodeRef getNodeSiteContainer(NodeRef nodeRef);

    /**
     * Get the web URL for the document's parent folder
     *
     * @param siteContainerNodeRef or null if the document is not from site
     * @return url for the content folder
     */
    public String getContentFolderUrl(NodeRef siteContainerNodeRef);

    /**
     * Determines whether the IMAP server is enabled.
     * 
     * @return true if enabled
     */
    public boolean getImapServerEnabled();  
    
    /**
     * Extract attachments from message.
     * 
     * @param messageRef nodeRef that represents message in Alfresco.
     * @param originalMessage original message in eml format.
     * @throws IOException
     * @throws MessagingException
     */
    public void extractAttachments(NodeRef messageRef, MimeMessage originalMessage) throws IOException, MessagingException;
    
    public String generateUniqueFilename(NodeRef destFolderNodeRef, String fileName);
    
    public void persistMessageHeaders(NodeRef nodeRef, MimeMessage message);

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

    /**
     * @param nodeRef NodeRef
     * @return path for node relatively to site root
     */
    public String getPathFromSites(NodeRef nodeRef);

    /**
     * @param assocRef an association between the node and it's parent
     * @return path for node relatively to repository
     */
    public String getPathFromRepo(ChildAssociationRef assocRef);
}
