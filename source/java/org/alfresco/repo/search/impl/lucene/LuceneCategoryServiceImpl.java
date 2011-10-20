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
package org.alfresco.repo.search.impl.lucene;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;

/**
 * Category service implementation
 * 
 * @author andyh
 */
public class LuceneCategoryServiceImpl implements CategoryService
{
    protected NodeService nodeService;
    
    protected NodeService publicNodeService;

    protected TenantService tenantService;

    protected NamespacePrefixResolver namespacePrefixResolver;

    protected DictionaryService dictionaryService;

    protected IndexerAndSearcher indexerAndSearcher;

    /**
     * 
     */
    public LuceneCategoryServiceImpl()
    {
        super();
    }

    // Inversion of control support

    /**
     * Set the node service
     * 
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the public node service
     * 
     * @param nodeService
     */
    public void setPublicNodeService(NodeService publicNodeService)
    {
        this.publicNodeService = publicNodeService;
    }

    /**
     * Set the tenant service
     * 
     * @param tenantService
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * Set the service to map prefixes to uris
     * 
     * @param namespacePrefixResolver
     */
    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    /**
     * Set the dictionary service
     * 
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the indexer and searcher
     * 
     * @param indexerAndSearcher
     */
    public void setIndexerAndSearcher(IndexerAndSearcher indexerAndSearcher)
    {
        this.indexerAndSearcher = indexerAndSearcher;
    }

