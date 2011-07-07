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

package org.alfresco.service.cmr.publishing;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;

/**
 * @author Brian
 *
 */
public interface NodeSnapshot
{
    /**
     * Retrieve the identifier of the node of which this is a snapshot
     * @return The NodeRef object that identifies the node
     */
    NodeRef getNodeRef();
    
    /**
     * Retrieve the primary parent association of the node at the moment this snapshot was taken
     * @return The ChildAssociationRef object that describes the primary parent association of the node
     */
    ChildAssociationRef getPrimaryParentAssoc();
    
    /**
     * Retrieve the primary path to the node at the moment this snapshot was taken. Note that the elements in this path describe
     * the associations between the nodes in the hierarchy - not the "display path" 
     * @return A Path object that describes the primary path to the node
     */
    Path getPrimaryPath();
    
    /**
     * The property values assigned to the node at the moment the snapshot was taken.
     * @return A map that associates property names to property values for the node.
     */
    Map<QName, Serializable> getProperties();
    
    /**
     * Retrieve all the parent associations of the node at the moment the snapshot was taken
     * @return A list of ChildAssociationRef objects, each describing one parent association of the node.
     */
    List<ChildAssociationRef> getAllParentAssocs();
    
    /**
     * Retrieve all the peer associations for which this node was the source at the moment the snapshot was taken
     * @return A list of AssociationRef objects, each describing a peer association for which this node is the source
     */
    List<AssociationRef> getOutboundPeerAssociations();
    
    /**
     * Retrieve the type of the node at the moment the snapshot was taken.
     * @return The QName that identifies the type of the node
     */
    QName getType();
    
    /**
     * Retrieve all the aspects that were applied to the node at the moment the snapshot was taken
     * @return A set of QName objects, each identifying an aspect that is applied to the node
     */
    Set<QName> getAspects();
    
    /**
     * @return the version of the node when the snapshot was taken.
     */
    String getVersion();
}
