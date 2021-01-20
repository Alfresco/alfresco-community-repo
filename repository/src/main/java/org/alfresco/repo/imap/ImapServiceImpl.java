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

import static org.alfresco.repo.imap.AlfrescoImapConst.DICTIONARY_TEMPLATE_PREFIX;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.mail.Flags;
import javax.mail.Header;
import javax.mail.Flags.Flag;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.imap.AlfrescoImapConst.ImapViewMode;
import org.alfresco.repo.imap.config.ImapConfigMountPointsBean;
import org.alfresco.repo.imap.exception.AlfrescoImapRuntimeException;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnRestoreNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteDoesNotExistException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.model.SubFolderFilter;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;
import org.alfresco.util.GUID;
import org.alfresco.util.MaxSizeMap;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.config.RepositoryFolderConfigBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.extensions.surf.util.I18NUtil;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.SimpleStoredMessage;

/**
 * @author Dmitry Vaserin
 * @author Arseny Kovalchuk
 * @author David Ward
 * @since 3.2
 */
public class ImapServiceImpl implements ImapService, OnRestoreNodePolicy, OnCreateChildAssociationPolicy, OnDeleteChildAssociationPolicy, OnUpdatePropertiesPolicy, BeforeDeleteNodePolicy
{
    private Log logger = LogFactory.getLog(ImapServiceImpl.class);

    private static final String ERROR_FOLDER_ALREADY_EXISTS = "imap.server.error.folder_already_exist";
    private static final String ERROR_MAILBOX_NAME_IS_MANDATORY = "imap.server.error.mailbox_name_is_mandatory";
    private static final String ERROR_CANNOT_GET_A_FOLDER = "imap.server.error.cannot_get_a_folder";
    private static final String ERROR_CANNOT_PARSE_DEFAULT_EMAIL = "imap.server.error.cannot_parse_default_email";
    
    private static final String CHECKED_NODES = "imap.flaggable.aspect.checked.list";
    private static final String FAVORITE_SITES = "imap.favorite.sites.list";
    private static final String UIDVALIDITY_TRANSACTION_LISTENER = "imap.uidvalidity.txn.listener";
    
    private SysAdminParams sysAdminParams;
    private FileFolderService fileFolderService;
    private NodeService nodeService;
    private PermissionService permissionService;
    private ServiceRegistry serviceRegistry;
    private BehaviourFilter policyBehaviourFilter;
    private NamespaceService namespaceService;
    private SearchService searchService;
    private AttachmentsExtractor attachmentsExtractor;

    // Note that this cache need not be cluster synchronized, as it is keyed by the cluster-safe 
    // change token.  Key is username, changeToken
    private Map<Pair<String, String>, FolderStatus> folderCache;
    private int folderCacheSize = 1000;
    private ReentrantReadWriteLock folderCacheLock = new ReentrantReadWriteLock();
    private SimpleCache<NodeRef, CacheItem> messageCache;
    private Map<String, ImapConfigMountPointsBean> imapConfigMountPoints;
    private Map<String, Integer> mountPointIds;
    private RepositoryFolderConfigBean[] ignoreExtractionFoldersBeans;
    private RepositoryFolderConfigBean imapHomeConfigBean;
    
    private NodeRef imapHomeNodeRef;
    private Set<NodeRef> ignoreExtractionFolders;

    private String defaultFromAddress;
    private String defaultToAddress;
    private String repositoryTemplatePath;
    private boolean extractAttachmentsEnabled = true;

    private Map<EmailBodyFormat, String> defaultBodyTemplates;

    private final static Map<QName, Flags.Flag> qNameToFlag;
    private final static Map<Flags.Flag, QName> flagToQname;

    private long imapServerShuffleMoveDeleteDelay = 5000L;
    private static final Timer deleteDelayTimer = new Timer();

    private boolean imapServerEnabled = false;

    private List<String> messageHeadersToPersist = Collections.<String>emptyList();

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

        public void setService(ImapServiceImpl service)
        {
            this.service = service;
        }

        @Override
        protected void onBootstrap(ApplicationEvent event)
        {
            service.startupInTxn(false);
        }

        @Override
        protected void onShutdown(ApplicationEvent event)
        {
            AuthenticationUtil.runAs(new RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    if (service.getImapServerEnabled())
                    {
                        service.shutdown();
                    }
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
    }

    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }
    
    public void setMessageCache(SimpleCache<NodeRef, CacheItem> messageCache)
    {
        this.messageCache = messageCache;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
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


    public void setAttachmentsExtractor(AttachmentsExtractor attachmentsExtractor)
    {
        this.attachmentsExtractor = attachmentsExtractor;
    }

    public void setImapHome(RepositoryFolderConfigBean imapHomeConfigBean)
    {
        this.imapHomeConfigBean = imapHomeConfigBean;
    }
    
    public void setFolderCacheSize(int folderCacheSize)
    {
        this.folderCacheSize = folderCacheSize;
    }

    public String getDefaultFromAddress()
    {
        return defaultFromAddress;
    }

    public void setDefaultFromAddress(String defaultFromAddress)
    {
        this.defaultFromAddress = defaultFromAddress;
    }
    
    public String getDefaultToAddress()
    {
        return defaultToAddress;
    }

    public void setDefaultToAddress(String defaultToAddress)
    {
        this.defaultToAddress = defaultToAddress;
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
        this.imapConfigMountPoints = new LinkedHashMap<String, ImapConfigMountPointsBean>(
                imapConfigMountPointsBeans.length * 2);
        this.mountPointIds = new HashMap<String, Integer>(imapConfigMountPointsBeans.length * 2);
        for (int i = 0; i < imapConfigMountPointsBeans.length; i++)
        {
            String name = imapConfigMountPointsBeans[i].getMountPointName();
            this.imapConfigMountPoints.put(name, imapConfigMountPointsBeans[i]);
            this.mountPointIds.put(name, i + 1);
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

    public void setImapServerEnabled(boolean enabled)
    {
        this.imapServerEnabled = enabled;
    }
    
    public void setMessageHeadersToPersist(List<String> headers)
    {
        this.messageHeadersToPersist  = headers;
    }

    public void setImapServerShuffleMoveDeleteDelay(long imapServerShuffleMoveDeleteDelay)
    {
        this.imapServerShuffleMoveDeleteDelay = imapServerShuffleMoveDeleteDelay;
    }
    
    public boolean getImapServerEnabled()
    {
        return this.imapServerEnabled;
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
        PropertyCheck.mandatory(this, "defaultToAddress", defaultToAddress);
        PropertyCheck.mandatory(this, "repositoryTemplatePath", repositoryTemplatePath);
        PropertyCheck.mandatory(this, "policyBehaviourFilter", policyBehaviourFilter);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "searchService", getSearchService());
        this.folderCache = new MaxSizeMap<Pair<String,String>, FolderStatus>(folderCacheSize, false);
        
        // be sure that a default e-mail is correct
        try
        {
            InternetAddress.parse(defaultFromAddress);
        }
        catch (AddressException ex)
        {
            throw new AlfrescoRuntimeException(
                    ERROR_CANNOT_PARSE_DEFAULT_EMAIL,
                    new Object[] {defaultFromAddress});
        }
        
        try
        {
            InternetAddress.parse(defaultToAddress);
        }
        catch (AddressException ex)
        {
            throw new AlfrescoRuntimeException(
                    ERROR_CANNOT_PARSE_DEFAULT_EMAIL,
                    new Object[] {defaultToAddress});
        }
    }

    /**
     * This method is run as System within a single transaction on startup.
     */
    public void startup()
    {
        bindBehaviour();
                        
        // Get NodeRefs for folders to ignore
        this.ignoreExtractionFolders = new HashSet<NodeRef>(ignoreExtractionFoldersBeans.length * 2);

        for (RepositoryFolderConfigBean ignoreExtractionFoldersBean : ignoreExtractionFoldersBeans)
        {
            NodeRef nodeRef = ignoreExtractionFoldersBean.getFolderPath(namespaceService, nodeService, searchService,
                    fileFolderService);

            if (!ignoreExtractionFolders.add(nodeRef))
            {
                // It was already in the set
                throw new AlfrescoRuntimeException("The folder extraction path has been referenced already: \n"
                        + "   Folder: " + ignoreExtractionFoldersBean);
            }
        }
        
        // Locate or create IMAP home
        imapHomeNodeRef = imapHomeConfigBean.getOrCreateFolderPath(namespaceService, nodeService, searchService, fileFolderService);        
    }

    public void shutdown()
    {
    }
    
    protected void startupInTxn(boolean force)
    {
        if (force || getImapServerEnabled())
        {
            AuthenticationUtil.runAs(new RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    List<AlfrescoImapFolder> mailboxes = serviceRegistry.getTransactionService().getRetryingTransactionHelper().doInTransaction(
                            new RetryingTransactionCallback<List<AlfrescoImapFolder>>()
                            {
                                @Override
                                public List<AlfrescoImapFolder> execute() throws Throwable
                                {
                                    startup();
                                    
                                    List<AlfrescoImapFolder> result = new LinkedList<AlfrescoImapFolder>();
                                    
                                    // Hit the mount points and warm the caches for early failure
                                    for (String mountPointName : imapConfigMountPoints.keySet())
                                    {
                                        result.addAll(listMailboxes(new AlfrescoImapUser(null, AuthenticationUtil
                                                .getSystemUserName(), null), mountPointName + "*", false));
                                    }
                                    
                                    return result;
                                }
                            });
                    
                    // Let each mailbox search trigger its own distinct transaction
                    for (AlfrescoImapFolder mailbox : mailboxes)
                    {
                        mailbox.getUidNext();
                    }
                    
                    return null;                    
                }
            }, AuthenticationUtil.getSystemUserName());
        }
    }

