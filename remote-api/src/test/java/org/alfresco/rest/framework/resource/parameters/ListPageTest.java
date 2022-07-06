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

package org.alfresco.rest.framework.resource.parameters;

import junit.framework.TestCase;
import org.alfresco.service.Experimental;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Experimental
@RunWith(MockitoJUnitRunner.class)
public class ListPageTest extends TestCase
{

    private List<PageFormat> getPageFormats() {
        return List.of(
            new PageFormat(10, 0),
            new PageFormat(10, 10),
            new PageFormat(110, 20)
        );
    }

    @Test
    public void testCreatePage()
    {
        final List<PageFormat> pageFormats = getPageFormats();
        for (PageFormat pageFormat : pageFormats)
        {
            final int pageSize = pageFormat.size;
            final int offset = pageFormat.offset;
            final List<Object> list = randomListOf(offset,100, Object.class);
            final Paging paging = Paging.valueOf(offset, pageSize);

            // when
            final CollectionWithPagingInfo<Object> page = ListPage.of(list, paging);

            assertThat(page)
                    .isNotNull()
                .extracting(CollectionWithPagingInfo::getCollection)
                    .isNotNull()
                    .isEqualTo(list.subList(offset, Math.min(offset + pageSize, list.size())))
                .extracting(Collection::size)
                    .isEqualTo(Math.min(pageSize, list.size() - offset));
        }
    }

    @Test
    public void testCreatePageForBiggerOffsetThanListSize()
    {
        final int offset = 10;
        final List<Object> list = createListOf(8, Object.class);
        final Paging paging = Paging.valueOf(offset, 5);

        // when
        final CollectionWithPagingInfo<Object> page = ListPage.of(list, paging);

        assertThat(page)
                .isNotNull()
            .extracting(CollectionWithPagingInfo::getCollection)
                .isNotNull()
            .extracting(Collection::isEmpty)
                .isEqualTo(true);
    }

    @Test
    public void testCreatePageForNullList()
    {
        // when
        final CollectionWithPagingInfo<Object> page = ListPage.of(null, Paging.DEFAULT);

        assertThat(page)
                .isNotNull()
            .extracting(CollectionWithPagingInfo::getCollection)
                .isNotNull()
            .extracting(Collection::isEmpty)
                .isEqualTo(true);
    }

    @Test
    public void testCreatePageForNullPaging()
    {
        final List<Object> list = createListOf(18, Object.class);

        // when
        final CollectionWithPagingInfo<Object> page = ListPage.of(list, null);

        assertThat(page)
            .isNotNull()
            .extracting(CollectionWithPagingInfo::getCollection)
            .isNotNull()
            .extracting(Collection::size)
            .isEqualTo(18);
    }

    private static <T> List<T> randomListOf(final int minSize, final int maxSize, final Class<T> clazz) {
        return createListOf(new Random().nextInt((maxSize - minSize) + 1) + minSize, clazz);
    }

    private static <T> List<T> createListOf(final int size, final Class<T> clazz) {
        final List<T> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(mock(clazz));
        }
        Collections.shuffle(list);

        return list;
    }

    private static final class PageFormat
    {
        private final int size;
        private final int offset;

        public PageFormat(int size, int offset)
        {
            this.size = size;
            this.offset = offset;
        }
    }
}