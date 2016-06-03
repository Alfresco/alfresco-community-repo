
package org.alfresco.repo.workflow.activiti;

import org.alfresco.repo.workflow.WorkflowAuthorityManager;
import org.alfresco.repo.workflow.WorkflowNodeConverter;
import org.alfresco.repo.workflow.WorkflowPropertyHandlerRegistry;
import org.alfresco.repo.workflow.activiti.properties.ActivitiPropertyConverter;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public class ActivitiWorkflowManager
{
    private final ActivitiPropertyConverter propertyConverter;
    private final WorkflowNodeConverter nodeConverter;
    private final WorkflowPropertyHandlerRegistry handlerRegistry;
    private final ActivitiWorkflowEngine workflowEngine;
    private final WorkflowAuthorityManager workflowAuthorityManager;
    
    public ActivitiWorkflowManager(ActivitiWorkflowEngine workflowEngine,
            ActivitiPropertyConverter propertyConverter,
            WorkflowPropertyHandlerRegistry handlerRegistry, 
            WorkflowNodeConverter nodeConverter,
            WorkflowAuthorityManager workflowAuthorityManager)
    {
        this.workflowEngine = workflowEngine;
        this.propertyConverter = propertyConverter;
        this.handlerRegistry = handlerRegistry;
        this.nodeConverter = nodeConverter;
        this.workflowAuthorityManager = workflowAuthorityManager;
    }

    /**
     * @return the propertyConverter
     */
    public ActivitiPropertyConverter getPropertyConverter()
    {
        return propertyConverter;
    }

    /**
     * @return the nodeConverter
     */
    public WorkflowNodeConverter getNodeConverter()
    {
        return nodeConverter;
    }

    /**
     * @return the handlerRegistry
     */
    public WorkflowPropertyHandlerRegistry getPropertyHandlerRegistry()
    {
        return handlerRegistry;
    }

    /**
     * @return the workflowEngine
     */
    public ActivitiWorkflowEngine getWorkflowEngine()
    {
        return workflowEngine;
    }

    /**
     * @return the workflowAuthorityManager
     */
    public WorkflowAuthorityManager getWorkflowAuthorityManager()
    {
        return workflowAuthorityManager;
    }
}
