/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.download;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * ActionServiceHelper interface.
 * 
 * Allows the download service to switch between executing the zip creation process in the current alfresco node,
 * or on a remote node.
 *  
 * @author Alex Miller
 */
public interface ActionServiceHelper
{

    /**
     * Implementations should trigger the CreateDownloadArchiveAction on the provided downloadNode
     * 
     * @param downloadNode
     */
    void executeAction(NodeRef downloadNode);

}
