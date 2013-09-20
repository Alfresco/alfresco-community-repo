/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper.WalkerCallbackAdapter;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;

/**
 * Query walker that adds all properties for "equals" comparison to a map. If an
 * unsupported property or comparison operation is encountered, an
 * {@link InvalidArgumentException} is thrown unless the method
 * {@link #handleUnmatchedComparison(int, String, String)} returns true (default
 * implementation returns false).
 * 
 * @author Frederik Heremans
 * @author Tijs Rademakers
 */
public class MapBasedQueryWalker extends WalkerCallbackAdapter
{
    private Set<String> supportedEqualsParameters;

    private Set<String> supportedMatchesParameters;

    private Set<String> supportedGreaterThanParameters;

    private Set<String> supportedGreaterThanOrEqualParameters;

    private Set<String> supportedLessThanParameters;

    private Set<String> supportedLessThanOrEqualParameters;

    private Map<String, String> equalsProperties;

    private Map<String, String> matchesProperties;
    
    private Map<String, String> greaterThanProperties;
    
    private Map<String, String> greaterThanOrEqualProperties;
    
    private Map<String, String> lessThanProperties;
    
    private Map<String, String> lessThanOrEqualProperties;
    
    private List<QueryVariableHolder> variableProperties;
    
    private boolean variablesEnabled;
    
    private NamespaceService namespaceService;
    
    private DictionaryService dictionaryService;

    public MapBasedQueryWalker(Set<String> supportedEqualsParameters, Set<String> supportedMatchesParameters)
    {
        this.supportedEqualsParameters = supportedEqualsParameters;
        this.supportedMatchesParameters = supportedMatchesParameters;
        this.equalsProperties = new HashMap<String, String>();
        this.matchesProperties = new HashMap<String, String>();
    }

    public void setSupportedGreaterThanParameters(Set<String> supportedGreaterThanParameters)
    {
        this.supportedGreaterThanParameters = supportedGreaterThanParameters;
        if (greaterThanProperties == null)
        {
            greaterThanProperties = new HashMap<String, String>();
        }
    }

    public void setSupportedGreaterThanOrEqualParameters(Set<String> supportedGreaterThanOrEqualParameters)
    {
        this.supportedGreaterThanOrEqualParameters = supportedGreaterThanOrEqualParameters;
        if (greaterThanOrEqualProperties == null)
        {
            greaterThanOrEqualProperties = new HashMap<String, String>();
        }
    }

    public void setSupportedLessThanParameters(Set<String> supportedLessThanParameters)
    {
        this.supportedLessThanParameters = supportedLessThanParameters;
        if (lessThanProperties == null)
        {
            lessThanProperties = new HashMap<String, String>();
        }
    }

    public void setSupportedLessThanOrEqualParameters(Set<String> supportedLessThanOrEqualParameters)
    {
        this.supportedLessThanOrEqualParameters = supportedLessThanOrEqualParameters;
        if (lessThanOrEqualProperties == null)
        {
            lessThanOrEqualProperties = new HashMap<String, String>();
        }
    }
    
    public void enableVariablesSupport(NamespaceService namespaceService, DictionaryService dictionaryService)
    {
        variablesEnabled = true;
        if (namespaceService == null)
        {
            throw new IllegalArgumentException("namespace service can't be null");
        }
        if (dictionaryService == null)
        {
            throw new IllegalArgumentException("dictionary service can't be null");
        }
        this.namespaceService = namespaceService;
        this.dictionaryService = dictionaryService;
        variableProperties = new ArrayList<QueryVariableHolder>();
    }
    
    public List<QueryVariableHolder> getVariableProperties() {
        return variableProperties;
    }

    @Override
    public void matches(String property, String value, boolean negated)
    {
        if(negated)
        {
            throw new InvalidArgumentException("Cannot use negated matching for property: " + property); 
        }
        if (variablesEnabled && property.startsWith("variables/")) 
        {
            processVariable(property, value, WhereClauseParser.MATCHES);
        }
        else if (supportedMatchesParameters != null && supportedMatchesParameters.contains(property))
        {
            matchesProperties.put(property, value);
        }
        else
        {
            throw new InvalidArgumentException("Cannot use matching for property: " + property); 
        }
    }
    
