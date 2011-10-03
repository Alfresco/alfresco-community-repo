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
package org.alfresco.repo.admin.patch.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.URLDecoder;

/**
 * Patch to migrate the AVM 'sitestore' Remote Store content to the new ADM
 * location for surf-configuration under the Sites folder in 4.0.
 * 
 * @see org.alfresco.repo.web.scripts.bean.ADMRemoteStore
 * @author Kevin Roast
 * @since 4.0
 */
public class AVMToADMRemoteStorePatch extends AbstractPatch
{
    private static final Log logger = LogFactory.getLog(AVMToADMRemoteStorePatch.class);
    
    private static final String MSG_MIGRATION_COMPLETE = "patch.avmToAdmRemoteStore.complete";
    private static final String SITE_CACHE_ID = "_SITE_CACHE";
    
    // patterns used to match site and user specific configuration locations
    // @see org.alfresco.repo.web.scripts.bean.ADMRemoteStore
    private static final Pattern USER_PATTERN_1 = Pattern.compile(".*/components/.*\\.user~(.*)~.*");
    private static final Pattern USER_PATTERN_2 = Pattern.compile(".*/pages/user/(.*?)(/.*)?$");
    private static final Pattern SITE_PATTERN_1 = Pattern.compile(".*/components/.*\\.site~(.*)~.*");
    private static final Pattern SITE_PATTERN_2 = Pattern.compile(".*/pages/site/(.*?)(/.*)?$");
    // name of the surf config folder
    private static final String SURF_CONFIG = "surf-config";
    
    private static final int SITE_BATCH_THREADS = 8;
    private static final int SITE_BATCH_SIZE = 100;
    private static final int MIGRATE_BATCH_THREADS = 8;
    private static final int MIGRATE_BATCH_SIZE = 100;
    
    private Map<String, NodeRef> siteReferenceCache = null;
    private SortedMap<String, AVMNodeDescriptor> paths;
    private SortedMap<String, AVMNodeDescriptor> retryPaths;
    private NodeRef surfConfigRef = null;
    private ThreadLocal<Pair<String, NodeRef>> lastFolderCache = new ThreadLocal<Pair<String,NodeRef>>()
    {
        protected Pair<String,NodeRef> initialValue()
        {
            return new Pair<String, NodeRef>("", null);
        };
    };
    
