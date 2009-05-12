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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.search.SearchTerm;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.icegreen.greenmail.foedus.util.MsgRangeFilter;
import com.icegreen.greenmail.imap.ImapConstants;
import com.icegreen.greenmail.mail.MovingMessage;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.FolderListener;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.store.MessageFlags;
import com.icegreen.greenmail.store.SimpleStoredMessage;
import com.icegreen.greenmail.util.GreenMailUtil;

/**
 * Implementation of greenmail MailFolder. It represents an Alfresco content folder and handles appendMessage, copyMessage, expunge (delete), getMessages, getMessage and so
 * requests.
 * 
 * @author Mike Shavnev
 */
public class AlfrescoImapMailFolder implements MailFolder
{

    private static Log logger = LogFactory.getLog(AlfrescoImapMailFolder.class);

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
     * Defines view mode. Can be one of the following: {@link AlfrescoImapConst#MODE_ARCHIVE} or {@link AlfrescoImapConst#MODE_VIRTUAL}.
     */
    private String viewMode;

    /**
     * Name of the mount point.
     */
    private String mountPointName;

    /**
     * Reference to the {@link ImapHelper} object.
     */
    private ImapHelper imapHelper;

    /**
     * Defines whether the folder is selectable or not.
     */
    private Boolean selectable;

    /**
     * Defines whether the folder is read-only for user or not.
     */
    private Boolean readOnly;
    

    private Map<Long, SimpleStoredMessage> messages = new TreeMap<Long, SimpleStoredMessage>();
    private boolean isBodyGenerated = false;

    private static final Flags PERMANENT_FLAGS = new Flags();

    private List<FolderListener> listeners = new LinkedList<FolderListener>();

    static
    {
        PERMANENT_FLAGS.add(Flags.Flag.ANSWERED);
        PERMANENT_FLAGS.add(Flags.Flag.DELETED);
        PERMANENT_FLAGS.add(Flags.Flag.DRAFT);
        PERMANENT_FLAGS.add(Flags.Flag.FLAGGED);
        PERMANENT_FLAGS.add(Flags.Flag.SEEN);
    }

    /**
     * Constructs {@link AlfrescoImapMailFolder} object.
     * 
     * @param qualifiedMailboxName - name of the mailbox (e.g. "admin" for admin user).
     * @param folderInfo - reference to the {@link FileInfo} object representing the folder.
     * @param folderName - name of the folder.
     * @param viewMode - defines view mode. Can be one of the following: {@link AlfrescoImapConst#MODE_ARCHIVE} or {@link AlfrescoImapConst#MODE_VIRTUAL}.
     * @param rootNodeRef - reference to the root node of the store where folder is placed.
     * @param mountPointName - name of the mount point.
     * @param imapHelper - reference to the {@link ImapHelper} object.
     */
    public AlfrescoImapMailFolder(String qualifiedMailboxName, FileInfo folderInfo, String folderName, String viewMode, NodeRef rootNodeRef, String mountPointName,
            ImapHelper imapHelper)
    {
        this(qualifiedMailboxName, folderInfo, folderName, viewMode, rootNodeRef, mountPointName, imapHelper, null);
    }

    /**
     * Constructs {@link AlfrescoImapMailFolder} object.
     * 
     * @param qualifiedMailboxName - name of the mailbox (e.g. "admin" for admin user).
     * @param folderInfo - reference to the {@link FileInfo} object representing the folder.
     * @param folderName - name of the folder.
     * @param viewMode - defines view mode. Can be one of the following: {@link AlfrescoImapConst#MODE_ARCHIVE} or {@link AlfrescoImapConst#MODE_VIRTUAL}.
     * @param rootNodeRef - reference to the root node of the store where folder is placed.
     * @param mountPointName - name of the mount point.
     * @param imapHelper - reference to the {@link ImapHelper} object.
     * @param selectable - defines whether the folder is selectable or not.
     */
    public AlfrescoImapMailFolder(String qualifiedMailboxName, FileInfo folderInfo, String folderName, String viewMode, NodeRef rootNodeRef, String mountPointName,
            ImapHelper imapHelper, Boolean selectable)
    {
        this.qualifiedMailboxName = qualifiedMailboxName;
        this.folderInfo = folderInfo;
        this.rootNodeRef = rootNodeRef;
        this.imapHelper = imapHelper;
        this.folderName = folderName != null ? folderName : (folderInfo != null ? folderInfo.getName() : null);
        this.viewMode = viewMode != null ? viewMode : AlfrescoImapConst.MODE_ARCHIVE;
        this.mountPointName = mountPointName;

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
                Boolean storedSelectable = !imapHelper.getNodeService().hasAspect(folderInfo.getNodeRef(), ImapModel.ASPECT_IMAP_FOLDER_NONSELECTABLE);
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
            
            AccessStatus status = imapHelper.hasPermission(folderInfo.getNodeRef(), PermissionService.WRITE);
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
            setSelectable(false);
        }
        
    }

