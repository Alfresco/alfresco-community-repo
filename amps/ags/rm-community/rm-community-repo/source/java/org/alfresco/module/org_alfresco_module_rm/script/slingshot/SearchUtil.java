/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.script.slingshot;

import static org.alfresco.model.ContentModel.ASSOC_CHILDREN;
import static org.alfresco.model.ContentModel.TYPE_CONTAINER;
import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Parent class for records search utilities
 *
 * @author Ross Gale
 * @since 2.7
 */
public class SearchUtil
{
    /**
     * Node service
     */
    protected NodeService nodeService;

    /**
     * Setter for node service
     *
     * @param nodeService Node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Use a container node ref and return the nodeIds of the contents
     *
     * @param nodeRef container
     * @return list of nodeIds
     */
    protected Set<String> retrieveAllNodeIds(NodeRef nodeRef)
    {
        List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(nodeRef);
        return childAssocRefs.stream().map(assoc -> assoc.getChildRef().getId()).collect(Collectors.toSet());
    }

    /**
     * Helper method to get the classification reason root container.
     * The method creates the container if it doesn't already exist.
     *
     * @return reference to the classification reason root container
     */
    protected NodeRef getRootContainer(QName container)
    {
        NodeRef rootNodeRef = nodeService.getRootNode(STORE_REF_WORKSPACE_SPACESSTORE);
        List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(rootNodeRef, ASSOC_CHILDREN, container);

        if (assocRefs.isEmpty())
        {
            return nodeService.createNode(rootNodeRef, ASSOC_CHILDREN, container, TYPE_CONTAINER).getChildRef();
        }
        else if (assocRefs.size() != 1)
        {
            throw new AlfrescoRuntimeException("Only one container is allowed.");
        }
        else
        {
            return assocRefs.iterator().next().getChildRef();
        }
    }
}
