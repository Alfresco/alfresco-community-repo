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

import java.io.Serializable;
import java.util.HashMap;
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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * A utility class to help out with environment-related operations that are used
 * by both the channel service and the publishing service.
 * 
 * @author Brian
 * 
 */
public class EnvironmentHelper
{
    private static final String ENVIRONMENT_CONTAINER_NAME = "environments";
    public static final String LIVE_ENVIRONMENT_NAME = "live";
    private static final Set<QName> PUBLISHING_QUEUE_TYPE = new HashSet<QName>();
    private Set<QName> environmentNodeTypes;

    private SiteService siteService;
    private NodeService nodeService;
    private NodeRefMapper nodeRefMapper;

    static
    {
        PUBLISHING_QUEUE_TYPE.add(PublishingModel.TYPE_PUBLISHING_QUEUE);
    }

    public EnvironmentHelper()
    {
        environmentNodeTypes = new HashSet<QName>();
        environmentNodeTypes.add(PublishingModel.TYPE_ENVIRONMENT);
    }

    /**
     * @param environmentNodeTypes
     *            the environmentNodeTypes to set
     */
    public void setEnvironmentNodeTypes(Set<QName> environmentNodeTypes)
    {
        this.environmentNodeTypes = environmentNodeTypes;
    }

    /**
     * @param siteService
     *            the siteService to set
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    /**
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param nodeRefMapper
     *            the nodeRefMapper to set
     */
    public void setNodeRefMapper(NodeRefMapper nodeRefMapper)
    {
        this.nodeRefMapper = nodeRefMapper;
    }

    public Map<String, NodeRef> getEnvironments(String siteId)
    {
        Map<String, NodeRef> results = new TreeMap<String, NodeRef>();
        NodeRef environmentContainer = getEnvironmentContainer(siteId);
        List<ChildAssociationRef> envAssocs = nodeService.getChildAssocs(environmentContainer,
                getEnvironmentNodeTypes());
        for (ChildAssociationRef envAssoc : envAssocs)
        {
            NodeRef environment = envAssoc.getChildRef();
            String name = (String) nodeService.getProperty(environment, ContentModel.PROP_NAME);
            results.put(name, environment);
        }
        return results;
    }

    /**
     * Given a noderef from the editorial space, this returns the corresponding noderef in the specified environment
     * @param environmentNode
     * @param nodeToMap
     * @return
     */
    public NodeRef mapEditorialToEnvironment(NodeRef environmentNode, NodeRef nodeToMap)
    {
        return nodeRefMapper.mapSourceNodeRef(nodeToMap);
    }

    /**
     * Given a noderef from the specified environment, this returns the corresponding noderef in the editorial space
     * @param environmentNode
     * @param nodeToMap
     * @return
     */
    public NodeRef mapEnvironmentToEditorial(NodeRef environmentNode, NodeRef nodeToMap)
    {
        return nodeRefMapper.mapDestinationNodeRef(nodeToMap);
    }

    public NodeRef getEnvironment(String siteId, String name)
    {
        ParameterCheck.mandatory("siteId", siteId);
        ParameterCheck.mandatory("name", name);

        NodeRef environmentContainer = getEnvironmentContainer(siteId);
        return nodeService.getChildByName(environmentContainer, ContentModel.ASSOC_CONTAINS, name);
    }

    public NodeRef getPublishingQueue(NodeRef environment)
    {
        ParameterCheck.mandatory("environment", environment);

        ChildAssociationRef queueAssoc = null;
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(environment, PUBLISHING_QUEUE_TYPE);
        for (ChildAssociationRef childAssoc : childAssocs)
        {
            if (childAssoc.getTypeQName().equals(PublishingModel.ASSOC_PUBLISHING_QUEUE))
            {
                queueAssoc = childAssoc;
                break;
            }
        }

        if (queueAssoc == null)
        {
            // No publishing queue
            queueAssoc = nodeService.createNode(environment, PublishingModel.ASSOC_PUBLISHING_QUEUE, QName.createQName(
                    PublishingModel.NAMESPACE, "publishingQueue"), PublishingModel.TYPE_PUBLISHING_QUEUE);
        }
        return queueAssoc.getChildRef();
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
                NodeRef environmentContainer = siteService.getContainer(siteId, ENVIRONMENT_CONTAINER_NAME);
                if (environmentContainer == null)
                {
                    // No environment container exists for this site yet. Create
                    // it.
                    environmentContainer = siteService.createContainer(siteId, ENVIRONMENT_CONTAINER_NAME,
                            PublishingModel.TYPE_CHANNEL_CONTAINER, null);

                    // Also create the default live environment
                    Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                    props.put(ContentModel.PROP_NAME, LIVE_ENVIRONMENT_NAME);
                    nodeService.createNode(environmentContainer, ContentModel.ASSOC_CONTAINS, QName.createQName(
                            NamespaceService.CONTENT_MODEL_1_0_URI, LIVE_ENVIRONMENT_NAME),
                            PublishingModel.TYPE_ENVIRONMENT, props);
                }
                return environmentContainer;
            }
        }, AuthenticationUtil.getSystemUserName());

    }
}
