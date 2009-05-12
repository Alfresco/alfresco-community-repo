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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.imap.config.ImapConfigElement.ImapConfig;
import org.alfresco.repo.imap.exception.AlfrescoImapFolderException;
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.icegreen.greenmail.imap.AuthorizationException;
import com.icegreen.greenmail.imap.ImapHostManager;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMailUtil;

/**
 * @author Mike Shavnev
 */
public class AlfrescoImapHostManager implements ImapHostManager
{

    private Log logger = LogFactory.getLog(AlfrescoImapHostManager.class);

    private ServiceRegistry serviceRegistry;

    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private ImapHelper imapHelper;

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
     * Returns an collection of mailboxes. Method searches mailboxes under mount points defined for a specific user. Mount points include user's IMAP Virtualised Views and Email
     * Archive Views. This method serves LIST command of the IMAP protocol.
     * 
     * @param user User making the request
     * @param mailboxPattern String name of a mailbox possible including a wildcard.
     * @return Collection of mailboxes matching the pattern.
     * @throws com.icegreen.greenmail.store.FolderException
     */
    public Collection<MailFolder> listMailboxes(GreenMailUser user, String mailboxPattern) throws FolderException
    {
        mailboxPattern = GreenMailUtil.convertFromUtf7(mailboxPattern);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Listing mailboxes: mailboxPattern=" + mailboxPattern);
        }
        mailboxPattern = imapHelper.getMailPathInRepo(mailboxPattern);
        if (logger.isDebugEnabled())
        {
            logger.debug("Listing mailboxes: mailboxPattern in alfresco=" + mailboxPattern);
        }
        return listMailboxes(user, mailboxPattern, false);
    }

    /**
     * Returns an collection of subscribed mailboxes. To appear in search result mailboxes should have {http://www.alfresco.org/model/imap/1.0}subscribed property specified for
     * user. Method searches subscribed mailboxes under mount points defined for a specific user. Mount points include user's IMAP Virtualised Views and Email Archive Views. This
     * method serves LSUB command of the IMAP protocol.
     * 
     * @param user User making the request
     * @param mailboxPattern String name of a mailbox possible including a wildcard.
     * @return Collection of mailboxes matching the pattern.
     * @throws com.icegreen.greenmail.store.FolderException
     */
    public Collection<MailFolder> listSubscribedMailboxes(GreenMailUser user, String mailboxPattern) throws FolderException
    {
        mailboxPattern = GreenMailUtil.convertFromUtf7(mailboxPattern);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Listing subscribed mailboxes: mailboxPattern=" + mailboxPattern);
        }
        mailboxPattern = imapHelper.getMailPathInRepo(mailboxPattern);
        if (logger.isDebugEnabled())
        {
            logger.debug("Listing subscribed mailboxes: mailboxPattern in alfresco=" + mailboxPattern);
        }

        return listMailboxes(user, mailboxPattern, true);
    }

    /**
     * Depend on listSubscribed param, list Mailboxes or list subscribed Mailboxes
     */
    private Collection<MailFolder> listMailboxes(GreenMailUser user, String mailboxPattern, boolean listSubscribed) throws FolderException
    {
        Collection<MailFolder> result = new LinkedList<MailFolder>();

        Map<String, NodeRef> mountPoints = imapHelper.getMountPoints();
        Map<String, ImapConfig> imapConfigs = imapHelper.getImapConfigs();

        NodeRef mountPoint;

        // List mailboxes that are in mount points
        for (String mountPointName : mountPoints.keySet())
        {

            mountPoint = mountPoints.get(mountPointName);
            FileInfo mountPointFileInfo = imapHelper.getFileFolderService().getFileInfo(mountPoint);
            NodeRef mountParent = imapHelper.getNodeService().getParentAssocs(mountPoint).get(0).getParentRef();
            String viewMode = imapConfigs.get(mountPointName).getMode();

            if (!mailboxPattern.equals("*"))
            {
                mountPoint = mountParent;
            }

            boolean isVirtualView = imapConfigs.get(mountPointName).getMode().equals(AlfrescoImapConst.MODE_VIRTUAL);
            Collection<MailFolder> folders = listFolder(mountPoint, mountPoint, user, mailboxPattern, listSubscribed, isVirtualView);
            if (folders != null)
            {
                for (MailFolder mailFolder : folders)
                {
                    AlfrescoImapMailFolder folder = (AlfrescoImapMailFolder) mailFolder;
                    folder.setMountPointName(mountPointName);
                    folder.setViewMode(viewMode);
                    folder.setMountParent(mountParent);
                }
                result.addAll(folders);
            }

            // Add mount point to the result list
            if (mailboxPattern.equals("*"))
            {
                if ((listSubscribed && isSubscribed(mountPointFileInfo, user.getLogin())) || (!listSubscribed))
                {
                    result.add(new AlfrescoImapMailFolder(user.getQualifiedMailboxName(), mountPointFileInfo, mountPointName, viewMode, mountParent, mountPointName, imapHelper));
                }
                // \NoSelect
                else if (listSubscribed && hasSubscribedChild(mountPointFileInfo, user.getLogin(), isVirtualView))
                {
                    result.add(new AlfrescoImapMailFolder(user.getQualifiedMailboxName(), mountPointFileInfo, mountPointName, viewMode, mountParent, mountPointName, imapHelper,
                            false));
                }
            }

        }

        // List mailboxes that are in user IMAP Home
        NodeRef root = imapHelper.getUserImapHomeRef(user.getLogin());
        Collection<MailFolder> imapFolders = listFolder(root, root, user, mailboxPattern, listSubscribed, false);

        if (imapFolders != null)
        {
            for (MailFolder mailFolder : imapFolders)
            {
                AlfrescoImapMailFolder folder = (AlfrescoImapMailFolder) mailFolder;
                folder.setViewMode(AlfrescoImapConst.MODE_ARCHIVE);
                folder.setMountParent(root);
            }
            result.addAll(imapFolders);
        }

        return result;

    }

    private Collection<MailFolder> listFolder(NodeRef mailboxRoot, NodeRef root, GreenMailUser user, String mailboxPattern, boolean listSubscribed, boolean isVirtualView)
            throws FolderException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Listing mailboxes: mailboxPattern=" + mailboxPattern);
        }

        int index = mailboxPattern.indexOf(AlfrescoImapConst.HIERARCHY_DELIMITER);

        String name = null;
        String remainName = null;

        if (index < 0)
        {
            name = mailboxPattern;
        }
        else
        {
            name = mailboxPattern.substring(0, index);
            remainName = mailboxPattern.substring(index + 1);
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Listing mailboxes: name=" + name);
        }

        if (index < 0)
        {
            if ("*".equals(name))
            {
                Collection<FileInfo> list = imapHelper.searchFolders(root, name, true, isVirtualView);
                if (listSubscribed)
                {
                    list = getSubscribed(list, user.getLogin());
                }

                if (list.size() > 0)
                {
                    return createMailFolderList(user, list, mailboxRoot);
                }
                return null;
            }
            else if (name.endsWith("*"))
            {
                List<FileInfo> fullList = new LinkedList<FileInfo>();
                List<FileInfo> list = imapHelper.searchFolders(root, name.replace('%', '*'), false, isVirtualView);
                Collection<FileInfo> subscribedList = list;
                if (listSubscribed)
                {
                    subscribedList = getSubscribed(list, user.getLogin());
                }

                if (list.size() > 0)
                {
                    fullList.addAll(subscribedList);
                    for (FileInfo fileInfo : list)
                    {
                        List<FileInfo> childList = imapHelper.searchFolders(fileInfo.getNodeRef(), "*", true, isVirtualView);
                        if (listSubscribed)
                        {
                        fullList.addAll(getSubscribed(childList, user.getLogin()));
                    }
                        else
                        {
                            fullList.addAll(childList);
                        }
                    }
                    return createMailFolderList(user, fullList, mailboxRoot);
                }
                return null;
            }
            else if ("%".equals(name))
            {
                List<FileInfo> list = imapHelper.searchFolders(root, "*", false, isVirtualView);
                LinkedList<MailFolder> subscribedList = new LinkedList<MailFolder>();

                if (listSubscribed)
                {
                for (FileInfo fileInfo : list)
                {
                    if (isSubscribed(fileInfo, user.getLogin()))
                    {
                            // folderName, viewMode, mountPointName will be setted in listMailboxes() method
                        subscribedList.add(new AlfrescoImapMailFolder(user.getQualifiedMailboxName(), fileInfo, null, null, mailboxRoot, null, imapHelper));
                    }
                    // \NoSelect
                    else if (hasSubscribedChild(fileInfo, user.getLogin(), isVirtualView))
                    {
                            // folderName, viewMode, mountPointName will be setted in listMailboxes() method
                        subscribedList.add(new AlfrescoImapMailFolder(user.getQualifiedMailboxName(), fileInfo, null, null, mailboxRoot, null, imapHelper, false));
                    }
                }
                }
                else
                {
                    return createMailFolderList(user, list, mailboxRoot);
                }

                return subscribedList;
            }
            else if (name.contains("%") || name.contains("*"))
            {
                List<FileInfo> list = imapHelper.searchFolders(root, name.replace('%', '*'), false, isVirtualView);
                Collection<FileInfo> subscribedList = list;
                if (listSubscribed)
                {
                    subscribedList = getSubscribed(list, user.getLogin());
                }

                if (subscribedList.size() > 0)
                {
                    return createMailFolderList(user, subscribedList, mailboxRoot);
                }
                return null;
            }
            else
            {
                List<FileInfo> list = imapHelper.searchFolders(root, name, false, isVirtualView);
                Collection<FileInfo> subscribedList = list;
                if (listSubscribed)
                {
                    subscribedList = getSubscribed(list, user.getLogin());
                }

                if (subscribedList.size() > 0)
                {
                    return createMailFolderList(user, subscribedList, mailboxRoot);
                }
                return null;
            }
        }

        // If (index != -1) this is not the last level
        Collection<MailFolder> result = new LinkedList<MailFolder>();

        List<FileInfo> list = imapHelper.searchFolders(root, name.replace('%', '*'), false, isVirtualView);
        for (FileInfo folder : list)
        {
            Collection<MailFolder> childFolders = listFolder(mailboxRoot, folder.getNodeRef(), user, remainName, listSubscribed, isVirtualView);

            if (childFolders != null)
            {
                result.addAll(childFolders);
            }
        }

        if (result.isEmpty())
        {
            return null;
        }

        return result;
    }

    /**
     * Renames an existing mailbox. The specified mailbox must already exist, the requested name must not exist already but must be able to be created and the user must have rights
     * to delete the existing mailbox and create a mailbox with the new name. Any inferior hierarchical names must also be renamed. If INBOX is renamed, the contents of INBOX are
     * transferred to a new mailbox with the new name, but INBOX is not deleted. If INBOX has inferior mailbox these are not renamed. This method serves RENAME command of the IMAP
     * protocol. <p/> Method searches mailbox under mount points defined for a specific user. Mount points include user's IMAP Virtualised Views and Email Archive Views.
     * 
     * @param user User making the request.
     * @param oldMailboxName String name of the existing folder
     * @param newMailboxName String target new name
     * @throws com.icegreen.greenmail.store.FolderException if an existing folder with the new name.
     * @throws AlfrescoImapFolderException if user does not have rights to create the new mailbox.
     */

    public void renameMailbox(GreenMailUser user, String oldMailboxName, String newMailboxName) throws FolderException, AuthorizationException
    {
        oldMailboxName = GreenMailUtil.convertFromUtf7(oldMailboxName);
        newMailboxName = GreenMailUtil.convertFromUtf7(newMailboxName);
        if (logger.isDebugEnabled())
        {
            logger.debug("Renaming folder: oldMailboxName=" + oldMailboxName + " newMailboxName=" + newMailboxName);
        }

        AlfrescoImapMailFolder sourceNode = (AlfrescoImapMailFolder) getFolder(user, GreenMailUtil.convertInUtf7(oldMailboxName));

        NodeRef root = imapHelper.getMailboxRootRef(oldMailboxName, user.getLogin());
        String mailboxRepoName = imapHelper.getMailPathInRepo(newMailboxName);

        StringTokenizer tokenizer = new StringTokenizer(mailboxRepoName, String.valueOf(AlfrescoImapConst.HIERARCHY_DELIMITER));

        NodeRef parentNodeRef = root;
        while (tokenizer.hasMoreTokens())
        {
            String folderName = tokenizer.nextToken();

            if (!tokenizer.hasMoreTokens())
            {
                try
                {
                    if (oldMailboxName.equalsIgnoreCase(AlfrescoImapConst.INBOX_NAME))
                    {
                        // If you trying to rename INBOX
                        // - just copy it to another folder with new name
                        // and leave INBOX (with children) intact.
                        fileFolderService.copy(sourceNode.getFolderInfo().getNodeRef(), parentNodeRef, folderName);
                        List<FileInfo> itemsForRemove = fileFolderService.list(sourceNode.getFolderInfo().getNodeRef());
                        for (FileInfo fileInfo : itemsForRemove)
                        {
                            fileFolderService.delete(fileInfo.getNodeRef());
                        }

                    }
                    else
                    {
                        fileFolderService.move(sourceNode.getFolderInfo().getNodeRef(), parentNodeRef, folderName);
                    }
                    return;
                }
                catch (FileExistsException e)
                {
                    throw new FolderException(FolderException.ALREADY_EXISTS_LOCALLY);
                }
                catch (FileNotFoundException e)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.error(e);
                    }
                }

            }
            else
            {
                List<FileInfo> folders = imapHelper.searchFolders(parentNodeRef, folderName, false, true);

                if (folders.size() == 0)
                {
                    AccessStatus status = imapHelper.hasPermission(parentNodeRef, PermissionService.WRITE);
                    if (status == AccessStatus.DENIED)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Creating folder: Cant't create folder - Permission denied");
                        }
                        throw new AlfrescoImapFolderException(AlfrescoImapFolderException.PERMISSION_DENIED);
                    }

                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Create mailBox: " + folderName);
                    }
                    FileFolderServiceImpl.makeFolders(fileFolderService, parentNodeRef, Arrays.asList(folderName), ContentModel.TYPE_FOLDER);
                }
                else
                {
                    parentNodeRef = folders.get(0).getNodeRef();
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("MailBox: " + folderName + " already exists");
                    }
                }
            }
        }

    }

    /**
     * Returns a reference to a newly created mailbox. The request should specify a mailbox that does not already exist on this server, that could exist on this server and that the
     * user has rights to create. This method serves CREATE command of the IMAP protocol.
     * 
     * @param user User making the request.
     * @param mailboxName String name of the target
     * @return an Mailbox reference.
     * @throws com.icegreen.greenmail.store.FolderException if mailbox already exists
     * @throws AlfrescoImapFolderException if user does not have rights to create the new mailbox.
     */
    public MailFolder createMailbox(GreenMailUser user, String mailboxName) throws AuthorizationException, FolderException
    {
        mailboxName = GreenMailUtil.convertFromUtf7(mailboxName);
        if (logger.isDebugEnabled())
        {
            logger.debug("Creating folder: " + mailboxName);
        }

        NodeRef root = imapHelper.getMailboxRootRef(mailboxName, user.getLogin());

        String mountPointName = imapHelper.getMountPointName(mailboxName);
        String mailboxRepoNam = imapHelper.getMailPathInRepo(mailboxName);
        StringTokenizer tokenizer = new StringTokenizer(mailboxRepoNam, String.valueOf(AlfrescoImapConst.HIERARCHY_DELIMITER));

        NodeRef parentNodeRef = root;

        while (tokenizer.hasMoreTokens())
        {
            String folderName = tokenizer.nextToken();

            List<FileInfo> folders = imapHelper.searchFolders(parentNodeRef, folderName, false, true);

            if (folders.size() == 0)
            {
                AccessStatus status = imapHelper.hasPermission(parentNodeRef, PermissionService.WRITE);
                if (status == AccessStatus.DENIED)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Creating folder: Cant't create folder - Permission denied");
                    }
                    throw new AlfrescoImapFolderException(AlfrescoImapFolderException.PERMISSION_DENIED);
                }

                if (logger.isDebugEnabled())
                {
                    logger.debug("Create mailBox: " + mailboxName);
                }
                FileInfo mailFolder = FileFolderServiceImpl.makeFolders(fileFolderService, parentNodeRef, Arrays.asList(folderName), ContentModel.TYPE_FOLDER);

                return new AlfrescoImapMailFolder(user.getQualifiedMailboxName(), mailFolder, folderName, imapHelper.getViewMode(mailboxName), root, mountPointName, imapHelper);

            }
            else
            {
                parentNodeRef = folders.get(0).getNodeRef();
                if (logger.isDebugEnabled())
                {
                    logger.debug("MailBox: " + folderName + " already exists");
                }
            }
        }

        throw new FolderException(FolderException.ALREADY_EXISTS_LOCALLY);
    }

    /**
     * Deletes an existing MailBox. Specified mailbox must already exist on this server, and the user must have rights to delete it. <p/> This method serves DELETE command of the
     * IMAP protocol.
     * 
     * @param user User making the request.
     * @param mailboxName String name of the target
     * @throws com.icegreen.greenmail.store.FolderException if mailbox has a non-selectable store with children
     */
    public void deleteMailbox(GreenMailUser user, String mailboxName) throws FolderException, AuthorizationException
    {
        AlfrescoImapMailFolder folder = (AlfrescoImapMailFolder) getFolder(user, mailboxName);
        NodeRef nodeRef = folder.getFolderInfo().getNodeRef();

        List<FileInfo> childFolders = imapHelper.searchFolders(nodeRef, "*", false, false);

        if (childFolders.isEmpty())
        {
            folder.signalDeletion();
            // Delete child folders and messages
            fileFolderService.delete(nodeRef);
        }
        else
        {
            if (folder.isSelectable())
            {
                // Delete all messages for this folder
                // Don't delete subfolders and their messages
                List<FileInfo> messages = imapHelper.searchFiles(nodeRef, "*", ImapModel.TYPE_IMAP_CONTENT, false);
                for (FileInfo message : messages)
                {
                    fileFolderService.delete(message.getNodeRef());
                }
                nodeService.addAspect(nodeRef, ImapModel.ASPECT_IMAP_FOLDER_NONSELECTABLE, null);
            }
            else
            {
                throw new FolderException(mailboxName + " - Can't delete a non-selectable store with children.");
            }
        }
    }

    /**
     * Returns a reference to an existing Mailbox. The requested mailbox must already exists on this server and the requesting user must have at least lookup rights. <p/> It is
     * also can be used by to obtain hierarchy delimiter by the LIST command: <p/> C: 2 list "" "" <p/> S: * LIST () "." "" <p/> S: 2 OK LIST completed. <p/> Method searches
     * mailbox under mount points defined for a specific user. Mount points include user's IMAP Virtualised Views and Email Archive Views.
     * 
     * @param user User making the request.
     * @param mailboxName String name of the target.
     * @return an Mailbox reference.
     */
    public MailFolder getFolder(GreenMailUser user, String mailboxName)
    {
        mailboxName = GreenMailUtil.convertFromUtf7(mailboxName);
        if (logger.isDebugEnabled())
        {
            logger.debug("Getting folder: " + mailboxName);
        }

        // If MailFolder object is used to obtain hierarchy delimiter by LIST command:
        // Example:
        // C: 2 list "" ""
        // S: * LIST () "." ""
        // S: 2 OK LIST completed.
        if ("".equals(mailboxName))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Request for the hierarchy delimiter");
            }
            return new AlfrescoImapMailFolder(user.getQualifiedMailboxName(), null, null, null, null, null, null);
        }

        NodeRef root = imapHelper.getMailboxRootRef(mailboxName, user.getLogin());
        String mountPointName = imapHelper.getMountPointName(mailboxName);
        String mailboxRepoName = imapHelper.getMailPathInRepo(mailboxName);

        StringTokenizer tokenizer = new StringTokenizer(mailboxRepoName, String.valueOf(AlfrescoImapConst.HIERARCHY_DELIMITER));
        int count = tokenizer.countTokens();
        NodeRef nodeRef = root;

        while (tokenizer.hasMoreTokens())
        {
            String t = tokenizer.nextToken();
            if (logger.isDebugEnabled())
            {
                logger.debug("token=" + t);
            }
            count--;

            List<FileInfo> list = imapHelper.searchFolders(nodeRef, t, false, true);

            if (count == 0)
            {
                if (!list.isEmpty())
                {

                    return new AlfrescoImapMailFolder(user.getQualifiedMailboxName(), list.get(0), list.get(0).getName(), imapHelper.getViewMode(mailboxName), root,
                            mountPointName, imapHelper);
                }
                else
                {
                    return new AlfrescoImapMailFolder(user.getQualifiedMailboxName(), null, null, null, null, null, null);
                }
            }
            else
            {
                if (!list.isEmpty())
                {
                    nodeRef = list.get(0).getNodeRef();
                }
                else
                {
                    return new AlfrescoImapMailFolder(user.getQualifiedMailboxName(), null, null, null, null, null, null);
                }
            }
        }

        throw new IllegalStateException("Error state");
    }

    /**
     * Simply calls {@link #getFolder(GreenMailUser, String)}. <p/> Added to implement {@link ImapHostManager}.
     */
    public MailFolder getFolder(GreenMailUser user, String mailboxName, boolean mustExist) throws FolderException
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
        return getFolder(user, AlfrescoImapConst.INBOX_NAME);
    }

    /**
     * Not supported. May be used by GreenMailUser.create() method. <p/> Added to implement {@link ImapHostManager}.
     */
    public void createPrivateMailAccount(GreenMailUser user) throws FolderException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Subscribes a user to a mailbox. The mailbox must exist locally and the user must have rights to modify it. <p/> This method serves SUBSCRIBE command of the IMAP protocol.
     * 
     * @param user User making the request
     * @param mailbox String representation of a mailbox name.
     */
    public void subscribe(final GreenMailUser user, final String mailbox) throws FolderException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Subscribing: " + mailbox);
        }
        AlfrescoImapMailFolder mailFolder = (AlfrescoImapMailFolder) getFolder(user, mailbox);
        nodeService.addAspect(mailFolder.getFolderInfo().getNodeRef(), ImapModel.ASPECT_IMAP_FOLDER_SUBSCRIBED, null);
