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

import static org.alfresco.rest.repo.resource.Folders.FOLDER_NAME_PREFIX;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.rest.repo.resource.general.MultiCreator;
import org.alfresco.rest.repo.resource.general.MultipleContentsCreator;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.model.FolderModel;

public class SerialFoldersCreator
    extends MultipleContentsCreator<FolderModel, MultiCreator.FoldersCreator>
    implements MultiCreator.FoldersCreator
{

    private final DataContent dataContent;
    private final Map<String, FolderModel> foldersCache;

    public SerialFoldersCreator(DataContent dataContent, Map<String, FolderModel> foldersCache)
    {
        super();
        this.dataContent = dataContent;
        this.foldersCache = foldersCache;
    }

    @Override
    protected SerialFoldersCreator self()
    {
        return this;
    }

    @Override
    public List<FolderModel> create()
    {
        verifyDataConsistency();
        return createRawFoldersUnder(parent, names);
    }

    @Override
    protected String generateRandomName()
    {
        return this.generateRandomNameWith(EMPTY);
    }

    @Override
    protected String generateRandomNameWith(String prefix)
    {
        return super.generateRandomNameWith(FOLDER_NAME_PREFIX + prefix + "_");
    }

    protected FolderModel createFolder(String folderName, String title, String description, FolderModel parent, String alias)
    {
        return new RepoFolderCreator(dataContent, foldersCache)
            .withAlias(alias)
            .withName(folderName)
            .withTitle(title)
            .withDescription(description)
            .underFolder(parent)
            .withinSite(site)
            .asUser(user)
            .create();
    }

    private List<FolderModel> createRawFoldersUnder(FolderModel parent, List<String> folderNames)
    {
        List<FolderModel> createdFolders = new ArrayList<>();
        AtomicInteger i = new AtomicInteger();
        folderNames.forEach(folderName -> {
            FolderModel createdFolder = createFolder(folderName, getOrNull(titles, i.get()), getOrNull(descriptions, i.get()), parent, getOrNull(aliases, i.get()));
            createdFolders.add(createdFolder);
            i.getAndIncrement();
        });

        return createdFolders;
    }
}
