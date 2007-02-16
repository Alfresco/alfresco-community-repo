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

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.RepositoryExporterService;

/**
 * Repository Exporter action executor
 * 
 * @author davidc
 */
public class RepositoryExporterActionExecuter extends ActionExecuterAbstractBase
{
    public static final String NAME = "repository-export";
    public static final String PARAM_PACKAGE_NAME = "package-name";
    public static final String PARAM_DESTINATION_FOLDER = "destination";
    
    /**
     * The exporter service
     */
    private RepositoryExporterService exporterService;
    
    /**
     * Sets the ExporterService to use
     * 
     * @param exporterService The ExporterService
     */
	public void setRepositoryExporterService(RepositoryExporterService exporterService) 
	{
		this.exporterService = exporterService;
	}
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.repo.ref.NodeRef, org.alfresco.repo.ref.NodeRef)
     */
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
        String packageName = (String)ruleAction.getParameterValue(PARAM_PACKAGE_NAME);
        NodeRef repoDestination = (NodeRef)ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER);
        exporterService.export(repoDestination, packageName);
    }

	/**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
	 */
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
        paramList.add(new ParameterDefinitionImpl(PARAM_PACKAGE_NAME, DataTypeDefinition.TEXT, true, 
              getParamDisplayLabel(PARAM_PACKAGE_NAME)));
        paramList.add(new ParameterDefinitionImpl(PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF, true, 
              getParamDisplayLabel(PARAM_DESTINATION_FOLDER)));
	}

}
