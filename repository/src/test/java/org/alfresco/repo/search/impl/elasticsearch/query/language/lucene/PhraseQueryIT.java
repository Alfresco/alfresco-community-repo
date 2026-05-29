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

import org.alfresco.repo.search.impl.elasticsearch.query.BasePhraseQueryIT;
import org.alfresco.service.cmr.repository.NodeRef;

public class PhraseQueryIT extends BasePhraseQueryIT
{
    @Override
    public void whenSearchUsingPhraseQuery()
    {
        assertContainsOnly(luceneSearch("\"yellow taxi\""), yellowTaxi);
        assertContainsOnly(luceneSearch("\"taxi yellow\""), taxiYellow);
    }

    @Override
    public void whenSearchUsingPhraseProximityQuery()
    {
        assertZeroResults(luceneSearch("\"test yellow\""));
        assertZeroResults(luceneSearch("\"test yellow\"~0"));
        assertZeroResults(luceneSearch("\"test yellow\"~1"));
        assertContainsOnly(luceneSearch("\"test yellow\"~2"), taxiYellow);
        assertContainsOnly(luceneSearch("\"test yellow\"~3"), taxiYellow, yellowTaxi);
        assertContainsOnly(luceneSearch("\"test yellow\"~4"), taxiYellow, yellowTaxi);
    }

    @Override
    public void whenSearchUsingPhraseProximityQueryOnASpecificField()
    {
        final String nameQuery = String.format("@cm\\:name:\"%s %s\"", proximityTestNameFirstPart, proximityTestNameLastPart);

        assertZeroResults(luceneSearch(nameQuery));
        assertZeroResults(luceneSearch(nameQuery + "~0"));
        assertContainsOnly(luceneSearch(nameQuery + "~1"), proximityTestFirst1Last);
        assertContainsOnly(luceneSearch(nameQuery + "~2"), proximityTestFirst1Last, proximityTestFirst2Last);
        assertContainsOnly(luceneSearch(nameQuery + "~3"), proximityTestFirst1Last, proximityTestFirst2Last);
    }

    @Override
    public void whenSearchPhraseUsingBooleanOperators()
    {
        /* d:content uses default Elasticsearch english text analysis with stopwords, 'a' is a stopword so it disappears from the query after text analysis: "just a test" -> "just ? test" */
        assertContainsOnly(luceneSearch("\"just a test\" OR \"yellow banana\""), test, bigYellowBanana, anotherTest);
        assertContainsOnly(luceneSearch("NOT(\"just a test\")"), bigYellowBanana, yellowTaxi, taxiYellow, proximityTestFirst1Last, proximityTestFirst2Last);
        assertContainsOnly(luceneSearch("NOT \"just a test\""), bigYellowBanana, yellowTaxi, taxiYellow, proximityTestFirst1Last, proximityTestFirst2Last);
        assertContainsOnly(luceneSearch("NOT(\"just test\")"), test, bigYellowBanana, yellowTaxi, taxiYellow, anotherTest, proximityTestFirst1Last, proximityTestFirst2Last);

        NodeRef yellowTaxiLondon = indexDocument("test another yellow taxi london");
        assertContainsOnly(luceneSearch("\"yellow taxi\" AND test"), yellowTaxi, yellowTaxiLondon);
        assertContainsOnly(luceneSearch("\"yellow taxi\" AND \"test another\""), yellowTaxiLondon);
    }

    @Override
    public void whenSearchUsingPhraseQueryOnASpecificField()
    {
        NodeRef documentTwo = indexDocument("document name two", "content");
        assertContainsOnly(luceneSearch("@cm\\:\\name:\"document name\""), yellowTaxi, documentTwo);
        assertContainsOnly(luceneSearch("@cm\\:name:\"name document\""), taxiYellow);
    }

    @Override
    public void whenSearchUsingPhraseQueryOnASpecificFieldUsingBooleanOperators()
    {
        NodeRef documentTwo = indexDocument("document name two", "content");
        assertContainsOnly(luceneSearch("@cm\\:name:\"document name\" AND content"), documentTwo);
        assertContainsOnly(luceneSearch("@cm\\:name:\"document name\" AND NOT content"), yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:\"document name\" OR banana"), documentTwo, yellowTaxi, bigYellowBanana);
    }
}
