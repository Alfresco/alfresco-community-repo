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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISResultSet;
import org.alfresco.cmis.CMISResultSetMetaData;
import org.alfresco.cmis.CMISResultSetRow;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.repo.search.results.ResultSetSPIWrapper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 */
public class CMISResultSetRowImpl implements CMISResultSetRow
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

    private Query query;

    private CMISDictionaryService cmisDictionaryService;

    public CMISResultSetRowImpl(CMISResultSet resultSet, int index, Map<String, Float> scores, NodeService nodeService, Map<String, NodeRef> nodeRefs, Query query, CMISDictionaryService cmisDictionaryService)
    {
        this.resultSet = resultSet;
        this.index = index;
        this.scores = scores;
        this.nodeService = nodeService;
        this.nodeRefs = nodeRefs;
        this.query = query;
        this.cmisDictionaryService = cmisDictionaryService;
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
        for (Column column : query.getColumns())
        {
            if (column.getAlias().equals(columnName))
            {
                CmisFunctionEvaluationContext context = new CmisFunctionEvaluationContext();
                context.setCmisDictionaryService(cmisDictionaryService);
                context.setNodeRefs(nodeRefs);
                context.setNodeService(nodeService);
                context.setScores(scores);
                context.setScore(getScore());
                return column.getFunction().getValue(column.getFunctionArguments(), context);
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
        }
        throw new UnsupportedOperationException("Ambiguous selector");
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
