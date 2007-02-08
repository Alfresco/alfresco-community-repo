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
