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
package org.alfresco.web.bean.categories;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.CategoryService.Depth;
import org.alfresco.service.cmr.search.CategoryService.Mode;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.alfresco.web.ui.repo.component.IRepoBreadcrumbHandler;

public class EditCategoryDialog extends BaseDialogBean
{
   private static final long serialVersionUID = 3316665625854694278L;
   
   private static final String DEFAULT_OUTCOME = "finish";
   private final static String MSG_EDIT_CATEGORY = "edit_category";
   private final static String MSG_FINISH = "finish_button";
   private static final String MSG_CATEGORIES = "categories";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";

   transient protected CategoryService categoryService;

   /** Action category node */
   private Node actionCategory = null;

   /** Currently visible category Node */
   private Node category = null;
   
   String categoryRef = null;
   
   /** Category path breadcrumb location */
   private List<IBreadcrumbHandler> location = null;

   /** Members of the linked items of a category */
   private Collection<ChildAssociationRef> members = null;

   /** Component references */
   protected UIRichList categoriesRichList;

   /** Dialog properties */
   private String name = null;
   private String description = null;

   @Override
   public void init(Map<String, String> parameters)
   {
      this.isFinished = false;
      
      // retrieve parameters
      categoryRef = parameters.get(CategoriesDialog.PARAM_CATEGORY_REF);
      
      // make sure nodeRef was supplied
      ParameterCheck.mandatoryString(CategoriesDialog.PARAM_CATEGORY_REF, categoryRef);
      
      // create the node
      this.category = new Node(new NodeRef(categoryRef));
      
      setActionCategory(category);
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      // add the category to the request object so it gets picked up by
      // category dialog, this will allow it to be edited in the breadcrumb
      context.getExternalContext().getRequestMap().put(
               CategoriesDialog.KEY_CATEGORY, this.category.getName());
      
      return outcome;
   }
   
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public UIRichList getCategoriesRichList()
   {
      return categoriesRichList;
   }

   public void setCategoriesRichList(UIRichList categoriesRichList)
   {
      this.categoriesRichList = categoriesRichList;
   }

   /**
    * @return the categoryService
    */
   private CategoryService getCategoryService()
   {
      //check for null in cluster environment
      if(categoryService == null)
      {
         categoryService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getCategoryService();
      }
      return categoryService;
   }

   public void setCategoryService(CategoryService categoryService)
   {
      this.categoryService = categoryService;
   }

   public Node getCategory()
   {
      return category;
   }

   public void setCategory(Node category)
   {
      this.category = category;
   }

   public Collection<ChildAssociationRef> getMembers()
   {
      return members;
   }

   public void setMembers(Collection<ChildAssociationRef> members)
   {
      this.members = members;
   }

   @SuppressWarnings("unchecked")
   public void setActionCategory(Node node)
   {
      this.actionCategory = node;

      if (node != null)
      {
         // setup form properties
         setName(node.getName());
         setDescription((String) node.getProperties().get(ContentModel.PROP_DESCRIPTION));
         setMembers(getCategoryService().getChildren(node.getNodeRef(), Mode.MEMBERS, Depth.ANY));
      }
      else
      {
         setName(null);
         setDescription(null);
         Object emptyCollection = Collections.emptyList();
         setMembers((Collection<ChildAssociationRef>) emptyCollection);
      }
   }

   public Node getActionCategory()
   {
      return actionCategory;
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {

      finishEdit();
      return outcome;
   }

   @Override
   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return Application.getMessage(fc, MSG_EDIT_CATEGORY) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) 
               + getActionCategory().getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }

   @Override
   public String getFinishButtonLabel()
   {

      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_FINISH);
   }

   public void setLocation(List<IBreadcrumbHandler> location)
   {
      this.location = location;
   }

   /**
    * @return Breadcrumb location list
    */
   public List<IBreadcrumbHandler> getLocation()
   {
      if (this.location == null)
      {
         List<IBreadcrumbHandler> loc = new ArrayList<IBreadcrumbHandler>(8);
         CategoriesDialog categoriesDialog = new CategoriesDialog();
         loc.add(categoriesDialog.new CategoryBreadcrumbHandler(null, Application.getMessage(FacesContext.getCurrentInstance(), MSG_CATEGORIES)));

         setLocation(loc);
      }
      return this.location;
   }

   public String finishEdit()
   {
      String outcome = DEFAULT_OUTCOME;

      try
      {

         // update the category node
         NodeRef nodeRef = getActionCategory().getNodeRef();
         getNodeService().setProperty(nodeRef, ContentModel.PROP_NAME, getName());

         // ALF-1788 Need to rename the association
         ChildAssociationRef assocRef = getNodeService().getPrimaryParent(nodeRef);
         QName qname = QName.createQName(
                 assocRef.getQName().getNamespaceURI(),
             QName.createValidLocalName(name));
         getNodeService().moveNode(
                 assocRef.getChildRef(),
                 assocRef.getParentRef(),
                 assocRef.getTypeQName(),
                 qname);
         
         // apply the titled aspect - for description
         if (getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_TITLED) == false)
         {
            Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(1, 1.0f);
            titledProps.put(ContentModel.PROP_DESCRIPTION, getDescription());
            getNodeService().addAspect(nodeRef, ContentModel.ASPECT_TITLED, titledProps);
         }
         else
         {
            getNodeService().setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, getDescription());
         }

         // edit the node in the breadcrumb if required
         CategoriesDialog categoriesDialog = new CategoriesDialog();
         List<IBreadcrumbHandler> location = getLocation();
         IBreadcrumbHandler handler = categoriesDialog.new CategoryBreadcrumbHandler(nodeRef, Repository.getNameForNode(getNodeService(), nodeRef));
         location.set(location.size() - 1, handler);
         setCategory(new Node(nodeRef));
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         outcome = null;
         ReportedException.throwIfNecessary(err);
      }

      return outcome;
   }
}
