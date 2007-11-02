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
package org.alfresco.email.server;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Class that supports functionality of aliasable aspect.
 * 
 * @author YanO
 * @since 2.2
 */
public class AliasableAspect implements NodeServicePolicies.OnAddAspectPolicy, NodeServicePolicies.OnUpdatePropertiesPolicy
{
    private PolicyComponent policyComponent;

    private NodeService nodeService;

    private SearchService searchService;

    public static final String SEARCH_TEMPLATE =
        "ASPECT:\"" + EmailServerModel.ASPECT_ALIASABLE +
        "\" +@" + NamespaceService.EMAILSERVER_MODEL_PREFIX + "\\:" + EmailServerModel.PROP_ALIAS.getLocalName() + ":\"%s\"";

    /**
     * @param searchService Alfresco Search Service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * @param nodeService  Alfresco Node Service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param policyComponent  Alfresco Policy Component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Spring initilaise method used to register the policy behaviours
     */
    public void initialise()
    {
        // Register the policy behaviours
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"), EmailServerModel.ASPECT_ALIASABLE, new JavaBehaviour(this,
                "onAddAspect", NotificationFrequency.FIRST_EVENT));
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), EmailServerModel.ASPECT_ALIASABLE, new JavaBehaviour(this,
                "onUpdateProperties"));
    }

    /**
     * Check that alias property isn't duplicated. If the rule is broken, AlfrescoRuntimeException will be thrown.
     * 
     * @param nodeRef Reference to target node
     * @param alias Alias that we want to set to the targen node
     * @exception AlfrescoRuntimeException Throws if the <b>alias</b> property is duplicated.
     */
    private void checkAlias(NodeRef nodeRef, String alias)
    {
        // Try to find duplication in the system
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        // Create search string like this: ASPECT:"emailserver:aliasable" +@emailserver\:alias:"alias_string"
        String query = String.format(SEARCH_TEMPLATE, alias);
        ResultSet res = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, query);
        for (int i = 0; i < res.length(); i++)
        {
            NodeRef resRef = res.getNodeRef(i);
            Object otherAlias = nodeService.getProperty(resRef, EmailServerModel.PROP_ALIAS);
            if (!resRef.equals(nodeRef) && alias.equals(otherAlias))
            {
                throw new AlfrescoRuntimeException("Node with alias=\"" + alias + "\" already exists. Duplicate isn't allowed.");
            }
        }
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies$OnAddAspectPolicy#onAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     * @exception AlfrescoRuntimeException Throws if the <b>alias</b> property is duplicated.
     */
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        Object alias = nodeService.getProperty(nodeRef, EmailServerModel.PROP_ALIAS);
        if (alias != null)
        {
            checkAlias(nodeRef, alias.toString());
        }
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies$OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     * @exception AlfrescoRuntimeException Throws if the <b>alias</b> property is duplicated.
     */
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        Serializable alias = after.get(EmailServerModel.PROP_ALIAS);
        if (alias != null)
        {
            checkAlias(nodeRef, alias.toString());
        }

    }
}
