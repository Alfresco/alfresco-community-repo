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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * MT ALF-6029 - fix bootstrapped creator/modifier and change "admin" to "System@xxx" (or "System" in case of default domain) for given nodes (ie. "Models" and "Workflow Definitions")
 */
public class MultiTFixAdminExistingTenantsPatch extends AbstractPatch
{
    private static final String MSG_RESULT = "patch.mtFixAdminExistingTenants.result";
    
    private TenantService tenantService;
    private BehaviourFilter policyBehaviourFilter;
    private NodeDAO nodeDAO;
    
    private List<String> pathsToNodes;
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }
    
    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter)
    {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }
    
    public void setPathsToNodes(List<String> pathsToNodes)
    {
        this.pathsToNodes = pathsToNodes;
    }
    
    /**
     * @see org.alfresco.repo.admin.patch.AbstractPatch#checkProperties()
     */
    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        
        checkPropertyNotNull(this.tenantService, "tenantService");
        checkPropertyNotNull(this.nodeDAO, "nodeDAO");
        checkPropertyNotNull(this.pathsToNodes, "pathsToNodes");
    }
    
    /**
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {
        String currentUserDomain = tenantService.getCurrentUserDomain();
        
        for (String xpathToNode : pathsToNodes)
        {
            fixAuditable(currentUserDomain, xpathToNode);
        }
        
        return I18NUtil.getMessage(MSG_RESULT);
    }
    
    private void fixAuditable(String currentUserDomain, String xpathToNode)
    {
        NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<NodeRef> nodeRefs = searchService.selectNodes(rootNodeRef,
                                  xpathToNode,
                                  null,
                                  namespaceService,
                                  false,
                                  SearchService.LANGUAGE_XPATH);
        
        if (nodeRefs.size() > 0)
        {
            NodeRef nodeRef = nodeRefs.get(0);
            
            Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(tenantService.getName(nodeRef)); // add tenant domain (since going via nodeDAO)
            if (nodePair != null)
            {
                String tenantSystem = tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), currentUserDomain);
                
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
                props.put(ContentModel.PROP_MODIFIER, tenantSystem);
                props.put(ContentModel.PROP_CREATOR, tenantSystem);
                
                try
                {
                    policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                    
                    nodeDAO.addNodeProperties(nodePair.getFirst(), props); // update only
                }
                finally
                {
                    policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                }
            }
        }
    }
}
