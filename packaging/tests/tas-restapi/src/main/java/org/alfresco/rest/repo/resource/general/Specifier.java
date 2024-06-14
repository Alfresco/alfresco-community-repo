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

import org.alfresco.rest.model.RestTagModel;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FileType;

/**
 * Specifies repository resource to perform an operation on, like: add, modify, remove.
 */
public interface Specifier
{
    interface FolderSpecifier extends Specifier
    {
        Creator.FolderCreator folder(String name);

        Creator.FolderCreator randomFolder();

        Creator.FolderCreator randomFolder(String prefix);

        MultiCreator.FoldersCreator folders(String... names);

        MultiCreator.FoldersCreator randomFolders(String... prefixes);

        MultiCreator.FoldersCreator randomFolders(int quantity);

        MultiCreator.FoldersCreator nestedFolders(String... names);

        MultiCreator.FoldersCreator nestedRandomFolders(String... prefixes);

        MultiCreator.FoldersCreator nestedRandomFolders(int depth);
    }

    interface FileSpecifier extends Specifier
    {
        Creator.FileCreator file(String name);

        Creator.FileCreator randomFile();

        Creator.FileCreator randomFile(String prefix);

        MultiCreator.FilesCreator files(String... names);

        MultiCreator.FilesCreator randomFiles(String... prefixes);

        MultiCreator.FilesCreator randomFiles(int quantity);
    }

    interface AssociationSpecifier extends Specifier
    {
        <CONTENT extends ContentModel> void secondaryContent(CONTENT content);

        <TAG extends RestTagModel> void tag(TAG tag);
    }

    interface MultiContentSpecifier extends FolderSpecifier, FileSpecifier, AssociationSpecifier
    {
        <CONTENT extends ContentModel> void secondaryContent(CONTENT... contents);

        <TAG extends RestTagModel> void tags(TAG... tags);
    }

    interface CategoriesSpecifier extends Specifier
    {
        Creator.CategoryCreator category(String name);

        Creator.CategoryCreator randomCategory();

        Creator.CategoryCreator randomCategory(String prefix);

        MultiCreator.CategoriesCreator categories(String... names);

        MultiCreator.CategoriesCreator randomCategories(String... prefixes);

        MultiCreator.CategoriesCreator randomCategories(int quantity);

        MultiCreator.CategoriesCreator nestedCategories(String... names);

        MultiCreator.CategoriesCreator nestedRandomCategories(String... prefixes);

        MultiCreator.CategoriesCreator nestedRandomCategories(int depth);
    }
}
