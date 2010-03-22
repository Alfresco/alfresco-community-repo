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
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Simple workflow action executor
 * 
 * @author Roy Wetherall
 */
public class TransitionSimpleWorkflowActionExecuter extends ActionExecuterAbstractBase 
{
    /** Node Service */
	private NodeService nodeService;

	/** Copy Service */
	private CopyService copyService;
	
	/** Indicates whether we are transitioning an accept step, false if a reject step */
	private boolean isAcceptTransition = true;
	
	/**
	 * Set the node service
	 * 
	 * @param nodeService  node service
	 */
	public void setNodeService(NodeService nodeService) 
	{
		this.nodeService = nodeService;
	}
	
	/**
	 * Set the copy service
	 * 
	 * @param copyService  copy service
	 */
	public void setCopyService(CopyService copyService)
	{
	    this.copyService = copyService;
	}
	
	/**
	 * Sets whether this is an accept transition or not
	 */
	public void setIsAcceptTransition(boolean value)
	{
	    this.isAcceptTransition = value;
	}

	/**
	 * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
	 */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{	
	}

	/**
	 * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	protected void executeImpl(
			final Action ruleAction,
			final NodeRef actionedUponNodeRef) 
	{
		if (nodeService.exists(actionedUponNodeRef) == true &&
		    nodeService.hasAspect(actionedUponNodeRef, ApplicationModel.ASPECT_SIMPLE_WORKFLOW) == true)
		{
		    NodeRef destinationFolder = null;
		    Boolean isMove = null;
		    
		    // Get the destination folder and determine whether we do a move or copy
		    Map<QName, Serializable> props = nodeService.getProperties(actionedUponNodeRef);
		    if (isAcceptTransition == true)
		    {
		        destinationFolder = (NodeRef)props.get(ApplicationModel.PROP_APPROVE_FOLDER);
	            isMove = (Boolean)props.get(ApplicationModel.PROP_APPROVE_MOVE);
		    }
		    else
		    {
		        destinationFolder = (NodeRef)props.get(ApplicationModel.PROP_REJECT_FOLDER);
                isMove = (Boolean)props.get(ApplicationModel.PROP_REJECT_MOVE);
		    }
		    
		    if (destinationFolder == null)
		    {
		        // We need a destination folder in order to transition the workflow
		        throw new AlfrescoRuntimeException("No folder was specified for the simple workflow step.");
		    }
		    
		    if (isMove == null)
		    {
		        // Assume default of false
		        isMove = Boolean.FALSE;
		    }
		    
		    // Set the name of the node
		    String name = (String)props.get(ContentModel.PROP_NAME);
		    
		    // first we need to take off the simpleworkflow aspect
            nodeService.removeAspect(actionedUponNodeRef, ApplicationModel.ASPECT_SIMPLE_WORKFLOW);
		    
		    if (Boolean.TRUE.equals(isMove) == true)
		    {
		        // move the node to the specified folder
		        String qname = QName.createValidLocalName(name);
		        nodeService.moveNode(
		                actionedUponNodeRef, 
		                destinationFolder, 
		                ContentModel.ASSOC_CONTAINS,
		                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, qname));
		    }
		    else
		    {     
		        // copy the node to the specified folder
		        String qname = QName.createValidLocalName(name);
		        NodeRef newNode = copyService.copy(
		                actionedUponNodeRef, 
		                destinationFolder, 
		                ContentModel.ASSOC_CONTAINS,
		                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, qname), 
		                true);
		         
		        // the copy service does not copy the name of the node so we
		        // need to update the property on the copied item
		        nodeService.setProperty(newNode, ContentModel.PROP_NAME, name);
		    }		            
		}
	}
}
