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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.imap.AlfrescoImapConst.ImapViewMode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.util.GUID;
import org.alfresco.util.Utf7;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.FileCopyUtils;

import com.icegreen.greenmail.foedus.util.MsgRangeFilter;
import com.icegreen.greenmail.imap.ImapConstants;
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
 */
public class AlfrescoImapFolder extends AbstractImapFolder
{
    private final static long YEAR_2005 = 1101765600000L;

    private static Log logger = LogFactory.getLog(AlfrescoImapFolder.class);

    /**
     * Reference to the {@link FileInfo} object representing the folder.
     */
    private FileInfo folderInfo;

    /**
     * Reference to the root node of the store where folder is placed.
     */
    private NodeRef rootNodeRef;

    /**
     * Name of the mailbox (e.g. "admin" for admin user).
     */
    private String qualifiedMailboxName;

    /**
     * Name of the folder.
     */
    private String folderName;

    /**
     * Defines view mode.
     */
    private ImapViewMode viewMode;

    /**
     * Name of the mount point.
     */
    private String mountPointName;

    /**
     * Reference to the {@link ImapService} object.
     */
    private ImapService imapService;

    /**
     * Defines whether the folder is selectable or not.
     */
    private boolean selectable;

    /**
     * Defines whether the folder is read-only for user or not.
     */
    private Boolean readOnly;
    
    private boolean extractAttachmentsEnabled;
    
    private Map<Long, SimpleStoredMessage> messages = new TreeMap<Long, SimpleStoredMessage>();
    private Map<Long, Integer> msnCache = new HashMap<Long, Integer>();
   
    private Map<Long, CacheItem> messagesCache = Collections.synchronizedMap(new MaxSizeMap<Long, CacheItem>(10, CACHE_SIZE));
    
    /** 
     * Map that ejects the last recently used element(s) to keep the size to a 
     * specified maximum
     * 
     * @param <K> Key
     * @param <V> Value
     */
    private class MaxSizeMap<K,V> extends LinkedHashMap<K,V> 
    {
        private int maxSize;

        public MaxSizeMap(int initialSize, int maxSize) 
        {
            super(initialSize, 0.75f, true);
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K,V> eldest) 
        {
            boolean remove = super.size() > this.maxSize;
            return remove;
        }
    }
    
    private final static int CACHE_SIZE = 20;

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

