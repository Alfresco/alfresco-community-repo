package org.alfresco.repo.action.executer;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Check out action executor
 * 
 * @author Roy Wetherall
 */
public class CheckOutActionExecuter extends ActionExecuterAbstractBase
{
    public static final String NAME = "check-out";
    public static final String PARAM_DESTINATION_FOLDER = "destination-folder";
    public static final String PARAM_ASSOC_TYPE_QNAME = "assoc-type";
    public static final String PARAM_ASSOC_QNAME = "assoc-name";

    /**
     * The version operations service
     */
    private CheckOutCheckInService cociService;
	
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
	 * Set the coci service
	 * 
	 * @param cociService  the coci service
	 */
	public void setCociService(CheckOutCheckInService cociService) 
	{
		this.cociService = cociService;
	}
    
	/**
	 * Add the parameter defintions
	 */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
		paramList.add(new ParameterDefinitionImpl(PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_DESTINATION_FOLDER)));
		paramList.add(new ParameterDefinitionImpl(PARAM_ASSOC_TYPE_QNAME, DataTypeDefinition.QNAME, false, getParamDisplayLabel(PARAM_ASSOC_TYPE_QNAME)));
		paramList.add(new ParameterDefinitionImpl(PARAM_ASSOC_QNAME, DataTypeDefinition.QNAME, false, getParamDisplayLabel(PARAM_ASSOC_QNAME)));
	}

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
		if (this.nodeService.exists(actionedUponNodeRef) == true &&
			this.nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_WORKING_COPY) == false &&
			isApplicableType(actionedUponNodeRef) == true)
		{
	        // Get the destination details
	        NodeRef destinationParent = (NodeRef)ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER);
	        QName destinationAssocTypeQName = (QName)ruleAction.getParameterValue(PARAM_ASSOC_TYPE_QNAME);
	        QName destinationAssocQName = (QName)ruleAction.getParameterValue(PARAM_ASSOC_QNAME);
	        
	        if (destinationParent == null || destinationAssocTypeQName == null || destinationAssocQName == null)
	        {
	            // Check the node out to the current location
	            this.cociService.checkout(actionedUponNodeRef);
	        }
	        else
	        {
	            // Check the node out to the specified location
	            this.cociService.checkout(
	                    actionedUponNodeRef, 
	                    destinationParent, 
	                    destinationAssocTypeQName, 
	                    destinationAssocQName);
	        }
		}
    }
}
