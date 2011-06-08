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
package org.alfresco.repo.admin.patch.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.config.JNDIConstants;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.avm.AVMChildEntryEntity;
import org.alfresco.repo.domain.avm.AVMNodeLinksDAO;
import org.alfresco.repo.domain.avm.AVMStoreDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.permissions.AclCrudDAO;
import org.alfresco.repo.domain.permissions.AclEntity;
import org.alfresco.repo.domain.permissions.AclUpdateEntity;
import org.alfresco.repo.domain.permissions.Authority;
import org.alfresco.repo.domain.permissions.Permission;
import org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor.StoreType;
import org.alfresco.repo.security.permissions.ACEType;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Alternative patch to remove ACLs from all WCM stores, and replace with WCM group-based ACLs.
 * 
 * @author janv
 */
public class ResetWCMToGroupBasedPermissionsPatch extends MoveWCMToGroupBasedPermissionsPatch
{
    private static Log logger = LogFactory.getLog(ResetWCMToGroupBasedPermissionsPatch.class);
    
    private static final String MSG_SUCCESS = "patch.resetWCMToGroupBasedPermissionsPatch.result";
    
    private AclCrudDAO aclCrudDAO;
    private AVMStoreDAO avmStoreDAO;
    private AVMNodeLinksDAO avmNodeLinksDAO;
    private PatchDAO patchDAO;
    
    private HelperDAO helper; // local helper class
    
    private PersonService personService;
    
    private static int batchSize = 500;
    
    // cache staging store acl change set and shared acl id
    private Map<String, Pair<Long, Long>> stagingData = new HashMap<String, Pair<Long, Long>>(10);
    
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setAvmStoreDAO(AVMStoreDAO avmStoreDAO)
    {
        this.avmStoreDAO = avmStoreDAO;
    }
    
    public void setAvmNodeLinksDAO(AVMNodeLinksDAO avmNodeLinksDAO)
    {
        this.avmNodeLinksDAO = avmNodeLinksDAO;
    }
    