    protected void bindBehaviour()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("[bindBeahaviour] Binding behaviours");
        }
        PolicyComponent policyComponent = (PolicyComponent) serviceRegistry.getService(QName.createQName(NamespaceService.ALFRESCO_URI, "policyComponent"));

        // Only listen to folders we've tagged with imap properties - not all folders or we'll really slow down the repository!
        policyComponent.bindAssociationBehaviour(
                OnCreateChildAssociationPolicy.QNAME,
                ImapModel.ASPECT_IMAP_FOLDER,
                ContentModel.ASSOC_CONTAINS,
                new JavaBehaviour(this, "onCreateChildAssociation", NotificationFrequency.EVERY_EVENT));
        policyComponent.bindAssociationBehaviour(
                OnDeleteChildAssociationPolicy.QNAME,
                ImapModel.ASPECT_IMAP_FOLDER,
                ContentModel.ASSOC_CONTAINS,
                new JavaBehaviour(this, "onDeleteChildAssociation", NotificationFrequency.EVERY_EVENT));
        policyComponent.bindClassBehaviour(
                OnUpdatePropertiesPolicy.QNAME,
                ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.EVERY_EVENT));
        policyComponent.bindClassBehaviour(
                BeforeDeleteNodePolicy.QNAME,
                ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "beforeDeleteNode", NotificationFrequency.EVERY_EVENT));
        policyComponent.bindClassBehaviour(
                OnRestoreNodePolicy.QNAME,
                ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "onRestoreNode", NotificationFrequency.EVERY_EVENT));
    }

    // ---------------------- Service Methods --------------------------------

    public SimpleStoredMessage getMessage(FileInfo mesInfo) throws MessagingException
    {
        NodeRef nodeRef = mesInfo.getNodeRef();
        Date modified = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
        if(modified != null)
        {
            CacheItem cached =  messageCache.get(nodeRef);
            if (cached != null)
            {
                if (cached.getModified().equals(modified))
                {
                    return cached.getMessage();
                }
            }
            SimpleStoredMessage message = createImapMessage(mesInfo, true);
            messageCache.put(nodeRef, new CacheItem(modified, message));            
            return message;
        }
        else
        {
            SimpleStoredMessage message = createImapMessage(mesInfo, true);
            return message;
        }
    }
        
    public SimpleStoredMessage createImapMessage(FileInfo fileInfo, boolean generateBody) throws MessagingException
    {
        // TODO MER 26/11/2010- this test should really be that the content of the node is of type message/RFC822
        Long key = (Long) fileInfo.getProperties().get(ContentModel.PROP_NODE_DBID);
        if (nodeService.hasAspect(fileInfo.getNodeRef(), ImapModel.ASPECT_IMAP_CONTENT))
        {
            return new SimpleStoredMessage(new ImapModelMessage(fileInfo, serviceRegistry, generateBody), new Date(), key);
        }
        else
        {
            return new SimpleStoredMessage(new ContentModelMessage(fileInfo, serviceRegistry, generateBody), new Date(), key);
        }
    }

    public void expungeMessage(FileInfo fileInfo)
    {
        Flags flags = getFlags(fileInfo);
        if (flags.contains(Flags.Flag.DELETED))
        {
            // See MNT-12259
            //fileFolderService.delete(fileInfo.getNodeRef());
            hideAndDelete(fileInfo.getNodeRef());
            messageCache.remove(fileInfo.getNodeRef());
        }
    }
    
    /**
     * Workaround for MNT-12259
     * @param nodeRef NodeRef
     */
    @SuppressWarnings("deprecation")
    private void hideAndDelete(final NodeRef nodeRef)
    {
        FileFilterMode.setClient(FileFilterMode.Client.imap);
        fileFolderService.setHidden(nodeRef, true);
        {
            // Get the current user
            final String deleteDelayUser = AuthenticationUtil.getFullyAuthenticatedUser();
            // Add a timed task to really delete the file
            TimerTask deleteDelayTask = new TimerTask()
            {
                @Override
                public void run()
                {
                    RunAsWork<Void> deleteDelayRunAs = new RunAsWork<Void>()
                    {
                        @Override
                        public Void doWork() throws Exception
                        {
                            return serviceRegistry.getTransactionService().getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
                            {
                                @Override
                                public Void execute() throws Throwable
                                {
                                    // Ignore if it is NOT hidden: the shuffle may have finished; the operation may have failed
                                    if (!nodeService.exists(nodeRef) || !fileFolderService.isHidden(nodeRef))
                                    {
                                        return null;
                                    }

                                    // Since this will run in a different thread, the client thread-local must be set
                                    // or else unhiding the node will not unhide it for IMAP.
                                    FileFilterMode.setClient(FileFilterMode.Client.imap);

                                    // Unhide the node, e.g. for archiving
                                    fileFolderService.setHidden(nodeRef, false);

                                    // This is the transaction-aware service
                                    fileFolderService.delete(nodeRef);

                                    if (logger.isDebugEnabled())
                                    {
                                        logger.debug("Node has been async deleted " + nodeRef);
                                    }

                                    return null;
                                }
                            });
                        }
                    };
                    try
                    {
                        AuthenticationUtil.runAs(deleteDelayRunAs, deleteDelayUser);
                    }
                    catch (Throwable e)
                    {
                        // consume exception to avoid it leaking from the TimerTask and causing the Timer to
                        // no longer accept tasks to be scheduled.
                        logger.info("Exception thrown during IMAP delete timer task.", e);
                    }
                }
            };
            // Schedule a real delete 5 seconds after the current time
            if (logger.isDebugEnabled())
            {
                logger.debug("Delete timer is scheduled for " + nodeRef);
            }
            deleteDelayTimer.schedule(deleteDelayTask, imapServerShuffleMoveDeleteDelay);
        }
    }
    
    /**
     * @throws AlfrescoImapRuntimeException
     */
    public AlfrescoImapFolder getOrCreateMailbox(AlfrescoImapUser user, String mailboxName, boolean mayExist, boolean mayCreate)
    {
        if (mailboxName == null)
        {
            throw new IllegalArgumentException(I18NUtil.getMessage(ERROR_MAILBOX_NAME_IS_MANDATORY));
        }
        // A request for the hierarchy delimiter
        if (mailboxName.length() == 0)
        {
            return new AlfrescoImapFolder(user.getLogin(), this, serviceRegistry);
        }
        final NodeRef root;
        List<String> pathElements = null;
        ImapViewMode viewMode = ImapViewMode.ARCHIVE;
        int index = mailboxName.indexOf(AlfrescoImapConst.HIERARCHY_DELIMITER);
        int mountPointId = 0;

        String rootPath = (index > 0) ? (mailboxName.substring(0, index)) : (mailboxName);

        ImapConfigMountPointsBean imapConfigMountPoint = this.imapConfigMountPoints.get(rootPath);
        if (imapConfigMountPoint != null)
        {
            mountPointId = this.mountPointIds.get(rootPath);
            root = imapConfigMountPoint.getFolderPath(serviceRegistry.getNamespaceService(), nodeService, searchService, fileFolderService);

            if (index > 0)
            {
                pathElements = Arrays.asList(mailboxName.substring(index + 1).split(String.valueOf(AlfrescoImapConst.HIERARCHY_DELIMITER)));
            }

            viewMode = imapConfigMountPoint.getMode();
        }
        else
        {
            root = getUserImapHomeRef(user.getLogin());
            pathElements = Arrays.asList(mailboxName.split(String.valueOf(AlfrescoImapConst.HIERARCHY_DELIMITER)));
        }

        FileInfo mailFolder;
        try
        {
            if (null != pathElements)
            {
                mailFolder = fileFolderService.resolveNamePath(root, pathElements, !mayCreate);
            }
            else
            {
                mailFolder = fileFolderService.getFileInfo(root);
            }
        }
        catch (FileNotFoundException e)
        {
            throw new AlfrescoImapRuntimeException(ERROR_CANNOT_GET_A_FOLDER, new String[] { mailboxName }, new FolderException(FolderException.NOT_LOCAL));
        }
        if (mailFolder == null)
        {
            if (!mayCreate)
            {
                throw new AlfrescoImapRuntimeException(ERROR_CANNOT_GET_A_FOLDER, new String[] { mailboxName }, new FolderException(FolderException.NOT_LOCAL));
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("Creating mailbox: " + mailboxName);
            }
            mailFolder = FileFolderUtil.makeFolders(fileFolderService, root, pathElements, ContentModel.TYPE_FOLDER);
        }
        else
        {
            if (!mayExist)
            {
                throw new AlfrescoImapRuntimeException(ERROR_FOLDER_ALREADY_EXISTS, new FolderException(FolderException.ALREADY_EXISTS_LOCALLY));
            }
        }
        String path = (null != pathElements) ? (pathElements.get(pathElements.size() - 1)) : (rootPath);
        return new AlfrescoImapFolder(mailFolder, user.getLogin(), path, mailboxName, viewMode, this, serviceRegistry, true, isExtractionEnabled(mailFolder.getNodeRef()),
                mountPointId);
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

        AlfrescoImapFolder folder = getOrCreateMailbox(user, mailboxName, true, false);
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
		
		AlfrescoImapFolder sourceNode = getOrCreateMailbox(user, oldMailboxName, true, false);
			  
        if (logger.isDebugEnabled())
        {
            logger.debug("Renaming folder oldMailboxName=" + oldMailboxName + " newMailboxName=" + newMailboxName);
        }
        
        NodeRef newMailParent;
        String newMailName;
        int index = newMailboxName.lastIndexOf(AlfrescoImapConst.HIERARCHY_DELIMITER);
        if (index < 0)
        {
            newMailParent = getUserImapHomeRef(user.getLogin());
            newMailName = newMailboxName;
        }
        else
        {
            newMailParent = getOrCreateMailbox(user, newMailboxName.substring(0, index), true, true).getFolderInfo().getNodeRef();
            newMailName = newMailboxName.substring(index + 1);
        }

        try
        {
            if (oldMailboxName.equalsIgnoreCase(AlfrescoImapConst.INBOX_NAME))
            {
                // If you trying to rename INBOX
                // - just copy it to another folder with new name
                // and leave INBOX (with children) intact.
                fileFolderService.copy(sourceNode.getFolderInfo().getNodeRef(), newMailParent,
                        AlfrescoImapConst.INBOX_NAME);
            }
            else
            {
                fileFolderService.move(sourceNode.getFolderInfo().getNodeRef(), newMailParent, newMailName);
            }
        }
        catch (FileNotFoundException e)
        {
            throw new AlfrescoRuntimeException(e.getMessage(), e);
        }
        catch (FileExistsException e)
        {
            throw new AlfrescoRuntimeException(e.getMessage(), e);
        }
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
    public FolderStatus getFolderStatus(final String userName, final NodeRef contextNodeRef, ImapViewMode viewMode)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("getFolderStatus contextNodeRef=" + contextNodeRef + ", viewMode=" + viewMode);
        }

        // No need to ACL check the change token read
        String changeToken = AuthenticationUtil.runAs(new RunAsWork<String>()
        {
            @Override
            public String doWork() throws Exception
            {
                return (String) nodeService.getProperty(contextNodeRef, ImapModel.PROP_CHANGE_TOKEN);
            }
        }, AuthenticationUtil.getSystemUserName());

        Pair<String, String> cacheKey = null;
        if (changeToken != null)
        {
            cacheKey = new Pair<String, String>(userName, changeToken);
            this.folderCacheLock.readLock().lock();
            try
            {
                FolderStatus result = this.folderCache.get(cacheKey);
                if (result != null)
                {
                    return result;
                }
            }
            finally
            {
                this.folderCacheLock.readLock().unlock();
            }
        }
        
        List<FileInfo> fileInfos = null;
        FileFilterMode.setClient(Client.imap);
        try
        {
            fileInfos = fileFolderService.listFiles(contextNodeRef);
        }
        finally
        {
            FileFilterMode.clearClient();
        }
        
        final NavigableMap<Long, FileInfo> currentSearch = new TreeMap<Long, FileInfo>();

        switch (viewMode)
        {
        case MIXED:
            for (FileInfo fileInfo : fileInfos)
            {
                currentSearch.put((Long) fileInfo.getProperties().get(ContentModel.PROP_NODE_DBID), fileInfo);
            }
            break;
        case ARCHIVE:
            for (FileInfo fileInfo : fileInfos)
            {
                if (nodeService.hasAspect(fileInfo.getNodeRef(), ImapModel.ASPECT_IMAP_CONTENT))
                {
                    currentSearch.put((Long) fileInfo.getProperties().get(ContentModel.PROP_NODE_DBID), fileInfo);
                }
            }
            break;
        case VIRTUAL:
            for (FileInfo fileInfo : fileInfos)
            {
                if (!nodeService.hasAspect(fileInfo.getNodeRef(), ImapModel.ASPECT_IMAP_CONTENT))
                {
                    currentSearch.put((Long) fileInfo.getProperties().get(ContentModel.PROP_NODE_DBID), fileInfo);
                }
            }
            break;
        }

        int messageCount = currentSearch.size(), recentCount = 0, unseenCount = 0, firstUnseen = 0;
        int i = 1;
        for (FileInfo fileInfo : currentSearch.values())
        {
            Flags flags = getFlags(fileInfo);
            if (flags.contains(Flags.Flag.RECENT))
            {
                recentCount++;
            }
            if (!flags.contains(Flags.Flag.SEEN))
            {
                if (firstUnseen == 0)
                {
                    firstUnseen = i;
                }
                unseenCount++;
            }
            i++;
        }
        // Add the IMAP folder aspect with appropriate initial values if it is not already there
        if (changeToken == null)
        {
            changeToken = GUID.generate();
            cacheKey = new Pair<String, String>(userName, changeToken);
            final String finalToken = changeToken;
            doAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    nodeService.setProperty(contextNodeRef, ImapModel.PROP_CHANGE_TOKEN, finalToken);
                    nodeService.setProperty(contextNodeRef, ImapModel.PROP_MAXUID, currentSearch.isEmpty() ? 0
                            : currentSearch.lastKey());
                    return null;
                }
            });
        }
        Long uidValidity = (Long) nodeService.getProperty(contextNodeRef, ImapModel.PROP_UIDVALIDITY);
        FolderStatus result = new FolderStatus(messageCount, recentCount, firstUnseen, unseenCount,
                uidValidity == null ? 0 : uidValidity, changeToken, currentSearch);
        this.folderCacheLock.writeLock().lock();
        try
        {
            FolderStatus oldResult = this.folderCache.get(cacheKey);
            if (oldResult != null)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("At end of getFolderStatus. Found info in cache, changeToken:" + changeToken);
                }
         
                return oldResult;
            }
            this.folderCache.put(cacheKey, result);

            if(logger.isDebugEnabled())
            {
                logger.debug("At end of getFolderStatus. Found files:" + currentSearch.size() + ", changeToken:" + changeToken);
            }
            return result;
        }
        finally
        {
            this.folderCacheLock.writeLock().unlock();
        }
    }

    public void subscribe(AlfrescoImapUser user, String mailbox)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Subscribing: " + user + ", " + mailbox);
        }
        AlfrescoImapFolder mailFolder = getOrCreateMailbox(user, mailbox, true, false);
        PersonService personService = serviceRegistry.getPersonService();
        NodeRef userRef = personService.getPerson(user.getLogin());

        nodeService.removeAssociation(userRef, mailFolder.getFolderInfo().getNodeRef(), ImapModel.ASSOC_IMAP_UNSUBSCRIBED);
    }

    public void unsubscribe(AlfrescoImapUser user, String mailbox)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Unsubscribing: " + user + ", " + mailbox);
        }
        AlfrescoImapFolder mailFolder = getOrCreateMailbox(user, mailbox, true, false);
        if(mailFolder.getFolderInfo() != null)
        {
            PersonService personService = serviceRegistry.getPersonService();
            NodeRef userRef = personService.getPerson(user.getLogin());
            nodeService.createAssociation(userRef, mailFolder.getFolderInfo().getNodeRef(), ImapModel.ASSOC_IMAP_UNSUBSCRIBED);
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
    public Flags getFlags(FileInfo messageInfo)
    {
        Flags flags = new Flags();
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
    public void setFlags(FileInfo messageInfo, Flags flags, boolean value)
    {
        checkForFlaggableAspect(messageInfo.getNodeRef());
        

        for (Flags.Flag flag : flags.getSystemFlags())
        {
            setFlag(messageInfo, flag, value);
        }
    }

    /**
     * Set flags to the specified message.
     * 
     * @param messageInfo FileInfo of imap Folder
     * @param flag flag to set.
     * @param value value value to set.
     */
    public void setFlag(FileInfo messageInfo, Flag flag, boolean value)
    {
        setFlag(messageInfo.getNodeRef(), flag, value);
    }

    private void setFlag(NodeRef nodeRef, Flag flag, boolean value)
    {
        String permission = (flag == Flag.DELETED ? PermissionService.DELETE_NODE : PermissionService.WRITE_PROPERTIES);
        
        
        AccessStatus status = permissionService.hasPermission(nodeRef, permission);
        if (status == AccessStatus.DENIED)
        {
            if(flag == Flag.DELETED)
            {
                logger.debug("[setFlag] Access denied to set DELETED FLAG:" + nodeRef);
                throw new AccessDeniedException("No permission to set DELETED flag");
            }
            if(flag == Flag.SEEN)
            {
                logger.debug("[setFlag] Access denied to set SEEN FLAG:" + nodeRef);
                //TODO - should we throw an exception here?
                //throw new AccessDeniedException("No permission to set DELETED flag");
            }
            else
            {
               
                logger.debug("[setFlag] Access denied to set flag:" + nodeRef);
                throw new AccessDeniedException("No permission to set flag:" + flag.toString());
            }
        }
        else
        {
            checkForFlaggableAspect(nodeRef);
            policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
            policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
            try
            {                    
                if(logger.isDebugEnabled())
                {
                    logger.debug("set flag nodeRef:" + nodeRef + ",flag:" + flagToQname.get(flag) + ", value:" + value);
                }
                nodeService.setProperty(nodeRef, flagToQname.get(flag), value);
                messageCache.remove(nodeRef);
            }
            finally
            {
                policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
                policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);                
            }
        }
    }

    /**
     * Depend on listSubscribed param, list Mailboxes or list subscribed Mailboxes
     */
    public List<AlfrescoImapFolder> listMailboxes(AlfrescoImapUser user, String mailboxPattern, boolean listSubscribed)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("[listMailboxes] user:" + user.getLogin() + ", mailboxPattern:" + mailboxPattern + ", listSubscribed:" + listSubscribed);
        }
        List<AlfrescoImapFolder> result = new LinkedList<AlfrescoImapFolder>();
        
        // List mailboxes that are in mount points
        int index = mailboxPattern.indexOf(AlfrescoImapConst.HIERARCHY_DELIMITER);
        String rootPath = index == -1 ? mailboxPattern : mailboxPattern.substring(0, index);
        boolean found = false;
        
        String userName = user.getLogin();
        Set<NodeRef> unsubscribedFodlers = getUnsubscribedFolders(userName);

        for (String mountPointName : imapConfigMountPoints.keySet())
        {
            if (mountPointName.matches(rootPath.replaceAll("[%\\*]", ".*")))
            {
                NodeRef mountPoint = getMountPoint(mountPointName);
                if (mountPoint != null)
                {
                    int mountPointId = mountPointIds.get(mountPointName);
                    FileInfo mountPointFileInfo = fileFolderService.getFileInfo(mountPoint);
                    ImapViewMode viewMode = imapConfigMountPoints.get(mountPointName).getMode();
                    if (index < 0)
                    {

                        if (!listSubscribed || !unsubscribedFodlers.contains(mountPointFileInfo.getNodeRef()))
                        {
                            result.add(new AlfrescoImapFolder(mountPointFileInfo, userName, mountPointName, mountPointName, viewMode,
                                    isExtractionEnabled(mountPointFileInfo.getNodeRef()), this, serviceRegistry, mountPointId));
                        }
                        else if (rootPath.endsWith("%") && !expandFolder(mountPoint, user, mountPointName, "%", true, viewMode, mountPointId).isEmpty()) // \NoSelect 
                        {
                            result.add(new AlfrescoImapFolder(mountPointFileInfo, userName, mountPointName, mountPointName, viewMode,
                                    this, serviceRegistry, false, isExtractionEnabled(mountPointFileInfo.getNodeRef()), mountPointId));
                        }
                        if (rootPath.endsWith("*"))
                        {
                            result.addAll(expandFolder(mountPoint, user, mountPointName, "*", listSubscribed, viewMode, mountPointId));                            
                        }                        
                    }
                    else
                    {
                        result.addAll(expandFolder(mountPoint, user, mountPointName,
                                mailboxPattern.substring(index + 1), listSubscribed, viewMode, mountPointId));
                    }
                }
                // If we had an exact match, there is no point continuing to search
                if (mountPointName.equals(rootPath))
                {
                    found = true;
                    break;
                }
            }
        }

        // List mailboxes that are in user IMAP Home
        if (!found)
        {
            NodeRef root = getUserImapHomeRef(user.getLogin());
            result.addAll(expandFolder(root, user, "", mailboxPattern, listSubscribed, ImapViewMode.ARCHIVE, 0));
        }
            
        logger.debug("listMailboxes returning size:" + result.size());
        
        return result;
    }
    
    /**
     * Recursively search the given root to get a list of folders
     * 
     * @return List<AlfrescoImapFolder>
     */
    private List<AlfrescoImapFolder> expandFolder(
            NodeRef root,
            AlfrescoImapUser user,
            String rootPath,
            String mailboxPattern,
            boolean listSubscribed,
            ImapViewMode viewMode,
            int mountPointId)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("expand folder: root:" + root + " user: " + user + " :mailboxPattern=" + mailboxPattern);
        }
        if (mailboxPattern == null)
            return null;
        int index = mailboxPattern.indexOf(AlfrescoImapConst.HIERARCHY_DELIMITER);

        String name = null;
        if (index < 0)
        {
            name = mailboxPattern;
        }
        else
        {
            name = mailboxPattern.substring(0, index);
        }
        String rootPathPrefix = rootPath.length() == 0 ? "" : rootPath + AlfrescoImapConst.HIERARCHY_DELIMITER; 

        if (logger.isDebugEnabled())
        {
            logger.debug("Listing mailboxes: name=" + name);
        }

        List<AlfrescoImapFolder> fullList = new LinkedList<AlfrescoImapFolder>();
        ImapSubFolderFilter filter = new ImapSubFolderFilter(viewMode, name.replace('%', '*'), Arrays.asList("calendar", "dataLists"));
        List<FileInfo> list;
        // Only list this folder if we have a wildcard name. Otherwise do a direct lookup by name.
        if (name.contains("*") || name.contains("%"))
        {
            FileFilterMode.setClient(Client.imap);
            try
            {
                // Check if the mount point is site root
                // then shortcut to user's favorite sites
                // MNT-12055
                if (serviceRegistry.getSiteService().getSiteRoot().equals(root))
                {
                    list = fileFolderService.toFileInfoList(filter.favs);
                }
                else
                {
                    list = fileFolderService.listFolders(root);
                }
            }
            finally
            {
                FileFilterMode.clearClient();
            }
        }
        else
        {
            NodeRef nodeRef = fileFolderService.searchSimple(root, name);
            FileInfo fileInfo;
            list = nodeRef == null || !(fileInfo = fileFolderService.getFileInfo(nodeRef)).isFolder() ? Collections.<FileInfo>emptyList() : Collections.singletonList(fileInfo);
        }
        
        if (index < 0)
        {
            String userName = user.getLogin();
            Set<NodeRef> unsubscribedFodlers = getUnsubscribedFolders(userName);

            // This is the last level            
            for (FileInfo fileInfo : list)
            {
                if (!filter.isEnterSubfolder(fileInfo.getNodeRef()))
                {
                    continue;
                }
                String folderPath = rootPathPrefix + fileInfo.getName();
                if (!listSubscribed || !unsubscribedFodlers.contains(fileInfo.getNodeRef()))
                {
                    fullList.add(new AlfrescoImapFolder(fileInfo, userName, fileInfo.getName(), folderPath, viewMode,
                            isExtractionEnabled(fileInfo.getNodeRef()), this, serviceRegistry, mountPointId));
                }
                else if (name.endsWith("%") && !expandFolder(fileInfo.getNodeRef(), user, folderPath, "%", true, viewMode, mountPointId).isEmpty()) // \NoSelect 
                {
                    fullList.add(new AlfrescoImapFolder(fileInfo, userName, fileInfo.getName(), folderPath, viewMode,
                            this, serviceRegistry, false, isExtractionEnabled(fileInfo.getNodeRef()), mountPointId));
                }
                if (name.endsWith("*"))
                {
                    fullList.addAll(expandFolder(fileInfo.getNodeRef(), user, folderPath, "*", listSubscribed, viewMode, mountPointId));                            
                }
            }
        }
        else
        {    
            // If (index != -1) this is not the last level
            for (FileInfo folder : list)
            {
                if (!filter.isEnterSubfolder(folder.getNodeRef()))
                {
                    continue;
                }
                fullList.addAll(expandFolder(folder.getNodeRef(), user, rootPathPrefix + folder.getName(),
                        mailboxPattern.substring(index + 1), listSubscribed, viewMode, mountPointId));
            }
        }
        return fullList;
    }

    /**
     * Map of mount points. Name of mount point == key in the map.
     * 
     * @return Map of mount points.
     */
    private NodeRef getMountPoint(String rootFolder)
    {
        final ImapConfigMountPointsBean config = imapConfigMountPoints.get(rootFolder);
        try
        {
            // Get node reference. Do it in new transaction to avoid RollBack in case when AccessDeniedException is thrown.
            return serviceRegistry.getTransactionService().getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>()
            {
                public NodeRef execute() throws Exception
                {
                    try
                    {
                        return config.getFolderPathOrNull(namespaceService, nodeService, searchService, fileFolderService);
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

    /**
     * Get the node ref of the user's imap home.   Will create it on demand if it 
     * does not already exist.
     * 
     * @param userName user name
     * @return user IMAP home reference and create it if it doesn't exist.
     */
    public NodeRef getUserImapHomeRef(final String userName)
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

    private Set<NodeRef> getUnsubscribedFolders(String userName)
    {
        Set<NodeRef> result = new HashSet<NodeRef>();
        PersonService personService = serviceRegistry.getPersonService();

        NodeRef userRef = null;

        if (personService.personExists(userName))
        {
            userRef = personService.getPerson(userName);
        }

        if (userRef != null)
        {
            List<AssociationRef> unsubscribedFodlers = nodeService.getTargetAssocs(userRef, ImapModel.ASSOC_IMAP_UNSUBSCRIBED);
            for (AssociationRef asocRef : unsubscribedFodlers)
            {
                result.add(asocRef.getTargetRef());
            }
        }

        return result;
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
        PersonService personService = serviceRegistry.getPersonService();
        
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
            
            if (personService.personExists(userName))
            {
                PreferenceService preferenceService = (PreferenceService) serviceRegistry.getService(ServiceRegistry.PREFERENCE_SERVICE);
                Map<String, Serializable> prefs = preferenceService.getPreferences(userName, AlfrescoImapConst.PREF_IMAP_FAVOURITE_SITES);

                for (String key : prefs.keySet())
                {
                    Boolean isImapFavourite = (Boolean) prefs.get(key);
                    if (isImapFavourite != null && isImapFavourite)
                    {
                        String siteName = key.substring(AlfrescoImapConst.PREF_IMAP_FAVOURITE_SITES.length() + 1); // count the dot
                        boolean isMember = false;
                        try
                        {
                            isMember = serviceRegistry.getSiteService().isMember(siteName, userName);
                        }
                        catch (SiteDoesNotExistException sdne)
                        {
                            // Ignore, see MNT-13579
                            // The site might be archived. In this case it will still be in user's preferences.
                        }

                        if (isMember)
                        {
                            SiteInfo siteInfo = serviceRegistry.getSiteService().getSite(siteName);
                            if (siteInfo != null)
                            {
                                if (logger.isDebugEnabled())
                                {
                                    logger.debug("[getFavouriteSites] User: " + userName + " Favourite site: " + siteInfo.getShortName());
                                }
                                favSites.add(siteInfo.getNodeRef());
                            }
                        }
                    }
                }
                if (logger.isDebugEnabled())
                {
                    logger.debug("[getFavouriteSites] Bind new Favorite sites' list to transaction " + AlfrescoTransactionSupport.getTransactionId());
                }
                AlfrescoTransactionSupport.bindResource(FAVORITE_SITES, favSites);
            }
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("[getFavouriteSites] end for user: " + userName);
        }

        return favSites;
    }

    /**
     * Checks for the existence of the flaggable aspect and adds it if it is not already present on the folder. 
     * @param nodeRef NodeRef
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
                    policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
                    policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
                    logger.debug("[checkForFlaggableAspect] Adding flaggable aspect to nodeRef: " + nodeRef);
                    Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
                    nodeService.addAspect(nodeRef, ImapModel.ASPECT_FLAGGABLE, aspectProperties);
                }
                finally
                {
                    policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
                    policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
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
                    
                    NodeRef rootNode = nodeService.getRootNode(storeRef);
                    
                    List<NodeRef> templates = searchService.selectNodes(rootNode, query, null, namespaceService, true);
                    if (templates == null || templates.size() == 0)
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("template not found:" + templateName);
                        }
                        throw new AlfrescoRuntimeException(String.format("[getDefaultEmailBodyTemplate] IMAP message template '%1$s' does not exist in the path '%2$s'.", templateName, repositoryTemplatePath));
                    }
                    final NodeRef defaultLocaleTemplate = templates.get(0);
      
                    NodeRef localisedSibling = serviceRegistry.getFileFolderService().getLocalizedSibling(defaultLocaleTemplate);
                    result = localisedSibling.toString();
                }
                // We are catching all exceptions. E.g. search service can possibly throw an exceptions on malformed queries.
                catch (Exception e)
                {
                    logger.error("ImapServiceImpl [getDefaultEmailBodyTemplate]", e);
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
        /**
         * Exclude folders which represent components with these IDs
         */
        private List<String> excludedComponentIds;
        
        ImapSubFolderFilter(ImapViewMode imapViewMode)
        {
            this.imapViewMode = imapViewMode;
            this.typesToExclude = ImapServiceImpl.this.serviceRegistry.getDictionaryService().getSubTypes(SiteModel.TYPE_SITE, true);
            this.favs = getFavouriteSites(getCurrentUser());
        }
        
        ImapSubFolderFilter(ImapViewMode imapViewMode, String mailboxPattern)
        {
            this(imapViewMode);
            this.mailboxPattern = mailboxPattern.replaceAll("\\*", "(.)*");;
        }

        ImapSubFolderFilter(ImapViewMode imapViewMode, String mailboxPattern, List<String> excludedComponentIds)
        {
            this(imapViewMode, mailboxPattern);
            this.excludedComponentIds = excludedComponentIds;
        }
        
        @Override
        public boolean isEnterSubfolder(ChildAssociationRef subfolderRef)
        {
            return isEnterSubfolder(subfolderRef.getChildRef());
        }

        private boolean containsIgnoreCase(String s, List<String> list)
        {
            return list.stream().anyMatch((e) -> e.equalsIgnoreCase(s));
        }
        
        public boolean isEnterSubfolder(NodeRef folder)
        {        
            String name = (String) nodeService.getProperty(folder, ContentModel.PROP_NAME);
            if (mailboxPattern != null)
            {
                logger.debug("Child name: " + name);
                if (logger.isDebugEnabled())
                {
                    logger.debug("Folder name: " + name + ". Pattern: " + mailboxPattern + ". Matches: " + name.matches(mailboxPattern));
                }
                if (!name.matches(mailboxPattern))
                    return false;
            }

            /**
             * Exclude folders which represent unsupported components, like calendar and dataLists.
             * See REPO-830
             */
            if (excludedComponentIds != null && !excludedComponentIds.isEmpty())
            {
                String componentId = (String) nodeService.getProperty(folder, SiteModel.PROP_COMPONENT_ID);
                if (componentId != null && containsIgnoreCase(componentId, excludedComponentIds))
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("[ImapSubFolderFilter] Excluding folder with name: " + name
                                + " because its componentID is: " + componentId);
                    }
                    return false;
                }
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
                        logger.debug("[ImapSubFolderFilter] (VIRTUAL) including fav site folder :" + name);
                        return true;
                    }
                    else
                    {
                        logger.debug("[ImapSubFolderFilter] (VIRTUAL) excluding non fav site folder :" + name);
                        return false;
                    }
                }
                else
                {
                    /**
                     * IN ARCHIVE MODE we don't display folders for any SITES, regardless of whether they are favourites.
                     */
                    logger.debug("[ImapSubFolderFilter] (ARCHIVE) excluding site folder :" + name);
                    return false;
                }
            }
            return true;
        }
        
    }

    private UidValidityTransactionListener getUidValidityTransactionListener(NodeRef folderRef)
    {
        String key = UIDVALIDITY_TRANSACTION_LISTENER + folderRef.toString();
        UidValidityTransactionListener txnListener = AlfrescoTransactionSupport.getResource(key);
        if (txnListener == null)
        {
            txnListener = new UidValidityTransactionListener(folderRef);
            AlfrescoTransactionSupport.bindListener(txnListener);
            AlfrescoTransactionSupport.bindResource(key, txnListener);
        }
        return txnListener;        
    }

    @Override
    public void onCreateChildAssociation(final ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        doAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                NodeRef childNodeRef = childAssocRef.getChildRef();
                
                if (serviceRegistry.getDictionaryService().isSubClass(nodeService.getType(childNodeRef), ContentModel.TYPE_CONTENT))
                {
                    long newId = (Long) nodeService.getProperty(childNodeRef, ContentModel.PROP_NODE_DBID);
                    // Keep a record of minimum and maximum node IDs in this folder in this transaction and add a listener that will
                    // update the UIDVALIDITY and MAXUID properties appropriately. Also force generation of a new change token
                    getUidValidityTransactionListener(childAssocRef.getParentRef()).recordNewUid(newId);
                    // Flag new content as recent
                    setFlag(childNodeRef, Flags.Flag.RECENT, true);
                }
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("[onCreateChildAssociation] Association " + childAssocRef + " created. CHANGETOKEN will be changed.");
                }
                return null;
            }
        });
    }
    
    @Override
    public void onDeleteChildAssociation(final ChildAssociationRef childAssocRef)
    {
        doAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                NodeRef childNodeRef = childAssocRef.getChildRef();
                if (serviceRegistry.getDictionaryService().isSubClass(nodeService.getType(childNodeRef),
                        ContentModel.TYPE_CONTENT))
                {
                    // Force generation of a new change token
                    getUidValidityTransactionListener(childAssocRef.getParentRef());

                    // Remove the message from the cache
                    messageCache.remove(childNodeRef);
                }
                if (logger.isDebugEnabled())
                {
                    logger.debug("[onDeleteChildAssociation] Association " + childAssocRef
                            + " created. CHANGETOKEN will be changed.");
                }
                return null;
            }
        });
    }
    
    @Override
    public void onUpdateProperties(final NodeRef nodeRef, final Map<QName, Serializable> before,
            final Map<QName, Serializable> after)
    {
        doAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            { 
                /**
                 * Imap only cares about a few properties however if those properties 
                 * change then the uidvalidity needs to be reset otherwise the new content
                 * won't get re-loaded.   This is nonsense for an email server, but needed for 
                 * modifiable repository.  Also we need to ignore certain properties.
                 */
                boolean hasChanged = false;
                
                if(!hasChanged)
                {
                    hasChanged = !EqualsHelper.nullSafeEquals(before.get(ContentModel.PROP_NAME), after.get(ContentModel.PROP_NAME));
                }
                if(!hasChanged)
                {
                    hasChanged = !EqualsHelper.nullSafeEquals(before.get(ContentModel.PROP_AUTHOR), after.get(ContentModel.PROP_AUTHOR));
                }
                if(!hasChanged)
                {
                    hasChanged = !EqualsHelper.nullSafeEquals(before.get(ContentModel.PROP_TITLE), after.get(ContentModel.PROP_TITLE));
                }
                if(!hasChanged)
                {
                    hasChanged = !EqualsHelper.nullSafeEquals(before.get(ContentModel.PROP_DESCRIPTION), after.get(ContentModel.PROP_DESCRIPTION));
                }
                
                if(!hasChanged)
                {
                    Serializable s1 = before.get(ContentModel.PROP_CONTENT);
                    Serializable s2 = after.get(ContentModel.PROP_CONTENT);
                
                    if(s1 != null && s2 != null)
                    {
                        ContentData c1 = (ContentData)s1;
                        ContentData c2 = (ContentData)s2;
                        
                        hasChanged = !EqualsHelper.nullSafeEquals(c1.getContentUrl(), c2.getContentUrl());
                    }
                }
                
                for (ChildAssociationRef parentAssoc : nodeService.getParentAssocs(nodeRef))
                {
                    NodeRef folderRef = parentAssoc.getParentRef();
                    if (nodeService.hasAspect(folderRef, ImapModel.ASPECT_IMAP_FOLDER))
                    {
                        messageCache.remove(nodeRef);

                        // Force generation of a new change token for the parent folders
                        UidValidityTransactionListener listener = getUidValidityTransactionListener(folderRef);
                
                        // if we have a significant change then we need to force a new uidvalidity.
                        if(hasChanged)
                        {
                            logger.debug("message has changed - force new uidvalidity for the parent folder");
                            listener.forceNewUidvalidity();
                        }
                    }
                }
                return null;
            }
        });
    }

    @Override
    public void onRestoreNode(final ChildAssociationRef childAssocRef)
    {
        doAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                NodeRef childNodeRef = childAssocRef.getChildRef();
                if (serviceRegistry.getDictionaryService().isSubClass(nodeService.getType(childNodeRef), ContentModel.TYPE_CONTENT))
                {
                    setFlag(childNodeRef, Flags.Flag.DELETED, false);
                    setFlag(childNodeRef, Flags.Flag.SEEN, false);
                }

                NodeRef folderRef = childAssocRef.getParentRef();
                long newId = (Long) nodeService.getProperty(childNodeRef, ContentModel.PROP_NODE_DBID);
                if (nodeService.hasAspect(folderRef, ImapModel.ASPECT_IMAP_FOLDER))
                {
                    // Force generation of a new change token and updating the UIDVALIDITY 
                    getUidValidityTransactionListener(folderRef).recordNewUid(newId);
                }
                return null;
            }
        });
    }

    @Override
    public void beforeDeleteNode(final NodeRef nodeRef)
    {
        doAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                for (ChildAssociationRef parentAssoc : nodeService.getParentAssocs(nodeRef))
                {
                    NodeRef folderRef = parentAssoc.getParentRef();
                    if (nodeService.hasAspect(folderRef, ImapModel.ASPECT_IMAP_FOLDER))
                    {
                        messageCache.remove(nodeRef);

                        // Force generation of a new change token
                        getUidValidityTransactionListener(folderRef);
                    }
                }
                return null;
            }
        });
    }

    private <R> R doAsSystem(RunAsWork<R> work)
    {
        policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
        policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
        try
        {
            return AuthenticationUtil.runAs(work, AuthenticationUtil.getSystemUserName());
        }
        finally
        {
            policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
            policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
        }
    }

    private class UidValidityTransactionListener extends TransactionListenerAdapter
    {
        // Generate a unique token for each folder change with which we can validate session caches
        private String changeToken = GUID.generate();
        private NodeRef folderNodeRef;
        private Long minUid;
        private Long maxUid;
        private boolean forceNewUidValidity = false;
        
        public UidValidityTransactionListener(NodeRef folderNodeRef)
        {
            this.folderNodeRef = folderNodeRef;
        }
        
        public void forceNewUidvalidity()
        {
            this.forceNewUidValidity = true;
        }
        
        public void recordNewUid(long newUid)
        {
            if (this.minUid == null)
            {
                this.minUid = this.maxUid = newUid;
            }
            else if (newUid < this.minUid)
            {
                this.minUid = newUid;
            }
            else if (newUid > this.maxUid)
            {
                this.maxUid = newUid;
            }
        }

        @Override
        public void beforeCommit(boolean readOnly)
        {
            if (readOnly)
            {
                return;
            }

            doAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    // This fires at the end of the transaction, so double-check that the node is still present
                    if (!nodeService.exists(folderNodeRef))
                    {
                        return null;
                    }
                    
                    if (UidValidityTransactionListener.this.forceNewUidValidity || UidValidityTransactionListener.this.minUid != null)
                    {
                        long modifDate = System.currentTimeMillis();
                        Long oldMax = (Long) nodeService.getProperty(folderNodeRef, ImapModel.PROP_MAXUID);
                        // Only update UIDVALIDITY if a new node has and ID that is smaller or equals the old maximum (as UIDs are always meant to increase)
                        if (UidValidityTransactionListener.this.forceNewUidValidity || oldMax == null || UidValidityTransactionListener.this.minUid <= oldMax)
                        {
                            nodeService.setProperty(folderNodeRef, ImapModel.PROP_UIDVALIDITY, modifDate);                            
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("UIDVALIDITY was modified for folder, nodeRef:" + folderNodeRef);
                            }
                        }
                        if(UidValidityTransactionListener.this.maxUid != null)
                        {
                            nodeService.setProperty(folderNodeRef, ImapModel.PROP_MAXUID, UidValidityTransactionListener.this.maxUid);
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("MAXUID was modified for folder, nodeRef:" + folderNodeRef);
                            }
                        }
                    }
                    nodeService.setProperty(folderNodeRef, ImapModel.PROP_CHANGE_TOKEN, changeToken);                            
                    return null;
                }                        
            });
        }
    }

    public NodeRef getNodeSiteContainer(final NodeRef inputNodeRef)
    {
        return doAsSystem(new RunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork() throws Exception
            {
                NodeRef nodeRef = inputNodeRef;
                while (true)
                {
                    NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
                    if (parent == null)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("The node with nodeRef:" + inputNodeRef + " is not in the site.");
                        }
                        nodeRef = null;
                        break;
                    }
                    nodeRef = parent;
                    if (nodeService.hasAspect(parent, SiteModel.ASPECT_SITE_CONTAINER))
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("The node with nodeRef:" + inputNodeRef + " is in the site.");
                        }
                        // found the container
                        break;
                    }
                }
                return nodeRef;
            }
        });

    }

    public String getContentFolderUrl(NodeRef contentNodeRef)
    {
        String url = "";
        String CONTAINER_URL_TEMPLATE = "%s/page/site/%s";
        String REPOSITORY_URL_TEMPLATE = "%s/page/%s";

        NodeRef siteContainerNodeRef = getNodeSiteContainer(contentNodeRef);

        if (siteContainerNodeRef != null)
        {
            // the node is in site's container
            // determine which one

            NodeRef siteNodeRef = nodeService.getPrimaryParent(siteContainerNodeRef).getParentRef();
            String siteName = ((String) nodeService.getProperty(siteNodeRef, ContentModel.PROP_NAME)).toLowerCase();
            String componentId = (String) nodeService.getProperty(siteContainerNodeRef, SiteModel.PROP_COMPONENT_ID);
            switch (componentId.toLowerCase())
            {
                case "datalists":
                    url = String.format(CONTAINER_URL_TEMPLATE, getShareApplicationContextUrl(), siteName + "/data-lists");
                    break;
                case "wiki":
                    url = String.format(CONTAINER_URL_TEMPLATE, getShareApplicationContextUrl(), siteName + "/wiki");
                    break;
                case "links":
                    url = String.format(CONTAINER_URL_TEMPLATE, getShareApplicationContextUrl(), siteName + "/links");
                    break;
                case "calendar":
                    url = String.format(CONTAINER_URL_TEMPLATE, getShareApplicationContextUrl(), siteName + "/calendar");
                    break;
                case "discussions":
                    url = String.format(CONTAINER_URL_TEMPLATE, getShareApplicationContextUrl(), siteName + "/discussions-topiclist");
                    break;
                case "blog":
                    url = String.format(CONTAINER_URL_TEMPLATE, getShareApplicationContextUrl(), siteName + "/blog-postlist");
                    break;
                case "documentlibrary":
                    String pathFromSites = getPathFromSites(nodeService.getPrimaryParent(contentNodeRef).getParentRef());
                    StringBuilder parsedPath = new StringBuilder();
                    String[] pathParts = pathFromSites.split("/");
                    if (pathParts.length > 2)
                    {
                        parsedPath.append(pathParts[0] + "/" + pathParts[1]);
                        parsedPath.append("?filter=path|");
                        for (int i = 2; i < pathParts.length; i++)
                        {
                            parsedPath.append("/").append(pathParts[i]);
                        }
                    }
                    else
                    {
                        parsedPath.append(pathFromSites);
                    }
                    url = String.format(CONTAINER_URL_TEMPLATE, getShareApplicationContextUrl(), parsedPath.toString());
                    break;
                default:
                    break;
            }
        }
        else
        {
            // the node is in repository
            String pathFromRepo = getPathFromRepo(nodeService.getPrimaryParent(contentNodeRef));
            StringBuilder parsedPath = new StringBuilder();
            String[] pathParts = pathFromRepo.split("/");
            if (pathParts.length > 1)
            {
                parsedPath.append(pathParts[0]);
                parsedPath.append("?filter=path|");
                for (int i = 1; i < pathParts.length; i++)
                {
                    parsedPath.append("/").append(pathParts[i]);
                }
            }
            else
            {
                parsedPath.append(pathFromRepo);
            }
            url = String.format(REPOSITORY_URL_TEMPLATE, getShareApplicationContextUrl(), parsedPath.toString());
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("Resolved content folder URL:" + url + " for node " + contentNodeRef);
        }
        return url;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public NamespaceService getNamespaceService()
    {
        return namespaceService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public SearchService getSearchService()
    {
        return searchService;
    }

    public void extractAttachments(NodeRef messageRef, MimeMessage originalMessage) throws IOException, MessagingException
    {
        attachmentsExtractor.extractAttachments(messageRef, originalMessage);
    }

    public String generateUniqueFilename(NodeRef destFolderNodeRef, String fileName)
    {
        if(fileFolderService.searchSimple(destFolderNodeRef, fileName) != null)
        {
            String name = fileName;
            String ext = "";
            if (fileName.lastIndexOf(".") != -1)
            {
                int index = fileName.lastIndexOf(".");
                name = fileName.substring(0, index);
                ext = fileName.substring(index);
            }
            int copyNum = 0;
            do
            {
                copyNum++;
            } while (fileFolderService.searchSimple(destFolderNodeRef, name + " (" + copyNum + ")" + ext) != null);
                fileName =  name + " (" + copyNum + ")" + ext;
        }
     	
        return fileName;
    }
    
    @SuppressWarnings("unchecked")
    public void persistMessageHeaders(NodeRef messageRef, MimeMessage message)
    {
        try
        {
            Enumeration<Header> headers = message.getAllHeaders();
            List<String> messaheHeadersProperties = new ArrayList<String>();
            while(headers.hasMoreElements())
            {
                Header header = headers.nextElement();
                if (isPersistableHeader(header))
                {
                    messaheHeadersProperties.add(header.getName() + ImapModel.MESSAGE_HEADER_TO_PERSIST_SPLITTER + header.getValue());
                    
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("[persistHeaders] Persisting Header " + header.getName() + " : " + header.getValue());
                    }
                }
            }
            
            Map<QName, Serializable> props = new HashMap<QName, Serializable>();
            props.put(ImapModel.PROP_MESSAGE_HEADERS, (Serializable)messaheHeadersProperties);
            
            serviceRegistry.getNodeService().addAspect(messageRef, ImapModel.ASPECT_IMAP_MESSAGE_HEADERS, props);
        }
        catch(MessagingException me)
        {
            
        }
    }
    
    private boolean isPersistableHeader(Header header)
    {
        for (String headerToPersist : messageHeadersToPersist)
        {
            if (headerToPersist.equalsIgnoreCase(header.getName()))
            {
                return true;
            }
        }
        return false; 
    }
    
    static class CacheItem
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

    @Override
    public String getPathFromSites(final NodeRef ref)
    {
        return doAsSystem(new RunAsWork<String>()
        {
            @Override
            public String doWork() throws Exception
            {
                String name = ((String) nodeService.getProperty(ref, ContentModel.PROP_NAME)).toLowerCase();
                if (nodeService.getType(ref).equals(SiteModel.TYPE_SITE))
                {
                    return name;
                }
                else
                {
                    NodeRef parent = nodeService.getPrimaryParent(ref).getParentRef();
                    return getPathFromSites(parent) + "/" + name;
                }
            }
        });
    }

    @Override
    public String getPathFromRepo(final ChildAssociationRef assocRef)
    {
        return doAsSystem(new RunAsWork<String>()
        {
            @Override
            public String doWork() throws Exception
            {
                NodeRef ref = assocRef.getParentRef();
                String name = ((String) nodeService.getProperty(ref, ContentModel.PROP_NAME)).toLowerCase();
                ChildAssociationRef parent = nodeService.getPrimaryParent(ref);
                QName qname = parent.getQName();
                if (qname.equals(QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "company_home")))
                {
                    return "repository";
                }
                else
                {
                    return getPathFromRepo(parent) + "/" + name;
                }
            }
        });
    }
}