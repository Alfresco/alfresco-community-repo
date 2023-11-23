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

import java.util.Map;

import org.alfresco.rest.repo.resource.general.ContentCreator;
import org.alfresco.rest.repo.resource.general.Creator;
import org.alfresco.rest.repo.resource.general.MultiCreator;
import org.alfresco.rest.repo.resource.general.Specifier;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.model.FolderModel;

public class RepoFolderCreator
    extends ContentCreator<FolderModel, Creator.FolderCreator>
    implements Creator.FolderCreator, Specifier.FolderSpecifier
{

    private final DataContent dataContent;
    private final Map<String, FolderModel> foldersCache;

    public RepoFolderCreator(DataContent dataContent, Map<String, FolderModel> foldersCache)
    {
        super(new FolderModel());
        this.dataContent = dataContent;
        this.foldersCache = foldersCache;
    }

    @Override
    protected RepoFolderCreator self()
    {
        return this;
    }

    @Override
    public FolderCreator folder(String name)
    {
        return this.withName(name);
    }

    @Override
    public FolderCreator randomFolder()
    {
        return this.withRandomName();
    }

    @Override
    public FolderCreator randomFolder(String prefix)
    {
        return this.withRandomName(prefix);
    }

    @Override
    public MultiCreator.FoldersCreator folders(String... names)
    {
        return new SerialFoldersCreator(dataContent, foldersCache).withNames(names).withinSite(site).asUser(user);
    }

    @Override
    public MultiCreator.FoldersCreator randomFolders(String... prefixes)
    {
        return new SerialFoldersCreator(dataContent, foldersCache).withRandomNames(prefixes).withinSite(site).asUser(user);
    }

    @Override
    public MultiCreator.FoldersCreator randomFolders(int quantity)
    {
        return new SerialFoldersCreator(dataContent, foldersCache).withRandomNames(quantity).withinSite(site).asUser(user);
    }

    @Override
    public MultiCreator.FoldersCreator nestedFolders(String... names)
    {
        return new NestedFoldersCreator(dataContent, foldersCache).withNames(names).withinSite(site).asUser(user);
    }

    @Override
    public MultiCreator.FoldersCreator nestedRandomFolders(String... prefixes)
    {
        return new NestedFoldersCreator(dataContent, foldersCache).withRandomNames(prefixes).withinSite(site).asUser(user);
    }

    @Override
    public MultiCreator.FoldersCreator nestedRandomFolders(int depth)
    {
        return new NestedFoldersCreator(dataContent, foldersCache).withRandomNames(depth).withinSite(site).asUser(user);
    }

    @Override
    public FolderCreator withRandomName(String prefix)
    {
        withAlias(prefix);
        return super.withRandomName(prefix);
    }

    @Override
    public FolderModel create()
    {
        FolderModel createdFolder = create(dataContent, dataContent::createFolder);
        foldersCache.put(alias, createdFolder);

        return createdFolder;
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
}
