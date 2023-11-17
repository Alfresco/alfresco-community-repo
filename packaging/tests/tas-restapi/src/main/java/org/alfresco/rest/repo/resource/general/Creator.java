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

import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestModel;
import org.alfresco.utility.model.UserModel;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Declares operations, which can be performed to create repository resource.
 *
 * @param <RESOURCE> repository resource, e.g. folder, category, etc.
 * @param <SELF> return type - this interface extension or implementation
 */
@SuppressWarnings({"PMD.GenericsNaming"})
public interface Creator<RESOURCE extends TestModel, SELF extends Creator<RESOURCE, ?>>
{
    SELF withName(String name);

    <USER extends UserModel> SELF asUser(USER user);

    RESOURCE create();

    interface ContentCreator<CONTENT extends ContentModel, SELF extends ContentCreator<CONTENT, ?>>
        extends Creator<CONTENT, SELF>
    {
        SELF withTitle(String title);

        default SELF withRandomTitle()
        {
            return withTitle(RandomStringUtils.randomAlphanumeric(10));
        }

        SELF withDescription(String description);

        default SELF withRandomDescription()
        {
            return withDescription(RandomStringUtils.randomAlphanumeric(20));
        }

        <FOLDER extends FolderModel> SELF underFolder(FOLDER parent);

        <SITE extends SiteModel> SELF withinSite(SITE site);
    }

    interface FolderCreator extends ContentCreator<FolderModel, FolderCreator>
    {}

    interface FileCreator extends ContentCreator<FileModel, FileCreator>
    {
        FileCreator withContent(String fileContent);

        default FileCreator withRandomContent()
        {
            return withRandomContent(RandomGenerator.getDefault().nextInt(30, 70), 10);
        }

        default FileCreator withRandomContent(int wordsNumber, int wordsMaxLength)
        {
            return withContent(IntStream.range(0, wordsNumber)
                .mapToObj(i -> RandomStringUtils.randomAlphanumeric(1, wordsMaxLength))
                .collect(Collectors.joining(" ")));
        }
    }

    interface CategoryCreator extends Creator<RestCategoryModel, CategoryCreator>
    {
        <CATEGORY extends RestCategoryModel> CategoryCreator underCategory(CATEGORY parent);
    }
}
