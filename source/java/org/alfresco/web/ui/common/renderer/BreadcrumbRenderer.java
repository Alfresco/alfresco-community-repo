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
package org.alfresco.web.ui.common.renderer;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.common.component.UIBreadcrumb;

/**
 * Renderer class for the UIBreadcrumb component
 * 
 * @author Kevin Roast
 */
public class BreadcrumbRenderer extends BaseRenderer
{
   // ------------------------------------------------------------------------------
   // Renderer implementation 
   
   /**
    * @see javax.faces.render.Renderer#decode(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void decode(FacesContext context, UIComponent component)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      String fieldId = getHiddenFieldName(context, component);
      String value = (String)requestMap.get(fieldId);
      if (value != null && value.length() != 0)
      {
         // create a breadcrumb specific action event if we were clicked
         int selectedIndex = Integer.parseInt(value);
         UIBreadcrumb.BreadcrumbEvent event = new UIBreadcrumb.BreadcrumbEvent(component, selectedIndex);
         component.queueEvent(event);
      }
   }

   /**
    * @see javax.faces.render.Renderer#encodeBegin(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void encodeBegin(FacesContext context, UIComponent component) throws IOException
   {
      // always check for this flag - as per the spec
      if (component.isRendered() == true)
      {
         Writer out = context.getResponseWriter();
         
         UIBreadcrumb breadcrumb = (UIBreadcrumb)component;
         // get the List of IBreadcrumbHandler elements from the component
         List<IBreadcrumbHandler> elements = (List)breadcrumb.getValue();
         
         boolean first = true;
         for (int index=0; index<elements.size(); index++)
         {
            IBreadcrumbHandler element = elements.get(index);
            
            // handle not optionally hiding the root part
            if (index != 0 || breadcrumb.getShowRoot() == true)
            {
               out.write( renderBreadcrumb(context, breadcrumb, element.toString(), index, first) );
               first = false;
            }
         }
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Private helpers
   
   /**
    * Render a single breadcrumb element as a link on the page
    * 
    * @param context    FacesContext
    * @param bc         The current UIBreadcrumb component
    * @param element    Text for the breadcrumb element
    * @param index      The index of the element into the original breadcrumb path
    * @param first      True is this is the root element
    * 
    * @return HTML for this breadcrumb element
    */
   private String renderBreadcrumb(FacesContext context, UIBreadcrumb bc, String element, int index, boolean first)
   {
      // render breadcrumb link element
      StringBuilder buf = new StringBuilder(200);
      
      // output separator
      if (first == false)
      {
         buf.append(' ')
            .append(Utils.encode(bc.getSeparator()))
            .append(' ');
      }
      
      // generate JavaScript to set a hidden form field and submit
      // a form which request attributes that we can decode
      buf.append("<a href='#' onclick=\"");
      buf.append(Utils.generateFormSubmit(context, bc, getHiddenFieldName(context, bc), Integer.toString(index)));
      buf.append('"');
      
      if (bc.getAttributes().get("style") != null)
      {
         buf.append(" style=\"")
            .append(bc.getAttributes().get("style"))
            .append('"');
      }
      if (bc.getAttributes().get("styleClass") != null)
      {
         buf.append(" class=\"")
            .append(bc.getAttributes().get("styleClass"))
            .append('"');
      }
      if (bc.getAttributes().get("tooltip") != null)
      {
         buf.append(" title=\"")
         .append(bc.getAttributes().get("tooltip"))
         .append('"');
      }
      buf.append('>');
      
      // output path element text
      // TODO: optionally crop text length with ellipses - use title attribute for all
      buf.append(Utils.encode(element));
      
      // close tag
      buf.append("</a>");
      
      return buf.toString();
   }
   
   /**
    * Get the hidden field name for this breadcrumb.
    * Assume there will not be many breadcrumbs on a page - therefore a hidden field
    * for each is not a significant issue.
    * 
    * @return hidden field name
    */
   private static String getHiddenFieldName(FacesContext context, UIComponent component)
   {
      return component.getClientId(context);
   }
}
