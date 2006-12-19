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
package org.alfresco.repo.jscript;

import java.util.Collection;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Scriptable;

/**
 * Support class for finding categories, finding root nodes for categories and creating root categories. 
 * 
 * @author Andy Hind
 */
public final class Classification  extends BaseScriptImplementation implements Scopeable
{
    @SuppressWarnings("unused")
    private Scriptable scope;

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
     * @see org.alfresco.repo.jscript.Scopeable#setScope(org.mozilla.javascript.Scriptable)
     */
    public void setScope(Scriptable scope)
    {
        this.scope = scope;
    }

    /**
     * Find all the category nodes in a given classification.
     * 
     * @param aspect
     * @return
     */
    public CategoryNode[] getAllCategoryNodes(String aspect)
    {
        return buildCategoryNodes(services.getCategoryService().getCategories(storeRef, createQName(aspect),
                CategoryService.Depth.ANY));
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
    
    public String[] jsGet_allClassificationAspects()
    {
        return getAllClassificationAspects();
    }

    /**
     * Create a root category in a classification.
     * 
     * @param aspect
     * @param name
     */
    public void createRootCategory(String aspect, String name)
    {
        services.getCategoryService().createRootCategory(storeRef, createQName(aspect), name);
    }

    /**
     * Get the root categories in a classification.
     * 
     * @param aspect
     * @return
     */
    public CategoryNode[] getRootCategories(String aspect)
    {
        return buildCategoryNodes(services.getCategoryService().getRootCategories(storeRef, createQName(aspect)));
    }

    private CategoryNode[] buildCategoryNodes(Collection<ChildAssociationRef> cars)
    {
        CategoryNode[] categoryNodes = new CategoryNode[cars.size()];
        int i = 0;
        for (ChildAssociationRef car : cars)
        {
            categoryNodes[i++] = new CategoryNode(car.getChildRef(), this.services, this.scope);
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
