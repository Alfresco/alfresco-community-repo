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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Flags.Flag;
import javax.mail.internet.MimeMessage;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.imap.AlfrescoImapConst.ImapViewMode;
import org.alfresco.repo.imap.ImapService.FolderStatus;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.util.GUID;
import org.alfresco.util.Utf7;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.icegreen.greenmail.foedus.util.MsgRangeFilter;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.FolderListener;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.store.MessageFlags;
import com.icegreen.greenmail.store.SimpleStoredMessage;

/**
 * Implementation of greenmail MailFolder. It represents an Alfresco content folder and handles
 * appendMessage, copyMessage, expunge (delete), getMessages, getMessage and so requests.
 * 
 * @author Mike Shavnev
 * @author David Ward
 */
public class AlfrescoImapFolder extends AbstractImapFolder implements Serializable
{
    private static final long serialVersionUID = -7223111284066976111L;

    private static Log logger = LogFactory.getLog(AlfrescoImapFolder.class);

    /**
     * Reference to the {@link FileInfo} object representing the folder.
     */
    private final FileInfo folderInfo;

    /**
     * Name of the folder.
     */
    private final String folderName;

    private final String folderPath;
    
    private final String userName;

    /**
     * Defines view mode.
     */
    private final ImapViewMode viewMode;

    /**
     * Reference to the {@link ImapService} object.
     */
    private final ImapService imapService;
    
    /**
     * Defines whether the folder is selectable or not.
     */
    private final boolean selectable;

    private final boolean extractAttachmentsEnabled;
    
    /**
     * Cached Folder status information, validated against a change token.
     */
    private FolderStatus folderStatus;
    
    private static final Flags PERMANENT_FLAGS = new Flags();

    static
    {
        PERMANENT_FLAGS.add(Flags.Flag.ANSWERED);
        PERMANENT_FLAGS.add(Flags.Flag.DELETED);
        PERMANENT_FLAGS.add(Flags.Flag.DRAFT);
        PERMANENT_FLAGS.add(Flags.Flag.FLAGGED);
        PERMANENT_FLAGS.add(Flags.Flag.SEEN);
    }
    
    public boolean isExtractAttachmentsEnabled() 
    {
        return extractAttachmentsEnabled;
    }

    /**
     * Protected constructor for the hierarchy delimiter
     */
    AlfrescoImapFolder(String userName, ServiceRegistry serviceRegistry)
    {
        this(null, userName, "", "", null, serviceRegistry, false, false);
    }
        

    /**
     * Constructs {@link AlfrescoImapFolder} object.
     * 
     * @param qualifiedMailboxName - name of the mailbox (e.g. "admin" for admin user).
     * @param folderInfo - reference to the {@link FileInfo} object representing the folder.
     * @param folderName - name of the folder.
     * @param viewMode - defines view mode. Can be one of the following: {@link AlfrescoImapConst#MODE_ARCHIVE} or {@link AlfrescoImapConst#MODE_VIRTUAL}.
     * @param rootNodeRef - reference to the root node of the store where folder is placed.
     * @param mountPointName - name of the mount point.
     */
    public AlfrescoImapFolder(
            FileInfo folderInfo,
            String userName,
            String folderName,
            String folderPath,
            ImapViewMode viewMode,
            boolean extractAttachmentsEnabled,
            ServiceRegistry serviceRegistry)
    {
        this(folderInfo, userName, folderName, folderPath, viewMode, serviceRegistry, null, extractAttachmentsEnabled);
    }

