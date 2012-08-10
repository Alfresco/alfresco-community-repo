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

package org.alfresco.repo.node;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utilities for working with System Nodes
 * 
 * @author Nick Burch
 * @since 4.1
 */
public abstract class SystemNodeUtils
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(SystemNodeUtils.class);
    
    private static QName SYSTEM_FOLDER_QNAME =
            QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "system");
    
    /**
     * Returns the System Container for the current tenant
     */
    public static NodeRef getSystemContainer(final NodeService nodeService, final Repository repositoryHelper)
    {
        // Grab the root of the repository, for the current tennant
        final NodeRef root = repositoryHelper.getRootHome();

        // Locate the system folder, in the root 
        List<ChildAssociationRef> sysRefs = nodeService.getChildAssocs(
                root, ContentModel.ASSOC_CHILDREN, SYSTEM_FOLDER_QNAME);
        if (sysRefs.size() != 1)
        {
            throw new IllegalStateException("System folder missing / duplicated! Found " + sysRefs);
        }
        final NodeRef system = sysRefs.get(0).getChildRef();
        
        return system;
    }
    
    /**
     * Returns the NodeRef of a given Child Container within the current Tenant's 
     *  System Container, if found
     */
    public static NodeRef getSystemChildContainer(final QName childName, final NodeService nodeService, final Repository repositoryHelper)
    {
        NodeRef system = getSystemContainer(nodeService, repositoryHelper);

        // Find the container, under system
        List<ChildAssociationRef> containerRefs = nodeService.getChildAssocs(
                system, ContentModel.ASSOC_CHILDREN, childName);

        NodeRef container = null;
        if (containerRefs.size() > 0)
        {
            container = containerRefs.get(0).getChildRef();
            if (containerRefs.size() > 1)
                logger.warn("Duplicate Shared Credentials Containers found: " + containerRefs);
        }

        return container;
    }
    
    /**
     * Returns the NodeRef of a given Child Container within the current Tenant's System Container,
     *  creating the Container as System if required.
     * The calling code should handle retries, locking etc.
     * 
     * @return the Child Container NodeRef, and whether the Container has just been created
     */
    public static Pair<NodeRef, Boolean> getOrCreateSystemChildContainer(final QName childName, 
            final NodeService nodeService, final Repository repositoryHelper)
    {
        NodeRef container = getSystemChildContainer(childName, nodeService, repositoryHelper);
        if (container != null)
        {
            return new Pair<NodeRef,Boolean>(container, Boolean.FALSE);
        }
        
        // Create
        container = AuthenticationUtil.runAsSystem(new RunAsWork<NodeRef>() {
            @Override
            public NodeRef doWork() throws Exception
            {
                NodeRef system = getSystemContainer(nodeService, repositoryHelper);
                
                NodeRef container = nodeService.createNode(
                        system, ContentModel.ASSOC_CHILDREN, childName, ContentModel.TYPE_CONTAINER
                ).getChildRef();
                nodeService.setProperty(container, ContentModel.PROP_NAME, childName.getLocalName());
                
                return container;
            }
        });
        
        return new Pair<NodeRef,Boolean>(container, Boolean.TRUE);
    }
}
