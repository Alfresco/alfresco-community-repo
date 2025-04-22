/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.calendar.cannedqueries;

import java.util.Date;

/**
 * Parameter objects for {@link GetCalendarEntriesCannedQuery}.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class GetCalendarEntriesCannedQueryParams extends CalendarEntity
{
    private final Long[] sitesContainerNodeIds;
    private final Date entriesFromDate;
    private final Date entriesToDate;

    public GetCalendarEntriesCannedQueryParams(Long[] sitesContainerNodeIds,
            Long nameQNameId,
            Long contentTypeQNameId,
            Long fromDateQNameId,
            Long toDateQNameId,
            Long recurrenceRuleQNameId,
            Long recurrenceLastMeetingQNameId,
            Date entriesFromDate,
            Date entriesToDate)
    {
        super(null, nameQNameId, contentTypeQNameId, fromDateQNameId,
                toDateQNameId, recurrenceRuleQNameId, recurrenceLastMeetingQNameId);

        this.sitesContainerNodeIds = sitesContainerNodeIds;
        this.entriesFromDate = entriesFromDate;
        this.entriesToDate = entriesToDate;
    }

    public Long[] getSitesContainerNodeIds()
    {
        return sitesContainerNodeIds;
    }

    public Date getEntriesFromDate()
    {
        return entriesFromDate;
    }

    public Date getEntriesToDate()
    {
        return entriesToDate;
    }
}
