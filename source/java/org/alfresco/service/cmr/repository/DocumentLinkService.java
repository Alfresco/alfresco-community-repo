/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.service.cmr.repository;

/**
 * Provides methods specific to manipulating links of documents
 * 
 * @author Ana Bozianu
 * @since 5.1
 */
public interface DocumentLinkService
{

    /**
     * Creates a link node as child of the destination node
     * 
     * @param source
     *            Node to create a link for. Can be a file or a folder.
     * @param destination
     *            Destination to create the link in. Must be a folder.
     * @return A reference to the created link node
     */
    public NodeRef createDocumentLink(NodeRef source, NodeRef destination);

    /**
     * Returns the destination node of the provided link
     * 
     * @param linkNodeRef
     *            The link node.
     * @return A reference to the destination of the provided link node
     */
    public NodeRef getLinkDestination(NodeRef linkNodeRef);

    /**
     * Deletes all links having the provided node as destination.
     * 
     * @param document
     *            The destination of the links to be deleted.
     */
    public DeleteLinksStatusReport deleteLinksToDocument(NodeRef document);

}
