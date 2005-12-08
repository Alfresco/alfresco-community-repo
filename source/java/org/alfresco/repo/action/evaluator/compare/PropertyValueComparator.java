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

/**
 * Property value comparator interface
 * 
 * @author Roy Wetherall
 */
public interface PropertyValueComparator
{
    /**
     * Callback method to register this comparator with the evaluator.
     * 
     * @param evaluator     the compare property value evaluator
     */
    void registerComparator(ComparePropertyValueEvaluator evaluator);
    
    /**
     * Compares the value of a property with the compare value, using the operator passed.
     * 
     * @param propertyValue     the property value
     * @param compareValue      the compare value
     * @param operation         the operation used to compare the two values
     * @return                  the result of the comparision, true if successful false otherwise
     */
    boolean compare(
            Serializable propertyValue,             
            Serializable compareValue, 
            ComparePropertyValueOperation operation);
}