    @Override
    public void comparison(int type, String propertyName, String propertyValue)
    {
        boolean throwError = false;
        
        if (variablesEnabled && propertyName.startsWith("variables/")) 
        {
            processVariable(propertyName, propertyValue, type);
        }
        else
        {
            if (type == WhereClauseParser.EQUALS)
            {
                if (supportedEqualsParameters != null && supportedEqualsParameters.contains(propertyName))
                {
                    equalsProperties.put(propertyName, propertyValue);
                }
                else
                {
                    throwError = !handleUnmatchedComparison(type, propertyName, propertyValue);
                }
            }
            else if (type == WhereClauseParser.MATCHES)
            {
                if (supportedMatchesParameters != null && supportedMatchesParameters.contains(propertyName))
                {
                    matchesProperties.put(propertyName, propertyValue);
                }
                else
                {
                    throwError = !handleUnmatchedComparison(type, propertyName, propertyValue);
                }
            }
            else if (type == WhereClauseParser.GREATERTHAN)
            {
                if (supportedGreaterThanParameters != null && supportedGreaterThanParameters.contains(propertyName))
                {
                    greaterThanProperties.put(propertyName, propertyValue);
                }
                else
                {
                    throwError = !handleUnmatchedComparison(type, propertyName, propertyValue);
                }
            }
            else if (type == WhereClauseParser.GREATERTHANOREQUALS)
            {
                if (supportedGreaterThanOrEqualParameters != null && supportedGreaterThanOrEqualParameters.contains(propertyName))
                {
                    greaterThanOrEqualProperties.put(propertyName, propertyValue);
                }
                else
                {
                    throwError = !handleUnmatchedComparison(type, propertyName, propertyValue);
                }
            }
            else if (type == WhereClauseParser.LESSTHAN)
            {
                if (supportedLessThanParameters != null && supportedLessThanParameters.contains(propertyName))
                {
                    lessThanProperties.put(propertyName, propertyValue);
                }
                else
                {
                    throwError = !handleUnmatchedComparison(type, propertyName, propertyValue);
                }
            }
            else if (type == WhereClauseParser.LESSTHANOREQUALS)
            {
                if (supportedLessThanOrEqualParameters != null && supportedLessThanOrEqualParameters.contains(propertyName))
                {
                    lessThanOrEqualProperties.put(propertyName, propertyValue);
                }
                else
                {
                    throwError = !handleUnmatchedComparison(type, propertyName, propertyValue);
                }
            }
            else
            {
                throwError = !handleUnmatchedComparison(type, propertyName, propertyValue);
            }
        }

        if (throwError) { throw new InvalidArgumentException(type + " is not allowed for 'scope' comparison."); }
    }

    public String getProperty(String propertyName, int type)
    {
        if (type == WhereClauseParser.EQUALS)
        {
            return equalsProperties.get(propertyName);
        }
        else if (type == WhereClauseParser.MATCHES)
        {
            return matchesProperties.get(propertyName);
        }
        else if (type == WhereClauseParser.GREATERTHAN && greaterThanProperties != null)
        {
            return greaterThanProperties.get(propertyName);
        }
        else if (type == WhereClauseParser.GREATERTHANOREQUALS && greaterThanOrEqualProperties != null)
        {
            return greaterThanOrEqualProperties.get(propertyName);
        }
        else if (type == WhereClauseParser.LESSTHAN && lessThanProperties != null)
        {
            return lessThanProperties.get(propertyName);
        }
        else if (type == WhereClauseParser.LESSTHANOREQUALS && lessThanOrEqualProperties != null)
        {
            return lessThanOrEqualProperties.get(propertyName);
        }
        else
        {
            throw new IllegalArgumentException("type " + type + " is not supported");
        }
    }

    /**
     * Get the property value, converted to the requested type.
     * 
     * @param parameters used to extract parameter value from
     * @param propertyName name of the parameter
     * @param returnType type of object to return
     * @return the converted parameter value. Null, if the property has no
     *         value.
     * @throws IllegalArgumentException when no conversion for the given
     *             returnType is available or if returnType is null.
     * @throws InvalidArgumentException when conversion to the given type was
     *             not possible due to an error while converting
     */
    @SuppressWarnings("unchecked")
    public <T extends Object> T getProperty(String propertyName, int type, Class<T> returnType)
    {
        if (returnType == null) { throw new IllegalArgumentException("ReturnType cannot be null"); }
        try
        {
            Object result = null;
            String stringValue = getProperty(propertyName, type);
            if (stringValue != null)
            {
                result = ConvertUtils.convert(stringValue, returnType);
                if (result instanceof String)
                {
                    // If a string is returned, no converter has been found
                    throw new IllegalArgumentException("Unable to convert parameter to type: " + returnType.getName());
                }
            }
            return (T) result;
        }
        catch (ConversionException ce)
        {
            // Conversion failed, wrap in Illegal
            throw new InvalidArgumentException("Query property value for '" + propertyName + "' should be a valid "
                    + returnType.getSimpleName());
        }
    }

