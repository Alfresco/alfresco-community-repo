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
package org.alfresco.repo.search.impl.elasticsearch.query;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import org.alfresco.util.Pair;

public class ElasticsearchQueryHelperTest
{
    @Test
    public void whenQueryDoesNotContainSolrSyntax_shouldNotChange()
    {
        List<String> queries = List.of("cm:name:\"filename\"",
                "cm:name:filename",
                "cm:name:this AND cm:content:\"other\"");

        for (String query : queries)
        {
            Assert.assertEquals(query, ElasticsearchQueryHelper.cleanUpFilterQueries(query));
        }
    }

    @Test
    public void whenQueryContainsSolrSyntaxInsideAValidQuery_shouldBeIgnored()
    {
        List<String> queries = List.of("cm:name:\"{!lucene}filename\"",
                "cm:name:\"fil{e}na{m}e\"",
                "cm:name:\"fil{e}na{m}e\"",
                "}{{cm:name:\"fil{e}na{m}e\"");

        for (String query : queries)
        {
            Assert.assertEquals(query, ElasticsearchQueryHelper.cleanUpFilterQueries(query));
        }
    }

    @Test
    public void whenQueryContainsSolrSyntax_shouldBeRemoved()
    {
        Assert.assertEquals("cm:name:filename",
                ElasticsearchQueryHelper.cleanUpFilterQueries("{!afts tag=exclude}cm:name:filename"));
        Assert.assertEquals("cm:name:filename AND content:\"file content\"",
                ElasticsearchQueryHelper.cleanUpFilterQueries("{!afts tag=exclude}cm:name:filename AND content:\"file content\""));
    }

    @Test
    public void whenQueryContainNamespacePropertyAndValue_queryAndLabelAreEqual()
    {
        List<String> facetQueries = List.of(
                "@{namespace}property:value",
                "@{http://www.alfresco.org/model/content/1.0}created:[NOW/DAY-1DAY TO NOW/DAY+1DAY]",
                "@{http://www.alfresco.org/model/content/1.0}content.size:[0 TO 10240]",
                "@{http://www.alfresco.org/model/content/1.0}modified:[NOW/DAY-7DAYS TO NOW/DAY+1DAY]");

        for (String facetQuery : facetQueries)
        {
            Optional<Pair<String, String>> actual = ElasticsearchQueryHelper.extractFacetQueryAndLabel(facetQuery);
            Optional<Pair<String, String>> expected = Optional.of(new Pair<>(facetQuery, facetQuery));
            Assert.assertEquals(expected, actual);
        }
    }
}
