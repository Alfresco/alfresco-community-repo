/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.usage.ContentQuotaException;
import org.alfresco.service.cmr.usage.ContentUsageService;
import org.alfresco.service.cmr.usage.UsageService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements Content Usage service and policies/behaviour.
 *
 */
public class ContentUsageImpl implements ContentUsageService,
                                         NodeServicePolicies.OnUpdatePropertiesPolicy,
                                         NodeServicePolicies.BeforeDeleteNodePolicy
{
    // Logger
    private static Log logger = LogFactory.getLog(ContentUsageImpl.class);
    
    /** Key to the deleted nodes */
    private static final String KEY_DELETED_NODES = "contentUsage.deletedNodes";
    
    /** Key to the updated nodes */
    private static final String KEY_UPDATED_NODES = "contentUsage.updatedNodes";
    
    /** Key to the deleted folder */
    private static final String KEY_DELETED_FOLDER = "contentUsage.deletedFolder";
    
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
            
            // Register interest in the beforeDeleteNode policy - for folder
            policyComponent.bindClassBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"), 
                    ContentModel.TYPE_FOLDER,
                    new JavaBehaviour(this, "beforeDeleteNode"));
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
    
    @SuppressWarnings("unchecked")
    private void recordUpdate(NodeRef nodeRef)
    {
        Set<NodeRef> updatedNodes = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(KEY_UPDATED_NODES);
        if (updatedNodes == null)
        {
            updatedNodes = new HashSet<NodeRef>();
            AlfrescoTransactionSupport.bindResource(KEY_UPDATED_NODES, updatedNodes);
        }
        updatedNodes.add(tenantService.getName(nodeRef));
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
        if (stores.contains(tenantService.getBaseName(nodeRef.getStoreRef()).toString()))
        {
            // Check for change in content size
            
            // TODO use data dictionary to get content property     
            ContentData contentDataBefore = (ContentData)before.get(ContentModel.PROP_CONTENT);
            Long contentSizeBefore = (contentDataBefore == null ? null : contentDataBefore.getSize());
            ContentData contentDataAfter = (ContentData)after.get(ContentModel.PROP_CONTENT);
            Long contentSizeAfter = (contentDataAfter == null ? null : contentDataAfter.getSize());
            
            // Check for change in owner/creator
            String ownerBefore = (String)before.get(ContentModel.PROP_OWNER);
            if (ownerBefore == null)
            {
                ownerBefore = (String)before.get(ContentModel.PROP_CREATOR);
            }
            String ownerAfter = (String)after.get(ContentModel.PROP_OWNER);
            if (ownerAfter == null)
            {
                ownerAfter = (String)after.get(ContentModel.PROP_CREATOR);
            }           
                        
            // check change in size (and possibly owner)
            if (contentSizeBefore == null && contentSizeAfter != null && contentSizeAfter != 0 && ownerAfter != null)
            {
                // new size has been added - note: ownerBefore does not matter since the contentSizeBefore is null
                if (logger.isDebugEnabled()) logger.debug("onUpdateProperties: updateSize (null -> "+contentSizeAfter+"): nodeRef="+nodeRef+", ownerAfter="+ownerAfter);
                incrementUserUsage(ownerAfter, contentSizeAfter, nodeRef);
                recordUpdate(nodeRef);
            }
            else if (contentSizeAfter == null && contentSizeBefore != null && contentSizeBefore != 0 && ownerBefore != null)
            {
                // old size has been removed - note: ownerAfter does not matter since contentSizeAfter is null
                if (logger.isDebugEnabled()) logger.debug("onUpdateProperties: updateSize ("+contentSizeBefore+" -> null): nodeRef="+nodeRef+", ownerBefore="+ownerBefore);
                decrementUserUsage(ownerBefore, contentSizeBefore, nodeRef);
                recordUpdate(nodeRef);
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
                        recordUpdate(nodeRef);
                    }
                    if (contentSizeAfter != 0 && ownerAfter != null)
                    {
                        incrementUserUsage(ownerAfter, contentSizeAfter, nodeRef);
                        recordUpdate(nodeRef);
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
                        recordUpdate(nodeRef);
                    }
                    else if (ownerAfter == null && ownerBefore != null && contentSizeBefore != 0)
                    {
                        // old owner has been removed
                        if (logger.isDebugEnabled()) logger.debug("onUpdateProperties: updateOwner ("+ownerBefore+" -> null): nodeRef="+nodeRef+", contentSize="+contentSizeBefore);
                        decrementUserUsage(ownerBefore, contentSizeBefore, nodeRef);
                        recordUpdate(nodeRef);
                    }
                    else if (ownerBefore != null && ownerAfter != null && ownerBefore.equals(ownerAfter) == false)
                    {
                        // owner has changed (size has not)
                        if (logger.isDebugEnabled()) logger.debug("onUpdateProperties: updateOwner ("+ownerBefore+" -> "+ownerAfter+"): nodeRef="+nodeRef+", contentSize="+contentSizeBefore);                       

                        if (contentSizeBefore != 0)
                        {
                            decrementUserUsage(ownerBefore, contentSizeBefore, nodeRef);
                            recordUpdate(nodeRef);
                        }
                        if (contentSizeAfter != 0)
                        {
                            incrementUserUsage(ownerAfter, contentSizeAfter, nodeRef);
                            recordUpdate(nodeRef);
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
            QName type = nodeService.getType(nodeRef);
            if (type.equals(ContentModel.TYPE_CONTENT))
            {
                // TODO use data dictionary to get content property
                ContentData contentData = (ContentData)nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
                
                if (contentData != null)
                {
                    long contentSize = contentData.getSize();
                    
                    // Get owner/creator
                    String owner = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_OWNER);
                    if (owner == null)
                    {
                        owner = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR);
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
            else if (type.equals(ContentModel.TYPE_FOLDER))
            {
                if (logger.isDebugEnabled()) logger.debug("beforeDeleteNode: folderNodeRef="+nodeRef);
                AlfrescoTransactionSupport.bindResource(KEY_DELETED_FOLDER, nodeRef);
            }
        }
    }
    
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
        
        long currentUsage = -1;
        
        NodeRef personNodeRef = getPerson(userName);
        if (personNodeRef != null)
        {    
            currentUsage = getUserStoredUsage(personNodeRef);
        }
        
        if (currentUsage != -1)
        {
            // add any deltas
            currentUsage = currentUsage + usageService.getTotalDeltaSize(personNodeRef);
            
            if (currentUsage < 0)
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("User usage ("+ userName+") is negative ("+currentUsage+") overriding to 0");
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
            personNodeRef = personService.getPerson(userName);
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
