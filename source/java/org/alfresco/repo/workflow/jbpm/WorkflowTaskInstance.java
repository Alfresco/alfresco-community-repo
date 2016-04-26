
package org.alfresco.repo.workflow.jbpm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.namespace.QName;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springmodules.workflow.jbpm31.JbpmFactoryLocator;

/**
 * Alfresco specific implementation of a jBPM task instance
 * 
 * @author davidc
 */
public class WorkflowTaskInstance extends TaskInstance
{
    private static final long serialVersionUID = 6824116036569411964L;

    /**
     * Used to look up the Alfresco JBPM Engine.
     */
    private String jbpmEngineName = null;

    /** Alfresco JBPM Engine */
    private static JBPMEngine jbpmEngine = null;

    /**
     * Construct
     */
    public WorkflowTaskInstance()
    {
        super();
    }

    /**
     * Construct
     * 
     * @param taskName String
     * @param actorId String
     */
    public WorkflowTaskInstance(String taskName, String actorId)
    {
        super(taskName, actorId);
    }

    /**
     * Sets jbpmEngineName which is used to get the JBPMEngine instance from a
     * BeanFactory
     * 
     * @param jbpmEngineName the jbpmEngineName to set
     */
    public void setJbpmEngineName(String jbpmEngineName)
    {
        this.jbpmEngineName = jbpmEngineName;
    }

    /**
     * Gets the JBPM Engine instance
     * 
     * @return JBPM Engine
     */
    private JBPMEngine getJBPMEngine()
    {
        if (jbpmEngine == null)
        {
            BeanFactoryLocator factoryLocator = new JbpmFactoryLocator();
            BeanFactoryReference factory = factoryLocator.useBeanFactory(null);
            if (jbpmEngineName == null) jbpmEngineName = "jbpm_engine";
            jbpmEngine = (JBPMEngine) factory.getFactory().getBean(jbpmEngineName);
            if (jbpmEngine == null) { throw new WorkflowException(
                        "Failed to retrieve JBPMEngine component"); }
        }
        return jbpmEngine;
    }

    @Override
    public void create(ExecutionContext executionContext)
    {
        super.create(executionContext);
        getJBPMEngine().setDefaultTaskProperties(this);
    }

    @Override
    public void end(Transition transition)
    {
        // Force assignment of task if transition is taken, but no owner has yet
        // been assigned
        if (actorId == null)
        {
            actorId = AuthenticationUtil.getFullyAuthenticatedUser();
        }

        // Set task properties on completion of task
        // NOTE: Set properties first, so they're available during the
        // submission of
        // task variables to the process context
        Map<QName, Serializable> taskProperties = new HashMap<QName, Serializable>();
        Transition outcome = (transition == null) ? token.getNode().getDefaultLeavingTransition()
                    : transition;
        if (outcome != null)
        {
            taskProperties.put(WorkflowModel.PROP_OUTCOME, outcome.getName());
        }
        taskProperties.put(WorkflowModel.PROP_STATUS, "Completed");
        getJBPMEngine().setTaskProperties(this, taskProperties);

        // perform transition
        super.end(transition);

        if (getTask().getStartState() != null)
        {
            // if ending a start task, push start task properties to process
            // context, if not
            // already done
            getJBPMEngine().setDefaultWorkflowProperties(this);

            // set task description
            getJBPMEngine().setDefaultStartTaskDescription(this);
        }
    }

}
