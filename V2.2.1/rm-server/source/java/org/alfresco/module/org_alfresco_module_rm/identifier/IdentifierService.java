/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.identifier;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Records management identifier service
 *
 * @author Roy Wetherall
 */
public interface IdentifierService
{
    /** Context value names */
    String CONTEXT_NODEREF = "noderef";
    String CONTEXT_PARENT_NODEREF = "parentndoeref";
    String CONTEXT_ORIG_TYPE = "origionaltype";

    /**
     * Register an identifier generator implementation with the service.
     *
     * @param identifierGenerator   identifier generator implementation
     */
    void register(IdentifierGenerator identifierGenerator);

    /**
     * Generate an identifier for a node with the given type and parent.
     *
     * @param type      type of the node
     * @param parent    parent of the ndoe
     * @return String   generated identifier
     */
    String generateIdentifier(QName type, NodeRef parent);

    /**
     * Generate an identifier for the given node.
     *
     * @param nodeRef   node reference
     * @return String   generated identifier
     */
    String generateIdentifier(NodeRef nodeRef);
}
