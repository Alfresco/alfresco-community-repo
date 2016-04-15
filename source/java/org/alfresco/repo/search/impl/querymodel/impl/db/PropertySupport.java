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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.search.adaptor.lucene.LuceneFunction;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

public class PropertySupport implements DBQueryBuilderComponent
{
    Pair<Long, QName> pair;

    private String value;

    private String[] values;

    private DBQueryBuilderJoinCommandType joinCommandType = DBQueryBuilderJoinCommandType.PROPERTY;

    private QName propertyQName;
    
    private DataTypeDefinition propertyDataType;

    private String fieldName;

    String alias;

    DBQueryBuilderPredicatePartCommandType commandType;

    LuceneFunction luceneFunction;

    private boolean leftOuter;
    
    /**
     * @param pair
     *            the pair to set
     */
    public void setPair(Pair<Long, QName> pair)
    {
        this.pair = pair;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    /**
     * @param values
     *            the values to set
     */
    public void setValues(String[] values)
    {
        this.values = values;
    }

  
    /**
     * @param joinCommandType the joinCommandType to set
     */
    public void setJoinCommandType(DBQueryBuilderJoinCommandType joinCommandType)
    {
        this.joinCommandType = joinCommandType;
    }

    /**
     * @param propertyQName
     *            the propertyQName to set
     */
    public void setPropertyQName(QName propertyQName)
    {
        this.propertyQName = propertyQName;
    }

    /**
     * @param fieldName
     *            the fieldName to set
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    /**
     * @param commandType
     *            the commandType to set
     */
    public void setCommandType(DBQueryBuilderPredicatePartCommandType commandType)
    {
        this.commandType = commandType;
    }
    
    /**
     * @param propertyDataType the propertyDataType to set
     */
    public void setPropertyDataType(DataTypeDefinition propertyDataType)
    {
        this.propertyDataType = propertyDataType;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#isSupported()
     */
    @Override
    public boolean isSupported()
    {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#prepare(org.alfresco.service.namespace
     * .NamespaceService, org.alfresco.service.cmr.dictionary.DictionaryService,
     * org.alfresco.repo.domain.qname.QNameDAO, org.alfresco.repo.domain.node.NodeDAO, java.util.Set, java.util.Map,
     * org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext)
     */
    @Override
    public void prepare(NamespaceService namespaceService, DictionaryService dictionaryService, QNameDAO qnameDAO, NodeDAO nodeDAO, TenantService tenantService, Set<String> selectors,
            Map<String, Argument> functionArgs, FunctionEvaluationContext functionContext, boolean supportBooleanFloatAndDouble)
    {

    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildJoins(java.util.Map,
     * java.util.List)
     */
    @Override
    public void buildJoins(Map<QName, DBQueryBuilderJoinCommand> singleJoins, List<DBQueryBuilderJoinCommand> multiJoins)
    {
        // JOIN is only required for ordering - predicts are done via semi-join
        alias = "PROP";
        if(commandType == DBQueryBuilderPredicatePartCommandType.ORDER)
        {
            DBQueryBuilderJoinCommand join = singleJoins.get(propertyQName);
            if (join == null)
            {
                if (pair != null)
                {
                    join = new DBQueryBuilderJoinCommand();
                    alias = "PROP_" + singleJoins.size();
                    join.setAlias(alias);
                    join.setOuter(leftOuter);
                    join.setType(joinCommandType);
                    join.setQnameId(pair.getFirst());
                    singleJoins.put(propertyQName, join);
                }
                else
                {
                    // there is no value for this property in the DB
                }
            }

            if(join != null)
            {
                alias = join.getAlias();
                if(leftOuter)
                {
                    join.setOuter(true);
                }
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildPredicateCommands(java.util
     * .List)
     */
    @Override
    public void buildPredicateCommands(List<DBQueryBuilderPredicatePartCommand> predicatePartCommands)
    {   
        if (pair == null)
        {
            DBQueryBuilderPredicatePartCommand command = new DBQueryBuilderPredicatePartCommand();
            command.setJoinCommandType(DBQueryBuilderJoinCommandType.NODE);
            switch(joinCommandType)
            {
            case NODE:
                command.setAlias("node");
                command.setType(commandType);
                command.setFieldName(fieldName);
                command.setValue(value);
                command.setValues(values);
                command.setFunction(luceneFunction);
                predicatePartCommands.add(command);
                break;
            case PROPERTY:
                command.setType(commandType.propertyNotFound());
                predicatePartCommands.add(command);
                break;
            case CONTENT_MIMETYPE:
            case CONTENT_URL:
            default:
                command.setType(commandType.propertyNotFound());
                predicatePartCommands.add(command);
                break;
            }
   
        }
        else
        {
            DBQueryBuilderPredicatePartCommand command = new DBQueryBuilderPredicatePartCommand();
            command.setJoinCommandType(joinCommandType);
            command.setQnameId(pair.getFirst());
            switch(joinCommandType)
            {
            case NODE:
                command.setAlias("node");
                command.setType(commandType);
                command.setValue(value);
                command.setValues(values);
                break;
            case PROPERTY:
                command.setAlias(alias);
                command.setType(commandType);
                if(propertyDataType != null)
                {
                    if (propertyDataType.getName().equals(DataTypeDefinition.ASSOC_REF))
                    {
                        command.setValue(value);
                        command.setValues(values);
                    }
                    else if (propertyDataType.getName().equals(DataTypeDefinition.CATEGORY))
                    {
                        command.setValue(value);
                        command.setValues(values);
                    }
                    else if (propertyDataType.getName().equals(DataTypeDefinition.DATE))
                    {
                        command.setValue(value);
                        command.setValues(values);
                    }
                    else if (propertyDataType.getName().equals(DataTypeDefinition.DATETIME))
                    {
                        command.setValue(value);
                        command.setValues(values);
                    }
                    else if (propertyDataType.getName().equals(DataTypeDefinition.LOCALE))
                    {
                        command.setValue(value);
                        command.setValues(values);
                    }
                    else if (propertyDataType.getName().equals(DataTypeDefinition.MLTEXT))
                    {
                        command.setValue(value);
                        command.setValues(values);
                    }
                    else if (propertyDataType.getName().equals(DataTypeDefinition.NODE_REF))
                    {
                        command.setValue(value);
                        command.setValues(values);
                    }
                    else if (propertyDataType.getName().equals(DataTypeDefinition.PERIOD))
                    {
                        command.setValue(value);
                        command.setValues(values);
                    }
                    else if (propertyDataType.getName().equals(DataTypeDefinition.QNAME))
                    {
                        command.setValue(value);
                        command.setValues(values);
                    }
                    else
                    {
                        command.setValue(DefaultTypeConverter.INSTANCE.convert(propertyDataType, value));
                        Collection<?> collection = DefaultTypeConverter.INSTANCE.convert(propertyDataType, values);
                        command.setValues(collection == null ? null : collection.toArray());
                    }
                }
                else
                {
                    command.setValue(value);
                    command.setValues(values);
                }
                break;
            case CONTENT_MIMETYPE:
                command.setAlias(alias);
                command.setType(commandType);
                
                // Good for order and predicates
                command.setValue(value);
                command.setValues(values);
                
                break;
            case CONTENT_URL:
                command.setAlias(alias);
                command.setType(commandType);
                if(commandType == DBQueryBuilderPredicatePartCommandType.ORDER)
                {
                    command.setValue(value);
                    command.setValues(values);
                }
                else
                {
                    command.setValue(DefaultTypeConverter.INSTANCE.convert(Long.class, value));
                    command.setValues(values == null ? null : DefaultTypeConverter.INSTANCE.convert(Integer.class,  Arrays.asList(values)).toArray(new Integer[]{}));
                }
                break;
            default:                
                command.setType(commandType.propertyNotFound());
                command.setValue(value);
                command.setValues(values);
                break;
            }
            
            command.setFieldName(fieldName);
            command.setFunction(luceneFunction);
            predicatePartCommands.add(command);
        }       
        
    }
 

    /**
     * @param luceneFunction LuceneFunction
     */
    public void setLuceneFunction(LuceneFunction luceneFunction)
    {
        this.luceneFunction = luceneFunction;
    }
    
    /**
     * @param leftOuter boolean
     */
    public void setLeftOuter(boolean leftOuter)
    {
        this.leftOuter = leftOuter;
    }

}