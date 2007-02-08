/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.ui.repo.tag;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.BaseComponentTag;

/**
 * @author Kevin Roast
 */
public class TemplateTag extends BaseComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.Template";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      // self rendering component
      return null;
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      setStringProperty(component, "engine", this.engine);
      setStringProperty(component, "template", this.template);
      setStringProperty(component, "templatePath", this.templatePath);
      setStringBindingProperty(component, "model", this.model);
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      
      this.engine = null;
      this.template = null;
      this.templatePath = null;
      this.model = null;
   }
   
   /**
    * Set the engine name
    *
    * @param engine     the engine
    */
   public void setEngine(String engine)
   {
      this.engine = engine;
   }

   /**
    * Set the template name
    *
    * @param template     the template
    */
   public void setTemplate(String template)
   {
      this.template = template;
   }
   
   /**
    * Set the template name based path
    *
    * @param templatePath     the template name based path
    */
   public void setTemplatePath(String templatePath)
   {
      this.templatePath = templatePath;
   }

   /**
    * Set the data model
    *
    * @param model     the model
    */
   public void setModel(String model)
   {
      this.model = model;
   }


   /** the template name based path */
   private String templatePath;

   /** the engine name */
   private String engine;

   /** the template */
   private String template;

   /** the data model */
   private String model;
}
