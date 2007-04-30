/**
 * 
 */
package org.alfresco.repo.avm.actions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 * This action knows how to start an AVM specific workflow.
 * @author britt
 */
public class StartAVMWorkflowAction extends ActionExecuterAbstractBase 
{
    private static Logger fgLogger = Logger.getLogger(StartAVMWorkflowAction.class);
    
    public static final String NAME = "start-avm-workflow";
    public static final String PARAM_WORKFLOW_NAME = "workflow-name";
    
    /**
     * Reference to workflow service.
     */
    private WorkflowService fWorkflowService;
    
    /**
     * Reference to person service.
     */
    private PersonService fPersonService;
    
    /**
     * Set the workflow service.
     * @param service The workflow service.
     */
    public void setWorkflowService(WorkflowService service)
    {
        fWorkflowService = service;
    }
    
    /**
     * Set the person service.
     * @param service The person service.
     */
    public void setPersonService(PersonService service)
    {
        fPersonService = service;
    }
    
    /**
     * Default constructor.
     */
    public StartAVMWorkflowAction()
    {
        super();
    }
    
    /**
     * Start an AVM specific workflow.
     * @param action The action instance.
     * @param actionedUponNodeRef This should be an AVM folder that contains
     * the nodes to be flowed.
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) 
    {
        String workflowName = (String)action.getParameterValue(PARAM_WORKFLOW_NAME);
        WorkflowDefinition def = fWorkflowService.getDefinitionByName(workflowName);
        Map<QName, Serializable> wfParams = new HashMap<QName, Serializable>();
        wfParams.put(WorkflowModel.ASSOC_PACKAGE, actionedUponNodeRef);
//        ArrayList<NodeRef> assigneeList = new ArrayList<NodeRef>();
//        assigneeList.add(fPersonService.getPerson("admin"));
//        wfParams.put(QName.createQName("http://www.alfresco.org/model/wcmwf/1.0", "assignee"), 
//                assigneeList);
        wfParams.put(QName.createQName("http://www.alfresco.org/model/wcmwf/1.0", "description"), 
                "This performs a submit.");
        wfParams.put(WorkflowModel.PROP_CONTEXT, actionedUponNodeRef);
        for (QName name : wfParams.keySet())
        {
            fgLogger.error(name);
            fgLogger.error(wfParams.get(name).getClass());
            fgLogger.error(wfParams.get(name));
        }
        WorkflowPath path = fWorkflowService.startWorkflow(def.id, wfParams);
        /*
        if (path != null)
        {
            fgLogger.error("Workflow path is not null.");
            // extract the start task
            List<WorkflowTask> tasks = fWorkflowService.getTasksForWorkflowPath(path.id);
            fgLogger.error(tasks.size() + " tasks.");
            if (tasks.size() == 1)
            {
                WorkflowTask startTask = tasks.get(0);
              
                if (startTask.state == WorkflowTaskState.IN_PROGRESS)
                {
                    fgLogger.error("Calling End Task.");
                     // end the start task to trigger the first 'proper'
                     // task in the workflow
                    fWorkflowService.endTask(startTask.id, null);
                }
            }
        }
        */
    }

    /**
     * Setup any parameters for this action.
     * @param paramList The list of parameters to add to.
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_WORKFLOW_NAME,
                                                  DataTypeDefinition.TEXT,
                                                  true,
                                                  getParamDisplayLabel(PARAM_WORKFLOW_NAME)));
    }
}
