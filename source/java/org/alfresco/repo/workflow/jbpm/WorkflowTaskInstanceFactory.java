
package org.alfresco.repo.workflow.jbpm;

import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.taskmgmt.TaskInstanceFactory;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * jBPM factory for creating Alfresco derived Task Instances
 * 
 * @author davidc
 */
public class WorkflowTaskInstanceFactory implements TaskInstanceFactory
{
    private static final long serialVersionUID = -8097108150047415711L;

    private String jbpmEngineName;

    /**
     * @param jbpmEngine the jbpmEngine to set
     */
    public void setJbpmEngine(String jbpmEngine)
    {
        this.jbpmEngineName = jbpmEngine;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jbpm.taskmgmt.TaskInstanceFactory#createTaskInstance(org.jbpm.graph
     * .exe.ExecutionContext)
     */
    public TaskInstance createTaskInstance(ExecutionContext executionContext)
    {
        WorkflowTaskInstance taskInstance = new WorkflowTaskInstance();
        taskInstance.setJbpmEngineName(jbpmEngineName);
        return taskInstance;
    }
}
