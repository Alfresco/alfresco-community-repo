/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;

/**
 * Check in action executor
 * 
 * @author Roy Wetherall
 */
public class CheckInActionExecuter extends ActionExecuterAbstractBase
{
    public static final String NAME = "check-in";
    public static final String PARAM_DESCRIPTION = "description";
    public static final String PARAM_MINOR_CHANGE = "minorChange";
    
    /**
     * The node service
     */
    private NodeService nodeService;
    
    /**
     * The coci service
     */
    private CheckOutCheckInService cociService;

    /**
     * Set node service
     * 
     * @param nodeService  the node service
     */
	public void setNodeService(NodeService nodeService) 
	{
		this.nodeService = nodeService;
	}
	
    /**
     * Set the checkIn checkOut service
     * 
     * @param cociService  the checkIn checkOut Service
     */
	public void setCociService(CheckOutCheckInService cociService) 
	{
		this.cociService = cociService;
	}

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.repo.ref.NodeRef, org.alfresco.repo.ref.NodeRef)
     */
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
        // First ensure that the actionedUponNodeRef is a workingCopy
        if (this.nodeService.exists(actionedUponNodeRef) == true &&
			this.nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_WORKING_COPY) == true)
        {
            // Get the version description
            String description = (String)ruleAction.getParameterValue(PARAM_DESCRIPTION);
            Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(1);
            versionProperties.put(Version.PROP_DESCRIPTION, description);
            
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
            
            // TODO determine whether the document should be kept checked out
            
            // Check the node in
            this.cociService.checkin(actionedUponNodeRef, versionProperties);
        }
    }

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
		paramList.add(new ParameterDefinitionImpl(PARAM_DESCRIPTION, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_DESCRIPTION)));
	}

}
