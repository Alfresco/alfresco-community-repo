/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rm.rest.api.model;

/**
 * Retention event values
 */
public enum RetentionEvents
{
    CASE_CLOSED("case_closed"),
    ABOLISHED("abolished"),
    RE_DESIGNATED("re_designated"),
    NO_LONGER_NEEDED("no_longer_needed"),
    SUPERSEDED("superseded"),
    VERSIONED("versioned"),
    STUDY_COMPLETE("study_complete"),
    TRAINING_COMPLETE("training_complete"),
    TRANSFERRED_INACTIVE_STORAGE("related_record_trasfered_inactive_storage"),
    OBSOLETE("obsolete"),
    ALLOWANCES_GRANTED_TERMINATED("all_allowances_granted_are_terminated"),
    WGI_ACTION_COMPLETE("WGI_action_complete"),
    SEPARATION("separation"),
    CASE_COMPLETE("case_complete"),
    DECLASSIFICATION_REVIEW("declassification_review");

    public final String eventName;

    RetentionEvents(String eventName)
    {
        this.eventName = eventName;
    }
}
