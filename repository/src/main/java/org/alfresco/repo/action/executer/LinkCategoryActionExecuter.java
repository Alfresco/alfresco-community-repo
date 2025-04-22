/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
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
     * @param nodeService
     *            the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the dictionary service
     * 
     * @param dictionaryService
     *            the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Add the parameter definitions
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_CATEGORY_ASPECT, DataTypeDefinition.QNAME, false, getParamDisplayLabel(PARAM_CATEGORY_ASPECT)));
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
            QName categoryAspect = (QName) ruleAction.getParameterValue(PARAM_CATEGORY_ASPECT);
            if (categoryAspect == null)
            {
                // Use the default general classifiable aspect
                // cm:generalclassifiable
                categoryAspect = ContentModel.ASPECT_GEN_CLASSIFIABLE;
            }
            NodeRef categoryValue = (NodeRef) ruleAction.getParameterValue(PARAM_CATEGORY_VALUE);

            // Check that the aspect is classifiable and is currently applied to the node
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

                // Check that the category property is not null
                if (categoryProperty == null)
                {
                    throw new AlfrescoRuntimeException("The category aspect " + categoryAspect.toPrefixString() + " does not have a category property to set.");
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
                        Collection<NodeRef> categories = null;
                        if (value == null)
                        {
                            categories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, categoryValue);
                        }
                        else
                        {
                            categories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, value);
                            if (categories.contains(categoryValue) == false)
                            {
                                categories.add(categoryValue);
                            }
                        }
                        this.nodeService.setProperty(actionedUponNodeRef, categoryProperty, (Serializable) categories);
                    }
                }
            }
        }
    }
}
