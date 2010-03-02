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
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;

/**
 * Implementation for the edit node categories dialog.
 * 
 * @author gavinc
 */
public class EditNodeCategoriesDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -1879847736997772606L;
   
   protected Node node;
   protected NodeRef addedCategory;
   protected List categories;
   protected String description;

   private static final String MSG_ERROR_UPDATE_CATEGORY = "error_update_category";
   private static final String MSG_MODIFY_CATEGORIES_OF = "modify_categories_of";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // reset variables
      this.categories = null;
      this.addedCategory = null;
      
      // retrieve parameters
      String nodeRef = parameters.get("nodeRef");
      
      // make sure nodeRef was supplied
      ParameterCheck.mandatoryString("nodeRef", nodeRef);
      
      // create the node
      this.node = new Node(new NodeRef(nodeRef));
      
      // determine description for dialog
      FacesContext context = FacesContext.getCurrentInstance();
      if (getDictionaryService().isSubClass(this.node.getType(), ContentModel.TYPE_FOLDER))
      {
         this.description = Application.getMessage(context, "editcategory_space_description"); 
      }
      else
      {
         this.description = Application.getMessage(context, "editcategory_description");
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // firstly retrieve all the properties for the current node
      Map<QName, Serializable> updateProps = getNodeService().getProperties(this.node.getNodeRef());

      // create a node ref representation of the selected id and set the new properties
      updateProps.put(ContentModel.PROP_CATEGORIES, (Serializable) categories);

      // set the properties on the node
      getNodeService().setProperties(this.node.getNodeRef(), updateProps);

      return outcome;
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }

   @Override
   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return Application.getMessage(fc, MSG_MODIFY_CATEGORIES_OF) + 
            " " + Application.getMessage(fc, MSG_LEFT_QUOTE) + this.node.getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }
   
   @Override
   public String getContainerDescription()
   {
      return this.description;
   }
   
   @Override
   protected String getErrorMessageId()
   {
      return MSG_ERROR_UPDATE_CATEGORY;
   }

   // ------------------------------------------------------------------------------
   // Bean property getters and setters

   /**
    * Returns a Map of the initial categories on the node keyed by the NodeRef
    *
    * @return Map of initial categories
    */
   public List getCategories()
   {
      if (this.categories == null)
      {
         // get the list of categories
         this.categories = (List)getNodeService().getProperty(this.node.getNodeRef(),
               ContentModel.PROP_CATEGORIES);
      }
      
      return this.categories;
   }

   /**
    * Sets the categories Map
    *
    * @param categories
    */
   public void setCategories(List categories)
   {
      this.categories = categories;
   }

   /**
    * Returns the last category added from the multi value editor
    *
    * @return The last category added
    */
   public NodeRef getAddedCategory()
   {
      return this.addedCategory;
   }

   /**
    * Sets the category added from the multi value editor
    *
    * @param addedCategory The added category
    */
   public void setAddedCategory(NodeRef addedCategory)
   {
      this.addedCategory = addedCategory;
   }
}
