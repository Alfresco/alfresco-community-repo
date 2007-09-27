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
package org.alfresco.web.bean.categories;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * Implementation for the edit node categories dialog.
 * 
 * @author gavinc
 */
public class EditNodeCategoriesDialog extends BaseDialogBean
{
   protected Node node;
   protected NodeRef addedCategory;
   protected List categories;
   protected String description;

   private static final String MSG_ERROR_UPDATE_CATEGORY = "error_update_category";
   private static final String MSG_MODIFY_CATEGORIES_OF = "modify_categories_of";
   
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
      if (this.dictionaryService.isSubClass(this.node.getType(), ContentModel.TYPE_FOLDER))
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
      Map<QName, Serializable> updateProps = this.nodeService.getProperties(this.node.getNodeRef());

      // create a node ref representation of the selected id and set the new properties
      updateProps.put(ContentModel.PROP_CATEGORIES, (Serializable) categories);

      // set the properties on the node
      this.nodeService.setProperties(this.node.getNodeRef(), updateProps);

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
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_MODIFY_CATEGORIES_OF) + 
            " '" + this.node.getName() + "'";
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
         this.categories = (List)this.nodeService.getProperty(this.node.getNodeRef(),
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
