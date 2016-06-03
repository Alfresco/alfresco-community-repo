package org.alfresco.service.cmr.workflow;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Workflow Definition Deployment
 *  
 * @author davidc
 */
@AlfrescoPublicApi
public class WorkflowDeployment
{
    /** Workflow Definition */
    @Deprecated
    public WorkflowDefinition definition;

    /** Workflow Status */
    @Deprecated
    public String[] problems;

    public WorkflowDeployment()
    {
        // Default Constructor.
    }
    
    public WorkflowDeployment(WorkflowDefinition definition, String... problems)
    {
        this.definition = definition;
        this.problems = problems;
    }

    /**
     * @return the definition
     */
    public WorkflowDefinition getDefinition()
    {
        return definition;
    }

    /**
     * @return the problems
     */
    public String[] getProblems()
    {
        return problems;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "WorkflowDeployment[def=" + definition + ",problems=" + ((problems == null) ? 0 : problems.length) + "]";
    }
}
