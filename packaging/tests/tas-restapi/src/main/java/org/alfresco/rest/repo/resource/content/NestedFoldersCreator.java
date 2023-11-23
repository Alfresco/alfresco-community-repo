/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.rest.repo.resource.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.model.FolderModel;

public class NestedFoldersCreator extends SerialFoldersCreator
{

    public NestedFoldersCreator(DataContent dataContent, Map<String, FolderModel> folders)
    {
        super(dataContent, folders);
    }

    @Override
    public List<FolderModel> create()
    {
        verifyDataConsistency();
        return createNestedFoldersUnder(parent, names, 0);
    }

    private List<FolderModel> createNestedFoldersUnder(FolderModel parent, List<String> folderNames, int index)
    {
        List<FolderModel> createdFolders = new ArrayList<>();
        folderNames.stream().findFirst().ifPresent(folderName -> {
            FolderModel createdFolder = createFolder(folderName, getOrNull(titles, index), getOrNull(descriptions, index), parent, getOrNull(aliases, index));
            createdFolders.add(createdFolder);
            List<String> remainingNames = folderNames.stream().skip(1).toList();
            if (!remainingNames.isEmpty())
            {
                createdFolders.addAll(createNestedFoldersUnder(createdFolder, remainingNames, index + 1));
            }
        });

        return createdFolders;
    }
}
