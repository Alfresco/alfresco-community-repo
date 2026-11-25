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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.junit.After;
import org.junit.Test;
import org.opensearch.client.opensearch._types.query_dsl.Query;

public class QueryConverterTest
{
    @After
    public void resetExactMatchFields()
    {
        QueryConverter.setExactMatchFields("");
    }

    @Test
    public void shouldConvertConfiguredExactMatchFieldToTermQuery()
    {
        QueryConverter.setExactMatchFields("TAG");

        Query query = QueryConverter.fromLuceneToElasticsearch(new TermQuery(new Term("TAG", "file jpg")));

        assertThat(query.isTerm(), is(true));
        assertThat(query.term().field(), is("TAG"));
        assertThat(query.term().value().stringValue(), is("file jpg"));
    }

    @Test
    public void shouldKeepUnconfiguredTermQueryAsQueryString()
    {
        QueryConverter.setExactMatchFields("TAG");

        Query query = QueryConverter.fromLuceneToElasticsearch(new TermQuery(new Term("cm:name", "file jpg")));

        assertThat(query.isQueryString(), is(true));
    }

    @Test
    public void shouldEncodeConfiguredNamespacedField()
    {
        QueryConverter.setExactMatchFields("cm:name");

        Query query = QueryConverter.fromLuceneToElasticsearch(new TermQuery(new Term("cm%3Aname", "file jpg")));

        assertThat(query.isTerm(), is(true));
        assertThat(query.term().field(), is("cm%3Aname"));
    }
}
