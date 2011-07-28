/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
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
    private Long parentNodeId;
    private Long nameQNameId;
    private Long contentTypeQNameId;
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
        super();
        this.parentNodeId = parentNodeId;
        this.nameQNameId = nameQNameId;
        this.contentTypeQNameId = contentTypeQNameId;
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
    
    public Long getParentNodeId()
    {
        return parentNodeId;
    }
    
    public Long getNameQNameId()
    {
        return nameQNameId;
    }

    public Long getContentTypeQNameId() 
    {
       return contentTypeQNameId;
    }

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