    /**
     * Adds {@link FolderListener} to the folder.
     * 
     * @param listener - new listener.
     */
    public void addListener(FolderListener listener)
    {
        listeners.add(listener);

    }

    protected void processTextMessage(MimeMessage message, FileInfo messageHome) throws MessagingException, ContentIOException, IOException
    {
        FileInfo messageBody = imapHelper.getFileFolderService().create(messageHome.getNodeRef(), AlfrescoImapConst.BODY_TEXT_PLAIN_NAME, ImapModel.TYPE_IMAP_BODY);
        ContentWriter writer = imapHelper.getFileFolderService().getWriter(messageBody.getNodeRef());
        writer.setMimetype(message.getContentType());
        writer.setEncoding("UTF-8");
        writer.putContent(message.getInputStream());
    }

    protected void processTextMessage(BodyPart part, FileInfo messageHome, boolean isBody) throws MessagingException, ContentIOException, IOException
    {
        FileInfo messageBody = null;
        ContentType ct = new ContentType(part.getContentType());
        ContentWriter writer = null;
        if (isBody)
        {
            if ("plain".equalsIgnoreCase(ct.getSubType()))
            {
                messageBody = imapHelper.getFileFolderService().create(messageHome.getNodeRef(), AlfrescoImapConst.BODY_TEXT_PLAIN_NAME, ImapModel.TYPE_IMAP_BODY);
                writer = imapHelper.getFileFolderService().getWriter(messageBody.getNodeRef());
                writer.setEncoding(MimeUtility.javaCharset(ct.getParameter("charset")));
                writer.setMimetype(ct.toString());
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                IOUtils.copy(part.getInputStream(), outputStream);
                OutputStreamWriter outputWriter = new OutputStreamWriter(writer.getContentOutputStream());
                outputWriter.write(outputStream.toString());
                outputWriter.flush();
                outputWriter.close();
            }
            else if ("html".equalsIgnoreCase(ct.getSubType()))
            {
                messageBody = imapHelper.getFileFolderService().create(messageHome.getNodeRef(), AlfrescoImapConst.BODY_TEXT_HTML_NAME, ImapModel.TYPE_IMAP_BODY);
                writer = imapHelper.getFileFolderService().getWriter(messageBody.getNodeRef());
                writer.setMimetype(ct.toString());
                String javaCharset = MimeUtility.javaCharset(ct.getParameter("charset"));
                writer.setEncoding(javaCharset);

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                IOUtils.copy(part.getInputStream(), os);
                OutputStreamWriter cosw = new OutputStreamWriter(writer.getContentOutputStream());
                cosw.write(os.toString());
                cosw.flush();
                cosw.close();
            }
        }
        else
        {
            saveAttachment(part, messageHome);
        }
    }

    protected void saveAttachment(BodyPart part, FileInfo messageHome) throws FileExistsException, MessagingException, ContentIOException, IOException
    {
        FileInfo messageBody = imapHelper.getFileFolderService().create(messageHome.getNodeRef(), MimeUtility.decodeText(part.getFileName()), ImapModel.TYPE_IMAP_ATTACH);
        ContentWriter writer = imapHelper.getFileFolderService().getWriter(messageBody.getNodeRef());
        writer.setMimetype(part.getContentType());
        writer.setEncoding("UTF-8");
        writer.putContent(part.getInputStream());

        String[] attachId = part.getHeader("Content-ID");
        if (attachId != null && attachId.length > 0)
        {
            imapHelper.getNodeService().setProperty(messageBody.getNodeRef(), ImapModel.PROP_ATTACH_ID, attachId[0]);
        }
    }

