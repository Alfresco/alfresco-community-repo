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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.imap;

import java.util.List;

import javax.mail.Flags;
import javax.mail.Flags.Flag;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * @author Arseny Kovalchuk
 */
public interface ImapService
{
    public List<AlfrescoImapFolder> listMailboxes(AlfrescoImapUser user, String mailboxPattern);

    public List<AlfrescoImapFolder> listSubscribedMailboxes(AlfrescoImapUser user, String mailboxPattern);

    public AlfrescoImapFolder createMailbox(AlfrescoImapUser user, String mailboxName);

    public void deleteMailbox(AlfrescoImapUser user, String mailboxName);

    public void renameMailbox(AlfrescoImapUser user, String oldMailboxName, String newMailboxName);

    public AlfrescoImapFolder getFolder(AlfrescoImapUser user, String mailboxName);
    
    public void subscribe(AlfrescoImapUser user, String mailbox);
    
    public void unsubscribe(AlfrescoImapUser user, String mailbox);
    
    public List<FileInfo> searchFiles(NodeRef contextNodeRef, String namePattern, boolean includeSubFolders);
    
    public List<FileInfo> searchFolders(NodeRef contextNodeRef, String namePattern, boolean includeSubFolders, String viewMode);
    
    public List<FileInfo> searchMails(NodeRef contextNodeRef, String namePattern, String viewMode, boolean includeSubFolders);
    
    public Flags getFlags(FileInfo messageFileInfo);
    
    public void setFlags(FileInfo messageFileInfo, Flags flags, boolean value);
    
    public void setFlag(FileInfo messageFileInfo, Flag flag, boolean value);
    
    public String getDefaultFromAddress();
    
    public String getRepositoryTemplatePath();
    
    public String getWebApplicationContextUrl();

}
