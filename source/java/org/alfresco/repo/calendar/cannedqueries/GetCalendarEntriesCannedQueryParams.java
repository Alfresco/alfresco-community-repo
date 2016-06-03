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
