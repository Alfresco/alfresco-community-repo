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
package org.alfresco.repo.search.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.search.SearcherException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Sorted results
 * @author andyh
 *
 */
public class SortedResultSet implements ResultSet
{
    ArrayList<NodeRefAndScore> nodeRefsAndScores;

    NodeService nodeService;

    SearchParameters searchParameters;

    ResultSet resultSet;

    /**
     * Source and resources required to sort
     * @param resultSet
     * @param nodeService
     * @param searchParameters
     * @param namespacePrefixResolver
     */
    public SortedResultSet(ResultSet resultSet, NodeService nodeService, SearchParameters searchParameters, NamespacePrefixResolver namespacePrefixResolver)
    {
        this.nodeService = nodeService;
        this.searchParameters = searchParameters;
        this.resultSet = resultSet;

        nodeRefsAndScores = new ArrayList<NodeRefAndScore>(resultSet.length());
        for (ResultSetRow row : resultSet)
        {
            nodeRefsAndScores.add(new NodeRefAndScore(row.getNodeRef(), row.getScore()));
        }
        ArrayList<AttributeOrder> order = new ArrayList<AttributeOrder>();
        for (SortDefinition sd : searchParameters.getSortDefinitions())
        {
            switch (sd.getSortType())
            {
            case FIELD:
                String field = sd.getField();
                if (field.startsWith("@"))
                {
                    QName qname = expandAttributeFieldName(field, namespacePrefixResolver);
                    order.add(new AttributeOrder(qname, sd.isAscending()));
                }
                break;
            case DOCUMENT:
                // ignore
                break;
            case SCORE:
                // ignore
                break;
            }
        }

        orderNodes(nodeRefsAndScores, order);

    }

    public void close()
    {
        resultSet.close();
    }

    public ChildAssociationRef getChildAssocRef(int n)
    {
        return nodeService.getPrimaryParent(nodeRefsAndScores.get(n).nodeRef);
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
        return nodeRefsAndScores.get(n).nodeRef;
    }

    public ResultSetMetaData getResultSetMetaData()
    {
        return resultSet.getResultSetMetaData();
    }

    public ResultSetRow getRow(int i)
    {
        if (i < length())
        {
            return new SortedResultSetRow(this, i);
        }
        else
        {
            throw new SearcherException("Invalid row");
        }
    }

    public float getScore(int n)
    {
        return nodeRefsAndScores.get(n).score;
    }

    public int length()
    {
        return nodeRefsAndScores.size();
    }

    public Iterator<ResultSetRow> iterator()
    {
        return new SortedResultSetRowIterator(this);
    }

    private void orderNodes(List<NodeRefAndScore> answer, List<AttributeOrder> order)
    {
        Collections.sort(answer, new NodeRefAndScoreComparator(nodeService, order));
    }

    private QName expandAttributeFieldName(String field, NamespacePrefixResolver namespacePrefixResolver)
    {
        QName qname;
        // Check for any prefixes and expand to the full uri
        if (field.charAt(1) != '{')
        {
            int colonPosition = field.indexOf(':');
            if (colonPosition == -1)
            {
                // use the default namespace
                qname = QName.createQName(NamespaceService.DEFAULT_URI, field.substring(1));
            }
            else
            {
                // find the prefix
                qname = QName.createQName(field.substring(1, colonPosition), field.substring(colonPosition + 1), namespacePrefixResolver);
            }
        }
        else
        {
            qname = QName.createQName(field.substring(1));
        }
        return qname;
    }

    static class NodeRefAndScoreComparator implements Comparator<NodeRefAndScore>
    {
        List<AttributeOrder> order;

        NodeService nodeService;

        NodeRefAndScoreComparator(NodeService nodeService, List<AttributeOrder> order)
        {
            this.nodeService = nodeService;
            this.order = order;
        }

        @SuppressWarnings("unchecked")
        public int compare(NodeRefAndScore n1, NodeRefAndScore n2)
        {
            // Treat missing nodes as null for comparison
            for (AttributeOrder attributeOrder : order)
            {
                Serializable o1;
                try
                {
                    o1 = nodeService.getProperty(n1.nodeRef, attributeOrder.attribute);
                }
                catch(InvalidNodeRefException inre)
                {
                    o1 = null;
                }
                Serializable o2;
                try
                { 
                    o2 = nodeService.getProperty(n2.nodeRef, attributeOrder.attribute);
                }
                catch(InvalidNodeRefException inre)
                {
                    o2 = null;
                }

                if (o1 == null)
                {
                    if (o2 == null)
                    {
                        continue;
                    }
                    else
                    {
                        return attributeOrder.ascending ? -1 : 1;
                    }
                }
                else
                {
                    if (o2 == null)
                    {
                        return attributeOrder.ascending ? 1 : -1;
                    }
                    else
                    {
                        if ((o1 instanceof Comparable) && (o2 instanceof Comparable))
                        {
                            return (attributeOrder.ascending ? 1 : -1) * ((Comparable) o1).compareTo((Comparable) o2);
                        }
                        else
                        {
                            continue;
                        }
                    }
                }

            }
            return 0;
        }
    }

    private static class AttributeOrder
    {
        QName attribute;

        boolean ascending;

        AttributeOrder(QName attribute, boolean ascending)
        {
            this.attribute = attribute;
            this.ascending = ascending;
        }
    }

    private static class NodeRefAndScore
    {
        NodeRef nodeRef;

        float score;

        NodeRefAndScore(NodeRef nodeRef, float score)
        {
            this.nodeRef = nodeRef;
            this.score = score;
        }

    }

    public int getStart()
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasMore()
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Bulk fetch results in the cache
     * 
     * @param bulkFetch
     */
    public boolean setBulkFetch(boolean bulkFetch)
    {
    	return resultSet.setBulkFetch(bulkFetch);
    }

    /**
     * Do we bulk fetch
     * 
     * @return - true if we do
     */
    public boolean getBulkFetch()
    {
        return resultSet.getBulkFetch();
    }

    /**
     * Set the bulk fetch size
     * 
     * @param bulkFetchSize
     */
    public int setBulkFetchSize(int bulkFetchSize)
    {
    	return resultSet.setBulkFetchSize(bulkFetchSize);
    }

    /**
     * Get the bulk fetch size.
     * 
     * @return the fetch size
     */
    public int getBulkFetchSize()
    {
        return resultSet.getBulkFetchSize();
    }

}