    /**
     * Constructs {@link AlfrescoImapFolder} object.
     * 
     * @param qualifiedMailboxName - name of the mailbox (e.g. "admin" for admin user).
     * @param folderInfo - reference to the {@link FileInfo} object representing the folder.
     * @param folderName - name of the folder.
     * @param viewMode - defines view mode. Can be one of the following: {@link AlfrescoImapConst#MODE_ARCHIVE} or {@link AlfrescoImapConst#MODE_VIRTUAL}.
     * @param rootNodeRef - reference to the root node of the store where folder is placed.
     * @param mountPointName - name of the mount point.
     * @param imapService - reference to the {@link ImapHelper} object.
     * @param selectable - defines whether the folder is selectable or not.
     */
    public AlfrescoImapFolder(
            FileInfo folderInfo,
            String userName,
            String folderName,
            String folderPath,
            ImapViewMode viewMode,
            ServiceRegistry serviceRegistry,
            Boolean selectable,
            boolean extractAttachmentsEnabled)
    {
        super(serviceRegistry);
        this.folderInfo = folderInfo;
        this.userName = userName;
        this.folderName = folderName != null ? folderName : (folderInfo != null ? folderInfo.getName() : null);
        this.folderPath = folderPath;
        this.viewMode = viewMode != null ? viewMode : ImapViewMode.ARCHIVE;
        this.extractAttachmentsEnabled = extractAttachmentsEnabled;
        this.imapService = serviceRegistry.getImapService();
        
        // MailFolder object can be null if it is used to obtain hierarchy delimiter by LIST command:
        // Example:
        // C: 2 list "" ""
        // S: * LIST () "." ""
        // S: 2 OK LIST completed.
        if (folderInfo != null)
        {
            if (selectable == null)
            {
                // isSelectable();
                Boolean storedSelectable = !serviceRegistry.getNodeService().hasAspect(folderInfo.getNodeRef(), ImapModel.ASPECT_IMAP_FOLDER_NONSELECTABLE);
                if (storedSelectable == null)
                {
                    this.selectable = true;
                }
                else
                {
                    this.selectable = storedSelectable;
                }
            }
            else
            {
                this.selectable = selectable;
            }
        }
        else
        {
            this.selectable = false;
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.icegreen.greenmail.store.MailFolder#getFullName()
     */
    @Override
    public String getFullName()
    {
        return Utf7.encode(AlfrescoImapConst.USER_NAMESPACE + AlfrescoImapConst.HIERARCHY_DELIMITER + this.userName
                + AlfrescoImapConst.HIERARCHY_DELIMITER + getFolderPath(), Utf7.UTF7_MODIFIED);
    }

    /* (non-Javadoc)
     * @see com.icegreen.greenmail.store.MailFolder#getName()
     */
    @Override
    public String getName()
    {
        return this.folderName;
    }

    /* (non-Javadoc)
     * @see com.icegreen.greenmail.store.MailFolder#isSelectable()
     */
    @Override
    public boolean isSelectable()
    {
        return this.selectable;
    }

    /**
     * Returns the contents of this folder.
     * 
     * @return A sorted map of UIDs to FileInfo objects.
     */
    private NavigableMap<Long, FileInfo> searchMails()
    {
        return getFolderStatus().search;
    }

    /**
     * Invalidates the current cached state
     * 
     * @return <code>true</code> if this instance is still valid for reuse
     */
    public boolean reset()
    {
        this.folderStatus = null;
        return new CommandCallback<Boolean>()
        {
            public Boolean command() throws Throwable
            {
                return serviceRegistry.getNodeService().exists(folderInfo.getNodeRef());
            }
        }.run(true);
    }

    protected FolderStatus getFolderStatus()
    {
        if (this.folderStatus == null)
        {
            CommandCallback<FolderStatus> command = new CommandCallback<FolderStatus>()
            {
                public FolderStatus command() throws Throwable
                {
                    return imapService.getFolderStatus(userName, folderInfo.getNodeRef(), viewMode);
                }
            };
            this.folderStatus = command.run();                         
        }
        return this.folderStatus;
    }
    
    /**
     * Appends message to the folder.
     * 
     * @param message - message.
     * @param flags - message flags.
     * @param internalDate - not used. Current date used instead.
     */
    @Override
    protected long appendMessageInternal(
            MimeMessage message,
            Flags flags,
            Date internalDate)
            throws FileExistsException, FileNotFoundException, IOException, MessagingException 
    {
        long uid = createMimeMessageInFolder(this.folderInfo, message, flags);
        // Invalidate current folder status
        this.folderStatus = null;
        return uid;
    }

    /**
     * Copies message with the given UID to the specified {@link MailFolder}.
     * 
     * @param uid - UID of the message
     * @param toFolder - reference to the destination folder.
     * @throws MessagingException 
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws FileExistsException 
     */
    @Override
    protected void copyMessageInternal(
            long uid, MailFolder toFolder)
            throws MessagingException, FileExistsException, FileNotFoundException, IOException 
    {
        AlfrescoImapFolder toImapMailFolder = (AlfrescoImapFolder) toFolder;

        NodeRef destFolderNodeRef = toImapMailFolder.getFolderInfo().getNodeRef();

        FileInfo sourceMessageFileInfo = searchMails().get(uid);

        if (serviceRegistry.getNodeService().hasAspect(sourceMessageFileInfo.getNodeRef(), ImapModel.ASPECT_IMAP_CONTENT))
        {
                //Generate body of message
            MimeMessage newMessage = new ImapModelMessage(sourceMessageFileInfo, serviceRegistry, true);
            toImapMailFolder.appendMessageInternal(newMessage, imapService.getFlags(sourceMessageFileInfo), new Date());
        }
        else
        {
            serviceRegistry.getFileFolderService().copy(sourceMessageFileInfo.getNodeRef(), destFolderNodeRef, null);
        }
    }

    /**
     * Marks all messages in the folder as deleted using {@link Flags.Flag#DELETED} flag.
     */
    @Override
    public void deleteAllMessagesInternal() throws FolderException
    {
        if (isReadOnly())
        {
            throw new FolderException("Can't delete all - Permission denied");
        }
        
        for (Map.Entry<Long, FileInfo> entry : searchMails().entrySet())
        {
            imapService.setFlag(entry.getValue(), Flags.Flag.DELETED, true);
            // comment out to physically remove content.
            // fileFolderService.delete(fileInfo.getNodeRef());
        }
    }

    /**
     * Deletes messages marked with {@link Flags.Flag#DELETED}. Note that this message deletes all messages with this flag.
     */
    @Override
    protected void expungeInternal() throws FolderException
    {
        if (isReadOnly())
        {
            throw new FolderException("Can't expunge - Permission denied");
        }

        for (Map.Entry<Long, FileInfo> entry : searchMails().entrySet())
        {
            imapService.expungeMessage(entry.getValue());
        }
    }

    /**
     * Returns the MSN number of the first unseen message.
     * 
     * @return MSN number of the first unseen message.
     */
    @Override
    public int getFirstUnseen()
    {
        return getFolderStatus().firstUnseen;
    }

    /**
     * Returns message by its UID.
     * 
     * @param uid - UID of the message.
     * @return message.
     * @throws MessagingException 
     */
    @Override
    protected SimpleStoredMessage getMessageInternal(long uid) throws MessagingException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("[getMessageInternal] " + this);
        }
        FileInfo mesInfo = searchMails().get(uid); 
        if (mesInfo == null)
        {
            return null;
        }
        return imapService.getMessage(mesInfo);
    }

