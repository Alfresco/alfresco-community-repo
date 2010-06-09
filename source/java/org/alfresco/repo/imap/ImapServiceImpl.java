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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.Flags;
import javax.mail.Flags.Flag;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.imap.AlfrescoImapConst.ImapViewMode;
import org.alfresco.repo.imap.config.ImapConfigMountPointsBean;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.site.SiteServiceException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.SubFolderFilter;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.Utf7;
import org.alfresco.util.config.RepositoryFolderConfigBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author Dmitry Vaserin
 * @author Arseny Kovalchuk
 * @since 3.2
 */
public class ImapServiceImpl implements ImapService
{
    private Log logger = LogFactory.getLog(ImapServiceImpl.class);

    private static final String ERROR_PERMISSION_DENIED = "imap.server.error.permission_denied";
    private static final String ERROR_FOLDER_ALREADY_EXISTS = "imap.server.error.folder_already_exist";
    private static final String ERROR_MAILBOX_NAME_IS_MANDATORY = "imap.server.error.mailbox_name_is_mandatory";
    private static final String ERROR_CANNOT_GET_A_FOLDER = "imap.server.error.cannot_get_a_folder";

    private SysAdminParams sysAdminParams;
    private FileFolderService fileFolderService;
    private NodeService nodeService;
    private PermissionService permissionService;
    private ServiceRegistry serviceRegistry;

    private Map<String, ImapConfigMountPointsBean> imapConfigMountPoints;
    private RepositoryFolderConfigBean[] ignoreExtractionFoldersBeans;
    private RepositoryFolderConfigBean imapHomeConfigBean;

    private NodeRef imapHomeNodeRef;
    private Set<NodeRef> ignoreExtractionFolders;

    private String defaultFromAddress;
    private String repositoryTemplatePath;
    private boolean extractAttachmentsEnabled = true;

    private final static Map<QName, Flags.Flag> qNameToFlag;
    private final static Map<Flags.Flag, QName> flagToQname;

    static
    {
        qNameToFlag = new HashMap<QName, Flags.Flag>();
        qNameToFlag.put(ImapModel.PROP_FLAG_ANSWERED, Flags.Flag.ANSWERED);
        qNameToFlag.put(ImapModel.PROP_FLAG_DELETED, Flags.Flag.DELETED);
        qNameToFlag.put(ImapModel.PROP_FLAG_DRAFT, Flags.Flag.DRAFT);
        qNameToFlag.put(ImapModel.PROP_FLAG_SEEN, Flags.Flag.SEEN);
        qNameToFlag.put(ImapModel.PROP_FLAG_RECENT, Flags.Flag.RECENT);
        qNameToFlag.put(ImapModel.PROP_FLAG_FLAGGED, Flags.Flag.FLAGGED);

        flagToQname = new HashMap<Flags.Flag, QName>();
        flagToQname.put(Flags.Flag.ANSWERED, ImapModel.PROP_FLAG_ANSWERED);
        flagToQname.put(Flags.Flag.DELETED, ImapModel.PROP_FLAG_DELETED);
        flagToQname.put(Flags.Flag.DRAFT, ImapModel.PROP_FLAG_DRAFT);
        flagToQname.put(Flags.Flag.SEEN, ImapModel.PROP_FLAG_SEEN);
        flagToQname.put(Flags.Flag.RECENT, ImapModel.PROP_FLAG_RECENT);
        flagToQname.put(Flags.Flag.FLAGGED, ImapModel.PROP_FLAG_FLAGGED);
    }

