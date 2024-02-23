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
package org.alfresco.rest.repo.resource.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestCategoryModel;

public class NestedCategoriesCreator extends SerialCategoriesCreator
{

    public NestedCategoriesCreator(RestWrapper restClient, Map<String, RestCategoryModel> categories)
    {
        super(restClient, categories);
    }

    @Override
    public List<RestCategoryModel> create()
    {
        return createNestedCategories(parent, names, 0);
    }

    private List<RestCategoryModel> createNestedCategories(RestCategoryModel parent, List<String> categoryNames, int index)
    {
        List<RestCategoryModel> createdCategories = new ArrayList<>();
        categoryNames.stream().findFirst().ifPresent(categoryName -> {
            RestCategoryModel createdCategory = createCategory(categoryName, getOrNull(aliases, index), parent);
            createdCategories.add(createdCategory);
            List<String> remainingNames = categoryNames.stream().skip(1).toList();
            if (!remainingNames.isEmpty())
            {
                createdCategories.addAll(createNestedCategories(createdCategory, remainingNames, index + 1));
            }
        });

        return createdCategories;
    }
}
