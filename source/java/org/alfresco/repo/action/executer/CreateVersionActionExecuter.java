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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;

/**
 * Add features action executor implementation.
 * 
 * @author Roy Wetherall
 */
public class CreateVersionActionExecuter extends ActionExecuterAbstractBase
{
    /**
     * Action constants
     */
	public static final String NAME = "create-version";
	public static final String PARAM_DESCRIPTION = "description";
	public static final String PARAM_MINOR_CHANGE = "minor-change";
	
	/** Node service */
    public NodeService nodeService;
    
    /** Version service */
	public VersionService versionService;
	
	/**
	 * Set node service
	 * 
	 * @param nodeService  node service
	 */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set version service
     * 
     * @param versionService    version service
     */          
    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.service.cmr.repository.NodeRef, NodeRef)
     */
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
		if (this.nodeService.exists(actionedUponNodeRef) == true && 
            this.nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_VERSIONABLE) == true)
		{
            Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(2);
		    
		    // Get the version description
            String description = (String)ruleAction.getParameterValue(PARAM_DESCRIPTION);
            if (description != null && description.length() != 0)
            {
                versionProperties.put(Version.PROP_DESCRIPTION, description);
            }
            
            // determine whether the change is minor or major
            Boolean minorChange = (Boolean)ruleAction.getParameterValue(PARAM_MINOR_CHANGE);
            if (minorChange != null && minorChange.booleanValue() == false)
            {
               versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
            }
            else
            {
               versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
            }

            // Create the version
			this.versionService.createVersion(actionedUponNodeRef, versionProperties);
		}
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
	    paramList.add(new ParameterDefinitionImpl(PARAM_MINOR_CHANGE, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_MINOR_CHANGE)));
	    paramList.add(new ParameterDefinitionImpl(PARAM_DESCRIPTION, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_DESCRIPTION)));
	}

}
