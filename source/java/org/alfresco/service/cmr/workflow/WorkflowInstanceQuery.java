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
    private String engine;

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
    
    public void setEngineId(String engine) {
		this.engine = engine;
	}
    
    public String getEngineId() {
		return engine;
	}

}
