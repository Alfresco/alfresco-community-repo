/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.repo.jscript.CategoryTemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.namespace.QName;

/**
 * Support for finding classifications and their root categories.
 * 
 * @author Andy Hind
 */
public final class Classification
{
    private ServiceRegistry services;
    private TemplateImageResolver imageResolver;
    private StoreRef storeRef;

    public Classification(StoreRef storeRef, ServiceRegistry services,  TemplateImageResolver imageResolver)
    {
        this.storeRef = storeRef;
        this.services = services;
        this.imageResolver = imageResolver;
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
            categoryNodes.add(new CategoryTemplateNode(car.getChildRef(), this.services, this.imageResolver));
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
