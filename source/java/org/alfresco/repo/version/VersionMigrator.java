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
package org.alfresco.repo.version;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.impl.MigrateVersionStorePatch;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorkerAdaptor;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Version2 Migrator
 */
public class VersionMigrator implements ApplicationEventPublisherAware
{
    protected static Log logger = LogFactory.getLog(VersionMigrator.class);
    
    public static final StoreRef VERSION_STORE_REF_OLD = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, VersionModel.STORE_ID);
    public static final StoreRef VERSION_STORE_REF_NEW = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID);
    
    private static final String MSG_PATCH_NOOP = "version_service.migration.patch.noop";
    private static final String MSG_PATCH_COMPLETE = "version_service.migration.patch.complete";
    private static final String MSG_PATCH_IN_PROGRESS = "version_service.migration.patch.in_progress";
    private static final String MSG_PATCH_SKIP1 = "version_service.migration.patch.warn.skip1";
    private static final String MSG_PATCH_SKIP2 = "version_service.migration.patch.warn.skip2";
    
    private static final String MSG_DELETE_COMPLETE = "version_service.migration.delete.complete";
    private static final String MSG_DELETE_SKIP1    = "version_service.migration.delete.warn.skip1";
    private static final String MSG_DELETE_SKIP2    = "version_service.migration.delete.warn.skip2";
    
    private static boolean busy = false;
    
    private static final String CLEAN_OLD_VERSION_STORE = "CleanOldVersionStore";
    private static final String MIGRATE_VERSION_STORE = "MigrateVersionStore";

    public final static String PREFIX_MIGRATED = "migrated-";
    
    private VersionServiceImpl version1Service = new VersionServiceImpl();
    
    private Version2ServiceImpl version2Service;
    private NodeService dbNodeService;
    private BehaviourFilter policyBehaviourFilter;
    private DictionaryService dictionaryService;
    private TransactionService transactionService;
    private NodeService versionNodeService; // NodeService impl which redirects to appropriate VersionService
    private RuleService ruleService;
    private JobLockService jobLockService;
    private ApplicationEventPublisher applicationEventPublisher;
    
    private Boolean migrationComplete = null;
    
    private int loggingInterval = 500;
    
    //private String lockToken = null;
    
    public void setVersion2ServiceImpl(Version2ServiceImpl versionService)
    {
        this.version2Service = versionService;
    }
    
    public void setDbNodeService(NodeService nodeService)
    {
        this.dbNodeService = nodeService;
    }
    
    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter)
    {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    public void setVersionNodeService(NodeService versionNodeService)
    {
        this.versionNodeService = versionNodeService;
    }
    
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }
    
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }
    
    public void setLoggingInterval(int loggingInterval)
    {
        this.loggingInterval = loggingInterval;
    }
    
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) 
    {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    
    public void init()
    {
        version1Service.setNodeService(dbNodeService);
        version1Service.setDbNodeService(dbNodeService);
        
        version2Service.setDbNodeService(dbNodeService);
    }
    
    public NodeRef migrateVersionHistory(NodeRef oldVHNodeRef, NodeRef versionedNodeRef)
    {
        VersionHistory vh = v1BuildVersionHistory(oldVHNodeRef, versionedNodeRef);
        
        // create new version history node
        NodeRef newVHNodeRef = v2CreateVersionHistory(versionedNodeRef);
        
        Version[] oldVersions = (Version[])vh.getAllVersions().toArray(new Version[]{});
        
        // Disable auditable behaviour - so that migrated versions maintain their original auditable properties (eg. created, creator)
        this.policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
        
        try
        {
            for (int i = (oldVersions.length-1); i >= 0; i--)
            {
                // migrate versions
                v2CreateNewVersion(newVHNodeRef, oldVersions[i]);
            }
        }
        finally
        {
            // Enable auditable behaviour
            this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
        }
        
        return newVHNodeRef;
    }
    
    private NodeRef v2CreateVersionHistory(NodeRef nodeRef)
    {
        return version2Service.createVersionHistory(nodeRef);
    }
    
    private NodeRef v2CreateNewVersion(NodeRef newVersionHistoryRef, Version oldVersion)
    {
        NodeRef versionedNodeRef = oldVersion.getVersionedNodeRef();     // nodeRef to versioned node in live store
        NodeRef frozenStateNodeRef = oldVersion.getFrozenStateNodeRef(); // nodeRef to version node in version store
        
        String versionLabel = oldVersion.getVersionLabel();
        String versionDescription = oldVersion.getDescription();
        
        QName sourceType = versionNodeService.getType(frozenStateNodeRef);
        Set<QName> nodeAspects = versionNodeService.getAspects(frozenStateNodeRef);
        Map<QName, Serializable> nodeProperties = versionNodeService.getProperties(frozenStateNodeRef);
        List<ChildAssociationRef> nodeChildAssocs = versionNodeService.getChildAssocs(frozenStateNodeRef);
        List<AssociationRef> nodeAssocs = versionNodeService.getTargetAssocs(frozenStateNodeRef, RegexQNamePattern.MATCH_ALL);
        
        long nodeDbId = (Long)nodeProperties.get(ContentModel.PROP_NODE_DBID);
        
        // ALFCOM-2658
        nodeProperties.put(ContentModel.PROP_NODE_UUID, frozenStateNodeRef.getId());
        
        int versionNumber = 0;
        
        // get oldVersion auditable properties (of the version node itself, rather than the live versioned node)
        NodeRef versionNode = VersionUtil.convertNodeRef(frozenStateNodeRef);
        Date versionCreated = (Date)dbNodeService.getProperty(versionNode, ContentModel.PROP_CREATED);
        String versionCreator = (String)dbNodeService.getProperty(versionNode, ContentModel.PROP_CREATOR);
        Date versionModified = (Date)dbNodeService.getProperty(versionNode, ContentModel.PROP_MODIFIED);
        String versionModifier = (String)dbNodeService.getProperty(versionNode, ContentModel.PROP_MODIFIER);
        Date versionAccessed = (Date)dbNodeService.getProperty(versionNode, ContentModel.PROP_ACCESSED);
        
        Map<String, Serializable> versionMetaDataProperties = version1Service.getVersionMetaData(versionNode);
        
        // Create the node details
        PolicyScope nodeDetails = new PolicyScope(sourceType);
        
        // add properties
        for (Map.Entry<QName, Serializable> entry : nodeProperties.entrySet())
        {
            nodeDetails.addProperty(sourceType, entry.getKey(), entry.getValue());
        }
        
        // add newVersion auditable properties (of the version node itself, rather than the live versioned node) - will be set on create
        nodeDetails.addProperty(sourceType, ContentModel.PROP_CREATED, versionCreated);
        nodeDetails.addProperty(sourceType, ContentModel.PROP_CREATOR, versionCreator);
        nodeDetails.addProperty(sourceType, ContentModel.PROP_MODIFIED, versionModified);
        nodeDetails.addProperty(sourceType, ContentModel.PROP_MODIFIER, versionModifier);
        nodeDetails.addProperty(sourceType, ContentModel.PROP_ACCESSED, versionAccessed);
        
        // add aspects
        for (QName aspect : nodeAspects)
        {
            // add aspect
            nodeDetails.addAspect(aspect);
            
            // copy the aspect properties
            ClassDefinition classDefinition = dictionaryService.getClass(aspect);
            if (classDefinition != null)
            {  
                Map<QName,PropertyDefinition> propertyDefinitions = classDefinition.getProperties();
                for (QName propertyName : propertyDefinitions.keySet())
                {
                    Serializable propValue = nodeProperties.get(propertyName);
                    nodeDetails.addProperty(aspect, propertyName, propValue);
                }
            }
        }
        
        // add child assocs (since 3.3 Ent - applies only to direct upgrade from 2.x to 3.3 Ent or higher)
        for (ChildAssociationRef childAssoc : nodeChildAssocs)
        {
            nodeDetails.addChildAssociation(sourceType, childAssoc);
        }
        
        // add target assocs (since 3.3 Ent - applies only to direct upgrade from 2.x to 3.3 Ent or higher)
        for (AssociationRef assoc : nodeAssocs)
        {
            nodeDetails.addAssociation(sourceType, assoc);
        }
        
        NodeRef newVersionRef = version2Service.createNewVersion(
                sourceType,
                newVersionHistoryRef,
                version2Service.getStandardVersionProperties(versionedNodeRef, nodeDbId, nodeAspects, versionNumber, versionLabel, versionDescription),
                versionMetaDataProperties,
                versionNumber,
                nodeDetails);
        
        return newVersionRef;
    }

    /**
     * Check whether the V1 history represented by oldVersionHistoryRef represents
     * a versioned working copy node (Alfresco V2.1.7 can create these)
     * 
     * @param oldVersionHistoryRef
     * @return
     */
    protected boolean v1CheckForVersionedWorkingCopy(NodeRef oldVersionHistoryRef)
    {
        boolean valid = true;

        // Get versioned nodeRef from one of the versions - note: assumes all versions refer to the same versioned nodeRef
        Collection<ChildAssociationRef> versions = dbNodeService.getChildAssocs(oldVersionHistoryRef);
        if (versions.size() > 0)
        {
            Iterator<ChildAssociationRef> itr = versions.iterator();
            ChildAssociationRef childAssocRef = itr.next();
            NodeRef versionRef = childAssocRef.getChildRef();
            
            Version version = version1Service.getVersion(versionRef);
            @SuppressWarnings("unchecked")
            List<QName> frozenAspects = (List<QName>)dbNodeService.getProperty(versionRef, VersionModel.PROP_QNAME_FROZEN_ASPECTS);
            if(frozenAspects.contains(ContentModel.ASPECT_WORKING_COPY) && frozenAspects.contains(ContentModel.ASPECT_VERSIONABLE))
            {
                valid = false;
            }
        }
        
        return valid;
    }
    
    protected NodeRef v1GetVersionedNodeRef(NodeRef oldVersionHistoryRef)
    {
        NodeRef versionedNodeRef = null;
        
        // Get versioned nodeRef from one of the versions - note: assumes all versions refer to the same versioned nodeRef
        Collection<ChildAssociationRef> versions = dbNodeService.getChildAssocs(oldVersionHistoryRef);
        if (versions.size() > 0)
        {
            Iterator<ChildAssociationRef> itr = versions.iterator();
            ChildAssociationRef childAssocRef = itr.next();
            NodeRef versionRef = childAssocRef.getChildRef();
            
            Version version = version1Service.getVersion(versionRef);
            
            versionedNodeRef = version.getVersionedNodeRef();
        }
        
        return versionedNodeRef;
    }
    
    private VersionHistory v1BuildVersionHistory(NodeRef oldVersionHistoryRef, NodeRef versionedNodeRef)
    {  
        return version1Service.buildVersionHistory(oldVersionHistoryRef, versionedNodeRef);
    }
    
    protected void v1DeleteVersionHistory(NodeRef oldVersionHistoryRef)
    {
        dbNodeService.deleteNode(oldVersionHistoryRef);
    }
    
    private void v1MarkVersionHistory(NodeRef oldVersionHistoryRef)
    {
        String migratedName = PREFIX_MIGRATED+oldVersionHistoryRef.getId();
        dbNodeService.setProperty(oldVersionHistoryRef, ContentModel.PROP_NAME, migratedName);
    }
    
    public List<ChildAssociationRef> getVersionHistories(final NodeRef rootNodeRef)
    {
        return dbNodeService.getChildAssocs(rootNodeRef);
    }
    
    protected void migrateVersion(NodeRef oldVHNodeRef, boolean deleteImmediately) throws Throwable
    {
        if(v1CheckForVersionedWorkingCopy(oldVHNodeRef))
        {
            NodeRef versionedNodeRef = v1GetVersionedNodeRef(oldVHNodeRef);
            migrateVersionHistory(oldVHNodeRef, versionedNodeRef);
        }
        else
        {
            logger.warn("Have found a versioned working copy node " + oldVHNodeRef + ", skipping");
        }
        
        if (deleteImmediately)
        {
            // delete old version history node
            v1DeleteVersionHistory(oldVHNodeRef);
        }
        else
        {
            // mark old version history node for later cleanup
            v1MarkVersionHistory(oldVHNodeRef);
        }
    }
    
    public boolean isMigrationComplete()
    {
        if (migrationComplete == null)
        {
            NodeRef oldRootNodeRef = dbNodeService.getRootNode(VersionMigrator.VERSION_STORE_REF_OLD);
            final List<ChildAssociationRef> childAssocRefs = getVersionHistories(oldRootNodeRef);
            
            migrationComplete = (childAssocRefs.size() == 0);
        }
        
        return migrationComplete;
    }
    
    /**
     * Attempts to refresh the lock.
     * 
     * @return          Returns the lock token
     */
    private void refreshLock(String lockToken)
    {
        if ((lockToken == null) || (jobLockService == null))
        {
            return;
        }
        jobLockService.refreshLock(lockToken, MigrateVersionStorePatch.LOCK, MigrateVersionStorePatch.LOCK_TTL);
        
        if (logger.isTraceEnabled())
        {
            logger.trace("Refreshed lock: "+lockToken+" with TTL of "+MigrateVersionStorePatch.LOCK_TTL+" ms ["+AlfrescoTransactionSupport.getTransactionId()+"]["+Thread.currentThread().getId()+"]");
        }
    }
    
    /**
     * Construct the migration data structures for the BatchProcessor
     * 
     * ALF-5621: construct the BatchProcessor migration work in a new transaction to ensure the
     * transaction is not alive during the batch processing work.
     *
     * @param batchSize
     * @param threadCount
     * @param limit
     * @param deleteImmediately
     * @param lockToken
     * @param isRunningAsJob
     * 
     * @return MigrationWork, encapsulating what needs to be done for version migration
     */
    private MigrationWork getMigrationWork(final int batchSize, final int threadCount, final int limit, final boolean deleteImmediately, final String lockToken, final boolean isRunningAsJob)
    {
        RetryingTransactionCallback<MigrationWork> buildMigrationWork = new RetryingTransactionCallback<MigrationWork>()
        {
            public MigrationWork execute() throws Throwable
            {
                final NodeRef oldRootNodeRef = dbNodeService.getRootNode(VersionMigrator.VERSION_STORE_REF_OLD);
                final NodeRef newRootNodeRef = dbNodeService.getRootNode(VersionMigrator.VERSION_STORE_REF_NEW);
                
                refreshLock(lockToken);
                
                long startTime = System.currentTimeMillis();
                
                final List<ChildAssociationRef> childAssocRefs = getVersionHistories(oldRootNodeRef);
                
                int toDo = childAssocRefs.size();
                
                if (toDo == 0)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug(I18NUtil.getMessage(MSG_PATCH_NOOP));
                    }
                    migrationComplete = true;
                    return null;
                }
                
                migrationComplete = false;
                
                if (logger.isInfoEnabled())
                {
                    logger.info("Found "+childAssocRefs.size()+" version histories in old version store (in "+((System.currentTimeMillis()-startTime)/1000)+" secs)");
                }
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("batchSize="+batchSize+", batchWorkerThreadCount="+threadCount+", deleteImmediately="+deleteImmediately);
                }
                
                long splitTime = System.currentTimeMillis();
                
                boolean firstMigration = false;
                
                if (! isRunningAsJob)
                {
                    List<ChildAssociationRef> newChildAssocRefs = getVersionHistories(newRootNodeRef);
                    firstMigration = (newChildAssocRefs.size() == 0);
                    
                    if (logger.isInfoEnabled())
                    {
                        if (! firstMigration)
                        {
                            logger.warn("This is not the first migration attempt. Found "+newChildAssocRefs.size()+" version histories in new version store (in "+((System.currentTimeMillis()-splitTime)/1000)+" secs)");
                        }
                    }
                }
                
                // note: assumes patch runs before cleanup starts
                int toMigrateCount = 0;
                int totalCount = 0;
                int alreadyMigratedCount = 0;
                
                boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);

                List<List<NodeRef>> batchProcessorWork = new ArrayList<List<NodeRef>>(2);
                
                try
                {
                    //
                    // split into batches (ignore version histories that have already been migrated)
                    //
                    
                    splitTime = System.currentTimeMillis();
                    
                    final List<NodeRef> tmpBatch = new ArrayList<NodeRef>(batchSize);
                    
                    int maxToDo = childAssocRefs.size();
                    
                    if (limit > -1)
                    {
                        maxToDo = limit;
                        
                        if (logger.isInfoEnabled())
                        {
                            logger.info("Limit this job cycle to max of "+limit+" version histories");
                        }
                    }
                    
                    for (final ChildAssociationRef childAssocRef : childAssocRefs)
                    {
                        // short-cut if first migration
                        if (!firstMigration)
                        {
                            if (isMigrated(childAssocRef))
                            {
                                // skip - already migrated
                                alreadyMigratedCount++;
                                continue;
                            }
                        }
                        
                        toMigrateCount++;
                        
                        if (tmpBatch.size() < batchSize)
                        {
                            tmpBatch.add(childAssocRef.getChildRef());
                        }
                        
                        if ((tmpBatch.size() == batchSize) ||
                            (toMigrateCount >= maxToDo) ||
                            (totalCount == childAssocRefs.size()))
                        {
                            if (tmpBatch.size() > 0)
                            {
                                // Each thread gets 1 batch to execute
                                batchProcessorWork.add(new ArrayList<NodeRef>(tmpBatch));
                                tmpBatch.clear();
                            }
                        }
                        
                        if (toMigrateCount >= maxToDo)
                        {
                            break;
                        }
                    }
                }
	            finally
	            {
	                MLPropertyInterceptor.setMLAware(wasMLAware);
	            }

	            return new MigrationWork(toMigrateCount, alreadyMigratedCount, toDo, batchProcessorWork, splitTime, startTime);
            }
        };

        MigrationWork work = transactionService.getRetryingTransactionHelper().doInTransaction(buildMigrationWork, false, true);
        return work;
    }
    
    /**
     * Do the Version migration work
     * @return          Returns null if no work to do, true if the work is done, false is incomplete (or in progress)
     */
    public Boolean migrateVersions(final int batchSize, final int threadCount, final int limit, final boolean deleteImmediately, final String lockToken, final boolean isRunningAsJob)
    {
        RetryingTransactionCallback<MigrationWork> buildMigrationWork = new RetryingTransactionCallback<MigrationWork>()
        {
            public MigrationWork execute() throws Throwable
            {
                final NodeRef oldRootNodeRef = dbNodeService.getRootNode(VersionMigrator.VERSION_STORE_REF_OLD);
                final NodeRef newRootNodeRef = dbNodeService.getRootNode(VersionMigrator.VERSION_STORE_REF_NEW);
                
                refreshLock(lockToken);
                
                long startTime = System.currentTimeMillis();
                
                final List<ChildAssociationRef> childAssocRefs = getVersionHistories(oldRootNodeRef);
                
                int toDo = childAssocRefs.size();
                
                if (toDo == 0)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug(I18NUtil.getMessage(MSG_PATCH_NOOP));
                    }
                    migrationComplete = true;
                    return null;
                }
                
                migrationComplete = false;
                
                if (logger.isInfoEnabled())
                {
                    logger.info("Found "+childAssocRefs.size()+" version histories in old version store (in "+((System.currentTimeMillis()-startTime)/1000)+" secs)");
                }
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("batchSize="+batchSize+", batchWorkerThreadCount="+threadCount+", deleteImmediately="+deleteImmediately);
                }
                
                long splitTime = System.currentTimeMillis();
                
                boolean firstMigration = false;
                
                if (! isRunningAsJob)
                {
                    List<ChildAssociationRef> newChildAssocRefs = getVersionHistories(newRootNodeRef);
                    firstMigration = (newChildAssocRefs.size() == 0);
                    
                    if (logger.isInfoEnabled())
                    {
                        if (! firstMigration)
                        {
                            logger.warn("This is not the first migration attempt. Found "+newChildAssocRefs.size()+" version histories in new version store (in "+((System.currentTimeMillis()-splitTime)/1000)+" secs)");
                        }
                    }
                }
                
                // note: assumes patch runs before cleanup starts
                int toMigrateCount = 0;
                int totalCount = 0;
                int alreadyMigratedCount = 0;
                List<List<NodeRef>> batchProcessorWork = new ArrayList<List<NodeRef>>(2);
                
                boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
                
                try
                {
                    //
                    // split into batches (ignore version histories that have already been migrated)
                    //
                    
                    splitTime = System.currentTimeMillis();
                    
                    final List<NodeRef> tmpBatch = new ArrayList<NodeRef>(batchSize);
                    
                    int maxToDo = childAssocRefs.size();
                    
                    if (limit > -1)
                    {
                        maxToDo = limit;
                        
                        if (logger.isInfoEnabled())
                        {
                            logger.info("Limit this job cycle to max of "+limit+" version histories");
                        }
                    }
                    
                    for (final ChildAssociationRef childAssocRef : childAssocRefs)
                    {
                        // short-cut if first migration
                        if (!firstMigration)
                        {
                            if (isMigrated(childAssocRef))
                            {
                                // skip - already migrated
                                alreadyMigratedCount++;
                                continue;
                            }
                        }
                        
                        toMigrateCount++;
                        
                        if (tmpBatch.size() < batchSize)
                        {
                            tmpBatch.add(childAssocRef.getChildRef());
                        }
                        
                        if ((tmpBatch.size() == batchSize) ||
                            (toMigrateCount >= maxToDo) ||
                            (totalCount == childAssocRefs.size()))
                        {
                            if (tmpBatch.size() > 0)
                            {
                                // Each thread gets 1 batch to execute
                                batchProcessorWork.add(new ArrayList<NodeRef>(tmpBatch));
                                tmpBatch.clear();
                            }
                        }
                        
                        if (toMigrateCount >= maxToDo)
                        {
                            break;
                        }
                    }
 
                    return new MigrationWork(toMigrateCount, alreadyMigratedCount, toDo, batchProcessorWork, splitTime, startTime);
                }
	            finally
	            {
	                MLPropertyInterceptor.setMLAware(wasMLAware);
	            }
            }
        };

        MigrationWork migrationWork = transactionService.getRetryingTransactionHelper().doInTransaction(buildMigrationWork, false, true);
        if(migrationWork == null)
        {
        	return Boolean.TRUE;
        }

        List<List<NodeRef>> batchProcessorWork = migrationWork.getBatchProcessorWork();
        int toMigrateCount = migrationWork.getToMigrateCount();
        int toDo = migrationWork.getToDo();
        long splitTime = migrationWork.getSplitTime();
        int alreadyMigratedCount = migrationWork.getAlreadyMigratedCount();
        long startTime = migrationWork.getStartTime();
        
        int batchErrorCount = 0;
        int batchCount = 0;
        
        boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
        
        try
        {
            batchCount = batchProcessorWork.size();
            
            if (batchCount > 0)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Split "+toMigrateCount+" into "+batchCount+" batches in "+(System.currentTimeMillis()-splitTime)+" ms");
                }
                
                //
                // do the work
                //
                
                final String runAsUser = AuthenticationUtil.getRunAsUser();
                
                BatchProcessWorkerAdaptor<List<NodeRef>> batchProcessorWorker = new BatchProcessWorkerAdaptor<List<NodeRef>>()
                {
                    public void beforeProcess() throws Throwable
                    {
                        // Disable rules
                        ruleService.disableRules();
                        
                        // Authentication
                        AuthenticationUtil.setRunAsUser(runAsUser);
                    }
                    
                    public void afterProcess() throws Throwable
                    {
                        // Enable rules
                        ruleService.enableRules();
                        // Clear authentication
                        AuthenticationUtil.clearCurrentSecurityContext();
                    }
                    
                    public void process(List<NodeRef> vhBatch) throws Throwable
                    {
                        long startTime = System.currentTimeMillis();
                        
                        refreshLock(lockToken);
                        
                        for (NodeRef oldVHNodeRef : vhBatch)
                        {
                            migrateVersion(oldVHNodeRef, deleteImmediately);
                        }
                        
                        if (logger.isTraceEnabled())
                        {
                            logger.trace("Migrated batch of "+vhBatch.size()+" version histories in "+(System.currentTimeMillis()-startTime)+ " ms ["+AlfrescoTransactionSupport.getTransactionId()+"]["+Thread.currentThread().getId()+"]");
                        }
                    }
                };
                
                boolean splitTxns = true;
                
                if (threadCount > 1)
                {
                    // process first batch only - to avoid retries on qname, acl
                    
                    List<List<NodeRef>> batchProcessorWorkFirst = new ArrayList<List<NodeRef>>(2);
                    batchProcessorWorkFirst.add(new ArrayList<NodeRef>(batchProcessorWork.get(0)));
                    BatchProcessor<List<NodeRef>> batchProcessorFirst = new BatchProcessor<List<NodeRef>>(
                            MIGRATE_VERSION_STORE,
                            transactionService.getRetryingTransactionHelper(),
                            batchProcessorWorkFirst, threadCount, 1,
                            applicationEventPublisher, logger, loggingInterval);
                    
                    batchProcessorFirst.process(batchProcessorWorker, splitTxns);
                    
                    batchErrorCount = batchProcessorFirst.getTotalErrors();
                    
                    batchProcessorWork.remove(0);
                }
                
                if (batchProcessorWork.size() > 0)
                {
                    // process remaining batches
                    BatchProcessor<List<NodeRef>> batchProcessor = new BatchProcessor<List<NodeRef>>(
                    		MIGRATE_VERSION_STORE,
                            transactionService.getRetryingTransactionHelper(),
                            batchProcessorWork, threadCount, 1,
                            applicationEventPublisher, logger, loggingInterval);
                    
                    batchProcessor.process(batchProcessorWorker, splitTxns);
                    
                    batchErrorCount = batchErrorCount + batchProcessor.getTotalErrors();
                }
            }
            
            if (alreadyMigratedCount > 0)
            {
                logger.warn(I18NUtil.getMessage(MSG_PATCH_SKIP2, alreadyMigratedCount));
            }
            
            if (batchCount > 0)
            {
                if (batchErrorCount > 0)
                {
                    logger.warn(I18NUtil.getMessage(MSG_PATCH_SKIP1, batchErrorCount, batchCount, ((System.currentTimeMillis()-startTime)/1000)));
                }
                else
                {
                    if ((limit == -1) || ((toMigrateCount+alreadyMigratedCount) == toDo))
                    {
                        logger.info(I18NUtil.getMessage(MSG_PATCH_COMPLETE, toMigrateCount, toDo, ((System.currentTimeMillis()-startTime)/1000), deleteImmediately));
                        migrationComplete = true;
                    }
                    else
                    {
                        logger.info(I18NUtil.getMessage(MSG_PATCH_IN_PROGRESS, toMigrateCount, toDo, ((System.currentTimeMillis()-startTime)/1000), deleteImmediately));
                    }
                }
            }
        }
        finally
        {
            MLPropertyInterceptor.setMLAware(wasMLAware);
        }
        
        return migrationComplete;
    }
    
    public void executeCleanup(final int batchSize, final int threadCount)
    {
        if (! busy)
        {
            try
            {
                busy = true;
                
                CleanupWork work = new CleanupWork(batchSize, threadCount);
                work.setLoggingInterval(loggingInterval);
                work.setApplicationEventPublisher(applicationEventPublisher);
                work.execute();
            }
            finally
            {
                busy = false;
            }
        }
    }
    
    private class CleanupWork
    {
    	private BatchProcessor<List<NodeRef>> batchProcessor;
    	private int toDo;
    	private int threadCount;
    	private int batchSize;
    	private int batchErrorCount;
    	private int batchCount;
    	private long splitTime;
    	private int notMigratedCount;
    	private int loggingInterval;
    	private Collection<List<NodeRef>> batchProcessorWork;
    	private ApplicationEventPublisher applicationEventPublisher;
    	
    	private long startTime;

    	public CleanupWork(final int batchSize, final int threadCount)
    	{
    		this.batchSize = batchSize;
    		this.threadCount = threadCount;
    	}

    	public void setLoggingInterval(int loggingInterval)
    	{
    		this.loggingInterval = loggingInterval;
    	}

        public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) 
        {
            this.applicationEventPublisher = applicationEventPublisher;
        }
        
    	private void setup()
    	{
            final RetryingTransactionHelper txHelper = transactionService.getRetryingTransactionHelper();
            txHelper.setMaxRetries(1);
            txHelper.doInTransaction(new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
                	startTime = System.currentTimeMillis();
                    
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("batchSize="+batchSize+", batchWorkerThreadCount="+threadCount);
                    }
                    
                    NodeRef oldRootNodeRef = dbNodeService.getRootNode(VersionMigrator.VERSION_STORE_REF_OLD);
                    List<ChildAssociationRef> childAssocRefs = getVersionHistories(oldRootNodeRef);
                    
                    toDo = childAssocRefs.size();
                    
                    if (toDo == 0)
                    {
                        migrationComplete = true;
                    }
                    else
                    {
                        if (logger.isInfoEnabled())
                        {
                            logger.info("Found "+toDo+" version histories to delete from old version store (in "+((System.currentTimeMillis()-startTime)/1000)+" secs)");
                        }
                        
                        // note: assumes cleanup runs after patch has completed
                        
                        notMigratedCount = 0;
                        batchErrorCount = 0;
                        batchCount = 0;
                        
                        boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
                        
                        try
                        {
                            //
                            // split into batches
                            //
                            
                            splitTime = System.currentTimeMillis();
                            
                            int totalCount = 0;
                            
                            batchProcessorWork = new ArrayList<List<NodeRef>>(2);
                            final List<NodeRef> tmpBatch = new ArrayList<NodeRef>(batchSize);
                            
                            for (final ChildAssociationRef childAssocRef : childAssocRefs)
                            {
                                totalCount++;
                                
                                if (!isMigrated(childAssocRef))
                                {
                                   notMigratedCount++;
                                }
                                else
                                {
                                    if (tmpBatch.size() < batchSize)
                                    {
                                        tmpBatch.add(childAssocRef.getChildRef());
                                    }
                                    
                                    if ((tmpBatch.size() == batchSize) || (totalCount == childAssocRefs.size()))
                                    {
                                        // Each thread gets 1 batch to execute
                                        batchProcessorWork.add(new ArrayList<NodeRef>(tmpBatch));
                                        tmpBatch.clear();
                                    }
                                }
                            }
                            
                            batchCount = batchProcessorWork.size();
                        }
                        finally
                        {
                            MLPropertyInterceptor.setMLAware(wasMLAware);
                        }
                    }

                    return null;
                }
            }, true, true);
    	}

    	private void cleanup()
    	{
    		// Note, this is not run in an enclosing transaction. The BatchProcessor will create
    		// transactions as necessary to perform the work.

    		boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);

    		try
    		{
	    		if (batchCount > 0)
	    		{
	    			if (logger.isDebugEnabled())
	    			{
	    				logger.debug("Split into "+batchCount+" batches in "+(System.currentTimeMillis()-splitTime)+" ms");
	    			}
	
	    			//
	    			// do the work
	    			//
	
	    			BatchProcessWorkerAdaptor<List<NodeRef>> batchProcessorWorker = new BatchProcessWorkerAdaptor<List<NodeRef>>()
	    			{
	    				public void process(List<NodeRef> vhBatch) throws Throwable
	    				{
	    					long startTime = System.currentTimeMillis();
	
	    					for (NodeRef oldVHNodeRef : vhBatch)
	    					{
	    						// delete old version history node
	    						v1DeleteVersionHistory(oldVHNodeRef);
	    					}
	
	    					if (logger.isTraceEnabled())
	    					{
	    						logger.trace("Deleted batch of "+vhBatch.size()+" migrated version histories in "+(System.currentTimeMillis()-startTime)+ " ms ["+AlfrescoTransactionSupport.getTransactionId()+"]["+Thread.currentThread().getId()+"]");
	    					}
	    				}
	    			};
	
	    			batchProcessor = new BatchProcessor<List<NodeRef>>(
	    					CLEAN_OLD_VERSION_STORE,
	    					transactionService.getRetryingTransactionHelper(),
	    					batchProcessorWork, threadCount, 1,
	    					applicationEventPublisher, logger, loggingInterval);
	
	    			boolean splitTxns = true;
	    			batchProcessor.process(batchProcessorWorker, splitTxns);
					batchErrorCount = batchProcessor.getTotalErrors();
	    		}
	    	}
	    	finally
	    	{
	    		MLPropertyInterceptor.setMLAware(wasMLAware);
	    	}
    	}

    	private void processResults()
    	{
            final RetryingTransactionHelper txHelper = transactionService.getRetryingTransactionHelper();
            txHelper.setMaxRetries(1);
            txHelper.doInTransaction(new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
		            boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
		
		    		try
		    		{
			            if (notMigratedCount > 0)
			            {
			                logger.warn(I18NUtil.getMessage(MSG_DELETE_SKIP2, notMigratedCount));
			            }
			            
			            if (batchCount > 0)
			            {
			                if (batchErrorCount > 0)
			                {
			                    logger.warn(I18NUtil.getMessage(MSG_DELETE_SKIP1, batchErrorCount, ((System.currentTimeMillis()-startTime)/1000)));
			                }
			                else
			                {
			                    logger.info(I18NUtil.getMessage(MSG_DELETE_COMPLETE, (toDo - notMigratedCount), toDo, ((System.currentTimeMillis()-startTime)/1000)));
			                    
			                    if (notMigratedCount == 0)
			                    {
			                        migrationComplete = null;
			                        isMigrationComplete();
			                    }
			                }
			            }
			    	}
			    	finally
			    	{
			    		MLPropertyInterceptor.setMLAware(wasMLAware);
			    	}
			    	
			    	return null;
                }
            }, true, true);
    	}

    	public void execute()
    	{
    		setup();
    		cleanup();
    		processResults();
    	}
    }

    protected boolean isMigrated(ChildAssociationRef vhChildAssocRef)
    {
        return (((String)dbNodeService.getProperty(vhChildAssocRef.getChildRef(), ContentModel.PROP_NAME)).startsWith(VersionMigrator.PREFIX_MIGRATED));
    }
    
    private static class MigrationWork
    {
    	private int toMigrateCount;
    	private int alreadyMigratedCount;
    	private int toDo;
    	private long splitTime;
    	private long startTime;
    	private List<List<NodeRef>> batchProcessorWork;

    	MigrationWork(int toMigrateCount, int alreadyMigratedCount,
				int toDo, List<List<NodeRef>> batchProcessorWork, long splitTime, long startTime) {
			super();
			this.toMigrateCount = toMigrateCount;
			this.alreadyMigratedCount = alreadyMigratedCount;
			this.toDo = toDo;
			this.splitTime = splitTime;
			this.startTime = startTime;
			this.batchProcessorWork = batchProcessorWork;
		}

		int getToMigrateCount() {
			return toMigrateCount;
		}

		int getAlreadyMigratedCount() {
			return alreadyMigratedCount;
		}

		int getToDo() {
			return toDo;
		}
		
		long getSplitTime() {
			return splitTime;
		}

		long getStartTime() {
			return startTime;
		}
		
		List<List<NodeRef>> getBatchProcessorWork()
		{
			return batchProcessorWork;
		}
    }
}