//        This is a multiuser support. Commented due new requirements
//        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
//        {
//            public Void doWork() throws Exception
//            {
//                AlfrescoImapMailFolder mailFolder = (AlfrescoImapMailFolder) getFolder(user, mailbox);
//                FileInfo fileInfo = mailFolder.getFolderInfo();
//                if (fileInfo != null)
//                {
//                    String subscribedList = (String) nodeService.getProperty(fileInfo.getNodeRef(), ImapModel.PROP_IMAP_FOLDER_SUBSCRIBED);
//                    if (subscribedList == null)
//                    {
//                        subscribedList = "";
//                    }
//                    subscribedList = subscribedList.replaceAll(imapHelper.formatUserEntry(user.getLogin()), "");
//                    subscribedList += imapHelper.formatUserEntry(user.getLogin());
//                    nodeService.setProperty(fileInfo.getNodeRef(), ImapModel.PROP_IMAP_FOLDER_SUBSCRIBED, subscribedList);
//                }
//                else
//                {
//                    logger.debug("MailBox: " + mailbox + "doesn't exsist. Maybe it was deleted earlier.");
//                }
//                return null;
//            }
//        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Unsubscribes from a given mailbox. <p/> This method serves UNSUBSCRIBE command of the IMAP protocol.
     * 
     * @param user User making the request
     * @param mailbox String representation of a mailbox name.
     */
    public void unsubscribe(final GreenMailUser user, final String mailbox) throws FolderException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Unsubscribing: " + mailbox);
        }
        AlfrescoImapMailFolder mailFolder = (AlfrescoImapMailFolder) getFolder(user, mailbox);
        nodeService.removeAspect(mailFolder.getFolderInfo().getNodeRef(), ImapModel.ASPECT_IMAP_FOLDER_SUBSCRIBED);
        
