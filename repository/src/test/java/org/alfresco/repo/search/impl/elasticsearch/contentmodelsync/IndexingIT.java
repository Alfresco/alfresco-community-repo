/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import org.alfresco.repo.search.impl.elasticsearch.query.ElasticsearchBaseQueryIT;

public class IndexingIT extends ElasticsearchBaseQueryIT
{
    /**
     * This test has no assertion because the behaviour of the Elasticsearch REST client. The index API throws an exception in case of any failure related with a given index request. For that reason in this test (see the linked issue below) we don't expect any indexing issue: the indexDocument(...) call should silently return after indexing the document.
     *
     * @see <a href="https://alfresco.atlassian.net/browse/SEARCH-2860">SEARCH-2860</a>
     */
    @Test
    @SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
    public void shouldIndexLongTextAsTokenized() throws IOException
    {
        File file = ResourceUtils.getFile("classpath:search/longtext.txt");
        String text = FileUtils.readFileToString(file, "UTF-8");
        indexDocument("longtext.txt", text);
    }
}