    /**
     * Bootstrap initialization bean for the service implementation.
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    public static class ImapServiceBootstrap extends AbstractLifecycleBean
    {
        private ImapServiceImpl service;
        private boolean imapServerEnabled;

        public void setService(ImapServiceImpl service)
        {
            this.service = service;
        }

        public void setImapServerEnabled(boolean imapServerEnabled)
        {
            this.imapServerEnabled = imapServerEnabled;
        }

        @Override
        protected void onBootstrap(ApplicationEvent event)
        {
            if (imapServerEnabled)
            {
                service.startup();
            }
        }

        @Override
        protected void onShutdown(ApplicationEvent event)
        {
            if (imapServerEnabled)
            {
                service.shutdown();
            }
        }
    }

    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }

    public FileFolderService getFileFolderService()
    {
        return fileFolderService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public PermissionService getPermissionService()
    {
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setImapHome(RepositoryFolderConfigBean imapHomeConfigBean)
    {
        this.imapHomeConfigBean = imapHomeConfigBean;
    }

    public String getDefaultFromAddress()
    {
        return defaultFromAddress;
    }

    public void setDefaultFromAddress(String defaultFromAddress)
    {
        this.defaultFromAddress = defaultFromAddress;
    }

    public String getWebApplicationContextUrl()
    {
        return sysAdminParams.getAlfrescoProtocol() + "://" + sysAdminParams.getAlfrescoHost() + ":" + sysAdminParams.getAlfrescoPort() + "/" + sysAdminParams.getAlfrescoContext();
    }

    public String getRepositoryTemplatePath()
    {
        return repositoryTemplatePath;
    }

    public void setRepositoryTemplatePath(String repositoryTemplatePath)
    {
        this.repositoryTemplatePath = repositoryTemplatePath;
    }

    public void setImapConfigMountPoints(ImapConfigMountPointsBean[] imapConfigMountPointsBeans)
    {
        this.imapConfigMountPoints = new LinkedHashMap<String, ImapConfigMountPointsBean>(imapConfigMountPointsBeans.length * 2);
        for (ImapConfigMountPointsBean bean : imapConfigMountPointsBeans)
        {
            this.imapConfigMountPoints.put(bean.getMountPointName(), bean);
        }
    }

    public void setIgnoreExtractionFolders(final RepositoryFolderConfigBean[] ignoreExtractionFolders)
    {
        this.ignoreExtractionFoldersBeans = ignoreExtractionFolders;
    }

    public void setExtractAttachmentsEnabled(boolean extractAttachmentsEnabled)
    {
        this.extractAttachmentsEnabled = extractAttachmentsEnabled;
    }

    // ---------------------- Lifecycle Methods ------------------------------

    public void init()
    {
        PropertyCheck.mandatory(this, "imapConfigMountPoints", imapConfigMountPoints);
        PropertyCheck.mandatory(this, "ignoreExtractionFoldersBeans", ignoreExtractionFoldersBeans);
        PropertyCheck.mandatory(this, "imapHome", imapHomeConfigBean);
        
        PropertyCheck.mandatory(this, "fileFolderService", fileFolderService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "permissionService", permissionService);
        PropertyCheck.mandatory(this, "serviceRegistry", serviceRegistry);
        PropertyCheck.mandatory(this, "defaultFromAddress", defaultFromAddress);
        PropertyCheck.mandatory(this, "repositoryTemplatePath", repositoryTemplatePath);
    }

    public void startup()
    {
        final NamespaceService namespaceService = serviceRegistry.getNamespaceService();
        final SearchService searchService = serviceRegistry.getSearchService();
        
        // Hit the mount points for early failure
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                getMountPoints();
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
        
        // Get NodeRefs for folders to ignore
        this.ignoreExtractionFolders = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Set<NodeRef>>()
        {
            public Set<NodeRef> doWork() throws Exception
            {
                Set<NodeRef> result = new HashSet<NodeRef>(ignoreExtractionFoldersBeans.length * 2);

                for (RepositoryFolderConfigBean ignoreExtractionFoldersBean : ignoreExtractionFoldersBeans)
                {
                    NodeRef nodeRef = ignoreExtractionFoldersBean.getFolderPath(
                            namespaceService, nodeService, searchService, fileFolderService);

                    if (!result.add(nodeRef))
                    {
                        // It was already in the set
                        throw new AlfrescoRuntimeException(
                                "The folder extraction path has been referenced already: \n" +
                                "   Folder: " + ignoreExtractionFoldersBean);
                    }
                }

                return result;
            }
        }, AuthenticationUtil.getSystemUserName());
        
        // Locate or create IMAP home
        imapHomeNodeRef = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                return imapHomeConfigBean.getOrCreateFolderPath(namespaceService, nodeService, searchService, fileFolderService);
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    public void shutdown()
    {
    }

    // ---------------------- Service Methods --------------------------------

    public List<AlfrescoImapFolder> listSubscribedMailboxes(AlfrescoImapUser user, String mailboxPattern)
    {
        mailboxPattern = Utf7.decode(mailboxPattern, Utf7.UTF7_MODIFIED);

        if (logger.isDebugEnabled())
        {
            logger.debug("Listing subscribed mailboxes: mailboxPattern=" + mailboxPattern);
        }
        mailboxPattern = getMailPathInRepo(mailboxPattern);
        if (logger.isDebugEnabled())
        {
            logger.debug("Listing subscribed mailboxes: mailboxPattern in alfresco=" + mailboxPattern);
        }
        return listMailboxes(user, mailboxPattern, true);
    }

    public List<AlfrescoImapFolder> listMailboxes(AlfrescoImapUser user, String mailboxPattern)
    {
        mailboxPattern = Utf7.decode(mailboxPattern, Utf7.UTF7_MODIFIED);

        if (logger.isDebugEnabled())
        {
            logger.debug("Listing  mailboxes: mailboxPattern=" + mailboxPattern);
        }
        mailboxPattern = getMailPathInRepo(mailboxPattern);
        if (logger.isDebugEnabled())
        {
            logger.debug("Listing  mailboxes: mailboxPattern in alfresco=" + mailboxPattern);
        }

        return listMailboxes(user, mailboxPattern, false);
    }

    public AlfrescoImapFolder createMailbox(AlfrescoImapUser user, String mailboxName)
    {
        if (mailboxName == null)
        {
            throw new IllegalArgumentException(I18NUtil.getMessage(ERROR_MAILBOX_NAME_IS_MANDATORY));
        }
        mailboxName = Utf7.decode(mailboxName, Utf7.UTF7_MODIFIED);
        if (logger.isDebugEnabled())
        {
            logger.debug("Create mailbox: " + mailboxName);
        }
        NodeRef root = getMailboxRootRef(mailboxName, user.getLogin());
        NodeRef parentNodeRef = root; // it is used for hierarhy deep search.
        for (String folderName : getMailPathInRepo(mailboxName).split(String.valueOf(AlfrescoImapConst.HIERARCHY_DELIMITER)))
        {
            NodeRef child = fileFolderService.searchSimple(parentNodeRef, folderName);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Trying to create folder '" + folderName + "'");
            }
            if (child == null)
            {
                // folder doesn't exist
                AccessStatus status = permissionService.hasPermission(parentNodeRef, PermissionService.CREATE_CHILDREN);
                if (status == AccessStatus.DENIED)
                {
                    throw new AlfrescoRuntimeException(ERROR_PERMISSION_DENIED);
                }
                FileInfo mailFolder = serviceRegistry.getFileFolderService().create(parentNodeRef, folderName, ContentModel.TYPE_FOLDER);
                return new AlfrescoImapFolder(
                        user.getQualifiedMailboxName(),
                        mailFolder,
                        folderName,
                        getViewMode(mailboxName),
                        root,
                        getMountPointName(mailboxName),
                        isExtractionEnabled(mailFolder.getNodeRef()),
                        serviceRegistry);
            }
            else
            {
                // folder already exists
                if (logger.isDebugEnabled())
                {
                    logger.debug("Folder '" + folderName + "' already exists");
                }
                // next search from new parent
                parentNodeRef = child;
            }
        }
        throw new AlfrescoRuntimeException(ERROR_FOLDER_ALREADY_EXISTS);
    }

    public void deleteMailbox(AlfrescoImapUser user, String mailboxName)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Delete mailbox: mailboxName=" + mailboxName);
        }
        if (mailboxName == null)
        {
            throw new IllegalArgumentException(I18NUtil.getMessage(ERROR_MAILBOX_NAME_IS_MANDATORY));
        }

        AlfrescoImapFolder folder = getFolder(user, mailboxName);
        NodeRef nodeRef = folder.getFolderInfo().getNodeRef();
        
        List<FileInfo> childFolders = fileFolderService.listFolders(nodeRef);

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
                List<FileInfo> messages = fileFolderService.listFiles(nodeRef);
                for (FileInfo message : messages)
                {
                    fileFolderService.delete(message.getNodeRef());
                }
                nodeService.addAspect(nodeRef, ImapModel.ASPECT_IMAP_FOLDER_NONSELECTABLE, null);
            }
            else
            {
                throw new AlfrescoRuntimeException(mailboxName + " - Can't delete a non-selectable store with children.");
            }
        }
    }

    public void renameMailbox(AlfrescoImapUser user, String oldMailboxName, String newMailboxName)
    {
        if (oldMailboxName == null || newMailboxName == null)
        {
            throw new IllegalArgumentException(ERROR_MAILBOX_NAME_IS_MANDATORY);
        }
        oldMailboxName = Utf7.decode(oldMailboxName, Utf7.UTF7_MODIFIED);
        newMailboxName = Utf7.decode(newMailboxName, Utf7.UTF7_MODIFIED);
        if (logger.isDebugEnabled())
        {
            logger.debug("Renaming folder oldMailboxName=" + oldMailboxName + " newMailboxName=" + newMailboxName);
        }

        AlfrescoImapFolder sourceNode = getFolder(user, oldMailboxName);

        NodeRef root = getMailboxRootRef(oldMailboxName, user.getLogin());
        String[] folderNames = getMailPathInRepo(newMailboxName).split(String.valueOf(AlfrescoImapConst.HIERARCHY_DELIMITER));
        String folderName = null;
        NodeRef parentNodeRef = root; // initial root for search
        try
        {
            for (int i = 0; i < folderNames.length; i++)
            {
                folderName = folderNames[i];
                if (i == (folderNames.length - 1)) // is it the last element
                {
                    if (oldMailboxName.equalsIgnoreCase(AlfrescoImapConst.INBOX_NAME))
                    {
                        // If you trying to rename INBOX
                        // - just copy it to another folder with new name
                        // and leave INBOX (with children) intact.
                        fileFolderService.copy(sourceNode.getFolderInfo().getNodeRef(), parentNodeRef, folderName);
                    }
                    else
                    {
                        fileFolderService.move(sourceNode.getFolderInfo().getNodeRef(), parentNodeRef, folderName);
                    }
                }
                else
                {
                    // not last element than checks if it exists and creates if doesn't
                    NodeRef child = fileFolderService.searchSimple(parentNodeRef, folderName);
                                      
                    if (child == null)
                    {
                        // check creation permission
                        AccessStatus status = permissionService.hasPermission(parentNodeRef, PermissionService.CREATE_CHILDREN);
                        if (status == AccessStatus.DENIED)
                        {
                            throw new AlfrescoRuntimeException(ERROR_PERMISSION_DENIED);
                        }

                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Creating folder '" + folderName + "'");
                        }
                        serviceRegistry.getFileFolderService().create(parentNodeRef, folderName, ContentModel.TYPE_FOLDER);
                    }
                    else
                    {
                        parentNodeRef = child;
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Folder '" + folderName + "' already exists");
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            if (e instanceof AlfrescoRuntimeException)
            {
                throw (AlfrescoRuntimeException) e;
            }
            else
            {
                throw new AlfrescoRuntimeException(e.getMessage(), e);
            }
        }
    }

    /**
     * Get Folder
     * @param user
     * @param mailboxName
     */
    public AlfrescoImapFolder getFolder(AlfrescoImapUser user, String mailboxName)
    {
        mailboxName = Utf7.decode(mailboxName, Utf7.UTF7_MODIFIED);
        if (logger.isDebugEnabled())
        {
            logger.debug("Get folder '" + mailboxName + "'");
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
            return new AlfrescoImapFolder(user.getQualifiedMailboxName(), serviceRegistry);
        }

        ImapViewMode viewMode = getViewMode(mailboxName);
        String mountPointName = getMountPointName(mailboxName);
        
        NodeRef root = getMailboxRootRef(mailboxName, user.getLogin());
        NodeRef nodeRef = root; // initial top folder
        
        String[] folderNames = getMailPathInRepo(mailboxName).split(String.valueOf(AlfrescoImapConst.HIERARCHY_DELIMITER));
        
        if(folderNames.length == 1 && folderNames[0].length() == 0)
        {
            // This is the root of the mount point e.g "Alfresco IMAP" which has a path from root of ""
            FileInfo folderFileInfo = fileFolderService.getFileInfo(root);
            
            AlfrescoImapFolder folder = new AlfrescoImapFolder(
                    user.getQualifiedMailboxName(),
                    folderFileInfo,
                    mountPointName,
                    viewMode,
                    root,
                    mountPointName,
                    isExtractionEnabled(folderFileInfo.getNodeRef()),
                    serviceRegistry);
        
            if (logger.isDebugEnabled())
            {
                logger.debug("Returning root folder '" + mailboxName + "'");
            }
            return folder;
        }
      
        for (int i = 0; i < folderNames.length; i++)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Processing of " + folderNames[i]);
            } 
            
