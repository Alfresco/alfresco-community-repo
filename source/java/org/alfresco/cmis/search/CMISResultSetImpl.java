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
package org.alfresco.cmis.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISQueryOptions;
import org.alfresco.cmis.CMISResultSet;
import org.alfresco.cmis.CMISResultSetMetaData;
import org.alfresco.cmis.CMISResultSetRow;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.util.Pair;

/**
 * @author andyh
 */
public class CMISResultSetImpl implements CMISResultSet, Serializable
{
    private static final long serialVersionUID = 2014688399588268994L;

    private Map<String, ResultSet> wrapped;
    
    private LimitBy limitBy;

    CMISQueryOptions options;

    NodeService nodeService;

    Query query;

    CMISDictionaryService cmisDictionaryService;

    DictionaryService alfrescoDictionaryService;

    public CMISResultSetImpl(Map<String, ResultSet> wrapped, CMISQueryOptions options, LimitBy limitBy, NodeService nodeService, Query query,
            CMISDictionaryService cmisDictionaryService, DictionaryService alfrescoDictionaryService)
    {
        this.wrapped = wrapped;
        this.options = options;
        this.limitBy = limitBy;
        this.nodeService = nodeService;
        this.query = query;
        this.cmisDictionaryService = cmisDictionaryService;
        this.alfrescoDictionaryService = alfrescoDictionaryService;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.search.CMISResultSet#close()
     */
    public void close()
    {
        // results sets can be used for more than one selector so we need to keep track of what we have closed
        Set<ResultSet> closed = new HashSet<ResultSet>();
        for (ResultSet resultSet : wrapped.values())
        {
            if (!closed.contains(resultSet))
            {
                resultSet.close();
                closed.add(resultSet);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.search.CMISResultSet#getMetaData()
     */
    public CMISResultSetMetaData getMetaData()
    {
        return new CMISResultSetMetaDataImpl(options, query, limitBy, cmisDictionaryService, alfrescoDictionaryService);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.search.CMISResultSet#getRow(int)
     */
    public CMISResultSetRow getRow(int i)
    {
        return new CMISResultSetRowImpl(this, i, getScores(i), nodeService, getNodeRefs(i), query, cmisDictionaryService);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.search.CMISResultSet#hasMore()
     */
    public boolean hasMore()
    {
        for (ResultSet resultSet : wrapped.values())
        {
            if (resultSet.hasMore())
            {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.search.CMISResultSet#length()
     */
    public int getLength()
    {
        for (ResultSet resultSet : wrapped.values())
        {
            return resultSet.length();
        }
        throw new IllegalStateException();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.search.CMISResultSet#start()
     */
    public int getStart()
    {
        return options.getSkipCount();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<CMISResultSetRow> iterator()
    {
        return new CMISResultSetRowIteratorImpl(this);
    }

    private Map<String, NodeRef> getNodeRefs(int i)
    {
        HashMap<String, NodeRef> refs = new HashMap<String, NodeRef>();
        for (String selector : wrapped.keySet())
        {
            ResultSet rs = wrapped.get(selector);
            refs.put(selector, rs.getNodeRef(i));
        }
        return refs;
    }

    private Map<String, Float> getScores(int i)
    {
        HashMap<String, Float> scores = new HashMap<String, Float>();
        for (String selector : wrapped.keySet())
        {
            ResultSet rs = wrapped.get(selector);
            scores.put(selector, Float.valueOf(rs.getScore(i)));
        }
        return scores;
    }

    public ChildAssociationRef getChildAssocRef(int n)
    {
        NodeRef nodeRef = getNodeRef(n);
        return nodeService.getPrimaryParent(nodeRef);
    }

    public List<ChildAssociationRef> getChildAssocRefs()
    {
        ArrayList<ChildAssociationRef> cars = new ArrayList<ChildAssociationRef>(length());
        for (ResultSetRow row : this)
        {
            cars.add(row.getChildAssocRef());
        }
        return cars;
    }

    public NodeRef getNodeRef(int n)
    {
        Map<String, NodeRef> refs = getNodeRefs(n);
        if (refs.size() == 1)
        {
            return refs.values().iterator().next();
        }
        else if(allNodeRefsEqual(refs))
        {
            return refs.values().iterator().next();
        }
        else       {
            throw new IllegalStateException("Ambiguous selector");
        }
    }

    private boolean allNodeRefsEqual(Map<String, NodeRef> selected)
    {
        NodeRef last = null;
        for (NodeRef current : selected.values())
        {
            if (last == null)
            {
                last = current;
            }
            else
            {
                if (!last.equals(current))
                {
                    return false;
                }
            }
        }
        return true;
    }

    public List<NodeRef> getNodeRefs()
    {
        ArrayList<NodeRef> nodeRefs = new ArrayList<NodeRef>(length());
        for (ResultSetRow row : this)
        {
            nodeRefs.add(row.getNodeRef());
        }
        return nodeRefs;
    }

    public CMISResultSetMetaData getResultSetMetaData()
    {
        return getMetaData();
    }

    public float getScore(int n)
    {
        Map<String, Float> scores = getScores(n);
        if (scores.size() == 1)
        {
            return scores.values().iterator().next();
        }
        else if(allScoresEqual(scores))
        {
            return scores.values().iterator().next();
        }
        else
        {
            throw new IllegalStateException("Ambiguous selector");
        }
    }
    
    private boolean allScoresEqual(Map<String, Float> scores)
    {
        Float last = null;
        for (Float current : scores.values())
        {
            if (last == null)
            {
                last = current;
            }
            else
            {
                if (!last.equals(current))
                {
                    return false;
                }
            }
        }
        return true;
    }

    public int length()
    {
        return getLength();
    }
    
    /**
     * Bulk fetch results in the cache - not supported here
     * 
     * @param bulkFetch
     */
    public boolean setBulkFetch(boolean bulkFetch)
    {
    	return false;
    }

    /**
     * Do we bulk fetch - not supported here
     * 
     * @return - true if we do
     */
    public boolean getBulkFetch()
    {
        return false;
    }

    /**
     * Set the bulk fetch size
     * 
     * @param bulkFetchSize
     */
    public int setBulkFetchSize(int bulkFetchSize)
    {
    	return 0;
    }

    /**
     * Get the bulk fetch size.
     * 
     * @return the fetch size
     */
    public int getBulkFetchSize()
    {
        return 0;
    }
    
    @Override
    public List<Pair<String, Integer>> getFieldFacet(String field)
    {
        return Collections.<Pair<String, Integer>>emptyList();
    }
}