    /**
     * Appends message to the folder.
     * 
     * @param message - message.
     * @param flags - message flags.
     * @param internalDate - not used. Current date used instead.
     * @return
     */
    public long appendMessage(MimeMessage message, Flags flags, Date internalDate) throws FolderException
    {
        if (this.readOnly)
        {
            throw new FolderException("Can't append message - Permission denied");
        }

        //TODO FILE EXIST
        String name = AlfrescoImapConst.MESSAGE_PREFIX + GUID.generate();
        FileInfo messageHome = imapHelper.getFileFolderService().create(folderInfo.getNodeRef(), name, ImapModel.TYPE_IMAP_CONTENT);
        final long newMessageUid = (Long) messageHome.getProperties().get(ContentModel.PROP_NODE_DBID);

        try
        {
            name = AlfrescoImapConst.MESSAGE_PREFIX  + newMessageUid;
            imapHelper.getFileFolderService().rename(messageHome.getNodeRef(), name);

            Object content = message.getContent();
            if (content instanceof Multipart)
            {
                Multipart multipart = (Multipart) content;

                for (int i = 0, n = multipart.getCount(); i < n; i++)
                {
                    Part part = multipart.getBodyPart(i);
                    createMessageFiles(messageHome, (MimeBodyPart) part);

                }
            }
            else
            {
                processTextMessage(message, messageHome);
            }

            imapHelper.setFlags(messageHome, flags, true);
            SimpleStoredMessage storedMessage = new SimpleStoredMessage(new AlfrescoImapMessage(messageHome, imapHelper, message), new Date(), newMessageUid);
            messages.put(newMessageUid, storedMessage);
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException("Internal error", e);
        }

        return newMessageUid;

    }

    private void createMessageFiles(FileInfo messageHome, MimeBodyPart part) throws IOException, MessagingException
    {

        Object content = part.getContent();

        if (content instanceof MimeMultipart)
        {
            int count = ((MimeMultipart) content).getCount();
            for (int i = 0; i < count; i++)
            {
                createMessageFiles(messageHome, (MimeBodyPart) ((MimeMultipart) content).getBodyPart(i));
            }
        }
        else
        {

            String partName = part.getFileName();
            if (partName == null)
            {
                processTextMessage(part, messageHome, true);
            }
            else
            {
                processTextMessage(part, messageHome, false);
            }

        }

    }

