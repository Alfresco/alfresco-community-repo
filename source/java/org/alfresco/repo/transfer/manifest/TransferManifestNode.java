/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer.manifest;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;

/**
 * Data value object - part of the transfer manifest
 * 
 * Represents a single node in the transfer manifest
 * 
 * @see org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode
 * @see org.alfresco.repo.transfer.manifest.TransferManifestNormalNode
 *
 * @author Mark Rogers
 */
public interface TransferManifestNode
{
    public NodeRef getNodeRef();
    public void setNodeRef(NodeRef nodeRef);

    public void setUuid(String uuid);
    public String getUuid();
    
    public void setParentPath(Path parentPath);
    public Path getParentPath();
    
    public void setPrimaryParentAssoc(ChildAssociationRef primaryParent);
    public ChildAssociationRef getPrimaryParentAssoc();
}
