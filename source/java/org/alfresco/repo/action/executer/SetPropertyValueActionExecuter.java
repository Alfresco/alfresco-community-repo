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
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.service.cmr.repository.NodeRef, NodeRef)
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
        paramList.add(new ParameterDefinitionImpl(PARAM_PROPERTY, DataTypeDefinition.QNAME, true, getParamDisplayLabel(PARAM_PROPERTY)));
		paramList.add(new ParameterDefinitionImpl(PARAM_VALUE, DataTypeDefinition.ANY, true, getParamDisplayLabel(PARAM_VALUE)));
	}

}
