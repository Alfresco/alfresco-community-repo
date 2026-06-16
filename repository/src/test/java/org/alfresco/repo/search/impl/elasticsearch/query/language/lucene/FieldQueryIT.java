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

package org.alfresco.repo.search.impl.elasticsearch.query.language.lucene;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.search.impl.elasticsearch.query.ElasticsearchBaseQueryIT;
import org.alfresco.service.cmr.repository.NodeRef;

@SuppressWarnings("PMD")
public class FieldQueryIT extends ElasticsearchBaseQueryIT
{
    protected NodeRef big_yellow_banana;
    protected NodeRef yellowTaxi;
    protected NodeRef banana_split;
    protected NodeRef just_a_test;
    protected NodeRef just_a_another_test;

    @Before
    public void initDocuments()
    {
        big_yellow_banana = indexDocument("big yellow banana");
        yellowTaxi = indexDocument("yellow taxi test another");
        banana_split = indexDocument("bigger banana split");
        just_a_test = indexDocument("just a test");
        just_a_another_test = indexDocument("just a another test");
    }

    @Test
    public void ID()
    {
        assertContainsOnly(luceneSearch("ID:" + QueryParser.escape(big_yellow_banana.toString())), big_yellow_banana);
    }
}
