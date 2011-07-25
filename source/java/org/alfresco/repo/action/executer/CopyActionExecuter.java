/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.alfresco.repo.action.executer;

import java.util.List;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.CopyService.CopyInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleServiceException;

/**
 * Copy action executor.
 * <p>
 * Copies the actioned upon node to a specified location.
 * 
 * @author Roy Wetherall
 */
public class CopyActionExecuter extends ActionExecuterAbstractBase
{
    public static final String ERR_OVERWRITE = "Unable to overwrite copy because more than one have been found.";
    
    public static final String NAME = "copy";
    public static final String PARAM_DESTINATION_FOLDER = "destination-folder";
    public static final String PARAM_DEEP_COPY = "deep-copy";
    public static final String PARAM_OVERWRITE_COPY = "overwrite-copy";
    
    private CopyService copyService;
	
	/**
	 * The node service
	 */
    private NodeService nodeService;
	private CheckOutCheckInService checkOutCheckInService;
    
    /**
     * Sets the node service
     */
    public void setNodeService(NodeService nodeService) 
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Sets the copy service
     */
    public void setCopyService(CopyService copyService) 
    {
        this.copyService = copyService;
    }
    

	/**
	 * Service to determine check-in or check-out status
	 */
	public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService)
    {
        this.checkOutCheckInService = checkOutCheckInService;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_DESTINATION_FOLDER)));
        paramList.add(new ParameterDefinitionImpl(PARAM_DEEP_COPY, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_DEEP_COPY)));		
        paramList.add(new ParameterDefinitionImpl(PARAM_OVERWRITE_COPY, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_OVERWRITE_COPY)));
    }

	@Override
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
        if (!nodeService.exists(actionedUponNodeRef))
	{
            return;
    	}
            NodeRef destinationParent = (NodeRef)ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER);
            
            // Get the deep copy value
            boolean deepCopy = false;
        Boolean deepCopyValue = (Boolean)ruleAction.getParameterValue(PARAM_DEEP_COPY);
        if (deepCopyValue != null)
        {
            deepCopy = deepCopyValue.booleanValue();
        }
	        
        // Get the overwirte value
        boolean overwrite = true;
        Boolean overwriteValue = (Boolean)ruleAction.getParameterValue(PARAM_OVERWRITE_COPY);
        if (overwriteValue != null)
        {
            overwrite = overwriteValue.booleanValue();
        }
        
        // Since we are overwriting we need to figure out whether the destination node exists
        NodeRef copyNodeRef = null;
        if (overwrite == true)
        {
            // Try and find copies of the actioned upon node reference.
            // Include the parent folder because that's where the copy will be if this action
            // had done the first copy.
            PagingResults<CopyInfo> copies = copyService.getCopies(
                    actionedUponNodeRef,
                    destinationParent,
                    new PagingRequest(1000));
            for (CopyInfo copyInfo : copies.getPage())
            {
                NodeRef copy = copyInfo.getNodeRef();
                // We know that it is in the destination parent, but avoid working copies
                if (checkOutCheckInService.isWorkingCopy(copy))
                {
                    continue;
                }
                if (copyNodeRef == null)
                {
                    copyNodeRef = copy;
                }
                else
                {
                    throw new RuleServiceException(ERR_OVERWRITE);
                }
            }
        }
        
        if (copyNodeRef != null)
        {
            // Overwrite the state of the destination node ref with the actioned upon node state
            this.copyService.copy(actionedUponNodeRef, copyNodeRef);
        }
        else
        {
            ChildAssociationRef originalAssoc = nodeService.getPrimaryParent(actionedUponNodeRef);
            // Create a new copy of the node
            this.copyService.copyAndRename(
	                actionedUponNodeRef, 
	                destinationParent,
                    originalAssoc.getTypeQName(),
                    originalAssoc.getQName(),
	                deepCopy);
        }
		}
}
