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
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;

/**
 * This action knows how to start an AVM specific workflow.
 * @author britt
 */
public class StartAVMWorkflowAction extends ActionExecuterAbstractBase 
{
    public static final String NAME = "start-avm-workflow";
    public static final String PARAM_STORE_NAME = "store-name";
    public static final String PARAM_WORKFLOW_NAME = "workflow-name";
    
    /**
     * Reference to workflow service.
     */
    private WorkflowService fWorkflowService;
    
    /**
     * Set the workflow service.
     * @param service The workflow service.
     */
    public void setWorkflowService(WorkflowService service)
    {
        fWorkflowService = service;
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
        String storeName = (String)action.getParameterValue(PARAM_STORE_NAME);
        WorkflowDefinition def = fWorkflowService.getDefinitionByName(name);
        NodeRef workflowPackage = fWorkflowService.createPackage(actionedUponNodeRef);
        Map<QName, Serializable> wfParams = new HashMap<QName, Serializable>();
        wfParams.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
    }

    /**
     * Setup any parameters for this action.
     * @param paramList The list of parameters to add to.
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_STORE_NAME,
                                                  DataTypeDefinition.TEXT,
                                                  true,
                                                  getParamDisplayLabel(PARAM_STORE_NAME)));
        paramList.add(new ParameterDefinitionImpl(PARAM_WORKFLOW_NAME,
                                                  DataTypeDefinition.TEXT,
                                                  true,
                                                  getParamDisplayLabel(PARAM_WORKFLOW_NAME)));
    }
}
