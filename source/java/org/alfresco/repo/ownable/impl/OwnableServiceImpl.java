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
package org.alfresco.repo.ownable.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * Ownership service support. Use in permissions framework as dynamic authority.
 * 
 * @author Andy Hind
 */
public class OwnableServiceImpl implements
        OwnableService, InitializingBean,
        NodeServicePolicies.OnAddAspectPolicy,
        NodeServicePolicies.OnUpdatePropertiesPolicy,
        NodeServicePolicies.OnRemoveAspectPolicy,
        NodeServicePolicies.OnDeleteNodePolicy
{
    private NodeService nodeService;
    private AuthenticationService authenticationService;
    private SimpleCache<NodeRef, String> nodeOwnerCache;
    private PolicyComponent policyComponent;
    private TenantService tenantService;
    private Set<String> storesToIgnorePolicies = Collections.emptySet();

    public OwnableServiceImpl()
    {
        super();
    }

    // IOC

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setStoresToIgnorePolicies(Set<String> storesToIgnorePolicies)
    {
        this.storesToIgnorePolicies = storesToIgnorePolicies;
    }

    /**
     * @param ownerCache
     *            a transactionally-safe cache of node owners
     */
    public void setNodeOwnerCache(SimpleCache<NodeRef, String> ownerCache)
    {
        this.nodeOwnerCache = ownerCache;
    }

    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "authenticationService", authenticationService);
        PropertyCheck.mandatory(this, "nodeOwnerCache", nodeOwnerCache);
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
    }
    
    public void init()
    {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME,
                ContentModel.ASPECT_OWNABLE,
                new JavaBehaviour(this, "onAddAspect"));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                ContentModel.ASPECT_OWNABLE,
                new JavaBehaviour(this, "onUpdateProperties"));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnRemoveAspectPolicy.QNAME,
                ContentModel.ASPECT_OWNABLE,
                new JavaBehaviour(this, "onRemoveAspect"));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnDeleteNodePolicy.QNAME,
                ContentModel.ASPECT_OWNABLE,
                new JavaBehaviour(this, "onDeleteNode"));
        
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME,
                ContentModel.ASPECT_AUDITABLE,
                new JavaBehaviour(this, "onAddAspect"));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                ContentModel.ASPECT_AUDITABLE,
                new JavaBehaviour(this, "onUpdateProperties"));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnRemoveAspectPolicy.QNAME,
                ContentModel.ASPECT_AUDITABLE,
                new JavaBehaviour(this, "onRemoveAspect"));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnDeleteNodePolicy.QNAME,
                ContentModel.ASPECT_AUDITABLE,
                new JavaBehaviour(this, "onDeleteNode"));
        
        policyComponent.bindClassBehaviour(
                CopyServicePolicies.OnCopyNodePolicy.QNAME,
                ContentModel.ASPECT_OWNABLE, 
                new JavaBehaviour(this, "onCopyNode", NotificationFrequency.EVERY_EVENT));      
        policyComponent.bindClassBehaviour(
                CopyServicePolicies.OnCopyNodePolicy.QNAME,
                ContentModel.ASPECT_AUDITABLE, 
                new JavaBehaviour(this, "onCopyNode", NotificationFrequency.EVERY_EVENT));      
    }

    // OwnableService implmentation

    public String getOwner(NodeRef nodeRef)
    {
        String userName = nodeOwnerCache.get(nodeRef);

        if (userName == null)
        {
            // If ownership is not explicitly set then we fall back to the creator
            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_OWNABLE))
            {
                userName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_OWNER));
            }
            else if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_AUDITABLE))
            {
                userName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
            }
            cacheOwner(nodeRef, userName);
        }

        return userName;
    }

    public void setOwner(NodeRef nodeRef, String userName)
    {
        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_OWNABLE))
        {
            HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>(1, 1.0f);
            properties.put(ContentModel.PROP_OWNER, userName);
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_OWNABLE, properties);
        }
        else
        {
            nodeService.setProperty(nodeRef, ContentModel.PROP_OWNER, userName);
        }
        cacheOwner(nodeRef, userName);
    }

    public void takeOwnership(NodeRef nodeRef)
    {
        setOwner(nodeRef, authenticationService.getCurrentUserName());
    }

    public boolean hasOwner(NodeRef nodeRef)
    {
        return getOwner(nodeRef) != null;
    }

    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        nodeOwnerCache.remove(nodeRef);
    }

    public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        nodeOwnerCache.remove(nodeRef);
    }

    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived)
    {
        nodeOwnerCache.remove(childAssocRef.getChildRef());
    }

    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        Serializable pb = before.get(ContentModel.PROP_OWNER);
        Serializable pa = after.get(ContentModel.PROP_OWNER);

        if (!EqualsHelper.nullSafeEquals(pb, pa))
        {
            nodeOwnerCache.remove(nodeRef);
            return;
        }
        
        pb = before.get(ContentModel.PROP_CREATOR);
        pa = after.get(ContentModel.PROP_CREATOR);

        if (pb != null && !EqualsHelper.nullSafeEquals(pb, pa))
        {
            // A 'null' creator means this is a new node
            nodeOwnerCache.remove(nodeRef);
            return;
        }
    }
    
    /**
     * When an owned or audited node is copied, control which properties
     *  go over, and which are re-created
     */
    public CopyBehaviourCallback onCopyNode(QName classRef, CopyDetails copyDetails)
    {
        return AuditableOwnableAspectCopyBehaviourCallback.INSTANCE;   
    }
    
    private void cacheOwner(NodeRef nodeRef, String userName)
    {
        // do not cache owners of nodes that are from stores that ignores policies
        // to prevent mess in nodeOwnerCache
        if (!storesToIgnorePolicies.contains(tenantService.getBaseName(nodeRef.getStoreRef()).toString()))
        {
            nodeOwnerCache.put(nodeRef, userName);
        }
    }
    
    /**
     * Extends the default copy behaviour to prevent copying of some ownable and
     *  auditable properties, but lets the aspects themselves go through.
     * 
     * @author Nick Burch
     * @since 3.4
     */
    private static class AuditableOwnableAspectCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
        private static final CopyBehaviourCallback INSTANCE = new AuditableOwnableAspectCopyBehaviourCallback();
        
        /**
         * Don't copy certain auditable p
         */
        @Override
        public Map<QName, Serializable> getCopyProperties(
                QName classQName, CopyDetails copyDetails, Map<QName, Serializable> properties)
        {
            if(classQName.equals(ContentModel.ASPECT_OWNABLE))
            {
                // The owner should become the user doing the copying
                if(properties.containsKey(ContentModel.PROP_OWNER))
                {
                    properties.put(ContentModel.PROP_OWNER, AuthenticationUtil.getFullyAuthenticatedUser());
                }
            }
            else if(classQName.equals(ContentModel.ASPECT_AUDITABLE))
            {
                // Have the key properties reset by the aspect
                properties.remove(ContentModel.PROP_CREATED);
                properties.remove(ContentModel.PROP_CREATOR);
                properties.remove(ContentModel.PROP_MODIFIED);
                properties.remove(ContentModel.PROP_MODIFIER);
            }
            
            return properties;
        }
        
        /**
         * Do copy the aspects
         * 
         * @return          Returns <tt>true</tt> always
         */
        @Override
        public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
        {
            return true;
        }
    }
}
