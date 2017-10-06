/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.utils;

import org.alfresco.rest.model.RestNodeBodyMoveCopyModel;

/**
 * Utility class for core components models
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class CoreUtil
{
    private CoreUtil()
    {
        // Intentionally blank
    }

    /**
     * Creates a body model for move/copy with the given the target node id
     *
     * @param nodeId The node id
     * @return The {@link RestNodeBodyMoveCopyModel} with for the given node id
     */
    public static RestNodeBodyMoveCopyModel createBodyForMoveCopy(String nodeId)
    {
        RestNodeBodyMoveCopyModel moveDestinationInfo = new RestNodeBodyMoveCopyModel();
        moveDestinationInfo.setTargetParentId(nodeId);
        return moveDestinationInfo;
    }
}
