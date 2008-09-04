/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis.search;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.cmis.dictionary.CMISDictionaryService;
import org.alfresco.cmis.dictionary.CMISPropertyDefinition;
import org.alfresco.cmis.dictionary.CMISPropertyType;
import org.alfresco.cmis.dictionary.CMISTypeDefinition;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.repo.search.impl.querymodel.impl.functions.PropertyAccessor;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 */
public class CMISResultSetMetaDataImpl implements CMISResultSetMetaData
{
    private CMISQueryOptions options;

    private Query query;

    private Map<String, CMISResultSetColumn> columnMetaData;

    private Map<String, CMISResultSetSelector> selectorMetaData;

    private CMISDictionaryService cmisDictionaryService;

    public CMISResultSetMetaDataImpl(CMISQueryOptions options, Query query, CMISDictionaryService cmisDictionaryService)
    {
        this.options = options;
        this.query = query;

        Map<String, Selector> selectors = query.getSource().getSelectors();
        selectorMetaData = new LinkedHashMap<String, CMISResultSetSelector>();
        for(Selector selector : selectors.values())
        {
            CMISTypeDefinition type = new CMISTypeDefinition(cmisDictionaryService, cmisDictionaryService.getCMISMapping().getCmisTypeId(selector.getType()));
            CMISResultSetSelector smd = new CMISResultSetSelectorImpl(selector.getAlias(), type);
            selectorMetaData.put(smd.getName(), smd);
        }

        List<Column> columns = query.getColumns();
        columnMetaData = new LinkedHashMap<String, CMISResultSetColumn>();

        int i = 0;
        for (Column column : query.getColumns())
        {
            CMISPropertyDefinition propertyDefinition = null;
            CMISPropertyType type = null;
            if (column.getFunction().getName().equals(PropertyAccessor.NAME))
            {
                PropertyArgument arg = (PropertyArgument) column.getFunctionArguments().get(PropertyAccessor.ARG_PROPERTY);
                QName propertyQName = arg.getPropertyName();
                QName typeQname = selectors.get(arg.getSelector()).getType();
                propertyDefinition = new CMISPropertyDefinition(cmisDictionaryService, propertyQName, typeQname);
                type = propertyDefinition.getPropertyType();
            }
            if (type == null)
            {
                type = cmisDictionaryService.getCMISMapping().getPropertyType(column.getFunction().getReturnType());
            }
            CMISResultSetColumn cmd = new CMISResultSetColumnImpl(column.getAlias(), propertyDefinition, type);
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
     * @see org.alfresco.cmis.search.CMISResultSetMetaData#getCoumn(java.lang.String)
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
     * @see org.alfresco.cmis.search.CMISResultSetMetaData#getSelector(java.lang.String)
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

}
