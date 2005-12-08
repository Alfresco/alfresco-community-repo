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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class AddFeaturesActionExecuter extends ActionExecuterAbstractBase
{
    /**
     * Action constants
     */
	public static final String NAME = "add-features";
	public static final String PARAM_ASPECT_NAME = "aspect-name";
    public static final String PARAM_ASPECT_PROPERTIES = "aspect_properties";
	
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
	 * Adhoc properties are allowed for this executor
	 */
	@Override
	protected boolean getAdhocPropertiesAllowed()
	{
		return true;
	}

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.service.cmr.repository.NodeRef, NodeRef)
     */
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
		if (this.nodeService.exists(actionedUponNodeRef) == true)
		{
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			QName aspectQName = null;
			
	        Map<String, Serializable> paramValues = ruleAction.getParameterValues();
	        for (Map.Entry<String, Serializable> entry : paramValues.entrySet())
			{
				if (entry.getKey().equals(PARAM_ASPECT_NAME) == true)
				{
					aspectQName = (QName)entry.getValue();
				}
				else
				{
					// Must be an adhoc property
					QName propertyQName = QName.createQName(entry.getKey());
					Serializable propertyValue = entry.getValue();
					properties.put(propertyQName, propertyValue);
				}
			}
			
	        // Add the aspect
	        this.nodeService.addAspect(actionedUponNodeRef, aspectQName, properties);
		}
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefintions(java.util.List)
     */
	@Override
	protected void addParameterDefintions(List<ParameterDefinition> paramList) 
	{
		paramList.add(new ParameterDefinitionImpl(PARAM_ASPECT_NAME, DataTypeDefinition.QNAME, true, getParamDisplayLabel(PARAM_ASPECT_NAME)));
	}

}
