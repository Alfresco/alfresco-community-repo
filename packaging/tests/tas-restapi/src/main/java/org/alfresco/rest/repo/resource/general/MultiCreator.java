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
package org.alfresco.rest.repo.resource.general;

import java.util.List;
import java.util.random.RandomGenerator;

import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestModel;
import org.alfresco.utility.model.UserModel;

/**
 * Declares actions, which can be performed to create multiple repository resources.
 *
 * @param <RESOURCE> repository resource, e.g. folder, category, etc.
 * @param <SELF> return type - this interface extension or implementation
 */
public interface MultiCreator<RESOURCE extends TestModel, SELF extends MultiCreator<RESOURCE, ?>>
{
    SELF withNames(String... names);

    <USER extends UserModel> SELF asUser(USER user);

    List<RESOURCE> create();

    interface ContentsCreator<MODEL extends ContentModel, SELF extends ContentsCreator<MODEL, ?>> extends MultiCreator<MODEL, SELF>
    {
        SELF withTitles(String... titles);

        SELF withRandomTitles();

        SELF withDescriptions(String... descriptions);

        SELF withRandomDescriptions();

        <FOLDER extends FolderModel> SELF underFolder(FOLDER parent);

        <SITE extends SiteModel> SELF withinSite(SITE site);
    }

    interface FoldersCreator extends ContentsCreator<FolderModel, FoldersCreator>
    {}

    interface FilesCreator extends ContentsCreator<FileModel, FilesCreator>
    {
        FilesCreator ofTypes(FileType... fileTypes);

        FilesCreator withContents(List<String> filesContents);

        default FilesCreator withRandomContents()
        {
            return withRandomContents(RandomGenerator.getDefault().nextInt(30, 70), 10);
        }

        FilesCreator withRandomContents(int wordsCount, int wordsMaxLength);
    }

    interface CategoriesCreator extends MultiCreator<RestCategoryModel, CategoriesCreator>
    {
        <CATEGORY extends RestCategoryModel> CategoriesCreator underCategory(CATEGORY parent);
    }
}
