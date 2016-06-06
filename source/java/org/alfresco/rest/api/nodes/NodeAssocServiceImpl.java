/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.nodes;

import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

import java.io.Serializable;
import java.util.List;

/**
 * @deprecated Intermediate wrapper (not a public service) - eventually push permissions down to NodeService
 *
 * @author janv
 */
public class NodeAssocServiceImpl implements NodeAssocService
{
    protected NodeService nodeService;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public AssociationRef createAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
            throws InvalidNodeRefException, AssociationExistsException

    {
        return nodeService.createAssociation(sourceRef, targetRef, assocTypeQName);
    }

    public void removeAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
            throws InvalidNodeRefException
    {
        nodeService.removeAssociation(sourceRef, targetRef, assocTypeQName);
    }

    public void setAssociations(NodeRef sourceRef, QName assocTypeQName, List<NodeRef> targetRefs)
    {
        nodeService.setAssociations(sourceRef, assocTypeQName, targetRefs);
    }

    public AssociationRef getAssoc(Long id)
    {
        return nodeService.getAssoc(id);
    }

    public List<AssociationRef> getTargetAssocs(NodeRef sourceRef, QNamePattern qnamePattern)
            throws InvalidNodeRefException
    {
        return nodeService.getTargetAssocs(sourceRef, qnamePattern);
    }


    public List<AssociationRef> getTargetAssocsByPropertyValue(NodeRef sourceRef, QNamePattern qnamePattern, QName propertyQName, Serializable propertyValue)
    {
        return nodeService.getTargetAssocsByPropertyValue(sourceRef, qnamePattern, propertyQName, propertyValue);
    }

    public List<AssociationRef> getSourceAssocs(NodeRef targetRef, QNamePattern qnamePattern)
            throws InvalidNodeRefException
    {
        return nodeService.getSourceAssocs(targetRef, qnamePattern);
    }
}
