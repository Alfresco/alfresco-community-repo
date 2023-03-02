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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;

import org.alfresco.rest.api.Categories;
import org.alfresco.rest.api.model.Category;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NodesCategoryLinksRelationTest
{
    private static final String CONTENT_ID = "content-node-id";
    private static final String CATEGORY_ID = "category-id";

    @Mock
    private Categories categoriesMock;
    @Mock
    private Category categoryMock;
    @Mock
    private Parameters parametersMock;

    @InjectMocks
    private NodesCategoryLinksRelation objectUnderTest;

    @Test
    public void testReadAll()
    {
        given(categoriesMock.listCategoriesForNode(any(), any())).willReturn(List.of(categoryMock));

        // when
        final CollectionWithPagingInfo<Category> actualCategoriesPage = objectUnderTest.readAll(CONTENT_ID, parametersMock);

        then(categoriesMock).should().listCategoriesForNode(CONTENT_ID, parametersMock);
        then(categoriesMock).shouldHaveNoMoreInteractions();
        assertThat(actualCategoriesPage)
            .isNotNull()
            .extracting(CollectionWithPagingInfo::getCollection)
            .isEqualTo(List.of(categoryMock));
    }

    @Test
    public void testCreate()
    {
        given(categoriesMock.linkNodeToCategories(any(), any(), any())).willReturn(List.of(categoryMock));

        // when
        final List<Category> actualCategories = objectUnderTest.create(CONTENT_ID, List.of(categoryMock), parametersMock);

        then(categoriesMock).should().linkNodeToCategories(CONTENT_ID, List.of(categoryMock), parametersMock);
        then(categoriesMock).shouldHaveNoMoreInteractions();
        assertThat(actualCategories)
            .isNotNull()
            .isEqualTo(List.of(categoryMock));
    }

    @Test
    public void testDelete()
    {
        // when
        objectUnderTest.delete(CONTENT_ID, CATEGORY_ID, parametersMock);

        then(categoriesMock).should().unlinkNodeFromCategory(CONTENT_ID, CATEGORY_ID, parametersMock);
        then(categoriesMock).shouldHaveNoMoreInteractions();
    }
}