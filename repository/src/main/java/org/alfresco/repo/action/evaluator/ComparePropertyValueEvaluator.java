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
package org.alfresco.repo.action.evaluator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.action.evaluator.compare.ContentPropertyName;
import org.alfresco.repo.action.evaluator.compare.PropertyValueComparator;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * Compare property value evaluator
 * 
 * @author Roy Wetherall
 */
public class ComparePropertyValueEvaluator extends ActionConditionEvaluatorAbstractBase
{
    private static Log logger = LogFactory.getLog(ComparePropertyValueEvaluator.class);

    /**
     * Evaluator constants
     */
    public final static String NAME = "compare-property-value";

    public final static String PARAM_PROPERTY = "property";
    public final static String PARAM_CONTENT_PROPERTY = "content-property";
    public final static String PARAM_VALUE = "value";
    public final static String PARAM_OPERATION = "operation";

    /**
     * The default property to check if none is specified in the properties
     */
    private final static QName DEFAULT_PROPERTY = ContentModel.PROP_NAME;

    /**
     * I18N message ID's
     */
    private static final String MSGID_INVALID_OPERATION = "compare_property_value_evaluator.invalid_operation";
    private static final String MSGID_NO_CONTENT_PROPERTY = "compare_property_value_evaluator.no_content_property";

    /**
     * Map of comparators used by different property types
     */
    private Map<QName, PropertyValueComparator> comparators = new HashMap<QName, PropertyValueComparator>();

    /**
     * The node service
     */
    protected NodeService nodeService;

    /**
     * The content service
     */
    protected ContentService contentService;

    /**
     * The dictionary service
     */
    protected DictionaryService dictionaryService;

    /**
     * Set node service
     * 
     * @param nodeService
     *            the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the content service
     * 
     * @param contentService
     *            the content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * Set the dictionary service
     * 
     * @param dictionaryService
     *            the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the list of property value comparators
     *
     * @param comparators
     *            the list of property value comparators
     */
    public void setPropertyValueComparators(List<PropertyValueComparator> comparators)
    {
        for (PropertyValueComparator comparator : comparators)
        {
            comparator.registerComparator(this);
        }
    }

    /**
     * Registers a comparator for a given property data type.
     * 
     * @param dataType
     *            property data type
     * @param comparator
     *            property value comparator
     */
    public void registerComparator(QName dataType, PropertyValueComparator comparator)
    {
        this.comparators.put(dataType, comparator);
    }