    @Override
    public void and()
    {
        // We don't need to do anything in this method. However, overriding the
        // method indicates that AND is
        // supported. OR is not supported at the same time.
    }
    
    protected void processVariable(String propertyName, String propertyValue, int type)
    {
        String localPropertyName = propertyName.replaceFirst("variables/", "");
        Object actualValue = null;
        DataTypeDefinition dataTypeDefinition = null;
        // variable scope global is default
        String scopeDef = "global";
        
        // look for variable scope
        if (localPropertyName.contains("local/"))
        {
            scopeDef = "local";
            localPropertyName = localPropertyName.replaceFirst("local/", "");
        }
    
        if (localPropertyName.contains("global/"))
        {
            localPropertyName = localPropertyName.replaceFirst("global/", "");
        }
        
        // look for variable type definition
        if ((propertyValue.contains("_") || propertyValue.contains(":")) && propertyValue.contains(" ")) 
        {
            int indexOfSpace = propertyValue.indexOf(' ');
            if ((propertyValue.contains("_") && indexOfSpace > propertyValue.indexOf("_")) || 
                    (propertyValue.contains(":") && indexOfSpace > propertyValue.indexOf(":")))
            {
                String typeDef = propertyValue.substring(0, indexOfSpace);
                try
                {
                    QName dataType = QName.createQName(typeDef.replace('_', ':'), namespaceService);
                    dataTypeDefinition = dictionaryService.getDataType(dataType);
                    propertyValue = propertyValue.substring(indexOfSpace + 1);
                }
                catch (Exception e)
                {
                    throw new ApiException("Error translating propertyName " + propertyName + " with value " + propertyValue);
                }
            }
        }
        
        if (dataTypeDefinition != null && "java.util.Date".equalsIgnoreCase(dataTypeDefinition.getJavaClassName()))
        {
            // fix for different ISO 8601 Date format classes in Alfresco (org.alfresco.util and Spring Surf)
            actualValue = ISO8601DateFormat.parse(propertyValue);
        }
        else if (dataTypeDefinition != null)
        {
            actualValue = DefaultTypeConverter.INSTANCE.convert(dataTypeDefinition, propertyValue);
        }
        else 
        {
            actualValue = propertyValue;
        }
        
        variableProperties.add(new QueryVariableHolder(localPropertyName, type, actualValue, scopeDef));
    }

    /**
     * Called when unsupported property is encountered or comparison operator
     * other than equals.
     * 
     * @return true, if the comparison is handles successfully. False, if an
     *         exception should be thrown because the comparison can't be
     *         handled.
     */
    protected boolean handleUnmatchedComparison(int type, String propertyName, String propertyValue)
    {
        return false;
    }
    
    public class QueryVariableHolder implements Serializable
    {
        private static final long serialVersionUID = 1L;
        
        private String propertyName;
        private int operator;
        private Object propertyValue;
        private String scope;
        
        public QueryVariableHolder() {}
        
        public QueryVariableHolder(String propertyName, int operator, Object propertyValue, String scope) {
            this.propertyName = propertyName;
            this.operator = operator;
            this.propertyValue = propertyValue;
            this.scope = scope;
        }
        
        public String getPropertyName()
        {
            return propertyName;
        }
        public void setPropertyName(String propertyName)
        {
            this.propertyName = propertyName;
        }
        public int getOperator()
        {
            return operator;
        }
        public void setOperator(int operator)
        {
            this.operator = operator;
        }
        public Object getPropertyValue()
        {
            return propertyValue;
        }
        public void setPropertyValue(Object propertyValue)
        {
            this.propertyValue = propertyValue;
        }
        public String getScope()
        {
            return scope;
        }
        public void setScope(String scope)
        {
            this.scope = scope;
        }
        public boolean isGlobalScope() {
            return "global".equals(scope);
        }
    }
}
