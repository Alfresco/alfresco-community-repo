/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefintions(java.util.List)
	 */
	protected void addParameterDefintions(List<ParameterDefinition> paramList) 
	{
        paramList.add(new ParameterDefinitionImpl(PARAM_PACKAGE_NAME, DataTypeDefinition.TEXT, true, 
              getParamDisplayLabel(PARAM_PACKAGE_NAME)));
        paramList.add(new ParameterDefinitionImpl(PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF, true, 
              getParamDisplayLabel(PARAM_DESTINATION_FOLDER)));
	}

}
