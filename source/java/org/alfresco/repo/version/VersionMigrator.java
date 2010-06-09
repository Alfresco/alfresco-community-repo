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
import org.alfresco.repo.domain.hibernate.SessionSizeResourceManager;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyScope;
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
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Version2 Migrator
 */
public class VersionMigrator
{
    protected static Log logger = LogFactory.getLog(VersionMigrator.class);
    
    public static final StoreRef VERSION_STORE_REF_OLD = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, VersionModel.STORE_ID);
    public static final StoreRef VERSION_STORE_REF_NEW = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID);
    
    /** track completion * */
    int percentComplete;
    
    /** start time * */
    long startTime;
    
    
    private static final String MSG_PATCH_NOOP = "version_service.migration.patch.noop";
    private static final String MSG_PATCH_COMPLETE = "version_service.migration.patch.complete";
    private static final String MSG_PATCH_SKIP1 = "version_service.migration.patch.warn.skip1";
    private static final String MSG_PATCH_SKIP2 = "version_service.migration.patch.warn.skip2";
    
    private static final String MSG_DELETE_PROGRESS = "version_service.migration.delete.progress";
    private static final String MSG_DELETE_COMPLETE = "version_service.migration.delete.complete";
    private static final String MSG_DELETE_SKIP1    = "version_service.migration.delete.warn.skip1";
    private static final String MSG_DELETE_SKIP2    = "version_service.migration.delete.warn.skip2";
    
    private static final String MSG_PATCH_PROGRESS = "patch.progress";
    
    private static final long RANGE_10 = 1000 * 60 * 90;
    private static final long RANGE_5 = 1000 * 60 * 60 * 4;
    private static final long RANGE_2 = 1000 * 60 * 90 * 10;
    
    private static boolean busy = false;
    
    public final static String PREFIX_MIGRATED = "migrated-";
    
    private VersionServiceImpl version1Service = new VersionServiceImpl();
    
    private Version2ServiceImpl version2Service;
    private NodeService dbNodeService;
    private BehaviourFilter policyBehaviourFilter;
    private DictionaryService dictionaryService;
    private TransactionService transactionService;
    private NodeService versionNodeService; // NodeService impl which redirects to appropriate VersionService
    
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
    
    public void init()
    {
        version1Service.setNodeService(dbNodeService);
        version1Service.setDbNodeService(dbNodeService);
        
        version2Service.setDbNodeService(dbNodeService);
    }
    
    public NodeRef migrateVersionHistory(NodeRef oldVHNodeRef, NodeRef versionedNodeRef)
    {
        if (logger.isTraceEnabled())
        {
            logger.trace("migrateVersionHistory: oldVersionHistoryRef = " + oldVHNodeRef);
        }
        
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
        
        if (logger.isTraceEnabled())
        {
            logger.trace("v2CreateNewVersion: oldVersionRef = " + frozenStateNodeRef + " " + oldVersion);
        }
        
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
        
        // set newVersion auditable properties (of the version node itself, rather than the live versioned node)
        Map<QName, Serializable> props = dbNodeService.getProperties(newVersionRef);
        props.put(ContentModel.PROP_CREATED, versionCreated);
        props.put(ContentModel.PROP_CREATOR, versionCreator);
        props.put(ContentModel.PROP_MODIFIED, versionModified);
        props.put(ContentModel.PROP_MODIFIER, versionModifier);
        props.put(ContentModel.PROP_ACCESSED, versionAccessed);
        dbNodeService.setProperties(newVersionRef, props);
        
        return newVersionRef;
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
    
    public int migrateVersions(final int batchSize, final boolean deleteImmediately)
    {
        final NodeRef oldRootNodeRef = dbNodeService.getRootNode(VersionMigrator.VERSION_STORE_REF_OLD);
        final NodeRef newRootNodeRef = dbNodeService.getRootNode(VersionMigrator.VERSION_STORE_REF_NEW);
        
        long splitTime = System.currentTimeMillis();
        final List<ChildAssociationRef> childAssocRefs = getVersionHistories(oldRootNodeRef);
        
        int toDo = childAssocRefs.size();
        
        if (toDo == 0)
        {
            logger.info(I18NUtil.getMessage(MSG_PATCH_NOOP));
            return 0;
        }
        
        if (logger.isInfoEnabled())
        {
            logger.info("Found "+childAssocRefs.size()+" version histories in old version store (in "+((System.currentTimeMillis()-splitTime)/1000)+" secs)");
        }
        
        splitTime = System.currentTimeMillis();
        final List<ChildAssociationRef> newChildAssocRefs = getVersionHistories(newRootNodeRef);
        final boolean firstMigration = (newChildAssocRefs.size() == 0);
        
        if (logger.isInfoEnabled())
        {
            if (! firstMigration)
            {
                logger.warn("This is not the first migration attempt. Found "+newChildAssocRefs.size()+" version histories in new version store (in "+((System.currentTimeMillis()-splitTime)/1000)+" secs)");
            }
        }
        
        // note: assumes patch runs before cleanup starts
        startTime = System.currentTimeMillis();
        percentComplete = 0;
        
        int vhCount = 0;
        int alreadyMigratedCount = 0;
        int failCount = 0;
        
        RetryingTransactionHelper txHelper = transactionService.getRetryingTransactionHelper();
        
        boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
        SessionSizeResourceManager.setDisableInTransaction();
        
        try
        {
            int totalCount = 0;
            
            final List<NodeRef> tmpBatch = new ArrayList<NodeRef>(batchSize);  
            
            for (final ChildAssociationRef childAssocRef : childAssocRefs)
            {
                reportProgress(MSG_PATCH_PROGRESS, toDo, totalCount);
                totalCount++;
                
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
                
                if (tmpBatch.size() < batchSize)
                {
                    tmpBatch.add(childAssocRef.getChildRef());
                }
                
                if ((tmpBatch.size() == batchSize) || (totalCount == childAssocRefs.size()))
                {
                    while (tmpBatch.size() != 0)
                    {
                        txHelper.setMaxRetries(1);
                        
                        try
                        {
                            txHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
                            {
                                public NodeRef execute() throws Throwable
                                {
                                    if (logger.isTraceEnabled())
                                    {
                                        logger.trace("Attempt to migrate batch of "+tmpBatch.size()+" version histories");
                                    }
                                    
                                    long startTime = System.currentTimeMillis();
                                    
                                    for (NodeRef oldVHNodeRef : tmpBatch)
                                    {
                                        try
                                        {
                                            NodeRef versionedNodeRef = v1GetVersionedNodeRef(oldVHNodeRef);
                                            migrateVersionHistory(oldVHNodeRef, versionedNodeRef);
                                            
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
                                        catch (Throwable t)
                                        {
                                            logger.error("Skipping migration of: " + oldVHNodeRef, t);
                                            throw t;
                                        }
                                    }
                                    
                                    if (logger.isDebugEnabled())
                                    {
                                        logger.debug("Migrated batch of "+tmpBatch.size()+" version histories in "+(System.currentTimeMillis()-startTime)+ " ms");
                                    }
                                    
                                    return null;
                                }
                            }, false, true);
                            
                            // batch successful
                            vhCount = vhCount + tmpBatch.size();
                            tmpBatch.clear();
                        }
                        catch (Throwable t)
                        {
                            // TODO if batchSize > 1 then could switch into batchSize=1 mode, and re-try one-by-one
                            // in theory, could fail on commit (although integrity checks are disabled by default) hence don't know which nodes failed
                            
                            logger.error("Skipping migration of batch size ("+tmpBatch.size()+"): "+t);
                            
                            // batch failed
                            failCount = failCount + tmpBatch.size();
                            tmpBatch.clear();
                        }
                    }
                }
            }
        }
        finally
        {
            MLPropertyInterceptor.setMLAware(wasMLAware);
            SessionSizeResourceManager.setEnableInTransaction();
        }
        
        if (failCount > 0)
        {
            logger.warn(I18NUtil.getMessage(MSG_PATCH_SKIP1, failCount));
        }
        else if (alreadyMigratedCount > 0)
        {
            logger.warn(I18NUtil.getMessage(MSG_PATCH_SKIP2, alreadyMigratedCount));
        }
        
        toDo = toDo - alreadyMigratedCount;
        
        if (vhCount != toDo)
        {
            logger.warn(I18NUtil.getMessage(MSG_PATCH_COMPLETE, vhCount, toDo, ((System.currentTimeMillis()-startTime)/1000)));
        }
        else
        {
            logger.info(I18NUtil.getMessage(MSG_PATCH_COMPLETE, vhCount, toDo, ((System.currentTimeMillis()-startTime)/1000)));
        }
        
        return vhCount;
    }
    
    
    public void executeCleanup(final int batchSize)
    {
        if (! busy)
        {
            try
            {
                busy = true;
                
                final RetryingTransactionHelper txHelper = transactionService.getRetryingTransactionHelper();
                txHelper.setMaxRetries(1);
                txHelper.doInTransaction(new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Throwable
                    {
                        NodeRef oldRootNodeRef = dbNodeService.getRootNode(VersionMigrator.VERSION_STORE_REF_OLD);
                        List<ChildAssociationRef> childAssocRefs = getVersionHistories(oldRootNodeRef);
                        
                        int toDo = childAssocRefs.size();
                        
                        if (toDo > 0)
                        {
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Found "+toDo+" version histories in old version store");
                            }
                            
                            // note: assumes cleanup runs after patch has completed
                            startTime = System.currentTimeMillis();
                            percentComplete = 0;
                            
                            int deletedCount = 0;
                            int failCount = 0;
                            int notMigratedCount = 0;
                            
                            boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
                            SessionSizeResourceManager.setDisableInTransaction();
                            
                            try
                            {
                                int batchCount = 0;
                                int totalCount = 0;
                                
                                final List<NodeRef> tmpBatch = new ArrayList<NodeRef>(batchSize);
                                
                                for (final ChildAssociationRef childAssocRef : childAssocRefs)
                                {
                                    reportProgress(MSG_DELETE_PROGRESS, toDo, totalCount);
                                    totalCount++;
                                    
                                    if (isMigrated(childAssocRef))
                                    {
                                        if (batchCount < batchSize)
                                        {
                                            tmpBatch.add(childAssocRef.getChildRef());
                                            batchCount++;
                                        }
                                        
                                        if ((batchCount == batchSize) || (totalCount == childAssocRefs.size()))
                                        {
                                            while (tmpBatch.size() != 0)
                                            {
                                                txHelper.setMaxRetries(1);
                                                NodeRef failed = txHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
                                                {
                                                    public NodeRef execute() throws Throwable
                                                    {
                                                        if (logger.isTraceEnabled())
                                                        {
                                                            logger.trace("Attempt to delete batch of "+tmpBatch.size()+" migrated version histories");
                                                        }
                                                        
                                                        long startTime = System.currentTimeMillis();
                                                        
                                                        for (NodeRef oldVHNodeRef : tmpBatch)
                                                        {
                                                            try
                                                            {
                                                                // delete old version history node
                                                                v1DeleteVersionHistory(oldVHNodeRef);
                                                            }
                                                            catch (Throwable t)
                                                            {
                                                                logger.error("Skipping deletion of: " + oldVHNodeRef, t);
                                                                return oldVHNodeRef;
                                                            }
                                                        }
                                                        
                                                        if (logger.isDebugEnabled())
                                                        {
                                                            logger.debug("Deleted batch of "+tmpBatch.size()+" migrated version histories in "+(System.currentTimeMillis()-startTime)+ " ms");
                                                        }
                                                        
                                                        return null;
                                                    }
                                                }, false, true);
                                                
                                                if (failed != null)
                                                {
                                                    tmpBatch.remove(failed); // retry batch without the failed node
                                                    failCount++;
                                                }
                                                else
                                                {
                                                    deletedCount = deletedCount + tmpBatch.size();
                                                    tmpBatch.clear();
                                                    batchCount = 0;
                                                }
                                            }
                                        }
                                    }
                                    else
                                    {
                                        notMigratedCount++;
                                    }
                                }
                            }
                            finally
                            {
                                MLPropertyInterceptor.setMLAware(wasMLAware);
                                SessionSizeResourceManager.setEnableInTransaction();
                            }
                            
                            if (failCount > 0)
                            {
                                logger.warn(I18NUtil.getMessage(MSG_DELETE_SKIP1, failCount));
                            }
                            
                            if (notMigratedCount > 0)
                            {
                                logger.warn(I18NUtil.getMessage(MSG_DELETE_SKIP2, notMigratedCount));
                            }
                            
                            if (deletedCount > 0)
                            {
                                logger.info(I18NUtil.getMessage(MSG_DELETE_COMPLETE, deletedCount, ((System.currentTimeMillis()-startTime)/1000)));
                            }
                            else if (logger.isDebugEnabled())
                            {
                                logger.debug(I18NUtil.getMessage(MSG_DELETE_COMPLETE, deletedCount, ((System.currentTimeMillis()-startTime)/1000)));
                            }
                        }
                        
                        return null;
                    }
                }, true, true);
            }
            finally
            {
                busy = false;
            }
        }
    }
    
    protected boolean isMigrated(ChildAssociationRef vhChildAssocRef)
    {
        return (((String)dbNodeService.getProperty(vhChildAssocRef.getChildRef(), ContentModel.PROP_NAME)).startsWith(VersionMigrator.PREFIX_MIGRATED));
    }
    
    /**
     * Support to report % completion and estimated completion time.
     * 
     * @param estimatedTotal
     * @param currentInteration
     */
    protected void reportProgress(String msgKey, long estimatedTotal, long currentInteration)
    {
        if (currentInteration == 0)
        {
            percentComplete = 0;
        }
        else if (currentInteration * 100l / estimatedTotal > percentComplete)
        {
            int previous = percentComplete;
            percentComplete = (int) (currentInteration * 100l / estimatedTotal);
            
            if (percentComplete < 100)
            {
                // conditional report
                long currentTime = System.currentTimeMillis();
                long timeSoFar = currentTime - startTime;
                long timeRemaining = timeSoFar * (100 - percentComplete) / percentComplete;
                
                int report = -1;
                
                if (timeRemaining > 60000)
                {
                    int reportInterval = getreportingInterval(timeSoFar, timeRemaining);
                    
                    for (int i = previous + 1; i <= percentComplete; i++)
                    {
                        if (i % reportInterval == 0)
                        {
                            report = i;
                        }
                    }
                    if (report > 0)
                    {
                        Date end = new Date(currentTime + timeRemaining);
                        logger.info(I18NUtil.getMessage(msgKey, report, end));
                    }
                }
            }
        }
    }

    private int getreportingInterval(long soFar, long toGo)
    {
        long total = soFar + toGo;
        if (total < RANGE_10)
        {
            return 10;
        }
        else if (total < RANGE_5)
        {
            return 5;
        }
        else if (total < RANGE_2)
        {
            return 2;
        }
        else
        {
            return 1;
        }
    }
}
