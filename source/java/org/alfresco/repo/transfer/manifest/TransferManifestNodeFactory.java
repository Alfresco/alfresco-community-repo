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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.TransferDefinition;

public interface TransferManifestNodeFactory
{
    /**
     * Create an object that represents the specified node in a form that can be used to transfer it elsewhere.
     * Calling this operation is identical to calling {@link TransferManifestNodeFactory#createTransferManifestNode(NodeRef, TransferDefinition, boolean)}
     * specifying <code>false</code> as the value of the <code>forceDelete</code> parameter.
     * @param nodeRef The identifier of the node to be distilled for transfer
     * @param definition The transfer definition against which the node is being transferred
     * @return An object that holds a snapshot of the state of the specified node suitable for transfer elsewhere. 
     */
    TransferManifestNode createTransferManifestNode(NodeRef nodeRef, TransferDefinition definition);

    /**
     * Create an object that represents the specified node in a form that can be used to transfer it elsewhere
     * @param nodeRef The identifier of the node to be distilled for transfer
     * @param definition The transfer definition against which the node is being transferred
     * @param forceDelete If this flag is set then the returned TransferManifestNode object will represent the removal
     * of the specified node, even if the node still exists in this repository. This allows a node to be removed from the
     * target repository even if it hasn't been removed in the source repository.
     * @return An object that holds a snapshot of the state of the specified node suitable for transfer elsewhere. 
     */
    TransferManifestNode createTransferManifestNode(NodeRef nodeRef, TransferDefinition definition, boolean forceDelete);
}
