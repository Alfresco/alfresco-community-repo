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
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Simple workflow action executor
 * 
 * @author Roy Wetherall
 */
public class SimpleWorkflowActionExecuter extends ActionExecuterAbstractBase 
{
	public static final String NAME = "simple-workflow";
	public static final String PARAM_APPROVE_STEP = "approve-step";
	public static final String PARAM_APPROVE_FOLDER = "approve-folder";
	public static final String PARAM_APPROVE_MOVE = "approve-move";
	public static final String PARAM_REJECT_STEP = "reject-step";
	public static final String PARAM_REJECT_FOLDER = "reject-folder";
	public static final String PARAM_REJECT_MOVE = "reject-move";
	
	private NodeService nodeService;

	public void setNodeService(NodeService nodeService) 
	{
		this.nodeService = nodeService;
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
		paramList.add(new ParameterDefinitionImpl(PARAM_APPROVE_STEP, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_APPROVE_STEP)));
		paramList.add(new ParameterDefinitionImpl(PARAM_APPROVE_FOLDER, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_APPROVE_FOLDER)));
		paramList.add(new ParameterDefinitionImpl(PARAM_APPROVE_MOVE, DataTypeDefinition.BOOLEAN, true, getParamDisplayLabel(PARAM_APPROVE_MOVE)));
		paramList.add(new ParameterDefinitionImpl(PARAM_REJECT_STEP, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_REJECT_STEP)));
		paramList.add(new ParameterDefinitionImpl(PARAM_REJECT_FOLDER, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_REJECT_FOLDER)));
		paramList.add(new ParameterDefinitionImpl(PARAM_REJECT_MOVE, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_REJECT_MOVE)));		
	}

	/**
	 * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	protected void executeImpl(
			Action ruleAction,
			NodeRef actionedUponNodeRef) 
	{
		if (this.nodeService.exists(actionedUponNodeRef) == true &&
		    this.nodeService.hasAspect(actionedUponNodeRef, ApplicationModel.ASPECT_SIMPLE_WORKFLOW) == false)
		{
			// Get the parameter values
			String approveStep = (String)ruleAction.getParameterValue(PARAM_APPROVE_STEP);
			NodeRef approveFolder = (NodeRef)ruleAction.getParameterValue(PARAM_APPROVE_FOLDER);
			Boolean approveMove = (Boolean)ruleAction.getParameterValue(PARAM_APPROVE_MOVE);
			String rejectStep = (String)ruleAction.getParameterValue(PARAM_REJECT_STEP);
			NodeRef rejectFolder = (NodeRef)ruleAction.getParameterValue(PARAM_REJECT_FOLDER);
			Boolean rejectMove = (Boolean)ruleAction.getParameterValue(PARAM_REJECT_MOVE);
			
			// Set the property values
			Map<QName, Serializable> propertyValues = new HashMap<QName, Serializable>();
			propertyValues.put(ApplicationModel.PROP_APPROVE_STEP, approveStep);
			propertyValues.put(ApplicationModel.PROP_APPROVE_FOLDER, approveFolder);
			if (approveMove != null)
			{
				propertyValues.put(ApplicationModel.PROP_APPROVE_MOVE, approveMove.booleanValue());
			}						
			propertyValues.put(ApplicationModel.PROP_REJECT_STEP, rejectStep);
			propertyValues.put(ApplicationModel.PROP_REJECT_FOLDER, rejectFolder);
	        if (rejectMove != null)
	        {
	        	propertyValues.put(ApplicationModel.PROP_REJECT_MOVE, rejectMove.booleanValue());
	        }
			
			// Apply the simple workflow aspect to the node
			this.nodeService.addAspect(actionedUponNodeRef, ApplicationModel.ASPECT_SIMPLE_WORKFLOW, propertyValues);
		}
	}
}
