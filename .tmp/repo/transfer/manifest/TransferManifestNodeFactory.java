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
package org.alfresco.repo.transfer.manifest;

import org.alfresco.repo.transfer.TransferContext;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.TransferDefinition;

public interface TransferManifestNodeFactory
{
    /**
     * Create an object that represents the specified node in a form that can be used to transfer it elsewhere.
     * Calling this operation is identical to calling {@link TransferManifestNodeFactory#createTransferManifestNode(NodeRef, TransferDefinition, TransferContext, boolean)}
     * specifying <code>false</code> as the value of the <code>forceDelete</code> parameter.
     * @param nodeRef The identifier of the node to be distilled for transfer
     * @param definition The transfer definition against which the node is being transferred
     * @param transferContext internal runtime context of a transfer
     * @return An object that holds a snapshot of the state of the specified node suitable for transfer elsewhere. 
     */
    TransferManifestNode createTransferManifestNode(NodeRef nodeRef, TransferDefinition definition, TransferContext transferContext);

    /**
     * Create an object that represents the specified node in a form that can be used to transfer it elsewhere
     * @param nodeRef The identifier of the node to be distilled for transfer
     * @param definition The transfer definition against which the node is being transferred
     * @param forceDelete If this flag is set then the returned TransferManifestNode object will represent the removal
     * of the specified node, even if the node still exists in this repository. This allows a node to be removed from the
     * target repository even if it hasn't been removed in the source repository.
     * @param transferContext internal runtime context of a transfer
     * @return An object that holds a snapshot of the state of the specified node suitable for transfer elsewhere. 
     */
    TransferManifestNode createTransferManifestNode(NodeRef nodeRef, TransferDefinition definition, TransferContext transferContext, boolean forceDelete);
}