    private ContentService contentService;
    private FileFolderService fileFolderService;
    private SiteService siteService;
    private AVMService avmService;
    private RuleService ruleService;
    private String avmStore;
    private String avmRootPath = "/";
    
    
    /**
     * @param contentService    the ContentService to set
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService; 
    }

    /**
     * @param fileFolderService the FileFolderService to set
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    /**
     * @param siteService       the SiteService to set
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    /**
     * @param avmService        the avmService to set
     */
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }
    
    /**
     * @param ruleService       the rule service to set
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    /**
     * @param avmStore          the avmStore to set
     */
    public void setAvmStore(String avmStore)
    {
        this.avmStore = avmStore;
    }

    /**
     * @param avmRootPath       the avmRootPath to set
     */
    public void setAvmRootPath(String avmRootPath)
    {
        if (avmRootPath != null && avmRootPath.length() != 0)
        {
            this.avmRootPath = avmRootPath;
        }
    }

    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(avmService, "avmService");
        checkPropertyNotNull(avmStore, "avmStore");
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {
        this.retryPaths = new TreeMap<String, AVMNodeDescriptor>();
        
        // firstly retrieve all AVM paths and descriptors that we need to process
        // execute in a single transaction to retrieve the stateless object list
        RetryingTransactionCallback<Void> work = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                long start = System.currentTimeMillis();
                paths = retrieveAVMPaths();
                logger.info("Retrieved: " + paths.size() + " AVM paths in " + (System.currentTimeMillis()-start) + "ms");
                
                // also calculate the surf-config reference under the Sites folder while in the txn
                surfConfigRef = getSurfConfigNodeRef(siteService.getSiteRoot());
                
                // pre-create folders that may cause contention later during multi-threaded batch processing
                List<String> folderPath = new ArrayList<String>();
                folderPath.add("components");
                FileFolderUtil.makeFolders(fileFolderService, surfConfigRef, folderPath, ContentModel.TYPE_FOLDER);
                folderPath.clear();
                folderPath.add("pages");
                folderPath.add("user");
                FileFolderUtil.makeFolders(fileFolderService, surfConfigRef, folderPath, ContentModel.TYPE_FOLDER);
                
                return null;
            }
        };
        this.transactionHelper.doInTransaction(work, false, true);
        
        try
        {
            // init the siteid to surf-config noderef cache
            this.siteReferenceCache = new ConcurrentHashMap<String, NodeRef>(16384);
            
            // get user names that will be used to RunAs and set permissions later
            String systemUser = AuthenticationUtil.getSystemUserName();
            final String tenantSystemUser = this.tenantAdminService.getDomainUser(
                    systemUser, this.tenantAdminService.getCurrentUserDomain());
            
            // build a set of unique site names
            final Set<String> sites = new HashSet<String>(paths.size());
            Matcher matcher;
            for (String path: paths.keySet())
            {
                String siteName = null;
                if ((matcher = SITE_PATTERN_1.matcher(path)).matches())
                {
                    siteName = matcher.group(1);
                }
                else if ((matcher = SITE_PATTERN_2.matcher(path)).matches())
                {
                    siteName = matcher.group(1);
                }
                if (siteName != null)
                {
                    sites.add(siteName);
                }
            }
            
            // retrieve the sites for the batch work provider
            final Iterator<String> siteItr = sites.iterator();
            
            // the work provider for the site 'surf-config' folder pre-create step
            BatchProcessWorkProvider<String> siteWorkProvider = new BatchProcessWorkProvider<String>()
            {
                @Override
                public synchronized Collection<String> getNextWork()
                {
                    int batchCount = 0;
                    
                    List<String> siteBatch = new ArrayList<String>(SITE_BATCH_SIZE);
                    while (siteItr.hasNext() && batchCount++ != SITE_BATCH_SIZE)
                    {
                        siteBatch.add(siteItr.next());
                    }
                    return siteBatch;
                }
                
                @Override
                public synchronized int getTotalEstimatedWorkSize()
                {
                    return sites.size();
                }
            };
            
            // batch process the sites in the set and pre-create the 'surf-config' folders for each site
            // add each config folder noderef to our cache ready for the config file migration processing
            BatchProcessor<String> siteBatchProcessor = new BatchProcessor<String>(
                    "AVMToADMRemoteStorePatch",
                    this.transactionHelper,
                    siteWorkProvider,
                    SITE_BATCH_THREADS,
                    SITE_BATCH_SIZE,
                    this.applicationEventPublisher,
                    logger,
                    SITE_BATCH_SIZE * 10);
            
            BatchProcessWorker<String> siteWorker = new BatchProcessWorker<String>()
            {
                @Override
                public void beforeProcess() throws Throwable
                {
                    AuthenticationUtil.setRunAsUser(tenantSystemUser);
                }
                
                @Override
                public void afterProcess() throws Throwable
                {
                    AuthenticationUtil.clearCurrentSecurityContext();
                }
                
                @Override
                public String getIdentifier(String entry)
                {
                    return entry;
                }
                
                @Override
                public void process(String siteName) throws Throwable
                {
                    // get the Site NodeRef
                    NodeRef siteRef = getSiteNodeRef(siteName);
                    if (siteRef != null)
                    {
                        // create the 'surf-config' folder for the site and cache the NodeRef to it
                        NodeRef surfConfigRef = getSurfConfigNodeRef(siteRef);
                        // TODO: create components and pages folders here would also reduce contention
                        siteReferenceCache.put(siteName, surfConfigRef);
                    }
                    else
                    {
                        logger.info("WARNING: unable to find site id: " + siteName);
                    }
                }
            };
            long start = System.currentTimeMillis();
            siteBatchProcessor.process(siteWorker, true);
            logger.info("Created 'surf-config' folders for: " + this.siteReferenceCache.size() + " sites in " + (System.currentTimeMillis()-start) + "ms");
            
            // retrieve AVM NodeDescriptor objects for the paths
            final Iterator<String> pathItr = this.paths.keySet().iterator();
            
            // the work provider for the config file migration
            BatchProcessWorkProvider<AVMNodeDescriptor> migrateWorkProvider = new BatchProcessWorkProvider<AVMNodeDescriptor>()
            {
                @Override
                public synchronized Collection<AVMNodeDescriptor> getNextWork()
                {
                    int batchCount = 0;
                    
                    List<AVMNodeDescriptor> nodes = new ArrayList<AVMNodeDescriptor>(MIGRATE_BATCH_SIZE);
                    while (pathItr.hasNext() && batchCount++ != MIGRATE_BATCH_SIZE)
                    {
                        nodes.add(paths.get(pathItr.next()));
                    }
                    return nodes;
                }
                
                @Override
                public synchronized int getTotalEstimatedWorkSize()
                {
                    return paths.size();
                }
            };
            
            // prepare the batch processor and worker object
            BatchProcessor<AVMNodeDescriptor> batchProcessor = new BatchProcessor<AVMNodeDescriptor>(
                    "AVMToADMRemoteStorePatch",
                    this.transactionHelper,
                    migrateWorkProvider,
                    MIGRATE_BATCH_THREADS,
                    MIGRATE_BATCH_SIZE,
                    this.applicationEventPublisher,
                    logger,
                    MIGRATE_BATCH_SIZE * 10);
            
            BatchProcessWorker<AVMNodeDescriptor> worker = new BatchProcessWorker<AVMNodeDescriptor>()
            {
                @Override
                public void beforeProcess() throws Throwable
                {
                    ruleService.disableRules();
                    AuthenticationUtil.setRunAsUser(tenantSystemUser);
                }
                
                @Override
                public void afterProcess() throws Throwable
                {
                    ruleService.enableRules();
                    AuthenticationUtil.clearCurrentSecurityContext();
                }
                
                @Override
                public String getIdentifier(AVMNodeDescriptor entry)
                {
                    return entry.getPath();
                }
                
                @Override
                public void process(AVMNodeDescriptor entry) throws Throwable
                {
                    migrateNode(entry);
                }
            };
            batchProcessor.process(worker, true);
            
            // retry the paths that were blocked due to multiple threads attemping to create
            // the same folder at the same time - these are dealt with now in a single thread!
            if (this.retryPaths.size() != 0)
            {
                logger.info("Retrying " + this.retryPaths.size() + " paths...");
                work = new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Exception
                    {
                        for (String path : retryPaths.keySet())
                        {
                            migrateNode(retryPaths.get(path));
                        }
                        return null;
                    }
                };
                this.transactionHelper.doInTransaction(work, false, true);
            }
            
            logger.info("Migrated: " + this.paths.size() + " AVM nodes to DM in " + (System.currentTimeMillis()-start) + "ms");
        }
        finally
        {
            // dispose of our cache
            this.siteReferenceCache = null;
        }
        
        return I18NUtil.getMessage(MSG_MIGRATION_COMPLETE);
    }
    
    /**
     * Migrate a single AVM node. Match, convert and copy the AVM surf config path to
     * the new ADM surf-config folder location, creating appropriate sub-folders and
     * finally copying the content from the AVM to the DM.
     * 
     * @param avmNode   AVMNodeDescriptor
     */
    private void migrateNode(final AVMNodeDescriptor avmNode)
    {
        String path = avmNode.getPath();
        
        final boolean debug = logger.isDebugEnabled();
        // what type of path is this?
        int index = path.indexOf(this.avmRootPath);
        if (index != -1)
        {
            // crop path removing the early paths we are not interested in
            path = path.substring(index + this.avmRootPath.length());
            if (debug) logger.debug("...processing path: " + path);
            
            // break down the path into its component elements to generate the parent folders
            List<String> pathElements = new ArrayList<String>(4);
            final StringTokenizer t = new StringTokenizer(path, "/");
            // the remainining path is of the form /<objecttype>[/<folder>]/<file>.xml
            while (t.hasMoreTokens())
            {
                pathElements.add(t.nextToken());
            }
            
            // match path against generic, user and site
            String userId = null;
            String siteName = null;
            Matcher matcher;
            if ((matcher = USER_PATTERN_1.matcher(path)).matches())
            {
                userId = URLDecoder.decode(matcher.group(1));
            }
            else if ((matcher = USER_PATTERN_2.matcher(path)).matches())
            {
                userId = URLDecoder.decode(matcher.group(1));
            }
            else if ((matcher = SITE_PATTERN_1.matcher(path)).matches())
            {
                siteName = matcher.group(1);
            }
            else if ((matcher = SITE_PATTERN_2.matcher(path)).matches())
            {
                siteName = matcher.group(1);
            }
            
            NodeRef surfConfigRef;
            if (siteName != null)
            {
                if (debug) logger.debug("...resolved site id: " + siteName);
                surfConfigRef = siteReferenceCache.get(siteName);
                if (surfConfigRef == null)
                {
                    logger.info("WARNING: unable to migrate path as site id cannot be found: " + siteName);
                }
            }
            else if (userId != null)
            {
                if (debug) logger.debug("...resolved user id: " + userId);
                surfConfigRef = this.surfConfigRef;
            }
            else
            {
                if (debug) logger.debug("...resolved generic path.");
                surfConfigRef = this.surfConfigRef;
            }
            
            // ensure folders exist down to the specified parent
            List<String> folderPath = pathElements.subList(0, pathElements.size() - 1);
            NodeRef parentFolder = null;
            Pair<String, NodeRef> lastFolderCache = this.lastFolderCache.get();
            String folderKey = (siteName != null) ? siteName + folderPath.toString() : folderPath.toString();
            if (folderKey.equals(lastFolderCache.getFirst()))
            {
                // found match to last used folder NodeRef
                if (debug) logger.debug("...cache hit - matched last folder reference: " + folderKey);
                parentFolder = lastFolderCache.getSecond();
            }
            if (parentFolder == null)
            {
                try
                {
                    parentFolder = FileFolderUtil.makeFolders(
                            this.fileFolderService,
                            surfConfigRef,
                            folderPath,
                            ContentModel.TYPE_FOLDER).getNodeRef();
                }
                catch (FileExistsException fe)
                {
                    // this occurs if a different thread running a separate txn has created a folder
                    // that we expected to exist - save a reference to this path to retry it again later
                    logger.warn("Unable to create folder: " + fe.getName() + " for path: " + avmNode.getPath() +
                                " - as another txn is busy, will retry later.");
                    retryPaths.put(avmNode.getPath(), avmNode);
                    return;
                }
                // save in last folder cache
                lastFolderCache.setFirst(folderKey);
                lastFolderCache.setSecond(parentFolder);
            }
            
            try
            {
                if (userId != null)
                {
                    // run as the appropriate user id to execute
                    AuthenticationUtil.pushAuthentication();
                    AuthenticationUtil.setFullyAuthenticatedUser(userId);
                    try
                    {
                        // create new node and perform writer content copy of the content from the AVM to the DM store
                        FileInfo fileInfo = fileFolderService.create(
                                parentFolder, avmNode.getName(), ContentModel.TYPE_CONTENT);
                        ContentWriter writer = contentService.getWriter(
                                fileInfo.getNodeRef(), ContentModel.PROP_CONTENT, true);
                        writer.putContent(avmService.getContentReader(-1, avmNode.getPath()));
                    }
                    finally
                    {
                        AuthenticationUtil.popAuthentication();
                    }
                }
                else
                {
                    // create new node and perform writer content copy of the content from the AVM to the DM store
                    FileInfo fileInfo = fileFolderService.create(
                            parentFolder, avmNode.getName(), ContentModel.TYPE_CONTENT);
                    ContentWriter writer = contentService.getWriter(
                            fileInfo.getNodeRef(), ContentModel.PROP_CONTENT, true);
                    writer.putContent(avmService.getContentReader(-1, avmNode.getPath()));
                }
            }
            catch (InvalidNodeRefException refErr)
            {
                // this occurs if a different thread running a separate txn has not yet created a folder
                // that we expected to exist - save a reference to this path to retry it again later
                logger.warn("Parent folder does not exist yet: " + refErr.getNodeRef() + " for path: " + avmNode.getPath() +
                            " - as another txn is busy, will retry later.");
                retryPaths.put(avmNode.getPath(), avmNode);
            }
        }
    }
    
    /**
     * @param shortName     Site shortname
     * 
     * @return the given Site folder node reference
     */
    private NodeRef getSiteNodeRef(String shortName)
    {
        SiteInfo siteInfo = this.siteService.getSite(shortName); 
        return siteInfo != null ? siteInfo.getNodeRef() : null;
    }
    
    /**
     * Return the "surf-config" noderef under the given root. Create the folder if it
     * does not exist yet.
     * 
     * @param rootRef   Parent node reference where the "surf-config" folder should be
     * 
     * @return surf-config folder ref
     */
    private NodeRef getSurfConfigNodeRef(final NodeRef rootRef)
    {
        NodeRef surfConfigRef = this.nodeService.getChildByName(
                rootRef, ContentModel.ASSOC_CONTAINS, SURF_CONFIG);
        if (surfConfigRef == null)
        {
            if (logger.isDebugEnabled())
                logger.debug("'surf-config' folder not found under current path, creating...");
            QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, SURF_CONFIG);
            Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1, 1.0f);
            properties.put(ContentModel.PROP_NAME, (Serializable) SURF_CONFIG);
            try
            {
                ChildAssociationRef ref = this.nodeService.createNode(
                        rootRef, ContentModel.ASSOC_CONTAINS, assocQName, ContentModel.TYPE_FOLDER, properties);
                surfConfigRef = ref.getChildRef();
            }
            catch (DuplicateChildNodeNameException dupErr)
            {
                // This exception is excepted in multi-threaded creation scenarios - but since fix for
                // ALF-10280 rev 30468 - it no longer automatically retries in the txn - therefore we
                // throw out an exception that does retry instead.
                throw new ConcurrencyFailureException("Forcing batch retry due to DuplicateChildNodeNameException" , dupErr);
            }
        }
        return surfConfigRef;
    }
    
    /**
     * @return the AVM paths for surf config object in the AVM sitestore 
     */
    private SortedMap<String, AVMNodeDescriptor> retrieveAVMPaths() throws Exception
    {
        logger.info("Retrieving paths from AVM store: " + this.avmStore + ":" + this.avmRootPath);
        
        SortedMap<String, AVMNodeDescriptor> paths = new TreeMap<String, AVMNodeDescriptor>();
        
        String avmPath = this.avmStore + ":" + this.avmRootPath;
        AVMNodeDescriptor node = this.avmService.lookup(-1, avmPath);
        if (node != null)
        {
            traverseNode(paths, node);
        }
        
        logger.info("Found: " + paths.size() + " AVM files nodes to migrate");
        
        return paths;
    }
    
    private void traverseNode(final SortedMap<String, AVMNodeDescriptor> paths, final AVMNodeDescriptor node)
        throws IOException
    {
        final boolean debug = logger.isDebugEnabled();
        final SortedMap<String, AVMNodeDescriptor> listing = this.avmService.getDirectoryListing(node);
        for (final AVMNodeDescriptor n : listing.values())
        {
            if (n.isFile())
            {
                if (debug) logger.debug("...adding path: " + n.getPath());
                paths.put(n.getPath(), n);
                if (paths.size() % 10000 == 0)
                {
                    logger.info("Collected " + paths.size() + " AVM paths...");
                }
            }
            else if (n.isDirectory())
            {
                traverseNode(paths, n);
            }
        }
    }
}
