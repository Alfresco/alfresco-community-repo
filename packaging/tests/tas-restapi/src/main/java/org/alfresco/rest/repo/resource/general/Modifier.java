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

import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestModel;
import org.alfresco.utility.model.UserModel;

/**
 * Declares operations, which can be performed on repository resource.
 *
 * @param <RESOURCE> repository resource, e.g. folder, category, etc.
 * @param <SELF> return type - this interface extension or implementation
 */
public interface Modifier<RESOURCE extends TestModel, SELF extends Modifier<RESOURCE, ?>>
{
    RESOURCE get(String id);

    <USER extends UserModel> SELF asUser(USER user);

    void delete();

    interface ContentModifier<CONTENT extends ContentModel, SELF extends Modifier<CONTENT, ?>>
        extends Modifier<CONTENT, SELF>
    {
        <FOLDER extends FolderModel> void moveTo(FOLDER target);

        <FOLDER extends FolderModel> CONTENT copyTo(FOLDER target);

        <FOLDER extends FolderModel> void linkTo(FOLDER secondaryParent);

        <FOLDER extends FolderModel> void linkTo(FOLDER... secondaryParents);

        <FOLDER extends FolderModel> void unlinkFrom(FOLDER secondaryParent);

        <CATEGORY extends RestCategoryModel> void linkTo(CATEGORY category);

        <CATEGORY extends RestCategoryModel> void linkTo(CATEGORY... categories);

        <CATEGORY extends RestCategoryModel> void unlinkFrom(CATEGORY category);

        <SITE extends SiteModel> SELF withinSite(SITE site);
    }

    interface FolderModifier extends ContentModifier<FolderModel, FolderModifier>,
        ResourceIntroducer<Specifier.MultiContentSpecifier>, ResourceRemover<Specifier.AssociationSpecifier>
    {}

    interface FileModifier extends ContentModifier<FileModel, FileModifier>
    {}

    interface CategoryModifier extends Modifier<RestCategoryModel, CategoryModifier>,
        ResourceIntroducer<Specifier.CategoriesSpecifier>
    {}
}
