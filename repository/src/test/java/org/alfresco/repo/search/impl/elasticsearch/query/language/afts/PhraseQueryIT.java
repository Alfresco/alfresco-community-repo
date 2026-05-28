/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

package org.alfresco.repo.search.impl.elasticsearch.query.language.afts;

import org.alfresco.repo.search.impl.elasticsearch.query.BasePhraseQueryIT;
import org.alfresco.service.cmr.repository.NodeRef;

public class PhraseQueryIT extends BasePhraseQueryIT
{
    @Override
    public void whenSearchUsingPhraseQuery()
    {
        assertContainsOnly(aftsSearch("\"yellow taxi\""), yellowTaxi);
        assertContainsOnly(aftsSearch("\"taxi yellow\""), taxiYellow);
    }

    @Override
    public void whenSearchUsingPhraseProximityQuery()
    {
        assertZeroResults(aftsSearch("\"test yellow\""));
        assertZeroResults(aftsSearch("test *(0) yellow"));
        assertZeroResults(aftsSearch("test *(1) yellow"));
        assertContainsOnly(aftsSearch("test *(2) yellow"), taxiYellow);
        assertContainsOnly(aftsSearch("test *(3) yellow"), taxiYellow, yellowTaxi);
        assertContainsOnly(aftsSearch("test *(4) yellow"), taxiYellow, yellowTaxi);
        assertContainsOnly(aftsSearch("test * yellow"), taxiYellow, yellowTaxi);
    }

    @Override
    public void whenSearchUsingPhraseProximityQueryOnASpecificField()
    {
        final String nameQuery = String.format("cm:name:(%s %%s %s)", proximityTestNameFirstPart, proximityTestNameLastPart);

        assertZeroResults(aftsSearch(String.format(nameQuery, "*(0)")));
        assertContainsOnly(aftsSearch(String.format(nameQuery, "*(1)")), proximityTestFirst1Last);
        assertContainsOnly(aftsSearch(String.format(nameQuery, "*(2)")), proximityTestFirst1Last, proximityTestFirst2Last);
        assertContainsOnly(aftsSearch(String.format(nameQuery, "*(3)")), proximityTestFirst1Last, proximityTestFirst2Last);
        assertContainsOnly(aftsSearch(String.format(nameQuery, "*")), proximityTestFirst1Last, proximityTestFirst2Last);
    }

    @Override
    public void whenSearchPhraseUsingBooleanOperators()
    {
        /* d:content uses default Elasticsearch english text analysis with stopwords, 'a' is a stopword so it disappears from the query after text analysis: "just a test" -> "just ? test" */
        assertContainsOnly(aftsSearch("\"just a test\" OR \"yellow banana\""), test, bigYellowBanana, anotherTest);
        assertContainsOnly(aftsSearch("NOT(\"just a test\")"), bigYellowBanana, yellowTaxi, taxiYellow, proximityTestFirst1Last, proximityTestFirst2Last);
        assertContainsOnly(aftsSearch("NOT \"just a test\""), bigYellowBanana, yellowTaxi, taxiYellow, proximityTestFirst1Last, proximityTestFirst2Last);
        assertContainsOnly(aftsSearch("!\"just a test\""), bigYellowBanana, yellowTaxi, taxiYellow, proximityTestFirst1Last, proximityTestFirst2Last);
        assertContainsOnly(aftsSearch("NOT(\"just test\")"), test, bigYellowBanana, yellowTaxi, taxiYellow, anotherTest, proximityTestFirst1Last, proximityTestFirst2Last);
        NodeRef yellowTaxiLondon = indexDocument("test another yellow taxi london");
        // this document is not returned with a 0 slop phrase query, because there is 'red' between 'yellow' and 'taxi'
        NodeRef yellowRedTaxi = indexDocument("yellow red taxi test another");
        assertContainsOnly(aftsSearch("\"yellow taxi\" AND test"), yellowTaxi, yellowTaxiLondon);
        assertContainsOnly(aftsSearch("\"yellow taxi\" AND \"test another\""), yellowTaxiLondon);
    }

    @Override
    public void whenSearchUsingPhraseQueryOnASpecificField()
    {
        NodeRef documentTwo = indexDocument("document name two", "content");
        assertContainsOnly(aftsSearch("cm:name:\"document name\""), yellowTaxi, documentTwo);
        assertContainsOnly(aftsSearch("cm:name:\"name document\""), taxiYellow);
    }

    @Override
    public void whenSearchUsingPhraseQueryOnASpecificFieldUsingBooleanOperators()
    {
        NodeRef documentTwo = indexDocument("document name two", "content");
        assertContainsOnly(aftsSearch("cm:name:\"document name\" AND content"), documentTwo);
        assertContainsOnly(aftsSearch("cm:name:\"document name\" AND NOT content"), yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:\"document name\" OR banana"), documentTwo, yellowTaxi, bigYellowBanana);
    }
}
