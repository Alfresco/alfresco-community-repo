/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.rm.rest.api;

import org.alfresco.rest.api.Nodes;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * RM Nodes API
 *
 * @author Ana Bozianu
 * @since 2.6
 */
public interface RMNodes extends Nodes
{
    public static String PATH_FILE_PLAN = "-filePlan-";
    public static String PATH_TRANSFERS = "-transfers-";
    public static String PATH_UNFILED = "-unfiled-";
    public static String PATH_HOLDS = "-holds-";

    public static String PARAM_INCLUDE_HAS_RETENTION_SCHEDULE = "hasRetentionSchedule";
    public static String PARAM_INCLUDE_IS_CLOSED = "isClosed";
    public static String PARAM_INCLUDE_IS_COMPLETED = "isCompleted";

    /**
     * Gets or creates the relative path starting from the provided parent folder.
     * The method decides the type of the created elements considering the 
     * parent container's type and the type of the node to be created.
     * @param parentFolderNodeId the parent folder to start from
     * @param relativePath the relative path
     * @param nodeTypeQName the type of the node to be created
     * @return reference to the last element of the created path
     */
    public NodeRef getOrCreatePath(String parentFolderNodeId, String relativePath, QName nodeTypeQName);
}
