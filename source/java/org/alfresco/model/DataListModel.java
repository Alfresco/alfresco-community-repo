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
package org.alfresco.model;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Data List Model Constants
 * 
 * @author Nick Burch
 * @since 4.0.2
 */
public interface DataListModel
{
    /** DataList Model */
    static final String DATALIST_MODEL_1_0_URI = NamespaceService.DATALIST_MODEL_1_0_URI;
    static final String DATALIST_MODEL_PREFIX = NamespaceService.DATALIST_MODEL_PREFIX;
    
    /** Data List */
    static final QName TYPE_DATALIST = QName.createQName(DATALIST_MODEL_1_0_URI, "dataList");
    static final QName PROP_DATALIST_ITEM_TYPE = QName.createQName(DATALIST_MODEL_1_0_URI, "dataListItemType");
    
    /** Parent of all Data List Items */
    static final QName TYPE_DATALIST_ITEM = QName.createQName(DATALIST_MODEL_1_0_URI, "dataListItem");
    
    /** Simple ToDo List */
    static final QName TYPE_TODO_LIST = QName.createQName(DATALIST_MODEL_1_0_URI, "todoList");
    static final QName PROP_TODO_TITLE = QName.createQName(DATALIST_MODEL_1_0_URI, "todoTitle");
    static final QName PROP_TODO_DUED_ATE = QName.createQName(DATALIST_MODEL_1_0_URI, "todoDueDate");
    static final QName PROP_TODO_PRIORITY = QName.createQName(DATALIST_MODEL_1_0_URI, "todoPriority");
    static final QName PROP_TODO_STATUS = QName.createQName(DATALIST_MODEL_1_0_URI, "todoStatus");
    static final QName PROP_TODO_NOTES = QName.createQName(DATALIST_MODEL_1_0_URI, "todoNotes");
    
    /** Simple Task List */
    static final QName TYPE_SIMPLE_TASK = QName.createQName(DATALIST_MODEL_1_0_URI, "simpletask");
    static final QName PROP_SIMPLE_TASK_DUE_DATE = QName.createQName(DATALIST_MODEL_1_0_URI, "simpletaskDueDate");
    static final QName PROP_SIMPLE_TASK_PRIORITY = QName.createQName(DATALIST_MODEL_1_0_URI, "simpletaskPriority");
    static final QName PROP_SIMPLE_TASK_STATUS = QName.createQName(DATALIST_MODEL_1_0_URI, "simpletaskStatus");
    static final QName PROP_SIMPLE_TASK_COMMENTS = QName.createQName(DATALIST_MODEL_1_0_URI, "simpletaskComments");
    
    /** Advanced Task List */
    static final QName TYPE_TASK = QName.createQName(DATALIST_MODEL_1_0_URI, "task");
    static final QName PROP_TASK_PRIORITY = QName.createQName(DATALIST_MODEL_1_0_URI, "taskPriority");
    static final QName PROP_TASK_STATUS = QName.createQName(DATALIST_MODEL_1_0_URI, "taskStatus");
    static final QName PROP_TASK_COMMENTS = QName.createQName(DATALIST_MODEL_1_0_URI, "taskComments");
    
    /** GANTT related parts of an Advanced Task */
    static final QName ASPECT_GANTT = QName.createQName(DATALIST_MODEL_1_0_URI, "gantt");
    static final QName PROP_GANTT_START_DATE = QName.createQName(DATALIST_MODEL_1_0_URI, "ganttStartDate");
    static final QName PROP_GANTT_END_DATE = QName.createQName(DATALIST_MODEL_1_0_URI, "ganttEndDate");
    static final QName PROP_GANTT_PERCENT_COMPLETE = QName.createQName(DATALIST_MODEL_1_0_URI, "ganttPercentComplete");
    
    /** Contact List */
    static final QName TYPE_CONTACT = QName.createQName(DATALIST_MODEL_1_0_URI, "contact");
    static final QName PROP_CONTACT_FIRSTNAME = QName.createQName(DATALIST_MODEL_1_0_URI, "contactFirstName");
    static final QName PROP_CONTACT_LASTNAME = QName.createQName(DATALIST_MODEL_1_0_URI, "contactLastName");
    static final QName PROP_CONTACT_EMAIL = QName.createQName(DATALIST_MODEL_1_0_URI, "contactEmail");
    static final QName PROP_CONTACT_COMPANY = QName.createQName(DATALIST_MODEL_1_0_URI, "contactCompany");
    static final QName PROP_CONTACT_JOBTITLE = QName.createQName(DATALIST_MODEL_1_0_URI, "contactJobTitle");
    static final QName PROP_CONTACT_PHONE_OFFICE = QName.createQName(DATALIST_MODEL_1_0_URI, "contactPhoneOffice");
    static final QName PROP_CONTACT_PHONE_MOBILE = QName.createQName(DATALIST_MODEL_1_0_URI, "contactPhoneMobile");
    static final QName PROP_CONTACT_NOTES = QName.createQName(DATALIST_MODEL_1_0_URI, "contactNotes");
    
