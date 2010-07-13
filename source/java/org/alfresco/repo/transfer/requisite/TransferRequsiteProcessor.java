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
package org.alfresco.repo.transfer.requisite;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Processor for transfer requsite file
 * @author mrogers
 *
 */
public interface TransferRequsiteProcessor
{
    /**
     * Called at the start of a transfer requsite
     */
    public void startTransferRequsite();
    
    /**
     * Called at the end of a transfer requsite 
     */
    public void endTransferRequsite();

    /**
     * Called when a missing content property is found
     * @param node
     * @param qname
     * @param name
     */
    public void missingContent(NodeRef node, QName qname, String name);
 

}
