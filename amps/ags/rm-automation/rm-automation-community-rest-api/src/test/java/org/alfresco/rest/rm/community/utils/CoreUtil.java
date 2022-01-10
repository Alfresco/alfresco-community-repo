/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

import java.lang.reflect.InvocationTargetException;

import org.alfresco.rest.model.RestNodeBodyMoveCopyModel;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.RepoTestModel;

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

    /**
     * Helper method to create a Content Model
     *
     * @return ContentModel
     */
    public static ContentModel toContentModel(String nodeId)
    {
        return toModel(nodeId, ContentModel.class);
    }

    /**
     * Helper method to create a File Model
     *
     * @return ContentModel
     */
    public  static FileModel toFileModel(String nodeId)
    {
        return toModel(nodeId,FileModel.class);
    }

    /**
     * Helper method to create a RepoTestModel using the node id
     *
     * @param nodeId  node ref of the test model
     * @param classOf repo test model class
     * @return
     */
    private static <T extends RepoTestModel> T toModel(String nodeId, Class classOf)
    {
        T target = null;
        try
        {
            target = (T) classOf.getDeclaredConstructor().newInstance();
        }
        catch (InvocationTargetException| NoSuchMethodException| IllegalAccessException | InstantiationException e)
        {
            e.printStackTrace();
        }

        target.setNodeRef(nodeId);
        return target;

    }


}
