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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.alfresco.repo.search.impl.elasticsearch.query.BaseDateMathQueryIT;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchParameters;

public class DateMathQueryIT extends BaseDateMathQueryIT
{

    @Override
    public void whenSearchFromYesterday()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setLanguage("afts");
        searchParams.setQuery("cm:modified:[NOW-1DAY/DAY TO NOW/DAY]");
        assertContainsOnly(searchFor(searchParams), yesterday, now);

    }

    @Override
    public void whenSearchYesterday()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setLanguage("afts");
        searchParams.setQuery("cm:modified:<NOW-2DAY/DAY TO NOW-1DAY/DAY]");
        assertContainsOnly(searchFor(searchParams), yesterday);
    }

    @Override
    public void whenSearchLastSixMonths()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setLanguage("afts");
        // last six months before yesterday
        searchParams.setQuery("cm:modified:[NOW-6MONTH/DAY TO NOW-1DAY/DAY>");
        assertContainsOnly(searchFor(searchParams), sixMonthsAgo, oneMonthAgo, oneWeekAgo, twoDaysAgo);

    }

    @Override
    public void whenSearchPreviousSixMonths()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setLanguage("afts");
        // six months starting one year ago: note the
        searchParams.setQuery("cm:modified:[NOW-1YEAR/DAY TO NOW-6MONTH/DAY>");
        assertContainsOnly(searchFor(searchParams), oneYearAgo);

    }

    @Override
    public void whenSearchWithTimezone()
    {
        NodeRef eightHoursForward = indexDocument("eightHoursForward", "eightHoursForward", Date.from(LocalDateTime.now().plusHours(8).toInstant(ZoneOffset.UTC)));

        SearchParameters searchParams = new SearchParameters();
        searchParams.setLanguage("afts");
        searchParams.setQuery("cm:modified:[NOW-12HOUR TO NOW+10HOUR]");
        assertContainsOnly(searchFor(searchParams), eightHoursForward, now);

        // switching to melbourne
        searchParams = new SearchParameters();
        searchParams.setLanguage("afts");
        searchParams.setTimezone("Australia/Melbourne");
        searchParams.setQuery("cm:modified:[NOW TO NOW+1HOUR]");
        assertZeroResults(searchFor(searchParams));

    }

}