    /*package*/ AlfrescoImapFolder(String qualifiedMailboxName, ServiceRegistry serviceRegistry)
    {
        this(qualifiedMailboxName, null, null, null, null, null, false, serviceRegistry);
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
            String qualifiedMailboxName,
            FileInfo folderInfo,
            String folderName,
            ImapViewMode viewMode,
            NodeRef rootNodeRef,
            String mountPointName,
            boolean extractAttachmentsEnabled,
            ServiceRegistry serviceRegistry)
    {
        this(qualifiedMailboxName, folderInfo, folderName, viewMode, rootNodeRef, mountPointName, serviceRegistry, null, extractAttachmentsEnabled);
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
            String qualifiedMailboxName,
            FileInfo folderInfo,
            String folderName,
            ImapViewMode viewMode,
            NodeRef rootNodeRef,
            String mountPointName,
            ServiceRegistry serviceRegistry,
            Boolean selectable,
            boolean extractAttachmentsEnabled)
    {
        super(serviceRegistry);
        this.qualifiedMailboxName = qualifiedMailboxName;
        this.folderInfo = folderInfo;
        this.rootNodeRef = rootNodeRef;
        this.folderName = folderName != null ? folderName : (folderInfo != null ? folderInfo.getName() : null);
        this.viewMode = viewMode != null ? viewMode : ImapViewMode.ARCHIVE;
        this.mountPointName = mountPointName;
        this.extractAttachmentsEnabled = extractAttachmentsEnabled;

        if (serviceRegistry != null)
        {
            this.imapService = serviceRegistry.getImapService();
        }

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
                    setSelectable(true);
                }
                else
                {
                    setSelectable(storedSelectable);
                }
            }
            else
            {
                setSelectable(selectable);
            }
            
            AccessStatus status = serviceRegistry.getPublicServiceAccessService().hasAccess(ServiceRegistry.NODE_SERVICE.getLocalName(), "createNode", folderInfo.getNodeRef(), null, null, null);
            //serviceRegistry.getPermissionService().hasPermission(folderInfo.getNodeRef(), PermissionService.WRITE);
            if (status == AccessStatus.DENIED)
            {
                readOnly = true;
            }
            else
            {
                readOnly = false;
            }
            
        }
        else
        {
            setSelectable(true);
        }
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
        AbstractMimeMessage internalMessage = createMimeMessageInFolder(this.folderInfo, message);
        long newMessageUid = (Long) internalMessage.getMessageInfo().getProperties().get(ContentModel.PROP_NODE_DBID);
        SimpleStoredMessage storedMessage = new SimpleStoredMessage(internalMessage, new Date(), newMessageUid);
        messages.put(newMessageUid, storedMessage);
        
        // Saving message sequence number to cache
        msnCache.put(newMessageUid, messages.size());

        return newMessageUid;
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

        SimpleStoredMessage message = messages.get(uid);
        FileInfo sourceMessageFileInfo = ((AbstractMimeMessage) message.getMimeMessage()).getMessageInfo();

        if (serviceRegistry.getNodeService().hasAspect(sourceMessageFileInfo.getNodeRef(), ImapModel.ASPECT_IMAP_CONTENT))
        {
                //Generate body of message
            MimeMessage newMessage = new ImapModelMessage(sourceMessageFileInfo, serviceRegistry, true);
            toImapMailFolder.appendMessageInternal(newMessage, message.getFlags(), new Date());
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
        if (this.readOnly)
        {
            throw new FolderException("Can't delete all - Permission denied");
        }
        
        for (SimpleStoredMessage mess : messages.values())
        {
            AbstractMimeMessage message = (AbstractMimeMessage) mess.getMimeMessage();
            FileInfo fileInfo = message.getMessageInfo();
            imapService.setFlag(fileInfo, Flags.Flag.DELETED, true);
            // comment out to physically remove content.
            // fileFolderService.delete(fileInfo.getNodeRef());
            messages.remove(mess.getUid());
            msnCache.remove(mess.getUid());
        }
    }

    /**
     * Deletes messages marked with {@link Flags.Flag#DELETED}. Note that this message deletes all messages with this flag.
     */
    @Override
    protected void expungeInternal() throws FolderException
    {
        if (this.readOnly)
        {
            throw new FolderException("Can't expunge - Permission denied");
        }

        Collection<SimpleStoredMessage> listMess = messages.values();
        for (SimpleStoredMessage mess : listMess)
        {

            Flags flags = getFlags(mess);
            if (flags.contains(Flags.Flag.DELETED))
            {
                NodeRef nodeRef = ((AbstractMimeMessage) mess.getMimeMessage()).getMessageInfo().getNodeRef();
                serviceRegistry.getFileFolderService().delete(nodeRef);
            }
        }
    }

    /**
     * Returns the number of the first unseen message.
     * 
     * @return Number of the first unseen message.
     */
    @Override
    protected int getFirstUnseenInternal()
    {
        return 0;
    }

    /**
     * Returns full name of the folder with namespace and full path delimited with the hierarchy delimiter
     * (see {@link AlfrescoImapConst#HIERARCHY_DELIMITER}) <p/>
     * E.g.: <br/>
     * #mail.admin."Repository_archive.Data Dictionary.Space Templates.Software Engineering Project"<br/>
     * This is required by GreenMail implementation.
     * 
     * @throws FileNotFoundException
     */
    @Override
    protected String getFullNameInternal() throws FileNotFoundException
    {
        // If MailFolder object is used to obtain hierarchy delimiter by LIST command:
        // Example:
        // C: 2 list "" ""
        // S: * LIST () "." ""
        // S: 2 OK LIST completed.

        if (rootNodeRef == null)
        {
            return "";
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("[getFullNameInternal] entry");
        }

        StringBuilder fullName = new StringBuilder();
        List<FileInfo> pathList;
        pathList = serviceRegistry.getFileFolderService().getNamePath(rootNodeRef, folderInfo.getNodeRef());
        fullName.append(ImapConstants.USER_NAMESPACE).append(AlfrescoImapConst.HIERARCHY_DELIMITER).append(qualifiedMailboxName);

        boolean isFirst = true;
        for (FileInfo path : pathList)
        {
            fullName.append(AlfrescoImapConst.HIERARCHY_DELIMITER);
            if (isFirst)
            {
                fullName.append("\"");
                isFirst = false;
                if (mountPointName != null)
                {
                    fullName.append(mountPointName);
                }
                else
                {
                    fullName.append(path.getName());
                }
            }
            else
            {
                fullName.append(path.getName());
            }
        }
        fullName.append("\"");
        if (logger.isDebugEnabled())
        {
            logger.debug("fullName: " + fullName);
        }
        return Utf7.encode(fullName.toString(), Utf7.UTF7_MODIFIED);
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
        AbstractMimeMessage mes = (AbstractMimeMessage) messages.get(uid).getMimeMessage();
        FileInfo mesInfo = mes.getMessageInfo();

        Date modified = (Date) serviceRegistry.getNodeService().getProperty(mesInfo.getNodeRef(), ContentModel.PROP_MODIFIED);
        if(modified != null)
        {
            CacheItem cached =  messagesCache.get(uid);
            if (cached != null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("retrieved message from cache uid: " + uid);
                }
                if (cached.getModified().equals(modified))
                {
                    return cached.getMessage();
                }
            }
            SimpleStoredMessage message = createImapMessage(mesInfo, uid, true);
            messagesCache.put(uid, new CacheItem(modified, message));
            
            if (logger.isDebugEnabled())
            {
                logger.debug("caching message uid: " + uid + " cacheSize: " + messagesCache.size());
            }
            
            return message;
        }
        else
        {
            SimpleStoredMessage message = createImapMessage(mesInfo, uid, true);
            return message;
        }
    }

    /**
     * Returns count of the messages in the folder.
     * 
     * @return Count of the messages.
     */
    @Override
    protected int getMessageCountInternal()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("[getMessageCountInternal] entry");
        }
        
        if (messages.size() == 0 && folderInfo != null)
        {
            List<FileInfo> fileInfos = imapService.searchMails(folderInfo.getNodeRef(), viewMode);
            convertToMessages(fileInfos);
        }
        if (logger.isDebugEnabled() && folderInfo != null)
        {
            logger.debug(folderInfo.getName() + " - Messages count:" + messages.size());
        }
        return messages.size();
    }

    /**
     * Returns UIDs of all messages in the folder.
     * 
     * @return UIDS of the messages.
     */
    @Override
    protected long[] getMessageUidsInternal()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("[getMessageUidsInternal] entry");
        }
        if (messages == null || messages.size() == 0 && folderInfo != null)
        {
            List<FileInfo> fileInfos = imapService.searchMails(folderInfo.getNodeRef(), viewMode);
            convertToMessages(fileInfos);
        }
        int len = messages.size();
        long[] uids = new long[len];
        Set<Long> keys = messages.keySet();
        int i = 0;
        for (Long key : keys)
        {
            uids[i++] = key;
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
            logger.debug("[getMessagesInternal] entry");
        }
        List<FileInfo> fileInfos = imapService.searchMails(folderInfo.getNodeRef(), viewMode);
        return convertToMessages(fileInfos);
    }

    private List<SimpleStoredMessage> convertToMessages(List<FileInfo> fileInfos)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("[convertToMessages] entry");
        }
        if (fileInfos == null || fileInfos.size() == 0)
        {
            logger.debug("[convertToMessages] - fileInfos is empty or null");
            return Collections.emptyList();
        }
        if (fileInfos.size() != messages.size())
        {
            for (FileInfo fileInfo : fileInfos)
            {
                try
                {
                    Long key = getMessageUid(fileInfo);
                    SimpleStoredMessage message = createImapMessage(fileInfo, key, false);
                    messages.put(key, message);

                    // Saving message sequence number to cache
                    msnCache.put(key, messages.size());

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
        }
        return new LinkedList<SimpleStoredMessage>(messages.values());
    }

    protected SimpleStoredMessage createImapMessage(FileInfo fileInfo, Long key, boolean generateBody) throws MessagingException
    {
        if (serviceRegistry.getNodeService().hasAspect(fileInfo.getNodeRef(), ImapModel.ASPECT_IMAP_CONTENT))
        {
            return new SimpleStoredMessage(new ImapModelMessage(fileInfo, serviceRegistry, generateBody), new Date(), key);
        }
        else
        {
            return new SimpleStoredMessage(new ContentModelMessage(fileInfo, serviceRegistry, generateBody), new Date(), key);
        }
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
        if (logger.isDebugEnabled())
        {
            logger.debug("[getMessagesInternal] entry");
        }
        if (messages == null || messages.size() == 0)
        {
            List<FileInfo> fileInfos = imapService.searchMails(folderInfo.getNodeRef(), viewMode);
            convertToMessages(fileInfos);
        }
        List<SimpleStoredMessage> ret = new ArrayList<SimpleStoredMessage>();
        for (int i = 0; i < messages.size(); i++)
        {
            if (msgRangeFilter.includes(i + 1))
            {
                ret.add(messages.get(i));
            }
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("[getMessagesInternal] exit");
        }

        return ret;
    }

    /**
     * Returns message sequence number in the folder by its UID.
     * 
     * @param uid - message UID.
     * @return message sequence number.
     * @throws FolderException if no message with given UID.
     */
    @Override
    protected int getMsnInternal(long uid) throws FolderException
    {
        Integer msn = msnCache.get(uid);
        if (msn != null)
        {
            return msn;
        }
        throw new FolderException("No such message.");
    }

    /**
     * Returns folder name.
     * 
     * @return folder name.
     */
    @Override
    protected String getNameInternal()
    {
        return folderName;
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
            logger.debug("[getNonDeletedMessagesInternal] entry");
        }
        List<SimpleStoredMessage> result = new ArrayList<SimpleStoredMessage>();

        if (messages.size() == 0 && folderInfo != null)
        {
            List<FileInfo> fileInfos = imapService.searchMails(folderInfo.getNodeRef(), viewMode);
            convertToMessages(fileInfos);
        }

        Collection<SimpleStoredMessage> values = messages.values();
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
    protected Flags getPermanentFlagsInternal()
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
    protected int getRecentCountInternal(boolean reset)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("[getRecentCountInternal] entry");
        }
        if (messages.size() == 0 && folderInfo != null)
        {
            List<FileInfo> fileInfos = imapService.searchMails(folderInfo.getNodeRef(), viewMode);
            convertToMessages(fileInfos);
        }

        int count = 0;
        Collection<SimpleStoredMessage> values = messages.values();
        for (SimpleStoredMessage message : values)
        {
            if (getFlags(message).contains(Flags.Flag.RECENT))
            {
                count++;
                if (reset)
                {
                    imapService.setFlag(((AbstractMimeMessage) message.getMimeMessage()).getMessageInfo(), Flags.Flag.RECENT, false);
                }
            }

        }

        if (logger.isDebugEnabled() && folderInfo != null)
        {
            logger.debug(folderInfo.getName() + " - Recent count: " + count + " reset: " + reset);
        }
        return count;
    }

    /**
     * Returns UIDNEXT value of the folder.
     * 
     * @return UIDNEXT value.
     */
    @Override
    protected long getUidNextInternal()
    {
        return getUidValidity();
    }

    /**
     * Returns UIDVALIDITY value of the folder.
     * 
     * @return UIDVALIDITY value.
     */
    @Override
    protected long getUidValidityInternal()
    {
       long modifDate = ((Date) serviceRegistry.getNodeService().getProperty(folderInfo.getNodeRef(), ContentModel.PROP_MODIFIED)).getTime();
       return (modifDate - YEAR_2005)/1000;
    }

    /**
     * Returns count of the messages with {@link Flags.Flag#SEEN} in the folder for the current user.
     * 
     * @return Count of the unseen messages for current user.
     */
    @Override
    protected int getUnseenCountInternal()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("[getUnseenCountInternal] entry");
        }
        if (messages.size() == 0 && folderInfo != null)
        {
            List<FileInfo> fileInfos = imapService.searchMails(folderInfo.getNodeRef(), viewMode);
            convertToMessages(fileInfos);
        }

        int count = 0;
        Collection<SimpleStoredMessage> values = messages.values();
        for (SimpleStoredMessage message : values)
        {
            if (!getFlags(message).contains(Flags.Flag.SEEN))
            {
                count++;
            }

        }
        if (logger.isDebugEnabled() && folderInfo != null)
        {
            logger.debug(folderInfo.getName() + " - Unseen count: " + count);
        }
        return count;
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
        SimpleStoredMessage message = messages.get(uid);
        FileInfo fileInfo = ((AbstractMimeMessage) message.getMimeMessage()).getMessageInfo();
        imapService.setFlags(fileInfo, MessageFlags.ALL_FLAGS, false);
        imapService.setFlags(fileInfo, flags, true);
        message = new SimpleStoredMessage(message.getMimeMessage(), message.getInternalDate(), uid);
        messages.put(uid, message);

        Long uidNotification = addUid ? uid : null;
        notifyFlagUpdate(msn, message.getFlags(), uidNotification, silentListener);
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
        SimpleStoredMessage message = (SimpleStoredMessage) messages.get(uid);

        imapService.setFlags(((AbstractMimeMessage) message.getMimeMessage()).getMessageInfo(), flags, value);
        message = new SimpleStoredMessage(message.getMimeMessage(), message.getInternalDate(), uid);
        messages.put(uid, message);

        Long uidNotification = null;
        if (addUid)
        {
            uidNotification = new Long(uid);
        }
        notifyFlagUpdate(msn, message.getFlags(), uidNotification, silentListener);

    }

    /**
     * @param fileInfo - {@link FileInfo} representing message.
     * @return UID of the message.
     */
    private long getMessageUid(FileInfo fileInfo)
    {
        if (serviceRegistry.getNodeService().getType(fileInfo.getNodeRef()).equals(ContentModel.TYPE_FOLDER))
        {
            long modifDate = ((Date) serviceRegistry.getNodeService().getProperty(fileInfo.getNodeRef(), ContentModel.PROP_MODIFIED)).getTime();
            return (modifDate - YEAR_2005)/1000;
        }
        
        return (Long) serviceRegistry.getNodeService().getProperty(fileInfo.getNodeRef(), ContentModel.PROP_NODE_DBID);
    }

    private Flags getFlags(SimpleStoredMessage mess)
    {
        return ((AbstractMimeMessage) mess.getMimeMessage()).getFlags();
    }

    // ----------------------Getters and Setters----------------------------

    public FileInfo getFolderInfo()
    {
        return folderInfo;
    }

    public void setFolderName(String folderName)
    {
        this.folderName = folderName;
    }

    public void setViewMode(ImapViewMode viewMode)
    {
        this.viewMode = viewMode;
    }

    public void setMountPointName(String mountPointName)
    {
        this.mountPointName = mountPointName;
    }

    public void setMountParent(NodeRef mountParent)
    {
        this.rootNodeRef = mountParent;
    }

    /**
     * Whether the folder is selectable.
     * 
     * @return {@code boolean}.
     */
    @Override
    protected boolean isSelectableInternal()
    {

        return this.selectable;
    }

    /**
     * Sets {@link #selectable} property.
     * 
     * @param selectable - {@code boolean}.
     */
    public void setSelectable(boolean selectable)
    {
        this.selectable = selectable;
        // Map<QName, Serializable> properties = folderInfo.getProperties();
        // properties.put(ImapModel.PROP_IMAP_FOLDER_SELECTABLE, this.selectable);
        // imapHelper.setProperties(folderInfo, properties);
    }

    /**
     * Whether the folder is read-only for user.
     * 
     * @return {@code boolean}
     */
    @Override
    protected boolean isReadOnly()
    {
        return readOnly;
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
     *  @return Wrapped AbstractMimeMessage which was created. 
     * @throws FileNotFoundException 
     * @throws FileExistsException 
     * @throws MessagingException 
     * @throws IOException 
     */
    private AbstractMimeMessage createMimeMessageInFolder(
            FileInfo folderFileInfo,
            MimeMessage message)
            throws FileExistsException, FileNotFoundException, IOException, MessagingException 
    {
        String name = AlfrescoImapConst.MESSAGE_PREFIX + GUID.generate();
        FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
        FileInfo messageFile = fileFolderService.create(folderFileInfo.getNodeRef(), name, ContentModel.TYPE_CONTENT);
        final long newMessageUid = (Long) messageFile.getProperties().get(ContentModel.PROP_NODE_DBID);
        name = AlfrescoImapConst.MESSAGE_PREFIX  + newMessageUid + AlfrescoImapConst.EML_EXTENSION;
        fileFolderService.rename(messageFile.getNodeRef(), name);
        
        if (extractAttachmentsEnabled)
        {
            extractAttachments(folderFileInfo, messageFile, message);
        }
        return new IncomingImapMessage(messageFile, serviceRegistry, message);
    }
    
    private void extractAttachments(
            FileInfo parentFolder,
            FileInfo messageFile,
            MimeMessage originalMessage)
            throws IOException, MessagingException
    {
        NodeService nodeService = serviceRegistry.getNodeService();
        FileFolderService fileFolderService = serviceRegistry.getFileFolderService();

        String messageName = (String)nodeService.getProperty(messageFile.getNodeRef(), ContentModel.PROP_NAME);
        String attachmentsFolderName = messageName + "-attachments";
        FileInfo attachmentsFolderFileInfo = null;
        Object content = originalMessage.getContent();
        if (content instanceof Multipart)
        {
            Multipart multipart = (Multipart) content;

            for (int i = 0, n = multipart.getCount(); i < n; i++)
            {
                Part part = multipart.getBodyPart(i);
                if ("attachment".equalsIgnoreCase(part.getDisposition()))
                {
                    if (attachmentsFolderFileInfo == null)
                    {
                        attachmentsFolderFileInfo = fileFolderService.create(
                                parentFolder.getNodeRef(),
                                attachmentsFolderName,
                                ContentModel.TYPE_FOLDER);
                        serviceRegistry.getNodeService().createAssociation(
                                messageFile.getNodeRef(),
                                attachmentsFolderFileInfo.getNodeRef(),
                                ImapModel.ASSOC_IMAP_ATTACHMENTS_FOLDER);
                    }
                    createAttachment(messageFile, attachmentsFolderFileInfo, part);
                }
            }
        }

    }
    
    private void createAttachment(FileInfo messageFile, FileInfo attachmentsFolderFileInfo, Part part) throws MessagingException, IOException
    {
        String fileName = part.getFileName();
        try
        {
            fileName = MimeUtility.decodeText(fileName);
        }
        catch (UnsupportedEncodingException e)
        {
            if (logger.isWarnEnabled())
            {
                logger.warn("Cannot decode file name '" + fileName + "'", e);
            }
        }

        ContentType contentType = new ContentType(part.getContentType());
        FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
        List<FileInfo> result = fileFolderService.search(attachmentsFolderFileInfo.getNodeRef(), fileName, false);
        // The one possible behaviour
        /*
        if (result.size() > 0)
        {
            for (FileInfo fi : result)
            {
                fileFolderService.delete(fi.getNodeRef());
            }
        }
        */
        // And another one behaviour which will overwrite the content of the existing file. It is performance preferable.
        FileInfo attachmentFile = null;
        if (result.size() == 0)
        {
            FileInfo createdFile = fileFolderService.create(
                    attachmentsFolderFileInfo.getNodeRef(),
                    fileName,
                    ContentModel.TYPE_CONTENT);
            serviceRegistry.getNodeService().createAssociation(
                    messageFile.getNodeRef(),
                    createdFile.getNodeRef(),
                    ImapModel.ASSOC_IMAP_ATTACHMENT);
            result.add(createdFile);
        }
        attachmentFile = result.get(0);
        ContentWriter writer = fileFolderService.getWriter(attachmentFile.getNodeRef());
        writer.setMimetype(contentType.getBaseType());
        OutputStream os = writer.getContentOutputStream();
        FileCopyUtils.copy(part.getInputStream(), os);
    }
    
    class CacheItem
    {
        private Date modified;
        private SimpleStoredMessage message;
        
        public CacheItem(Date modified, SimpleStoredMessage message)
        {
            this.setMessage(message);
            this.setModified(modified);
        }

        public void setModified(Date modified)
        {
            this.modified = modified;
        }

        public Date getModified()
        {
            return modified;
        }

        public void setMessage(SimpleStoredMessage message)
        {
            this.message = message;
        }

        public SimpleStoredMessage getMessage()
        {
            return message;
        }
    }
    
}