    public Collection<ChildAssociationRef> getChildren(NodeRef categoryRef, Mode mode, Depth depth)
    {
        if (categoryRef == null)
        {
            return Collections.<ChildAssociationRef> emptyList();
        }

        categoryRef = tenantService.getName(categoryRef);

        ResultSet resultSet = null;
        try
        {
            StringBuilder luceneQuery = new StringBuilder(64);

            switch (mode)
            {
            case ALL:
                luceneQuery.append("PATH:\"");
                luceneQuery.append(buildXPath(nodeService.getPath(categoryRef))).append("/");
                if (depth.equals(Depth.ANY))
                {
                    luceneQuery.append("/");
                }
                luceneQuery.append("*").append("\" ");
                break;
            case MEMBERS:
                luceneQuery.append("PATH:\"");
                luceneQuery.append(buildXPath(nodeService.getPath(categoryRef))).append("/");
                if (depth.equals(Depth.ANY))
                {
                    luceneQuery.append("/");
                }
                luceneQuery.append("member").append("\" ");
                break;
            case SUB_CATEGORIES:
                luceneQuery.append("+PATH:\"");
                luceneQuery.append(buildXPath(nodeService.getPath(categoryRef))).append("/");
                if (depth.equals(Depth.ANY))
                {
                    luceneQuery.append("/");
                }
                luceneQuery.append("*").append("\" ");
                luceneQuery.append("+TYPE:\"" + ContentModel.TYPE_CATEGORY.toString() + "\"");
                break;
            }

            // Get a searcher that will include Categories added in this transaction
            SearchService searcher = indexerAndSearcher.getSearcher(categoryRef.getStoreRef(), true);
            
            // Perform the search
            resultSet = searcher.query(categoryRef.getStoreRef(), "lucene", luceneQuery.toString(), null);

            // Convert from search results to the required Child Assocs
            return resultSetToChildAssocCollection(resultSet);
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
    }

    private String buildXPath(Path path)
    {
        StringBuilder pathBuffer = new StringBuilder(64);
        for (Iterator<Path.Element> elit = path.iterator(); elit.hasNext(); /**/)
        {
            Path.Element element = elit.next();
            if (!(element instanceof Path.ChildAssocElement))
            {
                throw new IndexerException("Confused path: " + path);
            }
            Path.ChildAssocElement cae = (Path.ChildAssocElement) element;
            if (cae.getRef().getParentRef() != null)
            {
                pathBuffer.append("/");
                pathBuffer.append(getPrefix(cae.getRef().getQName().getNamespaceURI()));
                pathBuffer.append(ISO9075.encode(cae.getRef().getQName().getLocalName()));
            }
        }
        return pathBuffer.toString();
    }

    HashMap<String, String> prefixLookup = new HashMap<String, String>();

    private String getPrefix(String uri)
    {
        String prefix = prefixLookup.get(uri);
        if (prefix == null)
        {
            Collection<String> prefixes = namespacePrefixResolver.getPrefixes(uri);
            for (String first : prefixes)
            {
                prefix = first;
                break;
            }

            prefixLookup.put(uri, prefix);
        }
        if (prefix == null)
        {
            return "";
        }
        else
        {
            return prefix + ":";
        }

    }

    private Collection<ChildAssociationRef> resultSetToChildAssocCollection(ResultSet resultSet)
    {
        List<ChildAssociationRef> collection = new LinkedList<ChildAssociationRef>();
        if (resultSet != null)
        {
            for (ResultSetRow row : resultSet)
            {
                try
                {
                    ChildAssociationRef car = nodeService.getPrimaryParent(row.getNodeRef());
                    collection.add(car);
                }
                catch(InvalidNodeRefException inre)
                {
                    // keep going the node has gone beneath us just skip it
                }
            }
        }
        return collection;
        // The caller closes the result set
    }

    public Collection<ChildAssociationRef> getCategories(StoreRef storeRef, QName aspectQName, Depth depth)
    {
        Collection<ChildAssociationRef> assocs = new LinkedList<ChildAssociationRef>();
        Set<NodeRef> nodeRefs = getClassificationNodes(storeRef, aspectQName);
        for (NodeRef nodeRef : nodeRefs)
        {
            assocs.addAll(getChildren(nodeRef, Mode.SUB_CATEGORIES, depth));
        }
        return assocs;
    }

    private Set<NodeRef> getClassificationNodes(StoreRef storeRef, QName qname)
    {
        storeRef = tenantService.getName(storeRef);

        ResultSet resultSet = null;
        try
        {
            resultSet = indexerAndSearcher.getSearcher(storeRef, false).query(storeRef, "lucene",
                    "PATH:\"/" + getPrefix(qname.getNamespaceURI()) + ISO9075.encode(qname.getLocalName()) + "\"", null);

            Set<NodeRef> nodeRefs = new HashSet<NodeRef>(resultSet.length());
            for (ResultSetRow row : resultSet)
            {
                nodeRefs.add(row.getNodeRef());
            }

            return nodeRefs;
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
    }

    public Collection<ChildAssociationRef> getClassifications(StoreRef storeRef)
    {
        storeRef = tenantService.getName(storeRef);

        ResultSet resultSet = null;
        try
        {
            resultSet = indexerAndSearcher.getSearcher(storeRef, false).query(storeRef, "lucene", "PATH:\"//cm:categoryRoot/*\"", null);
            return resultSetToChildAssocCollection(resultSet);
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
    }

    public Collection<QName> getClassificationAspects()
    {
        return dictionaryService.getSubAspects(ContentModel.ASPECT_CLASSIFIABLE, true);
    }

    public NodeRef createClassification(StoreRef storeRef, QName typeName, String attributeName)
    {
        throw new UnsupportedOperationException();
    }

    public Collection<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName)
    {
        Collection<ChildAssociationRef> assocs = new LinkedList<ChildAssociationRef>();
        Set<NodeRef> nodeRefs = getClassificationNodes(storeRef, aspectName);
        for (NodeRef nodeRef : nodeRefs)
        {
            assocs.addAll(getChildren(nodeRef, Mode.SUB_CATEGORIES, Depth.IMMEDIATE));
        }
        return assocs;
    }

    public ChildAssociationRef getCategory(NodeRef parent, QName aspectName, String name)
    {
        String uri = nodeService.getPrimaryParent(parent).getQName().getNamespaceURI();
        String validLocalName = QName.createValidLocalName(name);
        Collection<ChildAssociationRef> assocs = nodeService.getChildAssocs(parent, ContentModel.ASSOC_SUBCATEGORIES,
                QName.createQName(uri, validLocalName), false);
        if (assocs.isEmpty())
        {
            return null;
        }
        return assocs.iterator().next();
    }

    public Collection<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName, String name,
            boolean create)
    {
        Set<NodeRef> nodeRefs = getClassificationNodes(storeRef, aspectName);
        if (nodeRefs.isEmpty())
        {
            return Collections.emptySet();
        }
        Collection<ChildAssociationRef> assocs = new LinkedList<ChildAssociationRef>();
        for (NodeRef nodeRef : nodeRefs)
        {
            ChildAssociationRef category = getCategory(nodeRef, aspectName, name);
            if (category != null)
            {
                assocs.add(category);
            }
        }
        if (create && assocs.isEmpty())
        {
            assocs.add(createCategoryInternal(nodeRefs.iterator().next(), name));
        }
        return assocs;
    }
    
    public NodeRef createCategory(NodeRef parent, String name)
    {
        return createCategoryInternal(parent, name).getChildRef();
    }

    private ChildAssociationRef createCategoryInternal(NodeRef parent, String name)
    {
        if (!nodeService.exists(parent))
        {
            throw new AlfrescoRuntimeException("Missing category?");
        }
        String uri = nodeService.getPrimaryParent(parent).getQName().getNamespaceURI();
        String validLocalName = QName.createValidLocalName(name);
        ChildAssociationRef newCategory = publicNodeService.createNode(parent, ContentModel.ASSOC_SUBCATEGORIES, QName.createQName(uri, validLocalName), ContentModel.TYPE_CATEGORY);
        publicNodeService.setProperty(newCategory.getChildRef(), ContentModel.PROP_NAME, name);
        return newCategory;
    }

    public NodeRef createRootCategory(StoreRef storeRef, QName aspectName, String name)
    {
        Set<NodeRef> nodeRefs = getClassificationNodes(storeRef, aspectName);
        if (nodeRefs.size() == 0)
        {
            throw new AlfrescoRuntimeException("Missing classification: " + aspectName);
        }
        NodeRef parent = nodeRefs.iterator().next();
        return createCategory(parent, name);
    }

    public void deleteCategory(NodeRef nodeRef)
    {
        publicNodeService.deleteNode(nodeRef);
    }

    public void deleteClassification(StoreRef storeRef, QName aspectName)
    {
        throw new UnsupportedOperationException();
    }

    public List<Pair<NodeRef, Integer>> getTopCategories(StoreRef storeRef, QName aspectName, int count)
    {
        if (indexerAndSearcher instanceof LuceneIndexerAndSearcher)
        {
            AspectDefinition definition = dictionaryService.getAspect(aspectName);
            if(definition == null)
            {
                throw new IllegalStateException("Unknown aspect");
            }
            QName catProperty = null;
            Map<QName, PropertyDefinition> properties = definition.getProperties();
            for(QName pName : properties.keySet())
            {
                if(pName.getNamespaceURI().equals(aspectName.getNamespaceURI()))
                {
                    if(pName.getLocalName().equalsIgnoreCase(aspectName.getLocalName()))
                    {
                        PropertyDefinition def = properties.get(pName);
                        if(def.getDataType().getName().equals(DataTypeDefinition.CATEGORY))
                        {
                            catProperty = pName;
                        }
                    }
                }
            }
            if(catProperty == null)
            {
                throw new IllegalStateException("Aspect does not have category property mirroring the aspect name");
            }
            
            
            LuceneIndexerAndSearcher lias = (LuceneIndexerAndSearcher) indexerAndSearcher;
            String field = "@" + catProperty;
            SearchService searchService = lias.getSearcher(storeRef, false);
            if (searchService instanceof LuceneSearcher)
            {
                LuceneSearcher luceneSearcher = (LuceneSearcher)searchService;
                List<Pair<String, Integer>> topTerms = luceneSearcher.getTopTerms(field, count);
                List<Pair<NodeRef, Integer>> answer = new LinkedList<Pair<NodeRef, Integer>>();
                for (Pair<String, Integer> term : topTerms)
                {
                    Pair<NodeRef, Integer> toAdd;
                    NodeRef nodeRef = new NodeRef(term.getFirst());
                    if (nodeService.exists(nodeRef))
                    {
                        toAdd = new Pair<NodeRef, Integer>(nodeRef, term.getSecond());
                    }
                    else
                    {
                        toAdd = new Pair<NodeRef, Integer>(null, term.getSecond());
                    }
                    answer.add(toAdd);
                }
                return answer;
            }
            else
            {
                throw new UnsupportedOperationException("getPolularCategories is only supported for lucene indexes");
            }
        }
        else
        {
            throw new UnsupportedOperationException("getPolularCategories is only supported for lucene indexes");
        }
    }

}