    /**
     * Copies message with the given UID to the specified {@link MailFolder}.
     * 
     * @param uid - UID of the message
     * @param toFolder - reference to the destination folder.
     */
    public void copyMessage(long uid, MailFolder toFolder) throws FolderException
    {
        AlfrescoImapMailFolder toImapMailFolder = (AlfrescoImapMailFolder) toFolder;

        if (toImapMailFolder.isReadOnly())
        {
            throw new FolderException("Can't create folder - Permission denied");
        }
        
        NodeRef toNodeRef = toImapMailFolder.getFolderInfo().getNodeRef();

        SimpleStoredMessage message = messages.get(uid);
        FileInfo copyMess = ((AlfrescoImapMessage) message.getMimeMessage()).getMessageInfo();

        List<FileInfo> fis = new LinkedList<FileInfo>();

        if (imapHelper.getNodeService().getType(copyMess.getNodeRef()).equals(ImapModel.TYPE_IMAP_CONTENT))
        {
            
            //TODO FILE EXIST
            NodeRef messageFolder = imapHelper.getFileFolderService().create(toNodeRef, AlfrescoImapConst.MESSAGE_PREFIX + GUID.generate(), ImapModel.TYPE_IMAP_CONTENT).getNodeRef();
            
            final long nextUid = (Long) imapHelper.getNodeService().getProperty(messageFolder, ContentModel.PROP_NODE_DBID);

            String name = AlfrescoImapConst.MESSAGE_PREFIX + nextUid;
            try
            {
                imapHelper.getFileFolderService().rename(messageFolder, name);
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            Map<QName, Serializable> srcMesProps = imapHelper.getNodeService().getProperties(copyMess.getNodeRef());
            Map<QName, Serializable> dstMessProps = imapHelper.getNodeService().getProperties(messageFolder);
            srcMesProps.putAll(dstMessProps);
            imapHelper.getNodeService().setProperties(messageFolder, srcMesProps);


            fis = imapHelper.getFileFolderService().search(copyMess.getNodeRef(), "*", false);
            toNodeRef = messageFolder;
        }
        else
        {
            fis.add(copyMess);
        }
        for (FileInfo fi : fis)
        {
            try
            {
                imapHelper.getFileFolderService().copy(fi.getNodeRef(), toNodeRef, null);
            }
            catch (FileExistsException e)
            {
                logger.error(e);
            }
            catch (FileNotFoundException e)
            {
                logger.error(e);
            }
        }
    }

    /**
     * Marks all messages in the folder as deleted using {@link Flags.Flag#DELETED} flag.
     */
    public void deleteAllMessages() throws FolderException
    {
        if (this.readOnly)
        {
            throw new FolderException("Can't delete all - Permission denied");
        }
        
        for (SimpleStoredMessage mess : messages.values())
        {
            AlfrescoImapMessage message = (AlfrescoImapMessage) mess.getMimeMessage();
            FileInfo fileInfo = message.getMessageInfo();
            imapHelper.setFlag(fileInfo, Flags.Flag.DELETED, true);
            // comment out to physically remove content.
            // fileFolderService.delete(fileInfo.getNodeRef());
            messages.remove(mess.getUid());
        }
    }

    /**
     * Deletes messages marked with {@link Flags.Flag#DELETED}. Note that this message deletes all messages with this flag.
     */
    public void expunge() throws FolderException
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
                NodeRef nodeRef = ((AlfrescoImapMessage) mess.getMimeMessage()).getMessageInfo().getNodeRef();
                imapHelper.getFileFolderService().delete(nodeRef);
            }
        }
    }

    /**
     * Returns the number of the first unseen message.
     * 
     * @return Number of the first unseen message.
     */
    public int getFirstUnseen()
    {
        return 0;
    }

    /**
     * Returns full name of the folder with namespace and full path delimited with the hierarchy delimiter (see {@link AlfrescoImapConst#HIERARCHY_DELIMITER}) <p/> E.g.: <p/>
     * #mail.admin."Repository_archive.Data Dictionary.Space Templates.Software Engineering Project" <p/> This is required by GreenMail implementation.
     */
    public String getFullName()
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

        StringBuilder fullName = new StringBuilder();
        List<FileInfo> pathList;
        try
        {
            pathList = imapHelper.getFileFolderService().getNamePath(rootNodeRef, folderInfo.getNodeRef());
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
        }
        catch (FileNotFoundException e)
        {
            logger.error(e);
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("fullName: " + fullName);
        }
        return GreenMailUtil.convertInUtf7(fullName.toString());
    }

    /**
     * Returns message by its UID.
     * 
     * @param uid - UID of the message.
     * @return message.
     */
    public SimpleStoredMessage getMessage(long uid)
    {
        if (!isBodyGenerated)
        {
            // regenerate messages list and include message body into result
            getMessages();
        }
        return messages.get(uid);
    }

    /**
     * Returns count of the messages in the folder.
     * 
     * @return Count of the messages.
     */
    public int getMessageCount()
    {
        if (messages.size() == 0)
        {
            List<FileInfo> fileInfos = imapHelper.searchMails(folderInfo.getNodeRef(), "*", viewMode, false);
            getMessages(fileInfos, false);
        }
        if (logger.isDebugEnabled())
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
    public long[] getMessageUids()
    {
        if (messages == null || messages.size() == 0)
        {
            List<FileInfo> fileInfos = imapHelper.searchMails(folderInfo.getNodeRef(), "*", viewMode, false);
            getMessages(fileInfos, false);
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
    public List<SimpleStoredMessage> getMessages()
    {
        List<FileInfo> fileInfos = imapHelper.searchMails(folderInfo.getNodeRef(), "*", viewMode, false);
        return getMessages(fileInfos, true);
    }

    private List<SimpleStoredMessage> getMessages(List<FileInfo> fileInfos, boolean generateBody)
    {
        isBodyGenerated = generateBody;
        if (fileInfos == null || fileInfos.size() == 0)
        {
            messages = Collections.emptyMap();
        }
        if (fileInfos.size() != messages.size() || generateBody)
        {
            for (FileInfo fileInfo : fileInfos)
            {
                try
                {
                    Long key = getMessageUid(fileInfo);
                    SimpleStoredMessage message = new SimpleStoredMessage(new AlfrescoImapMessage(fileInfo, imapHelper, generateBody), new Date(), key);
                    messages.put(key, message);
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Message added: " + fileInfo.getName());
                    }
                }
                catch (MessagingException e)
                {
                    logger.warn("Invalid message! File name:" + fileInfo.getName(), e);
                }
            }
        }
        return new LinkedList<SimpleStoredMessage>(messages.values());
    }

    /**
     * Returns list of messages by filter.
     * 
     * @param msgRangeFilter - {@link MsgRangeFilter} object representing filter.
     * @return list of filtered messages.
     */
    public List<SimpleStoredMessage> getMessages(MsgRangeFilter msgRangeFilter)
    {
        if (messages == null || messages.size() == 0 || !isBodyGenerated)
        {
            List<FileInfo> fileInfos = imapHelper.searchMails(folderInfo.getNodeRef(), "*", viewMode, false);
            getMessages(fileInfos, true);
        }
        List<SimpleStoredMessage> ret = new ArrayList<SimpleStoredMessage>();
        for (int i = 0; i < messages.size(); i++)
        {
            if (msgRangeFilter.includes(i + 1))
            {
                ret.add(messages.get(i));
            }
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
    public int getMsn(long uid) throws FolderException
    {
        // Um not sure in this because the getMsn is not documented...
        // Implemented alike GreenMail implementation.
        Set<Long> keys = messages.keySet();
        int msn = 0;
        for (Long key : keys)
        {
            // "==" is legal with primitives and autoboxing
            if (key == uid)
            {
                return msn + 1;
            }
            msn++;
        }
        throw new FolderException("No such message.");
    }

    /**
     * Returns folder name.
     * 
     * @return folder name.
     */
    public String getName()
    {
        return folderName;
    }

    /**
     * Returns the list of messages that have no {@link Flags.Flag#DELETED} flag set for current user.
     * 
     * @return the list of non-deleted messages.
     */
    public List<SimpleStoredMessage> getNonDeletedMessages()
    {
        List<SimpleStoredMessage> result = new ArrayList<SimpleStoredMessage>();

        if (messages.size() == 0 || !isBodyGenerated)
        {
            List<FileInfo> fileInfos = imapHelper.searchMails(folderInfo.getNodeRef(), "*", viewMode, false);
            getMessages(fileInfos, true);
        }

        Collection<SimpleStoredMessage> values = messages.values();
        for (SimpleStoredMessage message : values)
        {
            if (!getFlags(message).contains(Flags.Flag.DELETED))
            {
                result.add(message);
            }

        }
        if (logger.isDebugEnabled())
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
    public Flags getPermanentFlags()
    {
        return PERMANENT_FLAGS;
    }

    /**
     * Returns count of messages with {@link Flags.Flag#RECENT} flag. If {@code reset} parameter is {@code true} - removes {@link Flags.Flag#RECENT} flag from the message for
     * current user.
     * 
     * @param reset - if true the {@link Flags.Flag#RECENT} will be deleted for current user if exists.
     * @return returns count of recent messages.
     */
    public int getRecentCount(boolean reset)
    {
        if (messages.size() == 0)
        {
            List<FileInfo> fileInfos = imapHelper.searchMails(folderInfo.getNodeRef(), "*", viewMode, false);
            getMessages(fileInfos, false);
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
                    imapHelper.setFlag(((AlfrescoImapMessage) message.getMimeMessage()).getMessageInfo(), Flags.Flag.RECENT, false);
                }
            }

        }

        if (logger.isDebugEnabled())
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
    public long getUidNext()
    {
        return getUidValidity();
    }

    /**
     * Returns UIDVALIDITY value of the folder.
     * 
     * @return UIDVALIDITY value.
     */
    public long getUidValidity()
    {
       return ((Date) imapHelper.getNodeService().getProperty(folderInfo.getNodeRef(), ContentModel.PROP_MODIFIED)).getTime();
    }

    /**
     * Returns count of the messages with {@link Flags.Flag#SEEN} in the folder for the current user.
     * 
     * @return Count of the unseen messages for current user.
     */
    public int getUnseenCount()
    {
        if (messages.size() == 0)
        {
            List<FileInfo> fileInfos = imapHelper.searchMails(folderInfo.getNodeRef(), "*", viewMode, false);
            getMessages(fileInfos, false);
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
        if (logger.isDebugEnabled())
        {
            logger.debug(folderInfo.getName() + " - Unseen count: " + count);
        }
        return count;
    }

    /**
     * Removes {@link FolderListener} from the folder.
     * 
     * @param listener - Listener to remove.
     */
    public void removeListener(FolderListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * Replaces flags for the message with the given UID. If {@code addUid} is set to {@code true} {@link FolderListener} objects defined for this folder will be notified.
     * {@code silentListener} can be provided - this listener wouldn't be notified.
     * 
     * @param flags - new flags.
     * @param uid - message UID.
     * @param silentListener - listener that shouldn't be notified.
     * @param addUid - defines whether or not listeners be notified.
     */
    public void replaceFlags(Flags flags, long uid, FolderListener silentListener, boolean addUid) throws FolderException
    {
        int msn = getMsn(uid);
        SimpleStoredMessage message = messages.get(uid);
        FileInfo fileInfo = ((AlfrescoImapMessage) message.getMimeMessage()).getMessageInfo();
        try
        {
            imapHelper.setFlags(fileInfo, MessageFlags.ALL_FLAGS, false);
            imapHelper.setFlags(fileInfo, flags, true);
            message = new SimpleStoredMessage(message.getMimeMessage(), message.getInternalDate(), uid);
            messages.put(uid, message);
        }
        catch (MessagingException e)
        {
            logger.warn("Can't set flags due to an error:", e);
        }

        Long uidNotification = addUid ? uid : null;
        notifyFlagUpdate(msn, message.getFlags(), uidNotification, silentListener);
    }

    private void notifyFlagUpdate(int msn, Flags flags, Long uidNotification, FolderListener silentListener)
    {
        synchronized (listeners)
        {
            for (FolderListener listener : listeners)
            {
                if (listener == silentListener)
                {
                    continue;
                }

                listener.flagsUpdated(msn, flags, uidNotification);
            }
        }
    }

    /**
     * Simply returns UIDs of all messages in the folder.
     * 
     * @param searchTerm - not used
     * @return UIDs of the messages
     */
    public long[] search(SearchTerm searchTerm)
    {
        return getMessageUids();
    }

    /**
     * Sets flags for the message with the given UID. If {@code addUid} is set to {@code true} {@link FolderListener} objects defined for this folder will be notified.
     * {@code silentListener} can be provided - this listener wouldn't be notified.
     * 
     * @param flags - new flags.
     * @param value - flags value.
     * @param uid - message UID.
     * @param silentListener - listener that shouldn't be notified.
     * @param addUid - defines whether or not listeners be notified.
     */
    public void setFlags(Flags flags, boolean value, long uid, FolderListener silentListener, boolean addUid) throws FolderException
    {
        int msn = getMsn(uid);
        SimpleStoredMessage message = (SimpleStoredMessage) messages.get(uid);

        try
        {
            imapHelper.setFlags(((AlfrescoImapMessage) message.getMimeMessage()).getMessageInfo(), flags, value);
            message = new SimpleStoredMessage(message.getMimeMessage(), message.getInternalDate(), uid);
            messages.put(uid, message);
        }
        catch (MessagingException e)
        {
            logger.warn("Can't set flags due to an error:", e);
        }

        Long uidNotification = null;
        if (addUid)
        {
            uidNotification = new Long(uid);
        }
        notifyFlagUpdate(msn, message.getFlags(), uidNotification, silentListener);

    }

    /**
     * Method is called before the deletion of the folder. Notifies {@link FolderListener} objects with {@link FolderListener#mailboxDeleted()} method calls.
     */
    public void signalDeletion()
    {
        synchronized (listeners)
        {
            for (int i = 0; i < listeners.size(); i++)
            {
                FolderListener listener = (FolderListener) listeners.get(i);
                listener.mailboxDeleted();
            }
        }
    }

    /**
     * Not supported. Added to implement {@link MailFolder#store(MovingMessage)}.
     */
    public void store(MovingMessage mail) throws Exception
    {
        throw new UnsupportedOperationException("Method store(MovingMessage) is not suppoted.");
    }

    /**
     * Not supported. Added to implement {@link MailFolder#store(MimeMessage)}.
     */
    public void store(MimeMessage message) throws Exception
    {
        throw new UnsupportedOperationException("Method store(MimeMessage) is not suppoted.");
    }

    /**
     * @param fileInfo - {@link FileInfo} representing message.
     * @return UID of the message.
     */
    private long getMessageUid(FileInfo fileInfo)
    {
        if (imapHelper.getNodeService().getType(fileInfo.getNodeRef()).equals(ContentModel.TYPE_FOLDER))
        {
            return ((Date) imapHelper.getNodeService().getProperty(fileInfo.getNodeRef(), ContentModel.PROP_MODIFIED)).getTime();
        }
        
        return (Long) imapHelper.getNodeService().getProperty(fileInfo.getNodeRef(), ContentModel.PROP_NODE_DBID);
    }

    private Flags getFlags(SimpleStoredMessage mess)
    {
        return ((AlfrescoImapMessage) mess.getMimeMessage()).getFlags();
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

    public void setViewMode(String viewMode)
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
    public boolean isSelectable()
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
     * @return {@code boolean}
     */
    public boolean isReadOnly()
    {
        return readOnly;
    }

}
