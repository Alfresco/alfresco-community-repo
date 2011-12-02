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
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.lucene.LuceneResultSetRow;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Sorted results
 * 
 * @author andyh
 */
public class SortedResultSet implements ResultSet
{
    private ArrayList<NodeRefAndScore> nodeRefsAndScores;

    private NodeService nodeService;

    private ResultSet resultSet;

    private DictionaryService dictionaryService;

    private Locale locale;

    private Collator collator;

    public SortedResultSet(ResultSet resultSet, NodeService nodeService, SearchParameters searchParametersx, NamespacePrefixResolver namespacePrefixResolver,
            DictionaryService dictionaryService, Locale locale)
    {
        this(resultSet, nodeService, searchParametersx.getSortDefinitions(), namespacePrefixResolver, dictionaryService, locale);
    }

    /**
     * Source and resources required to sort
     * 
     * @param resultSet
     * @param nodeService
     * @param searchParameters
     * @param namespacePrefixResolver
     */
    public SortedResultSet(ResultSet resultSet, NodeService nodeService, List<SortDefinition> sortDefinitions, NamespacePrefixResolver namespacePrefixResolver,
            DictionaryService dictionaryService, Locale locale)
    {
        this.nodeService = nodeService;
        this.resultSet = resultSet;
        this.dictionaryService = dictionaryService;
        this.locale = locale;

        collator = Collator.getInstance(this.locale);

        nodeRefsAndScores = new ArrayList<NodeRefAndScore>(resultSet.length());
        for (ResultSetRow row : resultSet)
        {
            LuceneResultSetRow lrow = (LuceneResultSetRow) row;
            nodeRefsAndScores.add(new NodeRefAndScore(row.getNodeRef(), row.getScore(), lrow.doc()));
        }
        ArrayList<OrderDefinition> order = new ArrayList<OrderDefinition>();
        for (SortDefinition sd : sortDefinitions)
        {
            switch (sd.getSortType())
            {
            case FIELD:
                String field = sd.getField();
                if (field.startsWith("@"))
                {
                    if (field.endsWith(".size"))
                    {
                        QName qname = expandAttributeFieldName(field.substring(0, field.length() - 5), namespacePrefixResolver);
                        if (qname != null)
                        {
                            PropertyDefinition propDef = dictionaryService.getProperty(qname);
                            if ((propDef != null) && propDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
                            {
                                order.add(new ContentSizeOrder(qname, sd.isAscending(), nodeService));
                            }

                        }
                    }
                    if (field.endsWith(".mimetype"))
                    {
                        QName qname = expandAttributeFieldName(field.substring(0, field.length() - 9), namespacePrefixResolver);
                        if (qname != null)
                        {
                            PropertyDefinition propDef = dictionaryService.getProperty(qname);
                            if ((propDef != null) && propDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
                            {
                                order.add(new ContentMimetypeOrder(qname, sd.isAscending(), nodeService, collator));
                            }

                        }
                    }
                    QName qname = expandAttributeFieldName(field, namespacePrefixResolver);
                    if (qname != null)
                    {
                        order.add(new AttributeOrder(qname, sd.isAscending(), nodeService, this.dictionaryService, collator, locale));
                    }
                }
                else
                {
                    if (field.equals("ID"))
                    {
                        order.add(new IdOrder(sd.isAscending(), collator));
                    }
                    else if (field.equals("EXACTTYPE"))
                    {
                        order.add(new TypeOrder(sd.isAscending(), nodeService, collator));
                    }
                    else if (field.equals("PARENT"))
                    {
                        order.add(new ParentIdOrder(sd.isAscending(), nodeService, collator));
                    }
                    else
                    {
                        // SKIP UNKNOWN throw new AlfrescoRuntimeException("Property is not orderable: "+field);
                    }
                }
                break;
            case DOCUMENT:
                order.add(new DocumentOrder(sd.isAscending()));
            case SCORE:
                order.add(new ScoreOrder(sd.isAscending()));
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

    private void orderNodes(List<NodeRefAndScore> answer, List<OrderDefinition> order)
    {
        Collections.sort(answer, new NodeRefAndScoreComparator(order));
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
                String prefix = field.substring(1, colonPosition);

                String uri = namespacePrefixResolver.getNamespaceURI(prefix);
                if (uri == null)
                {
                    return null;
                }

                // find the prefix
                qname = QName.createQName(prefix, field.substring(colonPosition + 1), namespacePrefixResolver);
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
        private List<OrderDefinition> order;

        NodeRefAndScoreComparator(List<OrderDefinition> order)
        {
            this.order = order;
        }

        public int compare(NodeRefAndScore n1, NodeRefAndScore n2)
        {
            // Treat missing nodes as null for comparison
            for (OrderDefinition orderDefinition : order)
            {
                int answer = orderDefinition.compare(n1, n2);
                if (answer != 0)
                {
                    return answer;
                }
                else
                {
                    continue;
                }
            }
            return 0;
        }
    }

    private static interface OrderDefinition
    {
        int compare(NodeRefAndScore n1, NodeRefAndScore n2);
    }

    private static class AttributeOrder implements OrderDefinition
    {
        QName attribute;

        boolean ascending;

        NodeService nodeService;

        DictionaryService dictionaryService;

        Collator collator;

        Locale locale;

        AttributeOrder(QName attribute, boolean ascending, NodeService nodeService, DictionaryService dictionaryService, Collator collator, Locale locale)
        {
            this.attribute = attribute;
            this.ascending = ascending;
            this.nodeService = nodeService;
            this.dictionaryService = dictionaryService;
            this.collator = collator;
            this.locale = locale;
        }

        public int compare(NodeRefAndScore n1, NodeRefAndScore n2)
        {
            // Treat missing nodes as null for comparison

            Serializable o1;
            try
            {
                o1 = nodeService.getProperty(n1.nodeRef, attribute);
            }
            catch (InvalidNodeRefException inre)
            {
                o1 = null;
            }
            Serializable o2;
            try
            {
                o2 = nodeService.getProperty(n2.nodeRef, attribute);
            }
            catch (InvalidNodeRefException inre)
            {
                o2 = null;
            }

            if (o1 == null)
            {
                if (o2 == null)
                {
                    return 0;
                }
                else
                {
                    return ascending ? -1 : 1;
                }
            }
            else
            {
                if (o2 == null)
                {
                    return ascending ? 1 : -1;
                }
                else
                {
                    PropertyDefinition propertyDefinition = dictionaryService.getProperty(attribute);
                    if (propertyDefinition != null)
                    {
                        DataTypeDefinition dataType = propertyDefinition.getDataType();
                        if (dataType.getName().equals(DataTypeDefinition.TEXT))
                        {
                            String s1 = DefaultTypeConverter.INSTANCE.convert(String.class, o1);
                            String s2 = DefaultTypeConverter.INSTANCE.convert(String.class, o2);
                            int answer = (ascending ? 1 : -1) * collator.compare(s1, s2);
                            return answer;
                        }
                        else if (dataType.getName().equals(DataTypeDefinition.MLTEXT))
                        {
                            String s1 = DefaultTypeConverter.INSTANCE.convert(MLText.class, o1).getValue(locale);
                            String s2 = DefaultTypeConverter.INSTANCE.convert(MLText.class, o2).getValue(locale);

                            if (s1 == null)
                            {
                                if (s2 == null)
                                {
                                    return 0;
                                }
                                else
                                {
                                    return ascending ? -1 : 1;
                                }
                            }
                            else
                            {
                                if (s2 == null)
                                {
                                    return ascending ? 1 : -1;
                                }
                                else
                                {
                                    int answer = (ascending ? 1 : -1) * collator.compare(s1, s2);
                                    return answer;
                                }
                            }
                        }
                        else
                        {
                            if ((o1 instanceof Comparable) && (o2 instanceof Comparable))
                            {
                                int answer = (ascending ? 1 : -1) * ((Comparable) o1).compareTo((Comparable) o2);
                                return answer;

                            }
                            else
                            {
                                return 0;
                            }
                        }
                    }
                    else
                    {
                        if ((o1 instanceof Comparable) && (o2 instanceof Comparable))
                        {
                            int answer = (ascending ? 1 : -1) * ((Comparable) o1).compareTo((Comparable) o2);
                            return answer;

                        }
                        else
                        {
                            return 0;
                        }
                    }
                }
            }

        }
    }

    private static class ContentSizeOrder implements OrderDefinition
    {
        QName attribute;

        boolean ascending;

        NodeService nodeService;

        ContentSizeOrder(QName attribute, boolean ascending, NodeService nodeService)
        {
            this.attribute = attribute;
            this.ascending = ascending;
            this.nodeService = nodeService;

        }

        public int compare(NodeRefAndScore n1, NodeRefAndScore n2)
        {
            // Treat missing nodes as null for comparison

            Serializable o1;
            try
            {
                o1 = nodeService.getProperty(n1.nodeRef, attribute);
            }
            catch (InvalidNodeRefException inre)
            {
                o1 = null;
            }
            Serializable o2;
            try
            {
                o2 = nodeService.getProperty(n2.nodeRef, attribute);
            }
            catch (InvalidNodeRefException inre)
            {
                o2 = null;
            }

            if (o1 == null)
            {
                if (o2 == null)
                {
                    return 0;
                }
                else
                {
                    return ascending ? -1 : 1;
                }
            }
            else
            {
                if (o2 == null)
                {
                    return ascending ? 1 : -1;
                }
                else
                {

                    ContentData cd1 = DefaultTypeConverter.INSTANCE.convert(ContentData.class, o1);
                    ContentData cd2 = DefaultTypeConverter.INSTANCE.convert(ContentData.class, o2);

                    if (cd1 == null)
                    {
                        if (cd2 == null)
                        {
                            return 0;
                        }
                        else
                        {
                            return ascending ? -1 : 1;
                        }
                    }
                    else
                    {
                        if (cd2 == null)
                        {
                            return ascending ? 1 : -1;
                        }
                        else
                        {
                            return (ascending ? 1 : -1) * (int)(cd1.getSize() - cd2.getSize());
                        }
                    }
                }
            }

        }
    }
    
    private static class ContentMimetypeOrder implements OrderDefinition
    {
        QName attribute;

        boolean ascending;

        NodeService nodeService;
        
        Collator collator;

        ContentMimetypeOrder(QName attribute, boolean ascending, NodeService nodeService, Collator collator)
        {
            this.attribute = attribute;
            this.ascending = ascending;
            this.nodeService = nodeService;
            this.collator = collator;
        }

        public int compare(NodeRefAndScore n1, NodeRefAndScore n2)
        {
            // Treat missing nodes as null for comparison

            Serializable o1;
            try
            {
                o1 = nodeService.getProperty(n1.nodeRef, attribute);
            }
            catch (InvalidNodeRefException inre)
            {
                o1 = null;
            }
            Serializable o2;
            try
            {
                o2 = nodeService.getProperty(n2.nodeRef, attribute);
            }
            catch (InvalidNodeRefException inre)
            {
                o2 = null;
            }

            if (o1 == null)
            {
                if (o2 == null)
                {
                    return 0;
                }
                else
                {
                    return ascending ? -1 : 1;
                }
            }
            else
            {
                if (o2 == null)
                {
                    return ascending ? 1 : -1;
                }
                else
                {

                    ContentData cd1 = DefaultTypeConverter.INSTANCE.convert(ContentData.class, o1);
                    ContentData cd2 = DefaultTypeConverter.INSTANCE.convert(ContentData.class, o2);

                    if (cd1 == null)
                    {
                        if (cd2 == null)
                        {
                            return 0;
                        }
                        else
                        {
                            return ascending ? -1 : 1;
                        }
                    }
                    else
                    {
                        if (cd2 == null)
                        {
                            return ascending ? 1 : -1;
                        }
                        else
                        {
                            return (ascending ? 1 : -1) * collator.compare(cd1.getMimetype(), cd2.getMimetype());
                        }
                    }
                }
            }

        }
    }

    private static class IdOrder implements OrderDefinition
    {
        boolean ascending;

        Collator collator;

        IdOrder(boolean ascending, Collator collator)
        {
            this.ascending = ascending;
            this.collator = collator;
        }

        public int compare(NodeRefAndScore n1, NodeRefAndScore n2)
        {
            // Treat missing nodes as null for comparison

            String o1 = n1.nodeRef.toString();
            String o2 = n2.nodeRef.toString();

            if (o1 == null)
            {
                if (o2 == null)
                {
                    return 0;
                }
                else
                {
                    return ascending ? -1 : 1;
                }
            }
            else
            {
                if (o2 == null)
                {
                    return ascending ? 1 : -1;
                }
                else
                {

                    int answer = (ascending ? 1 : -1) * collator.compare(o1, o2);
                    return answer;
                }
            }
        }
    }

    private static class ScoreOrder implements OrderDefinition
    {
        boolean ascending;

        ScoreOrder(boolean ascending)
        {
            this.ascending = ascending;
        }

        public int compare(NodeRefAndScore n1, NodeRefAndScore n2)
        {
            // Treat missing nodes as null for comparison
            return (ascending ? 1 : -1) * Float.compare(n1.score, n2.score);

        }
    }

    private static class DocumentOrder implements OrderDefinition
    {
        boolean ascending;

        DocumentOrder(boolean ascending)
        {
            this.ascending = ascending;
        }

        public int compare(NodeRefAndScore n1, NodeRefAndScore n2)
        {
            // Treat missing nodes as null for comparison
            return (ascending ? 1 : -1) * Float.compare(n1.doc, n2.doc);

        }
    }

    private static class TypeOrder implements OrderDefinition
    {
        boolean ascending;

        NodeService nodeService;

        Collator collator;

        TypeOrder(boolean ascending, NodeService nodeService, Collator collator)
        {
            this.ascending = ascending;
            this.nodeService = nodeService;
            this.collator = collator;
        }

        public int compare(NodeRefAndScore n1, NodeRefAndScore n2)
        {
            // Treat missing nodes as null for comparison

            String o1;
            try
            {
                o1 = nodeService.getType(n1.nodeRef).toString();
            }
            catch (InvalidNodeRefException inre)
            {
                o1 = null;
            }
            String o2;
            try
            {
                o2 = nodeService.getType(n2.nodeRef).toString();
            }
            catch (InvalidNodeRefException inre)
            {
                o2 = null;
            }

            if (o1 == null)
            {
                if (o2 == null)
                {
                    return 0;
                }
                else
                {
                    return ascending ? -1 : 1;
                }
            }
            else
            {
                if (o2 == null)
                {
                    return ascending ? 1 : -1;
                }
                else
                {

                    int answer = (ascending ? 1 : -1) * collator.compare(o1, o2);
                    return answer;
                }
            }

        }
    }

    private static class ParentIdOrder implements OrderDefinition
    {
        boolean ascending;

        NodeService nodeService;

        Collator collator;

        ParentIdOrder(boolean ascending, NodeService nodeService, Collator collator)
        {
            this.ascending = ascending;
            this.nodeService = nodeService;
            this.collator = collator;
        }

        public int compare(NodeRefAndScore n1, NodeRefAndScore n2)
        {
            // Treat missing nodes as null for comparison

            String o1 = null;
            ;
            try
            {
                ChildAssociationRef ca1 = nodeService.getPrimaryParent(n1.nodeRef);
                if ((ca1 != null) && (ca1.getParentRef() != null))
                {
                    o1 = ca1.getParentRef().toString();
                }
            }
            catch (InvalidNodeRefException inre)
            {
                o1 = null;
            }
            String o2 = null;
            try
            {
                ChildAssociationRef ca2 = nodeService.getPrimaryParent(n2.nodeRef);
                if ((ca2 != null) && (ca2.getParentRef() != null))
                {
                    o2 = ca2.getParentRef().toString();
                }
            }
            catch (InvalidNodeRefException inre)
            {
                o2 = null;
            }

            if (o1 == null)
            {
                if (o2 == null)
                {
                    return 0;
                }
                else
                {
                    return ascending ? -1 : 1;
                }
            }
            else
            {
                if (o2 == null)
                {
                    return ascending ? 1 : -1;
                }
                else
                {

                    int answer = (ascending ? 1 : -1) * collator.compare(o1, o2);
                    return answer;
                }
            }

        }
    }

    private static class NodeRefAndScore
    {
        NodeRef nodeRef;

        float score;

        int doc;

        NodeRefAndScore(NodeRef nodeRef, float score, int doc)
        {
            this.nodeRef = nodeRef;
            this.score = score;
            this.doc = doc;
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
    
    @Override
    public List<Pair<String, Integer>> getFieldFacet(String field)
    {
        return resultSet.getFieldFacet(field);
    }

}
