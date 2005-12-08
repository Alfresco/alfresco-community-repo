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
