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
package org.alfresco.repo.usage;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.usage.ContentQuotaException;
import org.alfresco.service.cmr.usage.ContentUsageService;
import org.alfresco.service.cmr.usage.UsageService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Implements Content Usage service and policies/behaviour.
 *
 */
public class ContentUsageImpl implements ContentUsageService,
                                         NodeServicePolicies.OnUpdatePropertiesPolicy,
                                         NodeServicePolicies.BeforeDeleteNodePolicy,
                                         //NodeServicePolicies.OnAddAspectPolicy,
                                         NodeServicePolicies.OnCreateNodePolicy
{
    // Logger
    private static Log logger = LogFactory.getLog(ContentUsageImpl.class);
    
    /** Key to the deleted nodes */
    private static final String KEY_DELETED_NODES = "contentUsage.deletedNodes";
    
    /** Key to the created nodes */
    private static final String KEY_CREATED_NODES = "contentUsage.createdNodes";
    
    private NodeService nodeService;
    private PersonService personService;
    private PolicyComponent policyComponent;
    private UsageService usageService;
    private AuthenticationContext authenticationContext;
    private TenantService tenantService;
    
    private boolean enabled = true;
    
    private List<String> stores;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setUsageService(UsageService usageService)
    {
        this.usageService = usageService;
    }
        
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
    
    public void setStores(List<String> stores)
    {
        this.stores = stores;
    }
    
    public List<String> getStores()
    {
        return this.stores;
    }
    
    /**
     * The initialise method
     */
    public void init()
    {
        if (enabled)
        {
            // Register interest in the onUpdateProperties policy - for content
            policyComponent.bindClassBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
                    ContentModel.TYPE_CONTENT,
                    new JavaBehaviour(this, "onUpdateProperties"));
            
            // Register interest in the beforeDeleteNode policy - for content
            policyComponent.bindClassBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                    ContentModel.TYPE_CONTENT,
                    new JavaBehaviour(this, "beforeDeleteNode"));
            
            // Register interest in the onCreateNode policy - for content
            policyComponent.bindClassBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
                    ContentModel.TYPE_CONTENT,
                    new JavaBehaviour(this, "onCreateNode"));
            
            /*
            // Register interest in the onAddAspect policy - for ownable
            policyComponent.bindClassBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"),
                    ContentModel.ASPECT_OWNABLE,
                    new JavaBehaviour(this, "onAddAspect"));
            */
        }
    }
    
    @SuppressWarnings("unchecked")
    private void recordDelete(NodeRef nodeRef)
    {
        Set<NodeRef> deletedNodes = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(KEY_DELETED_NODES);
        if (deletedNodes == null)
        {
            deletedNodes = new HashSet<NodeRef>();
            AlfrescoTransactionSupport.bindResource(KEY_DELETED_NODES, deletedNodes);
        }
        deletedNodes.add(tenantService.getName(nodeRef));
        
        Set<NodeRef> updatedNodes = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(KEY_CREATED_NODES);
        if (updatedNodes != null)
        {
            updatedNodes.remove(tenantService.getName(nodeRef));
        }
    }
    
    @SuppressWarnings("unchecked")
    private boolean alreadyDeleted(NodeRef nodeRef)
    {
        Set<NodeRef> deletedNodes = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(KEY_DELETED_NODES);
        if (deletedNodes != null)
        {
            for (NodeRef deletedNodeRef : deletedNodes)
            {
                if (deletedNodeRef.equals(nodeRef))
                {
                    if (logger.isDebugEnabled()) logger.debug("alreadyDeleted: nodeRef="+nodeRef);
                    return true;
                }
            }
        }
        return false;
    }
    
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        NodeRef nodeRef = childAssocRef.getChildRef();
        if (stores.contains(tenantService.getBaseName(nodeRef.getStoreRef()).toString()) && (! alreadyCreated(nodeRef)))
        {
            // TODO use data dictionary to get content property
            ContentData contentData = (ContentData)nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            
            if (contentData != null)
            {
                long contentSize = contentData.getSize();
                
                // Get owner/creator
                String owner = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_OWNER);
                if ((owner == null) || (owner.equals(OwnableService.NO_OWNER)))
                {
                    owner = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR);
                }
                
                if (contentSize != 0 && owner != null)
                {
                   // increment usage if node is being created
                   if (logger.isDebugEnabled()) logger.debug("onCreateNode: nodeRef="+nodeRef+", owner="+owner+", contentSize="+contentSize);
                   incrementUserUsage(owner, contentSize, nodeRef);
                   recordCreate(nodeRef);
                }
            }
        }
    }

    
    @SuppressWarnings("unchecked")
    private void recordCreate(NodeRef nodeRef)
    {
        Set<NodeRef> createdNodes = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(KEY_CREATED_NODES);
        if (createdNodes == null)
        {
            createdNodes = new HashSet<NodeRef>();
            AlfrescoTransactionSupport.bindResource(KEY_CREATED_NODES, createdNodes);
        }
        createdNodes.add(tenantService.getName(nodeRef));
    }
    
    @SuppressWarnings("unchecked")
    private boolean alreadyCreated(NodeRef nodeRef)
    {
        Set<NodeRef> createdNodes = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(KEY_CREATED_NODES);
        if (createdNodes != null)
        {
            for (NodeRef createdNodeRef : createdNodes)
            {
                if (createdNodeRef.equals(nodeRef))
                {
                    if (logger.isDebugEnabled()) logger.debug("alreadyCreated: nodeRef="+nodeRef);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Called after a node's properties have been changed.
     * 
     * @param nodeRef reference to the updated node
     * @param before the node's properties before the change
     * @param after the node's properties after the change 
     */
    public void onUpdateProperties(
            NodeRef nodeRef,
            Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        if (stores.contains(tenantService.getBaseName(nodeRef.getStoreRef()).toString()) && (! alreadyCreated(nodeRef)))
        {
            // Check for change in content size
            
            // TODO use data dictionary to get content property     
            ContentData contentDataBefore = (ContentData)before.get(ContentModel.PROP_CONTENT);
            Long contentSizeBefore = (contentDataBefore == null ? null : contentDataBefore.getSize());
            ContentData contentDataAfter = (ContentData)after.get(ContentModel.PROP_CONTENT);
            Long contentSizeAfter = (contentDataAfter == null ? null : contentDataAfter.getSize());
            
            // Check for change in owner/creator
            String ownerBefore = (String)before.get(ContentModel.PROP_OWNER);
            if ((ownerBefore == null) || (ownerBefore.equals(OwnableService.NO_OWNER)))
            {
                ownerBefore = (String)before.get(ContentModel.PROP_CREATOR);
            }
            String ownerAfter = (String)after.get(ContentModel.PROP_OWNER);
            if ((ownerAfter == null) || (ownerAfter.equals(OwnableService.NO_OWNER)))
            {
                ownerAfter = (String)after.get(ContentModel.PROP_CREATOR);
            }
            
            // check change in size (and possibly owner)
            if (contentSizeBefore == null && contentSizeAfter != null && contentSizeAfter != 0 && ownerAfter != null)
            {
                // new size has been added - note: ownerBefore does not matter since the contentSizeBefore is null
                if (logger.isDebugEnabled()) logger.debug("onUpdateProperties: updateSize (null -> "+contentSizeAfter+"): nodeRef="+nodeRef+", ownerAfter="+ownerAfter);
                incrementUserUsage(ownerAfter, contentSizeAfter, nodeRef);
            }
            else if (contentSizeAfter == null && contentSizeBefore != null && contentSizeBefore != 0 && ownerBefore != null)
            {
                // old size has been removed - note: ownerAfter does not matter since contentSizeAfter is null
                if (logger.isDebugEnabled()) logger.debug("onUpdateProperties: updateSize ("+contentSizeBefore+" -> null): nodeRef="+nodeRef+", ownerBefore="+ownerBefore);
                decrementUserUsage(ownerBefore, contentSizeBefore, nodeRef);
            }
            else if (contentSizeBefore != null && contentSizeAfter != null)
            {
                if (contentSizeBefore.equals(contentSizeAfter) == false)
                {
                    // size has changed (and possibly owner)
                    if (logger.isDebugEnabled()) logger.debug("onUpdateProperties: updateSize ("+contentSizeBefore+" -> "+contentSizeAfter+"): nodeRef="+nodeRef+", ownerBefore="+ownerBefore+", ownerAfter="+ownerAfter);
                    
                    if (contentSizeBefore != 0 && ownerBefore != null)
                    {
                        decrementUserUsage(ownerBefore, contentSizeBefore, nodeRef);
                    }
                    if (contentSizeAfter != 0 && ownerAfter != null)
                    {
                        incrementUserUsage(ownerAfter, contentSizeAfter, nodeRef);
                    }
                } 
                else 
                {
                    // same size - check change in owner only
                    if (ownerBefore == null && ownerAfter != null && contentSizeAfter != 0 && ownerAfter != null)
                    {
                        // new owner has been added
                        if (logger.isDebugEnabled()) logger.debug("onUpdateProperties: updateOwner (null -> "+ownerAfter+"): nodeRef="+nodeRef+", contentSize="+contentSizeAfter);
                        incrementUserUsage(ownerAfter, contentSizeAfter, nodeRef);
                    }
                    else if (ownerAfter == null && ownerBefore != null && contentSizeBefore != 0)
                    {
                        // old owner has been removed
                        if (logger.isDebugEnabled()) logger.debug("onUpdateProperties: updateOwner ("+ownerBefore+" -> null): nodeRef="+nodeRef+", contentSize="+contentSizeBefore);
                        decrementUserUsage(ownerBefore, contentSizeBefore, nodeRef);
                    }
                    else if (ownerBefore != null && ownerAfter != null && ownerBefore.equals(ownerAfter) == false)
                    {
                        // owner has changed (size has not)
                        if (logger.isDebugEnabled()) logger.debug("onUpdateProperties: updateOwner ("+ownerBefore+" -> "+ownerAfter+"): nodeRef="+nodeRef+", contentSize="+contentSizeBefore);
                        
                        if (contentSizeBefore != 0)
                        {
                            decrementUserUsage(ownerBefore, contentSizeBefore, nodeRef);
                        }
                        if (contentSizeAfter != 0)
                        {
                            incrementUserUsage(ownerAfter, contentSizeAfter, nodeRef);
                        }
                    }
                }
            }
        }
    }

    /**
     * Called before a node is deleted.
     * 
     * @param nodeRef   the node reference
     */
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        if (stores.contains(tenantService.getBaseName(nodeRef.getStoreRef()).toString()) && (! alreadyDeleted(nodeRef)))
        {
            // TODO use data dictionary to get content property
            ContentData contentData = (ContentData)nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            
            if (contentData != null)
            {
                long contentSize = contentData.getSize();
                
                // Get owner/creator
                String owner = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER); // allow for case where someone else is deleting the node
                if (owner == null)
                {
                    
                    owner = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_OWNER);
                    if ((owner == null) || (owner.equals(OwnableService.NO_OWNER)))
                    {
                        owner = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR);
                    }
                }
                
                if (contentSize != 0 && owner != null)
                {
                   // decrement usage if node is being deleted
                   if (logger.isDebugEnabled()) logger.debug("beforeDeleteNode: nodeRef="+nodeRef+", owner="+owner+", contentSize="+contentSize);
                   decrementUserUsage(owner, contentSize, nodeRef);
                   recordDelete(nodeRef);
                }
            }
        }
    }
    
    /**
     * Called after an <b>cm:ownable</b> aspect has been added to a node
     *
     * @param nodeRef the node to which the aspect was added
     * @param aspectTypeQName the type of the aspect
     */
    /* NOTE: now handled via onUpdateProperties as expected
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if ((stores.contains(tenantService.getBaseName(nodeRef.getStoreRef()).toString())) &&
            (aspectTypeQName.equals(ContentModel.ASPECT_OWNABLE)))
        {
            String newOwner = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_OWNER);
            String creator = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR);
            
            if ((newOwner != null) && (! newOwner.equals(creator)))
            {
                if (newOwner.equals(OwnableService.NO_OWNER))
                {
                    // usage has to be applied somewhere, default back to creator for now
                    newOwner = creator;
                }
                
                ContentData content = (ContentData)nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
                
                Map<QName, Serializable> before = new HashMap<QName, Serializable>(2);
                Map<QName, Serializable> after = new HashMap<QName, Serializable>(2);
                
                after.put(ContentModel.PROP_OWNER, newOwner);
                after.put(ContentModel.PROP_CONTENT, content);
                
                before.put(ContentModel.PROP_CREATOR, creator);
                before.put(ContentModel.PROP_CONTENT, content);
                
                onUpdateProperties(nodeRef, before, after);
            }
        }
    }
    */
    
    private void incrementUserUsage(String userName, long contentSize, NodeRef contentNodeRef)
    {
        if (! authenticationContext.isSystemUserName(userName))
        {
            // increment usage - add positive delta
            if (logger.isDebugEnabled()) logger.debug("incrementUserUsage: username="+userName+", contentSize="+contentSize+", contentNodeRef="+contentNodeRef);
            
            long currentSize = getUserUsage(userName);
            long quotaSize = getUserQuota(userName);
            
            long newSize = currentSize + contentSize;
            
            // check whether user's quota exceeded
            if ((quotaSize != -1) && (newSize > quotaSize))
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("User (" + userName + ") quota exceeded: content=" + contentSize +
                                  ", usage=" + currentSize +
                                  ", quota=" + quotaSize);
                }
                throw new ContentQuotaException("User quota exceeded");
            }
            
            NodeRef personNodeRef = getPerson(userName);
            if (personNodeRef != null)
            {
                usageService.insertDelta(personNodeRef, contentSize);
            }
        }
    }
    
    private void decrementUserUsage(String userName, long contentSize, NodeRef contentNodeRef)
    {
        if (! authenticationContext.isSystemUserName(userName))
        {
            // decrement usage - add negative delta
            if (logger.isDebugEnabled()) logger.debug("decrementUserUsage: username="+userName+", contentSize="+contentSize+", contentNodeRef="+contentNodeRef);
            
            long currentSize = getUserUsage(userName);
            
            long newSize = currentSize + contentSize;
            
            if (newSize < 0)
            {
               if (logger.isDebugEnabled())
               {
                   logger.debug("User (" + userName + ") has negative usage (" + newSize + ") - reset to 0");
               }
            }
            
            NodeRef personNodeRef = getPerson(userName);
            if (personNodeRef != null)
            {
                usageService.insertDelta(personNodeRef, (-contentSize));
            }
        }
    }
    
    /**
     * Set user's usage. Should only be called by background (collapse) job !
     * 
     * @param userName
     * @param currentUsage
     */
    public void setUserStoredUsage(NodeRef personNodeRef, long currentUsage)
    {
        if (personNodeRef != null)
        {     
            nodeService.setProperty(personNodeRef, ContentModel.PROP_SIZE_CURRENT, new Long(currentUsage));
        }
    }
    
    public long getUserStoredUsage(NodeRef personNodeRef)
    {
        Long currentUsage = null;
        if (personNodeRef != null)
        {
            currentUsage = (Long)nodeService.getProperty(personNodeRef, ContentModel.PROP_SIZE_CURRENT);
        }
        
        return (currentUsage == null ? -1 : currentUsage);
    }
    
    public long getUserUsage(String userName)
    {
        ParameterCheck.mandatoryString("userName", userName);

        long currentUsage = 0;
        NodeRef personNodeRef = getPerson(userName);
        if (personNodeRef != null)
        {
            currentUsage = getUserUsage(personNodeRef, false);
        }
        return currentUsage;
    }
    
    public long getUserUsage(NodeRef personNodeRef, boolean removeDeltas)
    {
        long currentUsage = -1;
        
        if (personNodeRef != null)
        {
            currentUsage = getUserStoredUsage(personNodeRef);
        }
        
        if (currentUsage != -1)
        {
            long deltaSize = removeDeltas ? usageService.getAndRemoveTotalDeltaSize(personNodeRef) :
                usageService.getTotalDeltaSize(personNodeRef);
            // add any deltas to the currentUsage, removing them if required
            currentUsage = currentUsage + deltaSize;
            
            if (currentUsage < 0)
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("User usage ("+ personNodeRef+") is negative ("+currentUsage+") overriding to 0");
                }
                currentUsage = 0;
            }
        }
        
        return currentUsage;
    }
    
    /**
     * Set user's current quota.
     * Usually called by Web Client (Admin Console) if admin is changing/setting a user's quota.
     * 
     * @param userName
     * @param currentQuota
     */
    public void setUserQuota(String userName, long currentQuota)
    {
        NodeRef personNodeRef = getPerson(userName);
        if (personNodeRef != null)
        {
            nodeService.setProperty(personNodeRef, ContentModel.PROP_SIZE_QUOTA, new Long(currentQuota));
        }
    }
    
    public long getUserQuota(String userName)
    {
        Long currentQuota = null;
        
        NodeRef personNodeRef = getPerson(userName);
        if (personNodeRef != null)
        {
            currentQuota = (Long)nodeService.getProperty(personNodeRef, ContentModel.PROP_SIZE_QUOTA);
        }
        
        return (currentQuota == null ? -1 : currentQuota);
    }
    
    public boolean getEnabled()
    {
        return enabled;
    }
    
    private NodeRef getPerson(String userName)
    {
        NodeRef personNodeRef = null;
        try
        {
            // false to not force user home creation
            personNodeRef = personService.getPerson(userName, false);
        }
        catch (NoSuchPersonException e)
        {
            // Can get this situation where the person does not exist and may not be created.
            // Had to add this catch when not forcing user home folder creation.
            // The boolean parameter to getPerson does two things. It should really be split into two booleans.
            personNodeRef = null;
        }
        catch (RuntimeException e)
        {
            // workaround for ETHREEOH-1457 where existing tenants (created using 3.0.1) may have been bootstrapped with creator set 
            // to super admin (eg. "admin") rather than "System@xxx". This workaround should remain until we patch any such existing tenants
            if (tenantService.isEnabled())
            {
                personNodeRef = null;
            }
            else
            {
                throw e;
            }
        }
        return personNodeRef;
    }
}