    public void setAclCrudDAO(AclCrudDAO aclCrudDAO)
    {
        this.aclCrudDAO = aclCrudDAO;
    }
    
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }
    
    public void setBatchSize(int batchSizeOverride)
    {
        batchSize = batchSizeOverride;
    }
    
    public ResetWCMToGroupBasedPermissionsPatch()
    {
        helper = new HelperDAO();
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        long splitTime = System.currentTimeMillis();
        
        List<AVMStoreDescriptor> stores = this.avmService.getStores();
        
        logger.info("Retrieved list of "+stores.size()+" AVM store descriptors in "+(System.currentTimeMillis()-splitTime)/1000+" secs");
        
        splitTime = System.currentTimeMillis();
        
        List<Pair<AVMStoreDescriptor, StoreType>> wcmStores = new ArrayList<Pair<AVMStoreDescriptor, StoreType>>(stores.size());
            
        int count = 0;
        
        for (AVMStoreDescriptor store : stores)
        {
            Map<QName, PropertyValue> storeProperties = this.avmService.getStoreProperties(store.getName());

            StoreType storeType = StoreType.getStoreType(store.getName(), store, storeProperties);
            
            if (! storeType.equals(StoreType.UNKNOWN))
            {
                wcmStores.add(new Pair<AVMStoreDescriptor, StoreType>(store, storeType));
                count++;
            }
        }
        
        logger.info("Retrieved store types for "+count+" WCM stores in "+(System.currentTimeMillis()-splitTime)/1000+" secs");
        
        splitTime = System.currentTimeMillis();
        
        count = 0;
        
        // process WCM staging stores
        for (Pair<AVMStoreDescriptor, StoreType> wcmStore : wcmStores)
        {
            AVMStoreDescriptor store = wcmStore.getFirst();
            StoreType storeType = wcmStore.getSecond();
            
            switch (storeType)
            {
            case STAGING:
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Process store "+store.getName()+" ("+storeType+")");
                }
                
                count++;
                
                makeGroupsIfRequired(store);
                addUsersToGroupIfRequired(store);
                
                // belts-and-braces - nullify root children acls (if any) that are not 'www' (which will be overwritten anyway for staging stores)
                nullifyAvmNodeAclsExcluding(store.getName(), JNDIConstants.DIR_DEFAULT_WWW);
                
                setStagingAreaPermissions(store);
                
                setStagingAreaMasks(store);
                
                break;
            case UNKNOWN:
                // non WCM store - nothing to do
            default:
            }
        }
        
        if (count > 0)
        {
            logger.info("Processed "+count+" WCM staging stores: "+(System.currentTimeMillis()-splitTime)/1000+" secs");
        }
        
        splitTime = System.currentTimeMillis();
        
        count = 0;
        
        // process WCM sandbox stores - nullify acls
        for (Pair<AVMStoreDescriptor, StoreType> wcmStore : wcmStores)
        {
            AVMStoreDescriptor store = wcmStore.getFirst();
            StoreType storeType = wcmStore.getSecond();
            
            switch (storeType)
            {
            case AUTHOR:
            case AUTHOR_PREVIEW:
            case AUTHOR_WORKFLOW:
            case AUTHOR_WORKFLOW_PREVIEW:   
            case WORKFLOW:
            case WORKFLOW_PREVIEW:
            case STAGING_PREVIEW:
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Nullify acls for store "+store.getName()+" ("+storeType+")");
                }
                
                count++;
                
                // nullify acls for avm nodes for this store
                nullifyAvmNodeAcls(store.getName());
                
                break;
                
            case STAGING:
            case UNKNOWN:
            default:
                break;
            }
        }
        
        if (count > 0)
        {
            logger.info("Nullified acls for "+count+" WCM sandbox stores: "+(System.currentTimeMillis()-splitTime)/1000+" secs");
        }
        
        if (wcmStores.size() > 0)
        {
            // delete any dangling acls/aces (across all stores) 
            // note: delete before creating new acls since dangling shared acl currently possible (after creating new sandbox)
            splitTime = System.currentTimeMillis();
            deleteDangling(); 
            logger.info("Deleted dangling acls/aces across all stores in "+(System.currentTimeMillis()-splitTime)/1000+" secs");
        }
        
        splitTime = System.currentTimeMillis();
        
        count = 0;
        
        // process WCM sandbox stores (1st layer)
        for (Pair<AVMStoreDescriptor, StoreType> wcmStore : wcmStores)
        {
            AVMStoreDescriptor store = wcmStore.getFirst();
            StoreType storeType = wcmStore.getSecond();
            
            switch (storeType)
            {
            case AUTHOR:
            case WORKFLOW:
                
                if (stagingData.get(extractBaseStore(store.getName())) == null)
                {
                    // skip store - no corresponding base store
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Skip store "+store.getName()+" ("+storeType+") since no corresponding base store");
                    }
                    
                    break;
                }
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Process store "+store.getName()+" ("+storeType+")");
                }
                
                count++;
                
                setSandboxPermissions(store);
                
                setSandBoxMasks(store);
                break;
                
            case STAGING_PREVIEW:
                
                if (stagingData.get(extractBaseStore(store.getName())) == null)
                {
                    // skip store - no corresponding base store
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Skip store "+store.getName()+" ("+storeType+") since no corresponding base store");
                    }
                    
                    break;
                }
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Process store "+store.getName()+" ("+storeType+")");
                }
                
                count++;
                
                setSandboxPermissions(store);
                
                setStagingAreaMasks(store);
                break;
                
            case AUTHOR_PREVIEW:
            case AUTHOR_WORKFLOW:
            case AUTHOR_WORKFLOW_PREVIEW:
            case WORKFLOW_PREVIEW:
            case STAGING:
            case UNKNOWN:
            default:
                break;
            }
        }
        
        // process WCM sandbox stores (2nd layer)
        for (Pair<AVMStoreDescriptor, StoreType> wcmStore : wcmStores)
        {
            AVMStoreDescriptor store = wcmStore.getFirst();
            StoreType storeType = wcmStore.getSecond();
            
            switch (storeType)
            {
            case AUTHOR_PREVIEW:
            case AUTHOR_WORKFLOW:
            case WORKFLOW_PREVIEW:
                
                if (stagingData.get(extractBaseStore(store.getName())) == null)
                {
                    // skip store - no corresponding base store
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Skip store "+store.getName()+" ("+storeType+") since no corresponding base store");
                    }
                    
                    break;
                }
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Process store "+store.getName()+" ("+storeType+")");
                }
                
                count++;
                
                setSandboxPermissions(store);
                
                setSandBoxMasks(store);
                break;
                
            case AUTHOR_WORKFLOW_PREVIEW:
            case AUTHOR:
            case WORKFLOW:
            case STAGING_PREVIEW:
            case STAGING:
            case UNKNOWN:
            default:
                break;
            }
        }

        // process WCM sandbox stores (3rd layer)
        for (Pair<AVMStoreDescriptor, StoreType> wcmStore : wcmStores)
        {
            AVMStoreDescriptor store = wcmStore.getFirst();
            StoreType storeType = wcmStore.getSecond();
            
            switch (storeType)
            {
            case AUTHOR_WORKFLOW_PREVIEW:
                
                if (stagingData.get(extractBaseStore(store.getName())) == null)
                {
                    // skip store - no corresponding base store
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Skip store "+store.getName()+" ("+storeType+") since no corresponding base store");
                    }
                    
                    break;
                }
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Process store "+store.getName()+" ("+storeType+")");
                }
                
                count++;
                
                setSandboxPermissions(store);
                
                setSandBoxMasks(store);
                break;
                
            case AUTHOR_PREVIEW:
            case AUTHOR_WORKFLOW:
            case WORKFLOW_PREVIEW:
            case AUTHOR:
            case WORKFLOW:
            case STAGING_PREVIEW:
            case STAGING:
            case UNKNOWN:
            default:
                break;
            }
        }
        
        if (count > 0)
        {
            logger.info("Processed "+count+" WCM sandbox stores: "+(System.currentTimeMillis()-splitTime)/1000+" secs");
        }
        
        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS);
        // done
        return msg;
    }
    
    private void makeGroupsIfRequired(AVMStoreDescriptor stagingStore)
    {
        long startTime = System.currentTimeMillis();
        
        String stagingStoreName = stagingStore.getName();
        
        int count = 0;
        
        for (String permission : MoveWCMToGroupBasedPermissionsPatch.PERMISSIONS)
        {
            String shortName = stagingStoreName + "-" + permission;
            String group = this.authorityService.getName(AuthorityType.GROUP, shortName);
            if (!this.authorityService.authorityExists(group))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Create: "+group);
                }
                
                this.authorityService.createAuthority(AuthorityType.GROUP, shortName);
                count++;
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Already exists: "+group);
                }
            }
        }
        
        if (logger.isDebugEnabled() && (count > 0))
        {
            logger.debug("Created "+count+" missing groups in "+(System.currentTimeMillis()-startTime)/1000+" secs");
        }
    }
    
    private void nullifyAvmNodeAcls(String storeName) throws Exception
    {
        long startTime = System.currentTimeMillis();
        
        int updatedCount = helper.nullifyAvmNodeAcls(storeName);
        
        if (logger.isDebugEnabled() && (updatedCount > 0))
        {
            logger.debug("nullifyAvmNodeAcls ("+updatedCount+") for store: "+storeName+" in "+(System.currentTimeMillis()-startTime)/1000+" secs");
        }
    }
    
    private void nullifyAvmNodeAclsExcluding(String storeName, String excludeRootChild) throws Exception
    {
        long startTime = System.currentTimeMillis();
        
        int updatedCount = helper.nullifyAvmNodeAclsExcluding(storeName, excludeRootChild);
        
        if (logger.isDebugEnabled() && (updatedCount > 0))
        {
            logger.debug("nullifyAvmNodeAcls ("+updatedCount+") for store: "+storeName+" excluding '"+excludeRootChild+"' in "+(System.currentTimeMillis()-startTime)/1000+" secs");
        }
    }
    
    private void deleteDangling()
    {
        try
        {
            long startTime = System.currentTimeMillis();
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Start deleting dangling acls/aces (across all stores)");
            }
            
            int aclsDeletedCount = helper.deleteDanglingAcls();
            int acesDeletedCount = patchDAO.deleteDanglingAces();
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Finish deleting dangling acls/aces ["+aclsDeletedCount+"/"+acesDeletedCount+"] (across all stores) in "+(System.currentTimeMillis()-startTime)/1000+" secs");
            }
        }
        catch (Throwable e)
        {
            String msg = "Failed to delete dangling acls/aces";
            logger.error(msg, e);
            throw new PatchException(msg, e);
        }
    }
    
    @Override
    protected void setStagingAreaPermissions(AVMStoreDescriptor stagingStore) throws Exception
    {
    	long startTime = System.currentTimeMillis();
    	
    	String stagingStoreName = stagingStore.getName();
    	
        if (logger.isDebugEnabled())
        {
        	logger.debug("Start set staging area permissions: "+stagingStoreName);
        }
        
        // create acl change set, defining acl and shared acl
        Long aclChangeSet = aclCrudDAO.createAclChangeSet();
        
        long definingAclId = helper.createWCMGroupBasedAcl(stagingStoreName, aclChangeSet, ACLType.DEFINING, false);
        long sharedAclId = helper.createWCMGroupBasedAcl(stagingStoreName, aclChangeSet, ACLType.SHARED, false);
        
        stagingData.put(stagingStoreName, new Pair<Long, Long>(aclChangeSet, sharedAclId));
        
        helper.updateAclInherited(definingAclId, sharedAclId);
        
        helper.updateAclInheritsFrom(sharedAclId, definingAclId);
        helper.updateAclInherited(sharedAclId, sharedAclId); // mimics current - do we need ?
        
        // set defining acl (on 'www')
        helper.setRootAcl(stagingStoreName, definingAclId);
        
        // set shared acls
        int updatedCount = helper.setSharedAcls(stagingStoreName, sharedAclId);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Finish set staging area permissions: "+stagingStore.getName()+" in "+(System.currentTimeMillis()-startTime)/1000+" secs (updated "+(updatedCount+1)+")");
        }
    }
    
    protected void setSandboxPermissions(AVMStoreDescriptor sandboxStore) throws Exception
    {
        long startTime = System.currentTimeMillis();
        
        String sandboxStoreName = sandboxStore.getName();
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Start set sandbox permissions: "+sandboxStoreName);
        }
        
        Pair<Long, Long> aclData = stagingData.get(extractBaseStore(sandboxStoreName));
        
        long aclChangeSet = aclData.getFirst();
        long baseSharedAclId = aclData.getSecond();
        
        String stagingStoreName = extractStagingAreaName(sandboxStoreName);
        
        // create layered acl
        long layeredAclId = helper.createWCMGroupBasedAcl(stagingStoreName, aclChangeSet, ACLType.LAYERED, true);
        long sharedAclId = helper.createWCMGroupBasedAcl(stagingStoreName, aclChangeSet, ACLType.SHARED, false);
        
        stagingData.put(sandboxStoreName, new Pair<Long, Long>(aclChangeSet, sharedAclId));
        
        helper.updateAclInheritsFrom(layeredAclId, baseSharedAclId);
        helper.updateAclInheritsFrom(sharedAclId, layeredAclId);
        
        // set layered acl (on 'www')
        helper.setRootAcl(sandboxStoreName, layeredAclId);
        
        // set shared acls
        int updatedCount = helper.setSharedAcls(sandboxStoreName, sharedAclId);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Finish set sandbox permissions: "+sandboxStoreName+" in "+(System.currentTimeMillis()-startTime)/1000+" secs (updated "+(updatedCount+1)+")");
        }
    }
    
    private void addUsersToGroupIfRequired(AVMStoreDescriptor stagingStore)
    {
        long startTime = System.currentTimeMillis();
        
        QName propQName = QName.createQName(null, ".web_project.noderef");
        
        PropertyValue pValue = this.avmService.getStoreProperty(stagingStore.getName(), propQName);
        
        if (pValue != null)
        {
            NodeRef webProjectNodeRef = (NodeRef) pValue.getValue(DataTypeDefinition.NODE_REF);
            
            List<ChildAssociationRef> userInfoRefs = this.nodeService.getChildAssocs(webProjectNodeRef,
                    WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
            
            for (ChildAssociationRef ref : userInfoRefs)
            {
                NodeRef userInfoRef = ref.getChildRef();
                String username = (String) this.nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERNAME);
                String userrole = (String) this.nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE);
                
                if (userrole.equals(PermissionService.ALL_PERMISSIONS))
                {
                    userrole = this.replaceAllWith;
                    this.nodeService.setProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE, userrole);
                    
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Set role "+userrole+" for user "+username+" in web project "+stagingStore.getName());
                    }
                }

                addToGroupIfRequired(stagingStore.getName(), username, userrole);
            }
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Check and add missing users (if any) to group(s) in "+(System.currentTimeMillis()-startTime)/1000+" secs");
        }
    }
    
    @Override
    protected void addToGroupIfRequired(String stagingStoreName, String user, String permission)
    {
        String shortName = stagingStoreName + "-" + permission;
        String group = this.authorityService.getName(AuthorityType.GROUP, shortName);
        Set<String> members = this.authorityService.getContainedAuthorities(AuthorityType.USER, group, true);
        if (!members.contains(user))
        {
            // skip mismatch - eg. user is linked to web project but does not exist as a person
            if (! personService.personExists(user))
            {
                logger.warn("Person does not exist: "+user+" (not added to: "+group);
            }
            else
            {
                this.authorityService.addAuthority(group, user);
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Added user "+user+" to: "+group);
                }
            }
        }
    }
    
    protected String extractBaseStore(String name)
    {
        // STAGING                            -> STAGING
        // STAGING--PREVIEW                   -> STAGING
        // STAGING--AUTHOR                    -> STAGING
        // STAGING--AUTHOR--PREVIEW           -> STAGING--AUTHOR
        // STAGING--AUTHOR--WORKFLOW          -> STAGING--AUTHOR
        // STAGING--AUTHOR--WORKFLOW--PREVIEW -> STAGING--AUTHOR--WORKFLOW
        // STAGING--WORKFLOW                  -> STAGING
        // STAGING--WORKFLOW--PREVIEW         -> STAGING--WORKFLOW
        
        int index = name.lastIndexOf(WCM_STORE_SEPARATOR);
        if (index != -1)
        {
            return name.substring(0, index);
        }
        
        return name;
    }
    
    private class HelperDAO
    {
        private int nullifyAvmNodeAcls(final String storeName)
        {
            try
            {
                long rootId = getAVMStoreCurrentRootNodeId(storeName);
                
                // recursively nullify below the root
                int updatedCount = nullifyAvmNodeAcls(rootId);
                
                // also nullify the root
                List<Long> childIds = new ArrayList<Long>(1);
                childIds.add(rootId);
                updatedCount += nullifyAvmNodeAclsForChildren(childIds);
                
                return updatedCount;
            }
            catch (Throwable e)
            {
                String msg = "Failed to nullify avm node acl ids for: "+storeName;
                logger.error(msg, e);
                throw new PatchException(msg, e);
            }
        }
        
        private int nullifyAvmNodeAclsExcluding(String storeName, String excludeRootChild)
        {
            try
            {
                long rootId = getAVMStoreCurrentRootNodeId(storeName);
                
                List<AVMChildEntryEntity> children = getAVMChildrenEntries(rootId);
                
                int totalUpdatedCount = 0;
                
                List<Long> childIds = new ArrayList<Long>(0);
                
                for (AVMChildEntryEntity child : children)
                {
                    Long childId = child.getChildId();
                    String name = child.getName();
                    
                    if (! name.equals(excludeRootChild))
                    {
                        // recursively nullify below the (non-excluded) root children
                        totalUpdatedCount += nullifyAvmNodeAcls(childId);
                        childIds.add(childId);
                    }
                }
                
                if (childIds.size() > 0)
                {
                    // also nullify the (non-excluded) root children
                    totalUpdatedCount += nullifyAvmNodeAclsForChildren(childIds);
                }
                
                return totalUpdatedCount;
            }
            catch (Throwable e)
            {
                String msg = "Failed to nullify avm node acl ids for: "+storeName+" (excluding "+excludeRootChild+")";
                logger.error(msg, e);
                throw new PatchException(msg, e);
            }
        }
        
        private int nullifyAvmNodeAcls(final long parentId) throws Exception
        {
            List<Long> childIds = getAVMChildren(parentId);
            
            int updatedCount = 0;
            
            if (childIds.size() > 0)
            {
                updatedCount = nullifyAvmNodeAclsForChildren(childIds);
                
                for (Long childId : childIds)
                {
                    // recursive - walk children
                    updatedCount += nullifyAvmNodeAcls(childId);
                }
            }
            
            return updatedCount;
        }
        
        private int nullifyAvmNodeAclsForChildren(List<Long> childIds)
        {
            int totalUpdateCount = 0;
            
            Iterator<Long> childIdIterator = childIds.iterator();
            List<Long> batchChildIds = new ArrayList<Long>(batchSize);
            
            while (childIdIterator.hasNext())
            {
                Long childId = childIdIterator.next();
                
                batchChildIds.add(childId);
                
                if (batchChildIds.size() == batchSize || !childIdIterator.hasNext())
                {
                    // execute the update
                    int batchUpdateCount = patchDAO.updateAVMNodesNullifyAcl(batchChildIds);
                    
                    totalUpdateCount = totalUpdateCount + batchUpdateCount;
                    batchChildIds.clear();
                }
            }
            
            return totalUpdateCount;
        }
        
        private int updateChildNodeAclIds(long aclId, List<Long> childIds)
        {
            int totalUpdateCount = 0;
            
            Iterator<Long> childIdIterator = childIds.iterator();
            List<Long> batchChildIds = new ArrayList<Long>(batchSize);
            
            while (childIdIterator.hasNext())
            {
                Long childId = childIdIterator.next();
                
                batchChildIds.add(childId);
                
                if (batchChildIds.size() == batchSize || !childIdIterator.hasNext())
                {
                    // execute the update
                    int batchUpdateCount = patchDAO.updateAVMNodesSetAcl(aclId, batchChildIds);
                    
                    totalUpdateCount = totalUpdateCount + batchUpdateCount;
                    
                    batchChildIds.clear();
                }
            }
            
            return totalUpdateCount;
        }
        
        // set root acl on top node (eg. defining or layered acl applied to 'www')
        private void setRootAcl(String storeName, long aclId) throws Exception
        {
            try
            {
                long rootId = getAVMStoreCurrentRootNodeId(storeName);
                
                List<AVMChildEntryEntity> children = getAVMChildrenEntries(rootId);
                
                int totalUpdatedCount = 0;
                
                for (AVMChildEntryEntity child : children)
                {
                    Long childId = child.getChildId();
                    String name = child.getName();
                    
                    if (name.equals(JNDIConstants.DIR_DEFAULT_WWW))
                    {
                        List<Long> childIds = new ArrayList<Long>(1);
                        childIds.add(childId);
                        
                        int updatedCount = updateChildNodeAclIds(aclId, childIds);
                        totalUpdatedCount += updatedCount;
                    }
                }
                
                // belts-and-braces - we expect to find & update 'www'
                if (totalUpdatedCount != 1)
                {
                    throw new AlfrescoRuntimeException("Failed to set top acl for store: "+storeName+" (unexpected updateCount = "+totalUpdatedCount);
                }
            }
            catch (Throwable e)
            {
                String msg = "Failed to set top acl for store: "+storeName;
                logger.error(msg, e);
                throw new PatchException(msg, e);
            }
        }
        
        // set shared acls (ie. below 'www' -> from 'avm_webapps' down)
        private int setSharedAcls(final String storeName, final long sharedAclId) throws Exception
        {
            try
            {
                long rootId = getAVMStoreCurrentRootNodeId(storeName);
                
                List<AVMChildEntryEntity> children = getAVMChildrenEntries(rootId);
                
                List<Long> childIds = new ArrayList<Long>(1);
                
                for (AVMChildEntryEntity child : children)
                {
                    Long childId = child.getChildId();
                    String name = child.getName();
                    
                    if (name.equals(JNDIConstants.DIR_DEFAULT_WWW))
                    {
                        childIds.add(childId);
                    }
                }
                
                // belts-and-braces
                if (childIds.size() != 1)
                {
                    throw new AlfrescoRuntimeException("Did not find one 'www' ("+childIds.size()+ ") for: "+storeName);
                }
                
                return setSharedAcls(childIds.get(0), sharedAclId);
            }
            catch (Throwable e)
            {
                String msg = "Failed to set shared acls for store: "+storeName;
                logger.error(msg, e);
                throw new PatchException(msg, e);
            }
        }
        
        private int setSharedAcls(final long parentId, final long sharedAclId) throws Exception
        {    
            List<Long> childIds = getAVMChildren(parentId);
            
            int updatedCount = 0;
            
            if (childIds.size() > 0)
            {
                updatedCount = updateChildNodeAclIds(sharedAclId, childIds);
                
                for (Long childId : childIds)
                {
                    // recursive - walk children
                    updatedCount += setSharedAcls(childId, sharedAclId);
                }
            }
            
            return updatedCount;
        }
        
        // note: dangling shared acl currently possible (after creating new sandbox)
        private int deleteDanglingAcls() throws Exception
        {
            List<Long> nonDanglingAclIds = patchDAO.selectNonDanglingAclIds();
            List<Long> aclIds = patchDAO.selectAllAclIds();
            
            // get set of dangling acl ids
            aclIds.removeAll(nonDanglingAclIds);
            
            int totalDeletedCount = 0;
                
            Iterator<Long> aclIdIterator = aclIds.iterator();
            List<Long> batchAclIds = new ArrayList<Long>(batchSize);
            
            while (aclIdIterator.hasNext())
            {
                Long aclId = aclIdIterator.next();
                
                batchAclIds.add(aclId);
                
                if (batchAclIds.size() == batchSize || !aclIdIterator.hasNext())
                {
                    // execute delete
                    int batchDeletedCount = patchDAO.deleteAclMembersForAcls(batchAclIds);
                    
                    totalDeletedCount = totalDeletedCount + batchDeletedCount;
                    batchAclIds.clear();
                }
            }
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Deleted "+totalDeletedCount+" dangling acl members");
            }
            
            totalDeletedCount = 0;
            
            aclIdIterator = aclIds.iterator();
            batchAclIds = new ArrayList<Long>(batchSize);
            
            while (aclIdIterator.hasNext())
            {
                Long aclId = aclIdIterator.next();
                
                batchAclIds.add(aclId);
                
                if (batchAclIds.size() == batchSize || !aclIdIterator.hasNext())
                {
                    // execute delete
                    int batchDeletedCount = patchDAO.deleteAcls(batchAclIds);
                    
                    totalDeletedCount = totalDeletedCount + batchDeletedCount;
                    batchAclIds.clear();
                }
            }
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Deleted "+totalDeletedCount+" dangling acls");
            }
            
            return totalDeletedCount;
        }
        
        private long getAVMStoreCurrentRootNodeId(final String avmStoreName)
        {
            return avmStoreDAO.getStore(avmStoreName).getRootNodeId();
        }
        
        private List<Long> getAVMChildren(final long parentId)
        {
            List<AVMChildEntryEntity> childEntries = getAVMChildrenEntries(parentId);
            List<Long> childIds = new ArrayList<Long>(childEntries.size());
            
            for (AVMChildEntryEntity childEntry : childEntries)
            {
                Long childId = (Long)childEntry.getChildId();
                childIds.add(childId);
            }
            return childIds;
        }
        
        private List<AVMChildEntryEntity> getAVMChildrenEntries(final long parentId)
        {
            return avmNodeLinksDAO.getChildEntriesByParent(parentId, null);
        }
        
        private long findOrCreateAce(final String authorityName, final String permissionName) throws Exception
        {
            Authority authority = aclCrudDAO.getOrCreateAuthority(authorityName);
            
            QName permissionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "cmobject");
            SimplePermissionReference permRef = SimplePermissionReference.getPermissionReference(permissionQName, permissionName);
            
            Permission permission = aclCrudDAO.getOrCreatePermission(permRef);
            
            return aclCrudDAO.getOrCreateAce(permission, authority, ACEType.ALL, AccessStatus.ALLOWED).getId();
        }
        
        private long createAcl(final long aclChangeSet, final ACLType aclType, boolean requiresVersion) throws Exception
        {
            AclEntity acl = new AclEntity();
            
            acl.setAclId(GUID.generate());
            acl.setAclType(aclType);
            acl.setAclVersion(Long.valueOf(1l));
            
            switch (aclType)
            {
            case FIXED:
            case GLOBAL:
                acl.setInherits(Boolean.FALSE);
            case OLD:
            case SHARED:
            case DEFINING:
            case LAYERED:
            default:
                acl.setInherits(Boolean.TRUE);
                break;
            }
            
            acl.setLatest(Boolean.TRUE);
            
            switch (aclType)
            {
            case OLD:
                acl.setVersioned(Boolean.FALSE);
                break;
            case FIXED:
            case GLOBAL:
            case SHARED:
            case DEFINING:
            case LAYERED:
            default:
                acl.setVersioned(Boolean.TRUE);
                break;
            }
            
            acl.setAclChangeSetId(aclChangeSet);
            acl.setRequiresVersion(requiresVersion);
            
            // save
            return aclCrudDAO.createAcl(acl).getId();
        }
        
        private void updateAclInheritsFrom(final long aclId, final long inheritsFromId) throws Exception
        {
            AclUpdateEntity aclEntity = aclCrudDAO.getAclForUpdate(aclId);
            aclEntity.setInheritsFrom(inheritsFromId);
            aclCrudDAO.updateAcl(aclEntity);
        }
        
        private void updateAclInherited(final long aclId, final long inheritedAclId) throws Exception
        {
            AclUpdateEntity aclEntity = aclCrudDAO.getAclForUpdate(aclId);
            aclEntity.setInheritedAcl(inheritedAclId);
            aclCrudDAO.updateAcl(aclEntity);
        }
        
        // assume groups exist, if permission does not exist then will be created
        private Long createWCMGroupBasedAcl(String stagingStoreName, long aclChangeSet, ACLType aclType, boolean requiresVersion) throws Exception
        {
            if (stagingStoreName.contains(WCM_STORE_SEPARATOR))
            {
                throw new AlfrescoRuntimeException("Unexpected staging store name: "+stagingStoreName);
            }
            
            List<Long> aceIds = new ArrayList<Long>(5);
            
            // find or create 5 aces
            long cmAceId = findOrCreateAce(PermissionService.GROUP_PREFIX + stagingStoreName + "-" + PermissionService.WCM_CONTENT_MANAGER, PermissionService.WCM_CONTENT_MANAGER);
            long cpAceId = findOrCreateAce(PermissionService.GROUP_PREFIX + stagingStoreName + "-" + PermissionService.WCM_CONTENT_PUBLISHER, PermissionService.WCM_CONTENT_PUBLISHER);
            long ccAceId = findOrCreateAce(PermissionService.GROUP_PREFIX + stagingStoreName + "-" + PermissionService.WCM_CONTENT_CONTRIBUTOR, PermissionService.WCM_CONTENT_CONTRIBUTOR);
            long crAceId = findOrCreateAce(PermissionService.GROUP_PREFIX + stagingStoreName + "-" + PermissionService.WCM_CONTENT_REVIEWER, PermissionService.WCM_CONTENT_REVIEWER); 
            
            long erAceId = findOrCreateAce(PermissionService.ALL_AUTHORITIES, PermissionService.READ);
            
            // create acl
            long aclId = createAcl(aclChangeSet, aclType, requiresVersion);
            
            aceIds.add(cmAceId);
            aceIds.add(cpAceId);
            aceIds.add(ccAceId);
            aceIds.add(crAceId);
            aceIds.add(erAceId);
            
            // create acl members
            aclCrudDAO.addAclMembersToAcl(aclId, aceIds, 0);
            
            return aclId;
        }
    }
}
