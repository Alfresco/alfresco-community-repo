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
 