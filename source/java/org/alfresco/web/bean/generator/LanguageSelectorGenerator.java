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
package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItems;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.users.UserPreferencesBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;
import org.alfresco.web.ui.repo.converter.LanguageConverter;

/**
 * Generates a LANGUAGE selector component.
 * 
 * @author Yannick Pignot
 */
public class LanguageSelectorGenerator extends BaseComponentGenerator
{
   protected Node node;
   
   protected UserPreferencesBean userPreferencesBean;
   
   @SuppressWarnings("unchecked")
   public UIComponent generate(FacesContext context, String id)
   {
         UIComponent component = context.getApplication().
               createComponent(UISelectOne.COMPONENT_TYPE);
         FacesHelper.setupComponentId(context, component, id);        
         
         // create the list of choices
          UISelectItems itemsComponent = (UISelectItems)context.getApplication().
             createComponent("javax.faces.SelectItems");
         
          itemsComponent.setValue(getLanguageItems());
          
          // add the items as a child component
          component.getChildren().add(itemsComponent);
          
         return component;
    }
        
   protected SelectItem[] getLanguageItems()
   {
       SelectItem[] items = userPreferencesBean.getAvailablesContentFilterLanguages(node.getNodeRef(), true);
         
         return items;
   }
   
   @Override
   protected UIComponent createComponent(FacesContext context, UIPropertySheet propertySheet, PropertySheetItem item) {
      
      this.node = propertySheet.getNode();
       
      return super.createComponent(context, propertySheet, item);
   }
    
    @Override
    protected void setupConverter(FacesContext context, 
            UIPropertySheet propertySheet, PropertySheetItem property, 
            PropertyDefinition propertyDef, UIComponent component)
    {        
          createAndSetConverter(context, LanguageConverter.CONVERTER_ID, component);
    }
      
    @Override
    protected void setupMandatoryValidation(FacesContext context, 
            UIPropertySheet propertySheet, PropertySheetItem item, 
            UIComponent component, boolean realTimeChecking, String idSuffix)
    {
         // null is may be a right value for a language
    }

   /**
    * Set the injected userPreferencesBean
    * 
    * @param userPreferencesBean
    */
   public void setUserPreferencesBean(UserPreferencesBean userPreferencesBean) 
   {
      this.userPreferencesBean = userPreferencesBean;
   }
}