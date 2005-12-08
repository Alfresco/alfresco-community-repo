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
