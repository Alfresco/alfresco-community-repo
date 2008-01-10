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