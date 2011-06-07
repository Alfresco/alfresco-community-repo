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

import static org.alfresco.repo.imap.AlfrescoImapConst.DICTIONARY_TEMPLATE_PREFIX;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.imap.AlfrescoImapConst.ImapViewMode;
import org.alfresco.repo.imap.config.ImapConfigMountPointsBean;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteChildAssociationPolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.site.SiteServiceException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.SubFolderFilter;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
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
import org.apache.poi.hmef.HMEFMessage;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.FileCopyUtils;

/**
 * @author Dmitry Vaserin
 * @author Arseny Kovalchuk
 * @since 3.2
 */
public class ImapServiceImpl implements ImapService, OnCreateChildAssociationPolicy, OnDeleteChildAssociationPolicy
{
    private Log logger = LogFactory.getLog(ImapServiceImpl.class);

    private static final String ERROR_PERMISSION_DENIED = "imap.server.error.permission_denied";
    private static final String ERROR_FOLDER_ALREADY_EXISTS = "imap.server.error.folder_already_exist";
    private static final String ERROR_MAILBOX_NAME_IS_MANDATORY = "imap.server.error.mailbox_name_is_mandatory";
    private static final String ERROR_CANNOT_GET_A_FOLDER = "imap.server.error.cannot_get_a_folder";

    private static final String CHECKED_NODES = "imap.flaggable.aspect.checked.list";
    private static final String FAVORITE_SITES = "imap.favorite.sites.list";
    private static final String UIDVALIDITY_LISTENER_ALREADY_BOUND = "imap.uidvalidity.already.bound";
    
    private SysAdminParams sysAdminParams;
    private FileFolderService fileFolderService;
    private NodeService nodeService;
    private PermissionService permissionService;
    private ServiceRegistry serviceRegistry;
    private BehaviourFilter policyBehaviourFilter;
    private MimetypeService mimetypeService; 

    /**
     * Folders cache
     * 
     * Key : folder name, Object : AlfrescoImapFolder
     */
    private SimpleCache<Serializable, Object> foldersCache;

    private Map<String, ImapConfigMountPointsBean> imapConfigMountPoints;
    private RepositoryFolderConfigBean[] ignoreExtractionFoldersBeans;
    private RepositoryFolderConfigBean imapHomeConfigBean;

    private NodeRef imapHomeNodeRef;
    private Set<NodeRef> ignoreExtractionFolders;

    private String defaultFromAddress;
    private String repositoryTemplatePath;
    private boolean extractAttachmentsEnabled = true;

    private Map<EmailBodyFormat, String> defaultBodyTemplates;

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

    public void setFoldersCache(SimpleCache<Serializable, Object> foldersCache)
    {
        this.foldersCache = foldersCache;
    }