//        This is a multiuser support. Commented due new requirements
//        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
//        {
//            public Void doWork() throws Exception
//            {
//                AlfrescoImapMailFolder mailFolder = (AlfrescoImapMailFolder) getFolder(user, mailbox);
//                if (mailFolder.getFolderInfo() != null)
//                {
//                    FileInfo fileInfo = mailFolder.getFolderInfo();
//                    String subscribedList = (String) nodeService.getProperty(fileInfo.getNodeRef(), ImapModel.PROP_IMAP_FOLDER_SUBSCRIBED);
//                    if (subscribedList == null)
//                    {
//                        subscribedList = "";
//                    }
//                    subscribedList = subscribedList.replaceAll(imapHelper.formatUserEntry(user.getLogin()), "");
//                    nodeService.setProperty(fileInfo.getNodeRef(), ImapModel.PROP_IMAP_FOLDER_SUBSCRIBED, subscribedList);
//                }
//                else
//                {
//                    logger.debug("MailBox: " + mailbox + " doesn't exsist. Maybe it was deleted earlier.");
//                }
//
//                return null;
//            }
//        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Not supported. Used by GreenMail class.
     */
    public List getAllMessages()
    {
        throw new UnsupportedOperationException();
    }
    
    private boolean isSubscribed(FileInfo fileInfo, String userName)
    {
        return nodeService.hasAspect(fileInfo.getNodeRef(), ImapModel.ASPECT_IMAP_FOLDER_SUBSCRIBED);
//        This is a multiuser support. Commented due new requirements

//        Map<QName, Serializable> properties = fileInfo.getProperties();
//        String subscribedList = (String) properties.get(ImapModel.PROP_IMAP_FOLDER_SUBSCRIBED);
//        if (subscribedList == null)
//        {
//            return false;
//        }
//        else
//        {
//            return subscribedList.contains(imapHelper.formatUserEntry(userName));
//        }

    }

    private Collection<FileInfo> getSubscribed(Collection<FileInfo> list, String userName)
    {
        Collection<FileInfo> result = new LinkedList<FileInfo>();

        for (FileInfo folderInfo : list)
        {
            if (isSubscribed(folderInfo, userName))
            {
                result.add(folderInfo);
            }
        }

        return result;
    }

    private boolean hasSubscribedChild(FileInfo parent, String userName, boolean isVirtualView)
    {
        List<FileInfo> list = imapHelper.searchFolders(parent.getNodeRef(), "*", true, isVirtualView);

        for (FileInfo fileInfo : list)
        {
            if (isSubscribed(fileInfo, userName))
            {
                return true;
            }
        }

        return false;
    }

    private Collection<MailFolder> createMailFolderList(GreenMailUser user, Collection<FileInfo> list, NodeRef imapUserHomeRef)
    {
        Collection<MailFolder> result = new LinkedList<MailFolder>();

        for (FileInfo folderInfo : list)
        {
            // folderName, viewMode, mountPointName will be setted in listSubscribedMailboxes() method
            result.add(new AlfrescoImapMailFolder(user.getQualifiedMailboxName(), folderInfo, null, null, imapUserHomeRef, null, imapHelper));
        }

        return result;

    }

    // ----------------------Getters and Setters----------------------------

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setImapHelper(ImapHelper imapHelper)
    {
        this.imapHelper = imapHelper;
    }

}
