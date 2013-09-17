/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.workflow.api.model;


public enum VariableScope
{
    LOCAL("local"), GLOBAL("global"), ANY("any");
    
    private String value;
    
    private VariableScope(String value) {
        this.value = value;
    }
    /**
     * @param scopeValue value of the scope, see {@link #getValue()}.
     * @return {@link VariableScope} for the given value. Returns null if the given value is not
     * a valid scope. 
     */
    public static VariableScope getScopeForValue(String scopeValue) {
        for(VariableScope scope : values()) 
        {
            if(scope.getValue().equals(scopeValue)) 
            {
                return scope;
            }
        }
        return null;
    }
    
    public String getValue() {
        return value;
    }
}