    public FileFolderService getFileFolderService()
    {
        return fileFolderService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    public void setPolicyFilter(BehaviourFilter policyFilter)
    {
        this.policyBehaviourFilter = policyFilter;
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

    public String getShareApplicationContextUrl()
    {
        return sysAdminParams.getShareProtocol() + "://" + sysAdminParams.getShareHost() + ":" + sysAdminParams.getSharePort() + "/" + sysAdminParams.getShareContext();
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
        PropertyCheck.mandatory(this, "policyBehaviourFilter", policyBehaviourFilter);
        PropertyCheck.mandatory(this, "mimetypeService", mimetypeService);
    }

    public void startup()
    {
        bindBeahaviour();
        
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

    protected void bindBeahaviour()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("[bindBeahaviour] Binding behaviours");
        }
        PolicyComponent policyComponent = (PolicyComponent) serviceRegistry.getService(QName.createQName(NamespaceService.ALFRESCO_URI, "policyComponent"));
        policyComponent.bindAssociationBehaviour(
                OnCreateChildAssociationPolicy.QNAME,
                ContentModel.TYPE_FOLDER,
                ContentModel.ASSOC_CONTAINS,
                new JavaBehaviour(this, "onCreateChildAssociation", NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindAssociationBehaviour(
                OnCreateChildAssociationPolicy.QNAME,
                ContentModel.TYPE_CONTENT,
                ContentModel.ASSOC_CONTAINS,
                new JavaBehaviour(this, "onCreateChildAssociation", NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindAssociationBehaviour(
                OnDeleteChildAssociationPolicy.QNAME,
                ContentModel.TYPE_FOLDER,
                ContentModel.ASSOC_CONTAINS,
                new JavaBehaviour(this, "onDeleteChildAssociation", NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindAssociationBehaviour(
                OnDeleteChildAssociationPolicy.QNAME,
                ContentModel.TYPE_CONTENT,
                ContentModel.ASSOC_CONTAINS,
                new JavaBehaviour(this, "onDeleteChildAssociation", NotificationFrequency.TRANSACTION_COMMIT));
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
            logger.debug("Listing subscribed mailboxes: mailbox path in Alfresco=" + mailboxPattern);
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
            logger.debug("Listing  mailboxes: mailbox path in Alfresco Repository = " + mailboxPattern);
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
            logger.debug("Creating mailbox: " + mailboxName);
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
                AlfrescoImapFolder resultFolder = new AlfrescoImapFolder(
                        user.getQualifiedMailboxName(),
                        mailFolder,
                        folderName,
                        getViewMode(mailboxName),
                        root,
                        getMountPointName(mailboxName),
                        isExtractionEnabled(mailFolder.getNodeRef()),
                        serviceRegistry);
                foldersCache.put(mailboxName, resultFolder);
                return resultFolder;
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
            logger.debug("Deleting mailbox: mailboxName=" + mailboxName);
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
        foldersCache.remove(mailboxName);
    }

    public void renameMailbox(AlfrescoImapUser user, String oldMailboxName, String newMailboxName)
    {
        if (oldMailboxName == null || newMailboxName == null)
        {
            throw new IllegalArgumentException(ERROR_MAILBOX_NAME_IS_MANDATORY);
        }
		
		AlfrescoImapFolder sourceNode = getFolder(user, oldMailboxName);
			  
        oldMailboxName = Utf7.decode(oldMailboxName, Utf7.UTF7_MODIFIED);
        newMailboxName = Utf7.decode(newMailboxName, Utf7.UTF7_MODIFIED);
        if (logger.isDebugEnabled())
        {
            logger.debug("Renaming folder oldMailboxName=" + oldMailboxName + " newMailboxName=" + newMailboxName);
        }

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
                    FileInfo newFileInfo = null;
                    if (oldMailboxName.equalsIgnoreCase(AlfrescoImapConst.INBOX_NAME))
                    {
                        // If you trying to rename INBOX
                        // - just copy it to another folder with new name
                        // and leave INBOX (with children) intact.
                        newFileInfo = fileFolderService.copy(sourceNode.getFolderInfo().getNodeRef(), parentNodeRef, folderName);
                    }
                    else
                    {
                        newFileInfo = fileFolderService.move(
                                sourceNode.getFolderInfo().getNodeRef(),
                                parentNodeRef, folderName);
                    }
                    
                    foldersCache.remove(oldMailboxName);
                    AlfrescoImapFolder resultFolder = new AlfrescoImapFolder(
                            user.getQualifiedMailboxName(),
                            newFileInfo,
                            folderName,
                            getViewMode(newMailboxName),
                            root,
                            getMountPointName(newMailboxName),
                            isExtractionEnabled(newFileInfo.getNodeRef()),
                            serviceRegistry);
                    foldersCache.put(newMailboxName, resultFolder);
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

    public AlfrescoImapFolder getFolder(AlfrescoImapUser user, String mailboxName)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Folders cache size is " + foldersCache.getKeys().size());
        }
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
            AlfrescoImapFolder hierarhyFolder = (AlfrescoImapFolder) foldersCache.get("hierarhy.delimeter");
            if (logger.isDebugEnabled())
            {
                logger.debug("Got a hierarhy delimeter from cache: " + hierarhyFolder);
            }
            if (hierarhyFolder == null)
            {
                hierarhyFolder = new AlfrescoImapFolder(user.getQualifiedMailboxName(), serviceRegistry);
                // TEMP Comment out putting this into the same cache as the "real folders"  Causes NPE in 
                // Security Interceptor
                //foldersCache.put("hierarhy.delimeter", hierarhyFolder);
            }
            return hierarhyFolder;
        }
        else if (AlfrescoImapConst.INBOX_NAME.equalsIgnoreCase(mailboxName) || AlfrescoImapConst.TRASH_NAME.equalsIgnoreCase(mailboxName))
        {
            String cacheKey = user.getLogin() + '.' + mailboxName;
            AlfrescoImapFolder imapSystemFolder = (AlfrescoImapFolder) foldersCache.get(cacheKey);
            
            if(imapSystemFolder != null)
            {    
                if (logger.isDebugEnabled())
                {
                    logger.debug("Got a system folder '" + mailboxName + "' from cache: " + imapSystemFolder);
                }
                
                /**
                 * Check whether resultFolder is stale
                 */
                if(imapSystemFolder.isStale())
                {
                    logger.debug("system folder is stale");
                    imapSystemFolder = null;
                }
            }
            
            if (imapSystemFolder == null)
            {
                NodeRef userImapRoot = getUserImapHomeRef(user.getLogin());
                NodeRef mailBoxRef = nodeService.getChildByName(userImapRoot, ContentModel.ASSOC_CONTAINS, mailboxName);
                if (mailBoxRef != null)
                {
                    FileInfo mailBoxFileInfo = fileFolderService.getFileInfo(mailBoxRef);
                    imapSystemFolder = new AlfrescoImapFolder(
                            user.getQualifiedMailboxName(),
                            mailBoxFileInfo,
                            mailBoxFileInfo.getName(),
                            getViewMode(mailboxName),
                            userImapRoot,
                            getMountPointName(mailboxName),
                            isExtractionEnabled(mailBoxFileInfo.getNodeRef()),
                            serviceRegistry);
                    foldersCache.put(cacheKey, imapSystemFolder);
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Returning folder '" + mailboxName + "'");
                    }
                }
                else
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Cannot get a folder '" + mailboxName + "'");
                    }
                    throw new AlfrescoRuntimeException(ERROR_CANNOT_GET_A_FOLDER, new String[] { mailboxName });
                }
            }
            return imapSystemFolder;
            
        }

        /**
         * Folder is not hierarchy.delimiter or a "System" folder (INBOX or TRASH)
         */
        AlfrescoImapFolder resultFolder = (AlfrescoImapFolder) foldersCache.get(mailboxName);
        
        if(resultFolder != null)
        {    
            if (logger.isDebugEnabled())
            {
                logger.debug("Got a folder '" + mailboxName + "' from cache: " + resultFolder);
            }
            
            /**
             * Check whether resultFolder is stale
             */
            if(resultFolder.isStale())
            {
                logger.debug("folder is stale");
                resultFolder = null;
            }
        }

        if (resultFolder == null)
        {
            ImapViewMode viewMode = getViewMode(mailboxName);
            String mountPointName = getMountPointName(mailboxName);

            NodeRef root = getMailboxRootRef(mailboxName, user.getLogin());
            NodeRef nodeRef = root; // initial top folder

            String[] folderNames = getMailPathInRepo(mailboxName).split(String.valueOf(AlfrescoImapConst.HIERARCHY_DELIMITER));

            if(folderNames.length == 1 && folderNames[0].length() == 0)
            {
                // This is the root of the mount point e.g "Alfresco IMAP" which has a path from root of ""
                FileInfo folderFileInfo = fileFolderService.getFileInfo(root);

                resultFolder = new AlfrescoImapFolder(
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
            }
            else
            {

                for (int i = 0; i < folderNames.length; i++)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Processing of '" + folderNames[i] + "'");
                    } 

                    NodeRef targetNode = fileFolderService.searchSimple(nodeRef, folderNames[i]);

                    if (targetNode == null)
                    {
                        resultFolder = new AlfrescoImapFolder(user.getQualifiedMailboxName(), serviceRegistry);
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Returning empty folder '" + folderNames[i] + "'");
                        }
                        // skip cache for root
                        return resultFolder;
                    }

                    if (i == (folderNames.length - 1)) // is last
                    {
                        FileInfo folderFileInfo = fileFolderService.getFileInfo(targetNode);

                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Found folder to list: " + folderFileInfo.getName());
                        }

                        resultFolder = new AlfrescoImapFolder(
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
                    }

                    /**
                     * End of loop - next element in path
                     */
                    nodeRef = targetNode;
                }
            }

            if (resultFolder == null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Cannot get a folder '" + mailboxName + "'");
                }
                throw new AlfrescoRuntimeException(ERROR_CANNOT_GET_A_FOLDER, new String[] { mailboxName });
            }
            else
            {
                foldersCache.put(mailboxName, resultFolder);
                if (logger.isDebugEnabled())
                {
                    logger.debug("Put a folder '" + mailboxName + "' to cache: " + resultFolder);
                }
            }
        }
        return resultFolder;

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
            logger.debug("[searchDeep] Start. nodeRef=" + contextNodeRef + ", viewMode=" + viewMode);
        }
        
        List<FileInfo> searchResult = fileFolderService.listDeepFolders(contextNodeRef, new ImapSubFolderFilter(viewMode));        
        
        if (logger.isDebugEnabled())
        {
            logger.debug("[searchDeep] End");
        }
        return new ArrayList<FileInfo>(searchResult);
    }


    /**
     * Shallow search for mailboxes in specified context
     * 
     * @param contextNodeRef context folder for search
     * @param viewMode is folder in "Virtual" View
     * @param namePattern name pattern for search
     * @return list of mailboxes
     */
    private List<FileInfo> searchByPattern(final NodeRef contextNodeRef, final ImapViewMode viewMode, final String namePattern)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("[searchByPattern] Start. nodeRef=" + contextNodeRef + ", viewMode=" + viewMode + " namePattern=" + namePattern);
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
            logger.debug("call file folder service to list folders");
            
            searchResult = fileFolderService.listFolders(contextNodeRef);
        }
        else
        {
            logger.debug("call listDeepFolders");
            searchResult = fileFolderService.listDeepFolders(contextNodeRef, new ImapSubFolderFilter(viewMode, namePattern));
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
            if (logger.isDebugEnabled())
            {
                logger.debug("[searchByPattern] End. nodeRef=" + contextNodeRef + ", viewMode=" + viewMode + ", namePattern=" + namePattern + ", searchResult=" +searchResult.size());
            }
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
            logger.debug("Search mails contextNodeRef=" + contextNodeRef + ", viewMode=" + viewMode );
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

        logger.debug("Found files:" + result.size());
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
            logger.debug("Unsubscribing by ASPECT_IMAP_FOLDER_NONSUBSCRIBED");
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
        if (nodeService.exists(messageInfo.getNodeRef()))
        {
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
            logger.debug("[setFlag] Access denied to add FLAG to " + nodeRef);
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
            logger.debug("[listMailboxes] user:" + user.getLogin() + ", mailboxPattern:" + mailboxPattern + ", listSubscribed:" + listSubscribed);
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
            /* FIX for ALF-2793 Reinstated
            if (!mailboxPattern.equals("*"))
            {
                mountPoint = mountParent;
            }
            */
            List<AlfrescoImapFolder> folders = expandFolder(mountPoint, mountPoint, user, mailboxPattern, listSubscribed, viewMode);
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
        List<AlfrescoImapFolder> imapFolders = expandFolder(root, root, user, mailboxPattern, listSubscribed, ImapViewMode.ARCHIVE);

        if (imapFolders != null)
        {
            for (AlfrescoImapFolder mailFolder : imapFolders)
            {
                //AlfrescoImapFolder folder = (AlfrescoImapFolder) mailFolder;
                mailFolder.setViewMode(ImapViewMode.ARCHIVE);
                mailFolder.setMountParent(root);
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
    private List<AlfrescoImapFolder> expandFolder(
            NodeRef mailboxRoot,
            NodeRef root,
            AlfrescoImapUser user,
            String mailboxPattern,
            boolean listSubscribed,
            ImapViewMode viewMode)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("expand folder: root:" + root + " user: " + user + " :mailboxPattern=" + mailboxPattern);
        }
        if (mailboxPattern == null)
            return null;
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
                List<FileInfo> list = searchByPattern(root, viewMode, name.replace('%', '*'));
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
                List<AlfrescoImapFolder> subscribedList = new LinkedList<AlfrescoImapFolder>();
                List<FileInfo> list = searchByPattern(root, viewMode, "*");
                if (listSubscribed)
                {
                    for (FileInfo fileInfo : list)
                    {
                        if (isSubscribed(fileInfo, user.getLogin()))
                        {
                            // folderName, viewMode, mountPointName will be set in listMailboxes() method
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
                            // folderName, viewMode, mountPointName will be set in listMailboxes() method
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
                List<FileInfo> list = searchByPattern(root, viewMode, name.replace('%', '*'));
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
                List<FileInfo> list = searchByPattern(root, viewMode, name);
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
        List<FileInfo> list = searchByPattern(root, viewMode, name.replace('%', '*'));
        for (FileInfo folder : list)
        {
            Collection<AlfrescoImapFolder> childFolders = expandFolder(mailboxRoot, folder.getNodeRef(), user, remainName, listSubscribed, viewMode);

            if (childFolders != null)
            {
                result.addAll(childFolders);
            }
        }
        return !result.isEmpty() ? result : null;
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
            logger.debug("[getMailPathInRepo] Path: " + mailPath);
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
            //Map<String, NodeRef> mountPoints = getMountPoints();
            //NodeRef rootRef = mountPoints.get(rootFolder);
            String path = remain;
            
            if(logger.isDebugEnabled())
            {
                logger.debug("[getMailPathInRepo] Mounted point returning: " + path);
            }

            return path;
        }
        else
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("[getMailPathInRepo] Not mounted. Returning path as is: " + mailPath);
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
        final NamespaceService namespaceService = serviceRegistry.getNamespaceService();
        final SearchService searchService = serviceRegistry.getSearchService();
        for (final ImapConfigMountPointsBean config : imapConfigMountPoints.values())
        {
            try
            {
                // Get node reference. Do it in new transaction to avoid RollBack in case when AccessDeniedException is thrown.
                NodeRef nodeRef = serviceRegistry.getTransactionService().getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Exception
                    {
                        try
                        {
                            return config.getFolderPath(namespaceService, nodeService, searchService, fileFolderService);
                        }
                        catch (AccessDeniedException e)
                        {
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("A mount point is skipped due to Access Dennied. \n" + "   Mount point: " + config + "\n" + "   User: "
                                        + AuthenticationUtil.getFullyAuthenticatedUser());
                            }
                        }

                        return null;
                    }
                }, true, true);

                if (nodeRef != null)
                {
                    if (!mountPointNodeRefs.add(nodeRef))
                    {
                        throw new IllegalArgumentException("A mount point has been defined twice: \n" + "   Mount point: " + config);
                    }
                    mountPoints.put(config.getMountPointName(), nodeRef);
                }
            }
            catch (AccessDeniedException e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("A mount point is skipped due to Access Dennied. \n" + "   Mount point: " + config + "\n" + "   User: "
                            + AuthenticationUtil.getFullyAuthenticatedUser());
                }
            }
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
        // XXX : put folders into the cache and get them in next lookups. 
        // The question is to get a mailBoxName for the cache key... And what if a folders list was changed?
        // So, for now keep it as is.
        for (FileInfo folderInfo : list)
        {
            // folderName, viewMode, mountPointName will be set in listSubscribedMailboxes() method
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
        if (logger.isDebugEnabled())
        {
            logger.debug("[getFavouriteSites] entry for user: " + userName);
        }
        List<NodeRef> favSites = AlfrescoTransactionSupport.getResource(FAVORITE_SITES);
        if (logger.isDebugEnabled())
        {
            if (favSites == null)
            {
                logger.debug("[getFavouriteSites] There is no Favorite sites' list bound to transaction " + AlfrescoTransactionSupport.getTransactionId());
            }
            else
            {
                logger.debug("[getFavouriteSites] Found Favorite sites' list bound to transaction " + AlfrescoTransactionSupport.getTransactionId());
            }
        }
        if (favSites == null)
        {
            favSites = new LinkedList<NodeRef>();

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
                                            logger.warn("[getFavouriteSites] Root sites folder was not created.");
                                        }
                                    } 
                                    catch (InvalidNodeRefException e)
                                    {
                                        // Do nothing. Root sites folder was
                                        // deleted.
                                        if (logger.isDebugEnabled())
                                        {
                                            logger.warn("[getFavouriteSites] Root sites folder was deleted.");
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
                        logger.debug("[getFavouriteSites] User: " + userName + " Favourite site: " + siteInfo.getShortName());
                    }
                    favSites.add(siteInfo.getNodeRef());
                }
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("[getFavouriteSites] Bind new Favorite sites' list to transaction " + AlfrescoTransactionSupport.getTransactionId());
            }
            AlfrescoTransactionSupport.bindResource(FAVORITE_SITES, favSites);
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("[getFavouriteSites] end for user: " + userName);
        }

        return favSites;
    }

    /**
     * Checks for the existence of the flaggable aspect and adds it if it is not already present on the folder. 
     * @param nodeRef
     */
    private void checkForFlaggableAspect(NodeRef nodeRef)
    {
        Set<NodeRef> alreadyChecked = AlfrescoTransactionSupport.getResource(CHECKED_NODES);
        if (alreadyChecked == null)
        {
            alreadyChecked = new HashSet<NodeRef>();
        }
        if (alreadyChecked.contains(nodeRef))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("[checkForFlaggableAspect] Flaggable aspect has been already checked for {" + nodeRef + "}");
            }
            return;
        }
        try
        {
            serviceRegistry.getLockService().checkForLock(nodeRef);
        }
        catch (NodeLockedException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("[checkForFlaggableAspect] Node {" + nodeRef + "} is locked");
            }
            alreadyChecked.add(nodeRef);
            return;
        }
        if (!nodeService.hasAspect(nodeRef, ImapModel.ASPECT_FLAGGABLE))
        {
            AccessStatus status = permissionService.hasPermission(nodeRef, PermissionService.WRITE_PROPERTIES);
            if (status == AccessStatus.DENIED)
            {
                logger.debug("[checkForFlaggableAspect] No permissions to add FLAGGABLE aspect" + nodeRef);
            }
            else
            {    
                try
                {
                    policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                    logger.debug("[checkForFlaggableAspect] Adding flaggable aspect to nodeRef: " + nodeRef);
                    Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
                    nodeService.addAspect(nodeRef, ImapModel.ASPECT_FLAGGABLE, aspectProperties);
                }
                finally
                {
                    policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                }
            }
        }
        alreadyChecked.add(nodeRef);
        AlfrescoTransactionSupport.bindResource(CHECKED_NODES, alreadyChecked);
    }
    
    private boolean isExtractionEnabled(NodeRef nodeRef)
    {
        return extractAttachmentsEnabled && !ignoreExtractionFolders.contains(nodeRef);
    }

    public String getDefaultEmailBodyTemplate(EmailBodyFormat type)
    {
        if (defaultBodyTemplates == null)
        {
            defaultBodyTemplates = new HashMap<EmailBodyFormat, String>(4);
            
            for (EmailBodyFormat onetype : EmailBodyFormat.values())
            {
                String result = onetype.getClasspathTemplatePath();
                try
                {
                    // This query uses cm:name to find the template node(s).
                    // For the case where the templates are renamed, it would be better to use a QName path-based query.
                    

                    final StringBuilder templateName = new StringBuilder(DICTIONARY_TEMPLATE_PREFIX).append("_").append(onetype.getTypeSubtype()).append("_").append(onetype.getWebApp()).append(".ftl");

                    final String repositoryTemplatePath = getRepositoryTemplatePath();
                    int indexOfStoreDelim = repositoryTemplatePath.indexOf(StoreRef.URI_FILLER);
                    if (indexOfStoreDelim == -1)
                    {
                        throw new IllegalArgumentException("Bad path format, " + StoreRef.URI_FILLER + " not found");
                    }
                    indexOfStoreDelim += StoreRef.URI_FILLER.length();
                    int indexOfPathDelim = repositoryTemplatePath.indexOf("/", indexOfStoreDelim);
                    if (indexOfPathDelim == -1)
                    {
                        throw new IllegalArgumentException("Bad path format, '/' not found");
                    }
                    final String storePath = repositoryTemplatePath.substring(0, indexOfPathDelim);
                    final String rootPathInStore = repositoryTemplatePath.substring(indexOfPathDelim);
                    final String query = rootPathInStore + "/" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + templateName;
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("[getDefaultEmailBodyTemplate] Query: " + query);
                    }
                    StoreRef storeRef = new StoreRef(storePath);
                    ResultSet resultSet = serviceRegistry.getSearchService().query(storeRef, "xpath", query);
                    if (resultSet == null || resultSet.length() == 0)
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("template not found:" + templateName);
                        }
                        throw new AlfrescoRuntimeException(String.format("[getDefaultEmailBodyTemplate] IMAP message template '%1$s' does not exist in the path '%2$s'.", templateName, repositoryTemplatePath));
                    }
                    final NodeRef defaultLocaleTemplate = resultSet.getNodeRef(0);
                    
                    NodeRef localisedSibling = serviceRegistry.getFileFolderService().getLocalizedSibling(defaultLocaleTemplate);

                    result = localisedSibling.toString();
                    
                    resultSet.close();
                }
                // We are catching all exceptions. E.g. search service can possibly throw an exceptions on malformed queries.
                catch (Exception e)
                {
                    logger.error("[getDefaultEmailBodyTemplate]", e);
                }
                defaultBodyTemplates.put(onetype, result);
            }
        }
        return defaultBodyTemplates.get(type);
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
    
    /**
     * Share Site Exclusion Filter
     */
    private class ImapSubFolderFilter implements SubFolderFilter
    {
        /**
         * Exclude Share Sites of TYPE_SITE
         */
        private Collection<QName> typesToExclude;
        private List<NodeRef> favs;
        private String mailboxPattern;
        private ImapViewMode imapViewMode;
        
        ImapSubFolderFilter(ImapViewMode imapViewMode)
        {
            this.imapViewMode = imapViewMode;
            this.typesToExclude = serviceRegistry.getDictionaryService().getSubTypes(SiteModel.TYPE_SITE, true);
            this.favs = getFavouriteSites(getCurrentUser());
        }
        
        ImapSubFolderFilter(ImapViewMode imapViewMode, String mailboxPattern)
        {
            this(imapViewMode);
            this.mailboxPattern = mailboxPattern.replaceAll("\\*", "(.)*");;
        }
        
        @Override
        public boolean isEnterSubfolder(ChildAssociationRef subfolderRef)
        {
            NodeRef folder = subfolderRef.getChildRef();
            if (mailboxPattern != null)
            {
                logger.debug("Child QName: " + subfolderRef.getQName());
                String name = (String) nodeService.getProperty(folder, ContentModel.PROP_NAME);
                if (logger.isDebugEnabled())
                {
                    logger.debug("Folder name: " + name + ". Pattern: " + mailboxPattern + ". Matches: " + name.matches(mailboxPattern));
                }
                if (!name.matches(mailboxPattern))
                    return false;
            }
            QName typeOfFolder = nodeService.getType(folder);
            if (typesToExclude.contains(typeOfFolder))
            {
                if (imapViewMode == ImapViewMode.VIRTUAL || imapViewMode == ImapViewMode.MIXED)
                {
                    /**
                     * In VIRTUAL and MIXED MODE WE SHOULD ONLY DISPLAY FOLDERS FROM FAVOURITE SITES
                     */
                    if (favs.contains(folder))
                    {
                        logger.debug("[ImapSubFolderFilter] (VIRTUAL) including fav site folder :" + subfolderRef.getQName());
                        return true;
                    }
                    else
                    {
                        logger.debug("[ImapSubFolderFilter] (VIRTUAL) excluding non fav site folder :" + subfolderRef.getQName());
                        return false;
                    }
                }
                else
                {
                    /**
                     * IN ARCHIVE MODE we don't display folders for any SITES, regardless of whether they are favourites.
                     */
                    logger.debug("[ImapSubFolderFilter] (ARCHIVE) excluding site folder :" + subfolderRef.getQName());
                    return false;
                }
            }
            return true;
        }
        
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        // Add a listener once, when a lots of messsages were created/moved into the folder
        if (AlfrescoTransactionSupport.getResource(UIDVALIDITY_LISTENER_ALREADY_BOUND) == null)
        {
            AlfrescoTransactionSupport.bindListener(new UidValidityTransactionListener(childAssocRef.getParentRef(), nodeService));
            AlfrescoTransactionSupport.bindResource(UIDVALIDITY_LISTENER_ALREADY_BOUND, true);
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("[onCreateChildAssociation] Association " + childAssocRef + " created. UIDVALIDITY will be changed.");
        }
    }
    
    public void onDeleteChildAssociation(ChildAssociationRef childAssocRef)
    {
        // Add a listener once, when a lots of messsages were created/moved into the folder
        if (AlfrescoTransactionSupport.getResource(UIDVALIDITY_LISTENER_ALREADY_BOUND) == null)
        {
            AlfrescoTransactionSupport.bindListener(new UidValidityTransactionListener(childAssocRef.getParentRef(), nodeService));
            AlfrescoTransactionSupport.bindResource(UIDVALIDITY_LISTENER_ALREADY_BOUND, true);
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("[onDeleteChildAssociation] Association " + childAssocRef + " removed. UIDVALIDITY will be changed.");
        }
    }
    
    private class UidValidityTransactionListener extends TransactionListenerAdapter
    {
        
        private RunAsWork<Long> work;
        
        UidValidityTransactionListener(NodeRef folderNodeRef, NodeService nodeService)
        {
            this.work = new IncrementUidValidityWork(folderNodeRef, nodeService);
        }
        
        @Override
        public void afterCommit()
        {
            AuthenticationUtil.runAs(this.work, AuthenticationUtil.getSystemUserName());
        }
        
    }
    
    private class IncrementUidValidityWork implements RunAsWork<Long>
    {
        private NodeService nodeService;
        private NodeRef folderNodeRef;
        
        public IncrementUidValidityWork(NodeRef folderNodeRef, NodeService nodeService)
        {
            this.folderNodeRef = folderNodeRef;
            this.nodeService = nodeService;
        }

        @Override
        public Long doWork() throws Exception
        {
            RetryingTransactionHelper txnHelper = serviceRegistry.getRetryingTransactionHelper();
            return txnHelper.doInTransaction(new RetryingTransactionCallback<Long>(){

                @Override
                public Long execute() throws Throwable
                {
                    long modifDate = new Date().getTime();
                    
                    if (!IncrementUidValidityWork.this.nodeService.hasAspect(folderNodeRef, ImapModel.ASPECT_IMAP_FOLDER))
                    {
                        Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>(1, 1);
                        aspectProperties.put(ImapModel.PROP_UIDVALIDITY, modifDate);
                        IncrementUidValidityWork.this.nodeService.addAspect(folderNodeRef, ImapModel.ASPECT_IMAP_FOLDER, aspectProperties);
                    }
                    else
                    {
                        IncrementUidValidityWork.this.nodeService.setProperty(folderNodeRef, ImapModel.PROP_UIDVALIDITY, modifDate);
                    }
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("UIDVALIDITY was modified");
                    }
                    return modifDate;
                }
                
            }, false, true);
        }
        
    }
    
    /**
     * Return true if provided nodeRef is in Sites/.../documentlibrary
     */
    public boolean isNodeInSitesLibrary(NodeRef nodeRef)
    {
        boolean isInDocLibrary = false;
        NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
        while (parent != null && !nodeService.getType(parent).equals(SiteModel.TYPE_SITE))
        {
            String parentName = (String) nodeService.getProperty(parent, ContentModel.PROP_NAME);
            if (parentName.equalsIgnoreCase("documentlibrary"))
            {
                isInDocLibrary = true;
            }
            nodeRef = parent;
            if (nodeService.getPrimaryParent(nodeRef) != null)
            {
                parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
            }
        }
        if (parent == null)
        {
            return false;
        }
        else
        {
            return nodeService.getType(parent).equals(SiteModel.TYPE_SITE) && isInDocLibrary;
        }
    }

    /**
     * Extract attachments from a MimeMessage
     * 
     * Puts the attachments into a subfolder below the parent folder.
     * 
     * @return the node ref of the folder containing the attachments or null if there are no
     * attachments.
     */
    public NodeRef extractAttachments(
            NodeRef parentFolder,
            NodeRef messageFile,
            MimeMessage originalMessage)
            throws IOException, MessagingException
    {
       
        String messageName = (String)nodeService.getProperty(messageFile, ContentModel.PROP_NAME);
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
                                parentFolder,
                                attachmentsFolderName,
                                ContentModel.TYPE_FOLDER);
                        nodeService.createAssociation(
                                messageFile,
                                attachmentsFolderFileInfo.getNodeRef(),
                                ImapModel.ASSOC_IMAP_ATTACHMENTS_FOLDER);
                    }
                    createAttachment(messageFile, attachmentsFolderFileInfo.getNodeRef(), part);
                }
            }
        }
        if(attachmentsFolderFileInfo != null)
        {
            return attachmentsFolderFileInfo.getNodeRef();
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Create an attachment given a mime part
     * 
     * @param messageFile the file containing the message
     * @param destinationFolder where to put the attachment
     * @param part the mime part
     * 
     * @throws MessagingException
     * @throws IOException
     */
    private void createAttachment(NodeRef messageFile, NodeRef destinationFolder, Part part) throws MessagingException, IOException
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
                
        if(contentType.getBaseType().equalsIgnoreCase("application/ms-tnef"))
        {
            // The content is TNEF
            HMEFMessage hmef = new HMEFMessage(part.getInputStream());
            
            //hmef.getBody();
            List<org.apache.poi.hmef.Attachment> attachments = hmef.getAttachments();
            for(org.apache.poi.hmef.Attachment attachment : attachments)
            {
                String subName = attachment.getLongFilename();
                
                NodeRef attachmentNode = fileFolderService.searchSimple(destinationFolder, subName);
                if (attachmentNode == null)
                {
                    /*
                     * If the node with the given name does not already exist
                     * Create the content node to contain the attachment
                     */
                    FileInfo createdFile = fileFolderService.create(
                            destinationFolder,
                            subName,
                            ContentModel.TYPE_CONTENT);
                    
                    attachmentNode = createdFile.getNodeRef();
                    
                    serviceRegistry.getNodeService().createAssociation(
                            messageFile,
                            attachmentNode,
                            ImapModel.ASSOC_IMAP_ATTACHMENT);
                
                
                    byte[] bytes = attachment.getContents();
                    ContentWriter writer = fileFolderService.getWriter(attachmentNode);
                    
                    //TODO ENCODING - attachment.getAttribute(TNEFProperty.);
                    String extension = attachment.getExtension();
                    String mimetype = mimetypeService.getMimetype(extension);
                    if(mimetype != null)
                    {
                        writer.setMimetype(mimetype);
                    }
                    
                    OutputStream os = writer.getContentOutputStream();
                    ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                    FileCopyUtils.copy(is, os);
                }
            }
        }
        else
        {
            // not TNEF
            NodeRef attachmentNode = fileFolderService.searchSimple(destinationFolder, fileName);
            if (attachmentNode == null)
            {
                /*
                 * If the node with the given name does not already exist
                 * Create the content node to contain the attachment
                 */
                FileInfo createdFile = fileFolderService.create(
                        destinationFolder,
                        fileName,
                        ContentModel.TYPE_CONTENT);
                
                attachmentNode = createdFile.getNodeRef();
                
                serviceRegistry.getNodeService().createAssociation(
                        messageFile,
                        attachmentNode,
                        ImapModel.ASSOC_IMAP_ATTACHMENT);
            

                // the part is a normal IMAP attachment
                ContentWriter writer = fileFolderService.getWriter(attachmentNode);
                writer.setMimetype(contentType.getBaseType());
        
                String charset = contentType.getParameter("charset");
                if(charset != null)
                {
                    writer.setEncoding(charset);
                }
        
                OutputStream os = writer.getContentOutputStream();
                FileCopyUtils.copy(part.getInputStream(), os);
            }
        }
    }
}