            NodeRef targetNode = fileFolderService.searchSimple(nodeRef, folderNames[i]);
            
            if (targetNode == null)
            {
                AlfrescoImapFolder folder = new AlfrescoImapFolder(user.getQualifiedMailboxName(), serviceRegistry);
                if (logger.isDebugEnabled())
                {
                    logger.debug("Returning empty folder '" + folderNames[i]);
                }
                return folder;
            }
            
            if (i == (folderNames.length - 1)) // is last
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("found folder to list ");
                }
                
                FileInfo folderFileInfo = fileFolderService.getFileInfo(targetNode);
                
                AlfrescoImapFolder folder = new AlfrescoImapFolder(
                        user.getQualifiedMailboxName(),
                        folderFileInfo,
                        folderFileInfo.getName(),
                        viewMode,
                        root,
                        mountPointName,
                        isExtractionEnabled(folderFileInfo.getNodeRef()),
                        serviceRegistry);
            
                if (logger.isDebugEnabled())
                {
                    logger.debug("Returning folder '" + mailboxName + "'");
                }
                return folder;
            }
  
            /**
             * End of loop - next element in path
             */
            nodeRef = targetNode;
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Cannot get a folder '" + mailboxName + "'");
        }
        throw new AlfrescoRuntimeException(ERROR_CANNOT_GET_A_FOLDER, new String[] { mailboxName });
    }
    
    /**
     * Deep search for mailboxes/folders in the specified context
     * 
     * Certain folders are excluded depending upon the view mode.
     * - For ARCHIVE mode all Share Sites are excluded.
     * - For MIXED and VIRTUAL non favourite sites are excluded.
     * 
     * @param contextNodeRef context folder for search
     * @param viewMode is folder in "Virtual" View
     * @return list of mailboxes/folders
     */
    private List<FileInfo> searchDeep(final NodeRef contextNodeRef, final ImapViewMode viewMode)
    {     
        if (logger.isDebugEnabled())
        {
            logger.debug("searchDeep start " + contextNodeRef + ", " + viewMode);
        }
        
        /**
         * Exclude Share Sites of TYPE_SITE
         */
        final Collection<QName> typesToExclude = getServiceRegistry().getDictionaryService().getSubTypes(SiteModel.TYPE_SITE, true);
        
        /**
         * Share Site Exclusion Filter
         */
        SubFolderFilter filter =  new SubFolderFilter()
        {
            List<NodeRef> favs = null;
            public boolean isEnterSubfolder(ChildAssociationRef subfolderRef)
            {
              
               NodeRef folder = subfolderRef.getChildRef();
               QName typeOfFolder = nodeService.getType(folder);
               if(typesToExclude.contains(typeOfFolder))
               {
                   if (viewMode == ImapViewMode.VIRTUAL || viewMode == ImapViewMode.MIXED)
                   {
                       /**
                        * In VIRTUAL and MIXED MODE WE SHOULD ONLY DISPLAY FOLDERS FROM FAVOURITE SITES
                        */
                       if(favs == null)
                       {
                           favs = getFavouriteSites(getCurrentUser());
                       }
                       
                       if(favs.contains(folder))
                       {
                           logger.debug("searchDeep (VIRTUAL) including fav site folder :" + subfolderRef.getQName());
                           return true;
                       }
                       else
                       {
                           logger.debug("searchDeep (VIRTUAL) excluding non fav site folder :" + subfolderRef.getQName());
                           return false;
                       }
                   }
                   else
                   {
                       /**
                        * IN ARCHIVE MODE we don't display folders for any SITES, regardless of whether they are favourites.
                        */
                       logger.debug("searchDeep (ARCHIVE) excluding site folder :" + subfolderRef.getQName());
                       return false;
                   }
               }
               return true;
            }
       }; 
            
        
        List<FileInfo> searchResult = fileFolderService.listDeepFolders(contextNodeRef, filter);        
        
        if (logger.isDebugEnabled())
        {
            logger.debug("SearchDeep folders return at end");
        }
        return new ArrayList<FileInfo>(searchResult);
    }


    /**
     * Shallow search for mailboxes in specified context
     * 
     * @param contextNodeRef context folder for search
     * @param namePattern name pattern for search
     * @param viewMode is folder in "Virtual" View
     * @return list of mailboxes
     */
    private List<FileInfo> searchFolders(NodeRef contextNodeRef, String namePattern, ImapViewMode viewMode)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Search folders, contextNodeRef" + contextNodeRef + " namePattern, " + namePattern);
        }
        
        List<FileInfo> searchResult; 
        
        /**
         * Shallow search for all folders below contextNodeRef
         */
        if("*".equals(namePattern))
        {
            /**
             * This is a simple listing of all folders below contextNodeRef
             */
            searchResult = fileFolderService.listFolders(contextNodeRef);
        }
        else
        {
            // MER TODO I'm not sure we ever get here in real use of IMAP.   But if we do then the use of this 
            // deprecated method needs to be re-worked. 
            searchResult = fileFolderService.search(contextNodeRef, namePattern, false, true, false);
        }
        
        
