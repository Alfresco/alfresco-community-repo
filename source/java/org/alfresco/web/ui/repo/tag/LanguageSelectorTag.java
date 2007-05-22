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
package org.alfresco.web.ui.repo.tag;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.tag.HtmlComponentTag;
import org.alfresco.web.ui.repo.component.UILanguageSelector;
import org.alfresco.web.ui.repo.component.UIMimeTypeSelector;

/**
 * Tag class for the Language selector component
 * 
 * @author yanipig
 */
public class LanguageSelectorTag extends HtmlComponentTag
{
   /** The value */
   private String value;
      
   /** Whether the component is disabled */
   private String disabled;
   
   /** 
    *  If the value is 'true', only the missing translations of the node will be return
    *  Else returns all the languages  
    */
   private String onlyAvailable;
   
   /**
    * If the value is 'true', the language of the node will be returned.
    * 
    * Without effect if <code>onlyAvailable</code> is 'false'
    */
   private String returnCurLgge;
   
   
   @Override
   public String getComponentType()
   {
      return UILanguageSelector.COMPONENT_TYPE;
   }

   @Override
   public String getRendererType()
   {
      return ComponentConstants.JAVAX_FACES_MENU;
   }
   
   @Override
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      setStringBindingProperty(component, "value", this.value);
      setBooleanProperty(component, "disabled", this.disabled);
      
      if(onlyAvailable != null && "true".equalsIgnoreCase(onlyAvailable))
      {
         UILanguageSelector lggSelector = (UILanguageSelector) getComponentInstance();
         lggSelector.setOnlyAvailableLanguages(true);
         
         if(returnCurLgge != null && "true".equalsIgnoreCase(returnCurLgge))
           {
              lggSelector.setReturnCurrentLanguage(true);
           }
      }
      
   }
   
   @Override
   public void release()
   {
      super.release();
      
      this.value = null;
      this.disabled = null;
      this.onlyAvailable = null;
   }
   
   /**
    * Set the value
    *
    * @param value     the value
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   /**
    * Sets whether the component should be rendered in a disabled state
    * 
    * @param disabled true to render the component in a disabled state
    */
   public void setDisabled(String disabled)
   {
      this.disabled = disabled;
   }

   /**
    * Sets whether the component should returns each language or only the available
    * translations of the cuyrrent node.
    * 
    * @param onlyAvailable
    */
   public void setOnlyAvailable(String onlyAvailable) 
   {
      this.onlyAvailable = onlyAvailable;
   }

   public String getOnlyAvailable() 
   {
      return onlyAvailable;
   }

   public String getReturnCurLgge() {
      return returnCurLgge;
   }

   /**
    * Sets whether the component should returns the language of the node 
     * 
    * Without effect if <code>onlyAvailable</code> is false.
    * 
    * @param returnCurLgge
    */
   public void setReturnCurLgge(String returnCurLgge) {
      this.returnCurLgge = returnCurLgge;
   }
}
