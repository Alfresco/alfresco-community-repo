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
package org.alfresco.repo.search.impl.lucene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.IndexerException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;

public class LuceneCategoryServiceImpl implements CategoryService
{
    private NodeService nodeService;

    private NamespacePrefixResolver namespacePrefixResolver;

    private DictionaryService dictionaryService;

    private LuceneIndexerAndSearcher indexerAndSearcher;

    public LuceneCategoryServiceImpl()
    {
        super();
    }

    // Inversion of control support

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setIndexerAndSearcher(LuceneIndexerAndSearcher indexerAndSearcher)
    {
        this.indexerAndSearcher = indexerAndSearcher;
    }

    public Collection<ChildAssociationRef> getChildren(NodeRef categoryRef, Mode mode, Depth depth)
    {
        if (categoryRef == null)
        {
            return Collections.<ChildAssociationRef> emptyList();
        }
        ResultSet resultSet = null;
        try
        {
            StringBuilder luceneQuery = new StringBuilder(64);

            switch(mode)
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

            resultSet = indexerAndSearcher.getSearcher(categoryRef.getStoreRef(), false).query(categoryRef.getStoreRef(), "lucene", luceneQuery.toString(), null, null);

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
        List<ChildAssociationRef> collection = new ArrayList<ChildAssociationRef>();
        if (resultSet != null)
        {
            for (ResultSetRow row : resultSet)
            {
                ChildAssociationRef car = nodeService.getPrimaryParent(row.getNodeRef());
                collection.add(car);
            }
        }
        return collection;
        // The caller closes the result set
    }

    public Collection<ChildAssociationRef> getCategories(StoreRef storeRef, QName aspectQName, Depth depth)
    {
        Collection<ChildAssociationRef> assocs = new ArrayList<ChildAssociationRef>();
        Set<NodeRef> nodeRefs = getClassificationNodes(storeRef, aspectQName);
        for (NodeRef nodeRef : nodeRefs)
        {
            assocs.addAll(getChildren(nodeRef, Mode.SUB_CATEGORIES, depth));
        }
        return assocs;
    }

    private Set<NodeRef> getClassificationNodes(StoreRef storeRef, QName qname)
    {
        ResultSet resultSet = null;
        try
        {
            resultSet = indexerAndSearcher.getSearcher(storeRef, false).query(storeRef, "lucene", "PATH:\"/" + getPrefix(qname.getNamespaceURI()) + ISO9075.encode(qname.getLocalName()) + "\"",
                    null, null);

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
        ResultSet resultSet = null;
        try
        {
            resultSet = indexerAndSearcher.getSearcher(storeRef, false).query(storeRef, "lucene", "PATH:\"//cm:categoryRoot/*\"", null, null);
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
        List<QName> list = new ArrayList<QName>();
        for (QName aspect : dictionaryService.getAllAspects())
        {
            if (dictionaryService.isSubClass(aspect, ContentModel.ASPECT_CLASSIFIABLE))
            {
                list.add(aspect);
            }
        }
        return list;
    }

    public NodeRef createClassifiction(StoreRef storeRef, QName typeName, String attributeName)
    {
        throw new UnsupportedOperationException();
    }

    public Collection<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName)
    {
        Collection<ChildAssociationRef> assocs = new ArrayList<ChildAssociationRef>();
        Set<NodeRef> nodeRefs = getClassificationNodes(storeRef, aspectName);
        for (NodeRef nodeRef : nodeRefs)
        {
            assocs.addAll(getChildren(nodeRef, Mode.SUB_CATEGORIES, Depth.IMMEDIATE));
        }
        return assocs;
    }

    public NodeRef createCategory(NodeRef parent, String name)
    {
        if(!nodeService.exists(parent))
        {
            throw new AlfrescoRuntimeException("Missing category?");
        }
        String uri = nodeService.getPrimaryParent(parent).getQName().getNamespaceURI();
        String validLocalName = QName.createValidLocalName(name);
        NodeRef newCategory = nodeService.createNode(parent, ContentModel.ASSOC_SUBCATEGORIES, QName.createQName(uri, validLocalName), ContentModel.TYPE_CATEGORY).getChildRef();
        nodeService.setProperty(newCategory, ContentModel.PROP_NAME, name);
        return newCategory;
    }

    public NodeRef createRootCategory(StoreRef storeRef, QName aspectName, String name)
    {
        Set<NodeRef> nodeRefs = getClassificationNodes(storeRef, aspectName);
        if(nodeRefs.size() == 0)
        {
            throw new AlfrescoRuntimeException("Missing classification: "+aspectName);
        }
        NodeRef parent = nodeRefs.iterator().next();
        return createCategory(parent, name);
    }

    public void deleteCategory(NodeRef nodeRef)
    {
        nodeService.deleteNode(nodeRef);
    }

    public void deleteClassification(StoreRef storeRef, QName aspectName)
    {
        throw new UnsupportedOperationException();
    }
    
    

    
    
}