// DO WE NEED TO WORRY ABOUT THE STUFF BELOW ?  
// WOULD ONLY BE RELEVANT if ContextNodeRef is site root.        
// IF contextNodeRef is the imap home or the contextNodeRef is in the depths of a non favourite site.
//        
//        Set<FileInfo> result = new HashSet<FileInfo>(searchResult);
//        if (viewMode == ImapViewMode.VIRTUAL || viewMode == ImapViewMode.MIXED)
//        {
//            /**
//             * In VIRTUAL and MIXED MODE WE SHOULD ONLY DISPLAY FAVOURITE SITES
//             */
//            List<SiteInfo> nonFavSites = getNonFavouriteSites(getCurrentUser());
//            for (SiteInfo siteInfo : nonFavSites)
//            {
//                FileInfo nonFavSite = fileFolderService.getFileInfo(siteInfo.getNodeRef());
//                
//                // search deep for all folders in the site
//                List<FileInfo> siteChilds = fileFolderService.search(nonFavSite.getNodeRef(), namePattern, false, true, true);
//                result.removeAll(siteChilds);
//                result.remove(nonFavSite);
//            }
//
//        }
//        else
//        {
//            /**
//             * IN ARCHIVE MODE we don't display folders any SITES
//             */
//            // Remove folders from Sites
//            List<SiteInfo> sites = serviceRegistry.getTransactionService().getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<List<SiteInfo>>()
//            {
//                public List<SiteInfo> execute() throws Exception
//                {
//                    List<SiteInfo> res = new ArrayList<SiteInfo>();
//                    try
//                    {
//
//                        res = serviceRegistry.getSiteService().listSites(getCurrentUser());
//                    }
//                    catch (SiteServiceException e)
//                    {
//                        // Do nothing. Root sites folder was not created.
//                        if (logger.isWarnEnabled())
//                        {
//                            logger.warn("Root sites folder was not created.");
//                        }
//                    }
//                    catch (InvalidNodeRefException e)
//                    {
//                        // Do nothing. Root sites folder was deleted.
//                        if (logger.isWarnEnabled())
//                        {
//                            logger.warn("Root sites folder was deleted.");
//                        }
//                    }
//                    
//                    if (logger.isDebugEnabled())
//                    {
//                        logger.debug("Search folders return ");
//                    }
//
//                    return res;
//                }
//            }, false, true);
//            
//            for (SiteInfo siteInfo : sites)
//            {
//                List<FileInfo> siteChilds = fileFolderService.search(siteInfo.getNodeRef(), namePattern, false, true, true);
//                result.removeAll(siteChilds);
//                // remove site
//                result.remove(fileFolderService.getFileInfo(siteInfo.getNodeRef()));
//            }
//
//        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Search folders return at end, namePattern:" + namePattern);
        }
       
        return searchResult;
    }

    /**
     * Search for emails in specified folder depending on view mode.
     * 
     * Shallow list of files
     * 
     * @param contextNodeRef context folder for search
     * @param viewMode context folder view mode
     * @return list of emails that context folder contains.
     */
    public List<FileInfo> searchMails(NodeRef contextNodeRef, ImapViewMode viewMode)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Search mails namePattern," + contextNodeRef + ", " + viewMode );
        }
        
        List<FileInfo> searchResult = fileFolderService.listFiles(contextNodeRef);
        
        List<FileInfo> result = new LinkedList<FileInfo>();
        //List<FileInfo> searchResult = fileFolderService.search(contextNodeRef, namePattern, true, false, includeSubFolders);
        switch (viewMode)
        {
        case MIXED:
            result = searchResult;
            break;
        case ARCHIVE:
            for (FileInfo fileInfo : searchResult)
            {
                if (nodeService.hasAspect(fileInfo.getNodeRef(), ImapModel.ASPECT_IMAP_CONTENT))
                {
                    result.add(fileInfo);
                }
            }
            break;
        case VIRTUAL:
            for (FileInfo fileInfo : searchResult)
            {
                if (!nodeService.hasAspect(fileInfo.getNodeRef(), ImapModel.ASPECT_IMAP_CONTENT))
                {
                    result.add(fileInfo);
                }
            }
            break;
        }

        logger.debug("found files:" + result.size());
        return result;
    }

    public void subscribe(AlfrescoImapUser user, String mailbox)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Subscribing: " + user + ", " + mailbox);
        }
        AlfrescoImapFolder mailFolder = getFolder(user, mailbox);
        nodeService.removeAspect(mailFolder.getFolderInfo().getNodeRef(), ImapModel.ASPECT_IMAP_FOLDER_NONSUBSCRIBED);
    }

    public void unsubscribe(AlfrescoImapUser user, String mailbox)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Unsubscribing: " + user + ", " + mailbox);
        }
        AlfrescoImapFolder mailFolder = getFolder(user, mailbox);
        if(mailFolder.getFolderInfo() != null)
        {
            logger.debug("unsubscribing by ASPECT_IMAP_FOLDER_NONSUBSCRIBED");
            nodeService.addAspect(mailFolder.getFolderInfo().getNodeRef(), ImapModel.ASPECT_IMAP_FOLDER_NONSUBSCRIBED, null);
        }
        else
        {
            // perhaps the folder has been deleted by another async process?
            logger.debug("Unable to find folder to unsubscribe");
        }
    }

    /**
     * Return flags that belong to the specified imap folder.
     * 
     * @param messageInfo imap folder info.
     * @return flags.
     */
    public synchronized Flags getFlags(FileInfo messageInfo)
    {
        Flags flags = new Flags();
        checkForFlaggableAspect(messageInfo.getNodeRef());
        Map<QName, Serializable> props = nodeService.getProperties(messageInfo.getNodeRef());

        for (QName key : qNameToFlag.keySet())
        {
            Boolean value = (Boolean) props.get(key);
            if (value != null && value)
            {
                flags.add(qNameToFlag.get(key));
            }
        }
        return flags;
    }

    /**
     * Set flags to the specified imapFolder.
     * 
     * @param messageInfo FileInfo of imap Folder.
     * @param flags flags to set.
     * @param value value to set.
     */
    public synchronized void setFlags(FileInfo messageInfo, Flags flags, boolean value)
    {
        checkForFlaggableAspect(messageInfo.getNodeRef());
        

        for (Flags.Flag flag : flags.getSystemFlags())
        {
            setFlag(messageInfo, flag, value);
        }
    }

    /**
     * Set flags to the specified imapFolder.
     * 
     * @param messageInfo FileInfo of imap Folder
     * @param flag flag to set.
     * @param value value value to set.
     */
    public void setFlag(FileInfo messageInfo, Flag flag, boolean value)
    {
        NodeRef nodeRef = messageInfo.getNodeRef();
        checkForFlaggableAspect(nodeRef);
        AccessStatus status = permissionService.hasPermission(nodeRef, PermissionService.WRITE_PROPERTIES);
        if (status == AccessStatus.DENIED)
        {
            logger.debug("checkForFlaggableAspect - no permissions to add FLAG " + nodeRef);
            //TODO should we throw an exception here?
        }
        else
        {
            nodeService.setProperty(messageInfo.getNodeRef(), flagToQname.get(flag), value);
        }
    }

    /**
     * Depend on listSubscribed param, list Mailboxes or list subscribed Mailboxes
     */
    private List<AlfrescoImapFolder> listMailboxes(AlfrescoImapUser user, String mailboxPattern, boolean listSubscribed)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("listMailboxes" + user + ", mailboxPattern:" + mailboxPattern + ", listSubscribed:" + listSubscribed);
        }
        List<AlfrescoImapFolder> result = new LinkedList<AlfrescoImapFolder>();

        Map<String, NodeRef> mountPoints = getMountPoints();

        NodeRef mountPoint;

        // List mailboxes that are in mount points
        for (String mountPointName : mountPoints.keySet())
        {

            mountPoint = mountPoints.get(mountPointName);
            FileInfo mountPointFileInfo = fileFolderService.getFileInfo(mountPoint);
            NodeRef mountParent = nodeService.getParentAssocs(mountPoint).get(0).getParentRef();
            ImapViewMode viewMode = imapConfigMountPoints.get(mountPointName).getMode();

            if (!mailboxPattern.equals("*"))
            {
                mountPoint = mountParent;
            }

            List<AlfrescoImapFolder> folders = listFolder(mountPoint, mountPoint, user, mailboxPattern, listSubscribed, viewMode);
            if (folders != null)
            {
                for (AlfrescoImapFolder mailFolder : folders)
                {
                    AlfrescoImapFolder folder = (AlfrescoImapFolder) mailFolder;
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
                    result.add(
                            new AlfrescoImapFolder(
                                    user.getQualifiedMailboxName(),
                                    mountPointFileInfo,
                                    mountPointName,
                                    viewMode,
                                    mountParent,
                                    mountPointName,
                                    isExtractionEnabled(mountPointFileInfo.getNodeRef()),
                                    serviceRegistry));
                }
                // \NoSelect
                else if (listSubscribed && hasSubscribedChild(mountPointFileInfo, user.getLogin(), viewMode))
                {
                    result.add(
                            new AlfrescoImapFolder(
                                    user.getQualifiedMailboxName(),
                                    mountPointFileInfo,
                                    mountPointName,
                                    viewMode,
                                    mountParent,
                                    mountPointName,
                                    serviceRegistry,
                                    false,
                                    isExtractionEnabled(mountPointFileInfo.getNodeRef())));
                }
            }
            
            
        }

        // List mailboxes that are in user IMAP Home
        NodeRef root = getUserImapHomeRef(user.getLogin());
        List<AlfrescoImapFolder> imapFolders = listFolder(root, root, user, mailboxPattern, listSubscribed, ImapViewMode.ARCHIVE);

        if (imapFolders != null)
        {
            for (AlfrescoImapFolder mailFolder : imapFolders)
            {
                AlfrescoImapFolder folder = (AlfrescoImapFolder) mailFolder;
                folder.setViewMode(ImapViewMode.ARCHIVE);
                folder.setMountParent(root);
            }
            result.addAll(imapFolders);
        }
        
        logger.debug("listMailboxes returning size:" + result.size());

        return result;

    }

    /**
     * Get the list of folders
     * 
     * @param mailboxRoot
     * @param root
     * @param user
     * @param mailboxPattern
     * @param listSubscribed
     * @param viewMode
     * @return
     */
    private List<AlfrescoImapFolder> listFolder(
            NodeRef mailboxRoot,
            NodeRef root,
            AlfrescoImapUser user,
            String mailboxPattern,
            boolean listSubscribed,
            ImapViewMode viewMode)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("List folder: mailboxPattern=" + mailboxPattern);
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
            // This is the last level
            
            if ("*".equals(name))
            {
                // Deep listing of all folders
                Collection<FileInfo> list = searchDeep(root, viewMode);
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
                // Ends with wildcard
                List<FileInfo> fullList = new LinkedList<FileInfo>();
                List<FileInfo> list = searchFolders(root, name.replace('%', '*'), viewMode);
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
                        List<FileInfo> childList = searchDeep(fileInfo.getNodeRef(), viewMode);
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
                // Non recursive listing
                List<FileInfo> list = searchFolders(root, "*", viewMode);
                LinkedList<AlfrescoImapFolder> subscribedList = new LinkedList<AlfrescoImapFolder>();

                if (listSubscribed)
                {
                    for (FileInfo fileInfo : list)
                    {
                        if (isSubscribed(fileInfo, user.getLogin()))
                        {
                            // folderName, viewMode, mountPointName will be setted in listMailboxes() method
                            subscribedList.add(
                                    new AlfrescoImapFolder(
                                            user.getQualifiedMailboxName(),
                                            fileInfo,
                                            null,
                                            null,
                                            mailboxRoot,
                                            null,
                                            isExtractionEnabled(fileInfo.getNodeRef()),
                                            serviceRegistry));
                        }
                        // \NoSelect
                        else if (hasSubscribedChild(fileInfo, user.getLogin(), viewMode))
                        {
                            // folderName, viewMode, mountPointName will be setted in listMailboxes() method
                            subscribedList.add(
                                    new AlfrescoImapFolder(
                                            user.getQualifiedMailboxName(),
                                            fileInfo,
                                            null,
                                            null,
                                            mailboxRoot,
                                            null,
                                            serviceRegistry,
                                            false,
                                            isExtractionEnabled(fileInfo.getNodeRef())));
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
                // wild cards in the middle of the name
                List<FileInfo> list = searchFolders(root, name.replace('%', '*'), viewMode);
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
                // No wild cards
                List<FileInfo> list = searchFolders(root, name, viewMode);
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
        List<AlfrescoImapFolder> result = new LinkedList<AlfrescoImapFolder>();

        List<FileInfo> list = searchFolders(root, name.replace('%', '*'), viewMode);
        for (FileInfo folder : list)
        {
            Collection<AlfrescoImapFolder> childFolders = listFolder(mailboxRoot, folder.getNodeRef(), user, remainName, listSubscribed, viewMode);

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
     * Convert mailpath from IMAP client representation to the alfresco representation view 
     * (e.g. with default settings 
     * "getMailPathInRepo(Repository_virtual/Imap Home)" will return "Company Home/Imap Home")
     * 
     * @param mailPath mailbox path in IMAP client
     * @return mailbox path in alfresco
     */
    private String getMailPathInRepo(String mailPath)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("getMailPathInRepo :" + mailPath);
        }
        String rootFolder;
        String remain = "";
        int index = mailPath.indexOf(AlfrescoImapConst.HIERARCHY_DELIMITER);
        if (index > 0)
        {
            // mail path contains a /
            rootFolder = mailPath.substring(0, index);
            remain = mailPath.substring(index + 1);
        }
        else
        {
            //mail path is a root folder
            rootFolder = mailPath;
        }
        if (imapConfigMountPoints.keySet().contains(rootFolder))
        {
            Map<String, NodeRef> mountPoints = getMountPoints();
            NodeRef rootRef = mountPoints.get(rootFolder);
            String path = remain;
            
            if(logger.isDebugEnabled())
            {
                logger.debug("getMailPathInRepo mounted point returning :" + path);
            }

            return path;
        }
        else
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("getMailPathInRepo not mounted returning path as is:" + mailPath);
            }
            return mailPath;
        }
    }

    /**
     * Return mount point name for the current mailbox.
     * 
     * @param mailboxName mailbox name in IMAP client.
     * @return mount point name or null.
     */
    private String getMountPointName(String mailboxName)
    {
        String rootFolder;
        int index = mailboxName.indexOf(AlfrescoImapConst.HIERARCHY_DELIMITER);
        if (index > 0)
        {
            rootFolder = mailboxName.substring(0, index);
        }
        else
        {
            rootFolder = mailboxName;
        }
        if (imapConfigMountPoints.keySet().contains(rootFolder))
        {
            return rootFolder;
        }
        else
        {
            return null;
        }

    }

    /**
     * Map of mount points. Name of mount point == key in the map.
     * 
     * @return Map of mount points.
     */
    private Map<String, NodeRef> getMountPoints()
    {
        Set<NodeRef> mountPointNodeRefs = new HashSet<NodeRef>(5);
        
        Map<String, NodeRef> mountPoints = new HashMap<String, NodeRef>();
        NamespaceService namespaceService = serviceRegistry.getNamespaceService();
        SearchService searchService = serviceRegistry.getSearchService();
        for (ImapConfigMountPointsBean config : imapConfigMountPoints.values())
        {
            // Get node reference
            NodeRef nodeRef = config.getFolderPath(namespaceService, nodeService, searchService, fileFolderService);

            if (!mountPointNodeRefs.add(nodeRef))
            {
                throw new IllegalArgumentException(
                        "A mount point has been defined twice: \n" +
                        "   Mount point: " + config);
            }
            mountPoints.put(config.getMountPointName(), nodeRef);
        }
        return mountPoints;
    }

    /**
     * Get root reference for the specified mailbox
     * 
     * @param mailboxName mailbox name in IMAP client.
     * @param userName
     * @return root reference for the specified mailbox
     */
    public NodeRef getMailboxRootRef(String mailboxName, String userName)
    {
        String rootFolder;
        int index = mailboxName.indexOf(AlfrescoImapConst.HIERARCHY_DELIMITER);
        if (index > 0)
        {
            rootFolder = mailboxName.substring(0, index);
        }
        else
        {
            rootFolder = mailboxName;
        }

        Map<String, ImapConfigMountPointsBean> imapConfigs = imapConfigMountPoints;
        if (imapConfigs.keySet().contains(rootFolder))
        {
            Map<String, NodeRef> mountPoints = getMountPoints();
            NodeRef mountRef = mountPoints.get(rootFolder);
            logger.debug("getMailboxRootRef mounted, " + mountRef);
            return mountRef;
            // MER EXPERIMENT
            //return nodeService.getParentAssocs(mountRef).get(0).getParentRef();
        }
        else
        {
            NodeRef ret = getUserImapHomeRef(userName);
            logger.debug("getMailboxRootRef using userImapHome, " + ret);
            return ret;
        }
    }

    /**
     * Get the node ref of the user's imap home.   Will create it on demand if it 
     * does not already exist.
     * 
     * @param userName user name
     * @return user IMAP home reference and create it if it doesn't exist.
     */
    private NodeRef getUserImapHomeRef(final String userName)
    {

        NodeRef userHome = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                // Look for user imap home
                NodeRef userHome = fileFolderService.searchSimple(imapHomeNodeRef, userName);
                if (userHome == null)
                {
                    // user imap home does not exist
                    NodeRef result = fileFolderService.create(imapHomeNodeRef, userName, ContentModel.TYPE_FOLDER).getNodeRef();
                    nodeService.setProperty(result, ContentModel.PROP_DESCRIPTION, userName);
                    
                    // create user inbox
                    fileFolderService.create(result, AlfrescoImapConst.INBOX_NAME, ContentModel.TYPE_FOLDER);
                    
                    // Set permissions on user's imap home
                    permissionService.setInheritParentPermissions(result, false);
                    permissionService.setPermission(result, PermissionService.OWNER_AUTHORITY, PermissionService.ALL_PERMISSIONS, true);
                    
                    return result;
                }

                return userHome;
            }
        }, AuthenticationUtil.getSystemUserName());
        
        return userHome;
    }

    private boolean isSubscribed(FileInfo fileInfo, String userName)
    {
        return !nodeService.hasAspect(fileInfo.getNodeRef(), ImapModel.ASPECT_IMAP_FOLDER_NONSUBSCRIBED);
    }

    /**
     * getSubscribed filters out folders which are not subscribed.
     * @param list
     * @param userName
     * @return collection of subscribed folders.
     */
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

    private boolean hasSubscribedChild(FileInfo parent, String userName, ImapViewMode viewMode)
    {
        List<FileInfo> list = searchDeep(parent.getNodeRef(), viewMode);

        for (FileInfo fileInfo : list)
        {
            if (isSubscribed(fileInfo, userName))
            {
                return true;
            }
        }

        return false;
    }

    
    private List<AlfrescoImapFolder> createMailFolderList(AlfrescoImapUser user, Collection<FileInfo> list, NodeRef imapUserHomeRef)
    {
        List<AlfrescoImapFolder> result = new LinkedList<AlfrescoImapFolder>();

        for (FileInfo folderInfo : list)
        {
            // folderName, viewMode, mountPointName will be setted in listSubscribedMailboxes() method
            result.add(
                    new AlfrescoImapFolder(
                            user.getQualifiedMailboxName(),
                            folderInfo,
                            null,
                            null,
                            imapUserHomeRef,
                            null,
                            isExtractionEnabled(folderInfo.getNodeRef()),
                            serviceRegistry));
        }

        return result;

    }

    /**
     * Return view mode ("virtual", "archive" or "mixed") for specified mailbox.
     * 
     * @param mailboxName name of the mailbox in IMAP client.
     * @return view mode of the specified mailbox.
     */
    private ImapViewMode getViewMode(String mailboxName)
    {
        String rootFolder;
        int index = mailboxName.indexOf(AlfrescoImapConst.HIERARCHY_DELIMITER);
        if (index > 0)
        {
            rootFolder = mailboxName.substring(0, index);
        }
        else
        {
            rootFolder = mailboxName;
        }
        if (imapConfigMountPoints.keySet().contains(rootFolder))
        {
            return imapConfigMountPoints.get(rootFolder).getMode();
        }
        else
        {
            return ImapViewMode.ARCHIVE;
        }
    }

    private String getCurrentUser()
    {
        return AuthenticationUtil.getFullyAuthenticatedUser();
    }

    /**
     * Return list of "favourite" sites, that belong to the specified user and are marked as "Imap favourite"
     * 
     * @param userName name of user
     * @return List of favourite sites.
     */
    private List<NodeRef> getFavouriteSites(final String userName)
    {
        List<NodeRef> favSites = new LinkedList<NodeRef>();

        PreferenceService preferenceService = (PreferenceService) serviceRegistry
                .getService(ServiceRegistry.PREFERENCE_SERVICE);
        Map<String, Serializable> prefs = preferenceService.getPreferences(
                userName, AlfrescoImapConst.PREF_IMAP_FAVOURITE_SITES);

        /**
         * List the user's sites
         */
        List<SiteInfo> sites = serviceRegistry.getTransactionService()
                .getRetryingTransactionHelper().doInTransaction(
                        new RetryingTransactionCallback<List<SiteInfo>>()
                        {
                            public List<SiteInfo> execute() throws Exception
                            {
                                List<SiteInfo> res = new ArrayList<SiteInfo>();
                                try
                                {

                                    res = serviceRegistry.getSiteService()
                                            .listSites(userName);
                                } 
                                catch (SiteServiceException e)
                                {
                                    // Do nothing. Root sites folder was not
                                    // created.
                                    if (logger.isDebugEnabled())
                                    {
                                        logger.warn("Root sites folder was not created.");
                                    }
                                } 
                                catch (InvalidNodeRefException e)
                                {
                                    // Do nothing. Root sites folder was
                                    // deleted.
                                    if (logger.isDebugEnabled())
                                    {
                                        logger.warn("Root sites folder was deleted.");
                                    }
                                }

                                return res;
                            }
                        }, false, true);

        for (SiteInfo siteInfo : sites)
        {
            String key = AlfrescoImapConst.PREF_IMAP_FAVOURITE_SITES + "."
                    + siteInfo.getShortName();
            Boolean isImapFavourite = (Boolean) prefs.get(key);
            if (isImapFavourite != null && isImapFavourite)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("favourite site user:" + userName + "site:" + siteInfo.getShortName());
                }
                favSites.add(siteInfo.getNodeRef());
            }
        }
        
        logger.debug("at end of getFavouriteSites user:" + userName);

        return favSites;
    }

    /**
     * Checks for the existence of the flaggable aspect and adds it if it is not already present on the folder. 
     * @param nodeRef
     */
    private void checkForFlaggableAspect(NodeRef nodeRef)
    {
        if (!nodeService.hasAspect(nodeRef, ImapModel.ASPECT_FLAGGABLE))
        {
            AccessStatus status = permissionService.hasPermission(nodeRef, PermissionService.WRITE_PROPERTIES);
            if (status == AccessStatus.DENIED)
            {
                logger.debug("checkForFlaggableAspect - no permissions to add FLAGGABLE aspect" + nodeRef);
            }
            else
            {    
                logger.debug("adding flaggable aspect nodeRef:" + nodeRef);
                Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
                nodeService.addAspect(nodeRef, ImapModel.ASPECT_FLAGGABLE, aspectProperties);
            }
        }
    }
    
    private boolean isExtractionEnabled(NodeRef nodeRef)
    {
        return extractAttachmentsEnabled && !ignoreExtractionFolders.contains(nodeRef);
    }

    /**
     * This method should returns a unique identifier of Alfresco server. The possible UID may be calculated based on IP address, Server port, MAC address, Web Application context.
     * This UID should be parseable into initial components. This necessary for the implementation of the following case: If the message being copied (e.g. drag-and-drop) between
     * two different Alfresco accounts in the IMAP client, we must unambiguously identify from which Alfresco server this message being copied. The message itself does not contain
     * content data, so we must download it from the initial server (e.g. using download content servlet) and save it into destination repository.
     * 
     * @return String representation of unique identifier of Alfresco server
     */
    public String getAlfrescoServerUID()
    {
        // TODO Implement as javadoc says.
        return "Not-Implemented";
    }
}