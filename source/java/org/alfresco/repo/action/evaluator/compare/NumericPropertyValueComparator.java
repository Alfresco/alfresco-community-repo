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
package org.alfresco.repo.action.evaluator.compare;

import java.io.Serializable;

import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;

/**
 * Numeric property value comparator.
 * 
 * @author Roy Wetherall
 */
public class NumericPropertyValueComparator implements PropertyValueComparator
{
    /**
     * I18N message ids
     */
    private static final String MSGID_INVALID_OPERATION = "numeric_property_value_comparator.invalid_operation";
    
    /**
     * @see org.alfresco.repo.action.evaluator.compare.PropertyValueComparator#compare(java.io.Serializable, java.io.Serializable, org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation)
     */
    public boolean compare(
            Serializable propertyValue,
            Serializable compareValue, 
            ComparePropertyValueOperation operation)
    {
        boolean result = false;
        if (operation == null)
        {
            operation = ComparePropertyValueOperation.EQUALS;
        }
        
        // TODO need to check that doing this potential conversion does not cause a problem
        double property = ((Number)propertyValue).doubleValue();
        double compare = ((Number)compareValue).doubleValue();
        
        switch (operation)
        {
            case EQUALS:
            {
                result = (property == compare);
                break;
            }
            case GREATER_THAN:
            {
                result = (property > compare);
                break;                
            }
            case GREATER_THAN_EQUAL:
            {
                result = (property >= compare);
                break;  
            }
            case LESS_THAN:
            {
                result = (property < compare);
                break;  
            }
            case LESS_THAN_EQUAL:
            {
                result = (property <= compare);
                break;  
            }
            default:
            {
                // Raise an invalid operation exception
                throw new ActionServiceException(
                        MSGID_INVALID_OPERATION, 
                        new Object[]{operation.toString()});
            }
        }
        
        return result;
    }

    /**
     * @see org.alfresco.repo.action.evaluator.compare.PropertyValueComparator#registerComparator(org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator)
     */
    public void registerComparator(ComparePropertyValueEvaluator evaluator)
    {
        evaluator.registerComparator(DataTypeDefinition.DOUBLE, this);
        evaluator.registerComparator(DataTypeDefinition.FLOAT, this);
        evaluator.registerComparator(DataTypeDefinition.INT, this);
        evaluator.registerComparator(DataTypeDefinition.LONG, this);
        
    }

}
