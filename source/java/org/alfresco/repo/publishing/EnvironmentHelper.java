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

package org.alfresco.repo.publishing;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;

/**
 * A utility class to help out with environment-related operations that are used by both the channel service
 * and the publishing service.
 * 
 * @author Brian
 *
 */
public class EnvironmentHelper
{
    private static final String ENVIRONMENT_CONTAINER_NAME = "environments";
    
    private Set<QName> environmentNodeTypes;
    private SiteService siteService;
    private NodeService nodeService;
    
    public EnvironmentHelper()
    {
        environmentNodeTypes = new HashSet<QName>();
        environmentNodeTypes.add(PublishingModel.TYPE_ENVIRONMENT);
    }
    
    /**
     * @param environmentNodeTypes the environmentNodeTypes to set
     */
    public void setEnvironmentNodeTypes(Set<QName> environmentNodeTypes)
    {
        this.environmentNodeTypes = environmentNodeTypes;
    }

    /**
     * @param siteService the siteService to set
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public Map<String,NodeRef> getEnvironments(String siteId)
    {
        Map<String,NodeRef> results = new TreeMap<String, NodeRef>(); 
        NodeRef environmentContainer = getEnvironmentContainer(siteId);
        List<ChildAssociationRef> envAssocs = nodeService.getChildAssocs(environmentContainer, getEnvironmentNodeTypes());
        for (ChildAssociationRef envAssoc : envAssocs)
        {
            NodeRef environment = envAssoc.getChildRef();
            String name = (String)nodeService.getProperty(environment, ContentModel.PROP_NAME);
            results.put(name, environment);
        }
        return results;
    }

    /**
     * @return
     */
    protected Set<QName> getEnvironmentNodeTypes()
    {
        return environmentNodeTypes;
    }

    /**
     * @param siteId
     * @return
     */
    private NodeRef getEnvironmentContainer(final String siteId)
    {
        return AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                NodeRef channelContainer = siteService.getContainer(siteId, ENVIRONMENT_CONTAINER_NAME);
                if (channelContainer == null)
                {
                    // No channel container exists for this site yet. Create it.
                    channelContainer = siteService.createContainer(siteId, ENVIRONMENT_CONTAINER_NAME,
                            PublishingModel.TYPE_CHANNEL_CONTAINER, null);
                }
                return channelContainer;
            }
        }, AuthenticationUtil.getSystemUserName());

    }
}
