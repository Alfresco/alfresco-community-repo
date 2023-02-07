/*
 * #%L
 * Alfresco Remote API
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.categories;

import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.alfresco.rest.api.Categories;
import org.alfresco.rest.api.model.Category;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubcategoriesRelationTest
{
    private static final String PARENT_CATEGORY_ID = "parent-category-node-id";
    private static final String CATEGORY_ID = "category-node-id";
    private static final String CATEGORY_NAME = "categoryName";
    private static final String SUBCATEGORY_NAME_PREFIX = "childCategoryName";

    @Mock
    private Categories categoriesMock;
    @Mock
    private Parameters parametersMock;

    @InjectMocks
    private SubcategoriesRelation objectUnderTest;

    @Test
    public void testCreateSubcategory()
    {
        final Category categoryToCreate = Category.builder().name(CATEGORY_NAME).create();
        final Category category = Category.builder().name(CATEGORY_NAME).parentId(PARENT_CATEGORY_ID).hasChildren(false).id(CATEGORY_ID).create();
        final List<Category> categoriesToCreate = List.of(categoryToCreate);
        given(categoriesMock.createSubcategories(any(), any(), any())).willCallRealMethod();
        given(categoriesMock.createSubcategories(any(), any(), any(), any())).willReturn(List.of(category));

        //when
        List<Category> categories = objectUnderTest.create(PARENT_CATEGORY_ID, categoriesToCreate, parametersMock);

        then(categoriesMock).should().createSubcategories(STORE_REF_WORKSPACE_SPACESSTORE, PARENT_CATEGORY_ID, categoriesToCreate, parametersMock);
        assertEquals(List.of(category), categories);
    }

    @Test
    public void testGetCategoryChildren() {
        final List<Category> categoryChildren = getCategories(3);
        given(categoriesMock.getCategoryChildren(any(), any())).willCallRealMethod();
        given(categoriesMock.getCategoryChildren(any(), any(), any())).willReturn(categoryChildren);

        //when
        final CollectionWithPagingInfo<Category> returnedChildren = objectUnderTest.readAll(PARENT_CATEGORY_ID, parametersMock);

        then(categoriesMock).should().getCategoryChildren(STORE_REF_WORKSPACE_SPACESSTORE, PARENT_CATEGORY_ID, parametersMock);
        assertEquals(categoryChildren, returnedChildren.getCollection());
    }

    private List<Category> getCategories(final int count)
    {
        return IntStream.range(0, count)
            .mapToObj(i -> Category.builder().name(SUBCATEGORY_NAME_PREFIX + "-" + i)
                .parentId(PARENT_CATEGORY_ID)
                .hasChildren(false)
                .id(CATEGORY_ID + "-" + i)
                .create())
            .collect(Collectors.toList());
    }
}
