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
package org.alfresco.repo.ownable.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.springframework.beans.factory.InitializingBean;

/**
 * Ownership service support. Use in permissions framework as dynamic authority.
 * 
 * @author Andy Hind
 */
public class OwnableServiceImpl implements OwnableService, InitializingBean, NodeServicePolicies.OnAddAspectPolicy, NodeServicePolicies.OnUpdatePropertiesPolicy,
        NodeServicePolicies.OnRemoveAspectPolicy, NodeServicePolicies.OnDeleteNodePolicy
{
    private NodeService nodeService;

    private AuthenticationService authenticationService;

    private SimpleCache<NodeRef, String> nodeOwnerCache;

    private PolicyComponent policyComponent;

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
        if (nodeService == null)
        {
            throw new IllegalArgumentException("Property 'nodeService' has not been set");
        }
        if (authenticationService == null)
        {
            throw new IllegalArgumentException("Property 'authenticationService' has not been set");
        }
        if (nodeOwnerCache == null)
        {
            throw new IllegalArgumentException("Property 'nodeOwnerCache' has not been set");
        }
        if (policyComponent == null)
        {
            throw new IllegalArgumentException("Property 'policyComponent' has not been set");
        }

        policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"), ContentModel.ASPECT_OWNABLE, new JavaBehaviour(this, "onAddAspect"));
        policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), ContentModel.ASPECT_OWNABLE, new JavaBehaviour(this, "onUpdateProperties"));
        policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onRemoveAspect"), ContentModel.ASPECT_OWNABLE, new JavaBehaviour(this,
                "onRemoveAspect"));
        policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"), ContentModel.ASPECT_OWNABLE, new JavaBehaviour(this, "onDeleteNode"));
        
        policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"), ContentModel.ASPECT_AUDITABLE, new JavaBehaviour(this, "onAddAspect"));
        policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), ContentModel.ASPECT_AUDITABLE, new JavaBehaviour(this, "onUpdateProperties"));
        policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onRemoveAspect"), ContentModel.ASPECT_AUDITABLE, new JavaBehaviour(this,
                "onRemoveAspect"));
        policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"), ContentModel.ASPECT_AUDITABLE, new JavaBehaviour(this, "onDeleteNode"));

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
            nodeOwnerCache.put(nodeRef, userName);
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
        nodeOwnerCache.put(nodeRef, userName);
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

        if (!EqualsHelper.nullSafeEquals(pb, pa))
        {
            nodeOwnerCache.remove(nodeRef);
            return;
        }

    }
}
