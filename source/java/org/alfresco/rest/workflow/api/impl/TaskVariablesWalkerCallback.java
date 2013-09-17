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
package org.alfresco.rest.workflow.api.impl;

import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.framework.resource.parameters.where.InvalidQueryException;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper.WalkerCallbackAdapter;
import org.alfresco.rest.workflow.api.model.VariableScope;

public class TaskVariablesWalkerCallback extends WalkerCallbackAdapter
{
    private static final String PROPERTY_SCOPE = "scope";
    
    private VariableScope scope = VariableScope.ANY;
    
   @Override
    public void comparison(int type, String propertyName, String propertyValue)
    {
       if (PROPERTY_SCOPE.equals(propertyName)) 
       {
           if (type != WhereClauseParser.EQUALS)
           {
               throw new InvalidQueryException("Only equals is allowed for 'scope' comparison.");
           }
           
           scope = VariableScope.getScopeForValue(propertyValue);
           if (scope == null)
           {
               throw new InvalidQueryException("Invalid value for 'scope' used in query: " + propertyValue + ".");
           }
       }
       else
       {
           throw new InvalidQueryException("Only property 'scope' is allowed in the query.");
       }
    }
   
   public VariableScope getScope()
   {
        return this.scope;
   }

}
