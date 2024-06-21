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
 * retention schedule
 */
public class RetentionSchedule
{
    private String id ;
    private String parentId;
    private String authority;
    private String instructions;
    private boolean isRecordLevel;
    private boolean isUnpublishedUpdates;
    private List<RetentionScheduleActionDefinition> actions;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getParentId()
    {
        return parentId;
    }

    public void setParentId(String parentId)
    {
        this.parentId = parentId;
    }

    public String getAuthority()
    {
        return authority;
    }

    public void setAuthority(String authority)
    {
        this.authority = authority;
    }

    public String getInstructions()
    {
        return instructions;
    }

    public void setInstructions(String instructions)
    {
        this.instructions = instructions;
    }

    public boolean getIsRecordLevel()
    {
        return isRecordLevel;
    }

    public void setIsRecordLevel(boolean isRecordLevel)
    {
        this.isRecordLevel = isRecordLevel;
    }

    public boolean getIsUnpublishedUpdates()
    {
        return isUnpublishedUpdates;
    }

    public void setIsUnpublishedUpdates(boolean unpublishedUpdates)
    {
        this.isUnpublishedUpdates = unpublishedUpdates;
    }

    public List<RetentionScheduleActionDefinition> getActions()
    {
        return actions;
    }

    public void setActions(List<RetentionScheduleActionDefinition> actions)
    {
        this.actions = actions;
    }
}
