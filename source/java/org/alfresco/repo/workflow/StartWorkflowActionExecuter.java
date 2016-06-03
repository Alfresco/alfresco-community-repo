package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Simple workflow action executor
 * 
 * @author David Caruana
 */
public class StartWorkflowActionExecuter extends ActionExecuterAbstractBase 
{
    public static final String NAME = "start-workflow";
    public static final String PARAM_WORKFLOW_NAME = "workflowName";
    public static final String PARAM_END_START_TASK = "endStartTask";
    public static final String PARAM_START_TASK_TRANSITION = "startTaskTransition";
    
    // action dependencies
    private NamespaceService namespaceService;
    private WorkflowService workflowService;
    private NodeService nodeService;

    
    /**
     * @param namespaceService NamespaceService
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @param nodeService NodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param workflowService WorkflowService
     */
    public void setWorkflowService(WorkflowService workflowService) 
    {
        this.workflowService = workflowService;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#getAdhocPropertiesAllowed()
     */
    @Override
    protected boolean getAdhocPropertiesAllowed()
    {
        return true;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_WORKFLOW_NAME, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_WORKFLOW_NAME)));
        paramList.add(new ParameterDefinitionImpl(PARAM_END_START_TASK, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_END_START_TASK)));
        paramList.add(new ParameterDefinitionImpl(PARAM_START_TASK_TRANSITION, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_START_TASK_TRANSITION)));
        // TODO: start task node parameter
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) 
    {
        // retrieve workflow definition
        String workflowName = (String)ruleAction.getParameterValue(PARAM_WORKFLOW_NAME);
        WorkflowDefinition def = workflowService.getDefinitionByName(workflowName);
        
        // create workflow package to contain actioned upon node
        NodeRef workflowPackage = (NodeRef)ruleAction.getParameterValue(WorkflowModel.ASSOC_PACKAGE.toPrefixString(namespaceService));
        workflowPackage = workflowService.createPackage(workflowPackage);
        ChildAssociationRef childAssoc = nodeService.getPrimaryParent(actionedUponNodeRef);
        nodeService.addChild(workflowPackage, actionedUponNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, childAssoc.getQName());
        
        // build map of workflow start task parameters
        Map<String, Serializable> paramValues = ruleAction.getParameterValues();
        Map<QName, Serializable> workflowParameters = new HashMap<QName, Serializable>();
        workflowParameters.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
        for (Map.Entry<String, Serializable> entry : paramValues.entrySet())
        {
            if (!entry.getKey().equals(PARAM_WORKFLOW_NAME))
            {
                QName qname = QName.createQName(entry.getKey(), namespaceService);
                Serializable value = entry.getValue();
                workflowParameters.put(qname, value);
            }
        }

        // provide a default context, if one is not specified
        Serializable context = workflowParameters.get(WorkflowModel.PROP_CONTEXT);
        if (context == null)
        {
            workflowParameters.put(WorkflowModel.PROP_CONTEXT, childAssoc.getParentRef());
        }

        // start the workflow
        WorkflowPath path = workflowService.startWorkflow(def.getId(), workflowParameters);

        // determine whether to auto-end the start task
        Boolean endStartTask = (Boolean)ruleAction.getParameterValue(PARAM_END_START_TASK);
        String startTaskTransition = (String)ruleAction.getParameterValue(PARAM_START_TASK_TRANSITION);
        endStartTask = (endStartTask == null) ? true : false;
        
        // auto-end the start task with the provided transition (if one)
        if (endStartTask)
        {
            List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
            for (WorkflowTask task : tasks)
            {
                workflowService.endTask(task.getId(), startTaskTransition);
            }
        }
    }
}
