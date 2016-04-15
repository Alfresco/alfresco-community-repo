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
     * @return                  the result of the comparison, true if successful false otherwise
     */
    boolean compare(
            Serializable propertyValue,             
            Serializable compareValue, 
            ComparePropertyValueOperation operation);
}
