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
 