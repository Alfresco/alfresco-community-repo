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
package org.alfresco.repo.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.repo.jscript.CategoryTemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.namespace.QName;

/**
 * Support for finding classifications and their root categories.
 * 
 * @author Andy Hind
 */
public class Classification extends BaseTemplateExtensionImplementation
{
    private ServiceRegistry services;
    private StoreRef storeRef;

    /**
     * Sets the service registry
     * 
     * @param services  the service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }
    
    /**
     * @param storeUrl  The store ref url to set.
     */
    public void setStoreUrl(String storeUrl)
    {
        this.storeRef = new StoreRef(storeUrl);
    }

    /**
     * Find all the category nodes in a given classification.
     * 
     * @param aspect
     * 
     * @return all the category nodes in a given classification.
     */
    public List<CategoryTemplateNode> getAllCategoryNodes(String aspect)
    {
        return buildCategoryNodes(services.getCategoryService().getCategories(storeRef, createQName(aspect),
                CategoryService.Depth.ANY));
    }
    
    /**
     * Find all the category nodes in a given classification.
     * 
     * @param aspect
     * 
     * @return all the category nodes in a given classification.
     */
    public List<CategoryTemplateNode> getAllCategoryNodes(QName aspect)
    {
        return buildCategoryNodes(services.getCategoryService().getCategories(storeRef, aspect,
                CategoryService.Depth.ANY));
    }

    /**
     * @return all the aspects that define a classification.
     */
    public List<QName> getAllClassificationAspects()
    {
        Collection<QName> aspects = services.getCategoryService().getClassificationAspects();
        ArrayList<QName> answer = new ArrayList<QName>(aspects.size());
        answer.addAll(aspects);
        return answer;
    }

    /**
     * Get the root categories in a classification.
     * 
     * @param aspect
     * 
     * @return List of TemplateNode
     */
    public List<CategoryTemplateNode> getRootCategories(String aspect)
    {
        return buildCategoryNodes(services.getCategoryService().getRootCategories(storeRef, createQName(aspect)));
    }

    
    private List<CategoryTemplateNode> buildCategoryNodes(Collection<ChildAssociationRef> cars)
    {
        ArrayList<CategoryTemplateNode> categoryNodes = new ArrayList<CategoryTemplateNode>(cars.size());
        for (ChildAssociationRef car : cars)
        {
            categoryNodes.add(new CategoryTemplateNode(car.getChildRef(), this.services, getTemplateImageResolver()));
        }
        return categoryNodes;
    }

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
}
