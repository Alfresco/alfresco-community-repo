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
package org.alfresco.repo.action.evaluator.compare;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.util.ISO8601DateFormat;

/**
 * Date property value comparator
 * 
 * @author Roy Wetherall
 */
public class DatePropertyValueComparator implements PropertyValueComparator
{
    /**
     * I18N message ids
     */
    private static final String MSGID_INVALID_OPERATION = "date_property_value_comparator.invalid_operation";
    
    /**
     * @see org.alfresco.repo.action.evaluator.compare.PropertyValueComparator#compare(java.io.Serializable, java.io.Serializable, org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation)
     */
    public boolean compare(Serializable propertyValue, Serializable compareValue, ComparePropertyValueOperation operation)
    {
        boolean result = false;
        
        if (operation == null)
        {
            operation = ComparePropertyValueOperation.EQUALS;
        }
        
        Date propertyDate = getDate(propertyValue);
        Date compareDate = getDate(compareValue);
        
        switch (operation)
        {
            case EQUALS:
            {
                result = propertyDate.equals(compareDate);
                break;
            }
            case LESS_THAN:
            {
                result = propertyDate.before(compareDate);
                break;
            }
            case LESS_THAN_EQUAL:
            {
                result = (propertyDate.equals(compareDate) || propertyDate.before(compareDate));
                break;
            }
            case GREATER_THAN:
            {
                result = propertyDate.after(compareDate);
                break;
            }
            case GREATER_THAN_EQUAL:
            {
                result = (propertyDate.equals(compareDate) || propertyDate.after(compareDate));
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

    private Date getDate(Serializable value) 
    {
        if(value instanceof Date)
        {
        	return (Date) value;
        } 
        else if(value instanceof String)
        {
        	return ISO8601DateFormat.parse((String) value);
        } 
    	throw new AlfrescoRuntimeException("Parameter 'compareValue' must be of type java.util.Date!");
	}

	/**
     * @see org.alfresco.repo.action.evaluator.compare.PropertyValueComparator#registerComparator(org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator)
     */
    public void registerComparator(ComparePropertyValueEvaluator evaluator)
    {
        evaluator.registerComparator(DataTypeDefinition.DATE, this);
        evaluator.registerComparator(DataTypeDefinition.DATETIME, this);        
    }

}
