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
public class Classification extends BaseTemplateProcessorExtension
{
    private ServiceRegistry services;
    private StoreRef storeRef;

    /**
     * Sets the service registry
     * 
     * @param services
     *            the service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }

    /**
     * @param storeUrl
     *            The store ref url to set.
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