    /** Issue List */
    static final QName TYPE_ISSUE = QName.createQName(DATALIST_MODEL_1_0_URI, "issue");
    static final QName PROP_ISSUE_ID = QName.createQName(DATALIST_MODEL_1_0_URI, "issueID");
    static final QName PROP_ISSUE_STATUS = QName.createQName(DATALIST_MODEL_1_0_URI, "issueStatus");
    static final QName PROP_ISSUE_PRIORITY = QName.createQName(DATALIST_MODEL_1_0_URI, "issuePriority");
    static final QName PROP_ISSUE_DUE_DATE = QName.createQName(DATALIST_MODEL_1_0_URI, "issueDueDate");
    static final QName PROP_ISSUE_COMMENTS = QName.createQName(DATALIST_MODEL_1_0_URI, "issueComments");
    
    /** Event List */
    static final QName TYPE_EVENT = QName.createQName(DATALIST_MODEL_1_0_URI, "event");
    static final QName PROP_EVENT_LOCATION = QName.createQName(DATALIST_MODEL_1_0_URI, "eventLocation");
    static final QName PROP_EVENT_NOTE = QName.createQName(DATALIST_MODEL_1_0_URI, "eventNote");
    static final QName PROP_EVENT_START_DATE = QName.createQName(DATALIST_MODEL_1_0_URI, "eventStartDate");
    static final QName PROP_EVENT_END_DATE = QName.createQName(DATALIST_MODEL_1_0_URI, "eventEndDate");
    static final QName PROP_EVENT_REGISTRATIONS = QName.createQName(DATALIST_MODEL_1_0_URI, "eventRegistrations");
    
    /** Location List */
    static final QName TYPE_LOCATION = QName.createQName(DATALIST_MODEL_1_0_URI, "location");
    static final QName PROP_LOCATION_ADDRESS1 = QName.createQName(DATALIST_MODEL_1_0_URI, "locationAddress1");
    static final QName PROP_LOCATION_ADDRESS2 = QName.createQName(DATALIST_MODEL_1_0_URI, "locationAddress2");
    static final QName PROP_LOCATION_ADDRESS3 = QName.createQName(DATALIST_MODEL_1_0_URI, "locationAddress3");
    static final QName PROP_LOCATION_ZIP = QName.createQName(DATALIST_MODEL_1_0_URI, "locationZip");
    static final QName PROP_LOCATION_STATE = QName.createQName(DATALIST_MODEL_1_0_URI, "locationState");
    static final QName PROP_LOCATION_COUNTRY = QName.createQName(DATALIST_MODEL_1_0_URI, "locationCountry");
    
    /** Meeting Agenda List */
    static final QName TYPE_MEETING_AGENDA = QName.createQName(DATALIST_MODEL_1_0_URI, "meetingAgenda");
    static final QName PROP_MEETING_AGENDA_REF = QName.createQName(DATALIST_MODEL_1_0_URI, "meetingAgendaRef");
    static final QName PROP_MEETING_AGENDA_TIME = QName.createQName(DATALIST_MODEL_1_0_URI, "meetingAgendaTime");
    static final QName PROP_MEETING_AGENDA_OWNER = QName.createQName(DATALIST_MODEL_1_0_URI, "meetingAgendaOwner");
    
    /** Event Agenda List */
    static final QName TYPE_EVENT_AGENDA = QName.createQName(DATALIST_MODEL_1_0_URI, "eventAgenda");
    static final QName PROP_EVENT_AGENDA_REF = QName.createQName(DATALIST_MODEL_1_0_URI, "eventAgendaRef");
    static final QName PROP_EVENT_AGENDA_START_TIME = QName.createQName(DATALIST_MODEL_1_0_URI, "eventAgendaStartTime");
    static final QName PROP_EVENT_AGENDA_END_TIME = QName.createQName(DATALIST_MODEL_1_0_URI, "eventAgendaEndTime");
    static final QName PROP_EVENT_AGENDA_SESSION_NAME = QName.createQName(DATALIST_MODEL_1_0_URI, "eventAgendaSessionName");
    static final QName PROP_EVENT_AGENDA_PRESENTER = QName.createQName(DATALIST_MODEL_1_0_URI, "eventAgendaPresenter");
    static final QName PROP_EVENT_AGENDA_AUDIENCE = QName.createQName(DATALIST_MODEL_1_0_URI, "eventAgendaAudience");
    static final QName PROP_EVENT_AGENDA_NOTES = QName.createQName(DATALIST_MODEL_1_0_URI, "eventAgendaNotes");
}
