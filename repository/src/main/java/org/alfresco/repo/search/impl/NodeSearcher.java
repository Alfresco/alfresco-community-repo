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
package org.alfresco.repo.search.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.jaxen.JaxenException;

import org.alfresco.repo.search.DocumentNavigator;
import org.alfresco.repo.search.NodeServiceXPath;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.XPathException;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * Helper class that walks a node hierarchy.
 * <p>
 * Some searcher methods on {@link org.alfresco.service.cmr.search.SearchService} can use this directly as its only dependencies are {@link org.alfresco.service.cmr.repository.NodeService}, {@link org.alfresco.service.cmr.dictionary.DictionaryService} and a {@link org.alfresco.service.cmr.search.SearchService}
 * 
 * @author Derek Hulley
 */
public class NodeSearcher
{
    private NodeService nodeService;

    private DictionaryService dictionaryService;

    private SearchService searchService;

    public NodeSearcher(NodeService nodeService, DictionaryService dictionaryService, SearchService searchService)
    {
        this.nodeService = nodeService;
        this.dictionaryService = dictionaryService;
        this.searchService = searchService;
    }

    /**
     * @see NodeServiceXPath
     */
    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpathIn,
            QueryParameterDefinition[] paramDefs, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks, String language)
    {
        try
        {
            String xpath = xpathIn;

            List<AttributeOrder> order = null;

            DocumentNavigator documentNavigator = new DocumentNavigator(dictionaryService, nodeService, searchService,
                    namespacePrefixResolver, followAllParentLinks);
            NodeServiceXPath nsXPath = new NodeServiceXPath(xpath, documentNavigator, paramDefs);
            for (String prefix : namespacePrefixResolver.getPrefixes())
            {
                nsXPath.addNamespace(prefix, namespacePrefixResolver.getNamespaceURI(prefix));
            }
            @SuppressWarnings("rawtypes")
            List list = nsXPath.selectNodes(nodeService.getPrimaryParent(contextNodeRef));
            HashSet<NodeRef> unique = new HashSet<NodeRef>(list.size());
            for (Object o : list)
            {
                if (o instanceof ChildAssociationRef)
                {
                    unique.add(((ChildAssociationRef) o).getChildRef());
                }
                else if (o instanceof DocumentNavigator.Property)
                {
                    unique.add(((DocumentNavigator.Property) o).parent);
                }
                else
                {
                    throw new XPathException("Xpath expression must only select nodes");
                }
            }

            List<NodeRef> answer = new ArrayList<NodeRef>(unique.size());
            answer.addAll(unique);
            if (order != null)
            {
                orderNodes(answer, order);
                for (NodeRef node : answer)
                {
                    StringBuffer buffer = new StringBuffer();
                    for (AttributeOrder attOrd : order)
                    {
                        buffer.append(" ").append(nodeService.getProperty(node, attOrd.attribute));
                    }
                }
            }
            return answer;
        }
        catch (JaxenException e)
        {
            throw new XPathException("Error executing xpath: \n" + "   xpath: " + xpathIn, e);
        }
    }

    private void orderNodes(List<NodeRef> answer, List<AttributeOrder> order)
    {
        Collections.sort(answer, new NodeRefComparator(nodeService, order));
    }

    static class NodeRefComparator implements Comparator<NodeRef>
    {
        List<AttributeOrder> order;
        NodeService nodeService;

        NodeRefComparator(NodeService nodeService, List<AttributeOrder> order)
        {
            this.nodeService = nodeService;
            this.order = order;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public int compare(NodeRef n1, NodeRef n2)
        {
            for (AttributeOrder attributeOrder : order)
            {
                Serializable o1 = nodeService.getProperty(n1, attributeOrder.attribute);
                Serializable o2 = nodeService.getProperty(n2, attributeOrder.attribute);

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

    /**
     * @see NodeServiceXPath
     */
    public List<Serializable> selectProperties(NodeRef contextNodeRef, String xpath,
            QueryParameterDefinition[] paramDefs, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks, String language)
    {
        try
        {
            DocumentNavigator documentNavigator = new DocumentNavigator(dictionaryService, nodeService, searchService,
                    namespacePrefixResolver, followAllParentLinks);
            NodeServiceXPath nsXPath = new NodeServiceXPath(xpath, documentNavigator, paramDefs);
            for (String prefix : namespacePrefixResolver.getPrefixes())
            {
                nsXPath.addNamespace(prefix, namespacePrefixResolver.getNamespaceURI(prefix));
            }
            @SuppressWarnings("rawtypes")
            List list = nsXPath.selectNodes(nodeService.getPrimaryParent(contextNodeRef));
            List<Serializable> answer = new ArrayList<Serializable>(list.size());
            for (Object o : list)
            {
                if (!(o instanceof DocumentNavigator.Property))
                {
                    throw new XPathException("Xpath expression must only select nodes");
                }
                answer.add(((DocumentNavigator.Property) o).value);
            }
            return answer;
        }
        catch (JaxenException e)
        {
            throw new XPathException("Error executing xpath", e);
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
}
