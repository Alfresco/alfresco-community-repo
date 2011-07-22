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
package org.alfresco.repo.jscript;

import java.util.Collection;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * Support class for finding categories, finding root nodes for categories and creating root categories. 
 * 
 * @author Andy Hind
 */
public final class Classification extends BaseScopableProcessorExtension
{
    private ServiceRegistry services;

    private StoreRef storeRef;
    
    /**
     * Set the default store reference
     * 
     * @param   storeRef the default store reference
     */
    public void setStoreUrl(String storeRef)
    {
        this.storeRef = new StoreRef(storeRef);
    }
    
    /**
     * Set the service registry
     * 
     * @param services  the service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }

    /**
     * Find all the category nodes in a given classification.
     * 
     * @param aspect
     * @return
     */
    public Scriptable getAllCategoryNodes(String aspect)
    {
        Object[] cats = buildCategoryNodes(services.getCategoryService().getCategories(
                            storeRef, createQName(aspect), CategoryService.Depth.ANY));
        return Context.getCurrentContext().newArray(getScope(), cats);
    }

    /**
     * Get all the aspects that define a classification.
     * 
     * @return
     */
    public String[] getAllClassificationAspects()
    {
        Collection<QName> aspects = services.getCategoryService().getClassificationAspects();
        String[] answer = new String[aspects.size()];
        int i = 0;
        for (QName qname : aspects)
        {
            answer[i++] = qname.toPrefixString(this.services.getNamespaceService());
        }
        return answer;
    }
    
    /**
     * Create a root category in a classification.
     * 
     * @param aspect
     * @param name
     */
    public CategoryNode createRootCategory(String aspect, String name)
    {
        NodeRef categoryNodeRef = services.getCategoryService().createRootCategory(storeRef, createQName(aspect), name);
        CategoryNode categoryNode = new CategoryNode(categoryNodeRef, this.services, getScope());

        return categoryNode;
    }
    
    /**
     * Get the category node from the category node reference.
     * 
     * @param categoryRef   category node reference
     * @return {@link CategoryNode} category node 
     */
    public CategoryNode getCategory(String categoryRef)
    {
        CategoryNode result = null;
        NodeRef categoryNodeRef = new NodeRef(categoryRef);
        if (services.getNodeService().exists(categoryNodeRef) == true &&
            services.getDictionaryService().isSubClass(ContentModel.TYPE_CATEGORY, services.getNodeService().getType(categoryNodeRef)) == true)
        {
            result = new CategoryNode(categoryNodeRef, this.services, getScope());
        }
        return result;
    }

    /**
     * Get the root categories in a classification.
     * 
     * @param aspect
     * @return
     */
    public Scriptable getRootCategories(String aspect)
    {
        Object[] cats = buildCategoryNodes(services.getCategoryService().getRootCategories(
                            storeRef, createQName(aspect)));
        return Context.getCurrentContext().newArray(getScope(), cats);
    }

    /**
     * Get the category usage count.
     * 
     * @param aspect
     * @param maxCount
     * @return
     */
    public Scriptable getCategoryUsage(String aspect, int maxCount)
    {
        List<Pair<NodeRef, Integer>> topCats = services.getCategoryService().getTopCategories(storeRef, createQName(aspect), maxCount);
        Object[] tags = new Object[topCats.size()];
        int i = 0;
        for (Pair<NodeRef, Integer> topCat : topCats)
        {
           tags[i++] = new Tag(new CategoryNode(topCat.getFirst(), this.services, getScope()), topCat.getSecond());
        }
        
        return Context.getCurrentContext().newArray(getScope(), tags);
    }

    /**
     * Build category nodes.
     * 
     * @param cars  list of associations to category nodes
     * @return {@link Object}[] array of category nodes
     */
    private Object[] buildCategoryNodes(Collection<ChildAssociationRef> cars)
    {
        Object[] categoryNodes = new Object[cars.size()];
        int i = 0;
        for (ChildAssociationRef car : cars)
        {
            categoryNodes[i++] = new CategoryNode(car.getChildRef(), this.services, getScope());
        }
        return categoryNodes;
    }

    /**
     * Create QName from string
     * 
     * @param s QName string value
     * @return {@link QName} qualified name object    
     */
    private QName createQName(String s)
    {
        QName qname;
        if (s.indexOf(QName.NAMESPACE_BEGIN) != -1)
        {
            qname = QName.createQName(s);
        }
        else
        {
            qname = QName.createQName(s, this.services.getNamespaceService());
        }
        return qname;
    }
    
    /**
     * Tag class returned from getCategoryUsage().
     * 
     * @param CategoryNode
     * @param frequency
     * @return
     */
    public final class Tag
    {
        private CategoryNode categoryNode;
        private int frequency = 0;
       
        public Tag(CategoryNode categoryNode, int frequency)
        {
            this.categoryNode = categoryNode;
            this.frequency = frequency;
        }
       
        public CategoryNode getCategory()
        {
            return categoryNode;
        }
       
        public int getFrequency()
        {
            return frequency;
        }
     }
}
