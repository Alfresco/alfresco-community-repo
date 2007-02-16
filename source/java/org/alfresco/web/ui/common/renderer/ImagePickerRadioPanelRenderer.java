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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.web.ui.common.PanelGenerator;

/**
 * Renderer for the image picker component that outputs the list of images
 * as radio buttons within a rounded corner panel
 * 
 * @author gavinc
 */
public class ImagePickerRadioPanelRenderer extends ImagePickerRadioRenderer
{
   /**
    * @see javax.faces.render.Renderer#encodeBegin(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void encodeBegin(FacesContext context, UIComponent component) throws IOException
   {
      if (component.isRendered() == false)
      {
         return;
      }

      // output the start of the surrounding rounded corner panel
      PanelGenerator.generatePanelStart(context.getResponseWriter(), 
            context.getExternalContext().getRequestContextPath(),
            (String)component.getAttributes().get("panelBorder"), 
            (String)component.getAttributes().get("panelBgcolor"));
      
      super.encodeBegin(context, component);
   }
   
   /**
    * @see javax.faces.render.Renderer#encodeEnd(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void encodeEnd(FacesContext context, UIComponent component) throws IOException
   {
      if (component.isRendered() == false)
      {
         return;
      }

      super.encodeEnd(context, component);
      
      // output the end of the surrounding rounded corner panel
      PanelGenerator.generatePanelEnd(context.getResponseWriter(),
            context.getExternalContext().getRequestContextPath(),
            (String)component.getAttributes().get("panelBorder"));
   }
}
 