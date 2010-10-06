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
package org.alfresco.repo.cmis.rest;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.alfresco.cmis.CMISPropertyDefinition;
import org.alfresco.cmis.CMISResultSet;
import org.alfresco.cmis.CMISResultSetColumn;
import org.alfresco.cmis.CMISResultSetMetaData;
import org.alfresco.cmis.CMISResultSetRow;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.repo.web.util.paging.Cursor;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;

import freemarker.core.Environment;
import freemarker.template.SimpleCollection;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateModelException;


/**
 * CMIS Result Set for use in Freemarker
 * 
 * @author davidc
 */
public class CMISTemplateResultSet implements Serializable
{
    private static final long serialVersionUID = 2245418238171563934L;
    
    private CMISResultSet resultSet;
    private Cursor cursor;
    private ServiceRegistry serviceRegistry;
    private TemplateImageResolver imageResolver;
    
    /**
     * Construct
     * 
     * @param resultSet
     * @param serviceRegistry
     * @param imageResolver
     */
    public CMISTemplateResultSet(CMISResultSet resultSet, Cursor cursor, ServiceRegistry serviceRegistry, TemplateImageResolver imageResolver)
    {
        this.resultSet = resultSet;
        this.cursor = cursor;
        this.serviceRegistry = serviceRegistry;
        this.imageResolver = imageResolver;
    }

    /**
     * @return  result set meta-data
     */
    public CMISResultSetMetaData getMetaData()
    {
        return resultSet.getMetaData();
    }

    /**
     * @return  result set length
     */
    public int getLength()
    {
        return resultSet.getLength();
    }

    /**
     * @return  start index within full result set
     */
    public int getStart()
    {
        return resultSet.getStart();
    }

    /**
     * @return  selectors
     */
    public String[] getSelectors()
    {
        return resultSet.getMetaData().getSelectorNames();
    }
    
    /**
     * @return  result set rows
     * @throws TemplateModelException
     */
    public TemplateCollectionModel getRows()
        throws TemplateModelException
    {
        return new SimpleCollection(new TemplateIterator(resultSet.iterator()), Environment.getCurrentEnvironment().getObjectWrapper());
    }
    
    
    /**
     * Result Set Iterator
     * 
     * @author davidc
     */
    public class TemplateIterator implements Iterator<TemplateIterator.TemplateRow>
    {
        private Iterator<CMISResultSetRow> iter;
        private int idx = 0;

        /**
         * Construct
         * 
         * @param iter
         */
        public TemplateIterator(Iterator<CMISResultSetRow> iter)
        {
            this.iter = iter; 
        }
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext()
        {
            return (cursor == null || idx < cursor.getRowCount()) && iter.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public TemplateRow next()
        {
            if (!hasNext())
            {
                throw new NoSuchElementException();
            }
            try
            {
                return new TemplateRow(iter.next());
            }
            finally
            {
                idx++;
            }
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove()
        {
            iter.remove();
        }

        
        /**
         * Template Row
         */
        public class TemplateRow
        {
            private CMISResultSetRow row;
            private Map<String, TemplateNode> nodes = null;
            
            /**
             * Construct
             * 
             * @param row
             */
            public TemplateRow(CMISResultSetRow row)
            {
                this.row = row;
            }
            
            /**
             * @return  a map of serializable column values with the column name as the key
             */
            public Map<String, Serializable> getValues()
            {
                return row.getValues();
            }

            /**
             * @return  nodes associated with row
             */
            public Collection<TemplateNode> getNodes()
            {
                Map<String, TemplateNode> nodes = buildNodes();
                return nodes.size() == 0 ? null : nodes.values();
            }

            /**
             * @return  node (if there is only a single node associated with the row), otherwise null
             */
            public TemplateNode getNode()
            {
                try
                {
                    NodeRef nodeRef = row.getNodeRef();
                    return new TemplateNode(nodeRef, serviceRegistry, imageResolver);
                }
                catch(UnsupportedOperationException e) {}
                return null;
            }
            
            /**
             * Builds a map of Template Nodes for the nodes associated with this row
             * 
             * @return  templates nodes indexed by selector
             */
            private Map<String, TemplateNode> buildNodes()
            {
                if (nodes == null)
                {
                    Map<String, NodeRef> nodeRefs = row.getNodeRefs();
                    if (nodeRefs == null || nodeRefs.size() == 0)
                    {
                        nodes = Collections.emptyMap();
                    }
                    else
                    {
                        HashMap<String, TemplateNode> templateNodes = new HashMap<String, TemplateNode>();
                        for (Map.Entry<String, NodeRef> entry : nodeRefs.entrySet())
                        {
                            templateNodes.put(entry.getKey(), new TemplateNode(entry.getValue(), serviceRegistry, imageResolver));
                        }
                        nodes = templateNodes;
                    }
                }
                return nodes;
            }

            /**
             * Gets column type for specified column name
             * 
             * @param colName  column name
             * @return  column type
             */
            public String getColumnType(String colName)
            {
                CMISResultSetColumn col = resultSet.getMetaData().getColumn(colName);
                return col == null ? null : col.getCMISDataType().getLabel();
            }

            /**
             * Gets property definition for specified column name
             * 
             * @param colName  column name
             * @return  property definition (or null, if not applicable)
             */
            public CMISPropertyDefinition getPropertyDefinition(String colName)
            {
                CMISResultSetColumn col = resultSet.getMetaData().getColumn(colName);
                return col == null ? null : col.getCMISPropertyDefinition();
            }
            
            /**
             * Gets node for specified selector
             * 
             * @param selector  
             * @return  template node
             */
            public TemplateNode getSelectorNode(String selector)
            {
                return nodes.get(selector);
            }
            
            /**
             * @return  overall score
             */
            public float getScore()
            {
                return row.getScore();
            }
            
            /**
             * @return  a map of selector name to score
             */
            public Float getSelectorScore(String selector)
            {
                return row.getScore(selector);
            }
            
            /**
             * NOTE: If you want the overall position in paged results you have to add the skipCount for the result set
             *  
             * @return  row index
             */
            public int getIndex()
            {
                return row.getIndex();
            }
        }
    }
}
