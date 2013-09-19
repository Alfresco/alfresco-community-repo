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
package org.alfresco.service.cmr.workflow;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.namespace.QName;

@AlfrescoPublicApi
public class WorkflowInstanceQuery
{
    public enum DatePosition
    {
        BEFORE, AFTER
    }

    private String workflowDefinitionId;
    private Boolean active = null;
    private Date startBefore;
    private Date startAfter;
    private Date endBefore;
    private Date endAfter;
    private List<String> excludedDefinitions;

    // Custom properties
    // Value for Date property must be Map<DatePosition, Date>
    private Map<QName, Object> customProps;

    public WorkflowInstanceQuery()
    {
        // Default
    }

    public WorkflowInstanceQuery(String workflowDefinitionId)
    {
        this.workflowDefinitionId = workflowDefinitionId;
    }

    public WorkflowInstanceQuery(Boolean active)
    {
        this.active = active;
    }

    public WorkflowInstanceQuery(String workflowDefinitionId, Boolean active)
    {
        this.workflowDefinitionId = workflowDefinitionId;
        this.active = active;
    }

    public Boolean getActive()
    {
        return active;
    }

    public void setActive(Boolean active)
    {
        this.active = active;
    }

    public Date getStartBefore()
    {
        return startBefore;
    }

    public void setStartBefore(Date startBefore)
    {
        this.startBefore = startBefore;
    }

    public Date getStartAfter()
    {
        return startAfter;
    }

    public void setStartAfter(Date startAfter)
    {
        this.startAfter = startAfter;
    }

    public Date getEndBefore()
    {
        return endBefore;
    }

    public void setEndBefore(Date endBefore)
    {
        this.endBefore = endBefore;
    }

    public Date getEndAfter()
    {
        return endAfter;
    }

    public void setEndAfter(Date endAfter)
    {
        this.endAfter = endAfter;
    }

    public Map<QName, Object> getCustomProps()
    {
        return customProps;
    }

    public void setCustomProps(Map<QName, Object> customProps)
    {
        this.customProps = customProps;
    }

    public String getWorkflowDefinitionId()
    {
        return workflowDefinitionId;
    }

    public void setWorkflowDefinitionId(String workflowDefinitionId)
    {
        this.workflowDefinitionId = workflowDefinitionId;
    }

    public List<String> getExcludedDefinitions()
    {
        return excludedDefinitions;
    }

    public void setExcludedDefinitions(List<String> excludedDefinitions)
    {
        this.excludedDefinitions = excludedDefinitions;
    }

}
