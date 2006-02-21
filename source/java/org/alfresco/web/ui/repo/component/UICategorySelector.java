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
package org.alfresco.web.ui.repo.component;

import java.util.ArrayList;
import java.util.Collection;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.CategoryService.Depth;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.repo.WebResources;

/**
 * Component to allow the selection of a category
 * 
 * @author gavinc
 */
public class UICategorySelector extends AbstractItemSelector
{
   // ------------------------------------------------------------------------------
   // Component Impl 
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.CategorySelector";
   }
   
   /**
    * 
    * @see org.alfresco.web.ui.repo.component.AbstractItemSelector#getDefaultLabel()
    */
   public String getDefaultLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "select_category_prompt");
   }

   /**
    * Use Spring JSF integration to return the category service bean instance
    * 
    * @param context    FacesContext
    * 
    * @return category service bean instance or throws runtime exception if not found
    */
   private static CategoryService getCategoryService(FacesContext context)
   {
      CategoryService service = Repository.getServiceRegistry(context).getCategoryService();
      if (service == null)
      {
         throw new IllegalStateException("Unable to obtain CategoryService bean reference.");
      }
      
      return service;
   }

   /**
    * Returns the parent id of the current category, or null if the parent has the category root type
    * 
    * @see org.alfresco.web.ui.repo.component.AbstractItemSelector#getParentNodeId(javax.faces.context.FacesContext)
    */
   public String getParentNodeId(FacesContext context)
   {
      String id = null;
      
      if (this.navigationId != null)
      {
         ChildAssociationRef parentRef = getNodeService(context).getPrimaryParent(
               new NodeRef(Repository.getStoreRef(), this.navigationId));
         Node parentNode = new Node(parentRef.getParentRef());
         
         DictionaryService dd = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getDictionaryService();
         
         if (dd.isSubClass(parentNode.getType(), ContentModel.TYPE_CATEGORYROOT) == false)
         {
            id = parentRef.getParentRef().getId();
         }
      }
      
      return id;
   }

   /**
    * Returns the child categories of the current navigation node
    * 
    * @see org.alfresco.web.ui.repo.component.AbstractItemSelector#getChildrenForNode(javax.faces.context.FacesContext)
    */
   public Collection<NodeRef> getChildrenForNode(FacesContext context)
   {
      NodeRef nodeRef = new NodeRef(Repository.getStoreRef(), this.navigationId);
      
      Collection<ChildAssociationRef> childRefs = getCategoryService(context).getChildren(nodeRef, 
            CategoryService.Mode.SUB_CATEGORIES, CategoryService.Depth.IMMEDIATE);
      Collection<NodeRef> refs = new ArrayList<NodeRef>(childRefs.size());
      for (ChildAssociationRef childRef : childRefs)
      {
         refs.add(childRef.getChildRef());
      }
      
      return refs;
   }

   /**
    * Returns the root categories
    * 
    * @see org.alfresco.web.ui.repo.component.AbstractItemSelector#getRootChildren(javax.faces.context.FacesContext)
    */
   public Collection<NodeRef> getRootChildren(FacesContext context)
   {
      Collection<ChildAssociationRef> childRefs = getCategoryService(context).getCategories(
            Repository.getStoreRef(), ContentModel.ASPECT_GEN_CLASSIFIABLE, Depth.IMMEDIATE);
      Collection<NodeRef> refs = new ArrayList<NodeRef>(childRefs.size());
      for (ChildAssociationRef childRef : childRefs)
      {
         refs.add(childRef.getChildRef());
      }
      
      return refs;
   }
   
   /**
    * @see org.alfresco.web.ui.repo.component.AbstractItemSelector#getItemIcon()
    */
   public String getItemIcon()
   {
      return WebResources.IMAGE_CATEGORY;
   }
}
