/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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

import java.util.List;

/**
 * @author sathishkumar.t
 */
public class RetentionScheduleActionDefinition
{
    private String id;
    private String name;
    private int periodAmount;
    private String period;
    private String periodProperty;
    private boolean combineDispositionStepConditions;
    private List<String> events;
    private boolean eligibleOnFirstCompleteEvent;
    private String description;
    private boolean retainRecordMetadataAfterDestruction;
    private String location;
    private int index;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getPeriodAmount()
    {
        return periodAmount;
    }

    public void setPeriodAmount(int periodAmount)
    {
        this.periodAmount = periodAmount;
    }

    public String getPeriod()
    {
        return period;
    }

    public void setPeriod(String period)
    {
        this.period = period;
    }

    public String getPeriodProperty()
    {
        return periodProperty;
    }

    public void setPeriodProperty(String periodProperty)
    {
        this.periodProperty = periodProperty;
    }

    public boolean getCombineDispositionStepConditions()
    {
        return combineDispositionStepConditions;
    }

    public void setCombineDispositionStepConditions(boolean combineDispositionStepConditions)
    {
        this.combineDispositionStepConditions = combineDispositionStepConditions;
    }

    public List<String> getEvents()
    {
        return events;
    }

    public void setEvents(List<String> events)
    {
        this.events = events;
    }

    public boolean getEligibleOnFirstCompleteEvent()
    {
        return eligibleOnFirstCompleteEvent;
    }

    public void setEligibleOnFirstCompleteEvent(boolean eligibleOnFirstCompleteEvent)
    {
        this.eligibleOnFirstCompleteEvent = eligibleOnFirstCompleteEvent;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public boolean getRetainRecordMetadataAfterDestruction() {
        return retainRecordMetadataAfterDestruction;
    }

    public void setRetainRecordMetadataAfterDestruction(boolean retainRecordMetadataAfterDestruction) {
        this.retainRecordMetadataAfterDestruction = retainRecordMetadataAfterDestruction;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }
}