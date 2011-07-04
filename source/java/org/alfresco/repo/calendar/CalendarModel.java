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
package org.alfresco.repo.calendar;

import org.alfresco.service.namespace.QName;

/**
 * Calendar models constants
 * 
 * @author Nick Burch
 */
public interface CalendarModel
{
    /** Calendar Model */
    public static final String CALENDAR_MODEL_URL = "http://www.alfresco.org/model/calendar";
    public static final String CALENDAR_MODEL_PREFIX = "ia";
    
    /** Event */
    public static final QName TYPE_EVENT = QName.createQName(CALENDAR_MODEL_URL, "calendarEvent"); 
    public static final QName PROP_WHAT = QName.createQName(CALENDAR_MODEL_URL, "whatEvent"); 
    public static final QName PROP_FROM_DATE = QName.createQName(CALENDAR_MODEL_URL, "fromDate"); 
    public static final QName PROP_TO_DATE = QName.createQName(CALENDAR_MODEL_URL, "toDate"); 
    public static final QName PROP_WHERE = QName.createQName(CALENDAR_MODEL_URL, "whereEvent"); 
    public static final QName PROP_DESCRIPTION = QName.createQName(CALENDAR_MODEL_URL, "descriptionEvent"); 
    public static final QName PROP_COLOR = QName.createQName(CALENDAR_MODEL_URL, "colorEvent"); 
    public static final QName PROP_RECURRENCE_RULE = QName.createQName(CALENDAR_MODEL_URL, "recurrenceRule"); 
    public static final QName PROP_RECURRENCE_LAST_MEETING = QName.createQName(CALENDAR_MODEL_URL, "recurrenceLastMeeting"); 
    public static final QName PROP_IS_OUTLOOK = QName.createQName(CALENDAR_MODEL_URL, "isOutlook"); 
    public static final QName PROP_OUTLOOK_UID = QName.createQName(CALENDAR_MODEL_URL, "outlookUID");
    public static final QName ASSOC_IGNORE_EVENT_LIST = QName.createQName(CALENDAR_MODEL_URL, "ignoreEventList");
    
    /** Ignored Event */
    public static final QName TYPE_IGNORE_EVENT = QName.createQName(CALENDAR_MODEL_URL, "ignoreEvent");
    public static final QName PROP_IGNORE_EVENT_DATE = QName.createQName(CALENDAR_MODEL_URL, "date");
}