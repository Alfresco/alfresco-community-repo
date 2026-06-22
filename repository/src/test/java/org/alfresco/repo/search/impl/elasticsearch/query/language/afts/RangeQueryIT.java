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
package org.alfresco.repo.search.impl.elasticsearch.query.language.afts;

import org.alfresco.repo.search.impl.elasticsearch.query.BaseRangeQueryIT;
import org.alfresco.service.cmr.search.SearchParameters;

public class RangeQueryIT extends BaseRangeQueryIT
{

    @Override
    public void whenSearchUsingDateRange()
    {
        assertContainsOnly(aftsSearch("cm:modified:['2020-05-11T00:00:00' TO '2020-05-12T23:59:59']"), testDates);
        assertContainsOnly(aftsSearch("cm:modified:'2020-05-11T00:00:00'..'2020-05-12T23:59:59'"), testDates);
        assertContainsOnly(aftsSearch("cm:modified:<'2020-05-11T00:00:00' TO '2020-05-12T23:59:59']"), testDates[1], testDates[2], testDates[3]);
        assertContainsOnly(aftsSearch("cm:modified:<'2020-05-11T00:00:00Z' TO '2020-05-12T23:59:59Z']"), testDates[1], testDates[2], testDates[3]);
        assertContainsOnly(aftsSearch("cm:modified:['2020-05-11T00:00:00' TO '2020-05-12T23:59:59'>"), testDates[0], testDates[1], testDates[2]);
        assertContainsOnly(aftsSearch("cm:modified:<'2020-05-11T00:00:00' TO '2020-05-12T23:59:59'>"), testDates[1], testDates[2]);
        // because other test data are in the index with a date in the future we need to filter also by name
        assertContainsOnly(aftsSearch("cm:modified:['2020-05-12T00:00:00' TO MAX] AND name:dateTest"), testDates[2], testDates[3], dateInNextCentury);
        assertContainsOnly(aftsSearch("cm:modified:[MIN TO '2020-05-11T23:59:59']"), dateBefore1970, testDates[0], testDates[1]);
    }

    @Override
    public void whenSearchUsingTimezone()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setLanguage("afts");
        searchParams.setQuery("cm:modified:['2020-05-11T04:00:00' TO '2020-05-11T04:00:00']");
        searchParams.setTimezone("Asia/Yerevan");
        // testDate[0]::"2020-05-11T00:00:00+00:00"
        assertContainsOnly(searchFor(searchParams), testDates[0]);
    }

    @Override
    public void whenSearchUsingTimezoneInQuery()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setLanguage("afts");
        searchParams.setQuery("cm:modified:['2020-05-11T04:00:00+04:00' TO '2020-05-11T04:00:00+04:00']");

        assertContainsOnly(searchFor(searchParams), testDates[0]);
    }

    @Override
    public void whenSearchUsingNumericRange()
    {
        assertContainsOnly(aftsSearch("cm:content.size:[100 to 1000]"), doc100, doc500, doc1000);
        assertContainsOnly(aftsSearch("cm:content.size:<100 to 1000]"), doc500, doc1000);
        assertContainsOnly(aftsSearch("cm:content.size:[100 to 1000>"), doc100, doc500);
        assertContainsOnly(aftsSearch("cm:content.size:<100 to 1000>"), doc500);
        assertContainsOnly(aftsSearch("cm:content.size:1..1000"), doc100, doc500, doc1000);
        assertContainsOnly(aftsSearch("cm:content.size:0..MAX"), doc100, doc500, doc1000, docMax);
        assertContainsOnly(aftsSearch("cm:content.size:MIN..500"), docMin, doc100, doc500);
        assertContainsOnly(aftsSearch("cm:content.size:[MIN to 1000]"), docMin, doc100, doc500, doc1000);
        assertContainsOnly(aftsSearch("cm:content.size:[MIN to MAX]"), docMin, doc100, doc500, doc1000, docMax);
    }

    @Override
    public void whenSearchUsingDecimalNumbersRange()
    {
        assertContainsOnly(aftsSearch("cm:ratingScore:[100.1 to 500.5]"), doc100, doc500);
        assertContainsOnly(aftsSearch("cm:ratingScore:<100.1 to 500.5]"), doc500);
        assertContainsOnly(aftsSearch("cm:ratingScore:[100.1 to 500.5>"), doc100);
        assertContainsOnly(aftsSearch("cm:content.size:<105.5 to 999.99>"), doc500);
        assertContainsOnly(aftsSearch("cm:ratingScore:100.1..500.5"), doc100, doc500);
        assertContainsOnly(aftsSearch("cm:content.size:10.5..MAX"), doc100, doc500, doc1000, docMax);
        assertContainsOnly(aftsSearch("cm:content.size:MIN..590.85"), docMin, doc100, doc500);
        assertContainsOnly(aftsSearch("cm:content.size:[MIN to 2050.22]"), docMin, doc100, doc500, doc1000);
        assertContainsOnly(aftsSearch("cm:content.size:[MIN to MAX]"), docMin, doc100, doc500, doc1000, docMax);
    }

    @Override
    public void whenSearchUsingPartialDate()
    {
        assertContainsOnly(aftsSearch("cm:modified:['1950' TO '2021']"), dateBefore1970, testDates[0], testDates[1], testDates[2], testDates[3]); // becomes 1950-01-01T00:00:00 TO 2021-01-01T23:59:59
        assertContainsOnly(aftsSearch("cm:modified:['2020-05' TO '2020-06']"), testDates); // becomes 2020-05-01T00:00:00 TO 2020-06-01T23:59:59
        assertContainsOnly(aftsSearch("cm:modified:['2020-05-11' TO '2020-05-11']"), testDates[0], testDates[1]); // becomes 2020-05-11T00:00:00 TO 2020-05-11T23:59:59
        assertContainsOnly(aftsSearch("cm:modified:['2020-05-11T01' TO '2020-05-11T01']"), testDates[1]); // becomes 2020-05-11T01:00:00 TO 2020-05-11T01:59:59
        assertContainsOnly(aftsSearch("cm:modified:['2020-05-11T01:00' TO '2020-05-11T01:59']"), testDates[1]); // becomes 2020-05-11T01:00:00 TO 2020-05-11T01:00:59
    }

}
