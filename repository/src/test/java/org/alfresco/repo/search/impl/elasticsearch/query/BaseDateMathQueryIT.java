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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.service.cmr.repository.NodeRef;

public abstract class BaseDateMathQueryIT extends ElasticsearchBaseQueryIT
{

    protected NodeRef yesterday;
    protected NodeRef oneYearAgo;
    protected NodeRef oneWeekAgo;
    protected NodeRef oneMonthAgo;
    protected NodeRef sixMonthsAgo;
    protected NodeRef now;
    protected NodeRef twoDaysAgo;

    @Before
    public void initDocuments()
    {

        now = indexDocument("today", "today");
        yesterday = indexDocument("yesterday", "yesterday", Date.from(LocalDateTime.now().minusDays(1).toInstant(ZoneOffset.UTC)));
        twoDaysAgo = indexDocument("twoDaysAgo", "twoDaysAgo", Date.from(LocalDateTime.now().minusDays(2).toInstant(ZoneOffset.UTC)));
        oneWeekAgo = indexDocument("oneWeekAgo", "oneWeekAgo", Date.from(LocalDateTime.now().minusWeeks(1).toInstant(ZoneOffset.UTC)));
        oneMonthAgo = indexDocument("oneMonthAgo", "oneMonthAgo", Date.from(LocalDateTime.now().minusMonths(1).toInstant(ZoneOffset.UTC)));
        sixMonthsAgo = indexDocument("sixMonthsAgo", "sixMonthsAgo", Date.from(LocalDateTime.now().minusMonths(6).toInstant(ZoneOffset.UTC)));
        oneYearAgo = indexDocument("oneYearAgo", "oneYearAgo", Date.from(LocalDateTime.now().minusYears(1).toInstant(ZoneOffset.UTC)));

    }

    @Test
    public abstract void whenSearchFromYesterday();

    @Test
    public abstract void whenSearchYesterday();

    @Test
    public abstract void whenSearchLastSixMonths();

    @Test
    public abstract void whenSearchPreviousSixMonths();

    @Test
    public abstract void whenSearchWithTimezone();

}
