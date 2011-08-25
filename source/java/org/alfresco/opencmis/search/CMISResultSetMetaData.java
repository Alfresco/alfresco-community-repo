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
package org.alfresco.opencmis.search;

import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.dictionary.PropertyDefinitionWrapper;
import org.alfresco.opencmis.dictionary.TypeDefinitionWrapper;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.repo.search.impl.querymodel.impl.functions.PropertyAccessor;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetType;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;

/**
 * @author andyh
 */
public class CMISResultSetMetaData implements ResultSetMetaData
{
    private CMISQueryOptions options;
    private SearchParameters searchParams;
    private LimitBy limitBy;

    private Map<String, CMISResultSetColumn> columnMetaData;

    private Map<String, CMISResultSetSelector> selectorMetaData;

    public CMISResultSetMetaData(CMISQueryOptions options, Query query, LimitBy limitBy,
            CMISDictionaryService cmisDictionaryService, DictionaryService alfrescoDictionaryService)
    {
        this.options = options;
        this.searchParams = new SearchParameters(options);
        this.limitBy = limitBy;
        Map<String, Selector> selectors = query.getSource().getSelectors();
        selectorMetaData = new LinkedHashMap<String, CMISResultSetSelector>();
        for (Selector selector : selectors.values())
        {
            TypeDefinitionWrapper type = cmisDictionaryService.findTypeForClass(selector.getType());
            CMISResultSetSelector smd = new CMISResultSetSelector(selector.getAlias(), type);
            selectorMetaData.put(smd.getName(), smd);
        }

        columnMetaData = new LinkedHashMap<String, CMISResultSetColumn>();
        for (Column column : query.getColumns())
        {
            PropertyDefinitionWrapper propertyDefinition = null;
            PropertyType type = null;
            QName alfrescoPropertyQName = null;
            QName alfrescoDataTypeQName = null;
            if (column.getFunction().getName().equals(PropertyAccessor.NAME))
            {
                PropertyArgument arg = (PropertyArgument) column.getFunctionArguments().get(
                        PropertyAccessor.ARG_PROPERTY);
                String propertyName = arg.getPropertyName();
                alfrescoPropertyQName = QName.createQName(propertyName);
                PropertyDefinition alfPropDef = alfrescoDictionaryService.getProperty(alfrescoPropertyQName);
                if (alfPropDef == null)
                {
                    alfrescoPropertyQName = null;
                } else
                {
                    alfrescoDataTypeQName = alfPropDef.getDataType().getName();
                }
                propertyDefinition = cmisDictionaryService.findProperty(propertyName);
                type = propertyDefinition.getPropertyDefinition().getPropertyType();
            }
            if (type == null)
            {
                type = cmisDictionaryService.findDataType(column.getFunction().getReturnType());
            }
            if (alfrescoDataTypeQName == null)
            {
                alfrescoDataTypeQName = cmisDictionaryService.findAlfrescoDataType(type);
            }
            CMISResultSetColumn cmd = new CMISResultSetColumn(column.getAlias(), propertyDefinition, type,
                    alfrescoPropertyQName, alfrescoDataTypeQName);
            columnMetaData.put(cmd.getName(), cmd);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSetMetaData#getColumnNames()
     */
    public String[] getColumnNames()
    {
        return columnMetaData.keySet().toArray(new String[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSetMetaData#getColumns()
     */
    public CMISResultSetColumn[] getColumns()
    {
        return columnMetaData.values().toArray(new CMISResultSetColumn[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.cmis.search.CMISResultSetMetaData#getCoumn(java.lang.String)
     */
    public CMISResultSetColumn getColumn(String name)
    {
        return columnMetaData.get(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSetMetaData#getQueryOptions()
     */
    public CMISQueryOptions getQueryOptions()
    {
        return options;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.cmis.search.CMISResultSetMetaData#getSelector(java.lang.
     * String)
     */
    public CMISResultSetSelector getSelector(String name)
    {
        return selectorMetaData.get(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSetMetaData#getSelectorNames()
     */
    public String[] getSelectorNames()
    {
        return selectorMetaData.keySet().toArray(new String[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSetMetaData#getSelectors()
     */
    public CMISResultSetSelector[] getSelectors()
    {
        return selectorMetaData.values().toArray(new CMISResultSetSelector[0]);
    }

    public LimitBy getLimitedBy()
    {
        return limitBy;
    }

    public PermissionEvaluationMode getPermissionEvaluationMode()
    {
        throw new UnsupportedOperationException();
    }

    public ResultSetType getResultSetType()
    {
        return ResultSetType.COLUMN_AND_NODE_REF;
    }

    public SearchParameters getSearchParameters()
    {
        return searchParams;
    }

}
