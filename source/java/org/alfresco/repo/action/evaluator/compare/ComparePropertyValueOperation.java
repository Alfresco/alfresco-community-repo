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

/**
 * ComparePropertyValueOperation enum.
 * <p>
 * Contains the operations that can be used when evaluating whether the value of a property
 * matches the value set.
 * <p>
 * Some operations can only be used with specific types.  If a mismatch is encountered an error will
 * be raised.
 */
public enum ComparePropertyValueOperation 
{
    EQUALS,                 // All property types 
    CONTAINS,               // String properties only
    BEGINS,                 // String properties only   
    ENDS,                   // String properties only
    GREATER_THAN,           // Numeric and date properties only
    GREATER_THAN_EQUAL,     // Numeric and date properties only
    LESS_THAN,              // Numeric and date properties only
    LESS_THAN_EQUAL         // Numeric and date properties only
}