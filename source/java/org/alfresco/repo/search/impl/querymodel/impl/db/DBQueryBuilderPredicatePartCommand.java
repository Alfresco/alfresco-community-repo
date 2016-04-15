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
package org.alfresco.repo.search.impl.querymodel.impl.db;

import org.alfresco.repo.search.adaptor.lucene.LuceneFunction;

/**
 * @author Andy
 */
public class DBQueryBuilderPredicatePartCommand
{
    DBQueryBuilderPredicatePartCommandType type;

    String fieldName;

    Object value;

    Object[] values;

    String alias;
    
    private DBQueryBuilderJoinCommandType joinCommandType;

    private LuceneFunction function;
    
    private Long qnameId;

    /**
     * @return the qnameId
     */
    public Long getQnameId()
    {
        return qnameId;
    }

    /**
     * @param qnameId the qnameId to set
     */
    public void setQnameId(Long qnameId)
    {
        this.qnameId = qnameId;
    }
    
    /**
     * @return the joinCommandType
     */
    public String getJoinCommandType()
    {
        return joinCommandType.toString();
    }

    /**
     * @param joinCommandType the joinCommandType to set
     */
    public void setJoinCommandType(DBQueryBuilderJoinCommandType joinCommandType)
    {
        this.joinCommandType = joinCommandType;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type.toString();
    }

    /**
     * @param type the type to set
     */
    public void setType(DBQueryBuilderPredicatePartCommandType type)
    {
        this.type = type;
    }

    /**
     * @return the fieldName
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    /**
     * @return the value
     */
    public Object getValue()
    {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Object value)
    {
        this.value = value;
    }

    /**
     * @return the values
     */
    public Object[] getValues()
    {
        return values;
    }

    /**
     * @param values the values to set
     */
    public void setValues(Object[] values)
    {
        this.values = values;
    }

    /**
     * @return the joinAlias
     */
    public String getAlias()
    {
        return alias;
    }

    /**
     * @param alias the joinAlias to set
     */
    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    /**
     * @return the function
     */
    public LuceneFunction getFunction()
    {
        return function;
    }

    /**
     * @param function the function to set
     */
    public void setFunction(LuceneFunction function)
    {
        this.function = function;
    }

    public String getFieldAndFunction()
    {
        if(function != null)
        {
            if(function == LuceneFunction.LOWER)
            {
                return "LOWER( "+alias +"." +fieldName+") ";
            }
            else if(function == LuceneFunction.UPPER)
            {
                return "UPPER( "+alias +"." +fieldName+") ";
            }
            else
            {
                return alias +"." +fieldName;
            }
        }
        else
        {
            return alias +"." +fieldName;
        }
    }
    
}
