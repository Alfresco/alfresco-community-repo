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
