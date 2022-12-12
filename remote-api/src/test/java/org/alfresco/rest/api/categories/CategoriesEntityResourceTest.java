/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.alfresco.rest.api.Categories;
import org.alfresco.rest.api.model.Category;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CategoriesEntityResourceTest
{
    private static final String CATEGORY_ID = "category-node-id";
    @Mock
    private Categories categoriesMock;
    @Mock
    private Category categoryMock;
    @Mock
    private Parameters parametersMock;

    @InjectMocks
    private CategoriesEntityResource objectUnderTest;

    @Test
    public void testReadCategoryById()
    {
        given(categoriesMock.getCategoryById(CATEGORY_ID, parametersMock)).willReturn(categoryMock);

        //when
        final Category category = objectUnderTest.readById(CATEGORY_ID, parametersMock);

        then(categoriesMock).should().getCategoryById(CATEGORY_ID, parametersMock);
        then(categoriesMock).shouldHaveNoMoreInteractions();
        assertEquals(categoryMock, category);
    }
}
