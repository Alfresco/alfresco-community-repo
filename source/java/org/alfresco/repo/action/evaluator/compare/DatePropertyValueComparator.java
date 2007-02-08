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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.action.evaluator.compare;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;

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
    public boolean compare(Serializable propertyValue,
            Serializable compareValue, ComparePropertyValueOperation operation)
    {
        boolean result = false;
        
        if (operation == null)
        {
            operation = ComparePropertyValueOperation.EQUALS;
        }
        
        Date propertyDate = (Date)propertyValue;
        Date compareDate = (Date)compareValue;
        
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

    /**
     * @see org.alfresco.repo.action.evaluator.compare.PropertyValueComparator#registerComparator(org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator)
     */
    public void registerComparator(ComparePropertyValueEvaluator evaluator)
    {
        evaluator.registerComparator(DataTypeDefinition.DATE, this);
        evaluator.registerComparator(DataTypeDefinition.DATETIME, this);        
    }

}
