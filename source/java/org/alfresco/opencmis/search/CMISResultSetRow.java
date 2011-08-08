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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.repo.search.impl.querymodel.impl.functions.PropertyAccessor;
import org.alfresco.repo.search.results.ResultSetSPIWrapper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 */
public class CMISResultSetRow implements ResultSetRow
{
    /**
     * The containing result set
     */
    private CMISResultSet resultSet;

    /**
     * The current position in the containing result set
     */
    private int index;

    private Map<String, Float> scores;

    private NodeService nodeService;

    private Map<String, NodeRef> nodeRefs;
    
    private Map<NodeRef, CMISNodeInfo> nodeInfos;

    private Query query;

    private CMISDictionaryService cmisDictionaryService;

    public CMISResultSetRow(CMISResultSet resultSet, int index, Map<String, Float> scores, NodeService nodeService,
            Map<String, NodeRef> nodeRefs, Map<NodeRef, CMISNodeInfo> nodeInfos, Query query,
            CMISDictionaryService cmisDictionaryService)
    {
        this.resultSet = resultSet;
        this.index = index;
        this.scores = scores;
        this.nodeService = nodeService;
        this.nodeRefs = nodeRefs;
        this.query = query;
        this.cmisDictionaryService = cmisDictionaryService;
        this.nodeInfos = nodeInfos;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSetRow#getIndex()
     */
    public int getIndex()
    {
        return index;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSetRow#getResultSet()
     */
    public ResultSet getResultSet()
    {
        return new ResultSetSPIWrapper<CMISResultSetRow, CMISResultSetMetaData>(resultSet);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSetRow#getScore()
     */
    public float getScore()
    {
        float count = 0;
        float overall = 0;
        for (Float score : scores.values())
        {
            overall = (overall * (count / (count + 1.0f))) + (score / (count + 1.0f));
        }
        return overall;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSetRow#getScore(java.lang.String)
     */
    public float getScore(String selectorName)
    {
        return scores.get(selectorName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSetRow#getScores()
     */
    public Map<String, Float> getScores()
    {
        return scores;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSetRow#getScore(java.lang.String)
     */
    public NodeRef getNodeRef(String selectorName)
    {
        return nodeRefs.get(selectorName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSetRow#getScores()
     */
    public Map<String, NodeRef> getNodeRefs()
    {
        return nodeRefs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSetRow#getValue(java.lang.String)
     */
    public Serializable getValue(String columnName)
    {
        CmisFunctionEvaluationContext context = new CmisFunctionEvaluationContext();
        context.setCmisDictionaryService(cmisDictionaryService);
        context.setNodeRefs(nodeRefs);
        context.setNodeInfos(nodeInfos);
        context.setNodeService(nodeService);
        context.setScores(scores);
        context.setScore(getScore());
        for (Column column : query.getColumns())
        {
            if (column.getAlias().equals(columnName))
            {
                return column.getFunction().getValue(column.getFunctionArguments(), context);
            }
            // Special case for one selector - ignore any table aliases
            // also allows look up direct and not by alias
            // Perhaps we should add the duplicates instead
            // TODO: check SQL 92 for single alias table behaviour for selectors
            if (nodeRefs.size() == 1)
            {
                if (column.getFunction().getName().equals(PropertyAccessor.NAME))
                {
                    PropertyArgument arg = (PropertyArgument) column.getFunctionArguments().get(
                            PropertyAccessor.ARG_PROPERTY);
                    String propertyName = arg.getPropertyName();
                    if (propertyName.equals(columnName))
                    {
                        return column.getFunction().getValue(column.getFunctionArguments(), context);
                    }
                    StringBuilder builder = new StringBuilder();
                    builder.append(arg.getSelector()).append(".").append(propertyName);
                    propertyName = builder.toString();
                    if (propertyName.equals(columnName))
                    {
                        return column.getFunction().getValue(column.getFunctionArguments(), context);
                    }
                }
            } else
            {
                if (column.getFunction().getName().equals(PropertyAccessor.NAME))
                {
                    PropertyArgument arg = (PropertyArgument) column.getFunctionArguments().get(
                            PropertyAccessor.ARG_PROPERTY);
                    StringBuilder builder = new StringBuilder();
                    builder.append(arg.getSelector()).append(".").append(arg.getPropertyName());
                    String propertyName = builder.toString();
                    if (propertyName.equals(columnName))
                    {
                        return column.getFunction().getValue(column.getFunctionArguments(), context);
                    }
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.search.CMISResultSetRow#getValues()
     */
    public Map<String, Serializable> getValues()
    {
        LinkedHashMap<String, Serializable> answer = new LinkedHashMap<String, Serializable>();
        for (String column : resultSet.getMetaData().getColumnNames())
        {
            answer.put(column, getValue(column));
        }
        return answer;
    }

    public CMISResultSet getCMISResultSet()
    {
        return resultSet;
    }

    public ChildAssociationRef getChildAssocRef()
    {
        NodeRef nodeRef = getNodeRef();
        return nodeService.getPrimaryParent(nodeRef);
    }

    public NodeRef getNodeRef()
    {
        if (nodeRefs.size() == 1)
        {
            return nodeRefs.values().iterator().next();
        } else if (allNodeRefsEqual(nodeRefs))
        {
            return nodeRefs.values().iterator().next();
        }
        throw new UnsupportedOperationException("Ambiguous selector");
    }

    private boolean allNodeRefsEqual(Map<String, NodeRef> selected)
    {
        NodeRef last = null;
        for (NodeRef current : selected.values())
        {
            if (last == null)
            {
                last = current;
            } else
            {
                if (!last.equals(current))
                {
                    return false;
                }
            }
        }
        return true;
    }

    public QName getQName()
    {
        return getChildAssocRef().getQName();
    }

    public Serializable getValue(QName qname)
    {
        throw new UnsupportedOperationException();
    }

}
