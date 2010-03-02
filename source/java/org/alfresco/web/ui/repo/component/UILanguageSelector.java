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
package org.alfresco.web.ui.repo.component;

import java.io.IOException;


import javax.faces.component.UISelectItems;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.users.UserPreferencesBean;
import org.alfresco.web.bean.users.SpaceUsersBean;

/**
 * Component that holds a list of languages avalaiable to make a node multilingual.
 * 
 * 
 * @author Yannick Pignot
 */
public class UILanguageSelector extends UISelectOne
{
   public static final String COMPONENT_TYPE = "org.alfresco.faces.LanguageSelector";
   public static final String COMPONENT_FAMILY = "javax.faces.SelectOne";
   
   // If true, the langage list is filtered to return all the langages yet available in the 
   // MLContainer of the current node. 
   // An available langage is a language where any translation is set.  
   private boolean onlyAvailableLanguages = false;

   // If true and if onlyAvailableLanguages, the list of available languages
   // will be return with the language of the node.
   // Used in the edit properties dialog.
   private boolean returnCurrentLanguage  = true;
   
   @Override
   @SuppressWarnings("unchecked")
   public void encodeBegin(FacesContext context) throws IOException
   {
      // if the component does not have any children yet create the
      // list of Languages the user can choose from as a child 
      // SelectItems component.
      if (getChildren().size() == 0)
      {
         UISelectItems items = (UISelectItems) context.getApplication().
               createComponent("javax.faces.SelectItems");
         items.setId(this.getId() + "_items");
         items.setValue(createList());        
         // add the child component
         getChildren().add(items);
      }
      // do the default processing
      super.encodeBegin(context);      
   }

   /**
    * Creates the list of SelectItem components to represent the list
    * of Langages the user can select from
    * 
    * @return List of SelectItem components
    */
   protected SelectItem[] createList()
   {       
         FacesContext fc = FacesContext.getCurrentInstance();       
          
       SpaceUsersBean spaceUserBean = (SpaceUsersBean) FacesHelper.getManagedBean(fc, "SpaceUsersBean");
       UserPreferencesBean userPreferencesBean = (UserPreferencesBean) FacesHelper.getManagedBean(fc, "UserPreferencesBean");
       
       // get the node ref
       NodeRef nodeRef = spaceUserBean.getNode().getNodeRef();
       

       if(this.onlyAvailableLanguages)
       {
          return userPreferencesBean.getAvailablesContentFilterLanguages(nodeRef, this.returnCurrentLanguage);
       }
       else
       {
          return userPreferencesBean.getContentFilterLanguages(false);
       }
      
      
   }

   /**
    * @return true if the list of languages is filtered
    */
   public boolean isOnlyAvailableLanguages() 
   {
      return onlyAvailableLanguages;
   }
   
   /**
    * @param onlyAvailableLanguages the list  of languages is filtered
    */
   public void setOnlyAvailableLanguages(boolean onlyAvailableLanguages) 
   {
      this.onlyAvailableLanguages = onlyAvailableLanguages;
   }

   /**
    * @return true if the list must contain the language of the current node
    */
   public boolean isReturnCurrentLanguage() 
   {
      return returnCurrentLanguage;
   }

   /**
    * Without effect if onlyAvailableLanguages is false 
    * 
    * @param returnCurrentLanguage the list must contain the language of the current node
    * 
    */
   public void setReturnCurrentLanguage(boolean returnCurrentLanguage) 
   {
      this.returnCurrentLanguage = returnCurrentLanguage;
   }
}