    /**
     * Returns count of the messages in the folder.
     * 
     * @return Count of the messages.
     */
    @Override
    public int getMessageCount()
    {
        return getFolderStatus().messageCount;
    }

    /**
     * Returns UIDs of all messages in the folder.
     * 
     * @return UIDS of the messages.
     */
    @Override
    public long[] getMessageUids()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("[getMessageUidsInternal] " + this);
        }
        
        Collection<Long> uidSet = searchMails().keySet();
        long[] uids = new long[uidSet.size()];
        int i = 0;
        for (Long uid : uidSet)
        {
            uids[i++] = uid;
        }
        return uids;
    }

    /**
     * Returns list of all messages in the folder.
     * 
     * @return list of {@link SimpleStoredMessage} objects.
     */
    @Override
    protected List<SimpleStoredMessage> getMessagesInternal()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("[getMessagesInternal] " + this);
        }
        return convertToMessages(searchMails().values());
    }

    private List<SimpleStoredMessage> convertToMessages(Collection<FileInfo> fileInfos)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("[convertToMessages] " + this);
        }
        if (fileInfos == null || fileInfos.size() == 0)
        {
            logger.debug("[convertToMessages] - fileInfos is empty or null");
            return Collections.emptyList();
        }
        List<SimpleStoredMessage> result = new LinkedList<SimpleStoredMessage>();
        for (FileInfo fileInfo : fileInfos)
        {
            try
            {
                result.add(imapService.createImapMessage(fileInfo, false));
                if (logger.isDebugEnabled())
                {
                    logger.debug("[convertToMessages] Message added: " + fileInfo.getName());
                }
            }
            catch (MessagingException e)
            {
                logger.warn("[convertToMessages] Invalid message! File name:" + fileInfo.getName(), e);
            }
        }
        return result;
    }

    /**
     * Returns list of messages by filter.
     * 
     * @param msgRangeFilter - {@link MsgRangeFilter} object representing filter.
     * @return list of filtered messages.
     */
    @Override
    protected List<SimpleStoredMessage> getMessagesInternal(MsgRangeFilter msgRangeFilter)
    {
        throw new UnsupportedOperationException("IMAP implementation doesn't support POP3 requests");
    }

    /**
     * Returns message sequence number in the folder by its UID.
     * 
     * @param uid - message UID.
     * @return message sequence number.
     * @throws FolderException if no message with given UID.
     */
    @Override
    public int getMsn(long uid) throws FolderException
    {
        NavigableMap<Long, FileInfo> messages = searchMails();
        if (!messages.containsKey(uid))
        {
            throw new FolderException("No such message.");            
        }
        return messages.headMap(uid, true).size();
    }

    /**
     * Returns the list of messages that have no {@link Flags.Flag#DELETED} flag set for current user.
     * 
     * @return the list of non-deleted messages.
     */
    @Override
    protected List<SimpleStoredMessage> getNonDeletedMessagesInternal()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("[getNonDeletedMessagesInternal] " + this);
        }
        List<SimpleStoredMessage> result = new ArrayList<SimpleStoredMessage>();

        Collection<SimpleStoredMessage> values = getMessagesInternal();
        for (SimpleStoredMessage message : values)
        {
            if (!getFlags(message).contains(Flags.Flag.DELETED))
            {
                result.add(message);
            }

        }
        if (logger.isDebugEnabled() && folderInfo != null)
        {
            logger.debug(folderInfo.getName() + " - Non deleted messages count:" + result.size());
        }
        return result;
    }

    /**
     * Returns permanent flags.
     * 
     * @return {@link Flags} object containing flags.
     */
    @Override
    public Flags getPermanentFlags()
    {
        return PERMANENT_FLAGS;
    }

    /**
     * Returns count of messages with {@link Flags.Flag#RECENT} flag.
     * If {@code reset} parameter is {@code true} - removes {@link Flags.Flag#RECENT} flag from
     * the message for current user.
     * 
     * @param reset - if true the {@link Flags.Flag#RECENT} will be deleted for current user if exists.
     * @return returns count of recent messages.
     */
    @Override
    public int getRecentCount(boolean reset)
    {
        int recent = getFolderStatus().recentCount;
        if (reset && recent > 0)
        {
            CommandCallback<Void> command = new CommandCallback<Void>()
            {
                public Void command() throws Throwable
                {
                    for (FileInfo fileInfo : folderStatus.search.values())
                    {
                        Flags flags = imapService.getFlags(fileInfo);
                        if (flags.contains(Flags.Flag.RECENT))
                        {
                            imapService.setFlag(fileInfo, Flags.Flag.RECENT, false);
                        }
                    }
                    return null;
                }
            };
            command.run();
        }
        return recent;        
    }

    /**
     * Returns UIDNEXT value of the folder.
     * 
     * @return UIDNEXT value.
     */
    @Override
    public long getUidNext()
    {
        NavigableMap<Long, FileInfo> search = getFolderStatus().search; 
        return search.isEmpty() ? 1 : search.lastKey() + 1;
    }
    
    /**
     * Returns UIDVALIDITY value of the folder.
     * 
     * @return UIDVALIDITY value.
     */
    @Override
    public long getUidValidity()
    {
        return getFolderStatus().uidValidity;
    }

    /**
     * Returns count of the messages with {@link Flags.Flag#SEEN} in the folder for the current user.
     * 
     * @return Count of the unseen messages for current user.
     */
    @Override
    public int getUnseenCount()
    {
        return getFolderStatus().unseenCount;
    }

    /**
     * Replaces flags for the message with the given UID. If {@code addUid} is set to {@code true}
     * {@link FolderListener} objects defined for this folder will be notified.
     * {@code silentListener} can be provided - this listener wouldn't be notified.
     * 
     * @param flags - new flags.
     * @param uid - message UID.
     * @param silentListener - listener that shouldn't be notified.
     * @param addUid - defines whether or not listeners be notified.
     * @throws FolderException 
     * @throws MessagingException 
     */
    @Override
    protected void replaceFlagsInternal(
            Flags flags,
            long uid,
            FolderListener silentListener,
            boolean addUid)
            throws FolderException, MessagingException 
    {
        int msn = getMsn(uid);
        FileInfo fileInfo = searchMails().get(uid);
        imapService.setFlags(fileInfo, MessageFlags.ALL_FLAGS, false);
        imapService.setFlags(fileInfo, flags, true);
        
        Long uidNotification = addUid ? uid : null;
        notifyFlagUpdate(msn, flags, uidNotification, silentListener);
    }

    /**
     * Sets flags for the message with the given UID. If {@code addUid} is set to {@code true}
     * {@link FolderListener} objects defined for this folder will be notified.
     * {@code silentListener} can be provided - this listener wouldn't be notified.
     * 
     * @param flags - new flags.
     * @param value - flags value.
     * @param uid - message UID.
     * @param silentListener - listener that shouldn't be notified.
     * @param addUid - defines whether or not listeners be notified.
     * @throws MessagingException 
     * @throws FolderException 
     */
    @Override
    protected void setFlagsInternal(
            Flags flags,
            boolean value,
            long uid,
            FolderListener silentListener,
            boolean addUid)
            throws MessagingException, FolderException 
    {
        int msn = getMsn(uid);
        FileInfo fileInfo = searchMails().get(uid);
        imapService.setFlags(fileInfo, flags, value);
        
        Long uidNotification = null;
        if (addUid)
        {
            uidNotification = new Long(uid);
        }
        notifyFlagUpdate(msn, flags, uidNotification, silentListener);

    }

    private Flags getFlags(SimpleStoredMessage mess)
    {
        return ((AbstractMimeMessage) mess.getMimeMessage()).getFlags();
    }

    // ----------------------Getters and Setters----------------------------

    public String getFolderPath()
    {
        return this.folderPath;
    }

    public FileInfo getFolderInfo()
    {
        return folderInfo;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.imap.AbstractImapFolder#isMarkedInternal()
     */
    @Override
    public boolean isMarked()
    {
        FolderStatus folderStatus = getFolderStatus();
        return folderStatus.recentCount > 0 || folderStatus.unseenCount > 0;
    }

    /**
     * Whether the folder is read-only for user.
     * 
     * @return {@code boolean}
     */
    @Override
    protected boolean isReadOnly()
    {
        AccessStatus status = serviceRegistry.getPublicServiceAccessService().hasAccess(ServiceRegistry.NODE_SERVICE.getLocalName(), "createNode", folderInfo.getNodeRef(), null, null, null);
        //serviceRegistry.getPermissionService().hasPermission(folderInfo.getNodeRef(), PermissionService.WRITE);
        return  status == AccessStatus.DENIED;
    }

    public ImapViewMode getViewMode()
    {
        return viewMode;
    }
    
    /**
     *  Creates the EML message in the specified folder.
     *  
     *  @param folderFileInfo The folder to create message in.
     *  @param message The original MimeMessage.
     *  @return ID of the new message created 
     * @throws FileNotFoundException 
     * @throws FileExistsException 
     * @throws MessagingException 
     * @throws IOException 
     */
    private long createMimeMessageInFolder(
            FileInfo folderFileInfo,
            MimeMessage message,
            Flags flags)
            throws FileExistsException, FileNotFoundException, IOException, MessagingException 
    {
        String name = AlfrescoImapConst.MESSAGE_PREFIX + GUID.generate();
        FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
        FileInfo messageFile = fileFolderService.create(folderFileInfo.getNodeRef(), name, ContentModel.TYPE_CONTENT);
        final long newMessageUid = (Long) messageFile.getProperties().get(ContentModel.PROP_NODE_DBID);
        name = AlfrescoImapConst.MESSAGE_PREFIX  + newMessageUid + AlfrescoImapConst.EML_EXTENSION;
        fileFolderService.rename(messageFile.getNodeRef(), name);
        Flags newFlags = new Flags(flags);
        newFlags.add(Flag.RECENT);
        imapService.setFlags(messageFile, newFlags, true);
        
        if (extractAttachmentsEnabled)
        {
            imapService.extractAttachments(folderFileInfo.getNodeRef(), messageFile.getNodeRef(), message);
        }
        // Force persistence of the message to the repository
        new IncomingImapMessage(messageFile, serviceRegistry, message);
        return newMessageUid;        
    }
}