    /**
     * Add parameter definitions
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_PROPERTY, DataTypeDefinition.QNAME, false, getParamDisplayLabel(PARAM_PROPERTY)));
        paramList.add(new ParameterDefinitionImpl(PARAM_CONTENT_PROPERTY, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_CONTENT_PROPERTY), false, "ac-content-properties"));
        paramList.add(new ParameterDefinitionImpl(PARAM_VALUE, DataTypeDefinition.ANY, true, getParamDisplayLabel(PARAM_VALUE)));
        paramList.add(new ParameterDefinitionImpl(PARAM_OPERATION, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_OPERATION), false, "ac-compare-operations"));
    }

    /**
     * @see ActionConditionEvaluatorAbstractBase#evaluateImpl(ActionCondition, NodeRef)
     */
    @SuppressWarnings("unchecked")
    public boolean evaluateImpl(
            ActionCondition ruleCondition,
            NodeRef actionedUponNodeRef)
    {
        boolean result = false;

        if (this.nodeService.exists(actionedUponNodeRef) == true)
        {
            // Get the name value of the node
            QName propertyQName = (QName) ruleCondition.getParameterValue(PARAM_PROPERTY);
            if (propertyQName == null)
            {
                if (logger.isWarnEnabled())
                    logger.warn("ComparePropertyValue - Property is NULL.  Setting to " + DEFAULT_PROPERTY);

                propertyQName = DEFAULT_PROPERTY;
            }

            // Get the original value and the value to match
            Serializable propertyValue = this.nodeService.getProperty(actionedUponNodeRef, propertyQName);
            Serializable compareValue = ruleCondition.getParameterValue(PARAM_VALUE);

            // Get the operation
            ComparePropertyValueOperation operation = null;
            String operationString = (String) ruleCondition.getParameterValue(PARAM_OPERATION);
            if (operationString != null)
            {
                operation = ComparePropertyValueOperation.valueOf(operationString);
            }

            // Look at the type of the property (assume to be ANY if none found in dictionary)
            QName propertyTypeQName = DataTypeDefinition.ANY;
            PropertyDefinition propertyDefintion = this.dictionaryService.getProperty(propertyQName);
            if (propertyDefintion != null)
            {
                propertyTypeQName = propertyDefintion.getDataType().getName();
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Evaluating Property Parameters, propertyQName - [" + propertyQName +
                        "] getInverted? [" + ruleCondition.getInvertCondition() + "] operation [" +
                        operation + "]");
                logger.debug("Compare Value [" + compareValue + "] Actual Value [" + propertyValue + "]");
            }

            // Sort out what to do if the property is a content property
            if (DataTypeDefinition.CONTENT.equals(propertyTypeQName) == true)
            {
                // Get the content property name
                ContentPropertyName contentProperty = null;
                String contentPropertyString = (String) ruleCondition.getParameterValue(PARAM_CONTENT_PROPERTY);
                if (contentPropertyString == null)
                {
                    // Error if no content property has been set
                    throw new ActionServiceException(MSGID_NO_CONTENT_PROPERTY);
                }
                else
                {
                    contentProperty = ContentPropertyName.valueOf(contentPropertyString);
                }

                // Get the content data
                if (propertyValue != null)
                {
                    ContentData contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, propertyValue);
                    switch (contentProperty)
                    {
                    case ENCODING:
                    {
                        propertyTypeQName = DataTypeDefinition.TEXT;
                        propertyValue = contentData.getEncoding();
                        break;
                    }
                    case SIZE:
                    {
                        propertyTypeQName = DataTypeDefinition.LONG;
                        propertyValue = contentData.getSize();
                        break;
                    }
                    case MIME_TYPE:
                    {
                        propertyTypeQName = DataTypeDefinition.TEXT;
                        propertyValue = contentData.getMimetype();
                        break;
                    }
                    }
                }
            }

            if (propertyValue != null)
            {
                // Try and get a matching comparator
                PropertyValueComparator comparator = this.comparators.get(propertyTypeQName);
                if (comparator != null)
                {
                    // Figure out if property is multivalued, compare all of the entries till finding a match
                    PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
                    if (propertyDef.isMultiValued())
                    {
                        for (Serializable value : ((ArrayList<Serializable>) propertyValue))
                        {
                            boolean success = comparator.compare(value, compareValue, operation);
                            if (success)
                            {
                                result = true;
                                break;
                            }
                        }
                    }
                    else
                    {
                        // Call the comparator for the property type
                        result = comparator.compare(propertyValue, compareValue, operation);
                    }
                }
                else
                {
                    if (logger.isWarnEnabled())
                    {
                        logger.warn("Comparator not found for property type " + propertyTypeQName);
                    }
                    // The default behaviour is to assume the property can only be compared using equals
                    if (operation != null && operation != ComparePropertyValueOperation.EQUALS)
                    {
                        // Error since only the equals operation is valid
                        throw new ActionServiceException(
                                MSGID_INVALID_OPERATION,
                                new Object[]{operation.toString(), propertyTypeQName.toString()});
                    }

                    // Use equals to compare the values
                    result = compareValue.equals(propertyValue);
                }
            }
            else
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("Condition Comparator encountered null value for property [" + propertyTypeQName + "]");
                }
            }
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Returning result " + result);
        }

        return result;
    }
}
