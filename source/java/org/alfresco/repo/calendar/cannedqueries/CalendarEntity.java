package org.alfresco.repo.calendar.cannedqueries;

import org.alfresco.repo.query.NodeBackedEntity;
import org.alfresco.service.cmr.calendar.CalendarEntry;

/**
 * Calendar Entity - low level representation of parts of a 
 *  {@link CalendarEntry} - used by GetCalendarEntries Canned Query
 *
 * @author Nick Burch
 * @since 4.0
 */
public class CalendarEntity extends NodeBackedEntity
{
    private String fromDate;
    private String toDate;
    private String recurrenceRule;
    private String recurrenceLastMeeting;
    
    // Supplemental query-related parameters
    private Long fromDateQNameId;
    private Long toDateQNameId;
    private Long recurrenceRuleQNameId;
    private Long recurrenceLastMeetingQNameId;
    
    /**
     * Default constructor
     */
    public CalendarEntity()
    {
       super();
    }
    
    public CalendarEntity(Long parentNodeId, Long nameQNameId, Long contentTypeQNameId,
                          Long fromDateQNameId, Long toDateQNameId,
                          Long recurrenceRuleQNameId, Long recurrenceLastMeetingQNameId)
    {
        super(parentNodeId, nameQNameId, contentTypeQNameId);
        this.fromDateQNameId = fromDateQNameId;
        this.toDateQNameId = toDateQNameId;
        this.recurrenceRuleQNameId = recurrenceRuleQNameId;
        this.recurrenceLastMeetingQNameId = recurrenceLastMeetingQNameId;
    }
    
    // (ISO-8061)
    public String getFromDate()
    {
        return fromDate;
    }
    
    public void setFromDate(String fromISO8601)
    {
        this.fromDate = fromISO8601;
    }
    
    // (ISO-8061)
    public String getToDate()
    {
        return toDate;
    }
    
    public void setToDate(String toISO8061)
    {
        this.toDate = toISO8061;
    }
    
    /**
     * SharePoint/Oulook rules string
     */
    public String getRecurrenceRule() 
    {
       return recurrenceRule;
    }

    /**
     * SharePoint/Oulook rules string
     */
    public void setRecurrenceRule(String recurrenceRule) 
    {
       this.recurrenceRule = recurrenceRule;
    }

    // (ISO-8061)
    public String getRecurrenceLastMeeting() 
    {
       return recurrenceLastMeeting;
    }

    public void setRecurrenceLastMeeting(String recurrenceLastMeetingISO8601) 
    {
       this.recurrenceLastMeeting = recurrenceLastMeetingISO8601;
    }
    
    
    // Supplemental query-related parameters
    
    public Long getFromDateQNameId() 
    {
       return fromDateQNameId;
    }

    public Long getToDateQNameId() 
    {
       return toDateQNameId;
    }

    public Long getRecurrenceRuleQNameId() 
    {
       return recurrenceRuleQNameId;
    }

    public Long getRecurrenceLastMeetingQNameId() 
    {
       return recurrenceLastMeetingQNameId;
    }
}
