package org.alfresco.repo.action.executer;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Add features action executor implementation.
 * 
 * @author Roy Wetherall
 */
public class SetPropertyValueActionExecuter extends ActionExecuterAbstractBase
{
    /**
     * Action constants
     */
	public static final String NAME = "set-property-value";
	public static final String PARAM_PROPERTY = "property";
    public static final String PARAM_VALUE = "value";
	
	/**
	 * The node service
	 */
	private NodeService nodeService;
	
    /**
     * Set the node service
     * 
     * @param nodeService  the node service
     */
	public void setNodeService(NodeService nodeService) 
	{
		this.nodeService = nodeService;
	}

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(Action, NodeRef)
     */
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
		if (this.nodeService.exists(actionedUponNodeRef) == true)
		{
            // Set the value of the property
		    this.nodeService.setProperty(
                    actionedUponNodeRef, 
                    (QName)ruleAction.getParameterValue(PARAM_PROPERTY), 
                    ruleAction.getParameterValue(PARAM_VALUE));            
		}
    }

    /**
     * Add parameter definitions
     */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
        paramList.add(new ParameterDefinitionImpl(PARAM_PROPERTY, DataTypeDefinition.QNAME, true, getParamDisplayLabel(PARAM_PROPERTY), false, "ac-properties"));
		paramList.add(new ParameterDefinitionImpl(PARAM_VALUE, DataTypeDefinition.ANY, true, getParamDisplayLabel(PARAM_VALUE)));
	}

}
