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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * Link category action executor
 * 
 * @author Roy Wetherall
 */
public class LinkCategoryActionExecuter extends ActionExecuterAbstractBase 
{
	/**
	 * Rule constants
	 */
	public static final String NAME = "link-category";
    public static final String PARAM_CATEGORY_ASPECT = "category-aspect";
    public static final String PARAM_CATEGORY_VALUE = "category-value";
	
	/**
	 * The node service
	 */
	private NodeService nodeService;
	
	/**
	 * The dictionary service
	 */
	private DictionaryService dictionaryService;
    
    /**
     * Sets the node service
     * 
     * @param nodeService  the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Sets the dictionary service
     * 
     * @param dictionaryService  the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Add the parameter definitions
     */
	@Override
	protected void addParameterDefintions(List<ParameterDefinition> paramList) 
	{
        paramList.add(new ParameterDefinitionImpl(PARAM_CATEGORY_ASPECT, DataTypeDefinition.QNAME, true, getParamDisplayLabel(PARAM_CATEGORY_ASPECT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_CATEGORY_VALUE, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_CATEGORY_VALUE)));
	}
	
    /**
     * Execute action implementation
     */
    @Override
    protected void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
		// Double check that the node still exists
		if (this.nodeService.exists(actionedUponNodeRef) == true)
		{
			// Get the rule parameter values
			QName categoryAspect = (QName)ruleAction.getParameterValue(PARAM_CATEGORY_ASPECT);
			NodeRef categoryValue = (NodeRef)ruleAction.getParameterValue(PARAM_CATEGORY_VALUE);
			
            // Check that the apect is classifiable and is currently applied to the node
            if (this.dictionaryService.isSubClass(categoryAspect, ContentModel.ASPECT_CLASSIFIABLE) == true)
            {
                // Get the category property qname
                QName categoryProperty = null;
                Map<QName, PropertyDefinition> propertyDefs = this.dictionaryService.getAspect(categoryAspect).getProperties();
                for (Map.Entry<QName, PropertyDefinition> entry : propertyDefs.entrySet()) 
                {
                    if (DataTypeDefinition.CATEGORY.equals(entry.getValue().getDataType().getName()) == true)
                    {
                        // Found the category property
                        categoryProperty = entry.getKey();
                        break;
                    }
                }
                
                if (categoryAspect != null)
                {
                    if (this.nodeService.hasAspect(actionedUponNodeRef, categoryAspect) == false)
                    {
                        // Add the aspect and set the category property value
                        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
                        properties.put(categoryProperty, categoryValue);
                        this.nodeService.addAspect(actionedUponNodeRef, categoryAspect, properties);
                    }
                    else
                    {
                        // Append the category value to the existing values
                        Serializable value = this.nodeService.getProperty(actionedUponNodeRef, categoryProperty);
                        Collection<NodeRef> categories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, value);
                        categories.add(categoryValue);
                        this.nodeService.setProperty(actionedUponNodeRef, categoryProperty, (Serializable)categories);
                    }
                }
            }			
		}
	}
}
