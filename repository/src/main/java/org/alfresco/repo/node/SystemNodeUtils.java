/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        List<ChildAssociationRef> containerRefs = getChildAssociationRefs(childName, nodeService, repositoryHelper);

        NodeRef container = null;
        if (containerRefs.size() > 0)
        {
            container = containerRefs.get(0).getChildRef();
            warnIfDuplicates(containerRefs);
        }

        return container;
    }

    /**
     * MNT-20212
     * Avoid using this method. It is meant only to fix that bug reported in the MNT
     *
     * Returns the list with all the NodeRef of a given Child Container within the current Tenant's
     *  System Container, if found
     */
    public static List<NodeRef> getSystemChildContainers(final QName childName, final NodeService nodeService, final Repository repositoryHelper)
    {
        List<NodeRef> allChildContainers = new ArrayList<NodeRef>();

        List<ChildAssociationRef> containerRefs = getChildAssociationRefs(childName, nodeService, repositoryHelper);

        if (containerRefs.size() > 0)
        {
            for (ChildAssociationRef containerRef : containerRefs)
            {
                allChildContainers.add(containerRef.getChildRef());
            }
            warnIfDuplicates(containerRefs);
        }

        return allChildContainers;
    }

    private static void warnIfDuplicates(List<ChildAssociationRef> containerRefs)
    {
        if (containerRefs.size() > 1)
        {
            logger.warn("Duplicate system containers found: " + containerRefs);
        }
    }

    private static List<ChildAssociationRef> getChildAssociationRefs(final QName childName, final NodeService nodeService,
        final Repository repositoryHelper)
    {
        final NodeRef system = getSystemContainer(nodeService, repositoryHelper);

        List<ChildAssociationRef> containerRefs = AuthenticationUtil.runAsSystem(new RunAsWork<List<ChildAssociationRef>>()
        {
            @Override
            public List<ChildAssociationRef> doWork() throws Exception
            {
                return nodeService.getChildAssocs(system, ContentModel.ASSOC_CHILDREN, childName);
            }
        });
        return containerRefs;
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
